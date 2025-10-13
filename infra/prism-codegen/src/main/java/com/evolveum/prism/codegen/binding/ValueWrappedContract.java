/*
 * Copyright (c) 2022 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
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
