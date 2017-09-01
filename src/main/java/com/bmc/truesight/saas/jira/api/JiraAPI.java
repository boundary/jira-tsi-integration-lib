package com.bmc.truesight.saas.jira.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.Field;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Priority;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;
import com.bmc.truesight.saas.jira.beans.Configuration;
import com.bmc.truesight.saas.jira.exception.JiraApiInstantiationFailedException;
import com.bmc.truesight.saas.jira.exception.ParsingException;
import com.bmc.truesight.saas.jira.util.Constants;
import com.bmc.truesight.saas.jira.util.Util;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author Santosh Patil, vitiwari
 * @Date 27/07/2017
 */
public class JiraAPI {

    private static final Logger log = LoggerFactory.getLogger(JiraAPI.class);

    private static final String JQL_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm";

    private static JiraAPI instance = null;
    private static String authCode;
    private static Configuration config;
    private static JiraRestClient jiraRestClient;
    private static String serverTimeZone;

    private JiraAPI() {
        super();
    }

    public static JiraAPI getInstance(Configuration configuration) throws JiraApiInstantiationFailedException {
        if (instance == null) {
            synchronized (JiraAPI.class) {
                if (instance == null) {
                    instance = new JiraAPI();
                    config = configuration;
                    authCode = instance.getAuthCode(configuration.getJiraUserName(), configuration.getJiraPassword());
                    try {
                        jiraRestClient = instance.getJiraRestClient();
                    } catch (URISyntaxException e) {
                        throw new JiraApiInstantiationFailedException("URI is not correct, Failed to create Jira REST client");
                    } catch (Exception e) {
                        throw new JiraApiInstantiationFailedException("Failed to create Jira REST client, " + e.getMessage());
                    }
                    try {
                        serverTimeZone = instance.getServerTimeZone();
                    } catch (ParsingException e) {
                        throw new JiraApiInstantiationFailedException("Failed to get serverTimeZone, " + e.getMessage());
                    }
                    return instance;
                }
            }
        }
        return instance;
    }

    private JiraRestClient getJiraRestClient() throws URISyntaxException {
        URI uri = new URI(this.getURL());
        final AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        final JiraRestClient jiraRestClient = factory.createWithBasicHttpAuthentication(uri, config.getJiraUserName(), config.getJiraPassword());
        return jiraRestClient;
    }

    public boolean isValidCredentials() {
        boolean isValid = true;
        try {
            Promise<User> promise = jiraRestClient.getUserClient().getUser(config.getJiraUserName());
            User user = promise.claim();
        } catch (RestClientException ex) {
            log.error("Authentication failed for host name {} , Response: Status Code {} ", config.getJiraHostName(), ex.getStatusCode().get());
            isValid = false;
        } catch (Exception ex) {
            log.debug("Jira validation failed {}", ex.getMessage());
            log.error("Something went wrong while logging into Jira, Login unsuccessful. Please run in debug mode to get more information");
            isValid = false;
        }
        return isValid;
    }

    public Map<Long, String> getTypeOfStatus() {
        Map<Long, String> typeOfStatus = new HashMap<>();
        for (Status status : jiraRestClient.getMetadataClient().getStatuses().claim()) {
            typeOfStatus.put(status.getId(), status.getName());
        }
        return typeOfStatus;
    }

    public Map<Long, String> getTypeOfIssues() {
        Map<Long, String> typeOfIssues = new HashMap<>();
        for (IssueType issueType : jiraRestClient.getMetadataClient().getIssueTypes().claim()) {
            typeOfIssues.put(issueType.getId(), issueType.getName());
        }
        return typeOfIssues;
    }

    public Map<String, String> getTypeFields() {
        Map<String, String> fields = new HashMap<>();
        for (Field field : jiraRestClient.getMetadataClient().getFields().claim()) {
            fields.put(field.getId(), field.getName());
        }
        return fields;
    }

    public Map<String, String> getSystemFields() {
        Map<String, String> systemFields = new HashMap<>();
        for (Field field : jiraRestClient.getMetadataClient().getFields().claim()) {
            if (field.getFieldType().name().equalsIgnoreCase(Constants.SYSTEM_FIELDS)) {
                systemFields.put(field.getId(), field.getName());
            }
        }
        return systemFields;
    }

    public Map<String, String> getCustomFields() {
        Map<String, String> customFields = new HashMap<>();
        for (Field field : jiraRestClient.getMetadataClient().getFields().claim()) {
            if (field.getFieldType().name().equalsIgnoreCase(Constants.CUSTOM_FIELDS)) {
                customFields.put(field.getId(), field.getName());
            }
        }
        return customFields;
    }

    public List<Issue> issuesList(final String jqlQuery, final Integer maxResult, final Integer startAt, HashSet<String> set) {
        Promise<SearchResult> searchJqlPromise = jiraRestClient.getSearchClient().searchJql(jqlQuery, maxResult, startAt, set);
        List<Issue> listOfIssues = (List<Issue>) searchJqlPromise.claim().getIssues();
        return listOfIssues;
    }

    public JsonNode search(final String finalUrl) throws ParsingException {
        JsonNode responseNode = null;
        HttpClient httpClient = null;
        boolean isSuccessful = false;
        int retryCount = 0;
        while (!isSuccessful && retryCount <= config.getRetryConfig()) {
            try {
                httpClient = HttpClientBuilder.create().build();
                HttpGet httpGet = new HttpGet(finalUrl);
                httpGet.addHeader("Authorization", Constants.JIRA_BASIC + authCode);
                httpGet.addHeader("Content-Type", "application/json");
                httpGet.addHeader("accept", "application/json");
                httpGet.addHeader("User-Agent", "JiraIntegration");
                HttpResponse response = httpClient.execute(httpGet);
                String responce = EntityUtils.toString(response.getEntity());
                ObjectMapper objectMapper = new ObjectMapper();
                responseNode = objectMapper.readTree(responce);
                return responseNode;
            } catch (IOException | ParseException ex) {
                if (retryCount < config.getRetryConfig()) {
                    retryCount++;
                    try {
                        log.error("[Retry  {} ], Waiting for {} sec before trying again ......" + retryCount, config.getWaitMsBeforeRetry());
                        Thread.sleep(config.getWaitMsBeforeRetry());
                    } catch (InterruptedException e1) {
                        log.error("Thread interrupted ......");
                    }
                    continue;
                } else {
                    throw new ParsingException(Util.format(Constants.AUTHENTICATION_FAILED, new Object[]{ex.getMessage()}));

                }

            }
        }
        return responseNode;
    }

    public Map<Long, String> getTypeOfPriority() {
        Map<Long, String> typeOfIssues = new HashMap<>();
        for (Priority priority : jiraRestClient.getMetadataClient().getPriorities().claim()) {
            typeOfIssues.put(priority.getId(), priority.getName());
        }
        return typeOfIssues;
    }

    public String getServerTimeZone() throws ParsingException {
        JsonNode responseNode = null;
        String serverTimezone = null;
        HttpClient httpClient = null;
        boolean isSuccessful = false;
        int retryCount = 0;
        String finalUrl = getURL() + Constants.JIRA_SERVERINFO_API;
        while (!isSuccessful && retryCount <= config.getRetryConfig()) {
            try {
                httpClient = HttpClientBuilder.create().build();
                HttpGet httpGet = new HttpGet(finalUrl);
                httpGet.addHeader("Authorization", Constants.JIRA_BASIC + authCode);
                httpGet.addHeader("Content-Type", "application/json");
                httpGet.addHeader("accept", "application/json");
                httpGet.addHeader("User-Agent", "JiraIntegration");
                HttpResponse response = httpClient.execute(httpGet);
                String responce = EntityUtils.toString(response.getEntity());
                ObjectMapper objectMapper = new ObjectMapper();
                responseNode = objectMapper.readTree(responce);
                if (!responseNode.isNull()) {
                    serverTimezone = responseNode.get(Constants.SERVER_CURRENT_TIME_FIELD).asText();
                    serverTimezone = serverTimezone.substring(serverTimezone.length() - 5);
                }
                return serverTimezone;
            } catch (IOException | ParseException ex) {
                if (retryCount < config.getRetryConfig()) {
                    retryCount++;
                    try {
                        log.error("[Retry  {} ], Waiting for {} sec before trying again ......", retryCount);
                        Thread.sleep(config.getWaitMsBeforeRetry());
                    } catch (InterruptedException e1) {
                        log.error("Thread interrupted ......");
                    }
                    continue;
                } else {
                    throw new ParsingException(Util.format(Constants.AUTHENTICATION_FAILED, new Object[]{ex.getMessage()}));

                }

            }
        }

        return serverTimezone;
    }

    public String getURL() {
        StringBuilder uriBuilder = new StringBuilder();
        uriBuilder.append(config.getProtocolType()).append(Constants.COLON_DOUBLE_SLASH);
        if (config.getJiraPort() != null && !config.getJiraPort().equalsIgnoreCase("")) {
            uriBuilder.append(config.getJiraHostName()).append(Constants.COLON).append(config.getJiraPort()).append(Constants.SLASH);
        } else {
            uriBuilder.append(config.getJiraHostName()).append(Constants.SLASH);
        }
        return uriBuilder.toString();
    }

    public String fieldQuery(List<String> fieldQuery) {
        StringBuilder query = new StringBuilder();
        int fieldCount = 0;
        for (String field : fieldQuery) {
            if (fieldCount == 0) {
                query.append("'").append(field).append("'");
            } else {
                query.append(",").append("'").append(field).append("'");
            }
            fieldCount += 1;
        }
        return query.toString();
    }

    public String getAuthCode(final String userName, String password) {
        byte[] encoded = Base64.encodeBase64((userName + ":" + password).getBytes());
        return new String(encoded);
    }

    public String getSearchUrl(final Integer maxResults, final Integer startAt, final String searchJql, final String fields) {
        String searchString = null;
        String url = getURL();
        try {
            if (fields != null) {
                searchString = String.format(url + Constants.JIRA_SEARCH_API + "?" + fields + "&maxResults=" + maxResults + "&startAt=%d&jql=", startAt) + URLEncoder.encode(searchJql, "UTF-8");
            } else {
                searchString = String.format(url + Constants.JIRA_SEARCH_API + "?maxResults=" + maxResults + "&startAt=%d&jql=", startAt) + URLEncoder.encode(searchJql, "UTF-8");
            }
        } catch (UnsupportedEncodingException ex) {
            log.error("Encoding UTF-8 is not supported");
        }
        return searchString;
    }

    public String buildJQLQuery(Map<String, List<String>> filter, Date startDate, Date endDate, String jqlQuery) {
        StringBuilder searchQuery = new StringBuilder();
        StringBuilder finalSearchQuery = new StringBuilder();
        String startJqlDate = getJQLTimeFormat(startDate);
        String endJqlDate = getJQLTimeFormat(endDate);
        searchQuery.append("( updated >= '").append(startJqlDate).append("' and updated <= '").append(endJqlDate).append("') ORDER BY updated");
        int queryCounter = 0;
        if (jqlQuery == null || jqlQuery.isEmpty()) {
            for (Map.Entry<String, List<String>> entry : filter.entrySet()) {
                List<String> values = entry.getValue();
                if (values.size() > 0) {
                    if (queryCounter == 0) {
                        finalSearchQuery.append(entry.getKey()).append(Constants.IN_OPERATOR).append(Constants.OPEN_PRANETHESIS).append(fieldQuery(values)).append(Constants.CLOSE_PRANETHESIS);
                    } else {
                        finalSearchQuery.append(Constants.JQL_AND_OPERATOR).append(entry.getKey()).append(Constants.IN_OPERATOR).append(Constants.OPEN_PRANETHESIS).append(fieldQuery(values)).append(Constants.CLOSE_PRANETHESIS);
                    }
                    queryCounter += 1;
                }
            }
            if (finalSearchQuery != null && queryCounter != 0) {
                finalSearchQuery.append(Constants.JQL_AND_OPERATOR).append(searchQuery.toString());
            } else {
                finalSearchQuery.append(searchQuery.toString());
            }
        } else {
            finalSearchQuery.append(jqlQuery).append(Constants.JQL_AND_OPERATOR).append(searchQuery.toString());
        }
        return finalSearchQuery.toString();
    }

    public String getJQLTimeFormat(Date date) {

        ZonedDateTime utcTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneOffset.UTC);
        ZonedDateTime serverDateTime = utcTime.withZoneSameInstant(ZoneId.of(serverTimeZone));
        return DateTimeFormatter.ofPattern(JQL_TIMESTAMP_FORMAT).format(serverDateTime);

    }
}
