/*
 * Copyright (c) 2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.codegen.binding;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.ComplexTypeDefinition;

public class ObjectableContract extends ContainerableContract {

    private QName containerName;

    public ObjectableContract(ComplexTypeDefinition typeDef, String packageName) {
        super(typeDef, packageName);
        typeDef.getTypeName();
    }

    public QName containerName() {
        return containerName;
    }

    void setContainerName(QName containerName) {
        this.containerName = containerName;
    }





}
