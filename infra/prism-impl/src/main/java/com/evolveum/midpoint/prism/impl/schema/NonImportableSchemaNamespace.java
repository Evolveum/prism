/*
 * Copyright (C) 2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.impl.schema;

/**
 * Namespaces that can appear in generated schema annotations/customizations, but should NOT be emitted as XSD imports.
 */
enum NonImportableSchemaNamespace {

    JAKARTA_JAXB("https://jakarta.ee/xml/ns/jaxb"),
    LEGACY_JAXB("http://java.sun.com/xml/ns/jaxb");

    private final String namespace;

    NonImportableSchemaNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * Returns true if the namespace should be emitted as an XSD import.
     */
    static boolean isImportable(String namespace) {
        return !JAKARTA_JAXB.namespace.equals(namespace)
                && !LEGACY_JAXB.namespace.equals(namespace);
    }
}
