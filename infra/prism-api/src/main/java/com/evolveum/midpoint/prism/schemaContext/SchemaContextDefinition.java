package com.evolveum.midpoint.prism.schemaContext;

/**
 * Created by Dominik.
 */
public interface SchemaContextDefinition {
    String getTypePath();
    String getAlgorithm();

    void setTypePath(String typePath);
    void setAlgorithm(String algorithmName);
}
