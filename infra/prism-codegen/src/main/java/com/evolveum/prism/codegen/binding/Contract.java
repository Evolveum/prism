/*
 * Copyright (c) 2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.codegen.binding;

import javax.xml.namespace.QName;

public abstract class Contract {


    protected final String packageName;

    public Contract(String packageName) {
        this.packageName = packageName;
    }

    protected String className(QName name) {
        // Lets assume we use upper case format
        return name.getLocalPart();
    }

    protected abstract String fullyQualifiedName();
}
