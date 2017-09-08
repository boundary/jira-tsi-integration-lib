package com.bmc.truesight.saas.jira.in;

import com.bmc.truesight.saas.jira.beans.Template;
import com.bmc.truesight.saas.jira.exception.JiraApiInstantiationFailedException;
import com.bmc.truesight.saas.jira.exception.JiraLoginFailedException;
import com.bmc.truesight.saas.jira.exception.ParsingException;

/**
 * This interface defines the parsing of the incidentTemplate or ChangeTemplate
 * Json.
 *
 * @author Santosh Patil
 *
 */
public interface TemplateParser {

    /**
     * This method reads and parse from a JSON String. This function is used in
     * case template JSON is available in String
     *
     * @param configJson Template JSON String
     * @return {@link Template}
     * @throws ParsingException Throws this exception if JSON parsing is not
     * successful
     */
    Template readParseConfigJson(Template defaultTemplate, String configJson) throws ParsingException;

    /**
     * This method reads and parse from a JSON file. This function is used in
     * case template JSON is available in json file
     *
     * @param defaultTemplate
     * @param fileName Template JSON fileName
     * @return {@link Template}
     * @throws ParsingException Throws this exception if JSON parsing is not
     * successful
     */
    Template readParseConfigFile(Template defaultTemplate, String fileName) throws ParsingException;

    Template ignoreFields(Template defaultTemplate) throws JiraApiInstantiationFailedException,JiraLoginFailedException;
}
