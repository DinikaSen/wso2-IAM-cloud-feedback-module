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

package org.wso2.carbon.identity.cloud.user.feedback.mgt.dao;

import org.wso2.carbon.identity.cloud.user.feedback.mgt.exception.FeedbackManagementException;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.model.Feedback;

import java.util.List;

/**
 * This interface access the data storage layer to store, update, retrieve and delete user feedback entries.
 */
public interface FeedbackMgtDAO {

    // TODO: Implement insertTags
    /**
     * Add a a feedback entry.
     *
     * @param userFeedback User feedback
     * @throws FeedbackManagementException
     */
    Feedback insertFeedbackEntry(Feedback userFeedback) throws FeedbackManagementException;

    /**
     * Retrieve a list of user feedback entries according to the specified conditions.
     *
     * @param count Limit
     * @param startIndex Offset
     * @return A list of user feedback entries
     * @throws FeedbackManagementException
     */
    List<Feedback> listFeedbackEntries(String filter, int limit, int offset, String sortBy,
                                       String sortOrder) throws FeedbackManagementException;

    /**
     * Retrieve a feedback entry.
     *
     * @param feedbackID ID of a feedback entry
     * @return Feedback entry
     * @throws FeedbackManagementException
     */
    Feedback getFeedbackEntry(int feedbackID) throws FeedbackManagementException;

    /**
     * Delete a feedback entry using the feedback ID.
     *
     * @param feedbackID ID of the feedback entry
     * @throws FeedbackManagementException
     */
    int deleteFeedbackEntry(int feedbackID) throws FeedbackManagementException;

    /**
     * Update a feedback entry.
     *
     * @param feedbackID    Previous name of the function library
     * @param feedbackEntry Feedback object with new details
     * @throws FeedbackManagementException
     */
    Feedback updateFeedbackEntry(int feedbackID, Feedback feedbackEntry) throws FeedbackManagementException;


    /**
     * Check existence of a feedback.
     *
     * @param feedbackID    Previous name of the function library
     * @return  isAvailable Existence of the feedback defined by the id.
     * @throws FeedbackManagementException
     */
    boolean isFeedbackAvailable(int feedbackID) throws FeedbackManagementException;
}
