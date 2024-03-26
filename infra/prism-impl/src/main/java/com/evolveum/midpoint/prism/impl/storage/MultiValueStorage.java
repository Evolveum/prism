package com.evolveum.midpoint.prism.impl.storage;

import com.evolveum.midpoint.prism.Itemable;
import com.evolveum.midpoint.prism.PrismValue;

import com.evolveum.midpoint.prism.equivalence.EquivalenceStrategy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

abstract public class MultiValueStorage<V extends PrismValue> extends AbstractMutableStorage<V> {

    protected void checkKeyUnique(@NotNull Itemable owner, V value) {

    }

    protected void iterateAndRemoveEquivalentValues(@NotNull Itemable owner, @NotNull V newValue, @Nullable EquivalenceStrategy strategy) throws ExactValueExistsException {
        boolean exactEquivalentFound = false;
        boolean somethingRemoved = false;
        var iterator = mutableIterator();
        while (iterator.hasNext()) {
            V currentValue = iterator.next();
            if (strategy.equals(currentValue, newValue)) {
                if (!exactEquivalentFound &&
                        (isDefaultEquals(strategy)) || exactEquals(currentValue, newValue)) {
                    exactEquivalentFound = true;
                } else {
                    iterator.remove();
                    somethingRemoved = true;
                    valueRemoved(currentValue);
                }
            }
        }
        if (exactEquivalentFound && !somethingRemoved) {
            throw ExactValueExistsException.INSTANCE;
        }
    }

    abstract Iterator<V> mutableIterator();

    @Override
    public ItemStorage<V> remove(V value, EquivalenceStrategy strategy) throws ValueDoesNotExistsException {
        boolean changed = false;
        Iterator<V> iterator = mutableIterator();
        while (iterator.hasNext()) {
            V val = iterator.next();
            if (val.representsSameValue(value, false) || val.equals(value, strategy)) {
                iterator.remove();
                valueRemoved(val);
                changed = true;
            }
        }
        if (!changed) {
            throw ValueDoesNotExistsException.INSTANCE;
        }
        return this;
    }

}
