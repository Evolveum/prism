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
import com.evolveum.midpoint.prism.xml.XmlTypeConverter;
import com.evolveum.midpoint.util.Producer;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

/**
 * Created by Dominik.
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "ProvisioningMetadataType", propOrder = {
        "lastProvisioningTimestamp"
})
public class ProvisioningMetadataType
        extends AbstractMutableContainerable {

    private static final long serialVersionUID = 201105211233L;
    public static final QName COMPLEX_TYPE = new QName(ObjectType.NS_FOO, "ProvisioningMetadataType");
    public static final ItemName F_LAST_PROVISIONING_TIMESTAMP = ItemName.interned(ObjectType.NS_FOO, "lastProvisioningTimestamp");
    public static final Producer<ProvisioningMetadataType> FACTORY = new Producer<ProvisioningMetadataType>() {

        private static final long serialVersionUID = 201105211233L;

        public ProvisioningMetadataType run() {
            return new ProvisioningMetadataType();
        }

    }
            ;

    public ProvisioningMetadataType() {
        super();
    }

    @Deprecated
    public ProvisioningMetadataType(PrismContext context) {
        super();
    }

    @XmlElement(name = "lastProvisioningTimestamp")
    public XMLGregorianCalendar getLastProvisioningTimestamp() {
        return this.prismGetPropertyValue(F_LAST_PROVISIONING_TIMESTAMP, XMLGregorianCalendar.class);
    }

    public void setLastProvisioningTimestamp(XMLGregorianCalendar value) {
        this.prismSetPropertyValue(F_LAST_PROVISIONING_TIMESTAMP, value);
    }

    public boolean equals(Object other) {
        return super.equals(other);
    }

    public ProvisioningMetadataType lastProvisioningTimestamp(XMLGregorianCalendar value) {
        setLastProvisioningTimestamp(value);
        return this;
    }

    public ProvisioningMetadataType lastProvisioningTimestamp(String value) {
        return lastProvisioningTimestamp(XmlTypeConverter.createXMLGregorianCalendar(value));
    }

    public<X >X end() {
        return ((X)((PrismContainerValue)((PrismContainer) asPrismContainerValue().getParent()).getParent()).asContainerable());
    }

    @Override
    public ProvisioningMetadataType clone() {
        return ((ProvisioningMetadataType) super.clone());
    }

}
