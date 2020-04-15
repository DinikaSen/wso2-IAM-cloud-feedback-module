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

package org.wso2.carbon.identity.cloud.user.feedback.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.constant.FeedbackMgtConstants;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.dao.FeedbackMgtDAO;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.dao.impl.FeedbackMgtDAOImpl;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.exception.FeedbackManagementException;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.model.Feedback;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.util.FeedbackExceptionManagementUtil;

import java.util.List;
import java.util.UUID;

/**
 * Feedback management service implementation.
 */
public class FeedbackManagementServiceImpl implements FeedbackManagementService {

    private static final Log log = LogFactory.getLog(FeedbackManagementServiceImpl.class);
    private static FeedbackManagementServiceImpl feedbackMgtService = new FeedbackManagementServiceImpl();

    /**
     * Private constructor which will not allow to create objects of this class from outside.
     */
    private FeedbackManagementServiceImpl() {

    }

    /**
     * Get FeedbackManagementServiceImpl instance.
     *
     * @return FeedbackManagementServiceImpl instance
     */
    public static FeedbackManagementServiceImpl getInstance() {

        return feedbackMgtService;
    }

    @Override
    public Feedback createFeedbackEntry(Feedback userFeedback) throws FeedbackManagementException {

        userFeedback.setUuid(UUID.randomUUID().toString());
        validateInputParameters(userFeedback);
        FeedbackMgtDAO feedbackMgtDAO = new FeedbackMgtDAOImpl();
        Feedback feedbackResult = feedbackMgtDAO.insertFeedbackEntry(userFeedback);
        if (log.isDebugEnabled()) {
            log.debug("Feedback entry added successfully. ID: " + feedbackResult.getUuid());
        }
        return feedbackResult;
    }

    @Override
    public List<Feedback> listFeedbackEntries(String filter, int limit, int offset, String sortBy,
                                              String sortOrder) throws FeedbackManagementException {

        FeedbackMgtDAO feedbackMgtDAO = new FeedbackMgtDAOImpl();
        List<Feedback> feedbackResults = feedbackMgtDAO.listFeedbackEntries(filter, limit, offset, sortBy, sortOrder);
        if (log.isDebugEnabled()) {
            log.debug("Feedback list retrieved successfully.");
        }
        return feedbackResults;
    }

    @Override
    public Feedback getFeedbackEntry(String feedbackID) throws FeedbackManagementException {

        FeedbackMgtDAO feedbackMgtDAO = new FeedbackMgtDAOImpl();
        Feedback feedbackResult = feedbackMgtDAO.getFeedbackEntry(feedbackID);
        if (log.isDebugEnabled()) {
            log.debug("Feedback entry retrieved successfully. ID: " + feedbackResult.getUuid());
        }
        return feedbackResult;
    }

    @Override
    public void deleteFeedbackEntry(String feedbackID) throws FeedbackManagementException {

        FeedbackMgtDAO feedbackMgtDAO = new FeedbackMgtDAOImpl();
        String deletedId = feedbackMgtDAO.deleteFeedbackEntry(feedbackID);
        if (log.isDebugEnabled()) {
            log.debug("Feedback entry deleted successfully. ID: " + deletedId);
        }
    }

    @Override
    public Feedback updateFeedbackEntry(String feedbackID, Feedback feedbackEntry) throws FeedbackManagementException {

        FeedbackMgtDAO feedbackMgtDAO = new FeedbackMgtDAOImpl();
        Feedback updatedFeedback = feedbackMgtDAO.updateFeedbackEntry(feedbackID, feedbackEntry);
        if (log.isDebugEnabled()) {
            log.debug("Feedback entry updated successfully. ID: " + updatedFeedback.getUuid());
        }
        return updatedFeedback;
    }

    @Override
    public Integer getCountOfFeedbackResults(String filter) throws FeedbackManagementException {

        FeedbackMgtDAO feedbackMgtDAO = new FeedbackMgtDAOImpl();
        Integer resultCount = feedbackMgtDAO.countListResults(filter);
        if (log.isDebugEnabled()) {
            log.debug("Feedback count for given filter : " + resultCount);
        }
        return resultCount;
    }

    /**
     * Validate whether message is available in the feedback object.
     *
     * @param feedback Feedback object
     * @throws FeedbackManagementException
     */
    private void validateInputParameters(Feedback feedback) throws FeedbackManagementException {

        if (feedback.getMessage() == null || feedback.getMessage().isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Feedback message cannot be empty");
            }
            throw FeedbackExceptionManagementUtil
                    .buildClientException(FeedbackMgtConstants.ErrorMessages.ERROR_CODE_FEEDBACK_MESSAGE_REQUIRED,
                            feedback.getUuid());
        }

        if (log.isDebugEnabled()) {
            log.debug("Feedback submission request validated successfully");
        }
    }

}
