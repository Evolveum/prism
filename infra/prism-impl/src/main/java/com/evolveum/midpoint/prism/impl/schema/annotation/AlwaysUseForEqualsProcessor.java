/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.schema.annotation;

import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.MutableItemDefinition;
import com.evolveum.midpoint.prism.MutablePrismContainerDefinition;
import com.evolveum.midpoint.prism.PrismConstants;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.exception.SchemaException;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.List;

public class AlwaysUseForEqualsProcessor extends AnnotationProcessor<MutableItemDefinition<?>, QName> {

    public AlwaysUseForEqualsProcessor() {
        super(PrismConstants.A_ALWAYS_USE_FOR_EQUALS, QName.class, ItemDefinition.class, null, null);
    }

    @Override
    public void process(@NotNull MutableItemDefinition<?> definition, @NotNull List<Element> elements) throws SchemaException {
        if (elements.isEmpty()) {
            return;
        }

        if (elements.size() == 1 && elements.get(0).getTextContent().isEmpty()) {
            definition.setAlwaysUseForEquals(true);
            definition.setAnnotation(PrismConstants.A_ALWAYS_USE_FOR_EQUALS, null);

            return;
        }

        if (definition instanceof MutablePrismContainerDefinition<?> pcd) {
            List<QName> qnames = elements.stream()
                    .map(DOMUtil::getQNameValue)
                    .filter(qname -> qname != null)
                    .toList();

            pcd.setAlwaysUseForEquals(qnames);

            for (QName qname : qnames) {
                ItemDefinition<?> id = pcd.findItemDefinition(ItemPath.create(qname));
                if (id instanceof MutableItemDefinition<?> mid) {
                    mid.setAlwaysUseForEquals(true);
                }
            }

            pcd.setAnnotation(PrismConstants.A_ALWAYS_USE_FOR_EQUALS, qnames);
        }
    }
}
