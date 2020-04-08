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

package org.wso2.carbon.identity.cloud.user.feedback.mgt.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.constant.FeedbackMgtConstants;
import org.wso2.carbon.identity.cloud.user.feedback.mgt.exception.FeedbackManagementRuntimeException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.*;

import static org.wso2.carbon.identity.cloud.user.feedback.mgt.constant.FeedbackMgtConstants.ErrorMessages.ERROR_CODE_BUILDING_CONFIG;

/**
 * Config parser for feedback-mgt configurations.
 */
public class FeedbackConfigParser {

    private static final String DATA_SOURCE_NAME = "DataSource.Name";
    private static Map<String, Object> configuration = new HashMap<>();
    private static SecretResolver secretResolver;
    private static final Log log = LogFactory.getLog(FeedbackConfigParser.class);
    private OMElement rootElement;

    public FeedbackConfigParser() {

        buildConfiguration();
    }

    public Map<String, Object> getConfiguration() {

        return configuration;
    }

    public String getFeedbackDataSource() {
        String dataSource = configuration.get(DATA_SOURCE_NAME).toString();
        System.out.println(dataSource);
        return configuration.get(DATA_SOURCE_NAME) == null ? null : configuration.get(DATA_SOURCE_NAME).toString();
    }

    private void buildConfiguration() {

        InputStream inStream = null;
        StAXOMBuilder builder;

        try {

            String configurationFilePath = CarbonUtils.getCarbonConfigDirPath() + File.separator +
                    FeedbackMgtConstants.FEEDBACK_MANAGEMENT_CONFIG_XML;
            File feedbackConfigXml = new File(configurationFilePath);
            if (feedbackConfigXml.exists()) {
                inStream = new FileInputStream(feedbackConfigXml);
            }

            if (inStream == null) {
                String message = "Feedback configuration not found";
                if (log.isDebugEnabled()) {
                    log.debug(message);
                }
                log.error(message);
                throw new FileNotFoundException(message);
            }

            builder = new StAXOMBuilder(inStream);
            rootElement = builder.getDocumentElement();
            Stack<String> nameStack = new Stack<>();
            secretResolver = SecretResolverFactory.create(rootElement, true);
            readChildElements(rootElement, nameStack);
        } catch (IOException | XMLStreamException e) {
            throw new FeedbackManagementRuntimeException(ERROR_CODE_BUILDING_CONFIG.getMessage(),
                    ERROR_CODE_BUILDING_CONFIG.getCode(), e);
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
            } catch (IOException e) {
                log.error("Error closing the input stream for feedback-mgt-config.xml", e);
            }
        }
    }

    private void readChildElements(OMElement serverConfig, Stack<String> nameStack) {

        for (Iterator childElements = serverConfig.getChildElements(); childElements.hasNext(); ) {
            OMElement element = (OMElement) childElements.next();
            nameStack.push(element.getLocalName());
            if (elementHasText(element)) {
                String key = getKey(nameStack);
                Object currentObject = configuration.get(key);
                String value = replaceSystemProperty(element.getText());
                if (secretResolver != null && secretResolver.isInitialized() &&
                        secretResolver.isTokenProtected(key)) {
                    value = secretResolver.resolve(key);
                }
                if (currentObject == null) {
                    configuration.put(key, value);
                } else if (currentObject instanceof ArrayList) {
                    ArrayList list = (ArrayList) currentObject;
                    if (!list.contains(value)) {
                        list.add(value);
                        configuration.put(key, list);
                    }
                } else {
                    if (!value.equals(currentObject)) {
                        ArrayList arrayList = new ArrayList(2);
                        arrayList.add(currentObject);
                        arrayList.add(value);
                        configuration.put(key, arrayList);
                    }
                }
            }
            readChildElements(element, nameStack);
            nameStack.pop();
        }
    }

    private String getKey(Stack<String> nameStack) {

        StringBuilder key = new StringBuilder();
        for (int i = 0; i < nameStack.size(); i++) {
            String name = nameStack.elementAt(i);
            key.append(name).append(".");
        }
        key.deleteCharAt(key.lastIndexOf("."));

        return key.toString();
    }

    private boolean elementHasText(OMElement element) {

        String text = element.getText();
        return text != null && text.trim().length() != 0;
    }

    private String replaceSystemProperty(String text) {

        int indexOfStartingChars = -1;
        int indexOfClosingBrace;

        // The following condition deals with properties.
        // Properties are specified as ${system.property},
        // and are assumed to be System properties
        StringBuilder textBuilder = new StringBuilder(text);
        while (indexOfStartingChars < textBuilder.indexOf("${")
                && (indexOfStartingChars = textBuilder.indexOf("${")) != -1
                && (indexOfClosingBrace = textBuilder.indexOf("}")) != -1) { // Is a property used?
            String sysProp = textBuilder.substring(indexOfStartingChars + 2, indexOfClosingBrace);
            String propValue = System.getProperty(sysProp);
            if (propValue != null) {
                textBuilder = new StringBuilder(textBuilder.substring(0, indexOfStartingChars) + propValue
                        + textBuilder.substring(indexOfClosingBrace + 1));
            }
            if (sysProp.equals(ServerConstants.CARBON_HOME)) {
                if (System.getProperty(ServerConstants.CARBON_HOME).equals(".")) {
                    textBuilder.insert(0, new File(".").getAbsolutePath() + File.separator);
                }
            }
        }
        text = textBuilder.toString();
        return text;
    }

    /**
     * Returns the element with the provided local part
     *
     * @param localPart local part name
     * @return Corresponding OMElement
     */
    public OMElement getConfigElement(String localPart) {

        return rootElement.getFirstChildWithName(new QName(FeedbackMgtConstants.FEEDBACK_MANAGEMENT_DEFAULT_NAMESPACE,
                localPart));
    }

    /**
     * Returns the QName with the consent name space
     *
     * @param localPart local part name
     * @return relevant QName
     */
    public QName getQNameWithConsentNS(String localPart) {

        return new QName(FeedbackMgtConstants.FEEDBACK_MANAGEMENT_DEFAULT_NAMESPACE, localPart);
    }

}
