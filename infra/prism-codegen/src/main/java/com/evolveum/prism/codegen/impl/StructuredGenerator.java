/*
 * Copyright (c) 2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.codegen.impl;

import java.util.List;

import com.evolveum.prism.codegen.binding.ItemBinding;
import com.evolveum.prism.codegen.binding.StructuredContract;
import com.evolveum.prism.codegen.binding.TypeBinding;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;

public abstract class StructuredGenerator<T extends StructuredContract> extends ContractGenerator<T> {

    public StructuredGenerator(CodeGenerator codeGenerator) {
        super(codeGenerator);
        // TODO Auto-generated constructor stub
    }


    @Override
    public void implement(T contract) {
        JDefinedClass clazz = codeModel()._getClass(contract.fullyQualifiedName());

        for (ItemBinding definition : contract.getLocalDefinitions()) {
            JType returnType = asReturnType(definition);
            JMethod method = clazz.method(JMod.PUBLIC, asReturnType(definition), definition.getterName());
            implementGetter(method, definition, returnType);

        }
    }

    protected abstract void implementGetter(JMethod method, ItemBinding definition, JType returnType);

    protected abstract void implementSetter(JMethod method, ItemBinding definition, JType returnType);


    private JType asReturnType(ItemBinding definition) {
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

}

