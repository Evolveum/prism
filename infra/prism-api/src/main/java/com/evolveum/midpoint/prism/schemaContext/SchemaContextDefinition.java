package com.evolveum.midpoint.prism.schemaContext;

import java.io.Serializable;
import javax.xml.namespace.QName;

/**
 * The interface provides Getter and Setter methods of possible attributes for definition the schema context annotation in xsd.
 *
 */
public interface SchemaContextDefinition extends Serializable {

    /**
     * Type object if directly defined.
     */
    QName getType();

    /**
     * Path of type object.
     */
    QName getTypePath();

    /**
     * Name of resolver for find attributes which the object depends.
     *
     * For example {@link ResourceObjectContextResolver}
     */
    QName getAlgorithm();

    void setType(QName type);

    void setTypePath(QName typePath);

    void setAlgorithm(QName algorithmName);
}
