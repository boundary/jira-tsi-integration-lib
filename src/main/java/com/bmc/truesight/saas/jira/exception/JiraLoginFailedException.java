package com.bmc.truesight.saas.jira.exception;

/**
 * This exception is thrown when login attempt fails in RemedyReader
 *
 * @author vitiwari
 *
 */
public class JiraLoginFailedException extends Exception {

    private static final long serialVersionUID = -4739634227509447336L;

    public JiraLoginFailedException() {
        super();
    }

    public JiraLoginFailedException(String message) {
        super(message);
    }

}
