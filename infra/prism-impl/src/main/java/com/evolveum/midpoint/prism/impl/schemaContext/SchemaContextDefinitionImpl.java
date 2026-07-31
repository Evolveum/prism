/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.impl.schemaContext;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.schemaContext.SchemaContextDefinition;

public class SchemaContextDefinitionImpl implements SchemaContextDefinition {

    private QName type;

    private ItemPath typePath;

    private ItemPath path;

    private QName algorithm;

    @Override
    public QName getType() {
        return type;
    }

    @Override
    public void setType(QName type) {
        this.type = type;
    }

    @Override
    public ItemPath getTypePath() {
        return typePath;
    }

    @Override
    public void setTypePath(ItemPath typePath) {
        this.typePath = typePath;
    }

    public QName getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(QName algorithm) {
        this.algorithm = algorithm;
    }

    @Override
    public void setPath(ItemPath path) {
        this.path = path;
    }

    @Override
    public ItemPath getPath() {
        return path;
    }
}
