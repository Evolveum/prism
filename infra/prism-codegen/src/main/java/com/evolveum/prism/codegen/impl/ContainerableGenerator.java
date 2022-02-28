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
import com.evolveum.midpoint.prism.impl.xjc.PrismForJAXBUtil;
import com.evolveum.prism.codegen.binding.ContainerableContract;
import com.evolveum.prism.codegen.binding.ItemBinding;
import com.evolveum.prism.codegen.binding.StructuredContract;
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
    protected static final String SET_REFERENCE_OBJECTABLE = "prismSetReferenceObjectable";

    protected static final String SET_PROPERTY_VALUE = "prismSetPropertyValue";
    private static final String SET_SINGLE_CONTAINERABLE = "prismSetSingleContainerable";
    private static final String GET_SINGLE_CONTAINERABLE = "prismGetSingleContainerable";

    private static final String GET_REFERENCABLE = "prismGetReferencable";
    private static final String SET_REFERENCABLE = "prismSetReferencable";


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
    protected boolean shouldUseJaxbElement(ItemBinding definition, T contract) {
        return false;
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
        JExpression extraArg = null;
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
                invocation = JExpr._this().invoke(GET_REFERENCABLE);
                extraArg = ((JClass) returnType).staticRef(FACTORY);
            } else if (definition.getDefinition() instanceof PrismContainerDefinition<?>){
                invocation = JExpr._this().invoke(GET_SINGLE_CONTAINERABLE);
            } else {
                invocation = JExpr._this().invoke(GET_PROPERTY_VALUE);
            }
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

        if (extraArg != null) {
            invocation.arg(extraArg);
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
    protected boolean shouldImplementSetter(JDefinedClass clazz, T contract, ItemBinding definition) {
        return !definition.isList();
    }

    @Override
    protected void implementSetter(JDefinedClass clazz, JMethod method, ItemBinding definition, JVar value) {
        JBlock body = method.body();

        var def = definition.getDefinition();

        var call = SET_PROPERTY_VALUE;
        if (def instanceof PrismContainerDefinition<?>) {
            call = SET_SINGLE_CONTAINERABLE;
        } else if (def instanceof PrismReferenceDefinition){
            call = SET_REFERENCABLE;
        }
        JInvocation invocation = body.invoke(JExpr._this(),call);
        //push arguments
        invocation.arg(fieldConstant(definition.constantName()));
        invocation.arg(value);
    }

    @Override
    protected void implementAdditionalFieldMethod(JDefinedClass clazz, ItemBinding definition, JType returnType) {

        if (definition.getDefinition() instanceof PrismReferenceDefinition) {
            PrismReferenceDefinition prismRef = (PrismReferenceDefinition) definition.getDefinition();
            QName compositeObjectName = prismRef.getCompositeObjectElementName();
            if (compositeObjectName != null) {
                String name = StructuredContract.javaFromItemName(compositeObjectName);
                JClass type = asBindingTypeUnwrapped(prismRef.getTargetTypeName());
                var itemName = fieldConstant(definition.constantName());

                JMethod getter = clazz.method(JMod.PUBLIC, type, "get" + name);
                getter.body()._return(JExpr.invoke(GET_REFERENCE_OBJECTABLE)
                        .arg(itemName)
                        .arg(type.dotclass()));

                JMethod setter = clazz.method(JMod.PUBLIC,void.class, "set" + name);
                var value = setter.param(type, "value");
                setter.body().invoke(SET_REFERENCE_OBJECTABLE).arg(itemName).arg(value);
            }
        } else if (definition.isList() && bindingFor(definition.getDefinition().getTypeName()).getDefaultContract() instanceof ContainerableContract) {
            JMethod create = clazz.method(JMod.PUBLIC, returnType, "create" + definition.getJavaName() + "List");
            create.body().staticInvoke(clazz(PrismForJAXBUtil.class), "createContainer")
                .arg(JExpr.invoke("asPrismContainerValue"))
                .arg(fieldConstant(definition.constantName()));
            create.body()._return(JExpr.invoke(definition.getterName()));
        }


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

    @Override
    protected void implementClone(JDefinedClass clazz, T contract, JMethod clone) {
        clone.body()._return(JExpr.cast(clazz, JExpr._super().invoke("clone")));
    }


}
