package com.evolveum.midpoint.prism.impl.schemaContext;

import com.evolveum.midpoint.prism.schemaContext.SchemaContextDefinition;

import javax.xml.namespace.QName;

/**
 * Created by Dominik.
 */
public class SchemaContextDefinitionImpl implements SchemaContextDefinition {

    QName typePath;
    QName algorithmName;

    @Override
    public QName getTypePath() {
        return typePath;
    }

    @Override
    public QName getAlgorithm() {
        return algorithmName;
    }

    public void setTypePath(QName typePath) {
        this.typePath = typePath;
    }

    @Override
    public void setAlgorithm(QName algorithmName) {
        this.algorithmName = algorithmName;
    }
}
