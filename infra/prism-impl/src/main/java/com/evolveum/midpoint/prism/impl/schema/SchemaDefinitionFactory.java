/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl.schema;

import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.EnumerationTypeDefinition.ValueDefinition;
import com.evolveum.midpoint.prism.impl.*;
import com.sun.xml.xsom.*;
import org.w3c.dom.Element;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.DisplayableValue;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import static com.evolveum.midpoint.prism.SimpleTypeDefinition.DerivationMethod.EXTENSION;
import static com.evolveum.midpoint.prism.SimpleTypeDefinition.DerivationMethod.RESTRICTION;
import static com.evolveum.midpoint.prism.SimpleTypeDefinition.DerivationMethod.SUBSTITUTION;

/**
 * @author semancik
 *
 */
public class SchemaDefinitionFactory {

    private static final QName TYPESAFE_ENUM_MEMBER = new QName("http://java.sun.com/xml/ns/jaxb","typesafeEnumMember");
    private static final String ENUMERATION_FACET = "enumeration";

    public MutableComplexTypeDefinition createComplexTypeDefinition(XSComplexType complexType,
            PrismContext prismContext, XSAnnotation annotation) throws SchemaException {

        QName typeName = new QName(complexType.getTargetNamespace(),complexType.getName());
        return new ComplexTypeDefinitionImpl(typeName);
    }

    public SimpleTypeDefinition createSimpleTypeDefinition(XSSimpleType simpleType,
            PrismContext prismContext, XSAnnotation annotation) throws SchemaException {

        QName typeName = new QName(simpleType.getTargetNamespace(), simpleType.getName());
        XSType baseType = simpleType.getBaseType();
        QName baseTypeName = baseType != null ? new QName(baseType.getTargetNamespace(), baseType.getName()) : null;
        SimpleTypeDefinition.DerivationMethod derivationMethod;
        switch (simpleType.getDerivationMethod()) {
            case XSSimpleType.EXTENSION: derivationMethod = EXTENSION; break;
            case XSSimpleType.RESTRICTION: derivationMethod = RESTRICTION; break;
            case XSSimpleType.SUBSTITUTION: derivationMethod = SUBSTITUTION; break;
            default: derivationMethod = null; // TODO are combinations allowed? e.g. EXTENSION+SUBSTITUTION?
        }
        return new SimpleTypeDefinitionImpl(typeName, baseTypeName, derivationMethod);
    }

    public <T> PrismPropertyDefinition<T> createPropertyDefinition(QName elementName, QName typeName, ComplexTypeDefinition complexTypeDefinition,
            PrismContext prismContext, XSAnnotation annotation, XSParticle elementParticle) throws SchemaException {
        var definedInType = complexTypeDefinition != null ? complexTypeDefinition.getTypeName() : null;
        return new PrismPropertyDefinitionImpl<>(elementName, typeName, definedInType);
    }

    public <T> MutablePrismPropertyDefinition<T> createPropertyDefinition(QName elementName, QName typeName, ComplexTypeDefinition complexTypeDefinition,
            PrismContext prismContext, XSAnnotation annotation, XSParticle elementParticle, Collection<? extends DisplayableValue<T>> allowedValues, T defaultValue) throws SchemaException {
        var definedInType = complexTypeDefinition != null ? complexTypeDefinition.getTypeName() : null;
        return new PrismPropertyDefinitionImpl<>(elementName, typeName, allowedValues, defaultValue, definedInType);
    }

    public PrismReferenceDefinition createReferenceDefinition(QName primaryElementName, QName typeName, ComplexTypeDefinition complexTypeDefinition,
            PrismContext prismContext, XSAnnotation annotation, XSParticle elementParticle) throws SchemaException {
        var definedInType = complexTypeDefinition != null ? complexTypeDefinition.getTypeName() : null;
        return new PrismReferenceDefinitionImpl(primaryElementName, typeName, definedInType);
    }

    public <C extends Containerable> PrismContainerDefinitionImpl<C> createContainerDefinition(
            QName elementName, ComplexTypeDefinition complexTypeDefinition, Class<C> compileTimeClass, QName definedInType)
            throws SchemaException {
        return new PrismContainerDefinitionImpl<>(elementName, complexTypeDefinition, compileTimeClass, definedInType);
    }

    public <T extends Objectable> PrismObjectDefinitionImpl<T> createObjectDefinition(
            QName elementName, ComplexTypeDefinition complexTypeDefinition, Class<T> compileTimeClass)
            throws SchemaException {
        return new PrismObjectDefinitionImpl<>(elementName, complexTypeDefinition, compileTimeClass);
    }

    /**
     * Create optional extra definition form a top-level complex type definition.
     * This is used e.g. to create object class definitions in midPoint
     */
    public <C extends Containerable> PrismContainerDefinition<C> createExtraDefinitionFromComplexType(XSComplexType complexType,
            ComplexTypeDefinition complexTypeDefinition, PrismContext prismContext,
            XSAnnotation annotation) throws SchemaException {
        // Create nothing by default
        return null;
    }

    /**
     * Called after the complex type definition is filled with items. It may be used to finish building
     * the definition, e.g. by adding data that depends on existing internal definitions.
     */
    public void finishComplexTypeDefinition(ComplexTypeDefinition complexTypeDefinition, XSComplexType complexType,
            PrismContext prismContext, XSAnnotation annotation) throws SchemaException {
        // Nothing to do by default
    }

    /**
     * Add extra annotations to a complexType DOM model. Used when serializing schema to DOM.
     */
    public void addExtraComplexTypeAnnotations(ComplexTypeDefinition definition, Element appinfo, SchemaToDomProcessor schemaToDomProcessor) {
        // Nothing to do by default
    }

    /**
     * Add extra annotations to a property DOM model. Used when serializing schema to DOM.
     */
    public void addExtraPropertyAnnotations(
            PrismPropertyDefinition<?> definition, Element appinfo, SchemaToDomProcessor schemaToDomProcessor) {
        // Nothing to do by default
    }

    /**
     * Add extra annotations to a reference DOM model. Used when serializing schema to DOM.
     */
    public void addExtraReferenceAnnotations(PrismReferenceDefinition definition, Element appinfo, SchemaToDomProcessor schemaToDomProcessor) {
        // Nothing to do by default
    }

    public SimpleTypeDefinitionImpl createEnumerationTypeDefinition(XSSimpleType simpleType, PrismContext prismContext,
            XSAnnotation annotation) {
        QName typeName = new QName(simpleType.getTargetNamespace(), simpleType.getName());
        XSType baseType = simpleType.getBaseType();
        QName baseTypeName = baseType != null ? new QName(baseType.getTargetNamespace(), baseType.getName()) : null;

        List<EnumerationTypeDefinition.ValueDefinition> values = createEnumerationValues(simpleType, typeName);
        return new EnumerationTypeDefinitionImpl(typeName, baseTypeName, values);
    }

    private List<ValueDefinition> createEnumerationValues(XSSimpleType simpleType, QName typeName) {
        if (simpleType instanceof XSRestrictionSimpleType) {
            Builder<ValueDefinition> ret = ImmutableList.<ValueDefinition>builder();
            Collection<? extends XSFacet> facets = ((XSRestrictionSimpleType) simpleType).getDeclaredFacets();
            for (XSFacet facet : facets) {
                ret.add(createEnumerationValue(facet, typeName));

            }
            return ret.build();
        }
        throw new IllegalArgumentException("Type must be instance of XSRestrictionSimpleType not " + simpleType);
    }

    private ValueDefinition createEnumerationValue(XSFacet facet, QName typeName) {
        if (!ENUMERATION_FACET.equals(facet.getName())) {
            throw new IllegalArgumentException("Only restriction facets are supported in type " + typeName);
        }

        String value = facet.getValue().toString();
        String documentationText = null;
        String constantName = null;
        XSAnnotation annotation = facet.getAnnotation();
        if (annotation != null) {
            Element documentationElement = SchemaProcessorUtil.getAnnotationElement(annotation, DOMUtil.XSD_DOCUMENTATION_ELEMENT);
            if (documentationElement != null) {
                documentationText = DOMUtil.serializeElementContent(documentationElement);
            }
            Element appInfo = SchemaProcessorUtil.getAnnotationElement(annotation, DOMUtil.XSD_APPINFO_ELEMENT);
            if (appInfo != null) {
                Element typeSetEnum = DOMUtil.getChildElement(appInfo, TYPESAFE_ENUM_MEMBER);
                if (typeSetEnum != null) {
                    constantName = typeSetEnum.getAttribute("name");
                    // Make sure to have NULL in constant name is string is blank
                    constantName = constantName.isBlank() ? null : constantName;
                }
            }
        }
        return new EnumerationTypeDefinitionImpl.ValueDefinitionImpl(value, documentationText, constantName);
    }

}
