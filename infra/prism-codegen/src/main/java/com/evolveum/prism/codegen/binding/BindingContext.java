/*
 * Copyright (c) 2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.codegen.binding;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.ComplexTypeDefinition;
import com.evolveum.midpoint.prism.PrismConstants;
import com.evolveum.midpoint.prism.TypeDefinition;
import com.evolveum.midpoint.prism.schema.PrismSchema;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.prism.codegen.binding.TypeBinding.Static;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class BindingContext {

    // FIXME: This should be probably package specific
    public static final String SCHEMA_CONSTANTS_GENERATED_CLASS_NAME = "com.evolveum.midpoint.schema.SchemaConstantsGenerated";

    public static final String TYPE_CONSTANT = "COMPLEX_TYPE";

    Set<PrismSchema> schemas = new HashSet<>();
    Map<QName, TypeBinding> bindings = new HashMap<>();

    Set<TypeBinding> staticBindings = new HashSet<>();
    Set<TypeBinding> derivedBindings = new HashSet<>();


    BiMap<String, String> xmlToJavaNs = HashBiMap.create();


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
        staticBinding(ItemPathType.COMPLEX_TYPE, ItemPathType.class);
        staticBinding(DOMUtil.XSD_QNAME, QName.class);
        staticBinding(PrismConstants.POLYSTRING_TYPE_QNAME, PolyStringType.class);
    }


    private void staticBinding(QName name, Class<?> javaClass) {
        TypeBinding binding = new TypeBinding.Static(name, javaClass);
        bindings.put(name, binding);
    }


    public void addSchemas(Collection<PrismSchema> schemas) {
        this.schemas.addAll(schemas);
    }

    public BindingContext process() {
        for (PrismSchema schema : schemas) {
            process(schema);
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
        @NotNull
        List<TypeDefinition> typeDefs = schema.getDefinitions(TypeDefinition.class);
        for (TypeDefinition typeDef : typeDefs) {
            TypeBinding binding = createBinding(typeDef);
            bindings.put(typeDef.getTypeName(), binding);
        }
    }

    @VisibleForTesting
    TypeBinding createBinding(TypeDefinition typeDef) {
        Class<?> existingClass = resolvePotentialStaticBinding(typeDef);
        TypeBinding binding = existingClass != null ?  new TypeBinding.Static(typeDef.getTypeName(), existingClass) : new TypeBinding.Derived(typeDef.getTypeName());

        if (binding instanceof Static) {
            staticBindings.add(binding);
        } else {
            derivedBindings.add(binding);
        }

        if (typeDef instanceof ComplexTypeDefinition) {
            return createFromComplexType(binding, (ComplexTypeDefinition) typeDef);
        }
        return binding;
    }

    private Class<?> resolvePotentialStaticBinding(TypeDefinition typeDef) {
        if (xmlToJavaNs.get(typeDef.getTypeName().getNamespaceURI()) != null) {
            // FIXME: for now ignore existing artefacts, lets regenerate them
            return null;
        }
        return typeDef.getCompileTimeClass();
    }


    private TypeBinding createFromComplexType(TypeBinding binding, ComplexTypeDefinition typeDef) {
        String packageName = xmlToJavaNs.get(typeDef.getTypeName().getNamespaceURI());
        if (typeDef.isObjectMarker()) {
            binding.defaultContract(new ObjectableContract(typeDef, packageName));
        } else if (typeDef.isContainerMarker()) {
            binding.defaultContract(new ContainerableContract(typeDef, packageName));
        } else {
            binding.defaultContract(new PlainStructuredContract(typeDef, packageName));
        }
        // Plain mapping
        return binding;
    }

    public TypeBinding requireBinding(@NotNull QName typeName) {
        return bindings.get(typeName);
    }

    public Iterable<TypeBinding> getDerivedBindings() {
        return derivedBindings;
    }

}
