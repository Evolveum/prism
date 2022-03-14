/*
 * Copyright (c) 2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.codegen.impl;

import java.util.Optional;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;

import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.prism.codegen.binding.BindingContext;
import com.evolveum.prism.codegen.binding.Contract;
import com.evolveum.prism.codegen.binding.TypeBinding;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMod;

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

    protected void declareSerialVersionUid(JDefinedClass clazz) {
        clazz.field(JMod.PRIVATE | JMod.FINAL | JMod.STATIC, long.class, "serialVersionUID",
                JExpr.lit(BindingContext.SERIAL_VERSION_UID));
    }

    protected JClass asBindingTypeUnwrapped(QName typeName) {
        TypeBinding binding = getCodeGenerator().bindingFor(typeName);

        if (binding == null) {
            throw new IllegalStateException("Missing binding for " + typeName);
        }

        JClass valueType = codeModel().ref(binding.defaultBindingClass());
        return valueType;
    }

    protected void createQNameConstant(JDefinedClass targetClass, String targetField, QName qname, JExpression namespaceArgument, boolean namespaceFieldIsLocal, boolean createPath) {
        if (namespaceArgument == null) {
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
