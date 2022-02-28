/*
 * Copyright (c) 2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.codegen.impl;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.Objectable;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.impl.binding.AbstractMutableObjectable;
import com.evolveum.prism.codegen.binding.BindingContext;
import com.evolveum.prism.codegen.binding.ObjectableContract;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;

public class ObjectableGenerator extends ContainerableGenerator<ObjectableContract> {

    private static final String GET_CONTAINER_NAME = "prismGetContainerName";
    private static final String GET_CONTAINER_TYPE = "prismGetContainerType";
    private static final String CONTAINER_NAME_CONSTANT = "CONTAINER_NAME";

    public ObjectableGenerator(CodeGenerator codeGenerator) {
        super(codeGenerator, AbstractMutableObjectable.class);
    }

    @Override
    public JDefinedClass declare(ObjectableContract contract) throws JClassAlreadyExistsException {
        JDefinedClass clazz = super.declare(contract);
        clazz._implements(clazz(Objectable.class));
        // FIXME: We should create CONTAINER_NAME constant
        if (contract.containerName() != null) {
            createQNameConstant(clazz, CONTAINER_NAME_CONSTANT, contract.containerName(), null, false, false);
        }
        return clazz;
    }

    @Override
    public void implement(ObjectableContract contract, JDefinedClass clazz) {
        // getContainerName

        JMethod contNameMethod = clazz.method(JMod.PROTECTED, QName.class, GET_CONTAINER_NAME);
        contNameMethod.annotate(Override.class);
        contNameMethod.body()._return(JExpr.ref(CONTAINER_NAME_CONSTANT));

        // getContainerType
        JMethod contTypeMethod = clazz.method(JMod.PROTECTED, QName.class, GET_CONTAINER_TYPE);
        contTypeMethod.annotate(Override.class);
        contTypeMethod.body()._return(JExpr.ref(BindingContext.TYPE_CONSTANT));

        var prismObjectType = clazz(PrismObject.class).narrow(contract.getTypeDefinition().isAbstract() ? clazz.wildcard() : clazz);
        clazz.method(JMod.PUBLIC, prismObjectType, "asPrismObject")
            .body()._return(JExpr._super().invoke("asPrismContainer"));

        super.implement(contract, clazz);
    }

}
