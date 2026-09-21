/*
 * Copyright (c) 2022 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.impl.binding;

import java.io.Serial;
import java.io.Serializable;

import com.evolveum.midpoint.prism.*;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.evolveum.midpoint.prism.binding.PlainStructured;
import com.evolveum.midpoint.prism.binding.StructuredEqualsStrategy;
import com.evolveum.midpoint.prism.binding.StructuredHashCodeStrategy;

import org.jetbrains.annotations.Nullable;

public abstract class AbstractPlainStructured implements PlainStructured, Serializable, JaxbVisitable, TrustDescriptorAware {

    @Serial private static final long serialVersionUID = 1L;

    /**
     * A {@link TrustDescriptor} attached to this data. It is here to make sure it's propagated along with the data, e.g.
     * when cloning, moving between parents, merging, etc.
     *
     * The urgent need (midPoint 4.11) is to provide this for {@code ExpressionType}, {@code ScriptExpressionEvaluatorType},
     * and a couple of others. Majority of them are {@code AbstractPlainStructured}, hence we provide the descriptor here.
     *
     * Alternatives were to put it into {@link PrismPropertyValue} but in midPoint, these beans are usually passed in their pure
     * (bean) form, without the prism envelope. And unlike {@link Containerable} where the two forms are interconnected,
     * in prism property values there's no way how to navigate from the value to the property value.
     *
     * Moreover, we don't want to add 4 bytes to really _all_ property values.
     *
     * The only problem in placing the descriptor here is that it won't work with other forms of {@link PrismPropertyValue}:
     * raw values and expression-based values. But both should be acceptable for scripts and expressions in midPoint 4.11.
     *
     * Behavior:
     *
     * - equals/hashCode: the descriptor is not considered in equality/hashCode
     * - clone: the descriptor is cloned along with the data (in shallow way, as it should be immutable)
     * - Java serialization: it is serialized along with the data
     * - XML/JSON/YAML serialization: it is NOT serialized
     *
     * @see TrustDescriptor
     */
    @Nullable private TrustDescriptor trustDescriptor;

    public AbstractPlainStructured() {
        // NOOP
    }

    public AbstractPlainStructured(AbstractPlainStructured other) {
        if (other == null) {
            throw new NullPointerException("other is null");
        }
        this.trustDescriptor = other.trustDescriptor;
    }

    @Override
    public @Nullable TrustDescriptor getTrustDescriptor() {
        return trustDescriptor;
    }

    @Override
    public void setTrustDescriptor(@Nullable TrustDescriptor trustDescriptor) {
        this.trustDescriptor = trustDescriptor;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public boolean equals(Object obj) {
        return equals(obj, StructuredEqualsStrategy.DEFAULT);
    }

    @Override
    public boolean equals(Object other, StructuredEqualsStrategy strategy) {
        if (other == null) {
            return false;
        }
        return (other instanceof AbstractPlainStructured);
    }

    @Override
    public int hashCode() {
        return hashCode(StructuredHashCodeStrategy.DEFAULT);
    }

    @Override
    public int hashCode(StructuredHashCodeStrategy strategy) {
        return 1;
    }

    @Override
    public void accept(JaxbVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public abstract PlainStructured clone();
}
