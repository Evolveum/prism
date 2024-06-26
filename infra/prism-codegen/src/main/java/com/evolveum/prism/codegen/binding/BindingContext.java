/*
 * Copyright (c) 2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.codegen.binding;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.SourceVersion;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.ComplexTypeDefinition;
import com.evolveum.midpoint.prism.EnumerationTypeDefinition;
import com.evolveum.midpoint.prism.PrismConstants;
import com.evolveum.midpoint.prism.PrismObjectDefinition;
import com.evolveum.midpoint.prism.TypeDefinition;
import com.evolveum.midpoint.prism.impl.binding.AbstractMutableObjectable;
import com.evolveum.midpoint.prism.schema.PrismSchema;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.prism.codegen.binding.TypeBinding.Static;
import com.evolveum.prism.xml.ns._public.query_3.OrderDirectionType;
import com.evolveum.prism.xml.ns._public.query_3.PagingType;
import com.evolveum.prism.xml.ns._public.query_3.QueryType;
import com.evolveum.prism.xml.ns._public.query_3.SearchFilterType;
import com.evolveum.prism.xml.ns._public.types_3.ChangeTypeType;
import com.evolveum.prism.xml.ns._public.types_3.DeltaSetTripleType;
import com.evolveum.prism.xml.ns._public.types_3.EvaluationTimeType;
import com.evolveum.prism.xml.ns._public.types_3.ItemDeltaItemType;
import com.evolveum.prism.xml.ns._public.types_3.ItemDeltaType;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;
import com.evolveum.prism.xml.ns._public.types_3.ItemType;
import com.evolveum.prism.xml.ns._public.types_3.ObjectDeltaType;
import com.evolveum.prism.xml.ns._public.types_3.ObjectType;
import com.evolveum.prism.xml.ns._public.types_3.PlusMinusZeroType;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringNormalizerConfigurationType;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;
import com.evolveum.prism.xml.ns._public.types_3.ProtectedStringType;
import com.evolveum.prism.xml.ns._public.types_3.ReferentialIntegrityType;
import com.evolveum.prism.xml.ns._public.types_3.SchemaDefinitionType;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class BindingContext {

    // FIXME: This should be probably package specific
    public static final String SCHEMA_CONSTANTS_GENERATED_CLASS_NAME = "com.evolveum.midpoint.schema.SchemaConstantsGenerated";

    public static final String TYPE_CONSTANT = "COMPLEX_TYPE";

    public static final long SERIAL_VERSION_UID = 201105211233L;

    Map<String, PrismSchema> schemas = new HashMap<>();
    Map<QName, TypeBinding> bindings = new HashMap<>();

    Set<Binding> staticBindings = new HashSet<>();
    Set<Binding> derivedBindings = new HashSet<>();


    BiMap<String, String> xmlToJavaNs = HashBiMap.create();

    private ConstantBinding constants;


    public BindingContext() {
        staticBinding(DOMUtil.XSD_STRING, String.class);
        staticBinding(DOMUtil.XSD_INT, Integer.class);
        staticBinding(DOMUtil.XSD_INTEGER, BigInteger.class);
        staticBinding(DOMUtil.XSD_DECIMAL, BigDecimal.class);
        staticBinding(DOMUtil.XSD_DOUBLE, Double.class);
        staticBinding(DOMUtil.XSD_FLOAT, Float.class);
        staticBinding(DOMUtil.XSD_LONG, Long.class);
        staticBinding(DOMUtil.XSD_SHORT, Short.class);
        staticBinding(DOMUtil.XSD_BYTE, Byte.class);
        staticBinding(DOMUtil.XSD_BOOLEAN, Boolean.class);
        staticBinding(DOMUtil.XSD_BASE64BINARY, byte[].class);
        staticBinding(DOMUtil.XSD_DATETIME, XMLGregorianCalendar.class);
        staticBinding(DOMUtil.XSD_DURATION, Duration.class);
        staticBinding(DOMUtil.XSD_QNAME, QName.class);

        staticBinding(ItemPathType.COMPLEX_TYPE, ItemPathType.class);

        staticBinding(PrismConstants.POLYSTRING_TYPE_QNAME, PolyStringType.class);

        staticBinding(DOMUtil.XSD_ANYURI, String.class);
        staticBinding(DOMUtil.XSD_ANYTYPE, Object.class);
        staticBinding(DOMUtil.XSD_ANY, Object.class);


        staticBinding(QueryType.COMPLEX_TYPE, QueryType.class);
        staticBinding(SearchFilterType.COMPLEX_TYPE, SearchFilterType.class);
        staticBinding(ItemDeltaType.COMPLEX_TYPE, ItemDeltaType.class);
        staticBinding(ObjectDeltaType.COMPLEX_TYPE, ObjectDeltaType.class);
        staticBinding(ProtectedStringType.COMPLEX_TYPE, ProtectedStringType.class);
        staticBinding(typesNs(ChangeTypeType.class), ChangeTypeType.class);
        staticBinding(typesNs(EvaluationTimeType.class), EvaluationTimeType.class);
        staticBinding(typesNs(ReferentialIntegrityType.class), ReferentialIntegrityType.class);
        staticBinding(PagingType.COMPLEX_TYPE, PagingType.class);
        staticBinding(PolyStringNormalizerConfigurationType.COMPLEX_TYPE, PolyStringNormalizerConfigurationType.class);
        staticBinding(DeltaSetTripleType.COMPLEX_TYPE, DeltaSetTripleType.class);
        staticBinding(ItemDeltaItemType.COMPLEX_TYPE, ItemDeltaItemType.class);
        staticBinding(typesNs(PlusMinusZeroType.class), PlusMinusZeroType.class);
        staticBinding(SchemaDefinitionType.COMPLEX_TYPE, SchemaDefinitionType.class);
        staticBinding(ItemType.COMPLEX_TYPE, ItemType.class);
        staticBinding(queryNs(OrderDirectionType.class), OrderDirectionType.class);

        staticBinding(typesNs(ObjectType.class), AbstractMutableObjectable.class);

        constants = new ConstantBinding();
    }

    private static QName typesNs(Class<?> clazz) {
        return new QName(PrismConstants.NS_TYPES, clazz.getSimpleName());
    }

    private static QName queryNs(Class<?> clazz) {
        return new QName(PrismConstants.NS_QUERY, clazz.getSimpleName());
    }



    private void staticBinding(QName name, Class<?> javaClass) {
        TypeBinding binding = new TypeBinding.Static(name, javaClass);
        bindings.put(name, binding);
    }


    public void addSchemas(Collection<PrismSchema> schemas) {
        for (PrismSchema schema: schemas) {
            this.schemas.put(schema.getNamespace(), schema);
        }
    }

    public BindingContext process() {

        for (PrismSchema schema : schemas.values()) {
            process(schema);
        }
        if (!constants.isEmpty()) {
            derivedBindings.add(constants);
        }
        return this;
    }

    public void addNamespaceMapping(String xmlNs, String javaNs) {
        xmlToJavaNs.put(xmlNs, verifyJavaPackageName(javaNs));
    }

    private @Nullable String verifyJavaPackageName(String javaNs) {
        // Verify if this is proper package name
        return javaNs;
    }

    @VisibleForTesting
    void process(PrismSchema schema) {

        createBinding(schema);

        for (TypeDefinition typeDef : schema.getDefinitions(TypeDefinition.class)) {
            if (!isSchemaNative(schema, typeDef)) {
                continue;
            }
            if (bindings.containsKey(typeDef.getTypeName())) {
                // Type was probably staticly mapped
                continue;
            }

            TypeBinding binding = createBinding(typeDef);
            TypeBinding previous = bindings.put(typeDef.getTypeName(), binding);
            if (previous != null) {
                throw new IllegalStateException("Binding " + binding + "mapped twice");
            }
        }
    }

    private SchemaBinding createBinding(PrismSchema schema) {
        String packageName = resolvePackageName(schema.getNamespace());
        var schemaBinding = new SchemaBinding(schema.getNamespace(), packageName);
        // FIXME: Detect if binding exists staticly
        derivedBindings.add(schemaBinding);
        ObjectFactoryContract contract = new ObjectFactoryContract(schemaBinding, schema, constants.get(schema.getNamespace()));
        schemaBinding.defaultContract(contract);
        constants.put(schema.getNamespace(), contract);
        return schemaBinding;
    }

    private boolean isSchemaNative(PrismSchema schema, TypeDefinition typeDef) {
        return schema.getNamespace().equals(typeDef.getTypeName().getNamespaceURI());
    }


    @VisibleForTesting
    TypeBinding createBinding(TypeDefinition typeDef) {
        Class<?> existingClass = resolvePotentialStaticBinding(typeDef);
        TypeBinding binding = existingClass != null ?  new TypeBinding.Static(typeDef.getTypeName(), existingClass) : new TypeBinding.Derived(typeDef.getTypeName());

        String packageName;
        if (binding instanceof Static) {
            assert existingClass != null;
            staticBindings.add(binding);
            packageName = existingClass.getPackageName();
        } else {
            derivedBindings.add(binding);
            packageName = resolvePackageName(typeDef.getTypeName().getNamespaceURI());

        }
        if (typeDef instanceof ComplexTypeDefinition) {
            return createFromComplexType(binding, (ComplexTypeDefinition) typeDef, packageName);
        } else if (typeDef instanceof EnumerationTypeDefinition) {
            binding.defaultContract(new EnumerationContract((EnumerationTypeDefinition) typeDef, packageName));
        }
        return binding;
    }

    private String resolvePackageName(String namespaceURI) {
        return xmlToJavaNs.computeIfAbsent(namespaceURI, n -> {
            try {
                return namespaceToPackageName(n);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(namespaceURI + "is not valid URI");
            }
        });
    }


    public String namespaceToPackageName(String n) throws URISyntaxException {
        StringBuilder packageName = new StringBuilder();
        URI uri = new URI(n);
        String host = uri.getHost();
        if (host != null) {
            String[] hostParts = host.split("\\.");
            for (int j = hostParts.length -1 ; j >= 0 ; j--) {
                packageName.append(normalizePackageNameComponent(hostParts[j]));
                packageName.append(".");
            }
        }

        String path = uri.getPath();
        if (path != null) {
            String[] pathParts = path.split("/");
            for (String pathCmp : pathParts) {
                pathCmp = normalizePackageNameComponent(pathCmp);
                if (pathCmp.isBlank()) {
                    continue;
                }
                packageName.append(pathCmp);
                packageName.append(".");
            }
        }

        String ret = packageName.toString();
        if (ret.endsWith(".")) {
            ret = ret.substring(0, ret.length() -1);
        }
        return ret;
    }


    private String normalizePackageNameComponent(String string) {
        // FIXME: Extend
        string = string.toLowerCase();

        if (SourceVersion.isKeyword(string)) {
            string = "_" + string;
        }
        string  = string.replaceAll("-", "_");
        return string;
    }


    private Class<?> resolvePotentialStaticBinding(TypeDefinition typeDef) {
        return typeDef.getCompileTimeClass();
    }


    private TypeBinding createFromComplexType(TypeBinding binding, ComplexTypeDefinition typeDef, String packageName) {
        if (typeDef.isObjectMarker()) {
            var objectable = new ObjectableContract(typeDef, packageName);
            binding.defaultContract(objectable);
            objectable.setContainerName(determineContainerName(typeDef));
        } else if (typeDef.isReferenceMarker()) {
            binding.defaultContract(new ReferenceContract(typeDef, packageName));
        } else if (typeDef.isContainerMarker()) {
            if (typeDef.isXsdAnyMarker()) {
                binding.defaultContract(new ContainerableAnyContract(typeDef, packageName));
            } else {
                binding.defaultContract(new ContainerableContract(typeDef, packageName));
            }
        } else if (isSimpleType(typeDef.getSuperType())) {
            binding.defaultContract(new ValueWrappedContract(typeDef, packageName));
        } else {
            binding.defaultContract(new PlainStructuredContract(typeDef, packageName));


        }
        // Plain mapping
        return binding;
    }

    private boolean isSimpleType(@Nullable QName superType) {
        // FIXME: Add proper checks, String is sufficient for current schema
        return DOMUtil.XSD_STRING.equals(superType);
    }

    private QName determineContainerName(ComplexTypeDefinition typeDef) {
        PrismSchema schema = schemas.get(typeDef.getTypeName().getNamespaceURI());
        var objDef = schema.findItemDefinitionByType(typeDef.getTypeName(), PrismObjectDefinition.class);
        return objDef != null ? objDef.getItemName() : null;
    }


    public TypeBinding requireBinding(@NotNull QName typeName) {
        TypeBinding ret = bindings.get(typeName);
        if (ret == null) {
            throw new IllegalStateException("Missing binding for " + typeName);
        }
        return ret;
    }

    public Iterable<Binding> getDerivedBindings() {
        return derivedBindings;
    }

    public void addConstantMapping(NamespaceConstantMapping constant) {
        this.constants.put(constant.getNamespace(), constant);
    }

}
