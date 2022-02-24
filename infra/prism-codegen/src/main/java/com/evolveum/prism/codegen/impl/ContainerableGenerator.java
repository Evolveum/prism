/*
 * Copyright (c) 2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.codegen.impl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.PrismContainer;
import com.evolveum.midpoint.prism.PrismContainerDefinition;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismReferenceDefinition;
import com.evolveum.midpoint.prism.impl.binding.AbstractMutableContainerable;
import com.evolveum.prism.codegen.binding.ContainerableContract;
import com.evolveum.prism.codegen.binding.ItemBinding;
import com.evolveum.prism.codegen.binding.TypeBinding;
import com.google.common.base.CaseFormat;
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
    protected static final String GET_REFERENCE_OBJECTABLE = "prismGetReferenceObjectable";


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

        applyDocumentation(clazz.javadoc(), contract.getDocumentation());
        annotateType(clazz, contract, XmlAccessType.PROPERTY);
        declareConstants(clazz, contract, contract.getLocalDefinitions());


        clazz.constructor(JMod.PUBLIC).body().invoke("super");

        var publicPrismConst = clazz.constructor(JMod.PUBLIC);
        publicPrismConst.param(PrismContext.class, "context");
        publicPrismConst.annotate(Deprecated.class);
        publicPrismConst.body().invoke("super");

        if (!contract.getTypeDefinition().isAbstract()) {
            declareFactory(clazz);
        }

        return clazz;
    }


    @Override
    protected JClass asBindingTypeOrJaxbElement(ItemBinding definition, T contract) {
        return asBindingTypeUnwrapped(definition.getDefinition().getTypeName());
    }

    @Override
    protected void implementGetter(JDefinedClass clazz, JMethod method, ItemBinding definition, JType returnType) {
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
            if (definition.getDefinition() instanceof PrismReferenceDefinition) {
                // FIXME: Generate ObjectType classes
                var refDef = (PrismReferenceDefinition) definition.getDefinition();

                String name = refDef.getCompositeObjectElementName() != null ? lowerToUpper(refDef.getCompositeObjectElementName().getLocalPart()) : null;
                QName targetType = refDef.getTargetTypeName();

                if (name != null && targetType != null) {
                    var objType = codeModel().ref(bindingFor(targetType).defaultBindingClass());
                    var objGetter = clazz.method(JMod.PUBLIC, objType, "get" + name);
                    objGetter.body()._return(JExpr.invoke(GET_REFERENCE_OBJECTABLE)
                            .arg(fieldConstant(definition.constantName()))
                            .arg(JExpr.dotclass(objType)));
                }

            }
            invocation = JExpr._this().invoke(GET_PROPERTY_VALUE);
        }
        // push arguments
        invocation.arg(fieldConstant(definition.constantName()));

        JType type = returnType;
        if (type.isPrimitive()) {
            JPrimitiveType primitive = (JPrimitiveType) type;
            invocation.arg(JExpr.dotclass(primitive.boxify()));
        } else {
            JClass clz = (JClass) type;
            if (definition.isList()) {
                invocation.arg(JExpr.dotclass(clz.getTypeParameters().get(0)));
            } else {
                invocation.arg(JExpr.dotclass(clz));
            }
        }

        body._return(invocation);
    }

    private String lowerToUpper(String localPart) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, localPart);
    }

    private String removeRefSuffix(String javaName) {
        if (javaName.endsWith("Ref")) {
            return javaName.substring(0, javaName.length() - 3);
        }
        return null;
    }

    private JExpression fieldConstant(String constantName) {
        return JExpr.ref("F_" + constantName);
    }


    @Override
    protected void implementSetter(JDefinedClass clazz, JMethod method, ItemBinding definition, JVar value) {
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

    @Override
    protected void implementationAfterFluentApi(T contract, JDefinedClass clazz) {
        createContainerFluentEnd(clazz);
    }

    protected JMethod createContainerFluentEnd(JDefinedClass implClass) {
        String methodName = "end";
        JMethod method = implClass.method(JMod.PUBLIC, (JType) null, methodName);
        method.type(method.generify("X"));
        JBlock body = method.body();

        body._return(JExpr.cast(method.type(),
                JExpr.invoke(JExpr.cast(clazz(PrismContainerValue.class),
                                JExpr.invoke(JExpr.cast(clazz(PrismContainer.class),
                                        JExpr.invoke(JExpr.invoke("asPrismContainerValue"),"getParent")), "getParent")),
                "asContainerable")));

        return method;
    }

    protected void fluentSetter(JDefinedClass clazz, Class<?> param, String methodName, String setterName) {
        var method = clazz.method(JMod.PUBLIC, clazz, methodName);
        var value = method.param(param, "value");
        method.body().invoke(setterName).arg(value);
        method.body()._return(JExpr._this());

    }


}
