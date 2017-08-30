package com.bmc.truesight.saas.jira.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Santosh Patil
 * @Date 01-08-2017
 */
public class Constants {

    Map<String, String> jsonKeysForSystemBuilinFields = new HashMap<>();
    public static int CHUNK_SIZE = 1000;
    public static int MAX_RETRY = 3;
    public static final int TSI_EVENT_SUCCESS = 202;
    public static final String COLON_DOUBLE_SLASH = "://";
    public static final String COLON = ":";
    public static final String SLASH = "/";
    public static final String SYSTEM_FIELDS = "JIRA";
    public static final String CUSTOM_FIELDS = "CUSTOM";
    public static final String SUPPORT_ALL = "all";
    public static final String JIRA_SEARCH_API = "rest/api/2/search";
    public static final String JIRA_BASIC = "Basic ";

    public static final Integer MAX_PROPERTY_FIELD_SUPPORTED = 128;
    public static Long MAX_EVENT_SIZE_ALLOWED_BYTES = 32000l;
    public static final int EVENT_INGESTION_STATE_SUCCESS = 200;
    public static final int EVENT_INGESTION_STATE_ACCEPTED = 202;
    public static final String JSON_ISSUES_NODE = "issues";
    public static final String JSON_FILED_NODE = "fields";
    public static final String JSON_ISSUES_TOTAL_KEY = "total";

    public static final String VALIDATION_MSG = "{} configuration file validation succesfull";
    public static final String CONFIG_NODE_NAME = "config";
    public static final String CONFIG_HOSTNAME_NODE_NAME = "jiraHostName";
    public static final String CONFIG_PORT_NODE_NAME = "jiraPort";
    public static final String CONFIG_USERNAME_NODE_NAME = "jiraUserName";
    public static final String CONFIG_PASSWORD_NODE_NAME = "jiraPassword";
    public static final String CONFIG_TSIENDPOINT_NODE_NAME = "tsiEventEndpoint";
    public static final String CONFIG_TSITOKEN_NODE_NAME = "tsiApiToken";
    public static final String CONFIG_CHUNKSIZE_NODE_NAME = "chunkSize";
    public static final String CONFIG_ISSUES_TYPE_CONDFIELDS_NODE_NAME = "issueTypeConditionFields";
    public static final String CONFIG_CONDSTATUSFIELDS_NODE_NAME = "statusConditionFields";
    public static final String CONFIG_RETRY_NODE_NAME = "retryConfig";
    public static final String CONFIG_WAITSMS_NODE_NAME = "waitMsBeforeRetry";
    public static final String EVENTDEF_NODE_NAME = "eventDefinition";
    public static final String PLACEHOLDER_START_TOKEN = "@";
    public static final Integer CONFIG_CHUNK_SIZE = 1000;
    public static final String CONFIG_PRIORITY_CONDITION_FIELDS = "priorityConditionFields";
    public static final String CONFIG_FILTER_NODE = "filter";
    public static final String CONFIG_START_DATE_CONDITION_FIELDS = "startDateTime";
    public static final String CONFIG_END_DATE_CONDITION_FIELDS = "endDateTime";
    public static final String JIRA_TEMPLATE_PATH = "jiraDefaultTemplate.json";
    public static final String JIRA_NONE_FIELD = "fields=*none";
    public static final String CONFIG_PROTOCOL_TYPE = "protocolType";
    //Messages
    public static final String CONFIG_FILE_NOT_FOUND = "Could not read the configuration file from location({0}) or it has different encoding than UTF8";
    public static final String CONFIG_FILE_NOT_VALID = "The configuration json is not a valid JSON,{0})";
    public static final String CONFIG_PROPERTY_NOT_FOUND = "There is an issue with 'config' field in default json file. {0}";
    public static final String CONFIG_PROPERTY_NOT_VALID = "Either the configuration file does not contain proper 'config' property, or 'fields' are not correct. {0}";
    public static final String PAYLOAD_PROPERTY_NOT_FOUND = "Either the configuration file does not contain proper 'eventDefinition' property, or 'fields' are not correct. {0}";
    public static final String PLACEHOLDER_PROPERTY_NOT_CORRECT = "The fields  for property {0} are not correct";
    public static final String CONFIG_VALIDATION_FAILED = "The fields for config elements are empty, it should be nonempty";
    public static final String PAYLOAD_PLACEHOLDER_DEFINITION_MISSING = "The definition for payload placeholder {0} is missing in the configuration file";
    public static final String jira_LOGIN_FAILED = "Login failed to jira Server, ({0})";
    public static final String FACTORY_INITIALIZATION_EXCEPTION = "Failed to create proper instance in the factory, Please ensure you are using right parameters";
    public static final String PROPERTY_NAME_INVALID = "The property \"{0}\" is not a valid Field Name, Only AlphaNumeric and Underscore are allowed characters in the Field Names.";
    public static final String CONFIG_DATE_FORMAT = "There is an issue with date fromat 'filter' field in default json file. {0}";
    public static final String AUTHENTICATION_FAILED = "Authentication failed";
    public static final String EVENT_FIELD_MISSING = "FingerPrintFields values are invalid, field \"{0}\" does not exist in Event fields. Accepted fields are {1}";
    ;
    public static final String EVENT_PROPERTY_FIELD_MISSING = "FingerPrintFields values are invalid, field \"{0}\" does not exist in user fields (in properties of eventDefinition)";
    ;
    public static final String APPLICATION_NAME_NOT_FOUND = "app_id field is missing from properties, please include one app_id field in properties";
    public static final String APPLICATION_LENGTH_INVALID = "app_id field value should be less than {}, please reduce the app_id length";
    public static final String PAYLOAD_PLACEHOLDER_FIELD_MISSING = "The field \"{0}\" does not exist in Template Configuration, please review the mapping";
    public static final String DATERANGE_VALIDATION_FAILED = "Start date & End date in config is not appropriate, start date should not be greater than end date and end date should not be greater than current date.";
    ;
	//Messages
    String LOGIN_FAILED = "Login failed to Remedy Server, ({0})";
    public static final String PROPERTY_FIELD_COUNT_EXCEEDS = "Event properties field count of {0} exceeds maximum of {1}.";
    public static final String JSON_NODE_RESOLUTION = "resolution";
    public static final String ID = "@ID";
    public static final String TSI_SEVERITY = "INFO";
    public static final String FILTER_CONFIG_NOT_FOUND = "Either the configuration file does not contain proper 'filter' property, or 'fields' are not correct. {0}";
    public static final String FIELD_ID = "id";
    public static String JQL_STATUS = "status in ";
    public static String JQL_AND_OPERATOR = " AND ";
    public static String JQL_ISSUES_TYPE = "issueType in ";
    public static String JQL_PRIORITY_TYPE = "priority in ";
    public static String OPEN_PRANETHESIS = "(";
    public static String CLOSE_PRANETHESIS = ")";
    public static String OR_OPERATOR = " OR ";
    public static String IN_OPERATOR = " in ";
    public static final String FILED_KEY = "@KEY";
    public static final String CUSTOM_FIELD_PREFIX = "customfield_";
    public static final String FIELD_FETCH_KEY = "key";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_VALUE = "value";
    public static final String APPLICATION_ID = "app_id";
    public static final String APPLICATION_NAME_INVALID = "The application \"{0}\" is not a valid , Only AlphaNumeric, Hyphen, Colon, Dot, Percentage, Star, Slash, at(@)  and Underscore are allowed characters in the application name.";
    public static final String SPECIAL_CHARACTOR = "!$%&'()+,;<=>?[]^`{|}~";
    public static String JIRA_TEMPLATE_FILE_NAME = "jiraDefaultTemplate.json";
    public static String JQL_QUERY_FIELD = "jqlQuery";
    public static String HOST = "Host";
    public static int APPLICATION_LENGTH = 100;
    public static final String APPLICATION_LENGTH_MEG = "The application \"{0}\" is not a valid , Maximum allowed characters length in apllication name/id is (100 characters)";
    public static final String JIRA_SERVERINFO_API = "rest/api/2/serverInfo";
    public static final String SERVER_CURRENT_TIME_FIELD = "serverTime";
    public static final String TEMPLATE_PATH = File.separator + "jiraTemplate.json";
    public static final String CONFIG_THREADS_NODE_NAME = "threadCount";
    public static final int UNAUTHORIZED_STATUS = 401;
    public static final List<String> FINGERPRINT_EVENT_FIELDS = new ArrayList<String>() {
        {
            add("source.name");
            add("title");
            add("status");
            add("severity");
            add("message");
        }
    };

}
