package com.bmc.truesight.saas.jira.util;

import org.joda.time.DateTime;

import java.util.Date;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * * @author Santosh Patil
 * @Date 25-01-2017
 * DateTime wrapper which caches the Date object at construction to avoid creating duplicate Date objects from calling
 * toDate repeatedly on DateTime
 */
//@Immutable
public class CachedDateTime {

    private final DateTime value;
    private final Date valueAsDate;

    /**
     * Creates new immutable DateTime with now as the value
     */
    public CachedDateTime() {
        this.value = new DateTime();
        this.valueAsDate = value.toDate();
    }

    public CachedDateTime(DateTime value) {
        this.value = checkNotNull(value);
        this.valueAsDate = value.toDate();
    }

    public Date toDate() {
        return valueAsDate;
    }

    public DateTime get() {
        return value;
    }
}
