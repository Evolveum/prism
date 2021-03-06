/*
 * Copyright (c) 2010-2017 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.util.histogram;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class Histogram<T> {

    private static final int ZEROS_BEFORE_SKIP = 5;
    private static final int ZEROS_AFTER_SKIP = 5;

    private final int step;
    private final int maxLength;

    public Histogram(int step, int maxLength) {
        this.step = step;
        this.maxLength = maxLength;
    }

    private final ArrayList<HistogramEntry<T>> entries = new ArrayList<>();

    private long minValue, maxValue, totalValue = 0;
    private T minItem, maxItem;
    private int items = 0;

    public void register(T item, long value) {
        if (items == 0) {
            minValue = maxValue = value;
            minItem = maxItem = item;
        } else {
            if (value < minValue) {
                minValue = value;
                minItem = item;
            }
            if (value > maxValue) {
                maxValue = value;
                maxItem = item;
            }
        }
        totalValue += value;
        items++;

        long bucketLong = value / step;
        int bucket = bucketLong < maxLength-1 ? (int) bucketLong : maxLength-1;
        if (entries.size() <= bucket) {
            entries.ensureCapacity(bucket);
            while (entries.size() <= bucket) {
                entries.add(new HistogramEntry<>());
            }
        }
        entries.get(bucket).record(item, value);
    }

    public int getStep() {
        return step;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public ArrayList<HistogramEntry<T>> getEntries() {
        return entries;
    }

    @SuppressWarnings("unused")
    public long getMinValue() {
        return minValue;
    }

    @SuppressWarnings("unused")
    public long getMaxValue() {
        return maxValue;
    }

    @SuppressWarnings("unused")
    public long getTotalValue() {
        return totalValue;
    }

    public int getItems() {
        return items;
    }

    public String dump(int columns) {
        StringBuilder sb = new StringBuilder();
        sb.append("Count: ").append(items).append("\n");
        if (items == 0) {
            return sb.toString();
        }
        sb.append("Min: ").append(minValue).append(" (").append(minItem).append(")\n");
        sb.append("Max: ").append(maxValue).append(" (").append(maxItem).append(")\n");
        sb.append("Avg: ").append(totalValue / items).append("\n");
        sb.append("\nHistogram:\n\n");
        int maxIntervalLength = Math.max(getIntervalString(entries.size()).length(), 10);
        String rowFormatString = "%" + maxIntervalLength + "s : %6s : %s : %s\n";
        //noinspection ConstantConditions
        int maxCount = entries.stream().mapToInt(e -> e.getItemsCount()).max().getAsInt();
        sb.append(String.format(rowFormatString, "Interval", "Items", column(0, maxCount, columns), "Representative"));
        int dividerLength = maxIntervalLength + columns + 40;
        sb.append(StringUtils.repeat("-", dividerLength)).append("\n");
        for (int i = 0; i < entries.size(); i++) {
            int skipUntil = computeSkipUntil(i);
            if (skipUntil >= 0) {
                sb.append(StringUtils.repeat(" ~ ", dividerLength/6 - 3));
                sb.append(skipUntil-i).append(" lines skipped");
                sb.append(StringUtils.repeat(" ~ ", dividerLength/6 - 3)).append("\n");
                i = skipUntil-1;
            } else {
                HistogramEntry<T> entry = entries.get(i);
                sb.append(String.format(rowFormatString, getIntervalString(i),
                        String.valueOf(entry.getItemsCount()), column(entry.getItemsCount(), maxCount, columns),
                        getRepresentative(entry)));
            }
        }
        sb.append("\n");
        return sb.toString();
    }

    private int computeSkipUntil(int i) {
        if (i < ZEROS_BEFORE_SKIP) {
            return -1;
        }
        int firstNonZero = zerosTo(i - ZEROS_BEFORE_SKIP);
        if (firstNonZero - ZEROS_AFTER_SKIP <= i+1) {        // so that at least 2 lines are skipped
            return -1;
        }
        return firstNonZero - ZEROS_AFTER_SKIP;
    }

    private int zerosTo(int i) {
        while (i < entries.size() && entries.get(i).getItemsCount() == 0) {
            i++;
        }
        return i;
    }

    private Object column(int count, int maxCount, int columns) {
        int bars = (int) ((double) columns * (double) count / (double) maxCount);
        if (count > 0 && bars == 0) {
            bars = 1;
        }
        return StringUtils.repeat("#", bars) + StringUtils.repeat(" ", columns-bars);
    }

    private String getRepresentative(HistogramEntry<T> entry) {
        if (entry.getRepresentativeItem() == null) {
            return "";
        }
        return entry.getRepresentativeItem() + " (" + entry.getRepresentativeItemValue() + ")";
    }

    private String getIntervalString(int i) {
        if (i == entries.size()-1) {
            return String.format("[%d-%d]", getLower(i), maxValue);
        } else {
            return String.format("[%d-%d]", getLower(i), getUpper(i));
        }
    }

    private long getLower(int bucket) {
        return bucket * step;
    }

    private long getUpper(int bucket) {
        return getLower(bucket+1)-1;
    }
}
