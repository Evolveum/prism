/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl;

import java.io.Serial;
import java.util.Optional;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.PrismReferenceDefinition.PrismReferenceDefinitionBuilder;
import com.evolveum.midpoint.prism.path.ItemName;

import com.evolveum.midpoint.prism.schema.SerializableReferenceDefinition;

import org.jetbrains.annotations.NotNull;

import com.evolveum.axiom.concepts.Lazy;
import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.delta.ReferenceDelta;
import com.evolveum.midpoint.prism.impl.delta.ReferenceDeltaImpl;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.path.ObjectReferencePathSegment;
import com.evolveum.midpoint.prism.util.DefinitionUtil;
import com.evolveum.prism.xml.ns._public.types_3.ObjectReferenceType;

import org.jetbrains.annotations.Nullable;

/**
 * Object Reference Schema Definition.
 *
 * Object Reference is a property that describes reference to an object. It is
 * used to represent association between objects. For example reference from
 * User object to Account objects that belong to the user. The reference is a
 * simple uni-directional link using an OID as an identifier.
 *
 * This type should be used for all object references so the implementations can
 * detect them and automatically resolve them.
 *
 * This class represents schema definition for object reference. See
 * {@link Definition} for more details.
 *
 * @author Radovan Semancik
 */
public class PrismReferenceDefinitionImpl
        extends ItemDefinitionImpl<PrismReference>
        implements PrismReferenceDefinition, PrismReferenceDefinitionBuilder, SerializableReferenceDefinition {

    @Serial private static final long serialVersionUID = 2427488779612517600L;

    private QName targetTypeName;

    /** @see #getTargetObjectDefinition() */
    private PrismObjectDefinition<?> targetObjectDefinition;

    private boolean isComposite = false;

    // TODO What will we do after deserialization?
    private transient Lazy<Optional<ComplexTypeDefinition>> structuredType;

    public PrismReferenceDefinitionImpl(QName elementName, QName typeName) {
        this(elementName, typeName, null);
    }

    public PrismReferenceDefinitionImpl(QName elementName, QName typeName, QName definedInType) {
        super(elementName, typeName, definedInType);
        structuredType = Lazy.from(() ->
                Optional.ofNullable(PrismContext.get().getSchemaRegistry().findComplexTypeDefinitionByType(getTypeName()))
        );
    }

    /**
     * Returns valid XSD object types whose may be the targets of the reference.
     *
     * Corresponds to "targetType" XSD annotation.
     *
     * Returns empty set if not specified. Must not return null.
     *
     * @return set of target type names
     */
    @Override
    public QName getTargetTypeName() {
        return targetTypeName;
    }

    @Override
    public void setTargetTypeName(QName targetTypeName) {
        checkMutable();
        this.targetTypeName = targetTypeName;
    }

    @Override
    public @Nullable PrismObjectDefinition<?> getTargetObjectDefinition() {
        return targetObjectDefinition;
    }

    public void setTargetObjectDefinition(PrismObjectDefinition<?> targetObjectDefinition) {
        this.targetObjectDefinition = targetObjectDefinition;
    }

    @Override
    public boolean isComposite() {
        return isComposite;
    }

    @Override
    public void setComposite(boolean isComposite) {
        checkMutable();
        this.isComposite = isComposite;
    }

    @Override
    public <T extends ItemDefinition<?>> T findItemDefinition(@NotNull ItemPath path, @NotNull Class<T> clazz) {
        if (!path.startsWithObjectReference()) {
            return super.findItemDefinition(path, clazz);
        } else {
            var first = path.first();
            ItemPath rest = path.rest();
            var targetType = getTargetTypeName();
            if (first instanceof ObjectReferencePathSegment) {
                var typeHint = ((ObjectReferencePathSegment) first).typeHint();
                if (typeHint.isPresent()) {
                    targetType = typeHint.get();
                }
            }
            if (targetType == null) {
                targetType = PrismContext.get().getDefaultReferenceTargetType();
            }
            PrismObjectDefinition<?> referencedObjectDefinition =
                    PrismContext.get().getSchemaRegistry().determineReferencedObjectDefinition(targetType, rest);
            return ((ItemDefinition<?>) referencedObjectDefinition).findItemDefinition(rest, clazz);
        }
    }

    @NotNull
    @Override
    public PrismReference instantiate() {
        return instantiate(getItemName());
    }

    @NotNull
    @Override
    public PrismReference instantiate(QName name) {
        name = DefinitionUtil.addNamespaceIfApplicable(name, this.itemName);
        return new PrismReferenceImpl(name, this);
    }

    @Override
    public @NotNull ReferenceDelta createEmptyDelta(ItemPath path) {
        return new ReferenceDeltaImpl(path, this);
    }

    @Override
    public Class<ObjectReferenceType> getTypeClass() {
        return ObjectReferenceType.class;
    }

    @Override
    public PrismReferenceDefinitionMutator mutator() {
        checkMutableOnExposing();
        return this;
    }

    @Override
    public @NotNull PrismReferenceDefinition clone() {
        return cloneWithNewName(itemName);
    }

    @Override
    public @NotNull PrismReferenceDefinition cloneWithNewName(@NotNull ItemName itemName) {
        PrismReferenceDefinitionImpl clone = new PrismReferenceDefinitionImpl(itemName, getTypeName());
        clone.copyDefinitionDataFrom(this);
        return clone;
    }

    protected void copyDefinitionDataFrom(PrismReferenceDefinition source) {
        super.copyDefinitionDataFrom(source);
        targetTypeName = source.getTargetTypeName();
        targetObjectDefinition = source.getTargetObjectDefinition();
        isComposite = source.isComposite();
    }

    /**
     * Return a human-readable name of this class suitable for logs.
     */
    @Override
    public String getDebugDumpClassName() {
        return "PRD";
    }

    @Override
    public String getDocClassName() {
        return "reference";
    }

    @Override
    protected void extendToString(StringBuilder sb) {
        super.extendToString(sb);
        if (isComposite) {
            sb.append(",composite");
        }
    }

    @Override
    public Optional<ComplexTypeDefinition> structuredType() {
        return structuredType.get();
    }
}
