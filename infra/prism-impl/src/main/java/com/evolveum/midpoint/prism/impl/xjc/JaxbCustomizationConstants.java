/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.impl.xjc;

import javax.xml.namespace.QName;

/**
 * JAXB customization names used in XSD annotation/appinfo elements.
 */
public final class JaxbCustomizationConstants {

    public static final String JAKARTA_NAMESPACE = "https://jakarta.ee/xml/ns/jaxb";
    public static final String LEGACY_NAMESPACE = "http://java.sun.com/xml/ns/jaxb";

    public static final QName TYPESAFE_ENUM_MEMBER = new QName(JAKARTA_NAMESPACE, "typesafeEnumMember");
    public static final QName TYPESAFE_ENUM_CLASS = new QName(JAKARTA_NAMESPACE, "typesafeEnumClass");
    public static final QName TYPESAFE_ENUM_MEMBER_LEGACY = new QName(LEGACY_NAMESPACE, "typesafeEnumMember");

    private JaxbCustomizationConstants() {
    }
}
