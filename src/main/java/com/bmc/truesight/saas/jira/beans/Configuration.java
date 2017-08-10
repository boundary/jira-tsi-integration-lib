package com.bmc.truesight.saas.jira.beans;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * This is a POJO class, which is mapped to the configuration field (ie config)
 * in incident/change json template. The fields contain the jira access details,
 * TSI details and other configuration
 *
 * @author Santosh Patil
 */
public class Configuration {

    private String jiraHostName;
    private Integer jiraPort;
    private String jiraUserName;
    private String jiraPassword;
    private String tsiEventEndpoint;
    private String tsiApiToken;
    private Integer chunkSize;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private Date startDateTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private Date endDateTime;
    private Integer retryConfig;
    private Integer waitMsBeforeRetry;
    private String protocalType;

    public String getProtocalType() {
        return protocalType;
    }

    public void setProtocalType(String protocalType) {
        this.protocalType = protocalType;
    }

    public Date getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(Date endDateTime) {
        this.endDateTime = endDateTime;
    }

    public String getJiraHostName() {
        return jiraHostName;
    }

    public void setJiraHostName(String jiraHostName) {
        this.jiraHostName = jiraHostName;
    }

    public Integer getJiraPort() {
        return jiraPort;
    }

    public void setJiraPort(Integer jiraPort) {
        this.jiraPort = jiraPort;
    }

    public String getJiraUserName() {
        return jiraUserName;
    }

    public void setJiraUserName(String jiraUserName) {
        this.jiraUserName = jiraUserName;
    }

    public String getJiraPassword() {
        return jiraPassword;
    }

    public void setJiraPassword(String jiraPassword) {
        this.jiraPassword = jiraPassword;
    }

    public String getTsiApiToken() {
        return tsiApiToken;
    }

    public void setTsiApiToken(String tsiApiToken) {
        this.tsiApiToken = tsiApiToken;
    }

    public Integer getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(Integer chunkSize) {
        this.chunkSize = chunkSize;
    }

    public Date getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(Date startDateTime) {
        this.startDateTime = startDateTime;
    }

    public Integer getRetryConfig() {
        return retryConfig;
    }

    public void setRetryConfig(Integer retryConfig) {
        this.retryConfig = retryConfig;
    }

    public String getTsiEventEndpoint() {
        return tsiEventEndpoint;
    }

    public void setTsiEventEndpoint(String tsiEventEndpoint) {
        this.tsiEventEndpoint = tsiEventEndpoint;
    }

    public Integer getWaitMsBeforeRetry() {
        return waitMsBeforeRetry;
    }

    public void setWaitMsBeforeRetry(Integer waitMsBeforeRetry) {
        this.waitMsBeforeRetry = waitMsBeforeRetry;
    }

}
