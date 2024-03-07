/*
 * Copyright (c) 2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.deleg;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.*;

import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;

public interface DefinitionDelegator extends Definition {

    Definition delegate();

    @Override
    default void accept(Visitor<Definition> visitor) {
        delegate().accept(visitor);
    }

    @Override
    default String debugDump() {
        return delegate().debugDump();
    }

    @Override
    default boolean accept(Visitor<Definition> visitor, SmartVisitation<Definition> visitation) {
        return delegate().accept(visitor, visitation);
    }

    @Override
    default @NotNull QName getTypeName() {
        return delegate().getTypeName();
    }

    @Override
    default String debugDump(int indent) {
        return delegate().debugDump(indent);
    }

    @Override
    default Object debugDumpLazily() {
        return delegate().debugDumpLazily();
    }

    @Override
    default Object debugDumpLazily(int indent) {
        return delegate().debugDumpLazily(indent);
    }

    @Override
    default boolean isRuntimeSchema() {
        return delegate().isRuntimeSchema();
    }

    @Override
    default boolean isIgnored() {
        return delegate().isIgnored();
    }

    @Override
    default ItemProcessing getProcessing() {
        return delegate().getProcessing();
    }

    @Override
    default boolean isAbstract() {
        return delegate().isAbstract();
    }

    @Override
    default boolean isDeprecated() {
        return delegate().isDeprecated();
    }

    @Override
    default boolean isRemoved() {
        return delegate().isRemoved();
    }
    @Override
    default String getRemovedSince() {
        return delegate().getRemovedSince();
    }

    @Override
    default boolean isOptionalCleanup() {
        return delegate().isOptionalCleanup();
    }

    @Override
    default boolean isExperimental() {
        return delegate().isExperimental();
    }

    @Override
    default String getPlannedRemoval() {
        return delegate().getPlannedRemoval();
    }

    @Override
    default boolean isElaborate() {
        return delegate().isElaborate();
    }

    @Override
    default String getDeprecatedSince() {
        return delegate().getDeprecatedSince();
    }

    @Override
    default DisplayHint getDisplayHint() {
        return delegate().getDisplayHint();
    }
    @Override
    default boolean isEmphasized() {
        return delegate().isEmphasized();
    }

    @Override
    default String getDisplayName() {
        return delegate().getDisplayName();
    }

    @Override
    default Integer getDisplayOrder() {
        return delegate().getDisplayOrder();
    }

    @Override
    default String getHelp() {
        return delegate().getHelp();
    }

    @Override
    default String getDocumentation() {
        return delegate().getDocumentation();
    }

    @Override
    default String getDocumentationPreview() {
        return delegate().getDocumentationPreview();
    }

    @Override
    default Class<?> getTypeClass() {
        return delegate().getTypeClass();
    }

    @Override
    default <A> A getAnnotation(QName qname) {
        return delegate().getAnnotation(qname);
    }

    @Override
    default @Nullable Map<QName, Object> getAnnotations() {
        return delegate().getAnnotations();
    }

    @Override
    default <A> void setAnnotation(QName qname, A value) {
        delegate().setAnnotation(qname, value);
    }

    @Override
    default List<SchemaMigration> getSchemaMigrations() {
        return delegate().getSchemaMigrations();
    }

    @Override
    default String debugDump(int indent, IdentityHashMap<Definition, Object> seen) {
        return delegate().debugDump(indent, seen);
    }

    @Override
    default String getMutabilityFlag() {
        return delegate().getMutabilityFlag();
    }
}
