package com.evolveum.prism.codegen.impl;

import com.evolveum.midpoint.prism.impl.binding.AbstractValueWrapper;
import com.evolveum.prism.codegen.binding.ValueWrappedContract;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
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
        return clazz;
    }

    @Override
    public void implement(ValueWrappedContract contract, JDefinedClass clazz) {
        // TODO Auto-generated method stub

    }
}
