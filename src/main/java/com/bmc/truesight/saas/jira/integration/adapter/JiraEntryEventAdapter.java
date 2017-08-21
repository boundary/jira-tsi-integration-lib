package com.bmc.truesight.saas.jira.integration.adapter;

import com.bmc.truesight.saas.jira.beans.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bmc.truesight.saas.jira.beans.FieldItem;
import com.bmc.truesight.saas.jira.beans.JIRAEventResponse;
import com.bmc.truesight.saas.jira.beans.TSIEvent;
import com.bmc.truesight.saas.jira.beans.Template;
import com.bmc.truesight.saas.jira.util.BuiltInFields;
import com.bmc.truesight.saas.jira.util.Constants;
import com.bmc.truesight.saas.jira.util.StringUtil;
import com.bmc.truesight.saas.jira.util.Util;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

/**
 * This is an adapter which converts the jira {@link Entry} items into
 * {@link TSIEvent} (TSI Events)
 *
 * @author Santosh Patil
 */
public class JiraEntryEventAdapter {

    private static final Logger log = LoggerFactory.getLogger(JiraEntryEventAdapter.class);

    /**
     * This method is an adapter which converts a jira Entry into Event object
     *
     * @param template A {@link Template} instance which contains the field
     * mapping and event Definition
     * @param entry {@link Entry} json Object representing Jira Record
     * @param serviceType
     * @return TsiEvent {@link TSIEvent} object compatible to TSI event
     * ingestion API
     */
    public TSIEvent convertEntryToEvent(Template template, JsonNode entry, final String serviceType) {

        TSIEvent event = new TSIEvent(template.getEventDefinition());

        event.setTitle(getValueFromEntry(template, entry, event.getTitle()));
        List<String> fPrintFields = new ArrayList<>();
        event.getFingerprintFields().forEach(fingerPrint -> {
            fPrintFields.add(getValueFromEntry(template, entry, fingerPrint));
        });
        event.setFingerprintFields(fPrintFields);
        Map<String, String> properties = event.getProperties();
        for (String key : properties.keySet()) {
            properties.put(key, getValueFromEntry(template, entry, properties.get(key)));
        }
        event.setSeverity(getValueFromEntry(template, entry, event.getSeverity()));
        event.setStatus(getValueFromEntry(template, entry, event.getStatus()));
        event.setCreatedAt(Long.toString(Util.convertIntoUTC(getValueFromEntry(template, entry, event.getCreatedAt()))));
        event.setEventClass(getValueFromEntry(template, entry, event.getEventClass()));

        // valiadting source
        EventSource source = event.getSource();
        source.setName(serviceType);
        source.setName(serviceType);
        source.setType(getValueFromEntry(template, entry, source.getType()));
        source.setRef(getValueFromEntry(template, entry, source.getRef()));

        EventSource sender = event.getSender();
        sender.setName(serviceType);
        sender.setType(getValueFromEntry(template, entry, sender.getType()));
        sender.setRef(getValueFromEntry(template, entry, sender.getRef()));
        return event;

    }

    private String getValueFromEntry(Template template, JsonNode entry, String placeholder) {
        if (placeholder.startsWith("@")) {
            String value = "";
            try {
                FieldItem fieldItem = template.getFieldItemMap().get(placeholder);
                JsonNode jsonNode = entry.get(Constants.JSON_FILED_NODE);
                if (Constants.ID.equalsIgnoreCase(placeholder)) {
                    value = entry.get(fieldItem.getFieldId()).asText();
                    return value;
                }
                if (Constants.FILED_KEY.equalsIgnoreCase(placeholder)) {
                    value = entry.get(fieldItem.getFieldId()).asText();
                    return value;
                }
                if (!jsonNode.get(fieldItem.getFieldId()).isMissingNode()) {
                    if (jsonNode.get(fieldItem.getFieldId()).isContainerNode()) {
                        if (!jsonNode.get(fieldItem.getFieldId()).isNull()) {
                            if (Util.isContaineString(fieldItem.getFieldId())) {
                                value = getCustomeFiledValues(jsonNode.get(fieldItem.getFieldId()));
                            } else if (BuiltInFields.COMPONENTS.getField().equalsIgnoreCase(fieldItem.getFieldId()) || BuiltInFields.VERSION.getField().equalsIgnoreCase(fieldItem.getFieldId()) || BuiltInFields.FIXVERSION.getField().equalsIgnoreCase(fieldItem.getFieldId())) {
                                value = getMultipleValues(jsonNode.get(fieldItem.getFieldId()));
                            } else if (BuiltInFields.LABLES.getField().equalsIgnoreCase(fieldItem.getFieldId())) {
                                value = getArrayValues(jsonNode.get(fieldItem.getFieldId()));
                            } else if (BuiltInFields.ISSUELINKS.getField().equalsIgnoreCase(fieldItem.getFieldId())) {
                                value = getIssueLinks(jsonNode.get(fieldItem.getFieldId()));
                            } else if (!jsonNode.get(fieldItem.getFieldId()).isNull()) {
                                value = jsonNode.get(fieldItem.getFieldId()).get(Constants.FIELD_NAME).asText();
                            }
                        }
                    } else if (!jsonNode.get(fieldItem.getFieldId()).isNull()) {
                        value = jsonNode.get(fieldItem.getFieldId()).asText();
                    }
                }
                if (value == null) {
                    return " ";
                } else {
                    return value;
                }
            } catch (Exception ex) {
                log.trace("Not able to find the field {}" + ex.getMessage());
                return value;
            }
        } else {
            return placeholder;
        }
    }

    public JIRAEventResponse eventList(JsonNode responseIssuesNode, Template template, final String serviceType) {
        List<TSIEvent> tsiValidEventList = new ArrayList<>();
        List<TSIEvent> invalidEventList = new ArrayList<>();
        JIRAEventResponse response = new JIRAEventResponse();
        for (JsonNode rootnode : responseIssuesNode) {
            TSIEvent event = convertEntryToEvent(template, rootnode, serviceType);
            if (StringUtil.isObjectJsonSizeAllowed(event)) {
                tsiValidEventList.add(event);
            } else {
                invalidEventList.add(event);
            }
        }
        List<String> invalidEvents = new ArrayList<>();
        if (invalidEventList.size() > 0) {
            try {
                for (TSIEvent event : invalidEventList) {
                    invalidEvents.add(event.getProperties().get(Constants.FIELD_FETCH_KEY));
                }
            } catch (Exception ex) {
                log.error("Exception occured while getting the invalid events {}", ex.getMessage());
            }
        }
        response.setValidEventList(tsiValidEventList);
        response.setInvalidEventList(invalidEventList);
        response.setInvalidEventIdsList(invalidEvents);
        return response;
    }

    private String getMultipleValues(JsonNode jsonNode) {
        StringBuilder value = new StringBuilder();
        int count = 0;
        if (jsonNode == null || jsonNode.isNull()) {
            return value.toString();
        } else {
            for (JsonNode node : jsonNode) {
                try {
                    if (count == 0) {
                        value.append(node.get(Constants.FIELD_NAME).asText());
                    } else {
                        value.append(",").append(node.get(Constants.FIELD_NAME).asText());
                    }
                    count++;
                } catch (Exception ex) {
                    log.trace("Not able to find the field {}" + ex.getMessage());
                }
            }
        }
        return value.toString();
    }

    private String getArrayValues(JsonNode jsonNode) {
        StringBuilder value = new StringBuilder();
        ObjectMapper mapper = new ObjectMapper();
        if (jsonNode == null || jsonNode.isNull()) {
            return value.toString();
        } else {
            ObjectReader obReader = mapper.reader(new TypeReference<List<String>>() {
            });
            try {
                List<String> condList = obReader.readValue(jsonNode);
                value.append(condList);
            } catch (IOException ex) {
                log.trace("Not able to find the multi values field and ignore it {} " + ex.getMessage());
            }
        }
        return value.toString();
    }

    private String getIssueLinks(JsonNode jsonNode) {
        StringBuilder value = new StringBuilder();
        Set<String> treeSet = new TreeSet<>();
        if (jsonNode == null || jsonNode.isNull()) {
            return value.toString();
        } else {
            for (JsonNode node : jsonNode) {
                try {
                    treeSet.add(node.get("type").get(Constants.FIELD_NAME).asText());
                } catch (Exception ex) {
                    log.trace("Not able to find the issue link field and ignore it {} " + ex.getMessage());
                }
            }
            value.append(treeSet);
        }
        return value.toString();
    }

    private String getCustomeFiledValues(JsonNode jsonNode) {
        StringBuilder value = new StringBuilder();
        Set<String> treeSet = new TreeSet<>();
        boolean isMultiArray = true;
        ObjectMapper mapper = new ObjectMapper();
        if (jsonNode == null || jsonNode.isNull() || jsonNode.size() <= 0) {
            return value.toString();
        } else {
            try {
                treeSet.add(jsonNode.get(Constants.FIELD_VALUE).asText());
                isMultiArray = false;
            } catch (Exception ex) {
                log.trace("Not able to find the custome field and ignore it {} " + ex.getMessage());
            }
            value.append(treeSet);
            if (isMultiArray) {
                ObjectReader obReader = mapper.reader(new TypeReference<List<String>>() {
                });
                try {
                    List<String> condList = obReader.readValue(jsonNode);
                    value.append(condList);
                } catch (Exception ex) {
                    log.trace("Not able to find the custome field and ignore it {} " + ex.getMessage());
                }
            }

        }
        return value.toString();
    }
}
