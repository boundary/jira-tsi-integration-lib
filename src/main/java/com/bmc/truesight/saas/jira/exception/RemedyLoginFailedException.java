package com.bmc.truesight.saas.jira.exception;


/**
 * This exception is thrown when login attempt fails in {@link RemedyReader}
 *
 * @author Santosh Patil
 *
 */
public class RemedyLoginFailedException extends Exception {

    private static final long serialVersionUID = -4739634227509447336L;

    public RemedyLoginFailedException() {
        super();
    }

    public RemedyLoginFailedException(String message) {
        super(message);
    }

}
