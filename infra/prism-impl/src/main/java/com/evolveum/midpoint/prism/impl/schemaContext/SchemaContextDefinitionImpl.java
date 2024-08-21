package com.evolveum.midpoint.prism.impl.schemaContext;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.schema.SchemaLookup;
import com.evolveum.midpoint.prism.schemaContext.SchemaContextDefinition;

/**
 * Created by Dominik.
 */
public class SchemaContextDefinitionImpl implements SchemaContextDefinition {

    private QName type;

    private QName typePath;

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
    public QName getTypePath() {
        return typePath;
    }

    @Override
    public void setTypePath(QName typePath) {
        this.typePath = typePath;
    }

    public QName getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(QName algorithm) {
        this.algorithm = algorithm;
    }

}
