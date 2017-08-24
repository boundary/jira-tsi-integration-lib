package com.bmc.truesight.saas.jira.beans;

import java.util.List;
import java.util.Map;

/**
 * This is a pojo Class which represents the json configuration template (
 * incidentTemplate and changeTemplate)
 *
 * @author Santosh Patil
 *
 */
public class Template {

    private Configuration config;
    private TSIEvent eventDefinition;
    private Map<String, FieldItem> FieldItemMap;
    private Map<String, List<String>> filter;
    private String jqlQuery;

    public String getJqlQuery() {
        return jqlQuery;
    }

    public void setJqlQuery(String jqlQuery) {
        this.jqlQuery = jqlQuery;
    }

    public Map<String, List<String>> getFilter() {
        return filter;
    }

    public void setFilter(Map<String, List<String>> filter) {
        this.filter = filter;
    }

    public Configuration getConfig() {
        return config;
    }

    public void setConfig(Configuration config) {
        this.config = config;
    }

    public TSIEvent getEventDefinition() {
        return eventDefinition;
    }

    public void setEventDefinition(TSIEvent eventDefinition) {
        this.eventDefinition = eventDefinition;
    }

    public Map<String, FieldItem> getFieldItemMap() {
        return FieldItemMap;
    }

    public void setFieldItemMap(Map<String, FieldItem> fieldItemMap) {
        FieldItemMap = fieldItemMap;
    }
}
