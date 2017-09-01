package com.bmc.truesight.saas.jira.impl;

import java.text.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bmc.truesight.saas.jira.api.JiraAPI;
import com.bmc.truesight.saas.jira.beans.Configuration;
import com.bmc.truesight.saas.jira.beans.JiraEventResponse;
import com.bmc.truesight.saas.jira.beans.Response;
import com.bmc.truesight.saas.jira.beans.Template;
import com.bmc.truesight.saas.jira.exception.JiraApiInstantiationFailedException;
import com.bmc.truesight.saas.jira.exception.JiraLoginFailedException;
import com.bmc.truesight.saas.jira.exception.JiraReadFailedException;
import com.bmc.truesight.saas.jira.exception.ParsingException;
import com.bmc.truesight.saas.jira.integration.adapter.JiraEntryEventAdapter;
import com.bmc.truesight.saas.jira.util.Constants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author Santosh Patil/vitiwari
 *
 */
public class JiraReader {

    private final static Logger log = LoggerFactory.getLogger(JiraReader.class);

    public boolean validateCredentials(Configuration config) throws JiraLoginFailedException, JiraApiInstantiationFailedException {
        JiraAPI jiraAPI = new JiraAPI(config);
        boolean isValid = jiraAPI.isValidCredentials();
        return isValid;
    }

    public JiraEventResponse readJiraTickets(Template template, int startFrom, int chunkSize, JiraEntryEventAdapter adapter) throws JiraReadFailedException, JiraApiInstantiationFailedException {
        Configuration config = template.getConfig();
        JiraEventResponse jiraResponse = null;
        JiraAPI jiraAPI = new JiraAPI(config);
        String searchQuery = jiraAPI.buildJQLQuery(template.getFilter(), config.getStartDateTime(), config.getEndDateTime(), template.getJqlQuery());
        log.debug("SearchQuery formed as ->{}", searchQuery);
        String url = jiraAPI.getURL();
        String searchUrl = jiraAPI.getSearchUrl(chunkSize, startFrom, searchQuery, "");
        JsonNode response;
        try {
            response = jiraAPI.search(searchUrl);
        } catch (ParsingException e) {
            log.error("Fetching the issues failed {}", e.getMessage());
            throw new JiraReadFailedException("Fetching the issues failed " + e.getMessage());
        }
        if (!response.isNull()) {
            JsonNode responseFiledsNode = response.get(Constants.JSON_ISSUES_NODE);
            if (!responseFiledsNode.isNull()) {
                jiraResponse = adapter.eventList(responseFiledsNode, template);
            }
        }
        return jiraResponse;
    }

    public int getAvailableRecordsCount(Template template) throws JiraReadFailedException, ParseException, JiraApiInstantiationFailedException {
        Configuration config = template.getConfig();
        int recordsCount = 0;
        JiraAPI jiraAPI = new JiraAPI(config);
        String searchQuery = jiraAPI.buildJQLQuery(template.getFilter(), config.getStartDateTime(), config.getEndDateTime(), template.getJqlQuery());
        log.debug("SearchQuery formed as ->{}", searchQuery);
        String finalSearchUrl = jiraAPI.getSearchUrl(0, 0, searchQuery, Constants.JIRA_NONE_FIELD);
        log.debug("finalSearchUrl formed as ->{}", finalSearchUrl);
        try {
            JsonNode responseNode = jiraAPI.search(finalSearchUrl);
            ObjectMapper mapper = new ObjectMapper();
            Response response = mapper.treeToValue(responseNode, Response.class);
            log.debug("Response as ->" + response.getTotal());
            recordsCount = response.getTotal();
        } catch (ParsingException e) {
            log.error("ParsingException : {}" + e.getMessage());
            throw new JiraReadFailedException(e.getMessage());
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException : {}" + e.getMessage());
            throw new JiraReadFailedException(e.getMessage());
        }
        return recordsCount;
    }

}
