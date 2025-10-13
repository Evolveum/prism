/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.xnode;

import javax.xml.namespace.QName;

/**
 *  Temporary interface to allow modifying XNode representation. Hopefully it will be removed soon. DO NOT USE.
 */
public interface XNodeMutator {

    void putToMapXNode(MapXNode map, QName key, XNode value);

    void setXNodeType(XNode node, QName explicitTypeName, boolean explicitTypeDeclaration);

}
