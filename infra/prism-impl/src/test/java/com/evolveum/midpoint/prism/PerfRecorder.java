/*
 * Copyright (c) 2016 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author semancik
 */
public class PerfRecorder {

    private String name;
    private int count = 0;
    private Double min = null;
    private Double max = null;
    private double sum = 0d;

    public PerfRecorder(String name) {
        super();
        this.name = name;
    }

    public void record(int index, double value) {
        sum += value;
        count++;
        if (min == null || value < min) {
            min = value;
        }
        if (max == null || value > max) {
            max = value;
        }
    }

    public int getCount() {
        return count;
    }

    public Double getMin() {
        return min;
    }

    public Double getMax() {
        return max;
    }

    public Double getSum() {
        return sum;
    }

    public double getAverage() {
        return sum / count;
    }

    public void assertAverageBelow(double expected) {
        assertThat(getAverage())
                .as("average for %s", name)
                .isLessThan(expected);
        // remove in 2022 if everybody's happy: original without AssertJ
//        AssertJUnit.assertTrue(name + ": Expected average below " + expected + " but was " + getAverage(), getAverage() < expected);
    }

    public void assertMaxBelow(double expected) {
        assertThat(max)
                .as("maximum for %s", name)
                .isLessThan(expected);
    }

    public String dump() {
        return name + ": min / avg / max = " + min + " / " + getAverage() + " / " + max
                + " (sum=" + sum + ", count=" + count + ")";
    }
}
