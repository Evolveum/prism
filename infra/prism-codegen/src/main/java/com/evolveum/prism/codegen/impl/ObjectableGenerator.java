/*
 * Copyright (c) 2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.codegen.impl;

import com.evolveum.midpoint.prism.Objectable;
import com.evolveum.prism.codegen.binding.ObjectableContract;
import com.evolveum.prism.codegen.binding.TypeBinding;
import com.sun.codemodel.ClassType;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;

public class ObjectableGenerator extends ContainerableGenerator<ObjectableContract> {

    public ObjectableGenerator(CodeGenerator codeGenerator) {
        super(codeGenerator);
    }

    @Override
    public void declare(ObjectableContract contract) throws JClassAlreadyExistsException {
        String name = contract.fullyQualifiedName();
        JDefinedClass clazz = codeModel()._class(name, ClassType.CLASS);

        if (contract.getSuperType() != null) {
            TypeBinding superType = bindingFor(contract.getSuperType());
            clazz._extends(codeModel().ref(superType.defaultBindingClass()));
        } else {
            clazz._implements(Objectable.class);
        }
    }

    @Override
    public void implement(ObjectableContract contract) {
        super.implement(contract);
    }

}
