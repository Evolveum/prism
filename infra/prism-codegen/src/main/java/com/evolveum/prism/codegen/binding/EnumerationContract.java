/*
 * Copyright (c) 2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.codegen.binding;

import java.util.Collection;
import java.util.Optional;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.EnumerationTypeDefinition;
import com.evolveum.midpoint.prism.EnumerationTypeDefinition.ValueDefinition;

public class EnumerationContract extends Contract {

    private EnumerationTypeDefinition typeDefinition;

    public EnumerationContract(EnumerationTypeDefinition typeDefinition, String packageName) {
        super(packageName);
        this.typeDefinition = typeDefinition;
    }

    @Override
    public String fullyQualifiedName() {
        return packageName + "." + typeDefinition.getTypeName().getLocalPart();
    }

    public Collection<ValueDefinition> values() {
        return typeDefinition.getValues();
    }

    public QName getQName() {
        return typeDefinition.getTypeName();
    }

    public Optional<String> getDocumentation() {
        return Optional.ofNullable(typeDefinition.getDocumentation());
    }

}
