/*
 * Copyright (c) 2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.codegen.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.PrismReferenceValue;
import com.evolveum.midpoint.prism.TypeDefinition;
import com.evolveum.midpoint.prism.impl.PrismReferenceValueImpl;
import com.evolveum.midpoint.util.Producer;
import com.evolveum.prism.codegen.binding.BindingContext;
import com.evolveum.prism.codegen.binding.ItemBinding;
import com.evolveum.prism.codegen.binding.ReferenceContract;
import com.evolveum.prism.codegen.binding.StructuredContract;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

public abstract class StructuredGenerator<T extends StructuredContract> extends ContractGenerator<T> {

    private static final String XML_ELEMENT_NAME = "name";
    private static final String VALUE_PARAM = "value";

    protected static final String FACTORY = "FACTORY";


    public StructuredGenerator(CodeGenerator codeGenerator) {
        super(codeGenerator);
    }

    protected void declareFactory(JDefinedClass clazz) {
        JClass producerType = clazz(Producer.class).narrow(clazz);
        JDefinedClass annonFactory = codeModel().anonymousClass(producerType);
        annonFactory.method(JMod.PUBLIC, clazz, "run").body()._return(JExpr._new(clazz));
        clazz.field(JMod.PUBLIC | JMod.STATIC | JMod.FINAL, producerType, FACTORY, JExpr._new(annonFactory));
    }

    protected void declareConstants(JDefinedClass clazz, StructuredContract contract) {
        clazz.field(JMod.PRIVATE | JMod.FINAL | JMod.STATIC, clazz(Long.class), "serialVersionUid",
                JExpr.lit(BindingContext.SERIAL_VERSION_UID));

        JFieldVar namespaceField = null;
        createQNameConstant(clazz, BindingContext.TYPE_CONSTANT, contract.getTypeDefinition().getTypeName(),  null, false, false);
        for(ItemBinding def : contract.getLocalDefinitions()) {
            createQNameConstant(clazz, "F_"  + def.constantName(), def.itemName(), namespaceField, true, true);
        }
    }

    @Override
    public void implement(T contract, JDefinedClass clazz) {
        for (ItemBinding definition : contract.getLocalDefinitions()) {

            // Getter
            JType bindingType = asBindingType(definition);
            JMethod getter = clazz.method(JMod.PUBLIC, asBindingType(definition), definition.getterName());

            // @XmlElement annotation
            JAnnotationUse jaxbAnn = getter.annotate(XmlElement.class);
            jaxbAnn.param(XML_ELEMENT_NAME, definition.itemName().getLocalPart());

            implementGetter(getter, definition, bindingType);

            // Setter
            JMethod setter = clazz.method(JMod.PUBLIC, void.class, definition.setterName());
            JVar valueParam = setter.param(bindingType, VALUE_PARAM);
            implementSetter(setter, definition, valueParam);
        }

        // Fluent API


        StructuredContract current = contract;
        // Generate fluent api from local definitions of current contract and
        // then from parent type contracts.
        Set<String> alreadyGenerated = new HashSet<>();
        while (current != null) {
            generateFluentApi(clazz, current.getLocalDefinitions(), alreadyGenerated);
            QName superType = current.getSuperType();
            current = null;
            if (superType != null) {
                var superContract = bindingFor(superType).getDefaultContract();
                if (superContract instanceof StructuredContract) {
                    current = (StructuredContract) superContract;
                }
            }

        }

        implementationAfterFluentApi(contract,clazz);
    }

    protected void implementationAfterFluentApi(T contract, JDefinedClass clazz) {
        // Intentional NOOP
    }

    private void generateFluentApi(JDefinedClass clazz, Iterable<ItemBinding> localDefs, Set<String> alreadyGenerated) {
        for (ItemBinding definition : localDefs) {
            if (alreadyGenerated.contains(definition.fieldName())) {
                continue;
            }
            alreadyGenerated.add(definition.fieldName());
            JMethod fluentSetter = clazz.method(JMod.PUBLIC, clazz, definition.fieldName());
            JType type = asBindingTypeUnwrapped(definition.getDefinition().getTypeName());
            JVar value = fluentSetter.param(type, "value");
            if (definition.isList()) {
                fluentSetter.body().invoke(JExpr.invoke(definition.getterName()), "add").arg(value);
            } else {
                fluentSetter.body().invoke(definition.setterName()).arg(value);
            }

            fluentSetter.body()._return(JExpr._this());

            // If binding is structured, generate begin / end method

            var targetContract = bindingFor(definition.getDefinition().getTypeName()).getDefaultContract();
            if (targetContract instanceof ReferenceContract) {
                var refClazz = codeModel().ref(((ReferenceContract) targetContract).fullyQualifiedName());
                declareReferenceMethod(clazz, definition.fieldName(), refClazz, (m,r) -> {});
                declareReferenceMethod(clazz, definition.fieldName(), refClazz, (method,refVal) -> {
                    var relation = method.param(QName.class, "relation");
                    method.body().invoke(refVal, "setRelation").arg(relation);
                });

            }

            if (targetContract instanceof StructuredContract) {
                var beginMethod = clazz.method(JMod.PUBLIC, type, "begin" + definition.getJavaName());
                value = beginMethod.body().decl(type, "value", JExpr._new(type));
                beginMethod.body().invoke(definition.fieldName()).arg(value);
                beginMethod.body()._return(value);
            }
        }
    }

    private void declareReferenceMethod(JDefinedClass clazz, String name, JClass refClazz, BiConsumer<JMethod, JVar> refValCustomizer) {
        var method = clazz.method(JMod.PUBLIC, clazz, name);
        var oid = method.param(String.class, "oid");
        var type = method.param(QName.class, "type");
        var body = method.body();
        var refVal = body.decl(clazz(PrismReferenceValue.class), "refVal",
                JExpr._new(clazz(PrismReferenceValueImpl.class)).arg(oid).arg(type));
        refValCustomizer.accept(method, refVal);
        // ObjectReferenceType instance
        var ort = body.decl(refClazz, "ort", JExpr._new(refClazz));
        body.invoke(ort, "setupReferenceValue").arg(refVal);
        body._return(JExpr.invoke(name).arg(ort));
    }

    protected abstract void implementGetter(JMethod method, ItemBinding definition, JType returnType);

    protected abstract void implementSetter(JMethod method, ItemBinding definition, JVar valueParam);



    protected JType asBindingType(ItemBinding definition) {
        JType valueType = asBindingTypeUnwrapped(definition.getDefinition().getTypeName());
        if (definition.isList()) {
            // Wrap as list
            valueType = codeModel().ref(List.class).narrow(valueType);
        }
        return valueType;
    }

    public void annotateType(JDefinedClass clazz, StructuredContract contract, XmlAccessType type) {
        // XML Accessor Type
        clazz.annotate(XmlAccessorType.class).param("value", clazz(XmlAccessType.class).staticRef(type.name()));

        // XML Type annotation
        JAnnotationUse typeAnnon = clazz.annotate(XmlType.class);
        typeAnnon.param("name", contract.getTypeDefinition().getTypeName().getLocalPart());

        // Property order
        JAnnotationArrayMember propOrder = typeAnnon.paramArray("propOrder");
        for(ItemBinding def : contract.getLocalDefinitions()) {
            propOrder.param(def.itemName().getLocalPart());
        }

        @NotNull
        Collection<TypeDefinition> subtypes = contract.getTypeDefinition().getStaticSubTypes();
        if (!subtypes.isEmpty()) {
            var seeAlso = clazz.annotate(XmlSeeAlso.class).paramArray("value");
            for(TypeDefinition subtype : subtypes) {
                seeAlso.param(codeModel().ref(bindingFor(subtype.getTypeName()).defaultBindingClass()));
            }


        }
    }
}

