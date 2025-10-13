/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.delta;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.prism.xml.ns._public.types_3.DeltaSetTripleType;

import java.util.Collection;

/**
 *
 */
public class DeltaSetTripleUtil {
    public static <T> void diff(Collection<T> valuesOld, Collection<T> valuesNew, DeltaSetTriple<T> triple) {
        if (valuesOld == null && valuesNew == null) {
            // No values, no change -> empty triple
            return;
        }
        if (valuesOld == null) {
            triple.getPlusSet().addAll(valuesNew);
            return;
        }
        if (valuesNew == null) {
            triple.getMinusSet().addAll(valuesOld);
            return;
        }
        for (T val : valuesOld) {
            if (valuesNew.contains(val)) {
                triple.getZeroSet().add(val);
            } else {
                triple.getMinusSet().add(val);
            }
        }
        for (T val : valuesNew) {
            if (!valuesOld.contains(val)) {
                triple.getPlusSet().add(val);
            }
        }
    }

    /**
     * Compares two (unordered) collections and creates a triple describing the differences.
     */
    public static <V extends PrismValue> PrismValueDeltaSetTriple<V> diffPrismValueDeltaSetTriple(Collection<V> valuesOld, Collection<V> valuesNew) {
        PrismValueDeltaSetTriple<V> triple = PrismContext.get().deltaFactory().createPrismValueDeltaSetTriple();
        diff(valuesOld, valuesNew, triple);
        return triple;
    }

    public static <V extends PrismValue> PrismValueDeltaSetTriple<V> allToZeroSet(Collection<V> values) {
        PrismValueDeltaSetTriple<V> triple = PrismContext.get().deltaFactory().createPrismValueDeltaSetTriple();
        triple.addAllToZeroSet(values);
        return triple;
    }

    public static boolean isEmpty(DeltaSetTripleType triple) {
        return triple == null || (triple.getZero().isEmpty() && triple.getPlus().isEmpty() && triple.getMinus().isEmpty());
    }
}
