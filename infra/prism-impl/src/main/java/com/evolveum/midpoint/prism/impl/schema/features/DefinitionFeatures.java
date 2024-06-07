/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.schema.features;

import static com.evolveum.midpoint.prism.PrismConstants.*;
import static com.evolveum.midpoint.prism.impl.schema.SchemaProcessorUtil.*;
import static com.evolveum.midpoint.prism.impl.schema.SchemaProcessorUtil.getAnnotationElement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.*;

import com.evolveum.midpoint.prism.ComplexTypeDefinition.ComplexTypeDefinitionLikeBuilder;

import com.evolveum.midpoint.prism.PrismReferenceDefinition.PrismReferenceDefinitionMutator;
import com.evolveum.midpoint.prism.TypeDefinition.TypeDefinitionLikeBuilder;
import com.evolveum.midpoint.prism.impl.schema.features.ItemDiagramSpecificationXsomParser.ItemDiagramSpecifications;

import com.evolveum.midpoint.prism.impl.PrismReferenceValueImpl;
import com.evolveum.midpoint.prism.impl.schema.SchemaProcessorUtil;
import com.evolveum.midpoint.prism.impl.schema.annotation.AlwaysUseForEquals;
import com.evolveum.midpoint.prism.impl.schema.features.SchemaMigrationXsomParser.SchemaMigrations;
import com.evolveum.midpoint.prism.impl.schemaContext.SchemaContextDefinitionImpl;
import com.evolveum.midpoint.prism.schema.*;
import com.evolveum.midpoint.prism.schemaContext.SchemaContextDefinition;
import com.evolveum.midpoint.prism.xml.XmlTypeConverter;

import com.evolveum.midpoint.util.MiscUtil;

import com.sun.xml.xsom.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.exception.SchemaException;

/** A collection of definition features known in prism. It may be converted to an enum eventually. */
public class DefinitionFeatures {

    private static final String ANY_TYPE = "anyType";

    public static final DefinitionFeature<QName, TypeDefinitionLikeBuilder, XSType, ?> DF_SUPERTYPE =
            DefinitionFeature.of(
                    QName.class,
                    TypeDefinitionLikeBuilder.class,
                    TypeDefinitionLikeBuilder::setSuperType,
                    XsomParsers.DF_SUPERTYPE_PARSER);

    public static final DefinitionFeature<Integer, TypeDefinitionLikeBuilder, XSType, ?> DF_INSTANTIATION_ORDER =
            DefinitionFeature.of(
                    Integer.class,
                    TypeDefinitionLikeBuilder.class,
                    TypeDefinitionLikeBuilder::setInstantiationOrder,
                    XsomParsers.singleValue(Integer.class, A_INSTANTIATION_ORDER));

    public static final DefinitionFeature<QName, ComplexTypeDefinitionLikeBuilder, XSType, ?> DF_DEFAULT_ITEM_TYPE_NAME =
            DefinitionFeature.of(
                    QName.class,
                    ComplexTypeDefinitionLikeBuilder.class,
                    ComplexTypeDefinitionLikeBuilder::setDefaultItemTypeName,
                    XsomParsers.qNameInherited(A_DEFAULT_ITEM_TYPE_NAME));

    public static final DefinitionFeature<String, ComplexTypeDefinitionLikeBuilder, XSType, ?> DF_DEFAULT_NAMESPACE =
            DefinitionFeature.of(
                    String.class,
                    ComplexTypeDefinitionLikeBuilder.class,
                    ComplexTypeDefinitionLikeBuilder::setDefaultNamespace,
                    XsomParsers.singleValueInherited(String.class, A_DEFAULT_NAMESPACE));

    public static final DefinitionFeature<IgnoredNamespaces, ComplexTypeDefinitionLikeBuilder, XSType, ?> DF_IGNORED_NAMESPACES =
            DefinitionFeature.of(
                    IgnoredNamespaces.class,
                    ComplexTypeDefinitionLikeBuilder.class,
                    (ctdBuilder, ignoredNamespaces) -> ctdBuilder.setIgnoredNamespaces(IgnoredNamespaces.unwrap(ignoredNamespaces)),
                    new DefinitionFeatureParser<>() {
                        @Override
                        public @Nullable IgnoredNamespaces getValue(@Nullable XSType xsType) {
                            List<String> namespaces = new ArrayList<>();
                            collectNamespaces(namespaces, xsType);
                            return IgnoredNamespaces.wrap(namespaces);
                        }

                        private void collectNamespaces(List<String> namespaces, XSType xsType) {
                            List<Element> annoElements = getAnnotationElements(xsType, A_IGNORED_NAMESPACE);
                            for (Element annoElement : annoElements) {
                                namespaces.add(annoElement.getTextContent());
                            }
                            if (xsType.getBaseType() != null && !xsType.getBaseType().equals(xsType)) {
                                collectNamespaces(namespaces, xsType.getBaseType());
                            }
                        }

                        @Override
                        public boolean applicableTo(Object sourceComponent) {
                            return sourceComponent instanceof XSType;
                        }
                    });

    public static final DefinitionFeature<IsAnyXsomParser.IsAny, ComplexTypeDefinitionLikeBuilder, XSType, ?> DF_IS_ANY_XSD =
            DefinitionFeature.of(
                    IsAnyXsomParser.IsAny.class,
                    ComplexTypeDefinitionLikeBuilder.class,
                    (ctdBuilder, value) -> {
                        if (value != null) {
                            ctdBuilder.setXsdAnyMarker(value.any());
                            ctdBuilder.setStrictAnyMarker(value.anyStrict());
                        }
                    },
                    XsomParsers.DF_IS_ANY_XSD_PARSER);

    public static final DefinitionFeature<String, PrismPresentationDefinition.Mutable, Object, ?> DF_DOCUMENTATION =
            DefinitionFeature.of(
                    String.class,
                    PrismPresentationDefinition.Mutable.class,
                    PrismPresentationDefinition.Mutable::setDocumentation,
                    XsomParsers.DF_DOCUMENTATION_PARSER);

    public static final DefinitionFeature<QName, ComplexTypeDefinitionLikeBuilder, XSType, ?> DF_EXTENSION_REF =
            DefinitionFeature.of(
                    QName.class,
                    ComplexTypeDefinitionLikeBuilder.class,
                    ComplexTypeDefinitionLikeBuilder::setExtensionForType,
                    XsomParsers.DF_EXTENSION_REF_PARSER,
                    SerializableComplexTypeDefinition.class,
                    SerializableComplexTypeDefinition::getExtensionForType,
                    XsdSerializers.qNameRef(A_EXTENSION));

    public static final DefinitionFeature<Boolean, ComplexTypeDefinitionLikeBuilder, Object, ?> DF_CONTAINER_MARKER =
            DefinitionFeature.of(
                    Boolean.class,
                    ComplexTypeDefinitionLikeBuilder.class,
                    ComplexTypeDefinitionLikeBuilder::setContainerMarker,
                    XsomParsers.DF_CONTAINER_MARKER_PROCESSOR);

    public static final DefinitionFeature<Boolean, ComplexTypeDefinitionLikeBuilder, Object, ?> DF_OBJECT_MARKER =
            DefinitionFeature.of(
                    Boolean.class,
                    ComplexTypeDefinitionLikeBuilder.class,
                    ComplexTypeDefinitionLikeBuilder::setObjectMarker,
                    XsomParsers.DF_OBJECT_MARKER_PROCESSOR);

    public static final DefinitionFeature<Boolean, ComplexTypeDefinitionLikeBuilder, Object, ?> DF_REFERENCE_MARKER =
            DefinitionFeature.of(
                    Boolean.class,
                    ComplexTypeDefinitionLikeBuilder.class,
                    ComplexTypeDefinitionLikeBuilder::setReferenceMarker,
                    XsomParsers.DF_REFERENCE_MARKER_PROCESSOR);

    public static final DefinitionFeature<Boolean, ComplexTypeDefinitionLikeBuilder, XSComplexType, ?> DF_LIST_MARKER =
            DefinitionFeature.of(
                    Boolean.class,
                    ComplexTypeDefinitionLikeBuilder.class,
                    ComplexTypeDefinitionLikeBuilder::setListMarker,
                    new DefinitionFeatureParser<>() {
                        @Override
                        public @NotNull Boolean getValue(@Nullable XSComplexType complexType) {
                            var attributeUses = complexType != null ? complexType.getAttributeUses() : null;
                            return attributeUses != null && attributeUses.stream()
                                    .anyMatch(au ->
                                            au.getDecl() != null
                                                    && DOMUtil.IS_LIST_ATTRIBUTE_NAME.equals(au.getDecl().getName()));
                        }
                    });

    public static final DefinitionFeature<Boolean, PrismReferenceDefinitionMutator, Object, ?> DF_COMPOSITE_MARKER =
            DefinitionFeature.of(
                    Boolean.class,
                    PrismReferenceDefinitionMutator.class,
                    PrismReferenceDefinitionMutator::setComposite,
                    XsomParsers.DF_COMPOSITE_MARKER_PROCESSOR);

    public static final DefinitionFeature<SchemaMigrations, PrismLifecycleDefinition.Mutable, Object, ?> DF_SCHEMA_MIGRATIONS =
            DefinitionFeature.of(
                    SchemaMigrations.class,
                    PrismLifecycleDefinition.Mutable.class,
                    (target, value) -> target.setSchemaMigrations(SchemaMigrations.unwrap(value)),
                    SchemaMigrationXsomParser.instance());

    public static final DefinitionFeature<ItemDiagramSpecifications, PrismPresentationDefinition.Mutable, Object, ?> DF_DIAGRAMS =
            DefinitionFeature.of(
                    ItemDiagramSpecifications.class,
                    PrismPresentationDefinition.Mutable.class,
                    (target, value) -> target.setDiagrams(ItemDiagramSpecifications.unwrap(value)),
                    ItemDiagramSpecificationXsomParser.instance());

    public static final DefinitionFeature<PrismItemAccessDefinition.Info, PrismItemAccessDefinition.Mutable, XSAnnotation, ?> DF_ACCESS =
            DefinitionFeature.of(
                    PrismItemAccessDefinition.Info.class,
                    PrismItemAccessDefinition.Mutable.class,
                    PrismItemAccessDefinition.Mutable::setInfo,
                    XsomParsers.DF_ACCESS_PROCESSOR,
                    PrismItemAccessDefinition.class,
                    PrismItemAccessDefinition::getInfo,
                    XsomParsers.DF_ACCESS_PROCESSOR);

    public static final DefinitionFeature<Boolean, PrismItemStorageDefinition.Mutable, Object, ?> DF_INDEXED =
            DefinitionFeature.of(
                    Boolean.class,
                    PrismItemStorageDefinition.Mutable.class,
                    PrismItemStorageDefinition.Mutable::setIndexed,
                    XsomParsers.marker(A_INDEXED));

    public static final DefinitionFeature<Boolean, PrismItemStorageDefinition.Mutable, Object, ?> DF_INDEX_ONLY =
            DefinitionFeature.of(
                    Boolean.class,
                    PrismItemStorageDefinition.Mutable.class,
                    PrismItemStorageDefinition.Mutable::setIndexOnly,
                    XsomParsers.marker(A_INDEX_ONLY));

    public static final DefinitionFeature<QName, PrismItemMatchingDefinition.Mutator, XSAnnotation, ?> DF_MATCHING_RULE =
            DefinitionFeature.of(
                    QName.class,
                    PrismItemMatchingDefinition.Mutator.class,
                    PrismItemMatchingDefinition.Mutator::setMatchingRuleQName,
                    XsomParsers.singleAnnotationValue(QName.class, A_MATCHING_RULE));

    @SuppressWarnings("rawtypes") // TODO how to fix this?
    public static final DefinitionFeature<PrismReferenceValue, PrismItemValuesDefinition.Mutator, XSAnnotation, ?> DF_VALUE_ENUMERATION_REF =
            DefinitionFeature.of(
                    PrismReferenceValue.class,
                    PrismItemValuesDefinition.Mutator.class,
                    PrismItemValuesDefinition.Mutator::setValueEnumerationRef,
                    annotation -> {
                        Element element = getAnnotationElement(annotation, A_VALUE_ENUMERATION_REF);
                        if (element == null) {
                            return null;
                        }
                        String oid = MiscUtil.requireNonNull(
                                element.getAttribute(ATTRIBUTE_OID_LOCAL_NAME),
                                "No OID in valueEnumerationRef annotation");
                        QName targetType = DOMUtil.getQNameAttribute(element, ATTRIBUTE_REF_TYPE_LOCAL_NAME);
                        return new PrismReferenceValueImpl(oid, targetType);
                    });

    public static class XsomParsers {

        public static final IsAnyXsomParser DF_IS_ANY_XSD_PARSER = IsAnyXsomParser.instance();

        /** Provides the name of XSD supertype (base type). */
        public static final DefinitionFeatureParser<QName, XSType> DF_SUPERTYPE_PARSER =
                new DefinitionFeatureParser<>() {
                    @Override
                    public @Nullable QName getValue(@Nullable XSType xsType) {
                        XSType baseType = xsType != null ? xsType.getBaseType() : null;
                        if (baseType != null && !baseType.getName().equals(ANY_TYPE)) {
                            return new QName(baseType.getTargetNamespace(), baseType.getName());
                        } else {
                            return null;
                        }
                    }
                };

        public static final DefinitionFeatureParser<QName, XSType> DF_EXTENSION_REF_PARSER =
                new DefinitionFeatureParser<>() {
                    @Override
                    public @Nullable QName getValue(@Nullable XSType sourceComplexType) throws SchemaException {
                        Element extensionAnnotationElement = getAnnotationElement(sourceComplexType, A_EXTENSION);
                        if (extensionAnnotationElement == null) {
                            return null;
                        }
                        QName extendedTypeName = DOMUtil.getQNameAttribute(extensionAnnotationElement, A_REF.getLocalPart());
                        if (extendedTypeName != null) {
                            return extendedTypeName;
                        }
                        throw new SchemaException(
                                "The %s annotation on %s complex type (%s) does not have %s attribute".formatted(
                                        A_EXTENSION, sourceComplexType.getName(), sourceComplexType.getTargetNamespace(),
                                        A_REF.getLocalPart()),
                                A_REF);
                    }
                };
        public static final DefinitionFeatureParser.Marker<Object> DF_CONTAINER_MARKER_PROCESSOR = markerInherited(A_CONTAINER);
        public static final DefinitionFeatureParser.Marker<Object> DF_OBJECT_MARKER_PROCESSOR = markerInherited(A_OBJECT);
        public static final DefinitionFeatureParser.Marker<Object> DF_REFERENCE_MARKER_PROCESSOR = markerInherited(A_OBJECT_REFERENCE);
        public static final DefinitionFeatureParser.Marker<Object> DF_EMBEDDED_OBJECT_MARKER_PROCESSOR = marker(A_EMBEDDED_OBJECT);
        public static final DefinitionFeatureParser.Marker<Object> DF_COMPOSITE_MARKER_PROCESSOR = marker(A_COMPOSITE);
        public static final DefinitionFeatureParser.Marker<Object> DF_DEPRECATED = marker(A_DEPRECATED);
        public static final EnumerationValuesXsomParser DF_ENUMERATION_VALUES_PARSER = new EnumerationValuesXsomParser();
        public static final EnumerationValuesInfoXsomParser DF_ENUMERATION_VALUES_INFO_PROCESSOR = new EnumerationValuesInfoXsomParser();
        public static final AccessXsomProcessor DF_ACCESS_PROCESSOR = new AccessXsomProcessor();
        public static final DefinitionFeatureParser<QName, Object> DF_TYPE_OVERRIDE_PROCESSOR = qName(A_TYPE);

        public static final DefinitionFeatureParser<DisplayHint, XSAnnotation> DF_DISPLAY_HINT_PARSER = new DefinitionFeatureParser<>() {
            @Override
            public @Nullable DisplayHint getValue(@Nullable XSAnnotation annotation) {
                Element element = getAnnotationElement(annotation, A_DISPLAY_HINT);
                if (element == null) {
                    return null;
                }
                return DisplayHint.findByValue(element.getTextContent());
            }
        };

        public static final DefinitionFeatureParser<String, Object> DF_DOCUMENTATION_PARSER = new DefinitionFeatureParser<>() {
            @Override
            public @Nullable String getValue(@Nullable Object sourceObject) {
                Element documentationElement = getAnnotationElement(sourceObject, DOMUtil.XSD_DOCUMENTATION_ELEMENT);
                if (documentationElement != null) {
                    // The documentation may be HTML-formatted. Therefore we want to keep the formatting and tag names.
                    return DOMUtil.serializeElementContent(documentationElement);
                } else {
                    return null;
                }
            }
        };
        public static final DefinitionFeatureParser<AlwaysUseForEquals, XSAnnotation> DF_ALWAYS_USE_FOR_EQUALS_PARSER = new DefinitionFeatureParser<>() {
            @Override
            public @Nullable AlwaysUseForEquals getValue(@Nullable XSAnnotation annotation) {
                return AlwaysUseForEquals.parse(
                        getAnnotationElements(annotation, A_ALWAYS_USE_FOR_EQUALS));
            }
        };

        @SuppressWarnings("SameParameterValue")
        public static <T> DefinitionFeatureParser<T, XSType> singleValue(@NotNull Class<T> valueClass, @NotNull QName name) {
            return new DefinitionFeatureParser<>() {
                @Override
                public @Nullable T getValue(@Nullable XSType source) throws SchemaException {
                    return getAnnotationConverted(source, name, valueClass);
                }
            };
        }

        @SuppressWarnings("SameParameterValue")
        public static DefinitionFeatureParser<String, Object> string(@NotNull QName name) {
            return new DefinitionFeatureParser<>() {
                @Override
                public @Nullable String getValue(@Nullable Object source) throws SchemaException {
                    return getAnnotationConverted(source, name, String.class);
                }
            };
        }

        public static <E extends Enum<E>> DefinitionFeatureParser<E, Object> enumBased(
                Class<E> valueClass, @NotNull QName name, @NotNull Function<E, String> valueExtractor) {
            return new DefinitionFeatureParser<>() {
                @Override
                public @Nullable E getValue(@Nullable Object source) throws SchemaException {
                    var element = getAnnotationElement(source, name);
                    if (element == null) {
                        return null;
                    }
                    String textContent = element.getTextContent();
                    if (textContent == null || textContent.isEmpty()) {
                        throw new SchemaException("Empty value for %s annotation".formatted(name));
                    }
                    for (E e : valueClass.getEnumConstants()) {
                        if (valueExtractor.apply(e).equals(textContent)) {
                            return e;
                        }
                    }
                    throw new SchemaException("Unknown value for %s annotation: %s".formatted(name, textContent));
                }
            };
        }

        @SuppressWarnings("SameParameterValue")
        public static <T> DefinitionFeatureParser<T, XSAnnotation> singleAnnotationValue(@NotNull Class<T> valueClass, @NotNull QName name) {
            return new DefinitionFeatureParser<>() {
                @Override
                public @Nullable T getValue(@Nullable XSAnnotation annotation) throws SchemaException {
                    return getAnnotationConverted(annotation, name, valueClass);
                }
            };
        }

        @SuppressWarnings("SameParameterValue")
        public static <T> DefinitionFeatureParser<T, XSType> singleValueInherited(@NotNull Class<T> valueClass, @NotNull QName name) {
            return new DefinitionFeatureParser<>() {
                @Override
                public @Nullable T getValue(@Nullable XSType sourceComponent) throws SchemaException {
                    return getAnnotationConvertedInherited(sourceComponent, name, valueClass);
                }

                @Override
                public boolean applicableTo(Object sourceComponent) {
                    return sourceComponent instanceof XSType;
                }
            };
        }

        /** TODO. */
        @SuppressWarnings("SameParameterValue")
        public static DefinitionFeatureParser.Marker<Object> marker(@NotNull QName name) {
            return new DefinitionFeatureParser.Marker<>() {
                @Override
                public @Nullable Boolean getValue(@Nullable Object sourceObject) throws SchemaException {
                    return parseMarkerElement(
                            getAnnotationElement(sourceObject, name));
                }
            };
        }

        public static DefinitionFeatureParser.Marker<XSComplexType> markerForComplexType(@NotNull QName name) {
            return new DefinitionFeatureParser.Marker<>() {
                @Override
                public @Nullable Boolean getValue(@Nullable XSComplexType sourceObject) throws SchemaException {
                    return parseMarkerElement(
                            getAnnotationElement(sourceObject, name));
                }
            };
        }

        public static DefinitionFeatureParser.Marker<Object> markerInherited(@NotNull QName name) {
            return new DefinitionFeatureParser.Marker<>() {
                @Override
                public @Nullable Boolean getValue(@Nullable Object sourceObject) throws SchemaException {
                    Element element;
                    if (sourceObject instanceof XSType type) {
                        element = getAnnotationElementCheckingAncestors(type, name);
                    } else {
                        element = getAnnotationElement(sourceObject, name);
                    }
                    return parseMarkerElement(element);
                }
            };
        }

        private static Boolean parseMarkerElement(Element element) throws SchemaException {
            if (element == null) {
                return null;
            }
            String textContent = element.getTextContent();
            if (textContent != null && !textContent.isEmpty()) {
                return XmlTypeConverter.toJavaValue(element, Boolean.class);
            } else {
                return true;
            }
        }

        @SuppressWarnings("SameParameterValue")
        public static DefinitionFeatureParser<QName, XSType> qNameInherited(@NotNull QName name) {
            return new DefinitionFeatureParser<>() {
                @Override
                public @Nullable QName getValue(@Nullable XSType xsType) {
                    var element = getAnnotationElementCheckingAncestors(xsType, name);
                    return element != null ? DOMUtil.getQNameValue(element) : null;
                }
            };
        }

        @SuppressWarnings("SameParameterValue")
        public static DefinitionFeatureParser<QName, Object> qName(@NotNull QName name) {
            return new DefinitionFeatureParser<>() {
                @Override
                public @Nullable QName getValue(@Nullable Object annotation) {
                    return SchemaProcessorUtil.getAnnotationQName(annotation, name);
                }
            };
        }

        @SuppressWarnings("SameParameterValue")
        public static DefinitionFeatureParser<QNameList, Object> qNameList(@NotNull QName name) {
            return new DefinitionFeatureParser<>() {
                @Override
                public @Nullable QNameList getValue(@Nullable Object annotation) {
                    return QNameList.wrap(
                            SchemaProcessorUtil.getAnnotationQNameList(annotation, name));
                }
            };
        }

        public static DefinitionFeatureParser<SchemaContextDefinition, XSAnnotation> schemaContextDefinitionParser() {
            return new DefinitionFeatureParser<>() {
                @Override
                public @Nullable SchemaContextDefinition getValue(@Nullable XSAnnotation annotation) {
                    if (getAnnotationElement(annotation, A_SCHEMA_CONTEXT) != null) {
                        SchemaContextDefinition schemaContextDefinition = new SchemaContextDefinitionImpl();
                        Element typeElement = getAnnotationElement(annotation, A_TYPE);
                        Element typePathElement = getAnnotationElement(annotation, A_TYPE_PATH);
                        Element algorithmElement = getAnnotationElement(annotation, A_ALGORITHM);

                        if (typeElement != null) {
                            schemaContextDefinition.setType(new QName(typeElement.getTextContent()));
                        }

                        if (typePathElement != null) {
                            schemaContextDefinition.setTypePath(new QName(typePathElement.getTextContent()));
                        }

                        if (algorithmElement != null) {
                            schemaContextDefinition.setAlgorithm(new QName(algorithmElement.getTextContent()));
                        }

                        return schemaContextDefinition;
                    }

                    return null;
                }
            };
        }
    }

    public static class XsdSerializers {

        public static @NotNull DefinitionFeatureSerializer<String> string(@NotNull QName annotationName) {
            return (value, target) -> target.addAnnotation(annotationName, value);
        }

        public static @NotNull DefinitionFeatureSerializer<Boolean> aBoolean(@NotNull QName annotationName) {
            return (value, target) -> target.addAnnotation(annotationName, value);
        }

        public static @NotNull DefinitionFeatureSerializer<QName> qName(@NotNull QName annotationName) {
            return (value, target) -> target.addAnnotation(annotationName, value);
        }

        /** This is perhaps a legacy format, abandoned for several uses, but kept e.g. for "extension for type". */
        public static @NotNull DefinitionFeatureSerializer<QName> qNameRef(@NotNull QName annotationName) {
            return (value, target) -> target.addRefAnnotation(annotationName, value);
        }

        public static <E extends Enum<E>> @NotNull DefinitionFeatureSerializer<E> enumBased(
                Class<E> valueClass, @NotNull QName annotationName, @NotNull Function<E, String> valueExtractor) {
            return (value, target) -> target.addAnnotation(annotationName, valueExtractor.apply(value));
        }
    }
}
