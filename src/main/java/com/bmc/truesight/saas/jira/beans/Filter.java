package com.bmc.truesight.saas.jira.beans;

import java.util.List;
import java.util.Map;

/**
 * This is a POJO class, which is mapped to the configuration field (ie config)
 * in incident/change json template. The fields contain the jira access details,
 * TSI details and other configuration
 *
 * @author Santosh Patil
 */
public class Filter {

    private Map<String, List<String>> filter;

    public Map<String, List<String>> getFilter() {
        return filter;
    }

    public void setFilter(Map<String, List<String>> filter) {
        this.filter = filter;
    }

}
