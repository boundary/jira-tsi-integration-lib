package com.bmc.truesight.saas.jira.api;

import com.atlassian.jira.rest.client.api.JiraRestClient;
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
import com.bmc.truesight.saas.jira.exception.ParsingException;
import com.bmc.truesight.saas.jira.util.Constants;
import com.bmc.truesight.saas.jira.util.StringUtil;
import com.bmc.truesight.saas.jira.util.Util;
import com.bmc.truesight.saas.remedy.integration.exception.BulkEventsIngestionFailedException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author Santosh Patil
 * @Date 27/07/2017
 */
public class JiraAPI {

    public static JiraRestClient getJiraRestClient(final String hostName, final String portNumber, final String userName, final String password, final String protocalType) {
        URI uri;
        try {
            if (portNumber != null && !portNumber.equalsIgnoreCase("")) {
                uri = new URI(Util.getURL(hostName, portNumber, userName, password, protocalType));
            } else {
                uri = new URI(Util.getURL(hostName, null, userName, password, protocalType));
            }
            final AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
            final JiraRestClient jiraRestClient = factory.createWithBasicHttpAuthentication(uri, userName, password);
            return jiraRestClient;
        } catch (URISyntaxException ex) {
            System.err.println("URISyntaxException" + "{} " + ex.getMessage());
        }
        return null;
    }

    public static boolean isValidCredentials(JiraRestClient jiraRestClient, final String userName) {
        boolean isValid = true;
        try {
            Promise<User> promise = jiraRestClient.getUserClient().getUser(userName);
            User user = promise.claim();
        } catch (Exception ex) {
            System.err.println(Constants.AUTHENTICATED_FAILED + "{} " + ex.getMessage());
            isValid = false;
        }
        return isValid;
    }

    public static Map<Long, String> getTypeOfStatus(JiraRestClient jiraRestClient) {
        Map<Long, String> typeOfStatus = new HashMap<>();
        for (Status status : jiraRestClient.getMetadataClient().getStatuses().claim()) {
            typeOfStatus.put(status.getId(), status.getName());
        }
        return typeOfStatus;
    }

    public static Map<Long, String> getTypeOfIssues(JiraRestClient jiraRestClient) {
        Map<Long, String> typeOfIssues = new HashMap<>();
        for (IssueType issueType : jiraRestClient.getMetadataClient().getIssueTypes().claim()) {
            typeOfIssues.put(issueType.getId(), issueType.getName());
        }
        return typeOfIssues;
    }

    public static Map<String, String> getTypeFields(JiraRestClient jiraRestClient) {
        Map<String, String> fields = new HashMap<>();
        for (Field field : jiraRestClient.getMetadataClient().getFields().claim()) {
            fields.put(field.getId(), field.getName());
        }
        return fields;
    }

    public static Map<String, String> getSystemFields(JiraRestClient jiraRestClient) {
        Map<String, String> systemFields = new HashMap<>();
        for (Field field : jiraRestClient.getMetadataClient().getFields().claim()) {
            if (field.getFieldType().name().equalsIgnoreCase(Constants.SYSTEM_FIELDS)) {
                systemFields.put(field.getId(), field.getName());
            }
        }
        return systemFields;
    }

    public static Map<String, String> getCustomFields(JiraRestClient jiraRestClient) {
        Map<String, String> customFields = new HashMap<>();
        for (Field field : jiraRestClient.getMetadataClient().getFields().claim()) {
            if (field.getFieldType().name().equalsIgnoreCase(Constants.CUSTOM_FIELDS)) {
                customFields.put(field.getId(), field.getName());
            }
        }
        return customFields;
    }

    public static List<Issue> issuesList(final String jqlQuery, final JiraRestClient jiraRestClient, final Integer maxResult, final Integer startAt, HashSet<String> set) {
        Promise<SearchResult> searchJqlPromise = jiraRestClient.getSearchClient().searchJql(jqlQuery, maxResult, startAt, set);
        List<Issue> listOfIssues = (List<Issue>) searchJqlPromise.claim().getIssues();
        return listOfIssues;
    }

    public static JsonNode search(final String finalUrl, final String basicAuthCode, Configuration config) throws ParsingException {
        JsonNode responseNode = null;
        HttpClient httpClient = null;
        boolean isSuccessful = false;
        int retryCount = 0;
        while (!isSuccessful && retryCount <= config.getRetryConfig()) {
            try {
                httpClient = HttpClientBuilder.create().build();
                HttpGet httpGet = new HttpGet(finalUrl);
                httpGet.addHeader("Authorization", Constants.JIRA_BASIC + basicAuthCode);
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
                        System.err.println("[Retry  {} ], Waiting for {} sec before trying again ......" + retryCount);
                        Thread.sleep(config.getWaitMsBeforeRetry());
                    } catch (InterruptedException e1) {
                        System.err.println("Thread interrupted ......");
                    }
                    continue;
                } else {
                    throw new ParsingException(StringUtil.format(Constants.AUTHENTICATED_FAILED, new Object[]{ex.getMessage()}));

                }

            }
        }
        return responseNode;
    }

    public static Map<Long, String> getTypeOfPriority(JiraRestClient jiraRestClient) {
        Map<Long, String> typeOfIssues = new HashMap<>();
        for (Priority priority : jiraRestClient.getMetadataClient().getPriorities().claim()) {
            typeOfIssues.put(priority.getId(), priority.getName());
        }
        return typeOfIssues;
    }
}
