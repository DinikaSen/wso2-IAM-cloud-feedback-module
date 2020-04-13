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

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class FeedbackTest {

    @Test
    public void testFeedback() throws Exception {

        Integer id = 1;
        String message = "This is a sample feedback";
        String email = "kim@gmail.com";
        String contactNo = "0123456789";
        String uuid = UUID.randomUUID().toString();
        String timeCreated = "2020-01-01 10:00:00";
        ArrayList<String > tags = new ArrayList<>(Arrays.asList("tag1", "tag2", "tag3"));

        Feedback feedback = new Feedback(id, message, email, contactNo, uuid, timeCreated);

        Assert.assertEquals(feedback.getMessage(), message);
        Assert.assertEquals(feedback.getEmail(), email);
        Assert.assertEquals(feedback.getContactNo(), contactNo);
        Assert.assertEquals(feedback.getUuid(), uuid);
        Assert.assertEquals(feedback.getTimeCreated(), timeCreated);

        feedback = new Feedback(uuid);

        Assert.assertEquals(feedback.getUuid(), uuid);

        feedback = new Feedback();
        feedback.setId(id);
        feedback.setMessage(message);
        feedback.setEmail(email);
        feedback.setContactNo(contactNo);
        feedback.setUuid(uuid);
        feedback.setTimeCreated(timeCreated);
        feedback.setTags(tags);

        Assert.assertEquals(feedback.getId(), id);
        Assert.assertEquals(feedback.getMessage(), message);
        Assert.assertEquals(feedback.getEmail(), email);
        Assert.assertEquals(feedback.getContactNo(), contactNo);
        Assert.assertEquals(feedback.getUuid(), uuid);
        Assert.assertEquals(feedback.getTimeCreated(), timeCreated);
        Assert.assertEquals(feedback.getTags(), tags);
    }
}
