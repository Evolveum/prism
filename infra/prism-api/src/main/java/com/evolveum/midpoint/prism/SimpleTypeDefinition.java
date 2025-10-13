/*
 * Copyright (c) 2010-2017 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism;

import javax.xml.namespace.QName;

/**
 * Defines a simple (non-structured) definition.
 *
 * This interface currently serves primarily for enums.
 *
 * TODO document
 */
public interface SimpleTypeDefinition extends TypeDefinition {

    enum DerivationMethod {
        EXTENSION, RESTRICTION, SUBSTITUTION
    }

    QName getBaseTypeName();

    DerivationMethod getDerivationMethod();

    interface SimpleTypeDefinitionMutator {
        // base type and derivation method are immutable
    }

    interface SimpleTypeDefinitionBuilder
            extends SimpleTypeDefinitionMutator, TypeDefinitionLikeBuilder {

        SimpleTypeDefinition getObjectBuilt();
    }
}
