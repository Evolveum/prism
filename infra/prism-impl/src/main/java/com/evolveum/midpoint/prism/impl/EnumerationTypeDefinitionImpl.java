/*
 * Copyright (c) 2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.EnumerationTypeDefinition;
import com.google.common.collect.ImmutableList;

public class EnumerationTypeDefinitionImpl extends SimpleTypeDefinitionImpl implements EnumerationTypeDefinition {

    private static final long serialVersionUID = -4026772475698851565L;

    private final List<ValueDefinition> values;

    public EnumerationTypeDefinitionImpl(QName typeName, QName baseTypeName, List<ValueDefinition> values) {
        super(typeName, baseTypeName, DerivationMethod.RESTRICTION);
        this.values = ImmutableList.copyOf(values);
    }

    @Override
    public Collection<ValueDefinition> getValues() {
        return values;
    }

    @NotNull
    @Override
    public SimpleTypeDefinitionImpl clone() {
        SimpleTypeDefinitionImpl clone = new EnumerationTypeDefinitionImpl(typeName, getBaseTypeName(), values);
        clone.copyDefinitionDataFrom(this);
        return clone;
    }

    public static class ValueDefinitionImpl implements ValueDefinition {

        private final String value;
        private final String documentation;
        private final String constantName;

        public ValueDefinitionImpl(String value, String documentation, String constantName) {

            this.value = value;
            this.documentation = documentation;
            this.constantName = constantName;
        }

        @Override
        public Optional<String> getDocumentation() {
            return Optional.ofNullable(this.documentation);
        }

        @Override
        public String getValue() {
            return this.value;
        }

        @Override
        public Optional<String> getConstantName() {
            return Optional.ofNullable(constantName);
        }



    }

}
