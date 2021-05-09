# Background & Assignment
At the telecom company where you are working there is a network switch producing output records of subscriber phone calls for predefined 
intervals. As a result, long calls may result in partial records. To be able to bill your subscribers, their total call 
duration must be calculated for each call.  

Your assignment is to create a program that reads files from the network switch, processes the contained CDRs (Call Data Records) and 
forwards the aggregated data for each call using the API (interface) provided. In addition to the interface, please find attached an example input file 
called `INFILE_ascii_big`.


##Input format
Each record consists of six fields, separated by either a colon or comma (see below).

### callId
Call identity string terminated by `":"`.
`_` (underscore) represents an incomplete record. 

### seqNum
A sequence number terminated by `","`

### aNum
A-number string (the one making the call) terminated by `","`

### bNum
B-number string (the one receiving the call) terminated by `","`

### causeForOutput
The reason record was created, terminated by `","`.
   * `1` represents ongoing call
   * `2` represents end of call
   * `0` is used for `incomplete records` (can appear during network problems)

### duration
Duration of the call, in minutes, terminated by `\n`

## API's
```java
public interface CallRecordsProcessor {
    void processBatch(InputStream in);
}

public enum ErrorCause {
        NO_MATCH,
        DUPLICATE_SEQ_NO
}

public interface BillingGateway {
    void beginBatch();
    void consume(String callId, int seqNum, String aNum, String bNum, byte causeForOutput, int duration);
    void endBatch(long totalDuration);
    void logError(ErrorCause errorCause, String callId, int seqNum, String aNum, String bNum)
}
```

* `CallRecordsProcessor.processBatch:` 
    Processes call records, aggregates and sends them to billing. The InputStream abstraction is there to facilitate testing.
* `BillingGateway.beginBatch:` 
    To be called at startup
* `BillingGateway.consume:` 
    To be called for every aggregated call record
* `BillingGateway.endBatch:` 
    To be called when processing of input data is done
* `BillingGateway.logError:` 
    To be called when there are incomplete records that cannot be matched or when there is a duplicate




## Aggregation Rules

Records for "ongoing calls", with the same `callId, aNum and bNum`, must be aggregated. When you read the "end of call record", 
your aggregated duration must be sent, using the `BillingGateway.consume` method.
When you reach the end of file, any ongoing aggregation must be sent (flushed) as well.

`Incomplete records` (the ones with missing `callId`) never create new aggregation sessions, instead they must be 
aggregated to the records with matching `aNum` and `bNum`. 
If there is no such match the `incomplete record` is skipped. If there is more than one match (due to calls with the same `aNum` 
and `bNum` but different `callId`), the `incomplete record` joins the first matching call. 

Aggregated call data flushed to BillingGateway must contain the highest `seqNum` within the aggregation session (this rule also
applies for any `incomplete record` within the session). `causeForOutput` should be the highest encountered while aggregating, unless there is an `incomplete record` 
within the session, then it should be `0`.

`seqNum` is a unique identifier within the file. If the same seqNum appears a second time, its record should be disregarded from 
aggregation.

## Examples
### One short and one long call
```
1B:1,111111,222222,2,5
1K:2,555555,666666,1,15
1K:3,555555,666666,1,15
1K:4,555555,666666,2,2
```
Should be aggregated and sent as:
```
1B:1,111111:222222,2,5
1K:4,555555,666666,2,32
```

### Call with no "end of call"  
```
111K:2,555555,666666,1,15
111K:3,555555,666666,1,15
111K:4,555555,666666,1,15
```
Should be aggregated and sent as:
```
111K:4,555555,666666,1,45
```

### Call with duplicate sequence number  
```
K:20,555555,666666,1,15
K:21,555555,666666,1,15
K:21,555555,666666,2,2
```
Should be aggregated and sent as:
```
K:21,555555,666666,1,30
```

### Calls with matching and non-matching partial record
The following lines:
```
7D:9,111111,222222,1,31
7D:10,111111,222222,1,4
1E:11,333333,444444,1,55
_:12,111111,222222,0,5
_:13,222233,445566,0,5
```
Should be aggregated and sent as:
```
7D:12:111111:222222:0,40
1E:11,333333,444444,1,55
```

## Evaluation Criteria
* Is the program robust?
* Is it easy to test?
* How is the memory consumption?
* How is the overall performance?
* Is the code readable and documented?
* Is the program maintainable (what would be the impact of a change, e.g. if we must aggregate data over different files)?
* Is it scalable and easy to change?


## Hints & suggestions
* Have a clear separation between the reader part and the processing part in your application.
* Look at `Application.java` for a suggestion on how to start.
