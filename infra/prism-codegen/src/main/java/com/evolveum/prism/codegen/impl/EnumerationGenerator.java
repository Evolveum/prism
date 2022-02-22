package com.evolveum.prism.codegen.impl;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

import com.evolveum.midpoint.prism.EnumerationTypeDefinition.ValueDefinition;
import com.evolveum.prism.codegen.binding.EnumerationContract;
import com.sun.codemodel.ClassType;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JEnumConstant;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JForEach;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;

public class EnumerationGenerator extends ContractGenerator<EnumerationContract> {

    public EnumerationGenerator(CodeGenerator codeGenerator) {
        super(codeGenerator);
    }

    @Override
    public JDefinedClass declare(EnumerationContract contract) throws JClassAlreadyExistsException {

        String fullyqualifiedName = contract.fullyQualifiedName();
        JDefinedClass clazz = codeModel()._class(JMod.PUBLIC, fullyqualifiedName, ClassType.ENUM);
        applyDocumentation(clazz.javadoc(), contract.getDocumentation());
        clazz.annotate(XmlType.class).param("name", contract.getQName().getLocalPart());
        clazz.annotate(XmlEnum.class);
        return clazz;
    }

    @Override
    public void implement(EnumerationContract contract, JDefinedClass clazz) {
        for (ValueDefinition value : contract.values()) {
            JEnumConstant enumConst = clazz.enumConstant(value.getConstantName().get());
            applyDocumentation(enumConst.javadoc(),value.getDocumentation());
            enumConst.arg(JExpr.lit(value.getValue()));
            enumConst.annotate(XmlEnumValue.class).param("value", value.getValue());
        }

        // value field
        JFieldVar valueField = clazz.field(JMod.PRIVATE | JMod.FINAL, String.class, "value");

        // Constructor
        JMethod constructor = clazz.constructor(JMod.NONE);
        JVar param = constructor.param(String.class, "v");
        constructor.body().assign(valueField, param);

        // public method value()
        clazz.method(JMod.PUBLIC, String.class, "value").body()._return(valueField);

        // from value method
        JMethod fromValue = clazz.method(JMod.PUBLIC | JMod.STATIC, clazz, "fromValue");
        JVar vParam = fromValue.param(String.class, "v");
        JForEach forLoop = fromValue.body().forEach(clazz, "c", clazz.staticInvoke("values"));
        forLoop.body()._if(forLoop.var().ref("value").invoke("equals").arg(vParam))
            ._then()._return(forLoop.var());
        fromValue.body()._throw(JExpr._new(clazz(IllegalArgumentException.class)).arg(vParam));
    }


}
