/*
 * Copyright (c) 2021 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
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

    public abstract String fullyQualifiedName();
}
