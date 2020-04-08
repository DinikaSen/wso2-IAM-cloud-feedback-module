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
 * Constants related to SQL operations.
 */
public class FeedbackMgtSQLConstants {

    public static final String INSERT_FEEDBACK_INFO =
            "INSERT INTO CLD_FEEDBACK (MESSAGE, EMAIL, CONTACT_NO, UUID) VALUES (?,?,?,?)";

    public static final String UPDATE_FEEDBACK_INFO =
            "UPDATE CLD_FEEDBACK SET MESSAGE = ?, EMAIL = ?, CONTACT_NO = ? WHERE ID = ?";

    public static final String STORE_FEEDBACK_TAG =
            "INSERT INTO CLD_FEEDBACK_TAGS (FEEDBACK_ID, TAG) VALUES (?,?)";

    public static final String GET_FEEDBACK_FROM_ID =
            "SELECT ID, MESSAGE, EMAIL, CONTACT_NO, UUID, TIME_CREATED FROM CLD_FEEDBACK WHERE ID = ?";

    public static final String LIST_PAGINATED_SORTED_FEEDBACK_FROM_ID =
            "SELECT ID, MESSAGE, EMAIL, CONTACT_NO, UUID, TIME_CREATED FROM CLD_FEEDBACK ORDER BY ? ? LIMIT ? " +
                    "OFFSET ?";

    public static final String LIST_PAGINATED_FEEDBACK_FROM_ID =
            "SELECT ID, MESSAGE, EMAIL, CONTACT_NO, UUID, TIME_CREATED FROM CLD_FEEDBACK LIMIT ? OFFSET ?";

    public static final String GET_FEEDBACK_TAGS_FROM_ID =
            "SELECT FEEDBACK_ID,TAG FROM CLD_FEEDBACK_TAGS WHERE FEEDBACK_ID = ?";

    public static final String REMOVE_FEEDBACK =
            "DELETE FROM CLD_FEEDBACK WHERE ID = ?";

    public static final String REMOVE_FEEDBACK_TAG =
            "DELETE FROM CLD_FEEDBACK_TAGS WHERE FEEDBACK_ID = ?";

    public static final String CHECK_FEEDBACK_EXISTENCE =
            "SELECT ID FROM CLD_FEEDBACK WHERE ID = ?";
}
