/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.query.builder;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.Containerable;
import com.evolveum.midpoint.prism.path.ItemPath;

public interface S_FilterEntry extends S_AtomicFilterEntry {

    S_AtomicFilterEntry not();

    default S_FilterEntryOrEmpty ownedBy(Class<? extends Containerable> clazz, Object... path) {
        return ownedBy(clazz, ItemPath.create(path));
    }

    S_FilterEntryOrEmpty ownedBy(Class<? extends Containerable> clazz, ItemPath path);

    default S_FilterEntryOrEmpty ownedBy(Class<? extends Containerable> clazz) {
        return ownedBy(clazz, (ItemPath) null);
    }

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
