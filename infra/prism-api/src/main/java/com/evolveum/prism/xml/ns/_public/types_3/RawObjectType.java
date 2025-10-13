/*
 * Copyright (C) 22 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.prism.xml.ns._public.types_3;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismObject;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RawObjectType", propOrder = {
})
public class RawObjectType extends ObjectType {

    private static final long serialVersionUID = 1L;

    private RawType value;

    public RawObjectType(RawType value) {
        super();
        this.value = value;
    }

    @Override
    public String getOid() {
        return null;
    }

    @Override
    public void setOid(String oid) {

    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public void setVersion(String version) {

    }

    @Override
    public PolyStringType getName() {
        return null;
    }

    @Override
    public void setName(PolyStringType name) {

    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void setDescription(String description) {

    }

    @Override
    public String toDebugName() {
        return "RawObjectType";
    }

    @Override
    public String toDebugType() {
        return "RawObjectType";
    }

    @Override
    public PrismObject asPrismObject() {
        throw new IllegalStateException("RawObjectType is not intended to be used as PrismObject");
    }

    @Override
    public void setupContainer(PrismObject object) {
        throw new IllegalStateException("RawObjectType is not intended to be used as container");
    }

    @Override
    public PrismContainerValue asPrismContainerValue() {
        throw new IllegalStateException("RawObjectType is not intended to be used as container");
    }

    @Override
    public void setupContainerValue(PrismContainerValue container) {
        throw new IllegalStateException("RawObjectType is not intended to be used as container");
    }

    public RawType rawValue() {
        return value;
    }

    @Override
    public String toString() {
        return "RawObjectType(" + value +")" ;
    }
}
