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

package org.wso2.carbon.identity.cloud.user.feedback.mgt.util;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.constant.FeedbackMgtConstants;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.exception.FeedbackManagementClientException;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.exception.FeedbackManagementServerException;

/**
 * Feedback management error handling class.
 */
public class FeedbackExceptionManagementUtil {

    /**
     * This method can be used to generate a FeedbackManagementServerException from FeedbackMgtConstants.ErrorMessages
     * object when no exception is thrown.
     *
     * @param error FeedbackManagementConstants.ErrorMessages.
     * @param data  data to replace if message needs to be replaced.
     * @return FeedbackManagementServerException.
     */
    public static FeedbackManagementServerException buildServerException(FeedbackMgtConstants.ErrorMessages error,
                                                                         String data) {

        String message = includeData(error, data);
        return new FeedbackManagementServerException(message, error.getCode());
    }

    /**
     * This method can be used to generate a FeedbackManagementServerException from FeedbackMgtConstants.ErrorMessages
     * object when an exception is thrown and no data needs to be inserted to the error message.
     *
     * @param error FeedbackManagementConstants.ErrorMessages.
     * @param e     Parent exception.
     * @return FeedbackManagementServerException.
     */
    public static FeedbackManagementServerException buildServerException(FeedbackMgtConstants.ErrorMessages error,
                                                                         Throwable e) {

        return new FeedbackManagementServerException(error.getMessage(), error.getCode(), e);
    }

    /**
     * This method can be used to generate a FeedbackManagementServerException from FeedbackMgtConstants.ErrorMessages
     * object when an exception is thrown.
     *
     * @param error FeedbackManagementConstants.ErrorMessages.
     * @param data  data to replace if message needs to be replaced.
     * @param e     Parent exception.
     * @return FeedbackManagementServerException
     */
    public static FeedbackManagementServerException buildServerException(FeedbackMgtConstants.ErrorMessages error,
                                                                         String data, Throwable e) {

        String message = includeData(error, data);
        return new FeedbackManagementServerException(message, error.getCode(), e);
    }

    /**
     * This method can be used to generate a FeedbackManagementClientException from FeedbackMgtConstants.ErrorMessages
     * object when no exception is thrown.
     *
     * @param error FeedbackManagementConstants.ErrorMessages.
     * @param data  data to replace if message needs to be replaced.
     * @return FeedbackManagementClientException.
     */
    public static FeedbackManagementClientException buildClientException(FeedbackMgtConstants.ErrorMessages error,
                                                                         String data) {

        String message = includeData(error, data);

        return new FeedbackManagementClientException(message, error.getCode());
    }

    /**
     * This method can be used to generate a FeedbackManagementClientException from FeedbackMgtConstants.ErrorMessages
     * object when an exception is thrown and no data needs to be inserted to the error message.
     *
     * @param error FeedbackManagementConstants.ErrorMessages.
     * @param e     Parent exception.
     * @return FeedbackManagementClientException.
     */
    public static FeedbackManagementClientException buildClientException(FeedbackMgtConstants.ErrorMessages error,
                                                                         Throwable e) {

        return new FeedbackManagementClientException(error.getMessage(), error.getCode(), e);
    }

    /**
     * This method can be used to generate a FeedbackManagementClientException from FeedbackMgtConstants.ErrorMessages
     * object when an exception is thrown.
     *
     * @param error FeedbackManagementConstants.ErrorMessages.
     * @param data  data to replace if message needs to be replaced.
     * @param e     Parent exception.
     * @return FeedbackManagementClientException
     */
    public static FeedbackManagementClientException buildClientException(FeedbackMgtConstants.ErrorMessages error,
                                                                         String data, Throwable e) {

        String message = includeData(error, data);
        return new FeedbackManagementClientException(message, error.getCode(), e);
    }

    /**
     * Include the data to the error message.
     *
     * @param error FeedbackMgtConstants.ErrorMessage.
     * @param data  data to replace if message needs to be replaced.
     * @return message format with data.
     */
    private static String includeData(FeedbackMgtConstants.ErrorMessages error, String data) {

        String message;
        if (StringUtils.isNotBlank(data)) {
            message = String.format(error.getMessage(), data);
        } else {
            message = error.getMessage();
        }
        return message;
    }
}
