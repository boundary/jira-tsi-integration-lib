package com.bmc.truesight.saas.jira.exception;

import com.bmc.truesight.saas.jira.in.TemplateValidator;

/**
 * This Exception is thrown in case of validation failure by
 * {@link TemplateValidator}
 *
 * @author Santosh Patil
 *
 */
public class ValidationException extends Exception {

    private static final long serialVersionUID = -5950039647191513352L;

    public ValidationException() {
        super();
    }

    public ValidationException(String message) {
        super(message);
    }

}
