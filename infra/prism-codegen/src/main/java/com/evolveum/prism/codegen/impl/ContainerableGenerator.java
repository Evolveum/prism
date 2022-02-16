/*
 * Copyright (c) 2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.codegen.impl;

import com.evolveum.midpoint.prism.PrismContainerDefinition;
import com.evolveum.midpoint.prism.PrismReferenceDefinition;
import com.evolveum.midpoint.prism.impl.binding.AbstractMutableContainerable;
import com.evolveum.prism.codegen.binding.ContainerableContract;
import com.evolveum.prism.codegen.binding.ItemBinding;
import com.evolveum.prism.codegen.binding.TypeBinding;
import com.sun.codemodel.ClassType;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

public class ContainerableGenerator<T extends ContainerableContract> extends StructuredGenerator<T> {

    protected static final String GET_PROPERTY_VALUE = "prismGetPropertyValue";
    protected static final String GET_PROPERTY_VALUES = "prismGetPropertyValues";
    protected static final String GET_CONTAINERABLE_VALUES = "prismGetContainerableList";
    protected static final String GET_REFERENCABLE_VALUES = "prismGetReferencableList";


    protected static final String SET_PROPERTY_VALUE = "prismSetPropertyValue";


    private final Class<?> baseClass;

    public ContainerableGenerator(CodeGenerator codeGenerator) {
        this(codeGenerator, AbstractMutableContainerable.class);
    }

    public ContainerableGenerator(CodeGenerator codeGenerator, Class<?> baseClass) {
        super(codeGenerator);
        this.baseClass = baseClass;
    }


    @Override
    public JDefinedClass declare(T contract) throws JClassAlreadyExistsException {
        String name = contract.fullyQualifiedName();
        int mods = contract.getTypeDefinition().isAbstract() ? JMod.PUBLIC | JMod.ABSTRACT : JMod.PUBLIC;
        JDefinedClass clazz = codeModel()._class(mods, name, ClassType.CLASS);

        if (contract.getSuperType() != null) {
            TypeBinding superType = bindingFor(contract.getSuperType());
            clazz._extends(codeModel().ref(superType.defaultBindingClass()));
        } else {
            clazz._extends(baseClass);
        }

        declareConstants(clazz, contract);

        if (!contract.getTypeDefinition().isAbstract()) {
            declareFactory(clazz);
        }

        return clazz;
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
            JClass rawType = ((JClass) returnType).getTypeParameters().get(0);
            if (definition.getDefinition() instanceof PrismContainerDefinition<?>) {

                invocation = JExpr._this().invoke(GET_CONTAINERABLE_VALUES);
                invocation.arg(rawType.staticRef(FACTORY));
            } else if (definition.getDefinition() instanceof PrismReferenceDefinition) {
                invocation = JExpr._this().invoke(GET_REFERENCABLE_VALUES);
                invocation.arg(rawType.staticRef(FACTORY));

            } else {
                invocation = JExpr._this().invoke(GET_PROPERTY_VALUES);
            }
        } else {
            invocation = JExpr._this().invoke(GET_PROPERTY_VALUE);
        }
        // push arguments
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
    protected void implementSetter(JMethod method, ItemBinding definition, JVar value) {
        JBlock body = method.body();

        // FIXME: Dispatch based on knowledge if type is container / reference / property
        //final String jaxbUtilMethod;
        //if (definition instanceof PrismContainerDefinition<?>) {
        //} else if (definition instanceof PrismReferenceDefinition){
        //} else {
        //}
        JInvocation invocation = body.invoke(JExpr._this(),SET_PROPERTY_VALUE);
        //push arguments
        invocation.arg(fieldConstant(definition.constantName()));
        invocation.arg(value);
    }



}
