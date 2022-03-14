/*
 * Copyright (c) 2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.codegen.binding;

public class SchemaBinding extends Binding {

    public static final String OBJECT_FACTORY = "ObjectFactory";
    private final String namespace;
    private final String packageName;

    public SchemaBinding(String namespace, String packageName) {
        this.namespace = namespace;
        this.packageName = packageName;
    }


    public String getPackageName() {
        return packageName;
    }

    @Override
    public String getNamespaceURI() {
        return namespace;
    }
}
