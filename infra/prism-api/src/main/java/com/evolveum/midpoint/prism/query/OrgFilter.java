/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.query;

import com.evolveum.midpoint.prism.PrismReferenceValue;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.util.annotation.Experimental;

/**
 *
 */
public interface OrgFilter extends ObjectFilter {

    enum Scope {
        ONE_LEVEL,
        SUBTREE,
        @Experimental ANCESTORS       // EXPERIMENTAL; OID has to belong to an OrgType!
    }

    PrismReferenceValue getOrgRef();

    Scope getScope();

    boolean isRoot();

    @Override
    OrgFilter clone();

    @Override
    default boolean matchesOnly(ItemPath... paths) {
        return false;
    }
}
