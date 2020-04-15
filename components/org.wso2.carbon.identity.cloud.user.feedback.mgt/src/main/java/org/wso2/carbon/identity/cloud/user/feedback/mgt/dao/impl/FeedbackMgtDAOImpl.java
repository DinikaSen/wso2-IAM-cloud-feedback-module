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
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.constant.FeedbackMgtConstants;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.constant.FeedbackMgtConstants.ErrorMessages;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.constant.FeedbackMgtSQLConstants;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.dao.FeedbackMgtDAO;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.exception.FeedbackManagementClientException;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.exception.FeedbackManagementException;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.model.Feedback;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.util.FeedbackExceptionManagementUtil;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.util.JdbcUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.wso2.carbon.identity.core.util.LambdaExceptionUtils.rethrowConsumer;

/**
 * This class access the CLD_FEEDBACK and CLD_FEEDBACK_TAGS tables in Feedback database to store, update retrieve
 * and delete feedback entries.
 */
public class FeedbackMgtDAOImpl implements FeedbackMgtDAO {

    private static final Log log = LogFactory.getLog(FeedbackMgtDAOImpl.class);

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
            throw FeedbackExceptionManagementUtil.buildServerException(ErrorMessages.ERROR_CODE_ADD_USER_FEEDBACK, e);
        }
        return userFeedback;
    }

    @Override
    public List<Feedback> listFeedbackEntries(String filter, int limit, int offset, String sortBy, String sortOrder)
            throws FeedbackManagementException {

        int limitValidated = validateLimitForPagination(limit);
        int offsetValidated = validateOffsetForPagination(offset);

        String sortByValidated = validateSortingAttribute(sortBy);
        String sortOrderValidated = validateSortingOrder(sortOrder);

        Pair<String, String> filterExpression = buildFilter(filter);

        if (filterExpression == null) {
            return listFeedbackEntries(limitValidated, offsetValidated, sortByValidated, sortOrderValidated);
        }

        String filterAttribute = filterExpression.getLeft();
        String filterResolvedForSQL = filterExpression.getRight();

        List<Feedback> feedbackResultsList;
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();

        try {
            switch (FeedbackMgtConstants.FilterableAttributes.valueOf(filterAttribute.toLowerCase())) {

                case email:
                    String sqlStatementWithSorting =
                            FeedbackMgtSQLConstants.LIST_FEEDBACK_WITH_FILTER + " " + sortByValidated + " " +
                                    sortOrderValidated + " " + FeedbackMgtSQLConstants.LIST_FEEDBACK_PAGINATION_TAIL;
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
                                                preparedStatement.setInt(2, limitValidated);
                                                preparedStatement.setInt(3, offsetValidated);
                                            });

                            if (feedbackInfoList != null) {
                                feedbackInfoList.forEach(rethrowConsumer(feedbackInfo -> {
                                    feedbackInfo.setTags(listTags(feedbackInfo.getId()));
                                }));
                            }
                            return feedbackInfoList;
                        });
                    } catch (TransactionException e) {
                        throw FeedbackExceptionManagementUtil
                                .buildServerException(ErrorMessages.ERROR_CODE_LIST_FEEDBACK, e);
                    }
                    return feedbackResultsList;

                case tag:

                    String sqlStatementForTagsWithSorting =
                            FeedbackMgtSQLConstants.LIST_FEEDBACK_WITH_TAGS_FILTER + " " + sortByValidated + " " +
                                    sortOrderValidated + " " + FeedbackMgtSQLConstants.LIST_FEEDBACK_PAGINATION_TAIL;
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
                        throw FeedbackExceptionManagementUtil
                                .buildServerException(ErrorMessages.ERROR_CODE_LIST_FEEDBACK, e);

                    }
                    return feedbackResultsList;
            }
        } catch (IllegalArgumentException e) {
            throw FeedbackExceptionManagementUtil
                    .buildClientException(ErrorMessages.ERROR_CODE_UNSUPPORTED_FILTER_ATTRIBUTE, filterAttribute, e);
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
            throw FeedbackExceptionManagementUtil
                    .buildServerException(ErrorMessages.ERROR_CODE_SELECT_FEEDBACK_BY_ID, feedbackID, e);
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
                return null;
            });
        } catch (TransactionException e) {
            throw FeedbackExceptionManagementUtil
                    .buildServerException(ErrorMessages.ERROR_CODE_DELETE_FEEDBACK, feedbackID, e);
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
            throw FeedbackExceptionManagementUtil.buildServerException(ErrorMessages.ERROR_CODE_UPDATE_USER_FEEDBACK,
                    feedbackID, e);
        }
        return getFeedbackEntry(feedbackID);
    }

    @Override
    public Integer countListResults(String filter) throws FeedbackManagementException {

        Pair<String, String> filterExpression = buildFilter(filter);

        if (filterExpression == null) {
            return countListResults();
        }

        String filterAttribute = filterExpression.getLeft();
        String filterQueryValue = filterExpression.getRight();

        int count;

        String sqlStatementWithFilter = generateSqlFilterForCount(filterAttribute);

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            count = jdbcTemplate.fetchSingleRecord(sqlStatementWithFilter, (resultSet, rowNumber) ->
                    resultSet.getInt(1), preparedStatement -> preparedStatement.setString(1, filterQueryValue));
        } catch (DataAccessException e) {
            throw FeedbackExceptionManagementUtil
                    .buildServerException(ErrorMessages.ERROR_CODE_GET_COUNT_WITH_FILTER, filter, e);
        }
        return count;

    }

    /**
     * Check whether feedback record by given resource ID exists in the database.
     *
     * @param feedbackId resource Id
     * @return Auto-incrementing id of the feedback record in database
     * @throws FeedbackManagementException
     */
    private Integer checkResourceExistence(String feedbackId) throws FeedbackManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            Integer id = jdbcTemplate.fetchSingleRecord(FeedbackMgtSQLConstants.CHECK_RESOURCE_EXISTS, (resultSet,
                                                                                                        rowNumber) ->
                            resultSet.getInt(1),
                    preparedStatement -> preparedStatement.setString(1, feedbackId));

            if (id == null) {
                throw FeedbackExceptionManagementUtil.buildClientException(ErrorMessages.ERROR_NOT_FOUND_RESOURCE_ID,
                        feedbackId);
            }

            return id;

        } catch (DataAccessException e) {
            throw FeedbackExceptionManagementUtil
                    .buildServerException(ErrorMessages.ERROR_CODE_SELECT_FEEDBACK_BY_ID, feedbackId, e);
        }
    }

    /**
     * Insert tags corresponding to a feedback record in the database.
     *
     * @param feedbackId auto-incrementing ID of the feedback record in the database
     * @throws FeedbackManagementException
     */
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
            throw FeedbackExceptionManagementUtil
                    .buildServerException(ErrorMessages.ERROR_CODE_ADD_USER_FEEDBACK_TAGS, e);
        }
    }

    /**
     * List tags corresponding to a feedback record in the database.
     *
     * @param feedbackId auto-incrementing ID of the feedback record in the database
     * @return List of tags corresponding to the requested feedback record
     * @throws FeedbackManagementException
     */
    private ArrayList<String> listTags(int feedbackId) throws FeedbackManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            ArrayList<String> tags = new ArrayList<>();
            jdbcTemplate.executeQuery(FeedbackMgtSQLConstants.GET_FEEDBACK_TAGS_FROM_ID, ((resultSet, rowNumber) ->
                            tags.add(resultSet.getString(1))),
                    preparedStatement -> preparedStatement.setInt(1, feedbackId));
            return tags;
        } catch (DataAccessException e) {
            throw FeedbackExceptionManagementUtil
                    .buildServerException(ErrorMessages.ERROR_CODE_LIST_FEEDBACK_TAGS, e);
        }
    }

    /**
     * Delete tags corresponding to a feedback record in the database.
     *
     * @param id           auto-incrementing ID of the feedback record in the database
     * @param feedbackUuid feedback resource ID
     * @throws FeedbackManagementException
     */
    private void deleteTags(int id, String feedbackUuid) throws FeedbackManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.withTransaction(namedTemplate -> {
                namedTemplate.executeUpdate(FeedbackMgtSQLConstants.REMOVE_FEEDBACK_TAG, preparedStatement ->
                        preparedStatement.setInt(1, id));
                return null;
            });
        } catch (TransactionException e) {
            throw FeedbackExceptionManagementUtil
                    .buildServerException(ErrorMessages.ERROR_CODE_DELETE_FEEDBACK_TAGS, feedbackUuid, e);
        }
    }

    /**
     * Get the count of Feedback entries when no filter is given.
     *
     * @return Number of matching entries
     * @throws FeedbackManagementException
     */
    private Integer countListResults() throws FeedbackManagementException {

        Integer count = 0;

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            count = jdbcTemplate.fetchSingleRecord(FeedbackMgtSQLConstants.GET_FEEDBACK_COUNT, (resultSet, rowNumber) ->
                    resultSet.getInt(1), null);
        } catch (DataAccessException e) {
            throw FeedbackExceptionManagementUtil
                    .buildServerException(ErrorMessages.ERROR_CODE_GET_COUNT_WITHOUT_FILTER, e);
        }
        return count;
    }

    /**
     * Retrieve a list of user feedback entries when no filter is given.
     *
     * @param limit     max entries in list
     * @param offset    entries to skip
     * @param sortBy    how to sort the list
     * @param sortOrder order of sorting (ASC/DESC)
     * @return A list of user feedback entries
     * @throws FeedbackManagementException
     */
    private List<Feedback> listFeedbackEntries(int limit, int offset, String sortBy, String sortOrder)
            throws FeedbackManagementException {

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
                                    preparedStatement.setInt(1, limit);
                                    preparedStatement.setInt(2, offset);
                                });

                if (feedbackInfoList != null) {
                    feedbackInfoList.forEach(rethrowConsumer(feedbackInfo -> {
                        feedbackInfo.setTags(listTags(feedbackInfo.getId()));
                    }));
                }
                return feedbackInfoList;
            });
        } catch (TransactionException e) {
            throw FeedbackExceptionManagementUtil.buildServerException(ErrorMessages.ERROR_CODE_LIST_FEEDBACK, e);
        }
        return feedbackResultsList;
    }

    /**
     * Validate the limit value for pagination.
     *
     * @param limit Count value.
     * @return Validated limit value
     * @throws FeedbackManagementException
     */
    private int validateLimitForPagination(int limit) throws FeedbackManagementException {

        if (limit == 0) {
            limit = FeedbackMgtConstants.DEFAULT_SEARCH_LIMIT;
            if (log.isDebugEnabled()) {
                log.debug("Limit is not defined the request, hence set to default value: " + limit);
            }
        } else if (limit < 0) {
            throw FeedbackExceptionManagementUtil.buildClientException(ErrorMessages.ERROR_CODE_INVALID_LIMIT,
                    String.valueOf(limit));
        }
        return limit;
    }

    /**
     * Validate the offset value for pagination.
     *
     * @param offset Starting index.
     * @return Validated offset value
     * @throws FeedbackManagementException
     */
    private int validateOffsetForPagination(int offset) throws FeedbackManagementException {

        if (offset < 0) {
            throw FeedbackExceptionManagementUtil.buildClientException(ErrorMessages.ERROR_CODE_INVALID_OFFSET,
                    String.valueOf(offset));
        }
        return offset;
    }

    /**
     * Validate the attribute provided to sort by.
     *
     * @param sortBy attribute by which sorting should be done
     * @return Validated sortBy attribute
     * @throws FeedbackManagementException
     */
    private String validateSortingAttribute(String sortBy) throws FeedbackManagementException {

        // If sortBy is not provided, it is set to the default value
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = FeedbackMgtConstants.DEFAULT_SORT_BY;

        } else if (!isSortableAttribute(sortBy)) {
            throw FeedbackExceptionManagementUtil
                    .buildClientException(ErrorMessages.ERROR_CODE_UNSUPPORTED_SORT_BY_ATTRIBUTE, sortBy);
        }

        return sortBy;
    }

    /**
     * Validate the sort order parameter.
     *
     * @param sortOrder how the retrieved records should be ordered
     * @return Validated sortOrder parameter
     * @throws FeedbackManagementException
     */
    private String validateSortingOrder(String sortOrder) throws FeedbackManagementException {

        //If sortOrder is not provided, it is set to the default value
        if (sortOrder == null || sortOrder.isEmpty()) {
            sortOrder = FeedbackMgtConstants.DEFAULT_SORT_ORDER;
        } else if (!isValidSortOrder(sortOrder)) {
            throw FeedbackExceptionManagementUtil
                    .buildClientException(ErrorMessages.ERROR_CODE_INVALID_SORT_ORDER, sortOrder);
        }
        return sortOrder;
    }

    /**
     * Check if provided sortBy attribute is supported.
     *
     * @param attribute by which sorting should be done
     * @return true if sort by attribute is supported
     */
    private boolean isSortableAttribute(String attribute) {

        return Arrays.stream(FeedbackMgtConstants.SortableAttributes.values())
                .anyMatch(sortableAttribute -> sortableAttribute.name().equals(attribute.toLowerCase()));
    }

    /**
     * Check if provided sortBy attribute is supported.
     *
     * @param sortOrder by which records should be ordered
     * @return true if sort order value is supported
     */
    private boolean isValidSortOrder(String sortOrder) {

        return Arrays.stream(FeedbackMgtConstants.SortOrderOperators.values())
                .anyMatch(sortableAttribute -> sortableAttribute.name().equals(sortOrder.toLowerCase()));
    }

    /**
     * Parse the user given filter parameter.
     *
     * @param filter value provided by the user
     * @return A pair of string values (filterAttribute -> filterOperation)
     * @throws FeedbackManagementException
     */
    private Pair<String, String> buildFilter(String filter) throws FeedbackManagementClientException {

        if (StringUtils.isNotBlank(filter)) {
            String[] filterArgs = filter.split(" ");
            if (filterArgs.length == 3) {

                String filterAttribute = filterArgs[0];

                if (isFilterableAttribute(filterAttribute)) {
                    String operation = filterArgs[1];
                    String attributeValue = filterArgs[2];
                    if (attributeValue.isEmpty()) {
                        attributeValue = "*";
                    }
                    return Pair.of(filterAttribute, generateFilterString(operation, attributeValue.trim()));
                } else {
                    throw FeedbackExceptionManagementUtil.buildClientException(FeedbackMgtConstants.ErrorMessages.
                            ERROR_CODE_UNSUPPORTED_FILTER_ATTRIBUTE, filterAttribute);
                }
            } else {
                throw FeedbackExceptionManagementUtil.buildClientException(FeedbackMgtConstants.ErrorMessages.
                        ERROR_CODE_INVALID_FILTER_QUERY, filter);
            }
        } else {
            return null;
        }
    }

    /**
     * Generate the sql filter string from filter operation and attribute value.
     *
     * @param operation      filter operation specified (eq, co, sw, ew)
     * @param attributeValue value to which filtering is applied
     * @return sql formatted filter term
     * @throws FeedbackManagementClientException
     */
    private String generateFilterString(String operation, String attributeValue)
            throws FeedbackManagementClientException {

        String formattedFilter = null;
        try {
            switch (FeedbackMgtConstants.AttributeOperators.valueOf(operation.toLowerCase())) {
                case sw:
                    formattedFilter = attributeValue + "%";
                    break;
                case ew:
                    formattedFilter = "%" + attributeValue;
                    break;
                case eq:
                    formattedFilter = attributeValue;
                    break;
                case co:
                    formattedFilter = "%" + attributeValue + "%";
                    break;
            }
        } catch (IllegalArgumentException e) {
            throw FeedbackExceptionManagementUtil
                    .buildClientException(ErrorMessages.ERROR_CODE_UNSUPPORTED_FILTER_OPERATION, operation, e);
        }

        return formattedFilter;
    }

    /**
     * Check if provided attribute is supported for filtering.
     *
     * @param attribute by which filtering should be done
     * @return true if filter by attribute is supported
     */
    private boolean isFilterableAttribute(String attribute) {

        return Arrays.stream(FeedbackMgtConstants.FilterableAttributes.values())
                .anyMatch(filterableAttribute -> filterableAttribute.name().equals(attribute.toLowerCase()));
    }

    /**
     * Generate sql statement part to count feedback records with filter.
     *
     * @param filterAttribute to be applied for sql command
     * @return sql statement part to be appended to base sql statement
     * @throws FeedbackManagementClientException
     */
    private String generateSqlFilterForCount(String filterAttribute) throws FeedbackManagementClientException {

        String sqlQueryPart = "";
        try {
            switch (FeedbackMgtConstants.FilterableAttributes.valueOf(filterAttribute.toLowerCase())) {
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
            throw FeedbackExceptionManagementUtil
                    .buildClientException(ErrorMessages.ERROR_CODE_UNSUPPORTED_FILTER_ATTRIBUTE, filterAttribute, e);
        }
        return sqlQueryPart;
    }
}
