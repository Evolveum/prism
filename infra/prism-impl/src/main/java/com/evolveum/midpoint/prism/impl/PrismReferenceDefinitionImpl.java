/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl;

import com.evolveum.axiom.concepts.Lazy;
import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.impl.delta.ReferenceDeltaImpl;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.path.ObjectReferencePathSegment;
import com.evolveum.midpoint.prism.util.DefinitionUtil;
import com.evolveum.midpoint.util.QNameUtil;
import com.evolveum.prism.xml.ns._public.types_3.ObjectReferenceType;

import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.util.Optional;

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
public class PrismReferenceDefinitionImpl extends ItemDefinitionImpl<PrismReference> implements MutablePrismReferenceDefinition {

    private static final long serialVersionUID = 2427488779612517600L;
    private QName targetTypeName;
    private QName compositeObjectElementName;
    private boolean isComposite = false;

    private transient Lazy<Optional<ComplexTypeDefinition>> structuredType;

    public PrismReferenceDefinitionImpl(QName elementName, QName typeName) {
        this(elementName, typeName, null);
    }

    public PrismReferenceDefinitionImpl(QName elementName, QName typeName, QName definedInType) {
        super(elementName, typeName, definedInType);
        structuredType = Lazy.from(() ->
                Optional.ofNullable(getPrismContext().getSchemaRegistry().findComplexTypeDefinitionByType(getTypeName()))
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
    public QName getCompositeObjectElementName() {
        return compositeObjectElementName;
    }

    public void setCompositeObjectElementName(QName compositeObjectElementName) {
        this.compositeObjectElementName = compositeObjectElementName;
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
    public boolean isValidFor(@NotNull QName elementQName, @NotNull Class<? extends ItemDefinition<?>> clazz, boolean caseInsensitive) {
        return clazz.isAssignableFrom(this.getClass()) &&
                (QNameUtil.match(elementQName, getItemName(), caseInsensitive) ||
                        QNameUtil.match(elementQName, getCompositeObjectElementName(), caseInsensitive));
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
                targetType = getPrismContext().getDefaultReferenceTargetType();
            }
            PrismObjectDefinition<?> referencedObjectDefinition =
                    getSchemaRegistry().determineReferencedObjectDefinition(targetType, rest);
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
        return new PrismReferenceImpl(name, this, getPrismContext());
    }

    @Override
    public @NotNull ItemDelta createEmptyDelta(ItemPath path) {
        return new ReferenceDeltaImpl(path, this, getPrismContext());
    }

    @Override
    public boolean canBeDefinitionOf(PrismValue pvalue) {
        if (pvalue == null) {
            return false;
        }
        if (!(pvalue instanceof PrismReferenceValue)) {
            return false;
        }
        Itemable parent = pvalue.getParent();
        if (parent != null) {
            if (!(parent instanceof PrismReference)) {
                return false;
            }
            return canBeDefinitionOf((PrismReference) parent);
        } else {
            return true;
        }
    }

    @Override
    public Class getTypeClass() {
        return ObjectReferenceType.class;
    }

    @Override
    public MutablePrismReferenceDefinition toMutable() {
        checkMutableOnExposing();
        return this;
    }

    @NotNull
    @Override
    public PrismReferenceDefinition clone() {
        PrismReferenceDefinitionImpl clone = new PrismReferenceDefinitionImpl(getItemName(), getTypeName());
        clone.copyDefinitionDataFrom(this);
        return clone;
    }

    protected void copyDefinitionDataFrom(PrismReferenceDefinition source) {
        super.copyDefinitionDataFrom(source);
        targetTypeName = source.getTargetTypeName();
        compositeObjectElementName = source.getCompositeObjectElementName();
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
