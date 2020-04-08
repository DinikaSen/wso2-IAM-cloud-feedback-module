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
import org.wso2.carbon.identity.cloud.user.feedback.mgt.exception.FeedbackManagementClientException;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.exception.FeedbackManagementException;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.model.Feedback;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.util.FeedbackConfigParser;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.util.FeedbackExceptionManagementUtil;

import java.util.List;
import java.util.UUID;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.wso2.carbon.identity.cloud.user.feedback.mgt.constant.FeedbackMgtConstants.ErrorMessages.ERROR_CODE_FEEDBACK_MESSAGE_REQUIRED;
import static org.wso2.carbon.identity.cloud.user.feedback.mgt.constant.FeedbackMgtConstants.FEEDBACK_SEARCH_LIMIT_PATH;

/**
 * Feedback management service implementation.
 */
public class FeedbackManagementServiceImpl implements FeedbackManagementService {

    private static final Log log = LogFactory.getLog(FeedbackManagementServiceImpl.class);
    private static FeedbackManagementServiceImpl feedbackMgtService = new FeedbackManagementServiceImpl();
    private static final int DEFAULT_SEARCH_LIMIT = 100;
    private FeedbackConfigParser configParser;

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
        validateInputParameters(userFeedback);
        userFeedback.setUuid(UUID.randomUUID().toString());
        FeedbackMgtDAO feedbackMgtDAO = new FeedbackMgtDAOImpl();
        Feedback feedbackResult = feedbackMgtDAO.insertFeedbackEntry(userFeedback);
        return feedbackResult;
    }

    @Override
    public List<Feedback> listFeedbackEntries(String filter, int limit, int offset, String sortBy,
                                              String sortOrder) throws FeedbackManagementException {
        FeedbackMgtDAO feedbackMgtDAO = new FeedbackMgtDAOImpl();
        List<Feedback> feedbackResults = feedbackMgtDAO.listFeedbackEntries(filter, limit, offset, sortBy, sortOrder);
        return feedbackResults;
    }

    @Override
    public Feedback getFeedbackEntry(int feedbackID) throws FeedbackManagementException {
        FeedbackMgtDAO feedbackMgtDAO = new FeedbackMgtDAOImpl();
        Feedback feedbackResult = feedbackMgtDAO.getFeedbackEntry(feedbackID);
        return feedbackResult;
    }

    @Override
    public void deleteFeedbackEntry(int feedbackID) throws FeedbackManagementException {
        FeedbackMgtDAO feedbackMgtDAO = new FeedbackMgtDAOImpl();
        int deletedId = feedbackMgtDAO.deleteFeedbackEntry(feedbackID);
        if (log.isDebugEnabled()) {
            log.debug("Feedback entry deleted successfully. ID: " + deletedId);
        }
    }

    @Override
    public void updateFeedbackEntry(int feedbackID, Feedback feedbackEntry) throws FeedbackManagementException {

    }

    private void validatePaginationParameters(int limit, int offset) throws FeedbackManagementClientException {

        if (limit < 0 || offset < 0) {
            throw FeedbackExceptionManagementUtil.handleClientException(FeedbackMgtConstants.ErrorMessages.
                    ERROR_CODE_INVALID_ARGUMENTS_FOR_LIM_OFFSET, null);
        }
    }

    @Override
    public boolean isFeedbackAvailable(int feedbackID) throws FeedbackManagementException {

        FeedbackMgtDAO feedbackMgtDAO = new FeedbackMgtDAOImpl();
        return feedbackMgtDAO.isFeedbackAvailable(feedbackID);
    }

    private void validateInputParameters(Feedback feedback) throws FeedbackManagementException {

        if (isBlank(feedback.getMessage())) {
            if (log.isDebugEnabled()) {
                log.debug("Feedback message cannot be empty");
            }
            throw FeedbackExceptionManagementUtil.handleClientException(ERROR_CODE_FEEDBACK_MESSAGE_REQUIRED, null);
        }

        if (log.isDebugEnabled()) {
            log.debug("Feedback submission request validation success");
        }
    }

    private int getDefaultLimitFromConfig() {

        int limit = DEFAULT_SEARCH_LIMIT;

        if (configParser.getConfiguration().get(FEEDBACK_SEARCH_LIMIT_PATH) != null) {
            limit = Integer.parseInt(configParser.getConfiguration()
                    .get(FEEDBACK_SEARCH_LIMIT_PATH).toString());
        }
        return limit;
    }
}
