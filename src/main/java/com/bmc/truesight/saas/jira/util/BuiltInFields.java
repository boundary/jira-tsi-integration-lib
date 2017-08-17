package com.bmc.truesight.saas.jira.util;

/**
 *
 * @author Santosh Patil
 * @Date 17-08-2017
 */
public enum BuiltInFields {

    VERSION("versions"),
    ISSUELINKS("issuelinks"),
    COMPONENTS("components"),
    LABLES("labels"),
    FIXVERSION("fixVersions");
    private final String name;

    BuiltInFields(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     *
     * @return
     */
    public String getField() {
        return name;
    }

    public static BuiltInFields findByNamespace(String namesapce) {
        for (BuiltInFields s : BuiltInFields.values()) {
            if (s.getField().equals(namesapce)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unable to find Field: " + namesapce);
    }

}
