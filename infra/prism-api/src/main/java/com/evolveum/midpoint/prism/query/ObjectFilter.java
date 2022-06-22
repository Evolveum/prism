/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.query;

import java.io.Serializable;

import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.match.MatchingRuleRegistry;
import com.evolveum.midpoint.util.DebugDumpable;
import com.evolveum.midpoint.util.exception.SchemaException;

public interface ObjectFilter extends DebugDumpable, Serializable, Revivable, Freezable, PrismContextSensitive {

    /**
     * Does a SHALLOW clone.
     */
    ObjectFilter clone();

    boolean match(PrismContainerValue value, MatchingRuleRegistry matchingRuleRegistry) throws SchemaException;

    void accept(Visitor visitor);

    @Override
    void revive(PrismContext prismContext) throws SchemaException;

    void checkConsistence(boolean requireDefinitions);

    boolean equals(Object o, boolean exact);

    /** Utility method performing {@link #equals(Object, boolean)} on two nullable objects. */
    static boolean equals(@Nullable ObjectFilter filter, @Nullable Object other, boolean exact) {
        return filter != null
                ? filter.equals(other, exact)
                : other == null;
    }
}
