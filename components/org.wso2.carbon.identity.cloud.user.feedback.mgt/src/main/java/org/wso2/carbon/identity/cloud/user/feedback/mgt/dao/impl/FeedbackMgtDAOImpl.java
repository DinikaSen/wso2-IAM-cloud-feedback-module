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
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.constant.FeedbackMgtConstants;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.constant.FeedbackMgtSQLConstants;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.exception.FeedbackManagementClientException;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.exception.FeedbackManagementServerException;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.util.FeedbackConfigParser;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.util.FeedbackExceptionManagementUtil;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.dao.FeedbackMgtDAO;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.exception.FeedbackManagementException;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.model.Feedback;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.constant.FeedbackMgtConstants.ErrorMessages;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.util.JdbcUtils;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;

import java.util.*;

import static org.wso2.carbon.identity.cloud.user.feedback.mgt.constant.FeedbackMgtConstants.ErrorMessages.*;
import static org.wso2.carbon.identity.cloud.user.feedback.mgt.constant.FeedbackMgtConstants.FEEDBACK_SEARCH_LIMIT_PATH;
import static org.wso2.carbon.identity.core.util.LambdaExceptionUtils.rethrowConsumer;

/**
 * This class access the CLD_FEEDBACK and CLD_FEEDBACK_TAGS tables in Feedback database to store, update retrieve
 * and delete feedback entries.
 */
public class FeedbackMgtDAOImpl implements FeedbackMgtDAO {

    private static final Log log = LogFactory.getLog(FeedbackMgtDAOImpl.class);
    private FeedbackConfigParser configParser;

    @Override
    public Feedback insertFeedbackEntry(Feedback userFeedback) throws FeedbackManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.withTransaction(template -> {
                int insertedId = template.executeInsert(FeedbackMgtSQLConstants.INSERT_FEEDBACK_INFO,
                        (preparedStatement -> {
                            preparedStatement.setString(1, userFeedback.getMessage());
                            preparedStatement.setString(2, userFeedback.getEmail());
                            preparedStatement.setString(3, userFeedback.getContactNo());
                            preparedStatement.setString(4, userFeedback.getUuid());
                        }), userFeedback, true);

                if (userFeedback.getTags() != null) {
                    addTags(insertedId, userFeedback.getTags());
                }
                return null;
            });
        } catch (TransactionException e) {
            throw FeedbackExceptionManagementUtil.handleServerException(ErrorMessages.ERROR_CODE_ADD_USER_FEEDBACK,
                    null, e);
        }
        return userFeedback;
    }

    @Override
    public List<Feedback> listFeedbackEntries(String filter, int limit, int offset, String sortBy,
                                              String sortOrder) throws FeedbackManagementException {

        validateAttributesForPagination(offset, limit);
        System.out.println("Offset : " + offset + " limit : " + limit);

        String sortByValidated = validateSortingAttribute(sortBy);
        String sortOrderValidated = validateSortingOrder(sortOrder);

        Pair<String, String> filterExpression = buildFilter(filter);

        if (filterExpression == null) {
            return listFeedbackEntries(limit, offset, sortByValidated, sortOrderValidated);
        }

        String filterAttribute = filterExpression.getLeft();
        String filterResolvedForSQL = resolveSQLFilter(filterExpression.getRight());
        System.out.println("fil attr : " + filterAttribute + " fil resolved : " + filterResolvedForSQL);

        List<Feedback> feedbackResultsList;
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();

        try {
            switch (FeedbackMgtConstants.FilterableAttributes.valueOf(filterAttribute)) {
                case email:
                    String sqlStatementWithSorting =
                            FeedbackMgtSQLConstants.LIST_FEEDBACK_WITH_FILTER + " " + sortByValidated + " " +
                                    sortOrderValidated + " " + FeedbackMgtSQLConstants.LIST_FEEDBACK_PAGINATION_TAIL;
                    System.out.println(sqlStatementWithSorting);
                    try {
                        feedbackResultsList = jdbcTemplate.withTransaction(template -> {
                            List<Feedback> feedbackInfoList =
                                    jdbcTemplate.executeQuery(sqlStatementWithSorting,
                                            (resultSet, rowNumber) -> {
                                                Feedback feedbackResult = new Feedback();
                                                feedbackResult.setId(resultSet.getInt(1));
                                                feedbackResult.setMessage(resultSet.getString(2));
                                                feedbackResult.setEmail(resultSet.getString(3));
                                                feedbackResult.setContactNo(resultSet.getString(4));
                                                feedbackResult.setUuid(resultSet.getString(5));
                                                feedbackResult.setTimeCreated(resultSet.getString(6));
                                                return feedbackResult;
                                            }, preparedStatement -> {
                                                preparedStatement.setString(1, filterResolvedForSQL);
                                                preparedStatement.setInt(2, limit);
                                                preparedStatement.setInt(3, offset);
                                            });

                            if (feedbackInfoList != null) {
                                feedbackInfoList.forEach(rethrowConsumer(feedbackInfo -> {
                                    feedbackInfo.setTags(listTags(feedbackInfo.getId()));
                                }));
                            }
                            return feedbackInfoList;
                        });
                    } catch (TransactionException e) {
                        throw new FeedbackManagementServerException(String.format(ErrorMessages.ERROR_CODE_LIST_FEEDBACK.
                                getMessage(), limit, offset), ErrorMessages.ERROR_CODE_LIST_FEEDBACK.getCode(), e);
                    }
                    return feedbackResultsList;
                case tag:

                    String sqlStatementForTagsWithSorting =
                            FeedbackMgtSQLConstants.LIST_FEEDBACK_WITH_TAGS_FILTER + " " + sortByValidated + " " +
                                    sortOrderValidated + " " + FeedbackMgtSQLConstants.LIST_FEEDBACK_PAGINATION_TAIL;
                    System.out.println(sqlStatementForTagsWithSorting);
                    try {
                        feedbackResultsList = jdbcTemplate.withTransaction(template -> {
                            List<Feedback> feedbackInfoList =
                                    jdbcTemplate.executeQuery(sqlStatementForTagsWithSorting,
                                            (resultSet, rowNumber) -> {
                                                Feedback feedbackResult = new Feedback();
                                                feedbackResult.setId(resultSet.getInt(1));
                                                feedbackResult.setMessage(resultSet.getString(2));
                                                feedbackResult.setEmail(resultSet.getString(3));
                                                feedbackResult.setContactNo(resultSet.getString(4));
                                                feedbackResult.setUuid(resultSet.getString(5));
                                                feedbackResult.setTimeCreated(resultSet.getString(6));
                                                feedbackResult.setTags(
                                                        new ArrayList<>(Arrays.asList(resultSet.getString(7))));
                                                return feedbackResult;
                                            }, preparedStatement -> {
                                                preparedStatement.setString(1, filterResolvedForSQL);
                                                preparedStatement.setInt(2, limit);
                                                preparedStatement.setInt(3, offset);
                                            });

                            return feedbackInfoList;
                        });
                    } catch (TransactionException e) {
                        throw new FeedbackManagementServerException(String.format(ErrorMessages.ERROR_CODE_LIST_FEEDBACK.
                                getMessage(), limit, offset), ErrorMessages.ERROR_CODE_LIST_FEEDBACK.getCode(), e);
                    }
                    return feedbackResultsList;
            }
        } catch (IllegalArgumentException e) {
            throw FeedbackExceptionManagementUtil.handleClientException(FeedbackMgtConstants.ErrorMessages.
                    ERROR_CODE_UNSUPPORTED_FILTER_ATTRIBUTE, filterAttribute, e);
        }
        return null;
    }

    @Override
    public Feedback getFeedbackEntry(String feedbackID) throws FeedbackManagementException {

        Feedback userFeedback;
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            userFeedback = jdbcTemplate.fetchSingleRecord(FeedbackMgtSQLConstants.GET_FEEDBACK_FROM_ID, (resultSet,
                                                                                                         rowNumber) ->
                            new Feedback(resultSet.getInt(1),
                                    resultSet.getString(2),
                                    resultSet.getString(3),
                                    resultSet.getString(4),
                                    resultSet.getString(5),
                                    resultSet.getString(6)),
                    preparedStatement -> preparedStatement.setString(1, feedbackID));
        } catch (DataAccessException e) {
            throw FeedbackExceptionManagementUtil.handleServerException(ErrorMessages.ERROR_CODE_SELECT_FEEDBACK_BY_ID,
                    String.valueOf(feedbackID), e);
        }

        if (userFeedback != null) {
            userFeedback.setTags(listTags(userFeedback.getId()));
        }

        return userFeedback;
    }

    @Override
    public String deleteFeedbackEntry(String feedbackID) throws FeedbackManagementException {

        Integer id = checkResourceExistence(feedbackID);
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.withTransaction(namedTemplate -> {
                namedTemplate.executeUpdate(FeedbackMgtSQLConstants.REMOVE_FEEDBACK, preparedStatement ->
                        preparedStatement.setString(1, feedbackID));
                deleteTags(id, feedbackID);
                if (log.isDebugEnabled()) {
                    log.debug("Successfully deleted the resource.");
                }
                return null;
            });
        } catch (TransactionException e) {
            throw FeedbackExceptionManagementUtil.handleServerException(ErrorMessages.ERROR_CODE_DELETE_FEEDBACK,
                    String.valueOf(feedbackID), e);
        }
        return feedbackID;
    }

    @Override
    public Feedback updateFeedbackEntry(String feedbackID, Feedback feedbackEntry) throws FeedbackManagementException {

        Integer id = checkResourceExistence(feedbackID);
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.withTransaction(namedTemplate -> {
                namedTemplate.executeUpdate(FeedbackMgtSQLConstants.UPDATE_FEEDBACK_INFO, preparedStatement -> {
                    preparedStatement.setString(1, feedbackEntry.getMessage());
                    preparedStatement.setString(2, feedbackEntry.getEmail());
                    preparedStatement.setString(3, feedbackEntry.getContactNo());
                    preparedStatement.setString(4, feedbackID);
                });

                deleteTags(id, feedbackID);
                if (feedbackEntry.getTags() != null) {
                    addTags(id, feedbackEntry.getTags());
                }
                return null;
            });
        } catch (TransactionException e) {
            throw FeedbackExceptionManagementUtil.handleServerException(ErrorMessages.ERROR_CODE_ADD_USER_FEEDBACK,
                    null, e);
        }
        return getFeedbackEntry(feedbackID);
    }

    @Override
    public Integer countListResults(String filter) throws FeedbackManagementException {

        Pair<String, String> filterExpression = buildFilter(filter);
        String filterAttribute = filterExpression.getLeft();
        String filterValue = filterExpression.getRight();

        if (StringUtils.isBlank(filterAttribute) || "*".equals(filterValue)) {
            return countListResults();
        }

        int count;
        String filterQueryValue = resolveSQLFilter(filterValue);
        String sqlStatementWithFilter = generateSqlFilterForCount(filterAttribute);

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            count = jdbcTemplate.fetchSingleRecord(sqlStatementWithFilter, (resultSet, rowNumber) ->
                            resultSet.getInt(1),
                    preparedStatement -> preparedStatement.setString(1, filterQueryValue));
        } catch (DataAccessException e) {
            throw FeedbackExceptionManagementUtil.handleServerException(ErrorMessages.ERROR_CODE_GET_COUNT_WITH_FILTER,
                    filterValue, e);
        }
        return count;

    }

    private Integer checkResourceExistence(String feedbackID) throws FeedbackManagementException {

        Integer id;
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            id = jdbcTemplate.fetchSingleRecord(FeedbackMgtSQLConstants.CHECK_RESOURCE_EXISTS, (resultSet, rowNumber) ->
                            resultSet.getInt(1),
                    preparedStatement -> preparedStatement.setString(1, feedbackID));
            if (id == null) {
                throw new FeedbackManagementClientException(ErrorMessages.ERROR_NOT_FOUND_RESOURCE_ID.getMessage(),
                        ErrorMessages.ERROR_NOT_FOUND_RESOURCE_ID.getCode());
            }
        } catch (DataAccessException e) {
            throw FeedbackExceptionManagementUtil.handleServerException(ErrorMessages.ERROR_CODE_SELECT_FEEDBACK_BY_ID,
                    String.valueOf(feedbackID), e);
        }
        return id;
    }

    private void addTags(int feedbackId, ArrayList<String> tags) throws FeedbackManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.withTransaction(template -> {
                template.executeBatchInsert(FeedbackMgtSQLConstants.STORE_FEEDBACK_TAG, (preparedStatement -> {

                    for (String tag : tags) {
                        preparedStatement.setInt(1, feedbackId);
                        preparedStatement.setString(2, tag);
                        preparedStatement.addBatch();
                    }
                }), null);
                return null;
            });
        } catch (TransactionException e) {
            throw FeedbackExceptionManagementUtil.handleServerException(ErrorMessages.ERROR_CODE_ADD_USER_FEEDBACK_TAGS,
                    null, e);
        }
    }

    private ArrayList<String> listTags(int feedbackId) throws FeedbackManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            ArrayList<String> tags = new ArrayList<>();
            jdbcTemplate.executeQuery(FeedbackMgtSQLConstants.GET_FEEDBACK_TAGS_FROM_ID, ((resultSet, rowNumber) ->
                            tags.add(resultSet.getString(1))),
                    preparedStatement -> preparedStatement.setInt(1, feedbackId));
            return tags;
        } catch (DataAccessException e) {
            throw FeedbackExceptionManagementUtil.handleServerException(ErrorMessages.ERROR_CODE_SELECT_FEEDBACK_BY_ID,
                    String.valueOf(feedbackId), e);
        }
    }

    private void deleteTags(int id, String feedbackUuid) throws FeedbackManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.withTransaction(namedTemplate -> {
                namedTemplate.executeUpdate(FeedbackMgtSQLConstants.REMOVE_FEEDBACK_TAG, preparedStatement ->
                        preparedStatement.setInt(1, id));
                return null;
            });
        } catch (TransactionException e) {
            throw FeedbackExceptionManagementUtil.handleServerException(ErrorMessages.ERROR_CODE_DELETE_FEEDBACK_TAGS,
                    String.valueOf(feedbackUuid), e);
        }
    }

    private Integer countListResults() throws FeedbackManagementException {

        int count = 0;

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            count = jdbcTemplate.fetchSingleRecord(FeedbackMgtSQLConstants.GET_FEEDBACK_COUNT, (resultSet, rowNumber) ->
                    resultSet.getInt(1), null);
        } catch (DataAccessException e) {
            throw FeedbackExceptionManagementUtil.handleServerException(ErrorMessages.ERROR_CODE_GET_COUNT, null, e);
        }
        return count;

    }

    private List<Feedback> listFeedbackEntries(int limit, int offset, String sortBy,
                                              String sortOrder) throws FeedbackManagementException {

        List<Feedback> feedbackResultsList;
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();

        String sqlStatementWithSorting =
                FeedbackMgtSQLConstants.LIST_FEEDBACK_WITHOUT_FILTER + sortBy + " " + sortOrder +
                FeedbackMgtSQLConstants.LIST_FEEDBACK_PAGINATION_TAIL;
        try {
            feedbackResultsList = jdbcTemplate.withTransaction(template -> {
                List<Feedback> feedbackInfoList =
                        jdbcTemplate.executeQuery(sqlStatementWithSorting,
                        (resultSet, rowNumber) -> {
                            Feedback feedbackResult = new Feedback();
                            feedbackResult.setId(resultSet.getInt(1));
                            feedbackResult.setMessage(resultSet.getString(2));
                            feedbackResult.setEmail(resultSet.getString(3));
                            feedbackResult.setContactNo(resultSet.getString(4));
                            feedbackResult.setUuid(resultSet.getString(5));
                            feedbackResult.setTimeCreated(resultSet.getString(6));
                            return feedbackResult;
                        }, preparedStatement -> {
                            preparedStatement.setString(1, sortBy);
                            //preparedStatement.setString(2, sortOrder);
                            preparedStatement.setInt(2, limit);
                            preparedStatement.setInt(3, offset);
                        });

                if (feedbackInfoList != null) {
                    feedbackInfoList.forEach(rethrowConsumer(feedbackInfo -> {
                        feedbackInfo.setTags(listTags(feedbackInfo.getId()));
                    }));
                }
                return feedbackInfoList;
            });
        } catch (TransactionException e) {
            throw new FeedbackManagementServerException(String.format(ErrorMessages.ERROR_CODE_LIST_FEEDBACK.
                    getMessage(), limit, offset), ErrorMessages.ERROR_CODE_LIST_FEEDBACK.getCode(), e);
        }
        return feedbackResultsList;
    }

    /**
     * Validates the offset and limit values for pagination.
     *
     * @param offset Starting index.
     * @param limit  Count value.
     * @throws FeedbackManagementException
     */
    private void validateAttributesForPagination(int offset, int limit) throws FeedbackManagementException {

        if (limit == 0) {
            limit = getDefaultLimitFromConfig();
            if (log.isDebugEnabled()) {
                log.debug("Limit is not defined the request, hence set tp default value: " + limit);
            }
        } else if (offset < 0) {
            throw new FeedbackManagementClientException(String.format(ErrorMessages.ERROR_CODE_INVALID_OFFSET.
                    getMessage()), ErrorMessages.ERROR_CODE_INVALID_OFFSET.getCode());
        }

        if (limit < 0) {
            throw new FeedbackManagementClientException(String.format(ErrorMessages.ERROR_CODE_INVALID_LIMIT.
                    getMessage(), limit, offset), ErrorMessages.ERROR_CODE_INVALID_LIMIT.getCode());
        }
    }

    private String validateSortingAttribute(String sortBy) throws FeedbackManagementException {

        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = FeedbackMgtConstants.DEFAULT_SORT_BY;

        } else if (!isSortableAttribute(sortBy)) {
            throw new FeedbackManagementClientException(String.format(ERROR_CODE_UNSUPPORTED_SORT_BY_ATTRIBUTE.
                    getMessage(), sortBy), ERROR_CODE_UNSUPPORTED_SORT_BY_ATTRIBUTE.getCode());
        }

        return sortBy;
    }

    private String validateSortingOrder(String sortOrder) throws FeedbackManagementException {

        if (sortOrder == null || sortOrder.isEmpty()) {
            sortOrder = FeedbackMgtConstants.DEFAULT_SORT_ORDER;
        } else if (!isValidSortOrder(sortOrder)) {
            throw new FeedbackManagementClientException(String.format(ErrorMessages.ERROR_CODE_INVALID_SORT_ORDER.
                    getMessage(), sortOrder), ErrorMessages.ERROR_CODE_INVALID_SORT_ORDER.getCode());
        }
        return sortOrder;
    }

    private boolean isSortableAttribute(String attribute) {

        return Arrays.stream(FeedbackMgtConstants.SortableAttributes.values()).anyMatch(sortableAttribute -> sortableAttribute.name()
                .equals(attribute));
    }

    private boolean isValidSortOrder(String sortOrder) {

        return Arrays.stream(FeedbackMgtConstants.SortOrderOperators.values()).anyMatch(sortableAttribute -> sortableAttribute.name()
                .equals(sortOrder));
    }

    private int getDefaultLimitFromConfig() {

        int limit = FeedbackMgtConstants.DEFAULT_SEARCH_LIMIT;

        if (configParser.getConfiguration().get(FEEDBACK_SEARCH_LIMIT_PATH) != null) {
            limit = Integer.parseInt(configParser.getConfiguration()
                    .get(FEEDBACK_SEARCH_LIMIT_PATH).toString());
        }
        return limit;
    }

    private Pair<String, String> buildFilter(String filter) throws FeedbackManagementClientException {

        if (StringUtils.isNotBlank(filter)) {
            String[] filterArgs = filter.split(" ");
            if (filterArgs.length == 3) {

                String filterAttribute = filterArgs[0];

                if (isFilterableAttribute(filterAttribute)) {
                    String operation = filterArgs[1];
                    String attributeValue = filterArgs[2];
                    return Pair.of(filterAttribute, generateFilterString(operation, attributeValue));
                } else {
                    throw FeedbackExceptionManagementUtil.handleClientException(FeedbackMgtConstants.ErrorMessages.
                            ERROR_CODE_UNSUPPORTED_FILTER_ATTRIBUTE, filterAttribute);
                }
            } else {
                throw FeedbackExceptionManagementUtil.handleClientException(FeedbackMgtConstants.ErrorMessages.
                        ERROR_CODE_INVALID_FILTER_QUERY, null);
            }
        } else {
            return null;
        }
    }

    private String generateFilterString(String operation, String attributeValue) throws FeedbackManagementClientException {

        String formattedFilter = null;
        try {
            switch (FeedbackMgtConstants.AttributeOperators.valueOf(operation)) {
                case sw:
                    formattedFilter = attributeValue + "*";
                    break;
                case ew:
                    formattedFilter = "*" + attributeValue;
                    break;
                case eq:
                    formattedFilter = attributeValue;
                    break;
                case co:
                    formattedFilter = "*" + attributeValue + "*";
                    break;
            }
        } catch (IllegalArgumentException e) {
            throw FeedbackExceptionManagementUtil.handleClientException(FeedbackMgtConstants.ErrorMessages.
                    ERROR_CODE_UNSUPPORTED_FILTER_OPERATION, operation, e);
        }

        return formattedFilter;
    }

    private boolean isFilterableAttribute(String attribute) {

        return Arrays.stream(FeedbackMgtConstants.FilterableAttributes.values()).anyMatch(filterableAttribute -> filterableAttribute.name()
                .equals(attribute));
    }

    // TODO: Check if really needed
    private String resolveSQLFilter(String filter) {
        String sqlFilter = "%";
        if (StringUtils.isNotBlank(filter)) {
            sqlFilter = filter.trim().replace("*", "%");
        }

        if (this.log.isDebugEnabled()) {
            this.log.debug("Input filter: " + filter + " resolved for SQL filter: " + sqlFilter);
        }

        return sqlFilter;
    }

    private String generateSqlFilterForCount(String filterAttribute) throws FeedbackManagementClientException {


        String sqlQueryPart = "";
        try {
            switch (FeedbackMgtConstants.FilterableAttributes.valueOf(filterAttribute)) {
                case email:
                    sqlQueryPart =
                            FeedbackMgtSQLConstants.GET_FEEDBACK_COUNT + FeedbackMgtConstants.WHERE +
                                    FeedbackMgtConstants.EMAIL + FeedbackMgtConstants.LIKE;
                    break;
                case tag:
                    sqlQueryPart =
                            FeedbackMgtSQLConstants.GET_FEEDBACK_COUNT_WITH_TAGS + FeedbackMgtConstants.WHERE +
                                    FeedbackMgtConstants.TAG + FeedbackMgtConstants.LIKE;
                    break;
            }
        } catch (IllegalArgumentException e) {
            throw FeedbackExceptionManagementUtil.handleClientException(FeedbackMgtConstants.ErrorMessages.
                    ERROR_CODE_UNSUPPORTED_FILTER_ATTRIBUTE, filterAttribute, e);
        }
        System.out.println(sqlQueryPart);
        return sqlQueryPart;

    }
}
