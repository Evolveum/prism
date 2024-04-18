/*
 * Copyright (c) 2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.prism.schema.SerializableDefinition;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;

/**
 * Simple type, which behaves as enumeration in most modeling languages.
 *
 * Enumeration has restricted value set - only declared value can be used.
 */
public interface EnumerationTypeDefinition
        extends SimpleTypeDefinition, SerializableDefinition {

    /**
     * Returns definition of enumeration values
     *
     * @return definitions of enumeration values
     */
    Collection<ValueDefinition> getValues();

    /**
     * Definition of enumeration value
     */
    interface ValueDefinition extends Serializable {

        String getValue();
        Optional<String> getDocumentation();
        Optional<String> getConstantName();
    }
}
