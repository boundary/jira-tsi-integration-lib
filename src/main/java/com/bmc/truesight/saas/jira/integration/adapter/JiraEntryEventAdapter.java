package com.bmc.truesight.saas.jira.integration.adapter;

import com.bmc.truesight.saas.jira.beans.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bmc.truesight.saas.jira.beans.FieldItem;
import com.bmc.truesight.saas.jira.beans.JIRAEventResponse;
import com.bmc.truesight.saas.jira.beans.TSIEvent;
import com.bmc.truesight.saas.jira.beans.Template;
import com.bmc.truesight.saas.jira.util.Constants;
import com.bmc.truesight.saas.jira.util.StringUtil;
import com.bmc.truesight.saas.jira.util.Util;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
            String value = "null";
            try {
                FieldItem fieldItem = template.getFieldItemMap().get(placeholder);
                JsonNode jsonNode = entry.get(Constants.JSON_FILED_NODE);

                if (Constants.ID.equalsIgnoreCase(placeholder)) {
                    value = entry.get(fieldItem.getFieldId()).asText();
                    return value;
                }
                if (!jsonNode.get(fieldItem.getFieldId()).isMissingNode()) {
                    if (jsonNode.get(fieldItem.getFieldId()).isContainerNode()) {
                        if (!jsonNode.get(fieldItem.getFieldId()).isNull()) {
                            value = jsonNode.get(fieldItem.getFieldId()).get(fieldItem.getFetchKey()).asText();
                        }
                    } else if (!jsonNode.get(fieldItem.getFieldId()).isNull()) {
                        value = jsonNode.get(fieldItem.getFetchKey()).asText();
                    }
                }
                String val = "";
                if (value == null) {
                } else {
                    val = value;
                    return val;
                }
            } catch (Exception ex) {
                return value;
            }
        } else {
            return placeholder;
        }

        return null;
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
        if (invalidEventList.size() > 0) {
            System.err.println("{}events dropped before sending to TSI {}" + invalidEventList.size());
            System.err.println("Events size is greater than allowed limit({})" + Constants.MAX_EVENT_SIZE_ALLOWED_BYTES + " bytes. Please review the field mapping ");
        }
        response.setValidEventList(tsiValidEventList);
        response.setInvalidEventList(invalidEventList);
        return response;
    }
}
