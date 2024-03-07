/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.equivalence.ParameterizedEquivalenceStrategy;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.polystring.PolyString;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.util.Objects;

/**
 * Extension of PrismContainerValue that holds object-specific data (OID and version).
 * It was created to make methods returning/accepting ItemValue universally usable;
 * not losing OID/version data when object values are passed via such interfaces.
 *
 * This value is to be held by PrismObject. And such object should hold exactly one
 * PrismObjectValue.
 */
public class PrismObjectValueImpl<O extends Objectable> extends PrismContainerValueImpl<O> implements PrismObjectValue<O> {

    protected String oid;
    protected String version;

    PrismObjectValueImpl() {
    }

    PrismObjectValueImpl(O objectable) {
        super(objectable);
    }

    private PrismObjectValueImpl(OriginType type, Objectable source, PrismContainerable<?> container, Long id,
            ComplexTypeDefinition complexTypeDefinition, String oid, String version) {
        super(type, source, container, id, complexTypeDefinition);
        this.oid = oid;
        this.version = version;
    }

    @Override
    public String getOid() {
        return oid;
    }

    @Override
    public void setOid(String oid) {
        checkMutable();
        this.oid = oid;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public void setVersion(String version) {
        checkMutable();
        this.version = version;
    }

    @Override
    public O asObjectable() {
        return asContainerable();
    }

    @Override
    public PrismObject<O> asPrismObject() {
        //noinspection unchecked
        return asObjectable().asPrismObject();
    }

    @Override
    public PolyString getName() {
        return asPrismObject().getName();
    }

    @Override
    public PrismContainer<?> getExtension() {
        return asPrismObject().getExtension();
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public PrismObjectValueImpl<O> clone() {
        return cloneComplex(CloneStrategy.LITERAL);
    }

    @Override
    public PrismObjectValueImpl<O> cloneComplex(CloneStrategy strategy) {
        PrismObjectValueImpl<O> clone = new PrismObjectValueImpl<>(
                getOriginType(), getOriginObject(), getParent(), getId(), complexTypeDefinition, oid, version);
        copyValues(strategy, clone);
        return clone;
    }

    // TODO consider the strategy
    @Override
    public int hashCode(@NotNull ParameterizedEquivalenceStrategy strategy) {
        return Objects.hash(super.hashCode(strategy), oid);
    }

    @Override
    public boolean equivalent(PrismContainerValue<?> other) {
        if (!(other instanceof PrismObjectValueImpl<?> otherPov)) {
            return false;
        }
        return StringUtils.equals(oid, otherPov.oid) && super.equivalent(other);
    }

    @Override
    public boolean equals(PrismValue other, @NotNull ParameterizedEquivalenceStrategy strategy) {
        return other instanceof PrismObjectValue &&
                Objects.equals(oid, ((PrismObjectValue<?>) other).getOid()) &&
                super.equals(other, strategy);
    }

    @Override
    public String toString() {
        // we don't delegate to PrismObject, because various exceptions during that process could in turn call this method
        StringBuilder sb = new StringBuilder();
        sb.append("POV:");
        if (getParent() != null) {
            sb.append(getParent().getElementName().getLocalPart()).append(":");
        } else if (getComplexTypeDefinition() != null) {
            sb.append(getComplexTypeDefinition().getTypeName().getLocalPart()).append(":");
        }
        sb.append(oid).append("(");
        PrismProperty<?> nameProperty = findProperty(new ItemName(PrismConstants.NAME_LOCAL_NAME));
        sb.append(nameProperty != null ? nameProperty.getRealValue() : null);
        sb.append(")");
        return sb.toString();
    }

    @Override
    protected void detailedDebugDumpStart(StringBuilder sb) {
        sb.append("POV").append(": ");
    }

    @Override
    protected void debugDumpIdentifiers(StringBuilder sb) {
        sb.append("oid=").append(oid);
        sb.append(", version=").append(version);
    }

    @Override
    public String toHumanReadableString() {
        return "oid="+oid+": "+items.size()+" items";
    }

    @Override
    public PrismContainer<O> asSingleValuedContainer(@NotNull QName itemName) {
        throw new UnsupportedOperationException("Not supported for PrismObjectValue yet.");
    }

    public static <T extends Objectable> T asObjectable(PrismObject<T> object) {
        return object != null ? object.asObjectable() : null;
    }

    @Override
    public Object getIdentifier() {
        return oid;
    }
}
