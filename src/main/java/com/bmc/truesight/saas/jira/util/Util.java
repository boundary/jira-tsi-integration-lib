package com.bmc.truesight.saas.jira.util;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Santosh Patil
 * @Date 28-07-2017
 */
public class Util {

    private static final Logger LOG = LoggerFactory.getLogger(Util.class);
    Properties prop = new Properties();
    InputStream input = null;
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public static String StringToDate(final String dateString) {
        DateTime dateTime = new DateTime(dateString);
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        return DATE_FORMAT.format(dateTime);
    }

    public static String getURL(final String hostName, final String portNumber, final String userName, final String password, final String protocalType) {
        StringBuilder uriBuilder = new StringBuilder();
        uriBuilder.append(protocalType).append(Constants.COLON_DOUBLE_SLASH);
        if (portNumber != null && !portNumber.equalsIgnoreCase("")) {
            uriBuilder.append(hostName).append(Constants.COLON).append(portNumber).append(Constants.SLASH);
        } else {
            uriBuilder.append(hostName).append(Constants.SLASH);
        }
        return uriBuilder.toString();
    }

    public static String getAuthCode(final String userName, String password) {
        byte[] encoded = Base64.encodeBase64((userName + ":" + password).getBytes());
        return new String(encoded);
    }

    public static String jqlBuilder(final String url, final Integer maxResults, final Integer startAt, final String searchJql, final String fields) {
        String searchString = null;
        try {
            if (fields != null) {
                searchString = String
                        .format(url + Constants.JIRA_SEARCH_API + "?" + fields + "&maxResults=" + maxResults + "&startAt=%d&jql=", startAt)
                        + URLEncoder.encode(searchJql, "UTF-8");
            } else {
                searchString = String
                        .format(url + Constants.JIRA_SEARCH_API + "?maxResults=" + maxResults + "&startAt=%d&jql=", startAt)
                        + URLEncoder.encode(searchJql, "UTF-8");
            }
        } catch (UnsupportedEncodingException ex) {
            java.util.logging.Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
        return searchString;
    }

    public static Date format(String dateString) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = formatter.parse(dateString);
        return date;
    }

    public static String singleQuote(String value) {
        value = "'" + value + "'";
        return value;
    }

    public static String JiraformatedDateAndTime(Date date) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        String jiraDateAndTimeFormat = formatter.format(date);
        return jiraDateAndTimeFormat;
    }

    public static long convertIntoUTC(String createdDate) {
        DateTime dateTime = new DateTime(createdDate, DateTimeZone.UTC);
        return dateTime.getMillis();
    }
}
