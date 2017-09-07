package com.bmc.truesight.saas.jira.exception;

/**
 *
 * @author Santosh Patil
 */
public class JiraErrorResponse extends Exception {

    private static final long serialVersionUID = 1L;

    public JiraErrorResponse() {
        super();
    }

    public JiraErrorResponse(String message) {
        super(message);
    }
}
