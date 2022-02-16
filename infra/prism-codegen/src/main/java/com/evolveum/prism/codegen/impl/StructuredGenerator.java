/*
 * Copyright (c) 2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.codegen.impl;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.util.Producer;
import com.evolveum.prism.codegen.binding.BindingContext;
import com.evolveum.prism.codegen.binding.ItemBinding;
import com.evolveum.prism.codegen.binding.StructuredContract;
import com.evolveum.prism.codegen.binding.TypeBinding;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
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
    }

    protected abstract void implementGetter(JMethod method, ItemBinding definition, JType returnType);

    protected abstract void implementSetter(JMethod method, ItemBinding definition, JVar valueParam);


    protected JType asBindingType(ItemBinding definition) {
        TypeBinding binding = getCodeGenerator().bindingFor(definition.getDefinition().getTypeName());

        if (binding == null) {
            throw new IllegalStateException("Missing binding for " + definition.getDefinition().getTypeName());
        }

        JType valueType;
        valueType = codeModel().ref(binding.defaultBindingClass());

        if (definition.isList()) {
            // Wrap as list
            valueType = codeModel().ref(List.class).narrow(valueType);
        }
        return valueType;
    }


    protected void createQNameConstant(JDefinedClass targetClass, String targetField, QName qname, JFieldVar namespaceField, boolean namespaceFieldIsLocal, boolean createPath) {
        JExpression namespaceArgument;
        if (namespaceField != null) {
            if (namespaceFieldIsLocal) {
                namespaceArgument = namespaceField;
            } else {
                JClass schemaClass = codeModel()._getClass(BindingContext.SCHEMA_CONSTANTS_GENERATED_CLASS_NAME);
                namespaceArgument = schemaClass.staticRef(namespaceField);
            }
        } else {
            namespaceArgument = JExpr.lit(qname.getNamespaceURI());
        }
        createNameConstruction(targetClass, targetField, qname, namespaceArgument, createPath ? ItemName.class : QName.class);
    }

    private void createNameConstruction(JDefinedClass definedClass, String fieldName,
            QName reference, JExpression namespaceArgument, Class<?> nameClass) {
        JClass clazz = (JClass) codeModel()._ref(nameClass);
        JInvocation invocation = JExpr._new(clazz);
        invocation.arg(namespaceArgument);
        invocation.arg(reference.getLocalPart());
        definedClass.field(JMod.PUBLIC | JMod.STATIC | JMod.FINAL, nameClass, fieldName, invocation);
    }
}

