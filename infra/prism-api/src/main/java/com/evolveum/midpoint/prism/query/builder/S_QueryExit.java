/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.query.builder;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.query.ObjectFilter;
import com.evolveum.midpoint.prism.query.ObjectQuery;

/**
 * See the grammar in Javadoc for {@code QueryBuilder}.
 */
public interface S_QueryExit {

    /**
     * Closes the {@link S_FilterEntry#block()} construction.
     * It is a bit high in hierarchy to allow empty block().end() construction without additional interface.
     */
    S_FilterExit endBlock();

    S_QueryExit asc(QName... names);
    S_QueryExit asc(ItemPath path);
    S_QueryExit desc(QName... names);
    S_QueryExit desc(ItemPath path);
    S_QueryExit offset(Integer n);
    S_QueryExit maxSize(Integer n);

    ObjectQuery build();
    ObjectFilter buildFilter();

}
