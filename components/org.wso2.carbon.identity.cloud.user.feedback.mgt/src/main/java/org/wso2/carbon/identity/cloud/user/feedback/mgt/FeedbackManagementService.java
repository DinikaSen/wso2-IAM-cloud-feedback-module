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
    Feedback createFeedbackEntry(Feedback userFeedback)
            throws FeedbackManagementException;

    /**
     * Retrieve user feedbacks according to the specified conditions.
     *
     * @param condition filter condition
     * @return A list of user feedback entries
     * @throws FeedbackManagementException
     */
    List<Feedback> listFeedbackEntries(String filter, int limit, int offset, String sortBy,
                                       String sortOrder)
            throws FeedbackManagementException;

    /**
     * Retrieve a feedback entry.
     *
     * @param feedbackID ID of a feedback entry
     * @return Feedback entry
     * @throws FeedbackManagementException
     */
    Feedback getFeedbackEntry(int feedbackID)
            throws FeedbackManagementException;

    /**
     * Delete a feedback entry using the feedback ID.
     *
     * @param feedbackID ID of the feedback entry
     * @throws FeedbackManagementException
     */
    void deleteFeedbackEntry(int feedbackID)
            throws FeedbackManagementException;

    /**
     * Update a feedback entry.
     *
     * @param feedbackID Previous name of the function library
     * @param feedbackEntry Feedback object with new details
     * @throws FeedbackManagementException
     */
    void updateFeedbackEntry(int feedbackID, Feedback feedbackEntry)
            throws FeedbackManagementException;

    /**
     * Check the existence of a feedback entry.
     *
     * @param feedbackID Feedback entry ID.
     * @return Feedback entry existence
     * @throws FeedbackManagementException
     */
    boolean isFeedbackAvailable(int feedbackID) throws FeedbackManagementException;

}
