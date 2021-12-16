/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl;

import com.evolveum.midpoint.prism.ComplexTypeDefinition;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismStaticConfiguration;
import com.evolveum.midpoint.prism.TypeDefinition;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.annotation.Experimental;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.util.QNameUtil;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public abstract class TypeDefinitionImpl extends DefinitionImpl implements TypeDefinition {

    private QName superType;
    protected Class<?> compileTimeClass;
    @NotNull final Set<TypeDefinition> staticSubTypes = new HashSet<>();
    protected Integer instantiationOrder;
    protected final transient SerializationProxy serializationProxy;

    TypeDefinitionImpl(QName typeName) {
        this(typeName, false);
    }

    private TypeDefinitionImpl(QName typeName, boolean schemaRegistryProvided) {
        super(typeName);
        this.serializationProxy = schemaRegistryProvided ? SerializationProxy.forTypeDef(typeName) : null;
    }

    protected static boolean useSerializationProxy(boolean localeEnabled) {
        return PrismStaticConfiguration.javaSerializationProxiesEnabled() && localeEnabled;
    }

    @Override
    public QName getSuperType() {
        return superType;
    }

    public void setSuperType(QName superType) {
        checkMutable();
        this.superType = superType;
    }

    @Override
    @NotNull
    public Collection<TypeDefinition> getStaticSubTypes() {
        return staticSubTypes;
    }

    public void addStaticSubType(TypeDefinition subtype) {
        staticSubTypes.add(subtype);
    }

    @Override
    public Integer getInstantiationOrder() {
        return instantiationOrder;
    }

    public void setInstantiationOrder(Integer instantiationOrder) {
        checkMutable();
        this.instantiationOrder = instantiationOrder;
    }

    @Override
    public Class<?> getCompileTimeClass() {
        return compileTimeClass;
    }

    public void setCompileTimeClass(Class<?> compileTimeClass) {
        checkMutable();
        this.compileTimeClass = compileTimeClass;
    }

    protected void copyDefinitionDataFrom(TypeDefinition source) {
        super.copyDefinitionDataFrom(source);
        this.superType = source.getSuperType();
        this.compileTimeClass = source.getCompileTimeClass();
    }

    @Override
    public boolean canRepresent(QName typeName) {
        if (QNameUtil.match(typeName, getTypeName())) {
            return true;
        }
        if (superType != null) {
            ComplexTypeDefinition supertypeDef = getPrismContext().getSchemaRegistry().findComplexTypeDefinitionByType(superType);
            return supertypeDef.canRepresent(typeName);
        } else {
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)  return true;
        if (!(o instanceof TypeDefinitionImpl)) return false;
        if (!super.equals(o)) return false;
        TypeDefinitionImpl that = (TypeDefinitionImpl) o;
        return Objects.equals(superType, that.superType) &&
                Objects.equals(compileTimeClass, that.compileTimeClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), superType, compileTimeClass);
    }

    /**
     * Crawls up the type hierarchy and looks for type name equivalence.
     */
    @Override
    @Experimental
    public boolean isAssignableFrom(TypeDefinition other, SchemaRegistry schemaRegistry) {
        if (QNameUtil.match(this.getTypeName(), DOMUtil.XSD_ANYTYPE)) {
            return true;
        }
        while (other != null) {
            if (QNameUtil.match(this.getTypeName(), other.getTypeName())) {
                return true;
            }
            if (other.getSuperType() == null) {
                return false;
            }
            other = schemaRegistry.findTypeDefinitionByType(other.getSuperType());
        }
        return false;
    }

    protected Object writeReplace() {
        return useSerializationProxy(serializationProxy != null) ? serializationProxy : this;
    }
}
