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

import org.wso2.carbon.identity.cloud.user.feedback.mgt.exception.FeedbackManagementException;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.model.Feedback;

import java.util.List;

/**
 * Feedback management service interface.
 */
public interface FeedbackManagementService {

    /**
     * Create a feedback entry.
     *
     * @param userFeedback User feedback
     * @throws FeedbackManagementException
     */
    Feedback createFeedbackEntry(Feedback userFeedback) throws FeedbackManagementException;

    /**
     * Retrieve list of user feedback according to the specified conditions.
     *
     * @param filter    filter condition
     * @param limit     max entries in list
     * @param offset    entries to skip
     * @param sortBy    how to sort the list
     * @param sortOrder order of sorting (ASC/DESC)
     * @return A list of user feedback entries
     * @throws FeedbackManagementException
     */
    List<Feedback> listFeedbackEntries(String filter, int limit, int offset, String sortBy, String sortOrder)
            throws FeedbackManagementException;

    /**
     * Retrieve a feedback entry.
     *
     * @param feedbackID Resource ID
     * @return Feedback entry
     * @throws FeedbackManagementException
     */
    Feedback getFeedbackEntry(String feedbackID) throws FeedbackManagementException;

    /**
     * Delete a feedback entry using the feedback ID.
     *
     * @param feedbackID Resource ID
     * @throws FeedbackManagementException
     */
    void deleteFeedbackEntry(String feedbackID) throws FeedbackManagementException;

    /**
     * Update a feedback entry. using the Feedback ID
     *
     * @param feedbackID    Resource ID
     * @param feedbackEntry Feedback object with updated details
     * @throws FeedbackManagementException
     */
    Feedback updateFeedbackEntry(String feedbackID, Feedback feedbackEntry) throws FeedbackManagementException;

    /**
     * Get the count of Feedback entries that match the given condition.
     *
     * @param filter Condition to filter records
     * @return Number of matching entries
     * @throws FeedbackManagementException
     */
    Integer getCountOfFeedbackResults(String filter) throws FeedbackManagementException;

    /**
     * Get the count of Feedback entries that match the given condition.
     *
     * @param feedbackId    Resource ID
     * @return true if feedback exists by the given ID
     * @throws FeedbackManagementException
     */
    boolean checkIfFeedbackExistsById(String feedbackId) throws FeedbackManagementException;
}
