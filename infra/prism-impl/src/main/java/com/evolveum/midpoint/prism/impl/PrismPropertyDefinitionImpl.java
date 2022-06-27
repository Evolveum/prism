/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import javax.xml.namespace.QName;

import com.evolveum.axiom.concepts.Lazy;
import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.delta.PropertyDelta;
import com.evolveum.midpoint.prism.impl.delta.PropertyDeltaImpl;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.util.DefinitionUtil;
import com.evolveum.midpoint.util.DisplayableValue;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Property Definition.
 * <p>
 * Property is a basic unit of information in midPoint. This class provides
 * definition of property type, multiplicity and so on.
 * <p>
 * Property is a specific characteristic of an object. It may be considered
 * object "attribute" or "field". For example User has fullName property that
 * contains string value of user's full name.
 * <p>
 * Properties may be single-valued or multi-valued
 * <p>
 * Properties may contain primitive types or complex types (defined by XSD
 * schema)
 * <p>
 * Property values are unordered, implementation may change the order of values
 * <p>
 * Duplicate values of properties should be silently removed by implementations,
 * but clients must be able tolerate presence of duplicate values.
 * <p>
 * Operations that modify the objects work with the granularity of properties.
 * They add/remove/replace the values of properties, but do not "see" inside the
 * property.
 * <p>
 * This class represents schema definition for property. See {@link Definition}
 * for more details.
 *
 * @author Radovan Semancik
 */
public class PrismPropertyDefinitionImpl<T> extends ItemDefinitionImpl<PrismProperty<T>> implements PrismPropertyDefinition<T>,
        MutablePrismPropertyDefinition<T> {

    // TODO some documentation
    private static final long serialVersionUID = 7259761997904371009L;
    private Collection<? extends DisplayableValue<T>> allowedValues;

    private Collection<? extends DisplayableValue<T>> suggestedValues;
    private Boolean indexed = null;
    private T defaultValue;
    private QName matchingRuleQName = null;

    private transient Lazy<Optional<ComplexTypeDefinition>> structuredType;

    public PrismPropertyDefinitionImpl(QName elementName, QName typeName) {
        this(elementName, typeName, (QName) null);
    }

    public PrismPropertyDefinitionImpl(QName elementName, QName typeName, QName definedInType) {
        super(elementName, typeName, definedInType);
        this.structuredType = Lazy.from(() ->
            Optional.ofNullable(getPrismContext().getSchemaRegistry().findComplexTypeDefinitionByType(getTypeName()))
        );
    }

    public PrismPropertyDefinitionImpl(QName elementName, QName typeName, T defaultValue) {
        this(elementName, typeName, defaultValue, null);
    }

    public PrismPropertyDefinitionImpl(QName elementName, QName typeName, T defaultValue, QName definedInType) {
        this(elementName, typeName, definedInType);
        this.defaultValue = defaultValue;
    }

    @Nullable
    @Override
    public Collection<? extends DisplayableValue<T>> getAllowedValues() {
        return allowedValues;
    }

    @Override
    public void setAllowedValues(Collection<? extends DisplayableValue<T>> allowedValues) {
        this.allowedValues = allowedValues;
    }

    @Nullable
    @Override
    public Collection<? extends DisplayableValue<T>> getSuggestedValues() {
        return suggestedValues;
    }

    @Override
    public void setSuggestedValues(Collection<? extends DisplayableValue<T>> suggestedValues) {
        this.suggestedValues = suggestedValues;
    }

    @Override
    public T defaultValue() {
        return defaultValue;
    }

    @Override
    public Boolean isIndexed() {
        return indexed;
    }

    @Override
    public void setIndexed(Boolean indexed) {
        checkMutable();
        this.indexed = indexed;
    }

    @Override
    public QName getMatchingRuleQName() {
        return matchingRuleQName;
    }

    @Override
    public void setMatchingRuleQName(QName matchingRuleQName) {
        checkMutable();
        this.matchingRuleQName = matchingRuleQName;
    }

    @NotNull
    @Override
    public PrismProperty<T> instantiate() {
        return instantiate(getItemName());
    }

    @NotNull
    @Override
    public PrismProperty<T> instantiate(QName name) {
        name = DefinitionUtil.addNamespaceIfApplicable(name, this.itemName);
        return new PrismPropertyImpl<>(name, this, getPrismContext());
    }

    @Override
    public @NotNull PropertyDelta<T> createEmptyDelta(ItemPath path) {
        return new PropertyDeltaImpl<>(path, this, getPrismContext());
    }

    @Override
    public boolean canBeDefinitionOf(PrismValue pvalue) {
        if (pvalue == null) {
            return false;
        }
        if (!(pvalue instanceof PrismPropertyValue<?>)) {
            return false;
        }
        Itemable parent = pvalue.getParent();
        if (parent != null) {
            if (!(parent instanceof PrismProperty<?>)) {
                return false;
            }
            //noinspection unchecked
            return canBeDefinitionOf((PrismProperty) parent);
        } else {
            // TODO: maybe look actual value java type?
            return true;
        }
    }

    @NotNull
    @Override
    public PrismPropertyDefinitionImpl<T> clone() {
        PrismPropertyDefinitionImpl<T> clone = new PrismPropertyDefinitionImpl<>(getItemName(), getTypeName());
        clone.copyDefinitionDataFrom(this);
        return clone;
    }

    protected void copyDefinitionDataFrom(PrismPropertyDefinition<T> source) {
        super.copyDefinitionDataFrom(source);
        allowedValues = source.getAllowedValues(); // todo new collection?
        suggestedValues = source.getSuggestedValues();
        indexed = source.isIndexed();
        defaultValue = source.defaultValue();
        matchingRuleQName = source.getMatchingRuleQName();
    }

    @Override
    protected void extendToString(StringBuilder sb) {
        super.extendToString(sb);
        if (indexed != null && indexed) {
            sb.append(",I");
        }
        if (allowedValues != null && !allowedValues.isEmpty()) {
            sb.append(",AVals:").append(allowedValues.size());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PrismPropertyDefinitionImpl<?> that = (PrismPropertyDefinitionImpl<?>) o;
        return Objects.equals(allowedValues, that.allowedValues)
                && Objects.equals(indexed, that.indexed)
                && Objects.equals(defaultValue, that.defaultValue)
                && Objects.equals(matchingRuleQName, that.matchingRuleQName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), allowedValues, indexed, defaultValue, matchingRuleQName);
    }

    /**
     * Return a human readable name of this class suitable for logs.
     */
    @Override
    protected String getDebugDumpClassName() {
        return "PPD";
    }

    @Override
    public String getDocClassName() {
        return "property";
    }

    @Override
    public MutablePrismPropertyDefinition<T> toMutable() {
        checkMutableOnExposing();
        return this;
    }

    @Override
    public Optional<ComplexTypeDefinition> structuredType() {
        return structuredType.get();
    }
}
