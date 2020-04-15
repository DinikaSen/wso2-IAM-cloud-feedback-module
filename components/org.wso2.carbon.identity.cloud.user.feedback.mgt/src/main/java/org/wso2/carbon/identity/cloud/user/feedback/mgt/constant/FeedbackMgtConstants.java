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

package org.wso2.carbon.identity.cloud.user.feedback.mgt.constant;

/**
 * Constants related to Feedback management configurations.
 */
public class FeedbackMgtConstants {

    public static final String FEEDBACK_MANAGEMENT_CONFIG_XML = "feedback-mgt-config.xml";
    public static final String FEEDBACK_MANAGEMENT_DEFAULT_NAMESPACE = "http://wso2.org/carbon/feedback/management";

    // Database type related constants
    public static final String MY_SQL = "MySQL";
    public static final String H2 = "H2";

    // Database table column names required for filter formation
    public static final String EMAIL = "EMAIL ";
    public static final String TAG = "TAG ";
    public static final String WHERE = "WHERE ";
    public static final String LIKE = "LIKE ?";
    public static final int DEFAULT_SEARCH_LIMIT = 30;
    public static final String DEFAULT_SORT_BY = "ID ";
    public static final String DEFAULT_SORT_ORDER = "ASC";

    /**
     * Error messages.
     */
    public enum ErrorMessages {

        ERROR_CODE_DATABASE_INITIALIZATION("FM-00001", "Error while initializing the feedback " +
                "management data source."),
        ERROR_CODE_BUILDING_CONFIG("FM-00002",
                "Error occurred while building configuration from" + FEEDBACK_MANAGEMENT_CONFIG_XML),

        // Client Errors
        ERROR_NOT_FOUND_RESOURCE_ID("FM-10001", "Resource id: %s is not found."),
        ERROR_CODE_UNSUPPORTED_FILTER_ATTRIBUTE("FM-10002", "Filtering is not supported for the " +
                "given attribute: %s."),
        ERROR_CODE_INVALID_OFFSET("FM-10003", "Requested offset: %s is invalid. Offset value should be " +
                "zero or greater than zero."),
        ERROR_CODE_INVALID_LIMIT("FM-10004", "Requested limit: %s is invalid. Limit value should be " +
                "greater than zero."),
        ERROR_CODE_UNSUPPORTED_SORT_BY_ATTRIBUTE("FM-10005", "Sorting is not supported for the given " +
                "attribute: %s."),
        ERROR_CODE_INVALID_SORT_ORDER("FM-10006", "Sort order: %s is invalid. Only 'asc' and 'desc' " +
                "are supported"),
        ERROR_CODE_INVALID_FILTER_QUERY("FM-10007", "Invalid filter query provided: %s."),
        ERROR_CODE_UNSUPPORTED_FILTER_OPERATION("FM-10008", "Filter operation: %s is not supported."),
        ERROR_CODE_FEEDBACK_MESSAGE_REQUIRED("FM-10009", "Feedback message is required for " +
                "feedback ID: %s."),

        // Server Errors
        ERROR_CODE_ADD_USER_FEEDBACK("FM-15001", "Error occurred while adding the feedback to DB."),
        ERROR_CODE_LIST_FEEDBACK("FM-15002", "Error occurred while listing feedback from DB"),
        ERROR_CODE_SELECT_FEEDBACK_BY_ID("FM-15003", "Error occurred while retrieving feedback from DB " +
                "for the ID: %s."),
        ERROR_CODE_DELETE_FEEDBACK("FM-15004", "Error occurred while deleting feedback from DB for the " +
                "ID: %s."),
        ERROR_CODE_UPDATE_USER_FEEDBACK("FM-15005", "Error occurred while updating feedback to DB for " +
                "the ID: %s."),
        ERROR_CODE_GET_COUNT_WITH_FILTER("FM-15006", "Error occurred while getting count of feedback " +
                "matching the filter %s."),
        ERROR_CODE_ADD_USER_FEEDBACK_TAGS("FM-15007", "Error occurred while adding the user feedback " +
                "tags to the DB"),
        ERROR_CODE_LIST_FEEDBACK_TAGS("FM-15008", "Error occurred while retrieving feedback tags " +
                "from DB"),
        ERROR_CODE_DELETE_FEEDBACK_TAGS("FM-15009", "Error occurred while deleting feedback tags from " +
                "DB for the ID: %s."),
        ERROR_CODE_GET_COUNT_WITHOUT_FILTER("FM-15010", "Error occurred while getting count of " +
                "feedback from the DB");

        private final String code;
        private final String message;

        ErrorMessages(String code, String message) {

            this.code = code;
            this.message = message;
        }

        public String getCode() {

            return code;
        }

        public String getMessage() {

            return message;
        }

        @Override
        public String toString() {

            return code + " : " + message;
        }
    }

    /**
     * Allowed filter operations.
     */
    public enum AttributeOperators {
        co, eq, ew, sw
    }

    /**
     * Filterable attributes.
     */
    public enum FilterableAttributes {
        email, tag
    }

    /**
     * Allowed sorting orders.
     */
    public enum SortOrderOperators {
        asc, desc
    }

    /**
     * Sortable attributes.
     */
    public enum SortableAttributes {
        time_created
    }

}
