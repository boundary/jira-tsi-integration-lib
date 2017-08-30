package com.bmc.truesight.saas.jira.in;

import com.bmc.truesight.saas.jira.beans.Template;
import com.bmc.truesight.saas.jira.exception.ParsingException;

public interface TemplatePreParser {

    /**
     * This method reads and parse a default JSON configuration file available
     * in the library resources and returns a template with default values. This
     * function should be called to have default configuration values already
     * available, The explicit configuration is passed in
     * {@link TemplateParser}, which overrides these values.
     *
     * @return {@link Template}
     * @throws ParsingException Throws this exception if default JSON parsing is
     * not successful
     */
    Template loadDefaults() throws ParsingException;

}
