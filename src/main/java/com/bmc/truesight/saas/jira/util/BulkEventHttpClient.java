package com.bmc.truesight.saas.jira.util;

import com.bmc.truesight.saas.jira.beans.Result;
import com.bmc.truesight.saas.jira.beans.TSIEvent;
import java.util.List;

import com.bmc.truesight.saas.remedy.integration.exception.BulkEventsIngestionFailedException;

/**
 * This class sends the lists of events to TSI.
 *
 * @author Santosh Patil
 */
public interface BulkEventHttpClient {

    Result pushBulkEventsToTSI(List<TSIEvent> bulkEvents) throws BulkEventsIngestionFailedException;

}
