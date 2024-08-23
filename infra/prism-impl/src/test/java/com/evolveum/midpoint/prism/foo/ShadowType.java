package com.evolveum.midpoint.prism.foo;

import com.evolveum.midpoint.prism.Objectable;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

import javax.xml.namespace.QName;

/**
 * Created by Dominik.
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "ShadowType", propOrder = {
        "resourceRef",
        "shadowLifecycleState",
        "purpose",
        "dead",
        "deathTimestamp",
        "pendingOperation",
        "synchronizationSituation",
        "synchronizationTimestamp",
        "fullSynchronizationTimestamp",
        "synchronizationSituationDescription",
        "correlation",
        "objectClass",
        "primaryIdentifierValue",
        "auxiliaryObjectClass",
        "kind",
        "intent",
        "tag",
        "protectedObject",
        "ignored",
        "assigned",
        "exists",
        "iteration",
        "iterationToken",
        "attributes",
        "referenceAttributes",
        "associations",
        "association",
        "activation",
        "credentials",
        "cachingMetadata"
})
public class ShadowType extends ObjectType
        implements Objectable {
    @Override
    protected QName prismGetContainerName() {
        return null;
    }

    @Override
    protected QName prismGetContainerType() {
        return null;
    }
}
