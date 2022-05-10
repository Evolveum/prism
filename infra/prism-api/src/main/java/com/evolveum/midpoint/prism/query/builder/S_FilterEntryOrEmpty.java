/*
 * Copyright (c) 2010-2015 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.query.builder;


import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.Containerable;
import com.evolveum.midpoint.prism.path.ItemPath;

public interface S_FilterEntryOrEmpty extends S_FilterEntry, S_FilterExit {

    default S_FilterEntryOrEmpty referencedBy(Class<? extends Containerable> clazz, Object... path) {
        return referencedBy(clazz, ItemPath.create(path), null);
    }
    default S_FilterEntryOrEmpty referencedBy(Class<? extends Containerable> clazz, ItemPath path) {
        return referencedBy(clazz, path, null);
    }

    S_FilterEntryOrEmpty referencedBy(Class<? extends Containerable> clazz, ItemPath path, QName relation);


}
