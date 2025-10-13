/*
 * Copyright (c) 2022 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.prism.codegen.binding;

import com.evolveum.midpoint.prism.ComplexTypeDefinition;

public class ContainerableAnyContract extends ContainerableContract {

    public ContainerableAnyContract(ComplexTypeDefinition typeDef, String packageName) {
        super(typeDef, packageName);
    }
}
