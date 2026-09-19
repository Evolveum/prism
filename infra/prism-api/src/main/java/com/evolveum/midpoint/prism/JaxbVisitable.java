/*
 * Copyright (c) 2010-2019 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.util.annotation.Experimental;
import com.evolveum.prism.xml.ns._public.types_3.RawType;
import jakarta.xml.bind.JAXBElement;

import java.util.Collection;

/**
 *  Represents visitable JAXB bean.
 *
 *  EXPERIMENTAL. Consider merging with traditional prism Visitable.
 */
@Experimental
@FunctionalInterface
public interface JaxbVisitable {

    static void accept(Object object, JaxbVisitor visitor) {
        if (object instanceof JaxbVisitable visitable) {
            visitable.accept(visitor);
        } else if (object instanceof Collection<?> collection) {
            for (Object item : collection) {
                accept(item, visitor);
            }
        } else if (object instanceof JAXBElement<?> element) {
            accept(element.getValue(), visitor);
        }
    }

    void accept(JaxbVisitor visitor);

    static void visitPrismStructure(JaxbVisitable visitable, Visitor prismVisitor) {
        if (visitable instanceof Containerable containerable) {
            containerable.asPrismContainerValue().accept(prismVisitor);
        } else if (visitable instanceof Referencable referencable) {
            PrismObject<?> object = referencable.asReferenceValue().getObject();
            if (object != null) {
                object.accept(prismVisitor);
            }
        } else if (visitable instanceof RawType raw) {
            if (raw.isParsed()) {
                raw.getAlreadyParsedValue().accept(prismVisitor);
            } else {
                // Should we attempt to parse the raw value? Probably not, see the comment on RawType.accept(..)
            }
        }
    }
}
