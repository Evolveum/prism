/*
 * Copyright (c) 2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.codegen.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;

import com.evolveum.midpoint.prism.Referencable;
import com.evolveum.midpoint.prism.impl.binding.AbstractReferencable;
import com.evolveum.prism.codegen.binding.ItemBinding;
import com.evolveum.prism.codegen.binding.ReferenceContract;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

public class ReferencableGenerator extends StructuredGenerator<ReferenceContract> {

    private static final Class<?> SUPER_CLASS = AbstractReferencable.class;

    public ReferencableGenerator(CodeGenerator codeGenerator) {
        super(codeGenerator);
    }

    @Override
    public JDefinedClass declare(ReferenceContract contract) throws JClassAlreadyExistsException {
        JDefinedClass clazz = codeModel()._class(contract.fullyQualifiedName());
        clazz._extends(clazz(SUPER_CLASS).narrow(clazz));
        clazz._implements(Referencable.class);
        annotateType(clazz, contract, XmlAccessType.PROPERTY);
        var allDef = new ArrayList<>(contract.getAttributeDefinitions());
        allDef.addAll(contract.getLocalDefinitions());
        declareConstants(clazz, contract, allDef);
        // FIXME: Declare F_OID, F_TYPE, F_RELATION

        declareFactory(clazz);

        for (ItemBinding def : contract.getLocalDefinitions()) {
            if (!superHasMethod(def.getterName())) {
                JType type = asBindingType(def, contract);
                clazz.field(JMod.PRIVATE, type, def.fieldName());

            }
        }

        clazz.method(JMod.PROTECTED, clazz, "thisInstance").body()._return(JExpr._this());

        return clazz;
    }

    @Override
    protected void implementGetter(JDefinedClass clazz, JMethod method, ItemBinding definition, JType returnType) {
        if (superHasMethod(method.name())) {
            method.body()._return(JExpr._super().invoke(method));
        } else {
            method.body()._return(JExpr.ref(definition.fieldName()));
        }
    }

    private boolean superHasMethod(String name) {
        for (Method method : SUPER_CLASS.getMethods()) {
            if (method.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void implementSetter(JDefinedClass clazz, JMethod method, ItemBinding definition, JVar valueParam) {
        if (superHasMethod(method.name())) {
            method.body().invoke(JExpr._super(),method).arg(valueParam);
        } else {
            method.body().assign(JExpr.ref(definition.fieldName()), valueParam);
        }
    }


    @Override
    protected void implementClone(JDefinedClass clazz, ReferenceContract contract, JMethod clone) {
        clone.body()._return(JExpr._super().invoke("clone").arg(JExpr.ref(FACTORY)));
    }



}
