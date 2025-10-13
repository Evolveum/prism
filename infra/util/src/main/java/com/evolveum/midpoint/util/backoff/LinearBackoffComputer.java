/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.util.backoff;

public class LinearBackoffComputer extends RetryLimitedBackoffComputer {

    private long delayInterval;

    public LinearBackoffComputer(int maxRetries, long delayInterval) {
        super(maxRetries);
        this.delayInterval = delayInterval;
    }

    @Override
    public long computeDelayWithinLimits(int retryNumber) {
        return Math.round(Math.random() * delayInterval);
    }
}
