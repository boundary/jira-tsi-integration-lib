package com.bmc.truesight.saas.jira.in;

import java.util.List;

import com.bmc.truesight.saas.jira.beans.Result;
import com.bmc.truesight.saas.jira.beans.TSIEvent;
import com.bmc.truesight.saas.jira.exception.BulkEventsIngestionFailedException;
import com.bmc.truesight.saas.jira.exception.TsiAuthenticationFailedException;

/**
 * This class sends the lists of events to TSI.
 *
 * @author Santosh Patil
 */
public interface BulkEventHttpClient {

    Result pushBulkEventsToTSI(List<TSIEvent> bulkEvents) throws BulkEventsIngestionFailedException, TsiAuthenticationFailedException;

}
