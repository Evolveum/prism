/*
 * Copyright (c) 2010-2017 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism;

import javax.xml.namespace.QName;

import com.evolveum.prism.xml.ns._public.query_3.SearchFilterType;
import com.evolveum.prism.xml.ns._public.types_3.EvaluationTimeType;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;
import com.evolveum.prism.xml.ns._public.types_3.ReferentialIntegrityType;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Common contract for types representing reference real values (not {@link PrismReferenceValue}).
 * Major example is `ObjectReferenceType` but that one is now in a downstream midPoint project.
 * Whenever we need to create representation of a reference in Prism we have to use other implementations.
 * But all these implementations have this interface in common.
 * Whenever possible, code against this interface.
 */
@Safe
public interface Referencable {

    PrismReferenceValue asReferenceValue();

    Referencable setupReferenceValue(PrismReferenceValue value);

    @Safe
    String getOid();

    static String getOid(Referencable referencable) {
        return referencable != null ? referencable.getOid() : null;
    }

    static @NotNull Set<String> getOids(@NotNull Collection<? extends Referencable> referencables) {
        return referencables.stream()
                .map(ref -> getOid(ref))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Safe
    QName getType();

    @Safe
    PolyStringType getTargetName();

    @Safe
    QName getRelation();

    @Safe
    String getDescription();

    @Safe
    EvaluationTimeType getResolutionTime();

    @Safe
    ReferentialIntegrityType getReferentialIntegrity();

    @Safe
    SearchFilterType getFilter();

    @Safe
    <O extends Objectable> PrismObject<O> getObject();

    @Safe
    Objectable getObjectable();

    Referencable clone();
}
