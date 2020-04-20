/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.cloud.user.feedback.mgt.dao.impl;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.dao.FeedbackMgtDAO;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.exception.FeedbackManagementClientException;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.internal.FeedbackManagementServiceDataHolder;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.model.Feedback;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.identity.cloud.user.feedback.mgt.util.TestUtils.closeH2Base;
import static org.wso2.carbon.identity.cloud.user.feedback.mgt.util.TestUtils.getConnection;
import static org.wso2.carbon.identity.cloud.user.feedback.mgt.util.TestUtils.initiateH2Base;
import static org.wso2.carbon.identity.cloud.user.feedback.mgt.util.TestUtils.mockServiceDataHolder;
import static org.wso2.carbon.identity.cloud.user.feedback.mgt.util.TestUtils.spyConnection;

@PrepareForTest({FeedbackManagementServiceDataHolder.class})
public class FeedbackMgtDAOImplTest extends PowerMockTestCase {

    private static List<Feedback> feedbackEntries = new ArrayList<>();

    @BeforeMethod
    public void setUp() throws Exception {

        initiateH2Base();

        Feedback feedback1 = new Feedback(1, "Sample message 1", "kim@abc.com", "0112222222",
                UUID.randomUUID().toString(), 1, UUID.randomUUID().toString(),
                new ArrayList<>(Arrays.asList("issue", "report a bug", "improve")));

        Feedback feedback2 = new Feedback(2, "Sample message 2", "alex@wso2.com", "0113333333",
                UUID.randomUUID().toString(), 2, UUID.randomUUID().toString(),
                new ArrayList<>(Arrays.asList("bug report", "needs improvement", "issue")));

        Feedback feedback3 = new Feedback(3, "Sample message 3", "kim@abc.com", "0114444444",
                UUID.randomUUID().toString(), 1, UUID.randomUUID().toString(),
                new ArrayList<>(Arrays.asList("improve", "suggestion", "complement", "issue")));

        feedbackEntries.add(feedback1);
        feedbackEntries.add(feedback2);
        feedbackEntries.add(feedback3);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        closeH2Base();
    }

    @DataProvider(name = "feedbackListFilterDataProvider")
    public Object[][] provideListFilterData() {

        return new Object[][]{
                // String filter, int limit, int offset, String sortBy, String sortOrder, int resultSize

                // Test Filter
                {"email eq kim@abc.com", 5, 0, "time_created", "asc", 2},
                {"email co wso2", 5, 0, "time_created", "asc", 1},
                {"email sw kim", 5, 0, "time_created", "asc", 2},
                {"email ew wso2.com", 5, 0, "time_created", "asc", 1},
                {"EMAIL EW wso2.com", 5, 0, "TIME_CREATED", "ASC", 1},
                {"tag eq suggestion", 5, 0, "time_created", "asc", 1},
                {"tag co improve", 5, 0, "time_created", "asc", 3},
                {"tag sw improve", 5, 0, "time_created", "asc", 2},
                {"tag ew bug", 5, 0, "time_created", "asc", 1},
                {null, 5, 0, "time_created", "asc", 3},

                // Test limit
                {null, 5, 0, "time_created", "asc", 3},
                {null, 2, 0, "time_created", "asc", 2},
                {null, 0, 0, "time_created", "asc", 3},
                {"email co @", 1, 0, "time_created", "asc", 1},

                // Test offset
                {null, 5, 2, "time_created", "asc", 1},
                {null, 5, 0, "time_created", "asc", 3},
                {"email co @", 5, 1, "time_created", "asc", 2},

                // Test sorting
                {null, 5, 0, "time_created", "asc", 3},
                {null, 5, 0, "time_created", "ASC", 3},
                {null, 5, 0, "time_created", "desc", 3},
                {null, 5, 0, "time_created", "DESC", 3},
                {null, 5, 0, "time_created", null, 3},
                {null, 5, 0, null, "asc", 3},
                {null, 5, 0, null, null, 3}
        };
    }

    @DataProvider(name = "feedbackListFilterInvalidDataProvider")
    public Object[][] provideListFilterInvalidData() {

        return new Object[][]{
                // String filter, int limit, int offset, String sortBy, String sortOrder

                // Test Filter
                {"email someString kim@abc.com", 5, 0, "time_created", "asc"},
                {"someString ew someString", 5, 0, "time_created", "asc"},
                {"someString", 5, 0, "time_created", "asc"},

                // Test limit
                {null, -1, 0, "time_created", "asc"},

                // Test offset
                {null, 5, -1, "time_created", "asc"},

                // Test sorting
                {null, 5, 0, "time_created", "someString"},
                {null, 5, 0, "someString", "asc"},
        };
    }

    @DataProvider(name = "feedbackListCountDataProvider")
    public Object[][] provideListCountData() {

        return new Object[][]{
                // String filter, int resultCount
                {"email eq kim@abc.com", 2},
                {"EMAIL EQ kim@abc.com", 2},
                {"", 3},
                {null, 3}
        };
    }

    @DataProvider(name = "feedbackListCountInvalidDataProvider")
    public Object[][] provideListCountInvalidData() {

        return new Object[][]{
                // String filter
                {"someString ew someString"},
                {"email someString someString"},
                {"someString"}
        };
    }

    @DataProvider(name = "feedbackUpdateDataProvider")
    public Object[][] provideUpdateData() {

        return new Object[][]{
                // message, email, contactNo, [tags]
                {"Updated message", "kim@wso2.com", "1111111111", new ArrayList<>(Arrays.asList("Updated Tag 1",
                        "Updated tag 2", "Updated tag3"))}
        };
    }

    @Test
    public void testAddFeedback() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockServiceDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            FeedbackMgtDAO feedbackMgtDAO = new FeedbackMgtDAOImpl();
            Feedback feedbackResult = feedbackMgtDAO.insertFeedbackEntry(feedbackEntries.get(0));

            Assert.assertEquals(feedbackResult.getMessage(), feedbackEntries.get(0).getMessage());
            Assert.assertEquals(feedbackResult.getEmail(), feedbackEntries.get(0).getEmail());
            Assert.assertEquals(feedbackResult.getContactNo(), feedbackEntries.get(0).getContactNo());
            Assert.assertEquals(feedbackResult.getUserId(), feedbackEntries.get(0).getUserId());
            Assert.assertEquals(feedbackResult.getTenantId(), feedbackEntries.get(0).getTenantId());
            Assert.assertEquals(feedbackResult.getUuid(), feedbackEntries.get(0).getUuid());
            Assert.assertEquals(feedbackResult.getTags(), feedbackEntries.get(0).getTags());
        }
    }

    @Test
    public void testGetFeedback() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockServiceDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            FeedbackMgtDAO feedbackMgtDAO = new FeedbackMgtDAOImpl();
            Feedback feedbackInsertResult = feedbackMgtDAO.insertFeedbackEntry(feedbackEntries.get(0));
            Assert.assertEquals(feedbackInsertResult.getUuid(), feedbackEntries.get(0).getUuid());

            Feedback feedbackById = feedbackMgtDAO.getFeedbackEntry(feedbackInsertResult.getUuid());

            Assert.assertEquals(feedbackById.getId(), feedbackInsertResult.getId());
            Assert.assertEquals(feedbackById.getMessage(), feedbackInsertResult.getMessage());
            Assert.assertEquals(feedbackById.getEmail(), feedbackInsertResult.getEmail());
            Assert.assertEquals(feedbackById.getContactNo(), feedbackInsertResult.getContactNo());
            Assert.assertEquals(feedbackById.getUuid(), feedbackInsertResult.getUuid());
            Assert.assertEquals(feedbackById.getTags(), feedbackInsertResult.getTags());
        }
    }

    @Test(dataProvider = "feedbackListFilterDataProvider")
    public void testListFeedback(String filter, int limit, int offset, String sortBy, String sortOrder, int resultSize)
            throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockServiceDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            FeedbackMgtDAO feedbackMgtDAO = new FeedbackMgtDAOImpl();

            Feedback feedbackResult1 = feedbackMgtDAO.insertFeedbackEntry(feedbackEntries.get(0));
            Assert.assertEquals(feedbackResult1.getUuid(), feedbackEntries.get(0).getUuid());

            Feedback feedbackResult2 = feedbackMgtDAO.insertFeedbackEntry(feedbackEntries.get(1));
            Assert.assertEquals(feedbackResult2.getUuid(), feedbackEntries.get(1).getUuid());

            Feedback feedbackResult3 = feedbackMgtDAO.insertFeedbackEntry(feedbackEntries.get(2));
            Assert.assertEquals(feedbackResult3.getUuid(), feedbackEntries.get(2).getUuid());

            List<Feedback> feedbackResultsList =
                    feedbackMgtDAO.listFeedbackEntries(filter, limit, offset, sortBy, sortOrder);

            Assert.assertEquals(feedbackResultsList.size(), resultSize);
        }
    }

    @Test(expectedExceptions = FeedbackManagementClientException.class,
            dataProvider = "feedbackListFilterInvalidDataProvider")
    public void testListFeedbackWithException(String filter, int limit, int offset, String sortBy, String sortOrder)
            throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockServiceDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            FeedbackMgtDAO feedbackMgtDAO = new FeedbackMgtDAOImpl();

            Feedback feedbackResult1 = feedbackMgtDAO.insertFeedbackEntry(feedbackEntries.get(0));
            Assert.assertEquals(feedbackResult1.getUuid(), feedbackEntries.get(0).getUuid());

            Feedback feedbackResult2 = feedbackMgtDAO.insertFeedbackEntry(feedbackEntries.get(1));
            Assert.assertEquals(feedbackResult2.getUuid(), feedbackEntries.get(1).getUuid());

            feedbackMgtDAO.listFeedbackEntries(filter, limit, offset, sortBy, sortOrder);

            Assert.fail("Expected: " + FeedbackManagementClientException.class.getName());
        }
    }

    @Test(dataProvider = "feedbackListCountDataProvider")
    public void testListCount(String filter, int resultCount)
            throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockServiceDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            FeedbackMgtDAO feedbackMgtDAO = new FeedbackMgtDAOImpl();

            Feedback feedbackResult1 = feedbackMgtDAO.insertFeedbackEntry(feedbackEntries.get(0));
            Assert.assertEquals(feedbackResult1.getUuid(), feedbackEntries.get(0).getUuid());

            Feedback feedbackResult2 = feedbackMgtDAO.insertFeedbackEntry(feedbackEntries.get(1));
            Assert.assertEquals(feedbackResult2.getUuid(), feedbackEntries.get(1).getUuid());

            Feedback feedbackResult3 = feedbackMgtDAO.insertFeedbackEntry(feedbackEntries.get(2));
            Assert.assertEquals(feedbackResult3.getUuid(), feedbackEntries.get(2).getUuid());

            int feedbackResultsCount = feedbackMgtDAO.countListResults(filter);

            Assert.assertEquals(feedbackResultsCount, resultCount);
        }
    }

    @Test(expectedExceptions = FeedbackManagementClientException.class, dataProvider =
            "feedbackListCountInvalidDataProvider")
    public void testListCountWithException(String filter) throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockServiceDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            FeedbackMgtDAO feedbackMgtDAO = new FeedbackMgtDAOImpl();

            Feedback feedbackResult1 = feedbackMgtDAO.insertFeedbackEntry(feedbackEntries.get(0));
            Assert.assertEquals(feedbackResult1.getUuid(), feedbackEntries.get(0).getUuid());

            Feedback feedbackResult2 = feedbackMgtDAO.insertFeedbackEntry(feedbackEntries.get(1));
            Assert.assertEquals(feedbackResult2.getUuid(), feedbackEntries.get(1).getUuid());

            Feedback feedbackResult3 = feedbackMgtDAO.insertFeedbackEntry(feedbackEntries.get(2));
            Assert.assertEquals(feedbackResult3.getUuid(), feedbackEntries.get(2).getUuid());

            feedbackMgtDAO.countListResults(filter);

            Assert.fail("Expected: " + FeedbackManagementClientException.class.getName());
        }
    }

    @Test
    public void testDeleteFeedback() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockServiceDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            FeedbackMgtDAO feedbackMgtDAO = new FeedbackMgtDAOImpl();

            Feedback feedbackResult = feedbackMgtDAO.insertFeedbackEntry(feedbackEntries.get(0));
            Assert.assertEquals(feedbackResult.getUuid(), feedbackEntries.get(0).getUuid());

            String deletedId = feedbackMgtDAO.deleteFeedbackEntry(feedbackResult.getUuid());

            Assert.assertEquals(deletedId, feedbackResult.getUuid());
        }
    }

    @Test(expectedExceptions = FeedbackManagementClientException.class)
    public void testDeleteFeedbackByInvalidId() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockServiceDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            FeedbackMgtDAO feedbackMgtDAO = new FeedbackMgtDAOImpl();
            feedbackMgtDAO.deleteFeedbackEntry("000");

            Assert.fail("Expected: " + FeedbackManagementClientException.class.getName());
        }
    }

    @Test(dataProvider = "feedbackUpdateDataProvider")
    public void testUpdateFeedback(String message, String email, String contactNo, ArrayList<String> tags)
            throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockServiceDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            Feedback feedbackToBeUpdated = new Feedback();
            feedbackToBeUpdated.setMessage(message);
            feedbackToBeUpdated.setEmail(email);
            feedbackToBeUpdated.setContactNo(contactNo);
            feedbackToBeUpdated.setTags(tags);

            FeedbackMgtDAO feedbackMgtDAO = new FeedbackMgtDAOImpl();
            Feedback feedbackInsertResult = feedbackMgtDAO.insertFeedbackEntry(feedbackEntries.get(0));

            Assert.assertEquals(feedbackInsertResult.getMessage(), feedbackEntries.get(0).getMessage());
            Assert.assertEquals(feedbackInsertResult.getEmail(), feedbackEntries.get(0).getEmail());
            Assert.assertEquals(feedbackInsertResult.getContactNo(), feedbackEntries.get(0).getContactNo());
            Assert.assertEquals(feedbackInsertResult.getUuid(), feedbackEntries.get(0).getUuid());
            Assert.assertEquals(feedbackInsertResult.getTags(), feedbackEntries.get(0).getTags());

            Feedback updatedFeedback = feedbackMgtDAO.updateFeedbackEntry(feedbackInsertResult.getUuid(),
                    feedbackToBeUpdated);

            Assert.assertEquals(updatedFeedback.getMessage(), message);
            Assert.assertEquals(updatedFeedback.getEmail(), email);
            Assert.assertEquals(updatedFeedback.getContactNo(), contactNo);
            Assert.assertEquals(updatedFeedback.getTags(), tags);

        }
    }

    @Test(expectedExceptions = FeedbackManagementClientException.class)
    public void testUpdateFeedbackByInvalidId() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockServiceDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            FeedbackMgtDAO feedbackMgtDAO = new FeedbackMgtDAOImpl();
            feedbackMgtDAO.updateFeedbackEntry("000", new Feedback());

            Assert.fail("Expected: " + FeedbackManagementClientException.class.getName());
        }
    }

    @Test
    public void testIfFeedbackExistsById() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockServiceDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            FeedbackMgtDAO feedbackMgtDAO = new FeedbackMgtDAOImpl();
            Feedback feedback = feedbackMgtDAO.insertFeedbackEntry(feedbackEntries.get(0));
            Assert.assertEquals(feedback.getUuid(), feedbackEntries.get(0).getUuid());

            Integer resultId = feedbackMgtDAO.checkIfFeedbackExists(feedback.getUuid());

            Assert.assertNotNull(resultId);
        }
    }

    @Test
    public void testIfFeedbackExistsByInvalidId() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockServiceDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            FeedbackMgtDAO feedbackMgtDAO = new FeedbackMgtDAOImpl();
            Integer resultId = feedbackMgtDAO.checkIfFeedbackExists("000");

            Assert.assertNull(resultId);
        }
    }
}
