/*
 * Copyright (c) 2010-2013 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism;

import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;

/**
 * @author semancik
 *
 */
public interface Objectable extends Containerable {

    @Safe // not executable, marking just intent
    String getOid();

    void setOid(String oid);

    @Safe // not executable, marking just intent
    String getVersion();

    void setVersion(String version);

    @Safe // not executable, marking just intent
    PolyStringType getName();

    void setName(PolyStringType name);

    @Safe // not executable, marking just intent
    String getDescription();

    void setDescription(String description);

    /**
     * Returns short string representing identity of this object.
     * It should container object type, OID and name. It should be presented
     * in a form suitable for log and diagnostic messages (understandable for
     * system administrator).
     */
    @Safe // not executable, marking just intent
    String toDebugName();

    /**
     * Returns short string identification of object type. It should be in a form
     * suitable for log messages. There is no requirement for the type name to be unique,
     * but it rather has to be compact. E.g. short element names are preferred to long
     * QNames or URIs.
     * @return
     */
    @Safe // not executable, marking just intent
    String toDebugType();

    PrismObject asPrismObject();

    void setupContainer(PrismObject object);
}
