/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.query.builder;

import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.Containerable;
import com.evolveum.midpoint.prism.path.ItemPath;

public interface S_FilterEntryOrEmpty extends S_FilterEntry, S_FilterExit {

    /**
     * Shortcut to {@link #referencedBy(Class, ItemPath, QName)} with null relation which is interpreted as "any".
     * This is actually great for filtering where something not specified mostly implies "I don't care",
     * but this is different from an existing REF filter, where null relation defaults to "default" relation.
     * Eventually, REF filter should be changed to be more intuitive.
     */
    default S_FilterEntryOrEmpty referencedBy(
            @NotNull Class<? extends Containerable> clazz,
            @NotNull ItemPath path) {
        return referencedBy(clazz, path, null);
    }

    /**
     * Creates `referencedBy` filter that matches if the queried object is referenced by other specified object or container.
     * This is a "blocky" filter that allows for inner filter, applied to the referencing entity.
     *
     * For example:
     * ----
     * // query for role referenced by user assignment, with inner filter applied to user
     * .referencedBy(UserType.class, ItemPath.create(F_ASSIGNMENT, F_TARGET_REF))
     * .block()
     * .not().item(UserType.F_COST_CENTER).isNull()
     * .and()
     * .item(UserType.F_POLICY_SITUATION).isNull()
     * .endBlock()
     * ----
     *
     * @param clazz type of the referenced object (can be an abstract type too)
     * @param path item path of the reference
     * @param relation optional relation of the incoming reference, null means it does not matter (any)
     */
    S_FilterEntryOrEmpty referencedBy(
            @NotNull Class<? extends Containerable> clazz,
            @NotNull ItemPath path,
            QName relation);

    /**
     * Creates REF filter with no values - good when only the target filter is required.
     *
     * For example:
     * ----
     * filter.ref(ObjectType.F_PARENT_ORG_REF)
     * .item(OrgType.F_DISPLAY_ORDER).eq(30) // target filter
     * ----
     */
    default S_FilterEntryOrEmpty ref(ItemPath path) {
        return ref(path, null, null, new String[0]);
    }

    default S_FilterEntryOrEmpty ref(ItemPath path, QName targetType, QName relation) {
        return ref(path, targetType, relation, new String[0]);
    }

    S_FilterEntryOrEmpty ref(ItemPath path, QName targetType, QName relation, String... oid);
}
