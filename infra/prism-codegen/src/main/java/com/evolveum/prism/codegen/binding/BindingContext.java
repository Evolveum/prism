/*
 * Copyright (c) 2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.codegen.binding;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.ComplexTypeDefinition;
import com.evolveum.midpoint.prism.PrismConstants;
import com.evolveum.midpoint.prism.TypeDefinition;
import com.evolveum.midpoint.prism.schema.PrismSchema;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class BindingContext {

    Set<PrismSchema> schemas = new HashSet<>();
    Map<QName, TypeBinding> bindings = new HashMap<>();
    BiMap<String, String> xmlToJavaNs = HashBiMap.create();


    public BindingContext() {
        staticBinding(DOMUtil.XSD_STRING, String.class);
        addNamespaceMapping(PrismConstants.NS_TYPES, PolyStringType.class.getPackageName());
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
            String xmlNs = schema.getNamespace();
            String javaNs = xmlToJavaNs.get(xmlNs);
            if (javaNs != null) {
                process(schema);
            }
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
        Class<?> existingClass = typeDef.getCompileTimeClass();
        TypeBinding binding = existingClass != null ?  new TypeBinding.Static(typeDef.getTypeName(), existingClass) : new TypeBinding.Derived(typeDef.getTypeName());
        if (typeDef instanceof ComplexTypeDefinition) {
            return createFromComplexType(binding, (ComplexTypeDefinition) typeDef);
        }
        return binding;
    }

    private TypeBinding createFromComplexType(TypeBinding binding, ComplexTypeDefinition typeDef) {
        if (typeDef.isObjectMarker()) {
            binding.defaultContract(new ObjectableContract(typeDef));
        } else if (typeDef.isContainerMarker()) {
            binding.defaultContract(new ContainerableContract(typeDef));
        } else {
            binding.defaultContract(new PlainStructuredContract(typeDef));
        }
        // Plain mapping
        return binding;
    }

    public TypeBinding requireBinding(@NotNull QName typeName) {
        return bindings.get(typeName);
    }

}
