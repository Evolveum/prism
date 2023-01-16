/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.util;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.util.MiscUtil;
import com.evolveum.midpoint.util.QNameUtil;

public class DefinitionUtil {
    public static final String MULTIPLICITY_UNBOUNDED = "unbounded";

    public static Integer parseMultiplicity(String stringMultiplicity) {
        if (stringMultiplicity == null) {
            return null;
        }
        if (stringMultiplicity.equals(MULTIPLICITY_UNBOUNDED)) {
            return -1;
        }
        return Integer.parseInt(stringMultiplicity);
    }

    // add namespace from the definition if it's safe to do so
    public static QName addNamespaceIfApplicable(QName name, QName definitionName) {
        if (StringUtils.isEmpty(name.getNamespaceURI())) {
            if (QNameUtil.match(name, definitionName)) {
                return definitionName;
            }
        }
        return name;
    }

    @Nullable
    public static <ID extends ItemDefinition<?>> ID findItemDefinition(
            @NotNull ItemDefinitionResolver itemDefinitionResolver,
            @NotNull Class<? extends Containerable> currentClass,
            @NotNull ItemPath itemPath,
            @NotNull Class<ID> type) {
        ItemDefinition<?> definition = itemDefinitionResolver.findItemDefinition(currentClass, itemPath);
        if (definition != null) {
            if (type.isAssignableFrom(definition.getClass())) {
                //noinspection unchecked
                return (ID) definition;
            } else {
                throw new IllegalArgumentException(
                        String.format("Expected definition of type %s but got %s; for path '%s'",
                                type.getSimpleName(), definition.getClass().getSimpleName(), itemPath));
            }
        }
        return null;
    }

    public static @Nullable <ID extends ItemDefinition<?>> ID findItemDefinition(
            @NotNull PrismContext prismContext,
            @NotNull Class<? extends Containerable> currentClass,
            @NotNull ItemPath itemPath,
            @NotNull Class<ID> type) {
        ComplexTypeDefinition ctd =
                MiscUtil.argNonNull(
                        prismContext.getSchemaRegistry().findComplexTypeDefinitionByCompileTimeClass(currentClass),
                        () -> "Definition for " + currentClass + " couldn't be found");
        return ctd.findItemDefinition(itemPath, type);
    }

    public static @NotNull <ID extends ItemDefinition<?>> ID findItemDefinitionMandatory(
            @NotNull PrismContext prismContext,
            @NotNull Class<? extends Containerable> currentClass,
            @NotNull ItemPath itemPath,
            @NotNull Class<ID> type) {
        return MiscUtil.argNonNull(
                findItemDefinition(prismContext, currentClass, itemPath, type),
                () -> String.format(
                        "Item path of '%s' in %s does not point to a valid %s",
                        itemPath, currentClass.getSimpleName(), type.getSimpleName()));
    }
}
