package com.evolveum.midpoint.prism.impl.storage;

import com.evolveum.midpoint.prism.Itemable;
import com.evolveum.midpoint.prism.PrismValue;

import org.jetbrains.annotations.NotNull;

abstract public class MultiValueStorage<V extends PrismValue> extends AbstractMutableStorage<V> {

    protected void checkKeyUnique(@NotNull Itemable owner, V value) {

    }


}
