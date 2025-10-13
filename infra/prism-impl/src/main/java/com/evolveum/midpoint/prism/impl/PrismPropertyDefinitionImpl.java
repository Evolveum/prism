/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.impl;

import java.io.Serial;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.PrismPropertyDefinition.PrismPropertyLikeDefinitionBuilder;
import com.evolveum.midpoint.prism.path.ItemName;

import com.evolveum.midpoint.prism.schema.SerializablePropertyDefinition;
import com.evolveum.midpoint.util.DisplayableValue;

import org.jetbrains.annotations.NotNull;

import com.evolveum.axiom.concepts.Lazy;
import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.delta.PropertyDelta;
import com.evolveum.midpoint.prism.impl.delta.PropertyDeltaImpl;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.util.DefinitionUtil;

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
        implements
        PrismPropertyDefinition<T>,
        PrismPropertyDefinition.PrismPropertyDefinitionMutator<T>,
        PrismItemValuesDefinition.Delegable<T>,
        PrismItemValuesDefinition.Mutator.Delegable<T>,
        PrismItemMatchingDefinition.Delegable<T>,
        PrismItemMatchingDefinition.Mutator.Delegable,
        PrismPropertyLikeDefinitionBuilder<T>,
        SerializablePropertyDefinition {

    // TODO some documentation
    @Serial private static final long serialVersionUID = 7259761997904371009L;

    @NotNull private final PrismItemValuesDefinition.Data<T> prismItemValuesDefinition = new PrismItemValuesDefinition.Data<>();
    @NotNull private final PrismItemMatchingDefinition.Data<T> prismItemMatchingDefinition;

    @Override
    public PrismItemValuesDefinition.Data<T> prismItemValuesDefinition() {
        return prismItemValuesDefinition;
    }

    @Override
    public PrismItemMatchingDefinition.Data<T> prismItemMatchingDefinition() {
        return prismItemMatchingDefinition;
    }

    private transient Lazy<Optional<ComplexTypeDefinition>> structuredType;

    public PrismPropertyDefinitionImpl(QName elementName, QName typeName) {
        this(elementName, typeName, (QName) null);
    }

    public PrismPropertyDefinitionImpl(QName elementName, QName typeName, QName definedInType) {
        super(elementName, typeName, definedInType);
        prismItemMatchingDefinition = new PrismItemMatchingDefinition.Data<>(typeName);
        this.structuredType = Lazy.from(() ->
            Optional.ofNullable(PrismContext.get().getSchemaRegistry().findComplexTypeDefinitionByType(getTypeName()))
        );
    }

    public PrismPropertyDefinitionImpl(QName elementName, QName typeName, T defaultValue) {
        this(elementName, typeName, defaultValue, null);
    }

    public PrismPropertyDefinitionImpl(QName elementName, QName typeName, T defaultValue, QName definedInType) {
        this(elementName, typeName, definedInType);
        setDefaultValue(defaultValue);
    }

    @Override
    public QName getMatchingRuleQName() {
        return PrismItemMatchingDefinition.Delegable.super.getMatchingRuleQName();
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
    public @NotNull PrismPropertyDefinitionImpl<T> clone() {
        return cloneWithNewName(itemName);
    }

    @Override
    public @NotNull PrismPropertyDefinitionImpl<T> cloneWithNewName(@NotNull ItemName itemName) {
        PrismPropertyDefinitionImpl<T> clone = new PrismPropertyDefinitionImpl<>(itemName, getTypeName());
        clone.copyDefinitionDataFrom(this);
        return clone;
    }

    protected void copyDefinitionDataFrom(PrismPropertyDefinition<T> source) {
        super.copyDefinitionDataFrom(source);
        prismItemValuesDefinition.copyFrom(source);
        prismItemMatchingDefinition.copyFrom(source);
    }

    @Override
    protected void extendToString(StringBuilder sb) {
        super.extendToString(sb);
        var allowedValues = getAllowedValues();
        if (allowedValues != null && !allowedValues.isEmpty()) {
            sb.append(",AVals:").append(allowedValues.size());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        PrismPropertyDefinitionImpl<?> that = (PrismPropertyDefinitionImpl<?>) o;
        return Objects.equals(prismItemValuesDefinition, that.prismItemValuesDefinition)
                && Objects.equals(prismItemMatchingDefinition, that.prismItemMatchingDefinition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), prismItemValuesDefinition, prismItemMatchingDefinition);
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
    public PrismPropertyDefinitionMutator<T> mutator() {
        checkMutableOnExposing();
        return this;
    }

    @Override
    public Optional<ComplexTypeDefinition> structuredType() {
        return structuredType.get();
    }

    /**
     * Not sure why the default method is not inherited here -
     * from {@link PrismItemValuesDefinition.Mutator.Delegable#setAllowedValues(Collection)}.
     */
    @Override
    public void setAllowedValues(Collection<? extends DisplayableValue<T>> displayableValues) {
        prismItemValuesDefinition.setAllowedValues(displayableValues);
    }
}
