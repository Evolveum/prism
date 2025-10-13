/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.impl;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.Hacks;
import com.evolveum.midpoint.prism.ParsingContext;
import com.evolveum.midpoint.prism.impl.marshaller.XNodeProcessorUtil;
import com.evolveum.midpoint.prism.impl.xnode.MapXNodeImpl;
import com.evolveum.midpoint.prism.impl.xnode.XNodeImpl;
import com.evolveum.midpoint.prism.xnode.*;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.prism.xml.ns._public.types_3.ProtectedDataType;

/**
 * TEMPORARY
 */
public class HacksImpl implements Hacks, XNodeMutator {

    @Override
    public void putToMapXNode(MapXNode map, QName key, XNode value) {
        ((MapXNodeImpl) map).put(key, (XNodeImpl) value);
    }

    @Override
    public <T> void parseProtectedType(ProtectedDataType<T> protectedType, MapXNode xmap, ParsingContext pc) throws SchemaException {
        XNodeProcessorUtil.parseProtectedType(protectedType, (MapXNodeImpl) xmap, pc);
    }

    @Override
    public void setXNodeType(XNode node, QName explicitTypeName, boolean explicitTypeDeclaration) {
        ((XNodeImpl) node).setTypeQName(explicitTypeName);
        ((XNodeImpl) node).setExplicitTypeDeclaration(explicitTypeDeclaration);
    }
}
