/*
 * Copyright (c) 2010-2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.util.backoff;

public class ExponentialBackoffComputer extends RetryLimitedBackoffComputer {

    private final long baseDelayInterval;
    private final int exponentialThreshold;
    private final Long delayIntervalLimit;

    public ExponentialBackoffComputer(int maxRetries,
            long baseDelayInterval, int exponentialThreshold, Long delayIntervalLimit) {
        super(maxRetries);
        this.baseDelayInterval = baseDelayInterval;
        this.exponentialThreshold = exponentialThreshold;
        this.delayIntervalLimit = delayIntervalLimit;
    }

    @Override
    public long computeDelayWithinLimits(int retryNumber) {
        //System.out.println("baseDelayInterval = " + baseDelayInterval + ", limits: " + exponentialThreshold + "/" + delayIntervalLimit + " (retry " + retryNumber + ")");
        double delayInterval = baseDelayInterval * Math.pow(2, Math.min(retryNumber, exponentialThreshold) - 1);
        if (delayIntervalLimit != null && delayInterval > delayIntervalLimit) {
            delayInterval = delayIntervalLimit;
        }
        return Math.round(Math.random() * delayInterval);
    }
}
