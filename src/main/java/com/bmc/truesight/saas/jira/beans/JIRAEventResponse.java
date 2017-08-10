package com.bmc.truesight.saas.jira.beans;

import java.util.List;

public class JIRAEventResponse {

    private List<TSIEvent> validEventList;
    private List<TSIEvent> invalidEventList;

    public List<TSIEvent> getInvalidEventList() {
        return invalidEventList;
    }

    public void setInvalidEventList(List<TSIEvent> invalidEventList) {
        this.invalidEventList = invalidEventList;
    }

    public List<TSIEvent> getValidEventList() {
        return validEventList;
    }

    public void setValidEventList(List<TSIEvent> validEventList) {
        this.validEventList = validEventList;
    }

  
}
