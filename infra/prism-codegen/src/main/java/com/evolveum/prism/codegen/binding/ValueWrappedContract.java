/*
 * Copyright (c) 2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.codegen.binding;

import com.evolveum.midpoint.prism.TypeDefinition;

public class ValueWrappedContract extends Contract {

    private TypeDefinition typeDefinition;

    public ValueWrappedContract(TypeDefinition typeDef, String packageName) {
        super(packageName);
        this.typeDefinition = typeDef;
    }

    @Override
    public String fullyQualifiedName() {
        return packageName + "." + typeDefinition.getTypeName().getLocalPart();
    }

    public TypeDefinition getTypeDefinition() {
        return typeDefinition;
    }
}
