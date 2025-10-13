/*
 * Copyright (c) 2010-2017 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.util.histogram;

public class HistogramEntry<T> {

    private int itemsCount;
    private long representativeItemValue;
    private T representativeItem;

    public int getItemsCount() {
        return itemsCount;
    }

    public T getRepresentativeItem() {
        return representativeItem;
    }

    public long getRepresentativeItemValue() {
        return representativeItemValue;
    }

    public void record(T item, long value) {
        if (representativeItem == null || representativeItemValue < value) {
            representativeItem = item;
            representativeItemValue = value;
        }
        itemsCount++;
    }
}
