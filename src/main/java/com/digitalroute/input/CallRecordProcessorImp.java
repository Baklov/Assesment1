package com.digitalroute.input;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.digitalroute.output.BillingGateway;
import com.digitalroute.output.BillingGateway.ErrorCause;

public class CallRecordProcessorImp implements CallRecordsProcessor {

	private BillingGateway billingGateway;
	long totalDuration=0;
	
	public CallRecordProcessorImp(BillingGateway billingGateway) {
		super();
		this.billingGateway = billingGateway;
	}

	@Override
	public void processBatch(InputStream in) {
		// TODO Auto-generated method stub
		
        try {
        	billingGateway.beginBatch();
////        	String val = "";
//        	java.net.URL test1 = getClass().getResource("/");
//        	java.net.URL test2 = getClass().getClassLoader().getResource("/");            
//        	java.net.URL test3 = getClass().getClassLoader().getResource("../");
//
//            System.out.println(test1.getPath());
//            System.out.println(test2.getPath());
//            System.out.println(test3.getPath());
    		BufferedReader r = new BufferedReader(new InputStreamReader(in));

            // reads each line
            String CDR;
            
            HashMap<String, List> workAggregateHashMap = new HashMap();
            int previousSeqNum=-1;;
			while((CDR = r.readLine()) != null) {
//			   val = val + CDR+"\r\n";
			   String[] splitCDR=splitCDR(CDR);
			   if (splitCDR==null) {
				   throw new Exception("Bed Format of a Call Data Record");
			   }
			   String splitCallIdAndSeqNum[]=splitCDR[0].split(":");
			   if (splitCallIdAndSeqNum.length==2) {
				   //`seqNum` is a unique identifier within the file. If the same seqNum appears a second time, its record should be disregarded from 
				   //aggregation.
				   if (Integer.parseInt(splitCallIdAndSeqNum[1])<=previousSeqNum){
					   if (Integer.parseInt(splitCallIdAndSeqNum[1])==previousSeqNum){
						   billingGateway.logError(ErrorCause.DUPLICATE_SEQ_NO, splitCallIdAndSeqNum[0], Integer.parseInt(splitCallIdAndSeqNum[1]), splitCDR[1], splitCDR[2]);
					   }
					   else {
						   billingGateway.logError(ErrorCause.NO_MATCH, splitCallIdAndSeqNum[0], Integer.parseInt(splitCallIdAndSeqNum[1]), splitCDR[1], splitCDR[2]);
					   }
					   continue;
				   }
				   else {
					   previousSeqNum=Integer.parseInt(splitCallIdAndSeqNum[1]);
				   }
				   String key= splitCallIdAndSeqNum[0]+"-"+splitCDR[1]+"-"+splitCDR[2];
				   
				   if ("_".equals(splitCallIdAndSeqNum[0])) {
					   int minSeqNum=-1;;
					   String callId="";
			// `Incomplete records` (the ones with missing `callId`) never create new aggregation sessions, instead they must be 
			//	   aggregated to the records with matching `aNum` and `bNum`.
						
					   for (String keyTemp: workAggregateHashMap.keySet()) {
						    List list =  workAggregateHashMap.get(keyTemp);
						    if (list.get(2).equals(splitCDR[1])
						    	&& list.get(3).equals(splitCDR[2])) {
			//    If there is more than one match (due to calls with the same `aNum` 
			//    and `bNum` but different `callId`), the `incomplete record` joins the first matching call.
						
						    	if (minSeqNum>-1) {
						    		if(Integer.parseInt(""+list.get(1))<minSeqNum) {
						    			minSeqNum=Integer.parseInt(""+list.get(1));
						    			callId=""+list.get(0);
						    		}
						    	}
						    	else {
						    		minSeqNum=Integer.parseInt(""+list.get(1));
						    		callId=""+list.get(0);
						    	}
						    	
						    }
						}
					   //
					 //  If there is no such match the `incomplete record` is skipped.
					   if (callId.isEmpty()) {
						   billingGateway.logError(ErrorCause.NO_MATCH, splitCallIdAndSeqNum[0], Integer.parseInt(splitCallIdAndSeqNum[1]), splitCDR[1], splitCDR[2]);
							
						   continue;
					   }
					   key= callId+"-"+splitCDR[1]+"-"+splitCDR[2];
				   }
				   if (workAggregateHashMap.containsKey(key)) {
					   int duration=Integer.parseInt((String)workAggregateHashMap.get(key).get(5)) ;
					   duration+= Integer.parseInt(splitCDR[4]);
					   workAggregateHashMap.get(key).set(1, ""+splitCallIdAndSeqNum[1]);
					   workAggregateHashMap.get(key).set(4, ""+splitCDR[3]);
					   workAggregateHashMap.get(key).set(5, ""+duration);
				   }
				   else {
					   List tempList = new ArrayList();
					   tempList.add(splitCallIdAndSeqNum[0]);
					   tempList.add(splitCallIdAndSeqNum[1]);
					   tempList.add(splitCDR[1]);
					   tempList.add(splitCDR[2]);
					   tempList.add(splitCDR[3]);
					   tempList.add(splitCDR[4]);
					   int duration= Integer.parseInt(splitCDR[4]);
					   workAggregateHashMap.put(key, tempList);
				   }
				   

				   //Records for "ongoing calls", with the same `callId, aNum and bNum`, must be aggregated. When you read the "end of call record", 
				   //your aggregated duration must be sent, using the `BillingGateway.consume` method.
//				   if (splitCDR[3].equals("2")) {
//					   billingGateway.consume(
//							   splitCallIdAndSeqNum[0], //callId Call identity string terminated by : . `_` (underscore) represents an incomplete record.
//							   Integer.parseInt(splitCallIdAndSeqNum[1]), //A sequence number terminated by ,
//							   splitCDR[1], //A-number string (the one making the call) terminated by ,
//							   splitCDR[2], //B-number string (the one receiving the call) terminated by ,
//							   Byte.parseByte(splitCDR[3]), //The reason record was created, terminated by , 1-represents ongoing call, 2-represents end of call,0-is used for `incomplete records` (can appear during network problems)
//							   Integer.parseInt((String) workAggregateHashMap.get(key).get(5)) //Duration of the call, in minutes, terminated by new row
//						);
//					   workAggregateHashMap.remove(key);
//				   }
			   }
			   else {
				   throw new Exception("Bed Format of a Call Data Record section CallID or SeqNUm");
			   }
			}
		//	Aggregated call data flushed to BillingGateway must contain the highest `seqNum` within the aggregation session (this rule also
		//	applies for any `incomplete record` within the session). `causeForOutput` should be the highest encountered while aggregating, unless there is an `incomplete record` 
		//	within the session, then it should be `0`.
			//When you reach the end of file, any ongoing aggregation must be sent (flushed) as well.
			for (String keyTemp: workAggregateHashMap.keySet()) {
				List list =  workAggregateHashMap.get(keyTemp);
				totalDuration+=Integer.parseInt((String) list.get(5));
				 billingGateway.consume(
						 (String)list.get(0), //callId Call identity string terminated by : . `_` (underscore) represents an incomplete record.
						 Integer.parseInt((String) list.get(1)), //A sequence number terminated by ,
						 (String)list.get(2), //A-number string (the one making the call) terminated by ,
						 (String)list.get(3), //B-number string (the one receiving the call) terminated by ,
						 Byte.parseByte((String) list.get(4)), //The reason record was created, terminated by , 1-represents ongoing call, 2-represents end of call,0-is used for `incomplete records` (can appear during network problems)
						 Integer.parseInt((String) list.get(5)) //Duration of the call, in minutes, terminated by new row
					);
			}
			billingGateway.endBatch(totalDuration);
	        in.close();
	      //  System.out.println("File1: " + val);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

	}

	private String[] splitCDR(String cDR) {
		// TODO Auto-generated method stub
//		1B:1,111111,222222,2,5
//		1K:2,555555,666666,1,15
//		1K:3,555555,666666,1,15
//		1K:4,555555,666666,2,2
		String strCDR[]=cDR.split(",");
		if (strCDR.length==5) {
			return strCDR;
		}
		else {
			String strCDR1[]= cDR.split(";");
			if (strCDR1.length==5) {
				return strCDR1;
			}		
			
		}
		return null;
		
	}

}
