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
import com.evolveum.midpoint.prism.impl.match.MatchingRuleRegistryImpl;
import com.evolveum.midpoint.prism.match.MatchingRule;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.prism.util.DefinitionUtil;
import com.evolveum.midpoint.prism.xml.XsdTypeMapper;
import com.evolveum.midpoint.util.DisplayableValue;

import com.evolveum.midpoint.util.exception.SchemaException;

import com.evolveum.midpoint.util.exception.SystemException;

import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;

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
public class PrismPropertyDefinitionImpl<T>
        extends ItemDefinitionImpl<PrismProperty<T>>
        implements PrismPropertyDefinition<T>, MutablePrismPropertyDefinition<T> {

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
            Optional.ofNullable(PrismContext.get().getSchemaRegistry().findComplexTypeDefinitionByType(getTypeName()))
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
    public @NotNull MatchingRule<T> getMatchingRule() {
        return MatchingRuleRegistryImpl.instance()
                .getMatchingRuleSafe(getMatchingRuleQName(), getTypeName());
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
        return new PrismPropertyImpl<>(name, this);
    }

    @Override
    public @NotNull PropertyDelta<T> createEmptyDelta(ItemPath path) {
        return new PropertyDeltaImpl<>(path, this);
    }

    @Override
    public boolean canBeDefinitionOf(@NotNull PrismValue pvalue) {
        if (!(pvalue instanceof PrismPropertyValue<?>)) {
            return false;
        }
        Itemable parent = pvalue.getParent();
        if (parent != null) {
            if (!(parent instanceof PrismProperty<?> property)) {
                return false;
            }
            //noinspection unchecked
            return canBeDefinitionOf((PrismProperty<T>) property);
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

    @Override
    public Class<T> getTypeClass() {
        return PrismContext.get().getSchemaRegistry().determineJavaClassForType(getTypeName());
    }
}
