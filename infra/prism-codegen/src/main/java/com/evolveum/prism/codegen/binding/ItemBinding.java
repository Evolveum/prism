/*
 * Copyright (c) 2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.codegen.binding;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.ItemDefinition;

import static com.google.common.base.CaseFormat.*;

public class ItemBinding {

    private ItemDefinition<?> definition;
    private QName qName;
    private String name;

    public ItemBinding(String name, ItemDefinition<?> def) {
        this.name = name;
        this.qName = def.getItemName();
        this.definition = def;
    }

    String getJavaName() {
        return name;
    }

    QName getQName() {
        return qName;
    }

    public ItemDefinition<?> getDefinition() {
        return definition;
    }

    public boolean isList() {
        return definition.getMaxOccurs() == -1 ||
               definition.getMaxOccurs() > 1;
    }

    public String getterName() {
        return StructuredContract.GET_PREFIX + name;
    }

    public String constantName() {
        return UPPER_CAMEL.to(UPPER_UNDERSCORE, name);
    }

    public QName itemName() {
        return qName;
    }

    public String setterName() {
        return StructuredContract.SET_PREFIX + name;
    }

    public String fieldName() {
        return UPPER_CAMEL.to(LOWER_CAMEL, name);
    }
}
