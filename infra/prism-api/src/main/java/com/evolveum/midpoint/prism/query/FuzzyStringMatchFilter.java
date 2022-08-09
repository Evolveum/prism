/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.query;

import java.io.Serializable;
import java.util.Map;
import javax.xml.namespace.QName;

import com.google.common.collect.ImmutableMap;

import com.evolveum.midpoint.prism.PrismConstants;

public interface FuzzyStringMatchFilter<T> extends PropertyValueFilter<T> {

    QName THRESHOLD = new QName(PrismConstants.NS_QUERY, "threshold");
    QName INCLUSIVE = new QName(PrismConstants.NS_QUERY, "inclusive");
    QName LEVENSHTEIN = new QName(PrismConstants.NS_QUERY, "levenshtein");
    QName SIMILARITY = new QName(PrismConstants.NS_QUERY, "similarity");

    FuzzyMatchingMethod getMatchingMethod();

    interface FuzzyMatchingMethod extends Serializable {

        QName getMethodName();

        Map<QName, Object> getAttributes();
    }

    abstract class ThresholdMatchingMethod<T extends Number> implements FuzzyMatchingMethod {

        private static final long serialVersionUID = 1L;
        private final T threshold;
        private final boolean inclusive;

        ThresholdMatchingMethod(T threshold, boolean inclusive) {
            this.threshold = threshold;
            this.inclusive = inclusive;
        }

        public T getThreshold() {
            return threshold;
        }

        public boolean isInclusive() {
            return inclusive;
        }

        @Override
        public Map<QName, Object> getAttributes() {
            return ImmutableMap.of(THRESHOLD, threshold, INCLUSIVE, inclusive);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "{" +
                    "threshold=" + threshold +
                    ", inclusive=" + inclusive +
                    '}';
        }
    }

    class Levenshtein extends ThresholdMatchingMethod<Integer> {

        private static final long serialVersionUID = 1L;

        public Levenshtein(Integer threshold, boolean inclusive) {
            super(threshold, inclusive);
        }

        @Override
        public QName getMethodName() {
            return LEVENSHTEIN;
        }
    }

    /**
     * Trigram similarity
     */
    class Similarity extends ThresholdMatchingMethod<Float> {

        private static final long serialVersionUID = 1L;

        public Similarity(Float threshold, boolean inclusive) {
            super(threshold, inclusive);
        }

        @Override
        public QName getMethodName() {
            return SIMILARITY;
        }
    }

    static Levenshtein levenshtein(int threshold, boolean inclusive) {
        return new Levenshtein(threshold, inclusive);
    }

    static Similarity similarity(float threshold, boolean inclusive) {
        return new Similarity(threshold, inclusive);
    }
}

