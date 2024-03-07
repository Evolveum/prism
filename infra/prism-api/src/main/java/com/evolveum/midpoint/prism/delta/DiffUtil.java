/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.delta;

import java.io.File;
import java.io.IOException;

import com.evolveum.midpoint.prism.Objectable;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.util.exception.SchemaException;

/**
 * @author semancik
 *
 */
public class DiffUtil {

    public static <T extends Objectable> ObjectDelta<T> diff(PrismObject<T> oldObject, PrismObject<T> newObject) {
        if (oldObject == null) {
            if (newObject == null) {
                return null;
            }
            ObjectDelta<T> objectDelta =
                    PrismContext.get().deltaFactory().object().create(newObject.getCompileTimeClass(), ChangeType.ADD);
            objectDelta.setOid(newObject.getOid());
            objectDelta.setObjectToAdd(newObject);
            return objectDelta;
        } else {
            return oldObject.diff(newObject);
        }
    }

    public static <T extends Objectable> ObjectDelta<T> diff(T oldObjectType, T newObjectType) throws SchemaException {
        PrismObject<T> oldObject = null;
        if (oldObjectType != null) {
            oldObject = oldObjectType.asPrismObject();
        }
        PrismObject<T> newObject = null;
        if (newObjectType != null) {
            newObject = newObjectType.asPrismObject();
        }
        return diff(oldObject, newObject);
    }


    public static <T extends Objectable> ObjectDelta<T> diff(String oldXml, String newXml, Class<T> type) throws SchemaException {
        PrismObject<T> oldObject = null;
        if (oldXml != null) {
            oldObject = PrismContext.get().parseObject(oldXml);
        }
        PrismObject<T> newObject = null;
        if (newXml != null) {
            newObject = PrismContext.get().parseObject(newXml);
        }
        return diff(oldObject, newObject);
    }

    public static <T extends Objectable> ObjectDelta<T> diff(File oldXmlFile, File newXmlFile, Class<T> type) throws SchemaException, IOException {
        PrismObject<T> oldObject = null;
        if (oldXmlFile != null) {
            oldObject = PrismContext.get().parseObject(oldXmlFile);
        }
        PrismObject<T> newObject = null;
        if (newXmlFile != null) {
            newObject = PrismContext.get().parseObject(newXmlFile);
        }
        return diff(oldObject, newObject);
    }

}
