/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.impl.schema.features;

import static com.evolveum.midpoint.prism.PrismConstants.*;
import static com.evolveum.midpoint.prism.impl.schema.SchemaProcessorUtil.getAnnotationElements;

import com.evolveum.midpoint.prism.annotation.DiagramElementFormType;
import com.evolveum.midpoint.prism.annotation.DiagramElementInclusionType;
import com.evolveum.midpoint.prism.annotation.ItemDiagramSpecification;
import com.evolveum.midpoint.prism.impl.schema.features.ItemDiagramSpecificationXsomParser.ItemDiagramSpecifications;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import com.evolveum.midpoint.prism.schema.DefinitionFeatureParser;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.MiscUtil;
import com.evolveum.midpoint.util.exception.SchemaException;

import java.util.ArrayList;
import java.util.List;

public class ItemDiagramSpecificationXsomParser implements DefinitionFeatureParser<ItemDiagramSpecifications, Object> {

    private static final ItemDiagramSpecificationXsomParser INSTANCE = new ItemDiagramSpecificationXsomParser();

    public static ItemDiagramSpecificationXsomParser instance() {
        return INSTANCE;
    }

    @Override
    public @Nullable ItemDiagramSpecifications getValue(@Nullable Object source) throws SchemaException {
        ArrayList<ItemDiagramSpecification> diagrams = new ArrayList<>();
        for (Element schemaMigrationElement : getAnnotationElements(source, A_DIAGRAM)) {
            diagrams.add(parseDiagram(schemaMigrationElement));
        }
        return ItemDiagramSpecifications.wrap(diagrams);
    }

    private ItemDiagramSpecification parseDiagram(Element diagramElement) throws SchemaException {
        Element nameElement =
                MiscUtil.requireNonNull(
                        DOMUtil.getChildElement(diagramElement, A_DIAGRAM_NAME),
                        "Missing diagram name");

        Element formElement = DOMUtil.getChildElement(diagramElement, A_DIAGRAM_FORM);
        DiagramElementFormType form =
                formElement != null ? DiagramElementFormType.parse(StringUtils.trim(formElement.getTextContent())) : null;

        Element inclusionElement = DOMUtil.getChildElement(diagramElement, A_DIAGRAM_INCLUSION);
        DiagramElementInclusionType inclusion = inclusionElement != null ?
                DiagramElementInclusionType.parse(StringUtils.trim(inclusionElement.getTextContent())) : null;

        Element subitemInclusionElement = DOMUtil.getChildElement(diagramElement, A_DIAGRAM_SUBITEM_INCLUSION);
        DiagramElementInclusionType subItemInclusion = subitemInclusionElement != null ?
                DiagramElementInclusionType.parse(StringUtils.trim(subitemInclusionElement.getTextContent())) : null;

        String name = StringUtils.trim(nameElement.getTextContent());
        return new ItemDiagramSpecification(name, form, inclusion, subItemInclusion);
    }

    /** Just the value holder to ensure type safety. */
    public static class ItemDiagramSpecifications
            extends AbstractValueWrapper.ForList<ItemDiagramSpecification> {

        ItemDiagramSpecifications(List<ItemDiagramSpecification> values) {
            super(values);
        }

        static @Nullable List<ItemDiagramSpecification> unwrap(@Nullable ItemDiagramSpecifications wrapper) {
            return wrapper != null ? wrapper.getValue() : null;
        }

        static @Nullable ItemDiagramSpecifications wrap(@Nullable List<ItemDiagramSpecification> values) {
            return values != null && !values.isEmpty() ? new ItemDiagramSpecifications(values) : null;
        }
    }
}
