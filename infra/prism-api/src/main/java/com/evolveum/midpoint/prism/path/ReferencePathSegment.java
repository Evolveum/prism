/*
 * Copyright (c) 2010-2015 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.path;

import java.util.Optional;

import javax.xml.namespace.QName;

/**
 * Denotes reference path segment: either ".." meaning owner, or "@" meaning referenced object.
 * (Note that these are to be used only in filters and order instructions, for now.)
 */
public abstract class ReferencePathSegment extends ItemPathSegment {

    public Optional<QName> typeHint() {
        return Optional.empty();
    }
}
