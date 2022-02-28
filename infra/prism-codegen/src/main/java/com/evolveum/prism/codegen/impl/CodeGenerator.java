/*
 * Copyright (c) 2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.codegen.impl;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.PrismConstants;
import com.evolveum.prism.codegen.binding.Binding;
import com.evolveum.prism.codegen.binding.BindingContext;
import com.evolveum.prism.codegen.binding.ConstantsContract;
import com.evolveum.prism.codegen.binding.ContainerableAnyContract;
import com.evolveum.prism.codegen.binding.ContainerableContract;
import com.evolveum.prism.codegen.binding.Contract;
import com.evolveum.prism.codegen.binding.EnumerationContract;
import com.evolveum.prism.codegen.binding.ObjectFactoryContract;
import com.evolveum.prism.codegen.binding.ObjectableContract;
import com.evolveum.prism.codegen.binding.PlainStructuredContract;
import com.evolveum.prism.codegen.binding.ReferenceContract;
import com.evolveum.prism.codegen.binding.TypeBinding;
import com.evolveum.prism.codegen.binding.ValueWrappedContract;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.writer.FileCodeWriter;

public class CodeGenerator {
    final JCodeModel model = new JCodeModel();
    private final CodeWriter codeWriter;
    private final BindingContext context;

    private final Set<Binding> processed = new HashSet<>();
    private final Set<String> pregeneratedNamespaces = ImmutableSet.<String>builder()
            .add(PrismConstants.NS_TYPES)
            .add(PrismConstants.NS_QUERY)
            .build();

    private Multimap<Class<? extends Contract>, ContractGenerator<?>> generators = ImmutableMultimap.<Class<? extends Contract>, ContractGenerator<?>>builder()
            .put(ObjectableContract.class, new ObjectableGenerator(this))
            .put(PlainStructuredContract.class, new PlainStructuredGenerator(this))
            .put(ContainerableContract.class, new ContainerableGenerator<>(this))
            .put(EnumerationContract.class, new EnumerationGenerator(this))
            .put(ReferenceContract.class, new ReferencableGenerator(this))
            .put(ValueWrappedContract.class, new ValueWrapperGenerator(this))
            .put(ObjectFactoryContract.class, new ObjectFactoryGenerator(this))
            .put(ConstantsContract.class, new SchemaConstantsGenerator(this))
            .put(ContainerableAnyContract.class, new ContainerableAnyGenerator(this))
            .build();


    public CodeGenerator(CodeWriter codeWriter, BindingContext context) {
        this.codeWriter = codeWriter;
        this.context = context;
    }


    public CodeGenerator(File outDir, BindingContext context) throws IOException {
        this(new FileCodeWriter(outDir), context);
    }


    public void process(Binding binding) throws CodeGenerationException {
        if (alreadyGenerated(binding)) {
            return;
        }
        try {
            for (Contract contract : binding.getContracts()) {
                for(ContractGenerator<Contract> generator : getGenerators(contract)) {
                    JDefinedClass clazz = generator.declare(contract);
                    if (clazz != null) {
                        generator.implement(contract, clazz);
                    }
                }
            }
        } catch (Exception e) {
            throw CodeGenerationException.of(e, "Can not generate code for %s. Reason: %s", binding, e.getMessage());
        }finally {
            processed.add(binding);
        }
    }

    public void process() throws CodeGenerationException {
        for (Binding binding : context.getDerivedBindings()) {
            process(binding);
        }
    }

    private boolean alreadyGenerated(Binding binding) {
        if (pregeneratedNamespaces.contains(binding.getNamespaceURI())) {
            return true;
        }
        return processed.contains(binding);
    }


    private Collection<ContractGenerator<Contract>>  getGenerators(Contract contract) throws CodeGenerationException {
        Class<? extends Contract> contractClass = contract.getClass();
        @SuppressWarnings({ "unchecked", "rawtypes" })
        Collection<ContractGenerator<Contract>> generator = (Collection) generators.get(contractClass);
        CodeGenerationException.check(!generator.isEmpty(), "Missing code generator for %s", contractClass);
        return generator;
    }

    public void write() throws IOException {
        model.build(codeWriter);
    }

    public TypeBinding bindingFor(@NotNull QName typeName) {
        return context.requireBinding(typeName);
    }

}
