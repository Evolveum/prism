/*
 * Copyright (c) 2010-2019 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.util.annotation.Experimental;
import com.evolveum.prism.xml.ns._public.types_3.RawType;

/**
 *  Represents visitable JAXB bean.
 *
 *  EXPERIMENTAL. Consider merging with traditional prism Visitable.
 */
@Experimental
@FunctionalInterface
public interface JaxbVisitable {

    void accept(JaxbVisitor visitor);

    static void visitPrismStructure(JaxbVisitable visitable, Visitor prismVisitor) {
        if (visitable instanceof Containerable) {
            ((Containerable) visitable).asPrismContainerValue().accept(prismVisitor);
        } else if (visitable instanceof Referencable) {
            PrismObject<?> object = ((Referencable) visitable).asReferenceValue().getObject();
            if (object != null) {
                object.accept(prismVisitor);
            }
        } else if (visitable instanceof RawType) {
            RawType raw = (RawType) visitable;
            if (raw.isParsed()) {
                raw.getAlreadyParsedValue().accept(prismVisitor);
            } else {
                // Should we attempt to parse the raw value? Probably not, see the comment on RawType.accept(..)
            }
        }
    }
}
