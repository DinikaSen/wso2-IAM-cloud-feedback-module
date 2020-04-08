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
 * Constants related to Feedback management configurations
 */
public class FeedbackMgtConstants {

    public static final String FEEDBACK_MANAGEMENT_CONFIG_XML = "feedback-mgt-config.xml";
    public static final String FEEDBACK_MANAGEMENT_DEFAULT_NAMESPACE = "http://wso2.org/carbon/feedback/management";
    public static final String FEEDBACK_SEARCH_LIMIT_PATH = "SearchLimits.Feedback";

    // Database type related constants
    public static final String MY_SQL = "MySQL";
    public static final String H2 = "H2";

    public enum ErrorMessages {
        ERROR_CODE_DATABASE_CONNECTION("FM_00001", "Error when getting a database connection object from the Feedback" +
                " data source."),
        ERROR_CODE_DATABASE_INITIALIZATION("FM_00002", "Error while initializing the feedback management data source."),
        ERROR_CODE_DATABASE_QUERY_PERFORMING("FM_00003", "Error in performing Database query: '%s.'"),
        MKMFLKDMLK("FM_00004", "There are more records than one found for query: '%s.'"),
        ERROR_CODE_AUTO_GENERATED_ID_FAILURE("FM_00005", "Creating the record failed with Auto-Generated ID, no ID " +
                "obtained."),
        ERROR_CODE_BUILDING_CONFIG("FM_00006", "Error occurred while building configuration from feedback-mgt-config" +
                ".xml."),
        ERROR_CODE_CREATE_DB_TABLES("FM_00007", "Error while creating database tables for Consent Management."),
        ERROR_CODE_RUN_SQL_QUERY("FM_00008", "Error while executing SQL query: %s"),
        ERROR_CODE_RUN_SQL_SCRIPT("FM_00009", "Error while executing Consent Management database creation script for " +
                "at: %s."),
        ERROR_CODE_UNSUPPORTED_DB("FM_00010", "Unsupported database: %s Database will not be created automatically. " +
                "Please create the database using appropriate database scripts for " +
                "the database."),
        ERROR_CODE_GET_DB_TYPE("FM_00011", "Error while getting the database connection metadata."),
        ERROR_CODE_NO_SQL_SCRIPT("FM_00012", "Could not find the database script at %s."),
        ERROR_CODE_ROLL_BACK_CONNECTION("CM_00089", "Transaction rollback connection error occurred while creating"
                + " database tables for Feedback Management."),
        ERROR_CODE_SELECT_FEEDBACK_BY_ID("CM_00016", "Error occurred while retrieving user feedback from " +
                "DB for the ID: %s."),
        ERROR_CODE_INVALID_ARGUMENTS_FOR_LIM_OFFSET("CM_00023", "Invalid offset requested. Offset value should be zero or greater than zero."),
        ERROR_CODE_PURPOSE_ID_INVALID("CM_00024", "Invalid Feedback ID: %s"),
        ERROR_CODE_LIST_FEEDBACK("CM_00010", "Error occurred while listing feedbacks from DB for " +
                "limit: %s and offset: %s."),
        ERROR_CODE_DELETE_FEEDBACK("CM_00009", "Error occurred while deleting feedback from DB for the ID: %s."),
        ERROR_CODE_FEEDBACK_MESSAGE_REQUIRED("CM_00019", "Purpose name is required."),
        ERROR_CODE_FEEDBACK_ID_REQUIRED("CM_00020", "Purpose ID is required."),
        ERROR_CODE_ADD_USER_FEEDBACK("FM_00015", "Error occurred while adding the user feedback to DB.");

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

}
