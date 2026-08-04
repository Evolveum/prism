/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.impl.schema;

import com.evolveum.midpoint.prism.impl.xjc.JaxbCustomizationConstants;

/**
 * Namespaces that can appear in generated schema annotations/customizations, but should NOT be emitted as XSD imports.
 */
enum NonImportableSchemaNamespace {

    JAKARTA_JAXB(JaxbCustomizationConstants.JAKARTA_NAMESPACE),
    LEGACY_JAXB(JaxbCustomizationConstants.LEGACY_NAMESPACE);

    private final String namespace;

    NonImportableSchemaNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * Returns true if the namespace should be emitted as an XSD import.
     */
    static boolean isImportable(String namespace) {
        for (NonImportableSchemaNamespace nonImportable : values()) {
            if (nonImportable.namespace.equals(namespace)) {
                return false;
            }
        }
        return true;
    }
}
