package com.evolveum.midpoint.prism.impl.storage;

import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.prism.equivalence.EquivalenceStrategy;
import static com.evolveum.midpoint.prism.equivalence.ParameterizedEquivalenceStrategy.DEFAULT_FOR_EQUALS;

abstract class AbstractMutableStorage<V extends PrismValue> implements ItemStorage<V> {

    static boolean isDefaultEquals(EquivalenceStrategy equivalenceStrategy) {
        return DEFAULT_FOR_EQUALS.equals(equivalenceStrategy);
    }

    static boolean  exactEquals(PrismValue currentValue, PrismValue newValue) {
        return DEFAULT_FOR_EQUALS.equals(currentValue, newValue);
    }

    protected void valueRemoved(V value) {
        value.setParent(null);
    }
}
