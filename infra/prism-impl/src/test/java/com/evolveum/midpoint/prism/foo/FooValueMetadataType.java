/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.foo;

import com.evolveum.midpoint.prism.PrismContainer;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.impl.binding.AbstractMutableContainerable;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.util.Producer;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import javax.xml.namespace.QName;

/**
 * Created by Dominik.
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "ValueMetadataType", propOrder = {
        "extension",
        "storage",
        "process",
        "provisioning",
        "transformation",
        "provenance"
})
public class FooValueMetadataType extends AbstractMutableContainerable {
    private static final long serialVersionUID = 201105211233L;
    public static final QName COMPLEX_TYPE = new QName(ObjectType.NS_FOO, "ValueMetadataType");
    public static final ItemName F_EXTENSION = ItemName.interned(ObjectType.NS_FOO, "extension");
    public static final ItemName F_STORAGE = ItemName.interned(ObjectType.NS_FOO, "storage");
    public static final ItemName F_PROCESS = ItemName.interned(ObjectType.NS_FOO, "process");
    public static final ItemName F_PROVISIONING = ItemName.interned(ObjectType.NS_FOO, "provisioning");
    public static final ItemName F_TRANSFORMATION = ItemName.interned(ObjectType.NS_FOO, "transformation");
    public static final ItemName F_PROVENANCE = ItemName.interned(ObjectType.NS_FOO, "provenance");
    public static final Producer<FooValueMetadataType> FACTORY = new Producer<FooValueMetadataType>() {

        private static final long serialVersionUID = 201105211233L;

        public FooValueMetadataType run() {
            return new FooValueMetadataType();
        }

    }
            ;

    public FooValueMetadataType() {
        super();
    }

    @Deprecated
    public FooValueMetadataType(PrismContext context) {
        super();
    }

    @XmlElement(name = "extension")
    public ExtensionType getExtension() {
        return this.prismGetSingleContainerable(F_EXTENSION, ExtensionType.class);
    }

    public void setExtension(ExtensionType value) {
        this.prismSetSingleContainerable(F_EXTENSION, value);
    }

    @XmlElement(name = "storage")
    public StorageMetadataType getStorage() {
        return this.prismGetSingleContainerable(F_STORAGE, StorageMetadataType.class);
    }

    public void setStorage(StorageMetadataType value) {
        this.prismSetSingleContainerable(F_STORAGE, value);
    }

    @XmlElement(name = "process")
    public ProcessMetadataType getProcess() {
        return this.prismGetSingleContainerable(F_PROCESS, ProcessMetadataType.class);
    }

    public void setProcess(ProcessMetadataType value) {
        this.prismSetSingleContainerable(F_PROCESS, value);
    }

    @XmlElement(name = "provisioning")
    public ProvisioningMetadataType getProvisioning() {
        return this.prismGetSingleContainerable(F_PROVISIONING, ProvisioningMetadataType.class);
    }

    public void setProvisioning(ProvisioningMetadataType value) {
        this.prismSetSingleContainerable(F_PROVISIONING, value);
    }

    @XmlElement(name = "transformation")
    public TransformationMetadataType getTransformation() {
        return this.prismGetSingleContainerable(F_TRANSFORMATION, TransformationMetadataType.class);
    }

    public void setTransformation(TransformationMetadataType value) {
        this.prismSetSingleContainerable(F_TRANSFORMATION, value);
    }

    @XmlElement(name = "provenance")
    public ProvenanceMetadataType getProvenance() {
        return this.prismGetSingleContainerable(F_PROVENANCE, ProvenanceMetadataType.class);
    }

    public void setProvenance(ProvenanceMetadataType value) {
        this.prismSetSingleContainerable(F_PROVENANCE, value);
    }

    public boolean equals(Object other) {
        return super.equals(other);
    }

    public FooValueMetadataType id(Long value) {
        setId(value);
        return this;
    }

    public FooValueMetadataType extension(ExtensionType value) {
        setExtension(value);
        return this;
    }

    public ExtensionType beginExtension() {
        ExtensionType value = new ExtensionType();
        extension(value);
        return value;
    }

    public FooValueMetadataType storage(StorageMetadataType value) {
        setStorage(value);
        return this;
    }

    public StorageMetadataType beginStorage() {
        StorageMetadataType value = new StorageMetadataType();
        storage(value);
        return value;
    }

    public FooValueMetadataType process(ProcessMetadataType value) {
        setProcess(value);
        return this;
    }

    public ProcessMetadataType beginProcess() {
        ProcessMetadataType value = new ProcessMetadataType();
        process(value);
        return value;
    }

    public FooValueMetadataType provisioning(ProvisioningMetadataType value) {
        setProvisioning(value);
        return this;
    }

    public ProvisioningMetadataType beginProvisioning() {
        ProvisioningMetadataType value = new ProvisioningMetadataType();
        provisioning(value);
        return value;
    }

    public FooValueMetadataType transformation(TransformationMetadataType value) {
        setTransformation(value);
        return this;
    }

    public TransformationMetadataType beginTransformation() {
        TransformationMetadataType value = new TransformationMetadataType();
        transformation(value);
        return value;
    }

    public FooValueMetadataType provenance(ProvenanceMetadataType value) {
        setProvenance(value);
        return this;
    }

    public ProvenanceMetadataType beginProvenance() {
        ProvenanceMetadataType value = new ProvenanceMetadataType();
        provenance(value);
        return value;
    }

    public<X >X end() {
        return ((X)((PrismContainerValue)((PrismContainer) asPrismContainerValue().getParent()).getParent()).asContainerable());
    }

    @Override
    public FooValueMetadataType clone() {
        return ((FooValueMetadataType) super.clone());
    }

}
