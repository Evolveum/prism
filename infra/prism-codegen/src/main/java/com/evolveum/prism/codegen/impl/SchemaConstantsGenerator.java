/*
 * Copyright (c) 2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.codegen.impl;

import com.evolveum.prism.codegen.binding.NamespaceConstantMapping;
import com.evolveum.prism.codegen.binding.ConstantsContract;
import com.evolveum.prism.codegen.binding.ItemBinding;
import com.evolveum.prism.codegen.binding.ObjectFactoryContract;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JMod;

public class SchemaConstantsGenerator extends ContractGenerator<ConstantsContract> {

    public SchemaConstantsGenerator(CodeGenerator codeGenerator) {
        super(codeGenerator);

    }

    @Override
    public JDefinedClass declare(ConstantsContract contract) throws JClassAlreadyExistsException {
        JDefinedClass constants = codeModel()._class(contract.fullyQualifiedName());

        for (NamespaceConstantMapping value : contract.getMappings().values()) {
            constants.field(JMod.PUBLIC | JMod.STATIC | JMod.FINAL,
                    String.class, namespaceField(value), JExpr.lit(value.getNamespace()));
        }
        return constants;
    }

    private String namespaceField(NamespaceConstantMapping contract) {
        return "NS_" + contract.getName();
    }

    @Override
    public void implement(ConstantsContract constants, JDefinedClass clazz) {

        for (NamespaceConstantMapping value : constants.getMappings().values()) {
            ObjectFactoryContract contract = constants.getContract(value);
            if (contract == null) {
                continue;
            }
            var namespaceField = JExpr.ref(namespaceField(value));
            for (ItemBinding item : contract.getItemNameToType()) {
                String fieldName = value.getPrefix() + "_" + item.constantName();
                createQNameConstant(clazz, fieldName, item.getQName(), namespaceField, true, true);
            }
        }



    }

}
