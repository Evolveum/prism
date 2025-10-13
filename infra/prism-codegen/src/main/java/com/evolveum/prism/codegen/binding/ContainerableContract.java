/*
 * Copyright (c) 2021 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.prism.codegen.binding;

import com.evolveum.midpoint.prism.ComplexTypeDefinition;

public class ContainerableContract extends StructuredContract {

    public ContainerableContract(ComplexTypeDefinition typeDef, String packageName) {
        super(typeDef, packageName);
    }

}
