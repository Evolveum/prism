package com.evolveum.midpoint.prism.schemaContext;

import javax.xml.namespace.QName;

/**
 * Created by Dominik.
 */
public interface SchemaContextDefinition {
    QName getTypePath();
    QName getAlgorithm();

    void setTypePath(QName typePath);
    void setAlgorithm(QName algorithmName);
}
