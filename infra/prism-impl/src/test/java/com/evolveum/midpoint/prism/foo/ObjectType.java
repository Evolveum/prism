/*
 * Copyright (c) 2010-2013 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.foo;

import com.evolveum.midpoint.prism.Containerable;
import com.evolveum.midpoint.prism.Objectable;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.impl.binding.AbstractMutableObjectable;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.prism.xml.ns._public.types_3.ObjectReferenceType;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;

import jakarta.xml.bind.annotation.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for ObjectType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ObjectType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://midpoint.evolveum.com/xml/ns/test/foo-1.xsd}name" minOccurs="0"/>
 *         &lt;element ref="{http://midpoint.evolveum.com/xml/ns/test/foo-1.xsd}description" minOccurs="0"/>
 *         &lt;element ref="{http://midpoint.evolveum.com/xml/ns/test/foo-1.xsd}extension" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="oid" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="version" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ObjectType", propOrder = {
    "name",
    "description",
    "extension",
    "parentOrgRef",
    "metadata"
})
@XmlSeeAlso({
    UserType.class
})
public abstract class ObjectType
    extends AbstractMutableObjectable
    implements Serializable, Objectable
{

    // This is NOT GENERATED. It is supplied here manually for the testing.
    public static final String NS_FOO = "http://midpoint.evolveum.com/xml/ns/test/foo-1.xsd";

    // This is NOT GENERATED. It is supplied here manually for the testing.
    public static final ItemName F_NAME = ItemName.from(NS_FOO, "name");
    public static final ItemName F_DESCRIPTION = ItemName.from(NS_FOO, "description");
    public static final ItemName F_EXTENSION = ItemName.from(NS_FOO, "extension");
    public static final ItemName F_PARENT_ORG_REF = ItemName.from(NS_FOO, "parentOrgRef");
    public static final ItemName F_METADATA = ItemName.interned(NS_FOO, "metadata");

    private static final long serialVersionUID = 201202081233L;
    protected PolyStringType name;
    protected String description;
    protected Extension extension;
    @XmlAttribute(name = "oid")
    protected String oid;
    @XmlAttribute(name = "version")
    protected String version;
    protected List<ObjectReferenceType> parentOrgRef;

    /**
     * Gets the value of the name property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    @Override
    public PolyStringType getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    @Override
    public void setName(PolyStringType value) {
        this.name = value;
    }

    /**
     * Gets the value of the description property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    @Override
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the extension property.
     *
     * @return
     *     possible object is
     *     {@link Extension }
     *
     */
    public Extension getExtension() {
        return extension;
    }

    /**
     * Sets the value of the extension property.
     *
     * @param value
     *     allowed object is
     *     {@link Extension }
     *
     */
    public void setExtension(Extension value) {
        this.extension = value;
    }

    /**
     * Gets the value of the oid property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    @Override
    public String getOid() {
        return oid;
    }

    /**
     * Sets the value of the oid property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    @Override
    public void setOid(String value) {
        this.oid = value;
    }

    /**
     * Gets the value of the version property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    @Override
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    @Override
    public void setVersion(String value) {
        this.version = value;
    }

    @Override
    public String toDebugName() {
        return toDebugType()+":"+getOid()+"("+getName()+")";
    }

    @Override
    public String toDebugType() {
        return "object";
    }

    @Override
    public PrismObject asPrismObject() {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public void setupContainer(PrismObject object) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public PrismContainerValue asPrismContainerValue() {
         throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public void setupContainerValue(PrismContainerValue container) {
        // just ignored (to allow getRealValue() to work on test prism objects)
    }

    public List<ObjectReferenceType> getParentOrgRef() {
        if (parentOrgRef == null) {
            parentOrgRef = new ArrayList<>();
        }
        return parentOrgRef;
    }

    @XmlElement(name = "metadata")
    public MetadataType getMetadata() {
        return this.prismGetSingleContainerable(F_METADATA, MetadataType.class);
    }

    public void setMetadata(MetadataType value) {
        this.prismSetSingleContainerable(F_METADATA, value);
    }

    public ObjectType metadata(MetadataType value) {
        setMetadata(value);
        return this;
    }

    public MetadataType beginMetadata() {
        MetadataType value = new MetadataType();
        metadata(value);
        return value;
    }
}
