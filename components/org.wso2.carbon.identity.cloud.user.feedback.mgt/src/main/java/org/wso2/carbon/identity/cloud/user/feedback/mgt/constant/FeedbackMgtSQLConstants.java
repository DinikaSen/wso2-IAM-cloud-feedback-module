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
            "UPDATE CLD_FEEDBACK SET MESSAGE = ?, EMAIL = ?, CONTACT_NO = ? WHERE UUID = ?";

    public static final String STORE_FEEDBACK_TAG =
            "INSERT INTO CLD_FEEDBACK_TAGS (FEEDBACK_ID, TAG) VALUES (?,?)";

    public static final String GET_FEEDBACK_FROM_ID =
            "SELECT ID, MESSAGE, EMAIL, CONTACT_NO, UUID, TIME_CREATED FROM CLD_FEEDBACK WHERE UUID = ?";

    public static final String LIST_FEEDBACK_WITH_FILTER =
            "SELECT ID, MESSAGE, EMAIL, CONTACT_NO, UUID, TIME_CREATED FROM CLD_FEEDBACK WHERE EMAIL LIKE ? ORDER BY ";

    public static final String LIST_FEEDBACK_WITH_TAGS_FILTER =
            "SELECT CLD_FEEDBACK.ID, MESSAGE, EMAIL, CONTACT_NO, UUID, TIME_CREATED, TAG FROM CLD_FEEDBACK INNER JOIN" +
                    " CLD_FEEDBACK_TAGS ON CLD_FEEDBACK.ID = CLD_FEEDBACK_TAGS.FEEDBACK_ID WHERE TAG LIKE ? ORDER BY ";

    public static final String LIST_FEEDBACK_WITHOUT_FILTER =
            "SELECT ID, MESSAGE, EMAIL, CONTACT_NO, UUID, TIME_CREATED FROM CLD_FEEDBACK ORDER BY ";

    public static final String LIST_FEEDBACK_PAGINATION_TAIL = "LIMIT ? OFFSET ?";

    public static final String GET_FEEDBACK_TAGS_FROM_ID = "SELECT TAG FROM CLD_FEEDBACK_TAGS WHERE FEEDBACK_ID = ?";

    public static final String REMOVE_FEEDBACK = "DELETE FROM CLD_FEEDBACK WHERE UUID = ?";

    public static final String REMOVE_FEEDBACK_TAG = "DELETE FROM CLD_FEEDBACK_TAGS WHERE FEEDBACK_ID = ?";

    public static final String CHECK_RESOURCE_EXISTS = "SELECT ID FROM CLD_FEEDBACK WHERE UUID = ?";

    public static final String GET_FEEDBACK_COUNT = "SELECT COUNT(UUID) FROM CLD_FEEDBACK ";

    public static final String GET_FEEDBACK_COUNT_WITH_TAGS = "SELECT COUNT(UUID) FROM CLD_FEEDBACK INNER JOIN " +
            "CLD_FEEDBACK_TAGS ON CLD_FEEDBACK.ID = CLD_FEEDBACK_TAGS.FEEDBACK_ID ";
}
