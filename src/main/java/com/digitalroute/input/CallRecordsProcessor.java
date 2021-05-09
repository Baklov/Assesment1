// Copyright 2000-2017 Digital Route AB. All rights reserved.
// DIGITAL ROUTE AB PROPRIETARY/CONFIDENTIAL.
// Use is subject to license terms.
//

package com.digitalroute.input;

import java.io.InputStream;

/**
 * This interface should be implemented to create your own processor.
 */
public interface CallRecordsProcessor {

    /**
     * Should be used to process CDR(Call Data Records) batches from an {@link java.io.InputStream}.
     * Look at {@link com.digitalroute.Application#main(String[])} for an example
     * @param in InputStream that should contain Call Data Records
     */
    void processBatch(InputStream in);
}
