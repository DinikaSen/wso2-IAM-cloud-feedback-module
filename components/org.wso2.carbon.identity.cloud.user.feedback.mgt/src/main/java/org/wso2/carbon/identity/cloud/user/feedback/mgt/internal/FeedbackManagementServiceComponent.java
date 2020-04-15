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

package org.wso2.carbon.identity.cloud.user.feedback.mgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.FeedbackManagementService;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.FeedbackManagementServiceImpl;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.constant.FeedbackMgtConstants;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.exception.FeedbackManagementRuntimeException;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.util.FeedbackConfigParser;
import org.wso2.carbon.user.core.service.RealmService;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * OSGi declarative services component which handled activation and deactivation of
 * FeedbackManagementServiceComponent.
 */
@Component(
        name = "user.feedback.management.service",
        immediate = true
)
public class FeedbackManagementServiceComponent {

    private static final Log log = LogFactory.getLog(FeedbackManagementServiceComponent.class);
    private ServiceRegistration serviceRegistration = null;
    private RealmService realmService;

    @Activate
    protected void activate(ComponentContext context) {

        BundleContext bundleContext = context.getBundleContext();
        log.info("Feedback Management bundle is activated.");
        FeedbackConfigParser configParser = new FeedbackConfigParser();
        DataSource dataSource = initDataSource(configParser);

        setDataSourceToDataHolder(dataSource);

        serviceRegistration = bundleContext.registerService(FeedbackManagementService.class,
                FeedbackManagementServiceImpl.getInstance(), null);
        if (log.isDebugEnabled()) {
            log.debug("Feedback Management bundle is activated.");
        }

    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        if (log.isDebugEnabled()) {
            log.debug("Function Library Management bundle is deactivated.");
        }

        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
    }

    private DataSource initDataSource(FeedbackConfigParser configParser) {

        String dataSourceName = configParser.getFeedbackDataSource();
        DataSource dataSource;
        Context ctx;
        log.info("In Feedback Management data source initiation " + dataSourceName);
        try {
            ctx = new InitialContext();
            dataSource = (DataSource) ctx.lookup(dataSourceName);
            if (log.isDebugEnabled()) {
                log.debug(String.format("Data source: %s found in context.", dataSourceName));
            }

            return dataSource;
        } catch (NamingException e) {
            throw new FeedbackManagementRuntimeException(FeedbackMgtConstants.ErrorMessages
                    .ERROR_CODE_DATABASE_INITIALIZATION.getMessage(),
                    FeedbackMgtConstants.ErrorMessages
                            .ERROR_CODE_DATABASE_INITIALIZATION.getCode(), e);
        }
    }

    @Reference(
            name = "realm.service",
            service = org.wso2.carbon.user.core.service.RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService"
    )
    protected void setRealmService(RealmService realmService) {

        this.realmService = realmService;
        if (realmService != null && log.isDebugEnabled()) {
            log.debug("RealmService is registered in ConsentManager service.");
        }
    }

    protected void unsetRealmService(RealmService realmService) {

        if (log.isDebugEnabled()) {
            log.debug("RealmService is unregistered in ConsentManager service.");
        }
        this.realmService = null;
    }

    private void setDataSourceToDataHolder(DataSource dataSource) {

        FeedbackManagementServiceDataHolder.getInstance().setDataSource(dataSource);
        if (log.isDebugEnabled()) {
            log.debug("Data Source is set to the Feedback Management Service.");
        }
    }
}
