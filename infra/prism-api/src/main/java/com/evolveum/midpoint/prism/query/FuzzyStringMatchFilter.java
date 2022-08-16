/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.query;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.query.fuzzy.LevenshteinComputer;

import com.evolveum.midpoint.prism.query.fuzzy.TriGramSimilarityComputer;

import com.google.common.collect.ImmutableMap;

import com.evolveum.midpoint.prism.PrismConstants;

import org.jetbrains.annotations.NotNull;

public interface FuzzyStringMatchFilter<T> extends PropertyValueFilter<T> {

    QName THRESHOLD = new QName(PrismConstants.NS_QUERY, "threshold");
    QName INCLUSIVE = new QName(PrismConstants.NS_QUERY, "inclusive");
    QName LEVENSHTEIN = new QName(PrismConstants.NS_QUERY, "levenshtein");
    QName SIMILARITY = new QName(PrismConstants.NS_QUERY, "similarity");

    FuzzyMatchingMethod getMatchingMethod();

    interface FuzzyMatchingMethod extends Serializable {

        QName getMethodName();

        Map<QName, Object> getAttributes();

        // TODO are string types ok here?
        boolean matches(String lValue, String rValue);
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

        public @NotNull T getThresholdRequired() {
            return Objects.requireNonNull(getThreshold(), () -> "Threshold must be specified in " + this);
        }

        public boolean isInclusive() {
            return inclusive;
        }

        /**
         * Computes the appropriate fuzzy match metric - the one that is being compared with the threshold.
         * For example, Levenshtein edit distance (an integer value) or trigram similarity value (a float).
         */
        public abstract T computeMatchMetricValue(String lValue, String rValue);

        /** Returns the Java type of the metric being used by this method (Integer, Float, ...). */
        public abstract Class<T> getMetricValueClass();

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

        @Override
        public Integer computeMatchMetricValue(String lValue, String rValue) {
            return LevenshteinComputer.computeLevenshteinDistance(lValue, rValue);
        }

        @Override
        public Class<Integer> getMetricValueClass() {
            return Integer.class;
        }

        @Override
        public boolean matches(String lValue, String rValue) {
            if (isInclusive()) {
                return computeMatchMetricValue(lValue, rValue) <= getThresholdRequired();
            } else {
                return computeMatchMetricValue(lValue, rValue) < getThresholdRequired();
            }
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

        @Override
        public Float computeMatchMetricValue(String lValue, String rValue) {
            return (float) TriGramSimilarityComputer.getSimilarity(lValue, rValue);
        }

        @Override
        public Class<Float> getMetricValueClass() {
            return Float.class;
        }

        @Override
        public boolean matches(String lValue, String rValue) {
            if (isInclusive()) {
                return computeMatchMetricValue(lValue, rValue) >= getThresholdRequired();
            } else {
                return computeMatchMetricValue(lValue, rValue) > getThresholdRequired();
            }
        }
    }

    static Levenshtein levenshtein(int threshold, boolean inclusive) {
        return new Levenshtein(threshold, inclusive);
    }

    static Similarity similarity(float threshold, boolean inclusive) {
        return new Similarity(threshold, inclusive);
    }
}

