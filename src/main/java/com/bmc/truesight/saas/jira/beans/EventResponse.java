package com.bmc.truesight.saas.jira.beans;

import java.util.List;

public class EventResponse {

    private List<TSIEvent> validEventList;
    private int largeInvalidEventCount;

    public List<TSIEvent> getValidEventList() {
        return validEventList;
    }

    public void setValidEventList(List<TSIEvent> validEventList) {
        this.validEventList = validEventList;
    }

    public int getLargeInvalidEventCount() {
        return largeInvalidEventCount;
    }

    public void setLargeInvalidEventCount(int largeInvalidEventCount) {
        this.largeInvalidEventCount = largeInvalidEventCount;
    }
}
