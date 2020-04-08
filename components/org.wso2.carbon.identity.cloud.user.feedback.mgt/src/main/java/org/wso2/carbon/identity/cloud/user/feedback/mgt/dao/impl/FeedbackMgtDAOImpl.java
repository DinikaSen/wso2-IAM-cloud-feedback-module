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

package org.wso2.carbon.identity.cloud.user.feedback.mgt.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.exception.FeedbackManagementClientException;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.exception.FeedbackManagementServerException;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.util.FeedbackExceptionManagementUtil;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.dao.FeedbackMgtDAO;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.exception.FeedbackManagementException;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.model.Feedback;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.constant.FeedbackMgtConstants.ErrorMessages;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.util.JdbcUtils;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.identity.cloud.user.feedback.mgt.constant.FeedbackMgtSQLConstants.*;

/**
 * This class access the CLD_FEEDBACK and CLD_FEEDBACK_TAGS tables in Feedback database to store, update retrieve
 * and delete feedback entries.
 */
public class FeedbackMgtDAOImpl implements FeedbackMgtDAO {

    private static final Log log = LogFactory.getLog(FeedbackMgtDAOImpl.class);

    @Override
    public Feedback insertFeedbackEntry(Feedback userFeedback) throws FeedbackManagementException {

//        NamedJdbcTemplate jdbcTemplate = JdbcUtils.getNewNamedTemplate();

        Feedback feedbackResult;
        int insertedID;
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            insertedID = jdbcTemplate.executeInsert(INSERT_FEEDBACK_INFO, (preparedStatement -> {
                preparedStatement.setString(1, userFeedback.getMessage());
                preparedStatement.setString(2, userFeedback.getEmail());
                preparedStatement.setString(3, userFeedback.getContactNo());
                preparedStatement.setString(4, userFeedback.getUuid());
            }), userFeedback, true);
            //TODO::Add new exception type with no string message passed
        } catch (DataAccessException e) {
            throw FeedbackExceptionManagementUtil.handleServerException(ErrorMessages.ERROR_CODE_ADD_USER_FEEDBACK,
                    null , e);
        }
        feedbackResult = new Feedback(insertedID, userFeedback.getMessage(),
                userFeedback.getEmail(), userFeedback.getUuid(), userFeedback.getTimeCreated());
        return feedbackResult;
    }

    @Override
    public List<Feedback> listFeedbackEntries(String filter, int limit, int offset, String sortBy,
                                              String sortOrder) throws FeedbackManagementException {

        if (!StringUtils.isBlank(filter) && !"*".equals(filter)) {
            String filterResolvedForSQL = this.resolveSQLFilter(filter);
            List<Feedback> feedbacks;
            JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();

            try {
                //String query = LIST_PAGINATED_SORTED_FEEDBACK_FROM_ID;
                String query = LIST_PAGINATED_FEEDBACK_FROM_ID;
                feedbacks = jdbcTemplate.executeQuery(query,
                        (resultSet, rowNumber) -> new Feedback(resultSet.getInt(1),
                                resultSet.getString(2),
                                resultSet.getString(3),
                                resultSet.getString(4),
                                resultSet.getString(5),
                                resultSet.getString(6)),
                        preparedStatement -> {
//                            preparedStatement.setString(1, sortBy);
//                            preparedStatement.setString(2, sortOrder);
                            preparedStatement.setInt(1, limit);
                            preparedStatement.setInt(2, offset);
                        });
            } catch (DataAccessException e) {
                throw new FeedbackManagementServerException(String.format(ErrorMessages.ERROR_CODE_LIST_FEEDBACK.
                        getMessage(), limit, offset), ErrorMessages.ERROR_CODE_LIST_FEEDBACK.getCode(), e);
            }
            return feedbacks;
        } else {
            return null;
            //return this.listFeedbackEntries(limit, offset);
        }
    }

    @Override
    public Feedback getFeedbackEntry(int feedbackID) throws FeedbackManagementException {
        Feedback userFeedback;
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            userFeedback = jdbcTemplate.fetchSingleRecord(GET_FEEDBACK_FROM_ID, (resultSet,
                                                                                  rowNumber) ->
                            new Feedback(resultSet.getInt(1),
                                    resultSet.getString(2),
                                    resultSet.getString(3),
                                    resultSet.getString(4),
                                    resultSet.getString(5),
                                    resultSet.getString(6)),
                    preparedStatement -> preparedStatement.setInt(1, feedbackID));
        } catch (DataAccessException e) {
            throw FeedbackExceptionManagementUtil.handleServerException(ErrorMessages.ERROR_CODE_SELECT_FEEDBACK_BY_ID,
                    String.valueOf(feedbackID), e);
        }
        return userFeedback;
    }

    @Override
    public int deleteFeedbackEntry(int feedbackID) throws FeedbackManagementException {
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        //NamedJdbcTemplate jdbcTemplate = JdbcUtils.getNewNamedTemplate();
        try {
            jdbcTemplate.executeUpdate(REMOVE_FEEDBACK, preparedStatement -> preparedStatement.setInt(1, feedbackID));
        } catch (DataAccessException e) {
            throw FeedbackExceptionManagementUtil.handleServerException(ErrorMessages.ERROR_CODE_DELETE_FEEDBACK,
                    String.valueOf(feedbackID), e);
        }
        return feedbackID;

    }

    //TODO
    @Override
    public Feedback updateFeedbackEntry(int feedbackID, Feedback feedbackEntry) throws FeedbackManagementException {
        return null;
    }

    @Override
    public boolean isFeedbackAvailable(int feedbackID) throws FeedbackManagementException {

        boolean isAvailable = false;

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            Integer id = jdbcTemplate.fetchSingleRecord(CHECK_FEEDBACK_EXISTENCE, (resultSet, rowNumber) ->
                            resultSet.getInt(1),
                    preparedStatement -> preparedStatement.setInt(1, feedbackID));
            if (id != null) {
                isAvailable = true;
            }
        } catch (DataAccessException e) {
            throw FeedbackExceptionManagementUtil.handleServerException(ErrorMessages.ERROR_CODE_SELECT_FEEDBACK_BY_ID,
                    String.valueOf(feedbackID), e);
        }
        return isAvailable;

    }

    private String resolveSQLFilter(String filter) {
        String sqlFilter = "%";
        if (StringUtils.isNotBlank(filter)) {
            sqlFilter = filter.trim().replace("*", "%").replace("?", "_");
        }

        if (this.log.isDebugEnabled()) {
            this.log.debug("Input filter: " + filter + " resolved for SQL filter: " + sqlFilter);
        }

        return sqlFilter;
    }

}
