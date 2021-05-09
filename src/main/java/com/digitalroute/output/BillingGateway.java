// Copyright 2000-2017 Digital Route AB. All rights reserved.
// DIGITAL ROUTE AB PROPRIETARY/CONFIDENTIAL.
// Use is subject to license terms.
//

package com.digitalroute.output;

public interface BillingGateway {
    enum ErrorCause {NO_MATCH,
        DUPLICATE_SEQ_NO}

    /**
     * beginBatch should be called when starting to read a input file. Before aggregation is started
     */
    void beginBatch();

    /**
     * Consume a record from aggregation ether by encountering causeForOutput '2' or when file processing is
     * completed, before calling {@link #endBatch(long)}
     * @param callId Character representing the call
     * @param seqNum Highest seqNum in the aggregated call session
     * @param aNum From number
     * @param bNum To number
     * @param causeForOutput Highest cause for output unless it contains incomplete records, then it should contain 0
     * @param duration Accumulated duration for the aggregated call session
     */
    void consume(String callId, int seqNum, String aNum, String bNum, byte causeForOutput, int duration);

    /**
     * Should be called after all call records in a input file has been processed
     * @param totalDuration Total duration of all the flushed call sessions
     */
    void endBatch(long totalDuration);

    void logError(ErrorCause errorCause, String callId, int seqNum, String aNum, String bNum);
}

