package com.bmc.truesight.saas.jira.util;

import com.bmc.truesight.saas.jira.beans.TSIEvent;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * String utilities
 *
 * @author Santosh Patil
 *
 */
public class StringUtil {

    private static final Logger log = LoggerFactory.getLogger(StringUtil.class);

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

    public static boolean isValidValue(String inputString) {
        String[] strlCharactersArray = new String[inputString.length()];
        for (int i = 0; i < inputString.length(); i++) {
            strlCharactersArray[i] = Character
                    .toString(inputString.charAt(i));
        }
        int count = 0;
        for (String strlCharactersArray1 : strlCharactersArray) {
            if (Constants.SPECIAL_CHARACTOR.contains(strlCharactersArray1)) {
                count++;
            }
        }
        if (inputString != null && count == 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isValidApplicationIdlength(String applicationId) {
        boolean isValidId = true;
        if (applicationId != null) {
            if (applicationId.length() <= Constants.APPLICATION_LENGTH) {
            } else {
                isValidId = false;
            }
        }
        return isValidId;
    }
}
