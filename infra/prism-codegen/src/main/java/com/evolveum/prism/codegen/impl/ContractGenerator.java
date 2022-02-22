/*
 * Copyright (c) 2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.codegen.impl;

import java.util.Optional;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;

import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.prism.codegen.binding.Contract;
import com.evolveum.prism.codegen.binding.TypeBinding;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;

public abstract class ContractGenerator<T extends Contract> {

    private final CodeGenerator codeGenerator;

    public ContractGenerator(CodeGenerator codeGenerator) {
        this.codeGenerator = codeGenerator;
    }

    public abstract JDefinedClass declare(T contract) throws JClassAlreadyExistsException;

    public abstract void implement(T contract, JDefinedClass clazz);


    public CodeGenerator getCodeGenerator() {
        return codeGenerator;
    }

    public JCodeModel codeModel() {
        return codeGenerator.model;
    }

    public JClass clazz(Class<?> clz) {
        return codeModel().ref(clz);
    }

    public TypeBinding bindingFor(QName name) {
        return codeGenerator.bindingFor(name);
    }

    protected void applyDocumentation(JDocComment javadoc, Optional<String> documentation) {
        if (documentation.isPresent()) {
            Document docDom = DOMUtil.parseDocument(documentation.get());
            String docText = docDom.getDocumentElement().getTextContent();
            javadoc.add(docText);

        }

    }
}
