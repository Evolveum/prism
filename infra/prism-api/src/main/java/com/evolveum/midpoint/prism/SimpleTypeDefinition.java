/*
 * Copyright (c) 2010-2017 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
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
}
