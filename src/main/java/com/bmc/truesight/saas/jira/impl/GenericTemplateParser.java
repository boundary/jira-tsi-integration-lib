package com.bmc.truesight.saas.jira.impl;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.bmc.truesight.saas.jira.api.JiraAPI;
import com.bmc.truesight.saas.jira.beans.Configuration;
import com.bmc.truesight.saas.jira.beans.FieldItem;
import com.bmc.truesight.saas.jira.beans.Filter;
import com.bmc.truesight.saas.jira.beans.TSIEvent;
import com.bmc.truesight.saas.jira.beans.Template;
import com.bmc.truesight.saas.jira.exception.ParsingException;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bmc.truesight.saas.jira.in.TemplateParser;
import com.bmc.truesight.saas.jira.util.Constants;
import com.bmc.truesight.saas.jira.util.StringUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Generic Template Parser
 *
 * @author Santosh Patil
 *
 */
public class GenericTemplateParser implements TemplateParser {

    private static final Logger log = LoggerFactory.getLogger(GenericTemplateParser.class);

    @Override
    public Template readParseConfigJson(Template defaultTemplate, String configJson) throws ParsingException {
        return parse(defaultTemplate, configJson);
    }

    /**
     * Used to parse the template in case of template available as json file
     *
     * @param fileName Name of the template json file
     * @throws ParsingException throws exception in case of unsuccessful parsing
     */
    @Override
    public Template readParseConfigFile(Template defaultTemplate, String fileName) throws ParsingException {
        // Read the file in String
        String configJson = null;
        try {
            configJson = FileUtils.readFileToString(new File(fileName), "UTF8");
        } catch (IOException e) {
            throw new ParsingException(StringUtil.format(Constants.CONFIG_FILE_NOT_VALID, new Object[]{fileName}));
        }
        return parse(defaultTemplate, configJson);
    }

    private Template parse(Template defaultTemplate, String configJson) throws ParsingException {
        //Template template = new Template();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = null;
        try {
            rootNode = mapper.readTree(configJson);
        } catch (IOException e) {
            throw new ParsingException(StringUtil.format(Constants.CONFIG_FILE_NOT_VALID, new Object[]{configJson, e.getMessage()}));
        }

        // Read the config details and map to pojo
        String configString;
        JsonNode configuration = rootNode.get("config");
        JsonNode filterConfiguration = rootNode.get("filter");
        Configuration config = null;
        Filter filter = null;
        if (configuration != null) {
            try {
                configString = mapper.writeValueAsString(configuration);
                config = mapper.readValue(configString, Configuration.class);
            } catch (IOException e) {
                throw new ParsingException(StringUtil.format(Constants.CONFIG_PROPERTY_NOT_VALID, new Object[]{e.getMessage()}));
            }
            Configuration defaultConfig = defaultTemplate.getConfig();
            updateConfig(defaultConfig, config);
            //defaultTemplate
        } else {
            log.trace("config field is not found, falling back to default values while parsing");
        }
        if (filterConfiguration != null) {
            Map<String, List<String>> filterItemMap = defaultTemplate.getFilter();
            if (filterConfiguration != null) {
                Iterator<Entry<String, JsonNode>> filterNodes = filterConfiguration.fields();
                ObjectReader obReader = mapper.reader(new TypeReference<List<String>>() {
                });
                while (filterNodes.hasNext()) {
                    try {
                        Map.Entry<String, JsonNode> entry = (Map.Entry<String, JsonNode>) filterNodes.next();
                        List<String> condList = obReader.readValue(entry.getValue());
                        filterItemMap.put(entry.getKey(), condList);
                    } catch (IOException ex) {
                        java.util.logging.Logger.getLogger(GenericTemplatePreParser.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            }

            //defaultTemplate
        } else {
            log.trace("config field is not found, falling back to default values while parsing");
        }
        // Read the payload details and map to pojo
        JsonNode payloadNode = rootNode.get("eventDefinition");
        TSIEvent event = null;
        if (payloadNode != null) {
            try {
                String payloadString = mapper.writeValueAsString(payloadNode);
                event = mapper.readValue(payloadString, TSIEvent.class);
            } catch (IOException e) {
                throw new ParsingException(StringUtil.format(Constants.PAYLOAD_PROPERTY_NOT_FOUND, new Object[]{e.getMessage()}));
            }
            TSIEvent defaultEvent = defaultTemplate.getEventDefinition();
            updateEventDefinition(defaultEvent, event);
            //defaultTemplate
        } else {
            log.trace("eventDefinition field not found, falling back to default values while parsing");
        }

        // Iterate over the properties and if it starts with '@', put it to
        // itemValueMap
        Iterator<Entry<String, JsonNode>> nodes = rootNode.fields();
        Map<String, FieldItem> fieldItemMap = defaultTemplate.getFieldItemMap();
        while (nodes.hasNext()) {
            Map.Entry<String, JsonNode> entry = (Map.Entry<String, JsonNode>) nodes.next();
            if (entry.getKey().startsWith("@")) {
                try {
                    String placeholderNode = mapper.writeValueAsString(entry.getValue());
                    FieldItem placeholderDefinition = mapper.readValue(placeholderNode, FieldItem.class);
                    fieldItemMap.put(entry.getKey(), placeholderDefinition);
                } catch (IOException e) {
                    throw new ParsingException(
                            StringUtil.format(Constants.PLACEHOLDER_PROPERTY_NOT_CORRECT, new Object[]{entry.getKey()}));
                }
            }
        }

        return defaultTemplate;
    }

    private void updateConfig(Configuration defaultConfig, Configuration config) {
        if (config.getJiraHostName() != null && !config.getJiraHostName().equals("")) {
            defaultConfig.setJiraHostName(config.getJiraHostName());
        }
        if (config.getJiraPassword() != null && !config.getJiraPassword().equals("")) {
            defaultConfig.setJiraPassword(config.getJiraPassword());
        }
        if (config.getJiraPort() != null) {
            defaultConfig.setJiraPort(config.getJiraPort());
        }
        if (config.getJiraUserName() != null && !config.getJiraUserName().equals("")) {
            defaultConfig.setJiraUserName(config.getJiraUserName());
        }
        if (config.getTsiEventEndpoint() != null && !config.getTsiEventEndpoint().equals("")) {
            defaultConfig.setTsiEventEndpoint(config.getTsiEventEndpoint());
        }
        if (config.getTsiApiToken() != null && !config.getTsiApiToken().equals("")) {
            defaultConfig.setTsiApiToken(config.getTsiApiToken());
        }
        if (config.getStartDateTime() != null) {
            defaultConfig.setStartDateTime(config.getStartDateTime());
        }
        if (config.getEndDateTime() != null) {
            defaultConfig.setEndDateTime(config.getEndDateTime());
        }
        if (config.getProtocolType() != null) {
            defaultConfig.setProtocolType(config.getProtocolType());
        }
        //Disabled ability to override from the user 
        /* if (config.getChunkSize() != null) {
            defaultConfig.setChunkSize(config.getChunkSize());
        }*/
        if (config.getRetryConfig() != null) {
            defaultConfig.setRetryConfig(config.getRetryConfig());
        }
        if (config.getWaitMsBeforeRetry() != null) {
            defaultConfig.setWaitMsBeforeRetry(config.getRetryConfig());
        }
    }

    private void updateEventDefinition(TSIEvent defaultEvent, TSIEvent event) {
        if (event.getTitle() != null && !event.getTitle().equals("")) {
            defaultEvent.setTitle(event.getTitle());
        }
        if (event.getStatus() != null && !event.getStatus().equals("")) {
            defaultEvent.setStatus(event.getStatus());
        }
        if (event.getSeverity() != null && !event.getSeverity().equals("")) {
            defaultEvent.setSeverity(event.getSeverity());
        }
        if (event.getFingerprintFields() != null && event.getFingerprintFields().size() > 0) {
            defaultEvent.setFingerprintFields(event.getFingerprintFields());
        }
        if (event.getEventClass() != null && !event.getEventClass().equals("")) {
            defaultEvent.setEventClass(event.getEventClass());
        }
        if (event.getCreatedAt() != null && !event.getCreatedAt().equals("")) {
            defaultEvent.setCreatedAt(event.getCreatedAt());
        }
        if (event.getMessage() != null && !event.getMessage().equals("")) {
            defaultEvent.setMessage(event.getMessage());
        }
        if (event.getProperties() != null && event.getProperties().size() > 0) {
            Map<String, String> defPropertyMap = defaultEvent.getProperties();
            Map<String, String> propertyMap = event.getProperties();
            event.getProperties().keySet().forEach(key -> {
                defPropertyMap.put(key, propertyMap.get(key));
            });
        }
        if (event.getSource() != null && event.getSource().equals("")) {
            defaultEvent.setSource(event.getSource());
        }
        if (event.getSender() != null && event.getSender().equals("")) {
            defaultEvent.setSender(event.getSender());
        }
    }

    @Override
    public Template ignoreFields(Template defaultTemplate, final String hostName, final String userName, final String password, final String portNumber, final String protocolType) {
        try {
            Map<String, String> fields = new HashMap<>();
            JiraRestClient client = JiraAPI.getJiraRestClient(hostName, portNumber, userName, password, protocolType);
            boolean isValid = JiraAPI.isValidCredentials(client, userName, hostName);
            if (isValid) {
                Map<String, String> jiraFields = new HashMap<>();
                jiraFields = JiraAPI.getTypeFields(client);
                fields.put(Constants.APPLICATION_ID, Constants.APPLICATION_ID);
                fields.put(Constants.FIELD_FETCH_KEY, Constants.FIELD_FETCH_KEY);
                fields.put(Constants.FIELD_ID, Constants.FIELD_ID);
                fields.put(Constants.FILED_KEY, Constants.FILED_KEY);
                fields.put(Constants.ID, Constants.ID);
                fields.putAll(jiraFields);
                Map<String, FieldItem> finalFieldItem = new HashMap<>();
                for (Map.Entry<String, FieldItem> entry : defaultTemplate.getFieldItemMap().entrySet()) {
                    FieldItem fieldItem = entry.getValue();
                    if (fields.containsKey(fieldItem.getFieldId())) {
                        finalFieldItem.put(entry.getKey(), entry.getValue());
                    }
                }
                defaultTemplate.setFieldItemMap(finalFieldItem);
                Map<String, String> finalPropertiesFields = new HashMap<>();
                for (Map.Entry<String, String> val : defaultTemplate.getEventDefinition().getProperties().entrySet()) {
                    FieldItem fieldItem = finalFieldItem.get(val.getValue());
                    if (fieldItem == null) {
                        if (Constants.APPLICATION_ID.equalsIgnoreCase(val.getKey())) {
                            finalPropertiesFields.put(val.getKey(), val.getValue());
                        } else {
                            log.info("Field Mapping is invalid hence ignoring it, property name is {} " + val.getKey());
                        }
                    } else {
                        finalPropertiesFields.put(val.getKey(), val.getValue());
                    }
                }
                FieldItem fieldItem = finalFieldItem.get(defaultTemplate.getEventDefinition().getCreatedAt());
                if (fieldItem == null) {
                    FieldItem fieldItemVal = defaultTemplate.getFieldItemMap().get(defaultTemplate.getEventDefinition().getCreatedAt());
                    log.info("Field Mapping is invalid hence ignoring it, property name is {} " + fieldItemVal.getFieldId());
                }
                FieldItem status = finalFieldItem.get(defaultTemplate.getEventDefinition().getStatus());
                if (status == null) {
                    FieldItem fieldItemVal = defaultTemplate.getFieldItemMap().get(defaultTemplate.getEventDefinition().getStatus());
                    log.info("Field Mapping is invalid hence ignoring it, property name is {} " + fieldItemVal.getFieldId());
                }
                FieldItem severity = finalFieldItem.get(defaultTemplate.getEventDefinition().getStatus());
                if (severity == null) {
                    FieldItem fieldItemVal = defaultTemplate.getFieldItemMap().get(defaultTemplate.getEventDefinition().getSeverity());
                    log.info("Field Mapping is invalid hence ignoring it, property name is {} " + fieldItemVal.getFieldId());
                }
                defaultTemplate.getEventDefinition().setProperties(finalPropertiesFields);
            }
        } catch (Exception ex) {
            log.trace("Exception occured while featching fileds from JIRA {}" + ex.getMessage());
        }
        return defaultTemplate;
    }
}
