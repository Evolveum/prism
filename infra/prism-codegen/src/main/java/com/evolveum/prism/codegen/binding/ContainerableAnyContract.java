/*
 * Copyright (c) 2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.codegen.binding;

import com.evolveum.midpoint.prism.ComplexTypeDefinition;

public class ContainerableAnyContract extends ContainerableContract {

    public ContainerableAnyContract(ComplexTypeDefinition typeDef, String packageName) {
        super(typeDef, packageName);
    }
}
