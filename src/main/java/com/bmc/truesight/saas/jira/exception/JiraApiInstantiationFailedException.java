package com.bmc.truesight.saas.jira.exception;

public class JiraApiInstantiationFailedException extends Exception {

    private static final long serialVersionUID = -7698012939089656739L;

    public JiraApiInstantiationFailedException() {
        super();
    }

    public JiraApiInstantiationFailedException(String message) {
        super(message);
    }

}
