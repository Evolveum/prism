/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.impl.schema.features;

import static com.evolveum.midpoint.prism.PrismConstants.SCHEMA_APP_INFO;
import static com.evolveum.midpoint.prism.PrismConstants.SCHEMA_DOCUMENTATION;
import static com.evolveum.midpoint.prism.impl.schema.SchemaProcessorUtil.getAnnotationElement;

import java.util.ArrayList;
import java.util.List;

import com.evolveum.midpoint.prism.PrismConstants;

import com.sun.xml.xsom.*;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import com.evolveum.midpoint.prism.schema.DefinitionFeatureParser;

import org.w3c.dom.NodeList;

/**
 * Collects values with their labels and documentation from XSD.
 *
 * For example,
 *
 * ----
 *         <xsd:restriction base="xsd:string">
 *             <xsd:enumeration value="pegLeg">
 *                 <xsd:annotation>
 *                     <xsd:documentation>Pirate with a peg leg</xsd:documentation>
 *                     <xsd:appinfo>
 *                         <a:label>Peg Leg</a:label>
 *                     </xsd:appinfo>
 *                 </xsd:annotation>
 *             </xsd:enumeration>
 *             <xsd:enumeration value="hook"/>
 *         </xsd:restriction>
 * ----
 *
 * Returns a list of records: (pegLeg, Peg Leg, Pirate with a peg leg) + (hook, null, null)
 */
public class EnumerationValuesInfoXsomParser
        implements DefinitionFeatureParser<List<EnumerationValuesInfoXsomParser.EnumValueInfo>, XSType> {

    @Override
    public @Nullable List<EnumValueInfo> getValue(@Nullable XSType xsType) {
        var simpleType = xsType != null ? xsType.asSimpleType() : null;
        if (simpleType == null || !simpleType.isRestriction()) {
            return null;
        }
        List<EnumValueInfo> rv = new ArrayList<>();
        for (XSFacet facet : simpleType.asRestriction().getDeclaredFacets(XSFacet.FACET_ENUMERATION)) {
            String value = facet.getValue().value;
            Element documentationElement = getAnnotationElement(facet.getAnnotation(), SCHEMA_DOCUMENTATION);
            Element appInfo = getAnnotationElement(facet.getAnnotation(), SCHEMA_APP_INFO);
            String label = value;
            if (appInfo != null) {
                NodeList labelElements = appInfo.getElementsByTagNameNS(
                        PrismConstants.A_LABEL.getNamespaceURI(),
                        PrismConstants.A_LABEL.getLocalPart());
                if (labelElements.getLength() != 0) {
                    var labelElement = (Element) labelElements.item(0);
                    label = labelElement.getTextContent();
                }
            }
            String documentation = documentationElement != null ? documentationElement.getTextContent() : null;
            rv.add(new EnumValueInfo(value, label, documentation));
        }
        return List.copyOf(rv);
    }

    public record EnumValueInfo(String value, String label, String documentation) {
    }
}
