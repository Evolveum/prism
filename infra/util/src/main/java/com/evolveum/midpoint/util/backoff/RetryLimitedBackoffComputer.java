/*
 * Copyright (c) 2010-2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.util.backoff;

public abstract class RetryLimitedBackoffComputer implements BackoffComputer {

    private final int maxRetries;

    RetryLimitedBackoffComputer(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    @Override
    public long computeDelay(int retryNumber) throws NoMoreRetriesException {
        if (retryNumber <= maxRetries) {
            return computeDelayWithinLimits(retryNumber);
        } else {
            throw new NoMoreRetriesException("Limit of " + maxRetries + " exceeded");
        }
    }

    protected abstract long computeDelayWithinLimits(int retryNumber);
}
