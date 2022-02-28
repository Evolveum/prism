package com.evolveum.prism.codegen.impl;

import java.util.List;

import javax.xml.bind.annotation.XmlType;

import com.evolveum.midpoint.prism.impl.xjc.PrismForJAXBUtil;
import com.evolveum.prism.codegen.binding.ContainerableAnyContract;
import com.google.common.collect.Iterables;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;

public class ContainerableAnyGenerator extends ContainerableGenerator<ContainerableAnyContract> {

    public ContainerableAnyGenerator(CodeGenerator codeGenerator, Class<?> baseClass) {
        super(codeGenerator, baseClass);
    }

    public ContainerableAnyGenerator(CodeGenerator codeGenerator) {
        super(codeGenerator);
    }

    @Override
    public JDefinedClass declare(ContainerableAnyContract contract) throws JClassAlreadyExistsException {
        var clazz = super.declare(contract);
        var xmlType = Iterables.find(clazz.annotations(), a -> a.getAnnotationClass().equals(clazz(XmlType.class)));
        ((JAnnotationArrayMember) xmlType.getAnnotationMembers().get("propOrder")).param("any");
        return clazz;
    }

    @Override
    protected void implementationAfterFluentApi(ContainerableAnyContract contract, JDefinedClass clazz) {
        JMethod any = clazz.method(JMod.PUBLIC,  clazz(List.class).narrow(Object.class) ,"getAny");
        any.body()._return(clazz(PrismForJAXBUtil.class).staticInvoke("getAny")
                .arg(JExpr.invoke("asPrismContainerValue"))
                .arg(clazz(Object.class).dotclass())
                );
        super.implementationAfterFluentApi(contract, clazz);
    }

}
