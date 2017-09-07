package com.bmc.truesight.saas.jira.beans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 *
 * @author Santosh Patil
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorResponse {

    public String[] getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(String[] errorMessages) {
        this.errorMessages = errorMessages;
    }

    public String[] getWarningMessages() {
        return warningMessages;
    }

    public void setWarningMessages(String[] warningMessages) {
        this.warningMessages = warningMessages;
    }
    private String[] errorMessages;
    private String[] warningMessages;

}
