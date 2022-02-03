/*
 * Copyright (c) 2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.codegen.binding;

import com.evolveum.midpoint.prism.ComplexTypeDefinition;

public class ObjectableContract extends ContainerableContract {

    public ObjectableContract(ComplexTypeDefinition typeDef) {
        super(typeDef);
        typeDef.getTypeName();
    }





}
