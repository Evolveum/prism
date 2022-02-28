/*
 * Copyright (c) 2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.codegen.binding;

import javax.lang.model.SourceVersion;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.util.DOMUtil;

import static com.google.common.base.CaseFormat.*;

public class ItemBinding {

    private final ItemDefinition<?> definition;
    private final QName qName;
    private final String name;
    private final boolean attribute;

    public ItemBinding(String name, ItemDefinition<?> def, boolean attribute) {
        this.name = name;
        this.qName = def.getItemName();
        this.definition = def;
        this.attribute = attribute;
    }

    public String getJavaName() {
        return name;
    }

    public QName getQName() {
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
        if (isBoolean(definition)) {
            return StructuredContract.IS_PREFIX + name;
        }
        return StructuredContract.GET_PREFIX + name;
    }

    private boolean isBoolean(ItemDefinition<?> definition) {
        return DOMUtil.XSD_BOOLEAN.equals(definition.getTypeName());
    }

    public String constantName() {
        // FIXME: This is needed for few cases where in model, we use OID insted of Oid
        var mod =  name.contains("OID") ? name.replace("OID", "Oid") : name;
        return UPPER_CAMEL.to(UPPER_UNDERSCORE, mod);
    }

    public QName itemName() {
        return qName;
    }

    public String setterName() {
        return StructuredContract.SET_PREFIX + name;
    }

    public String fieldName() {
        String maybe = UPPER_CAMEL.to(LOWER_CAMEL, name);
        if (SourceVersion.isKeyword(maybe)) {
            maybe = "_" + maybe;
        }
        return maybe;
    }

    public boolean isRaw() {
        return DOMUtil.XSD_ANYTYPE.equals(definition.getTypeName());
    }

    public boolean isAttribute() {
        return attribute;
    }
}
