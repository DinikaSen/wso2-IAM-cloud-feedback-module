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

import org.apache.abdera.model.Feed;

import java.util.ArrayList;

/**
 * This is the feedback entity object class.
 */
public class Feedback {

    private Integer id;
    private String message;
    private String email;
    private String contactNo;
    private ArrayList<String> tags;
    private String uuid;
    private String timeCreated;

    public Feedback(String message) {
        this.message = message;
    }

    public Feedback(Integer id, String message, String email, String contactNo, ArrayList<String> tags, String uuid, String timeCreated) {
        this.id = id;
        this.message = message;
        this.email = email;
        this.contactNo = contactNo;
        this.tags = tags;
        this.uuid = uuid;
        this.timeCreated = timeCreated;
    }

    public Feedback(Integer id, String message, String email, String contactNo, String uuid, String timeCreated) {
        this.id = id;
        this.message = message;
        this.email = email;
        this.contactNo = contactNo;
        this.uuid = uuid;
        this.timeCreated = timeCreated;
    }

    public Feedback(Integer id, String message, String email, String uuid, String timeCreated) {
        this.id = id;
        this.message = message;
        this.email = email;
        this.uuid = uuid;
        this.timeCreated = timeCreated;
    }

    public Feedback(Integer id, String message, String uuid, String timeCreated) {
        this.id = id;
        this.message = message;
        this.uuid = uuid;
        this.timeCreated = timeCreated;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

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
     * Set the list of tags related with the feedback.
     *
     * @return List of tags
     */
    public ArrayList<String> getTags() {
        return tags;
    }

    /**
     * Set the list of tags corresponding to the feedback.
     *
     * @param tags List of tags
     */
    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    public String getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(String timeCreated) {
        this.timeCreated = timeCreated;
    }
}
