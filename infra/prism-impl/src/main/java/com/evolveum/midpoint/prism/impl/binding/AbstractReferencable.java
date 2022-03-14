/*
 * Copyright (c) 2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.binding;

import java.io.Serializable;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.Objectable;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismReference;
import com.evolveum.midpoint.prism.PrismReferenceValue;
import com.evolveum.midpoint.prism.Referencable;
import com.evolveum.midpoint.prism.impl.PrismReferenceValueImpl;
import com.evolveum.midpoint.prism.impl.xjc.PrismForJAXBUtil;
import com.evolveum.midpoint.util.Producer;
import com.evolveum.prism.xml.ns._public.query_3.SearchFilterType;
import com.evolveum.prism.xml.ns._public.types_3.EvaluationTimeType;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;
import com.evolveum.prism.xml.ns._public.types_3.ReferentialIntegrityType;

public abstract class AbstractReferencable<T extends AbstractReferencable<T>> implements Cloneable, Referencable, Serializable {

    private PrismReferenceValue value;

    @Override
    public PrismReferenceValue asReferenceValue() {
        if (value == null) {
            value = new PrismReferenceValueImpl();
        }
        return value;
    }

    @Override
    public T setupReferenceValue(PrismReferenceValue value) {
        this.value = value;
        return thisInstance();
    }

    protected abstract T thisInstance();

    @Override
    public QName getType() {
        return asReferenceValue().getTargetType();
    }

    @Override
    public String getOid() {
        return asReferenceValue().getOid();
    }

    public void setType(QName value) {
        asReferenceValue().setTargetType(value, true);
    }

    public void setOid(String value) {
        asReferenceValue().setOid(value);
    }

    @Override
    public QName getRelation() {
        return asReferenceValue().getRelation();
    }

    public void setRelation(QName value) {
        asReferenceValue().setRelation(value);
    }

    @Override
    public String getDescription() {
        return asReferenceValue().getDescription();
    }

    public void setDescription(String value) {
        asReferenceValue().setDescription(value);
    }

    @Override
    public SearchFilterType getFilter() {
        return PrismForJAXBUtil.getFilter(asReferenceValue());
    }

    public void setFilter(SearchFilterType value) {
        PrismForJAXBUtil.setReferenceFilterClauseXNode(asReferenceValue(), value);
    }

    @Override
    public EvaluationTimeType getResolutionTime() {
        return asReferenceValue().getResolutionTime();
    }

    public void setResolutionTime(EvaluationTimeType value) {
        asReferenceValue().setResolutionTime(value);
    }

    @Override
    public ReferentialIntegrityType getReferentialIntegrity() {
        return asReferenceValue().getReferentialIntegrity();
    }

    public void setReferentialIntegrity(ReferentialIntegrityType value) {
        asReferenceValue().setReferentialIntegrity(value);
    }

    public PrismObject getObject() {
        return asReferenceValue().getObject();
    }

    public Objectable getObjectable() {
        return PrismForJAXBUtil.getReferenceObjectable(asReferenceValue());
    }

    @Override
    public PolyStringType getTargetName() {
        return PrismForJAXBUtil.getReferenceTargetName(asReferenceValue());
    }

    public void setTargetName(PolyStringType value) {
        PrismForJAXBUtil.setReferenceTargetName(asReferenceValue(), value);
    }

    @SuppressWarnings("unchecked")
    public<X >X end() {
        return ((X)((PrismContainerValue<?>)((PrismReference) asReferenceValue().getParent()).getParent()).asContainerable());
    }

    @Override
    public int hashCode() {
        return asReferenceValue().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AbstractReferencable)) {
            return false;
        }

        return this.asReferenceValue().equals(((AbstractReferencable)obj).asReferenceValue());
    }

    @Override
    public abstract AbstractReferencable<T> clone();

    protected <T extends AbstractReferencable<T>> T clone(Producer<T> producer) {
        T ret = producer.run();
        ret.setupReferenceValue(asReferenceValue().clone());
        return ret;
    }

    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName())
                .append("[")
                .append("value=")
                .append(value)
                .append("]")
                .toString();
    }

}
