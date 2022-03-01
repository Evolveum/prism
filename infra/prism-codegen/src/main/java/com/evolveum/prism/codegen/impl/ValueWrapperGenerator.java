package com.evolveum.prism.codegen.impl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import com.evolveum.midpoint.prism.impl.binding.AbstractValueWrapper;
import com.evolveum.prism.codegen.binding.ValueWrappedContract;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;

public class ValueWrapperGenerator extends ContractGenerator<ValueWrappedContract> {


    public ValueWrapperGenerator(CodeGenerator codeGenerator) {
        super(codeGenerator);
    }

    @Override
    public JDefinedClass declare(ValueWrappedContract contract) throws JClassAlreadyExistsException {
        JDefinedClass clazz = codeModel()._class(contract.fullyQualifiedName());
        JType bindingType = asBindingTypeUnwrapped(contract.getTypeDefinition().getSuperType());
        clazz._extends(clazz(AbstractValueWrapper.class).narrow(bindingType));
        clazz.annotate(XmlAccessorType.class)
            .param("value", clazz(XmlAccessType.class).staticRef(XmlAccessType.FIELD.name()));

        clazz.annotate(XmlType.class)
            .param("name", contract.getTypeDefinition().getTypeName().getLocalPart())
            .paramArray("propOrder").param("value");

        declareSerialVersionUid(clazz);
        return clazz;
    }

    @Override
    public void implement(ValueWrappedContract contract, JDefinedClass clazz) {
        JType bindingType = asBindingTypeUnwrapped(contract.getTypeDefinition().getSuperType());

        var value = clazz.field(JMod.PROTECTED, bindingType, "value");
        value.annotate(XmlValue.class);

        var get = clazz.method(JMod.PUBLIC, bindingType, "getValue");
        get.body()._return(value);
        get.annotate(Override.class);
        var set = clazz.method(JMod.PUBLIC, void.class, "setValue");
        var param = set.param(bindingType, "val");
        set.annotate(Override.class);
        set.body().assign(value, param);

        var clone = clazz.method(JMod.PUBLIC, clazz, "clone");
        var ret = clone.body().decl(clazz, "ret", JExpr._new(clazz));
        clone.body().invoke(ret, "setValue").arg(JExpr._this().invoke("getValue"));
        clone.body()._return(ret);

    }
}
