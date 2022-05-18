/*
 * Copyright (c) 2010-2015 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.query.builder;

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
}
