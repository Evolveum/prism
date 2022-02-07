/*
 * Copyright (c) 2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.codegen.impl;

import java.io.IOException;
import java.util.Map;

import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;

import com.evolveum.prism.codegen.binding.BindingContext;
import com.evolveum.prism.codegen.binding.Contract;
import com.evolveum.prism.codegen.binding.ObjectableContract;
import com.evolveum.prism.codegen.binding.TypeBinding;
import com.google.common.collect.ImmutableMap;
import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.writer.FileCodeWriter;

public class CodeGenerator {
    JCodeModel model = new JCodeModel();
    private CodeWriter codeWriter;
    private BindingContext context;

    private Map<Class<? extends Contract>, ContractGenerator<?>> generators = ImmutableMap.<Class<? extends Contract>, ContractGenerator<?>>builder()
            .put(ObjectableContract.class, new ObjectableGenerator(this))
            .build();


    public CodeGenerator(CodeWriter codeWriter, BindingContext context) {
        this.codeWriter = codeWriter;
        this.context = context;
    }


    public void process(TypeBinding binding) throws JClassAlreadyExistsException {

        for (Contract contract : binding.getContracts()) {
            ContractGenerator<Contract> generator = getGenerator(contract);
            JDefinedClass clazz = generator.declare(contract);
            generator.implement(contract, clazz);
        }
    }

    private ContractGenerator<Contract> getGenerator(Contract contract) {
        Class<? extends Contract> contractClass = contract.getClass();
        // FIXME: Check if generator is present
        @SuppressWarnings("unchecked")
        ContractGenerator<Contract> generator = (ContractGenerator<Contract>) generators.get(contractClass);
        return generator;
    }

    public void setWriter(FileCodeWriter writer) {
        this.codeWriter = writer;
    }

    public void write() throws IOException {
        model.build(codeWriter);
    }

    public TypeBinding bindingFor(@NotNull QName typeName) {
        return context.requireBinding(typeName);
    }

}
