package com.bmc.truesight.saas.jira.util;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Santosh Patil
 */
public class searchQueryBuilder {

    public static String buildJQLQuery(Map<String, List<String>> filter, String startDate, String endDate) throws ParseException {
        StringBuilder searchQuery = new StringBuilder();
        StringBuilder finalSearchQuery = new StringBuilder();
        searchQuery.append("updated >= '").append(startDate)
                .append("' and updated <= '").append(endDate)
                .append("' ORDER BY updated");
        int queryCounter = 0;
        for (Map.Entry<String, List<String>> entry : filter.entrySet()) {
            List<String> values = entry.getValue();
            if (values.size() > 0) {
                if (queryCounter == 0) {
                    finalSearchQuery.append(Constants.OPEN_PRANETHESIS).append(fieldQuery(values, entry.getKey())).append(Constants.CLOSE_PRANETHESIS);
                } else {
                    finalSearchQuery.append(Constants.JQL_AND_OPERATOR).append(Constants.OPEN_PRANETHESIS).append(fieldQuery(values, entry.getKey())).append(Constants.CLOSE_PRANETHESIS);
                }
                queryCounter += 1;
            }
        }
        if (finalSearchQuery != null) {
            finalSearchQuery.append(Constants.JQL_AND_OPERATOR).append(searchQuery.toString());
        }
        return finalSearchQuery.toString();
    }

    public static String fieldQuery(List<String> fieldQuery, String key) {
        StringBuilder query = new StringBuilder();
        int count = 0;
        for (String field : fieldQuery) {
            if (count == 0) {
                query.append(key).append(Constants.EQUAL_OPERATOR).append("'").append(field).append("'");
            } else {
                query.append(Constants.OR_OPERATOR).append(key).append(Constants.EQUAL_OPERATOR).append("'").append(field).append("'");
            }
            count += 1;
        }
        return query.toString();
    }
}
