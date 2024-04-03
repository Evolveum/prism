/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.util.annotation.Experimental;

/**
 *  EXPERIMENTAL
 */
@Experimental
public interface MutableItemDefinition<I extends Item<?, ?>> extends ItemDefinition<I>, MutableDefinition {

    void setMinOccurs(int value);

    void setMaxOccurs(int value);

    void setCanRead(boolean val);

    void setCanModify(boolean val);

    void setCanAdd(boolean val);

    void setValueEnumerationRef(PrismReferenceValue valueEnumerationRef);

    void setOperational(boolean operational);

    void setAlwaysUseForEquals(boolean alwaysUseForEquals);

    void setDynamic(boolean value);

    // use with care
    void setItemName(QName name);

    void setReadOnly();

    void setDeprecatedSince(String value);

    void setPlannedRemoval(String value);

    void setElaborate(boolean value);

    void setHeterogeneousListItem(boolean value);

    void setSubstitutionHead(QName value);

    void setIndexOnly(boolean value);

    void setInherited(boolean value);

    void setSearchable(boolean value);

    /**
     * A variant of {@link MutableItemDefinition} that does not allow any modifications. Useful for implementations that want
     * to allow only selected mutating operations.
     */
    interface Unsupported<I extends Item<?, ?>> extends MutableItemDefinition<I>, MutableDefinition.Unsupported {

        @Override
        default void setMinOccurs(int value) {
            throw new UnsupportedOperationException();
        }

        @Override
        default void setMaxOccurs(int value) {
            throw new UnsupportedOperationException();
        }

        @Override
        default void setCanRead(boolean val) {
            throw new UnsupportedOperationException();
        }

        @Override
        default void setCanModify(boolean val) {
            throw new UnsupportedOperationException();
        }

        @Override
        default void setCanAdd(boolean val) {
            throw new UnsupportedOperationException();
        }

        @Override
        default void setValueEnumerationRef(PrismReferenceValue valueEnumerationRef) {
            throw new UnsupportedOperationException();
        }

        @Override
        default void setOperational(boolean operational) {
            throw new UnsupportedOperationException();
        }

        @Override
        default void setAlwaysUseForEquals(boolean alwaysUseForEquals) {
            throw new UnsupportedOperationException();
        }

        @Override
        default void setDynamic(boolean value) {
            throw new UnsupportedOperationException();
        }

        @Override
        default void setItemName(QName name) {
            throw new UnsupportedOperationException();
        }

        @Override
        default void setReadOnly() {
            throw new UnsupportedOperationException();
        }

        @Override
        default void setDeprecatedSince(String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        default void setPlannedRemoval(String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        default void setElaborate(boolean value) {
            throw new UnsupportedOperationException();
        }

        @Override
        default void setHeterogeneousListItem(boolean value) {
            throw new UnsupportedOperationException();
        }

        @Override
        default void setSubstitutionHead(QName value) {
            throw new UnsupportedOperationException();
        }

        @Override
        default void setIndexOnly(boolean value) {
            throw new UnsupportedOperationException();
        }

        @Override
        default void setInherited(boolean value) {
            throw new UnsupportedOperationException();
        }

        @Override
        default void setSearchable(boolean value) {
            throw new UnsupportedOperationException();
        }
    }
}
