package com.evolveum.midpoint.prism.impl.schemaContext;

import com.evolveum.midpoint.prism.schemaContext.SchemaContextDefinition;

/**
 * Created by Dominik.
 */
public class SchemaContextDefinitionImpl implements SchemaContextDefinition {

    String typePath;
    String algorithmName;

    @Override
    public String getTypePath() {
        return typePath;
    }

    @Override
    public String getAlgorithm() {
        return algorithmName;
    }

    public void setTypePath(String typePath) {
        this.typePath = typePath;
    }

    @Override
    public void setAlgorithm(String algorithmName) {
        this.algorithmName = algorithmName;
    }
}
