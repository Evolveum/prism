/*
 * Copyright (c) 2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.codegen.impl;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;

import com.evolveum.axiom.concepts.Lazy;
import com.evolveum.midpoint.prism.JaxbVisitor;
import com.evolveum.midpoint.prism.PrismPropertyDefinition;
import com.evolveum.midpoint.prism.Raw;
import com.evolveum.midpoint.prism.binding.StructuredCopy;
import com.evolveum.midpoint.prism.binding.StructuredEqualsStrategy;
import com.evolveum.midpoint.prism.binding.StructuredHashCodeStrategy;
import com.evolveum.midpoint.prism.impl.binding.AbstractPlainStructured;
import com.evolveum.midpoint.prism.impl.xjc.PrismForJAXBUtil;
import com.evolveum.prism.codegen.binding.ItemBinding;
import com.evolveum.prism.codegen.binding.PlainStructuredContract;
import com.evolveum.prism.codegen.binding.TypeBinding;
import com.google.common.collect.Iterables;
import com.sun.codemodel.ClassType;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

public class PlainStructuredGenerator extends StructuredGenerator<PlainStructuredContract> {

    private static final JExpression TRUE = JExpr.TRUE;
    private static final JExpression FALSE = JExpr.FALSE;

    private static final String HASH_CODE = "hashCode";
    private static final String COPY_VALUE = "of";
    private static final String COPY_LIST = "ofList";


    public PlainStructuredGenerator(CodeGenerator codeGenerator) {
        super(codeGenerator);
    }

    @Override
    public JDefinedClass declare(PlainStructuredContract contract) throws JClassAlreadyExistsException {
        String name = contract.fullyQualifiedName();
        JDefinedClass clazz = codeModel()._class(name, ClassType.CLASS);

        JClass superClazz = clazz(AbstractPlainStructured.class);
        if (contract.getSuperType() != null) {
            TypeBinding superType = bindingFor(contract.getSuperType());
            superClazz = codeModel().ref(superType.defaultBindingClass());
        }
        clazz._extends(superClazz);
        applyDocumentation(clazz.javadoc(), contract.getDocumentation());
        annotateType(clazz, contract, XmlAccessType.FIELD);
        // fields are first to minimize diff against cxf version
        declareFields(clazz, contract);
        var allDef = new ArrayList<>(contract.getAttributeDefinitions());
        allDef.addAll(contract.getLocalDefinitions());
        declareConstants(clazz, contract, allDef);

        // Default constructor
        clazz.constructor(JMod.PUBLIC);

        // Copy constructor
        copyConstructor(clazz, contract, allDef);
        return clazz;
    }

    private void copyConstructor(JDefinedClass clazz, PlainStructuredContract contract, ArrayList<ItemBinding> allDef) {
        var constr = clazz.constructor(JMod.PUBLIC);
        var other = constr.param(clazz, "other");
        constr.body().invoke("super").arg(other);
        for (ItemBinding item : itemDefinitions(contract)) {
            constr.body().assign(JExpr.refthis(item.fieldName()),
                    clazz(StructuredCopy.class).staticInvoke(item.isList() ? COPY_LIST : COPY_VALUE).arg(other.ref(item.fieldName())));
        }
    }

    @Override
    protected Iterable<ItemBinding> itemDefinitions(PlainStructuredContract contract) {
        return Iterables.concat(contract.getAttributeDefinitions(), contract.getLocalDefinitions());
    }


    private void declareFields(JDefinedClass clazz, PlainStructuredContract contract) {
        for (ItemBinding item : itemDefinitions(contract)) {
            String fieldName = item.fieldName();
            JType type = asBoxedTypeIfPossible(asBindingType(item, contract), item);
            JFieldVar field = clazz.field(JMod.PROTECTED, type, fieldName);
            if (shouldUseJaxbElement(item, contract)) {
                JAnnotationUse ann = field.annotate(XmlElementRef.class);
                ann.param("name", item.getQName().getLocalPart());
                ann.param("namespace", item.getQName().getNamespaceURI());
            }
            if (item.isAttribute()) {
                field.annotate(XmlAttribute.class).param("name", item.getQName().getLocalPart());
            } else {
                var xmlElement = Lazy.from(() -> field.annotate(XmlElement.class));
                if (!item.fieldName().equals(item.getQName().getLocalPart())) {
                    xmlElement.get().param("name", item.getQName().getLocalPart());
                }
                if (item.getDefinition() instanceof PrismPropertyDefinition<?>) {
                    Object defValue = ((PrismPropertyDefinition<?>) item.getDefinition()).defaultValue();
                    if (defValue != null) {
                        xmlElement.get().param("defaultValue", defValue.toString());
                    }
                    if (item.getDefinition().getMinOccurs() >= 1) {
                        xmlElement.get().param("required", true);
                    }
                }
                if (item.isRaw()) {
                    field.annotate(Raw.class);
                }
            }

        }
    }

    @Override
    protected void implementGetter(JDefinedClass clazz, JMethod method, ItemBinding definition, JType returnType) {
        if (definition.isList()) {
            method.body()._if(JExpr.ref(definition.fieldName()).eq(JExpr._null()))
                ._then().assign(JExpr.ref(definition.fieldName()),
                        JExpr._new(clazz(ArrayList.class).narrow(((JClass) returnType).getTypeParameters())));
        }
        method.body()._return(JExpr._this().ref(definition.fieldName()));
    }

    @Override
    protected boolean shouldAnnotateGetter(ItemBinding definition, PlainStructuredContract contract) {
        return false;
    }

    private JExpression fieldConstant(String constantName) {
        return JExpr.ref("F_" + constantName);
    }

    @Override
    protected boolean shouldImplementSetter(JDefinedClass clazz, PlainStructuredContract contract,
            ItemBinding definition) {
        return !definition.isList();
    }

    @Override
    protected void implementSetter(JDefinedClass clazz, JMethod method, ItemBinding definition, JVar value) {
        JBlock body = method.body();
        body.assign(JExpr._this().ref(definition.fieldName()), value);
    }

    @Override
    protected void implementEquals(JDefinedClass clazz, PlainStructuredContract contract) {
        JMethod equals = clazz.method(JMod.PUBLIC, boolean.class, "equals");
        JVar other = equals.param(Object.class, "other");
        JVar strategy = equals.param(StructuredEqualsStrategy.class, "strategy");

        JBlock b = equals.body();

        // If same, return true
        b._if(JExpr._this().eq(other))._then()._return(TRUE);

        // If not instance of clazz return false
        b._if(other._instanceof(clazz).not())._then()._return(FALSE);
        // If super.equals is false return fasle
        b._if(JExpr._super().invoke("equals").arg(other).arg(strategy).not())._then()._return(FALSE);


        JVar casted = b.decl(clazz, "casted",    JExpr.cast(clazz, other));
        Iterable<ItemBinding> toCompare = itemDefinitions(contract);
        for (ItemBinding item : toCompare) {
            String field = item.fieldName();
            // Check strategy for field pair, if not equals return false
            b._if(strategy.invoke("equals").arg(JExpr.ref(field)).arg(casted.ref(field)).not())
                ._then()._return(FALSE);
        }

        b._return(TRUE);
    }

    @Override
    protected void implementationAfterFluentApi(PlainStructuredContract contract, JDefinedClass clazz) {
        super.implementationAfterFluentApi(contract, clazz);

        var accept = clazz.method(JMod.PUBLIC, void.class, "accept");
        accept.annotate(Override.class);
        var visitor = accept.param(JaxbVisitor.class, "visitor");
        accept.body().invoke(JExpr._super(),accept).arg(visitor);
        for (ItemBinding item : contract.getLocalDefinitions()) {
            accept.body().staticInvoke(clazz(PrismForJAXBUtil.class), "accept")
                .arg(JExpr.ref(item.fieldName()))
                .arg(visitor);
        }

    }

    @Override
    protected void implementHashCode(JDefinedClass clazz, PlainStructuredContract contract) {
        var method = clazz.method(JMod.PUBLIC, int.class, HASH_CODE);
        var strategy = method.param(StructuredHashCodeStrategy.class, "strategy");
        var hash = method.body().decl(method.type(), "current", JExpr._super().invoke(HASH_CODE).arg(strategy));
        for (ItemBinding item : contract.getLocalDefinitions()) {
            method.body().assign(hash, strategy.invoke(HASH_CODE).arg(hash).arg(JExpr.ref(item.fieldName())));
        }
        method.body()._return(hash);
    }

    @Override
    protected void implementClone(JDefinedClass clazz, PlainStructuredContract contract, JMethod clone) {
        clone.body()._return(JExpr._new(clazz).arg(JExpr._this()));
    }
}
