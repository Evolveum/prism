/*
 * Copyright (c) 2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.codegen.impl;

import com.evolveum.prism.codegen.binding.BindingContext;
import com.evolveum.prism.codegen.binding.ItemBinding;
import com.evolveum.prism.codegen.binding.PlainStructuredContract;
import com.evolveum.prism.codegen.binding.TypeBinding;
import com.sun.codemodel.ClassType;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

public class PlainStructuredGenerator extends StructuredGenerator<PlainStructuredContract> {

    protected static final String GET_PROPERTY_VALUE = "prismGetPropertyValue";
    protected static final String GET_PROPERTY_VALUES = "prismGetPropertyValues";
    protected static final String SET_PROPERTY_VALUE = "prismSetPropertyValue";

    public PlainStructuredGenerator(CodeGenerator codeGenerator) {
        super(codeGenerator);
    }

    @Override
    public JDefinedClass declare(PlainStructuredContract contract) throws JClassAlreadyExistsException {
        String name = contract.fullyQualifiedName();
        JDefinedClass clazz = codeModel()._class(name, ClassType.CLASS);

        if (contract.getSuperType() != null) {
            TypeBinding superType = bindingFor(contract.getSuperType());
            clazz._extends(codeModel().ref(superType.defaultBindingClass()));
        }

        createQNameConstant(clazz, BindingContext.TYPE_CONSTANT, contract.getTypeDefinition().getTypeName(),  null, false, false);
        declareConstants(clazz, contract);
        declareFields(clazz, contract);
        return clazz;
    }

    private void declareFields(JDefinedClass clazz, PlainStructuredContract contract) {
        for (ItemBinding item : contract.getLocalDefinitions()) {
            String fieldName = item.fieldName();
            JType type = asBindingType(item);
            clazz.field(JMod.PRIVATE, type, fieldName);
        }
    }

    @Override
    protected void implementGetter(JMethod method, ItemBinding definition, JType returnType) {
        method.body()._return(JExpr._this().ref(definition.fieldName()));
    }

    private JExpression fieldConstant(String constantName) {
        return JExpr.ref("F_" + constantName);
    }


    @Override
    protected void implementSetter(JMethod method, ItemBinding definition, JVar value) {
        JBlock body = method.body();
        body.assign(JExpr._this().ref(definition.fieldName()), value);
    }

}
