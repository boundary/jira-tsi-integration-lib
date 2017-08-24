package com.bmc.truesight.saas.jira.util;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Santosh Patil
 */
public class searchQueryBuilder {
    
    public static String buildJQLQuery(Map<String, List<String>> filter, String startDate, String endDate, final String jqlQuery) throws ParseException {
        StringBuilder searchQuery = new StringBuilder();
        StringBuilder finalSearchQuery = new StringBuilder();
        searchQuery.append("updated >= '").append(startDate)
                .append("' and updated <= '").append(endDate)
                .append("' ORDER BY updated");
        int queryCounter = 0;
        if (jqlQuery == null || jqlQuery.isEmpty()) {
            for (Map.Entry<String, List<String>> entry : filter.entrySet()) {
                List<String> values = entry.getValue();
                if (values.size() > 0) {
                    if (queryCounter == 0) {
                        finalSearchQuery.append(entry.getKey()).append(Constants.IN_OPERATOR).append(Constants.OPEN_PRANETHESIS).append(fieldQuery(values)).append(Constants.CLOSE_PRANETHESIS);
                    } else {
                        finalSearchQuery.append(Constants.JQL_AND_OPERATOR).append(entry.getKey()).append(Constants.IN_OPERATOR).append(Constants.OPEN_PRANETHESIS).append(fieldQuery(values)).append(Constants.CLOSE_PRANETHESIS);
                    }
                    queryCounter += 1;
                }
            }
            if (finalSearchQuery != null && queryCounter != 0) {
                finalSearchQuery.append(Constants.JQL_AND_OPERATOR).append(searchQuery.toString());
            } else {
                finalSearchQuery.append(searchQuery.toString());
            }
        } else {
            finalSearchQuery.append(jqlQuery).append(Constants.JQL_AND_OPERATOR).append(searchQuery.toString());
        }
        return finalSearchQuery.toString();
    }
    
    public static String fieldQuery(List<String> fieldQuery) {
        StringBuilder query = new StringBuilder();
        int fieldCount = 0;
        for (String field : fieldQuery) {
            if (fieldCount == 0) {
                query.append("'").append(field).append("'");
            } else {
                query.append(",").append("'").append(field).append("'");
            }
            fieldCount += 1;
        }
        return query.toString();
    }
}
