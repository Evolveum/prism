/*
 * Copyright (c) 2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.codegen.binding;

import com.evolveum.midpoint.prism.ComplexTypeDefinition;

public class ReferenceContract extends StructuredContract {

    public ReferenceContract(ComplexTypeDefinition typeDefinition, String packageName) {
        super(typeDefinition, packageName);
    }

}
