/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.query.builder;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.path.ItemPath;

/**
 * See the grammar in Javadoc for {@code QueryBuilder}.
 */
public interface S_RightHandItemEntry {

    // TODO add support for matching rules
    S_FilterExit item(QName... names);
    S_FilterExit item(ItemPath itemPath, ItemDefinition<?> itemDefinition);
}
