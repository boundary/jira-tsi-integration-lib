package com.bmc.truesight.saas.jira.util;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bmc.truesight.saas.jira.beans.TSIEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author Santosh Patil,vitiwari
 * @Date 28-07-2017
 */
public class Util {

    private static final Logger log = LoggerFactory.getLogger(Util.class);

    Properties prop = new Properties();
    InputStream input = null;
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public static String StringToDate(final String dateString) {
        DateTime dateTime = new DateTime(dateString);
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        return DATE_FORMAT.format(dateTime);
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

    public static long convertIntoUTC(String createdDate) {
        if (createdDate.equalsIgnoreCase("") || createdDate == null) {
            return 0L;
        } else {
            DateTime dateTime = new DateTime(createdDate, DateTimeZone.UTC);
            return dateTime.getMillis();
        }
    }

    public static boolean isCustomField(String value) {
        return value.contains(Constants.CUSTOM_FIELD_PREFIX);
    }

    public static String format(String template, Object[] args) {
        MessageFormat fmt = new MessageFormat(template);
        return fmt.format(args);
    }

    public final static boolean isValidJavaIdentifier(String s) {
        // an empty or null string cannot be a valid identifier
        if (s == null || s.length() == 0) {
            return false;
        }
        char[] c = s.toCharArray();
        if (!Character.isJavaIdentifierStart(c[0]) || (c[0] == '$')) {
            return false;
        }
        for (int i = 1; i < c.length; i++) {
            if (!Character.isJavaIdentifierPart(c[i]) || (c[i] == '$')) {
                return false;
            }
        }

        return true;
    }

    public static boolean isObjectJsonSizeAllowed(TSIEvent event) {
        boolean isAllowed = true;
        if (event != null) {
            ObjectMapper mapper = new ObjectMapper();
            String eventJson;
            try {
                eventJson = mapper.writeValueAsString(event);
                final byte[] utf8Bytes = eventJson.getBytes("UTF-8");
                if (utf8Bytes.length >= Constants.MAX_EVENT_SIZE_ALLOWED_BYTES) {
                    isAllowed = false;
                }
            } catch (JsonProcessingException e) {
                log.error("Event to json conversion has some exception, {}", new Object[]{e.getMessage()});
            } catch (UnsupportedEncodingException e) {
                log.error("Event to json conversion has some problem in encoding, {}", new Object[]{e.getMessage()});
            }
        }
        return isAllowed;

    }
}
