/*
 * Copyright (c) 2016-2017 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.util;

import com.evolveum.midpoint.prism.Objectable;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.util.annotation.Experimental;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface to plug in a monitoring code to prism. Implementation of this
 * interface are called when selected important (usually expensive) operations
 * take place in prism. This can be used for gathering stats, making assertions
 * in the test code, etc.
 *
 * @author semancik
 */
public interface PrismMonitor {

    <O extends Objectable> void recordPrismObjectCompareCount(PrismObject<O> thisObject, Object thatObject);

    <O extends Objectable> void beforeObjectClone(@NotNull PrismObject<O> orig);

    <O extends Objectable> void afterObjectClone(@NotNull PrismObject<O> orig, @Nullable PrismObject<O> clone);

    /** Beware! This may not cover all object serializations. Hopefully at least the majority. */
    @Experimental
    void beforeObjectSerialization(@NotNull PrismObject<?> item);

    @Experimental
    void afterObjectSerialization(@NotNull PrismObject<?> item);

    /** Beware! This may not cover all object parsing operations. Hopefully at least the majority. */
    @Experimental
    void beforeObjectParsing();

    @Experimental
    void afterObjectParsing(@Nullable PrismObject<?> object);
}
