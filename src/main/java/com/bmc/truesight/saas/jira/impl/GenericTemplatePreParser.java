package com.bmc.truesight.saas.jira.impl;

import com.bmc.truesight.saas.jira.beans.Configuration;
import com.bmc.truesight.saas.jira.beans.FieldItem;
import com.bmc.truesight.saas.jira.beans.TSIEvent;
import com.bmc.truesight.saas.jira.beans.Template;
import com.bmc.truesight.saas.jira.exception.ParsingException;
import com.bmc.truesight.saas.jira.in.TemplatePreParser;
import com.bmc.truesight.saas.jira.util.Constants;
import com.bmc.truesight.saas.jira.util.StringUtil;
import com.bmc.truesight.saas.jira.util.Util;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

/**
 * This class helps in preParsing the default master configurations and return
 * as a {@link Template} object.
 *
 * @author Santosh Patil
 *
 */
public class GenericTemplatePreParser implements TemplatePreParser {

    @Override
    public Template loadDefaults() throws ParsingException {

        Template template = new Template();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = null;
        try {
            String configJson = getFile(Constants.JIRA_TEMPLATE_FILE_NAME);
            rootNode = mapper.readTree(configJson);
        } catch (IOException e) {
            throw new ParsingException(StringUtil.format(Constants.CONFIG_FILE_NOT_VALID, new Object[]{e.getMessage()}));
        }

        // Read the config details and map to pojo
        Configuration config = new Configuration();
        try {
            JsonNode configuration = rootNode.get(Constants.CONFIG_NODE_NAME);
            if (configuration != null) {
                JsonNode hostNode = configuration.get(Constants.CONFIG_HOSTNAME_NODE_NAME);
                if (hostNode != null) {
                    config.setJiraHostName(hostNode.asText());
                }

                JsonNode portNode = configuration.get(Constants.CONFIG_PORT_NODE_NAME);
                if (portNode != null) {
                    config.setJiraPort(portNode.asText());
                }

                JsonNode userNode = configuration.get(Constants.CONFIG_USERNAME_NODE_NAME);
                if (userNode != null) {
                    config.setJiraUserName(userNode.asText());
                }

                JsonNode passNode = configuration.get(Constants.CONFIG_PASSWORD_NODE_NAME);
                if (passNode != null) {
                    config.setJiraPassword(passNode.asText());
                }

                JsonNode tsiEndNode = configuration.get(Constants.CONFIG_TSIENDPOINT_NODE_NAME);
                if (tsiEndNode != null) {
                    config.setTsiEventEndpoint(tsiEndNode.asText());
                }

                JsonNode tsiKeyNode = configuration.get(Constants.CONFIG_TSITOKEN_NODE_NAME);
                if (tsiKeyNode != null) {
                    config.setTsiApiToken(tsiKeyNode.asText());
                }

                //Setting Config chunk size as constant
                config.setChunkSize(Constants.CONFIG_CHUNK_SIZE);
                JsonNode retryNode = configuration.get(Constants.CONFIG_RETRY_NODE_NAME);
                if (retryNode != null) {
                    config.setRetryConfig(retryNode.asInt());
                }

                JsonNode waitMsNode = configuration.get(Constants.CONFIG_WAITSMS_NODE_NAME);
                if (waitMsNode != null) {
                    config.setWaitMsBeforeRetry(waitMsNode.asInt());
                }
                JsonNode protocalType = configuration.get(Constants.CONFIG_PROTOCAL_TYPE);
                if (protocalType != null) {
                    config.setProtocolType(protocalType.asText());
                }
                String endDate = configuration.get(Constants.CONFIG_END_DATE_CONDITION_FIELDS).asText();
                if (endDate != null) {
                    config.setEndDateTime(Util.format(endDate));
                }
                String startDate = configuration.get(Constants.CONFIG_START_DATE_CONDITION_FIELDS).asText();
                if (startDate != null) {
                    config.setStartDateTime(Util.format(startDate));
                }

            }

            template.setConfig(config);
        } catch (Exception e) {
            throw new ParsingException(StringUtil.format(Constants.CONFIG_PROPERTY_NOT_FOUND, new Object[]{e.getMessage()}));
        }
        //Read the payload details and map to pojo
        try {
            JsonNode payloadNode = rootNode.get(Constants.EVENTDEF_NODE_NAME);
            String payloadString = mapper.writeValueAsString(payloadNode);
            TSIEvent event = mapper.readValue(payloadString, TSIEvent.class);
            template.setEventDefinition(event);
        } catch (IOException e) {
            throw new ParsingException(StringUtil.format(Constants.PAYLOAD_PROPERTY_NOT_FOUND, new Object[]{}));
        }

        // Iterate over the properties and if it starts with '@', put it to
        // itemValueMap
        Iterator<Entry<String, JsonNode>> nodes = rootNode.fields();
        Map<String, FieldItem> fieldItemMap = new HashMap<>();
        while (nodes.hasNext()) {
            Map.Entry<String, JsonNode> entry = (Map.Entry<String, JsonNode>) nodes.next();
            if (entry.getKey().startsWith(Constants.PLACEHOLDER_START_TOKEN)) {
                try {
                    String placeholderNode = mapper.writeValueAsString(entry.getValue());
                    FieldItem placeholderDefinition = mapper.readValue(placeholderNode, FieldItem.class);
                    fieldItemMap.put(entry.getKey(), placeholderDefinition);
                } catch (IOException e) {
                    throw new ParsingException(StringUtil.format(Constants.PAYLOAD_PROPERTY_NOT_FOUND, new Object[]{entry.getKey()}));
                }
            }
        }
        template.setFieldItemMap(fieldItemMap);
        JsonNode filterConfiguration = rootNode.get(Constants.CONFIG_FILTER_NODE);
        Map<String, List<String>> filterItemMap = new HashMap<>();
        if (filterConfiguration != null) {
            Iterator<Entry<String, JsonNode>> filterNodes = filterConfiguration.fields();
            ObjectReader obReader = mapper.reader(new TypeReference<List<String>>() {
            });
            while (filterNodes.hasNext()) {
                Map.Entry<String, JsonNode> entry = (Map.Entry<String, JsonNode>) filterNodes.next();
                try {
                    List<String> condList = obReader.readValue(entry.getValue());
                    filterItemMap.put(entry.getKey(), condList);
                } catch (IOException ex) {
                    throw new ParsingException(StringUtil.format(Constants.CONFIG_VALIDATION_FAILED, new Object[]{entry.getKey()}));
                }

            }
        }
        JsonNode jqlQuery = rootNode.get(Constants.JQL_QUERY_FIELD);
        if (jqlQuery != null) {
            template.setJqlQuery(jqlQuery.asText());
        }
        template.setFilter(filterItemMap);
        return template;
    }

    private String getFile(String fileName) throws IOException {
        //Get file from resources folder
        InputStream inputStream = ClassLoader.getSystemResourceAsStream(fileName);
        InputStreamReader streamReader = new InputStreamReader(inputStream, "UTF-8");
        BufferedReader in = new BufferedReader(streamReader);
        StringBuffer text = new StringBuffer("");
        for (String line; (line = in.readLine()) != null;) {
            text = text.append(line);
        }
        return text.toString();
    }

}
