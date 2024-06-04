package com.evolveum.midpoint.prism.schemaContext;

import javax.xml.namespace.QName;

/**
 * Created by Dominik.
 */
public interface SchemaContextDefinition {

    QName getType();

    QName getTypePath();

    QName getAlgorithm();

    void setType(QName type);

    void setTypePath(QName typePath);

    void setAlgorithm(QName algorithmName);
}
