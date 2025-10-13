/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.util.backoff;

public interface BackoffComputer {

    class NoMoreRetriesException extends Exception {
        NoMoreRetriesException(String message) {
            super(message);
        }
    }

    /**
     * @param retryNumber starts at 1
     */
    long computeDelay(int retryNumber) throws NoMoreRetriesException;

}
