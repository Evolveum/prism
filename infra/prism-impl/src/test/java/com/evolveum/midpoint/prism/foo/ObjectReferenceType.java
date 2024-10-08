/*
 * Copyright (c) 2010-2019 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2014.02.07 at 10:53:52 AM CET
//


package com.evolveum.midpoint.prism.foo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.evolveum.midpoint.prism.Referencable;
import com.evolveum.midpoint.prism.impl.binding.AbstractReferencable;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.util.Producer;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyAttribute;
import jakarta.xml.bind.annotation.XmlAnyElement;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;

import static com.evolveum.midpoint.prism.foo.ObjectType.NS_FOO;

/**
 *
 *                 Reference to an object. It contains OID of the object that it refers to.
 *
 *
 * <p>Java class for ObjectReferenceType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ObjectReferenceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="filter" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;any processContents='lax' maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="object" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;any processContents='lax' maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *                 &lt;anyAttribute processContents='lax'/>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="oid" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="type" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ObjectReferenceType", propOrder = {
    "description",
    "documentation",
    "filter",
    "resolutionTime",
    "referentialIntegrity",
    "targetName"
})
public class ObjectReferenceType extends AbstractReferencable<ObjectReferenceType>
        implements Referencable
{
    public static final QName COMPLEX_TYPE = new QName(NS_FOO, "ObjectReferenceType");
    public static final ItemName F_TYPE = ItemName.interned(NS_FOO, "type");
    public static final ItemName F_OID = ItemName.interned(NS_FOO, "oid");
    public static final ItemName F_RELATION = ItemName.interned(NS_FOO, "relation");
    public static final ItemName F_DESCRIPTION = ItemName.interned(NS_FOO, "description");
    public static final ItemName F_DOCUMENTATION = ItemName.interned(NS_FOO, "documentation");
    public static final ItemName F_FILTER = ItemName.interned(NS_FOO, "filter");
    public static final ItemName F_RESOLUTION_TIME = ItemName.interned(NS_FOO, "resolutionTime");
    public static final ItemName F_REFERENTIAL_INTEGRITY = ItemName.interned(NS_FOO, "referentialIntegrity");
    public static final ItemName F_TARGET_NAME = ItemName.interned(NS_FOO, "targetName");
    public static final Producer<ObjectReferenceType> FACTORY = new Producer<ObjectReferenceType>() {

        private static final long serialVersionUID = 201105211233L;

        public ObjectReferenceType run() {
            return new ObjectReferenceType();
        }

    };
    protected String description;
    protected ObjectReferenceType.Filter filter;
    protected ObjectReferenceType.Object object;
    @XmlAttribute(name = "oid")
    protected String oid;
    @XmlAttribute(name = "type")
    protected QName type;

    /**
     * Gets the value of the description property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
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
    public void setDescription(String value) {
        this.description = value;
    }

    @Override
    public AbstractReferencable<ObjectReferenceType> clone() {
        return null;
    }

    /**
     * Sets the value of the filter property.
     *
     * @param value
     *     allowed object is
     *     {@link ObjectReferenceType.Filter }
     *
     */
    public void setFilter(ObjectReferenceType.Filter value) {
        this.filter = value;
    }

    /**
     * Sets the value of the object property.
     *
     * @param value
     *     allowed object is
     *     {@link ObjectReferenceType.Object }
     *
     */
    public void setObject(ObjectReferenceType.Object value) {
        this.object = value;
    }

    /**
     * Gets the value of the oid property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
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
    public void setOid(String value) {
        this.oid = value;
    }

    @Override
    protected ObjectReferenceType thisInstance() {
        return null;
    }

    /**
     * Gets the value of the type property.
     *
     * @return
     *     possible object is
     *     {@link QName }
     *
     */
    public QName getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     *
     * @param value
     *     allowed object is
     *     {@link QName }
     *
     */
    public void setType(QName value) {
        this.type = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;any processContents='lax' maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     *
     *
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "any"
    })
    public static class Filter
        implements Serializable
    {

        private static final long serialVersionUID = 201202081233L;
        @XmlAnyElement(lax = true)
        protected List<java.lang.Object> any;

        /**
         * Gets the value of the any property.
         *
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the any property.
         *
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getAny().add(newItem);
         * </pre>
         *
         *
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Element }
         * {@link java.lang.Object }
         *
         *
         */
        public List<java.lang.Object> getAny() {
            if (any == null) {
                any = new ArrayList<>();
            }
            return this.any;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;any processContents='lax' maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/sequence>
     *       &lt;anyAttribute processContents='lax'/>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     *
     *
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "any"
    })
    public static class Object
        implements Serializable
    {

        private static final long serialVersionUID = 201202081233L;
        @XmlAnyElement(lax = true)
        protected List<java.lang.Object> any;
        @XmlAnyAttribute
        private Map<QName, String> otherAttributes = new HashMap<>();

        /**
         * Gets the value of the any property.
         *
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the any property.
         *
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getAny().add(newItem);
         * </pre>
         *
         *
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Element }
         * {@link java.lang.Object }
         *
         *
         */
        public List<java.lang.Object> getAny() {
            if (any == null) {
                any = new ArrayList<>();
            }
            return this.any;
        }

        /**
         * Gets a map that contains attributes that aren't bound to any typed property on this class.
         *
         * <p>
         * the map is keyed by the name of the attribute and
         * the value is the string value of the attribute.
         *
         * the map returned by this method is live, and you can add new attribute
         * by updating the map directly. Because of this design, there's no setter.
         *
         *
         * @return
         *     always non-null
         */
        public Map<QName, String> getOtherAttributes() {
            return otherAttributes;
        }

    }

}
