/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.query;

import com.evolveum.prism.xml.ns._public.types_3.ObjectType;

public class TypedObjectQuery<T extends ObjectType> {

    private Class<T> objectClass;
    private ObjectQuery objectQuery;

    public TypedObjectQuery() {
    }

    public TypedObjectQuery(Class<T> objectClass, ObjectQuery objectQuery) {
        this.objectClass = objectClass;
        this.objectQuery = objectQuery;
    }

    public Class<T> getObjectClass() {
        return objectClass;
    }

    public void setObjectClass(Class<T> objectClass) {
        this.objectClass = objectClass;
    }

    public ObjectQuery getObjectQuery() {
        return objectQuery;
    }

    public void setObjectQuery(ObjectQuery objectQuery) {
        this.objectQuery = objectQuery;
    }
}
