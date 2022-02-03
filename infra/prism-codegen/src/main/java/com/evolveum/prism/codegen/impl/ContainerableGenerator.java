/*
 * Copyright (c) 2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.codegen.impl;

import com.evolveum.midpoint.prism.impl.xjc.PrismForJAXBUtil;
import com.evolveum.prism.codegen.binding.ContainerableContract;
import com.evolveum.prism.codegen.binding.ItemBinding;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;

public class ContainerableGenerator<T extends ContainerableContract> extends StructuredGenerator<T> {

    public static final String AS_PRISM_OBJECT = "asPrismObject";
    public static final String AS_PRISM_OBJECT_VALUE = "asPrismObjectValue";
    public static final String AS_PRISM_CONTAINER_VALUE = "asPrismContainerValue";
    protected static final String AS_PRISM_CONTAINER = "asPrismContainer";

    protected static final String PRISM_UTIL_GET_PROPERTY_VALUE = "getPropertyValue";
    protected static final String PRISM_UTIL_GET_PROPERTY_VALUES = "getPropertyValues";
    protected static final String PRISM_UTIL_SET_PROPERTY_VALUE = "setPropertyValue";
    protected static final String PRISM_UTIL_GET_REFERENCE_OBJECTABLE = "getReferenceObjectable";
    protected static final String PRISM_UTIL_SET_REFERENCE_VALUE_AS_REF = "setReferenceValueAsRef";
    protected static final String PRISM_UTIL_GET_FILTER = "getFilter";
    protected static final String PRISM_UTIL_SET_REFERENCE_FILTER_CLAUSE_XNODE = "setReferenceFilterClauseXNode";
    protected static final String PRISM_UTIL_GET_REFERENCE_TARGET_NAME = "getReferenceTargetName";
    protected static final String PRISM_UTIL_SET_REFERENCE_TARGET_NAME = "setReferenceTargetName";
    protected static final String PRISM_UTIL_OBJECTABLE_AS_REFERENCE_VALUE = "objectableAsReferenceValue";
    protected static final String PRISM_UTIL_SETUP_CONTAINER_VALUE = "setupContainerValue";
    protected static final String PRISM_UTIL_CREATE_TARGET_INSTANCE = "createTargetInstance";

    public ContainerableGenerator(CodeGenerator codeGenerator) {
        super(codeGenerator);
    }


    @Override
    public void declare(T contract) throws JClassAlreadyExistsException {
        // TODO Auto-generated method stub

    }


    @Override
    protected void implementGetter(JMethod method, ItemBinding definition, JType returnType) {
        JBlock body = method.body();
        JInvocation invocation;
        /*if (hasAnnotationClass(method, XmlAnyElement.class)) {
            // handling xsd any
            invocation = clazz(PrismForJAXBUtil.class).staticInvoke(PRISM_GET_ANY);
            invocation.arg(JExpr.invoke(AS_PRISM_CONTAINER_VALUE));

            JClass clazz = (JClass) field.type();
            invocation.arg(JExpr.dotclass(clazz.getTypeParameters().get(0)));
            body._return(invocation);
            return;
        }*/

        if (definition.isList()) {
            invocation = clazz(PrismForJAXBUtil.class).staticInvoke(PRISM_UTIL_GET_PROPERTY_VALUES);
        } else {
            invocation = clazz(PrismForJAXBUtil.class).staticInvoke(PRISM_UTIL_GET_PROPERTY_VALUE);
        }
        // push arguments
        invocation.arg(JExpr.invoke(AS_PRISM_CONTAINER_VALUE));
        invocation.arg(fieldConstant(definition.constantName()));

        JType type = returnType;
        if (type.isPrimitive()) {
            JPrimitiveType primitive = (JPrimitiveType) type;
            invocation.arg(JExpr.dotclass(primitive.boxify()));
        } else {
            JClass clazz = (JClass) type;
            if (definition.isList()) {
                invocation.arg(JExpr.dotclass(clazz.getTypeParameters().get(0)));
            } else {
                invocation.arg(JExpr.dotclass(clazz));
            }
        }

        body._return(invocation);
    }

    private JExpression fieldConstant(String constantName) {
        return JExpr.ref("F_" + constantName);
    }


    @Override
    protected void implementSetter(JMethod method, ItemBinding definition, JType returnType) {
        // TODO Auto-generated method stub

    }



}
