/*
 * Copyright (c) 2010-2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.prism.annotation.ItemDiagramSpecification;

import javax.xml.namespace.QName;

/**
 * An interface that provides an ability to modify a definition.
 */
public interface MutableDefinition extends Definition {

    void setProcessing(ItemProcessing processing);

    void setDeprecated(boolean deprecated);

    void setRemoved(boolean removed);

    void setRemovedSince(String removedSince);

    void setOptionalCleanup(boolean optionalCleanup);

    void setExperimental(boolean experimental);

    void setEmphasized(boolean emphasized);

    void setDisplayHint(DisplayHint display);

    void setDisplayName(String displayName);

    void setDisplayOrder(Integer displayOrder);

    void setHelp(String help);

    void setRuntimeSchema(boolean value);

    void setTypeName(QName typeName);

    void setDocumentation(String value);

    void addSchemaMigration(SchemaMigration schemaMigration);

    void addDiagram(ItemDiagramSpecification diagram);

    void setMerge(Merge merge);

    /**
     * A variant of {@link MutableDefinition} that does not allow any modifications. Useful for implementations that want
     * to allow only selected mutating operations.
     */
    interface Unsupported extends MutableDefinition {

        @Override
        default void setProcessing(ItemProcessing processing) {
            throw new UnsupportedOperationException();
        }

        @Override
        default void setDeprecated(boolean deprecated) {
            throw new UnsupportedOperationException();
        }

        @Override
        default void setRemoved(boolean removed) {
            throw new UnsupportedOperationException();
        }

        @Override
        default void setRemovedSince(String removedSince) {
            throw new UnsupportedOperationException();
        }

        @Override
        default void setOptionalCleanup(boolean optionalCleanup) {
            throw new UnsupportedOperationException();
        }

        @Override
        default void setExperimental(boolean experimental) {
            throw new UnsupportedOperationException();
        }

        @Override
        default void setEmphasized(boolean emphasized) {
            throw new UnsupportedOperationException();
        }

        @Override
        default void setDisplayHint(DisplayHint display) {
            throw new UnsupportedOperationException();
        }

        @Override
        default void setMerge(Merge merge) {
            throw new UnsupportedOperationException();
        }

        @Override
        default void setDisplayName(String displayName) {
            throw new UnsupportedOperationException();
        }

        @Override
        default void setDisplayOrder(Integer displayOrder) {
            throw new UnsupportedOperationException();
        }

        @Override
        default void setHelp(String help) {
            throw new UnsupportedOperationException();
        }

        @Override
        default void setRuntimeSchema(boolean value) {
            throw new UnsupportedOperationException();
        }

        @Override
        default void setTypeName(QName typeName) {
            throw new UnsupportedOperationException();
        }

        @Override
        default void setDocumentation(String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        default void addSchemaMigration(SchemaMigration schemaMigration) {
            throw new UnsupportedOperationException();
        }

        @Override
        default void addDiagram(ItemDiagramSpecification diagram) {
            throw new UnsupportedOperationException();
        }
    }
}
