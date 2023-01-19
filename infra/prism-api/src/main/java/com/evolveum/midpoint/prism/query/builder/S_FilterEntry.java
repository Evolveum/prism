/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.query.builder;

import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.query.ObjectFilter;
import com.evolveum.midpoint.prism.query.OrgFilter;
import com.evolveum.midpoint.util.annotation.Experimental;

/**
 * See the grammar in Javadoc for {@code QueryBuilder}.
 */
public interface S_FilterEntry {

    S_FilterEntry not();
    S_FilterExit all();
    S_FilterExit none();
    S_FilterExit undefined();
    S_FilterExit filter(ObjectFilter filter); // not much tested, use with care
    S_ConditionEntry item(QName... names);
    S_ConditionEntry item(String... names);
    S_ConditionEntry item(ItemPath path);
    S_ConditionEntry item(ItemPath itemPath, ItemDefinition<?> itemDefinition);

    @Experimental
    S_ConditionEntry itemWithDef(ItemDefinition<?> itemDefinition, QName... names);

    S_ConditionEntry item(PrismContainerDefinition<?> containerDefinition, QName... names);
    S_ConditionEntry item(PrismContainerDefinition<?> containerDefinition, ItemPath itemPath);

    @Experimental
    S_MatchingRuleEntry itemAs(PrismProperty<?> property); // TODO choose better name for this method

    S_FilterExit id(String... identifiers);
    S_FilterExit id(long... identifiers);
    S_FilterExit ownerId(String... identifiers);
    S_FilterExit ownerId(long... identifiers);
    S_FilterExit isDirectChildOf(PrismReferenceValue value);
    S_FilterExit isChildOf(PrismReferenceValue value);
    S_FilterExit isDirectChildOf(String oid);
    S_FilterExit isChildOf(String oid);
    S_FilterExit isParentOf(PrismReferenceValue value); // reference should point to OrgType
    S_FilterExit isParentOf(String oid); // oid should be of an OrgType
    S_FilterExit isInScopeOf(String oid, OrgFilter.Scope scope);
    S_FilterExit isInScopeOf(PrismReferenceValue value, OrgFilter.Scope scope);
    S_FilterExit isRoot();
    S_FilterExit fullText(String... words);
    S_FilterEntryOrEmpty block();
    S_FilterEntryOrEmpty type(Class<? extends Containerable> type);
    S_FilterEntryOrEmpty type(QName type);
    S_FilterEntryOrEmpty exists(Object... components);

    default S_FilterEntryOrEmpty ownedBy(Class<? extends Containerable> clazz, Object... path) {
        return ownedBy(clazz, ItemPath.create(path));
    }

    S_FilterEntryOrEmpty ownedBy(Class<? extends Containerable> clazz, ItemPath path);

    default S_FilterEntryOrEmpty ownedBy(Class<? extends Containerable> clazz) {
        return ownedBy(clazz, (ItemPath) null);
    }

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
     * Ref filter with no values with optional ref-target filter that can follow this call immediately.
     *
     * For example:
     * ----
     * filter.ref(ObjectType.F_PARENT_ORG_REF)
     * .item(OrgType.F_DISPLAY_ORDER).eq(30) // target filter
     * ----
     *
     * Use combo {@link #item(ItemPath)} and {@link S_ConditionEntry#ref(PrismReferenceValue...)}
     * for simple REF filters and multi-value support.
     */
    default S_FilterEntryOrEmpty ref(ItemPath path) {
        return ref(path, null, null, new String[0]);
    }

    /**
     * Ref filter for target type and relation with optional ref-target filter that can follow this call immediately.
     *
     * Use combo {@link #item(ItemPath)} and {@link S_ConditionEntry#ref(PrismReferenceValue...)}
     * for simple REF filters and multi-value support.
     */
    default S_FilterEntryOrEmpty ref(ItemPath path, QName targetType, QName relation) {
        return ref(path, targetType, relation, new String[0]);
    }

    /**
     * Ref filter with single value and optional ref-target filter that can follow this call immediately.
     *
     * Use combo {@link #item(ItemPath)} and {@link S_ConditionEntry#ref(PrismReferenceValue...)}
     * for simple REF filters and multi-value support.
     */
    S_FilterEntryOrEmpty ref(ItemPath path, QName targetType, QName relation, String... oid);
}
