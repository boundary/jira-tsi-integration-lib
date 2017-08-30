package com.bmc.truesight.saas.jira.exception;

public class JiraReadFailedException extends Exception {

    private static final long serialVersionUID = -6337692208390334342L;

    public JiraReadFailedException() {
        super();
    }

    public JiraReadFailedException(String message) {
        super(message);
    }
}
