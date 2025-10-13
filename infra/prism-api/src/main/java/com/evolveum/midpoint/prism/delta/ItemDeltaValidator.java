/*
 * Copyright (c) 2017 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.delta;

import com.evolveum.midpoint.prism.PrismValue;

/**
 * @author semancik
 *
 */
@FunctionalInterface
public interface ItemDeltaValidator<V extends PrismValue> {

    void validate(PlusMinusZero plusMinusZero, V itemValue);

}
