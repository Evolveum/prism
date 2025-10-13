/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.query;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.path.ItemPath;

import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.PrismReferenceDefinition;
import com.evolveum.midpoint.prism.PrismReferenceValue;

public interface RefFilter extends ValueFilter<PrismReferenceValue, PrismReferenceDefinition> {

    @Override
    RefFilter clone();

    void setOidNullAsAny(boolean oidNullAsAny);

    void setTargetTypeNullAsAny(boolean targetTypeNullAsAny);

    boolean isOidNullAsAny();

    boolean isTargetTypeNullAsAny();

    @Override
    default @Nullable QName getMatchingRule() {
        return getDeclaredMatchingRule();
    }

    @Override
    default @Nullable QName getDeclaredMatchingRule() {
        return null;
    }

    /**
     * Returns filter, which reference target must match.
     *
     * @return null or target filter if specified
     */
    @Nullable
    default ObjectFilter getFilter() {
        throw new UnsupportedOperationException("Not implemented");
    }

}
