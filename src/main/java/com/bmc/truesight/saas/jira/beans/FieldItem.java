package com.bmc.truesight.saas.jira.beans;

/**
 * This is a pojo class, which is used in the
 * {@link com.bmc.truesight.saas.remedy.integration.beans.Template Template}
 *
 * @author Santosh Patil
 *
 */
public class FieldItem {

    private String fieldId;
    private String fetchKey;

    public String getFetchKey() {
        return fetchKey;
    }

    public void setFetchKey(String fetchKey) {
        this.fetchKey = fetchKey;
    }
    public String getFieldId() {
        return fieldId;
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

}
