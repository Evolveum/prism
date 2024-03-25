/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.schema.features;

import static com.evolveum.midpoint.prism.impl.schema.SchemaProcessorUtil.getAnnotationElement;

import java.util.List;
import javax.xml.namespace.QName;

import com.google.common.collect.ImmutableList;
import com.sun.xml.xsom.*;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import com.evolveum.midpoint.prism.EnumerationTypeDefinition;
import com.evolveum.midpoint.prism.impl.EnumerationTypeDefinitionImpl;
import com.evolveum.midpoint.prism.schema.DefinitionFeatureParser;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.MiscUtil;

public class EnumerationValuesXsomParser
        implements DefinitionFeatureParser<List<EnumerationTypeDefinition.ValueDefinition>, XSSimpleType> {

    private static final QName TYPESAFE_ENUM_MEMBER = new QName("https://jakarta.ee/xml/ns/jaxb", "typesafeEnumMember");
    private static final QName TYPESAFE_ENUM_MEMBER_LEGACY = new QName("http://java.sun.com/xml/ns/jaxb","typesafeEnumMember");

    @Override
    public @Nullable List<EnumerationTypeDefinition.ValueDefinition> getValue(@Nullable XSSimpleType simpleType) {
        if (simpleType == null || !simpleType.isRestriction()) {
            return null;
        } else {
            var ret = ImmutableList.<EnumerationTypeDefinition.ValueDefinition>builder();
            for (XSFacet facet : simpleType.asRestriction().getDeclaredFacets(XSFacet.FACET_ENUMERATION)) {
                ret.add(createEnumerationValue(facet));
            }
            var list = ret.build();
            return list.isEmpty() ? null : list;
        }
    }

    private EnumerationTypeDefinition.ValueDefinition createEnumerationValue(XSFacet facet) {
        String value = facet.getValue().toString();
        String documentationText = null;
        String constantName = null;
        XSAnnotation annotation = facet.getAnnotation();
        if (annotation != null) {
            Element documentationElement = getAnnotationElement(annotation, DOMUtil.XSD_DOCUMENTATION_ELEMENT);
            if (documentationElement != null) {
                documentationText = DOMUtil.serializeElementContent(documentationElement);
            }
            Element appInfo = getAnnotationElement(annotation, DOMUtil.XSD_APPINFO_ELEMENT);
            if (appInfo != null) {
                // Jakarta namespace
                Element typeSetEnum = DOMUtil.getChildElement(appInfo, TYPESAFE_ENUM_MEMBER);
                if (typeSetEnum == null) {
                    // SUN JAXB namespace
                    typeSetEnum = DOMUtil.getChildElement(appInfo, TYPESAFE_ENUM_MEMBER_LEGACY);
                }
                if (typeSetEnum != null) {
                    // Make sure to have NULL in constant name if string is blank
                    constantName = MiscUtil.nullIfBlank(typeSetEnum.getAttribute("name"));
                }
            }
        }
        return new EnumerationTypeDefinitionImpl.ValueDefinitionImpl(value, documentationText, constantName);
    }
}
