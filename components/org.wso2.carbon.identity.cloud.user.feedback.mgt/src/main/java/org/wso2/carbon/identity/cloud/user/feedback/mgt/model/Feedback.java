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

package org.wso2.carbon.identity.cloud.user.feedback.mgt.model;

import java.util.ArrayList;

/**
 * This is the feedback entity object class.
 */
public class Feedback {

    private Integer id;
    private String message;
    private String email;
    private String contactNo;
    private String userId;
    private int tenantId;
    private ArrayList<String> tags;
    private String uuid;
    private String timeCreated;

    public Feedback() {

    }

    public Feedback(Integer id, String message, String email, String contactNo, String userId, int tenantId,
                    String uuid, ArrayList<String> tags) {

        this.id = id;
        this.message = message;
        this.email = email;
        this.contactNo = contactNo;
        this.userId = userId;
        this.tenantId = tenantId;
        this.uuid = uuid;
        this.tags = tags;
    }

    public Feedback(Integer id, String message, String email, String contactNo, String userId, int tenantId,
                    String uuid, String timeCreated) {

        this.id = id;
        this.message = message;
        this.email = email;
        this.contactNo = contactNo;
        this.userId = userId;
        this.tenantId = tenantId;
        this.uuid = uuid;
        this.timeCreated = timeCreated;
    }

    /**
     * Get feedback ID.
     *
     * @return Autoincrement Feedback ID in the database.
     */
    public Integer getId() {

        return id;
    }

    /**
     * Set feedback ID.
     *
     * @param id Autoincrement Feedback ID in the database
     */
    public void setId(Integer id) {

        this.id = id;
    }

    /**
     * Get feedback resource UUID.
     *
     * @return UUID of the feedback resource
     */
    public String getUuid() {

        return uuid;
    }

    /**
     * Get feedback message.
     *
     * @param uuid UUID of the feedback resource
     */
    public void setUuid(String uuid) {

        this.uuid = uuid;
    }

    /**
     * Get feedback message.
     *
     * @return Feedback message
     */
    public String getMessage() {

        return message;
    }

    /**
     * Set feedback message.
     *
     * @param message Feedback message
     */
    public void setMessage(String message) {

        this.message = message;
    }

    /**
     * Get email of the user that submitted the feedback.
     *
     * @return Email of the user
     */
    public String getEmail() {

        return email;
    }

    /**
     * Set email of the user that submitted the feedback.
     *
     * @param email Email of the user
     */
    public void setEmail(String email) {

        this.email = email;
    }

    /**
     * Get contact number of the user that submitted the feedback.
     *
     * @return Contact number of the user
     */
    public String getContactNo() {

        return contactNo;
    }

    /**
     * Set contact number od the user that submitted the feedback.
     *
     * @param contactNo Contact number of the user
     */
    public void setContactNo(String contactNo) {

        this.contactNo = contactNo;
    }

    /**
     * Get the list of tags related to the feedback.
     *
     * @return List of tags
     */
    public ArrayList<String> getTags() {

        return tags;
    }

    /**
     * Set the list of tags related to the feedback.
     *
     * @param tags List of tags
     */
    public void setTags(ArrayList<String> tags) {

        this.tags = tags;
    }

    /**
     * Get the time that the feedback was created.
     *
     * @return Timestamp as a string
     */
    public String getTimeCreated() {

        return timeCreated;
    }

    /**
     * Set the time that the feedback was created.
     *
     * @param timeCreated Timestamp as a string
     */
    public void setTimeCreated(String timeCreated) {

        this.timeCreated = timeCreated;
    }

    /**
     * Get the unique id of the user that submitted the feedback.
     *
     * @return  userId
     */
    public String getUserId() {

        return userId;
    }

    /**
     * Set the unique id of the user that submitted the feedback.
     *
     * @param userId unique id of the user
     */
    public void setUserId(String userId) {

        this.userId = userId;
    }

    /**
     * tenant domain of the user that submitted the feedback.
     *
     * @return  tenant domain name
     */
    public int getTenantId() {

        return tenantId;
    }

    /**
     * Set the tenant domain ID of the user that submitted the feedback.
     *
     * @param tenantId ID of the tenant domain of the user
     */
    public void setTenantId(int tenantId) {

        this.tenantId = tenantId;
    }
}
