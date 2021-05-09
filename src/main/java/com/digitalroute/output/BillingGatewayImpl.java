// Copyright 2000-2017 Digital Route AB. All rights reserved.
// DIGITAL ROUTE AB PROPRIETARY/CONFIDENTIAL.
// Use is subject to license terms.
//

package com.digitalroute.output;

public class BillingGatewayImpl implements BillingGateway {

	@Override
	public void beginBatch() {
		// TODO Auto-generated method stub
		
	}

	@Override
	//To be called for every aggregated call record
	public void consume(String callId, int seqNum, String aNum, String bNum, byte causeForOutput, int duration) {
		// TODO Auto-generated method stub
		
		
	}

	@Override
	public void endBatch(long totalDuration) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void logError(ErrorCause errorCause, String callId, int seqNum, String aNum, String bNum) {
		// TODO Auto-generated method stub
		
	}
 
}

