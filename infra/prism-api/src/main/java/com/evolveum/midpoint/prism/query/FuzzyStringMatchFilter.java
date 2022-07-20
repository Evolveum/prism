package com.evolveum.midpoint.prism.query;

import java.io.Serializable;
import java.util.Map;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.PrismConstants;
import com.google.common.collect.ImmutableMap;

public interface FuzzyStringMatchFilter<T> extends PropertyValueFilter<T> {

    public static final QName THRESHOLD = new QName(PrismConstants.NS_QUERY, "threshold");
    public static final QName INCLUSIVE = new QName(PrismConstants.NS_QUERY, "inclusive");
    public static final QName LEVENSTEIN = new QName(PrismConstants.NS_QUERY, "levenstein");
    public static final QName SIMILARITY = new QName(PrismConstants.NS_QUERY, "similarity");


    FuzzyMatchingMethod getMatchingMethod();

    public interface FuzzyMatchingMethod extends Serializable {

        public abstract QName getMethodName();

        public abstract Map<QName, Object> getAttributes();
    }

    public abstract class ThresholdMatchingMethod<T extends Number> implements FuzzyMatchingMethod {

        private static final long serialVersionUID = 1L;
        private final T threshold;
        private final boolean inclusive;

        public ThresholdMatchingMethod(T threshold, boolean inclusive) {
            this.threshold = threshold;
            this.inclusive = inclusive;
        }

        T geThreshold() {
            return threshold;
        }

        boolean isInclusive() {
            return inclusive;
        }

        @Override
        public Map<QName, Object> getAttributes() {
            return ImmutableMap.of(THRESHOLD, threshold, INCLUSIVE, inclusive);
        }
    }

    public class Levenstein extends ThresholdMatchingMethod<Integer> {

        private static final long serialVersionUID = 1L;

        public Levenstein(Integer threshold, boolean inclusive) {
            super(threshold, inclusive);
        }

        @Override
        public QName getMethodName() {
            return LEVENSTEIN;
        }
    }

    /**
     *
     * Trigram Similarity
     *
     */
    public class Similarity extends ThresholdMatchingMethod<Float> {

        private static final long serialVersionUID = 1L;

        public Similarity(Float threshold, boolean inclusive) {
            super(threshold, inclusive);
        }

        @Override
        public QName getMethodName() {
            return SIMILARITY;
        }
    }

    static Levenstein levenstein(int threshold, boolean inclusive) {
        return new Levenstein(threshold, inclusive);
    }

    static Similarity similarity(float threshold, boolean inclusive) {
        return new Similarity(threshold, inclusive);
    }
}

