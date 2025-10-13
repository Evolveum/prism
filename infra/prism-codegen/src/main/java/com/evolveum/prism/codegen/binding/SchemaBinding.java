/*
 * Copyright (c) 2022 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
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
