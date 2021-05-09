package test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.digitalroute.Application;
import com.digitalroute.input.CallRecordProcessorImp;
import com.digitalroute.input.CallRecordsProcessor;
import com.digitalroute.output.BillingGateway;
import com.digitalroute.output.BillingGateway.ErrorCause;

class testCDR {

	CallRecordsProcessor processor = new CallRecordProcessorImp(new BillingGateway() {
        @Override
        public void beginBatch() {
            System.out.println("Begin: reads files from the network switch, processes the contained CDRs (Call Data Records) and forwards the aggregated data! " );
            
        }
        @Override
        public void consume(String callId, int seqNum, String aNum, String bNum, byte causeForOutput, int duration) {
            System.out.println("consume: " + callId +", "+  seqNum +", "+ aNum +", "+ bNum +", "+ causeForOutput +", "+ duration);
        }
        @Override
        public void endBatch(long totalDuration) {
            System.out.println("endBatch: totalDuration " + totalDuration);
        }
        @Override
        public void logError(ErrorCause errorCause, String callId, int seqNum, String aNum, String bNum) {
            System.out.println("logError " +errorCause  +", "+ callId +", "+  seqNum +", "+ aNum +", "+ bNum);
        }
    });
	
	//One short and one long call
	@Test
	void testShortLongCDR() {		 
	        
		String strtest= "1B:1,111111,222222,2,5\r\n"
				+ "1K:2,555555,666666,1,15\r\n"
				+ "1K:3,555555,666666,1,15\r\n"
				+ "1K:4,555555,666666,2,2";
		 
		 InputStream inputStream = new ByteArrayInputStream(strtest.getBytes());

	        //perform processing
	        processor.processBatch(inputStream);
	        
	        assertEquals(((CallRecordProcessorImp)processor).getTotalDuration(), 37);
	    
	}

	//Call with no "end of call"
		@Test
		void testNoEndOffCallCDR() {
			 
		        
			String strtest=   "111K:2,555555,666666,1,15\r\n"
							+ "111K:3,555555,666666,1,15\r\n"
							+ "111K:4,555555,666666,1,15";
			 
			 InputStream inputStream = new ByteArrayInputStream(strtest.getBytes());

		        //perform processing
		        processor.processBatch(inputStream);
		        
		        assertEquals(((CallRecordProcessorImp)processor).getTotalDuration(), 45);
		    
		}
		//Call with duplicate sequence number
		@Test
		void testDuplicateSeqNumCDR() {
					 
				        
			String strtest=   "K:20,555555,666666,1,15\r\n"
							+ "K:21,555555,666666,1,15\r\n"
							+ "K:21,555555,666666,2,2"; //duplicate
					 
			InputStream inputStream = new ByteArrayInputStream(strtest.getBytes());

			//perform processing
			processor.processBatch(inputStream);
				        
			assertEquals(((CallRecordProcessorImp)processor).getTotalDuration(), 30);
				    
		}
		//Calls with matching and non-matching partial record
		@Test
		void testMatchingUnMatchingPartialRecords() {							 
						        
			String strtest=   "7D:9,111111,222222,1,31\r\n"
							+ "7D:10,111111,222222,1,4\r\n"
							+ "1E:11,333333,444444,1,55\r\n"
							+ "_:12,111111,222222,0,5\r\n"
							+ "_:13,222233,445566,0,5"; 
							 
			InputStream inputStream = new ByteArrayInputStream(strtest.getBytes());

			//perform processing
			processor.processBatch(inputStream);
						        
			assertEquals(((CallRecordProcessorImp)processor).getTotalDuration(), 95);
						    
		}
}
