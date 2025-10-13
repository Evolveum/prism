/*
 * Copyright (c) 2010-2013 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.delta;

import com.evolveum.prism.xml.ns._public.types_3.ChangeTypeType;

/**
 * @author semancik
 *
 */
public enum ChangeType {
    ADD, MODIFY, DELETE;

    public static ChangeType toChangeType(ChangeTypeType changeType){

        if (changeType == null){
            return null;
        }

        switch (changeType){
        case ADD : return ChangeType.ADD;
        case DELETE : return ChangeType.DELETE;
        case MODIFY : return ChangeType.MODIFY;
        default : throw new IllegalArgumentException("Unknow change type: " + changeType);
        }
    }

    public static ChangeTypeType toChangeTypeType(ChangeType changeType){

        if (changeType == null) {
            return null;
        }

        switch (changeType) {
            case ADD : return ChangeTypeType.ADD;
            case DELETE : return ChangeTypeType.DELETE;
            case MODIFY : return ChangeTypeType.MODIFY;
            default : throw new IllegalArgumentException("Unknow change type: " + changeType);
        }
    }
}
