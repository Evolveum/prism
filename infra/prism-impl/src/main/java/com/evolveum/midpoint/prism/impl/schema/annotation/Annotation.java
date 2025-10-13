/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.impl.schema.annotation;

import static com.evolveum.midpoint.prism.PrismConstants.*;
import static com.evolveum.midpoint.prism.impl.schema.features.DefinitionFeatures.XsomParsers.DF_DOCUMENTATION_PARSER;

import java.util.Collection;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.schemaContext.SchemaContextDefinition;

import com.sun.xml.xsom.XSAnnotation;

import com.evolveum.midpoint.prism.Definition.DefinitionMutator;
import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.impl.schema.features.DefinitionFeatures.XsomParsers;
import com.evolveum.midpoint.prism.impl.schema.features.QNameList;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.schema.DefinitionFeature;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.MiscUtil;
import com.evolveum.midpoint.util.exception.SchemaException;

/**
 * A specialization of a {@link DefinitionFeature}, such that:
 *
 * . the XSOM source being always {@link XSAnnotation},
 * . the list of these features is provided in the form of a Java enum ({@link Annotation}, i.e., a fixed list of values.
 *
 * It contains {@link AnnotationBasedFeature} instance (a subclass of {@link DefinitionFeature}) that carries out
 * practically all the processing.
 *
 * TODO think if we can somehow unify this with {@link DefinitionFeature}. It seems we could.
 */
public enum Annotation {

    ALWAYS_USE_FOR_EQUALS(AnnotationBasedFeature.custom(
            A_ALWAYS_USE_FOR_EQUALS, AlwaysUseForEquals.class,
            ItemDefinition.ItemDefinitionMutator.class,
            (target, alwaysUseForEquals) -> {
                if (alwaysUseForEquals == null) {
                    target.setAlwaysUseForEquals(true);
                    return;
                }

                if (alwaysUseForEquals.isUniversal()) {
                    target.setAlwaysUseForEquals(true);
                    return;
                }

                if (target instanceof PrismContainerDefinition<?> pcd) {

                    Collection<QName> itemNames = alwaysUseForEquals.getItemNames();

                    pcd.mutator().setAlwaysUseForEquals(itemNames);

                    // TODO TODO TODO - is this OK? What if we are modifying a shared definition?
                    for (QName itemName : itemNames) {
                        ItemDefinition<?> id =
                                MiscUtil.stateNonNull( // should be SchemaException but that's complicated now
                                        pcd.findItemDefinition(ItemPath.create(itemName)),
                                        "No definition for item '%s' in %s (referenced by alwaysUseForEquals annotation)",
                                        itemName, pcd);
                        id.mutator().setAlwaysUseForEquals(true);
                    }
                }
            },
            XsomParsers.DF_ALWAYS_USE_FOR_EQUALS_PARSER)),

    DEPRECATED(AnnotationBasedFeature.forBooleanMark(
            A_DEPRECATED, DefinitionMutator.class, DefinitionMutator::setDeprecated)),

    DEPRECATED_SINCE(AnnotationBasedFeature.forString(
            A_DEPRECATED_SINCE, PrismLifecycleDefinition.Mutable.class, PrismLifecycleDefinition.Mutable::setDeprecatedSince)),

    DISPLAY_NAME(AnnotationBasedFeature.forString(
            A_DISPLAY_NAME, PrismPresentationDefinition.Mutable.class, PrismPresentationDefinition.Mutable::setDisplayName)),

    DISPLAY_ORDER(AnnotationBasedFeature.forType(
            A_DISPLAY_ORDER, Integer.class,
            PrismPresentationDefinition.Mutable.class, PrismPresentationDefinition.Mutable::setDisplayOrder)),

    DOCUMENTATION(AnnotationBasedFeature.custom(
            DOMUtil.XSD_DOCUMENTATION_ELEMENT, String.class,
            PrismPresentationDefinition.Mutable.class, PrismPresentationDefinition.Mutable::setDocumentation,
            // Original parser takes arbitrary Object values, which does not fit here, hence the restrictToSource call.
            DF_DOCUMENTATION_PARSER.restrictToSource(XSAnnotation.class))),

    ELABORATE(AnnotationBasedFeature.forBooleanMark(
            A_ELABORATE, ItemDefinition.ItemDefinitionMutator.class, ItemDefinition.ItemDefinitionMutator::setElaborate)),

    @Deprecated
    EMPHASIZED(AnnotationBasedFeature.forBooleanMark(
            A_EMPHASIZED, PrismPresentationDefinition.Mutable.class, PrismPresentationDefinition.Mutable::setEmphasized)),

    DISPLAY_HINT(AnnotationBasedFeature.custom(
            A_DISPLAY_HINT, DisplayHint.class,
            PrismPresentationDefinition.Mutable.class, PrismPresentationDefinition.Mutable::setDisplayHint,
            XsomParsers.DF_DISPLAY_HINT_PARSER)),

    EXPERIMENTAL(AnnotationBasedFeature.forBooleanMark(
            A_EXPERIMENTAL, PrismLifecycleDefinition.Mutable.class, PrismLifecycleDefinition.Mutable::setExperimental)),

    HELP(AnnotationBasedFeature.forString(
            A_HELP, PrismPresentationDefinition.Mutable.class, PrismPresentationDefinition.Mutable::setHelp)),

    HETEROGENEOUS_LIST_ITEM(AnnotationBasedFeature.forBooleanMark(
            A_HETEROGENEOUS_LIST_ITEM, ItemDefinition.ItemDefinitionMutator.class, ItemDefinition.ItemDefinitionMutator::setHeterogeneousListItem)),

    IGNORE(AnnotationBasedFeature.forBooleanMark(
            A_IGNORE, ItemDefinition.ItemDefinitionMutator.class, ItemDefinition.ItemDefinitionMutator::setIgnored)),

    OBJECT_REFERENCE_TARGET_TYPE(AnnotationBasedFeature.custom(
            A_OBJECT_REFERENCE_TARGET_TYPE, QName.class,
            PrismReferenceDefinition.PrismReferenceDefinitionMutator.class, PrismReferenceDefinition.PrismReferenceDefinitionMutator::setTargetTypeName,
            XsomParsers.qName(A_OBJECT_REFERENCE_TARGET_TYPE).restrictToSource(XSAnnotation.class))),

    OPERATIONAL(AnnotationBasedFeature.forBooleanMark(
            A_OPERATIONAL, ItemDefinition.ItemDefinitionMutator.class, ItemDefinition.ItemDefinitionMutator::setOperational)),

    OPTIONAL_CLEANUP(AnnotationBasedFeature.forBooleanMark(
            A_OPTIONAL_CLEANUP, DefinitionMutator.class, DefinitionMutator::setOptionalCleanup)),

    PLANNED_REMOVAL(AnnotationBasedFeature.forString(
            A_PLANNED_REMOVAL, PrismLifecycleDefinition.Mutable.class, PrismLifecycleDefinition.Mutable::setPlannedRemoval)),

    PROCESSING(AnnotationBasedFeature.custom(
            A_PROCESSING, ItemProcessing.class,
            ItemDefinition.ItemDefinitionMutator.class, ItemDefinition.ItemDefinitionMutator::setProcessing,
            XsomParsers.enumBased(ItemProcessing.class, A_PROCESSING, ItemProcessing::getValue)
                    .restrictToSource(XSAnnotation.class))),

    REMOVED(AnnotationBasedFeature.forBooleanMark(
            A_REMOVED, PrismLifecycleDefinition.Mutable.class, PrismLifecycleDefinition.Mutable::setRemoved)),

    REMOVED_SINCE(AnnotationBasedFeature.forString(
            A_REMOVED_SINCE, PrismLifecycleDefinition.Mutable.class, PrismLifecycleDefinition.Mutable::setRemovedSince)),

    SEARCHABLE(AnnotationBasedFeature.forBooleanMark(
            A_SEARCHABLE, PrismItemStorageDefinition.Mutable.class, PrismItemStorageDefinition.Mutable::setSearchable)),

    MERGER(AnnotationBasedFeature.forString(
            A_MERGER, DefinitionMutator.class, DefinitionMutator::setMergerIdentifier)),

    NATURAL_KEY(AnnotationBasedFeature.custom(
            A_NATURAL_KEY, QNameList.class,
            DefinitionMutator.class, (def, qNameList) -> def.setNaturalKeyConstituents(QNameList.unwrap(qNameList)),
            XsomParsers.qNameList(A_NATURAL_KEY).restrictToSource(XSAnnotation.class))),

    SCHEMA_CONTEXT(AnnotationBasedFeature.custom(A_SCHEMA_CONTEXT, SchemaContextDefinition.class,
                    DefinitionMutator.class,
                    DefinitionMutator::setSchemaContextDefinition,
                    XsomParsers.schemaContextDefinitionParser())
    );

    /** This is the object that does all the work. */
    private final AnnotationBasedFeature<?, ?> definitionFeature;

    Annotation(AnnotationBasedFeature<?, ?> definitionFeature) {
        this.definitionFeature = definitionFeature;
    }

    public static void parseAllAnnotations(Object target, XSAnnotation annotation) throws SchemaException {
        for (Annotation a : Annotation.values()) {
            a.parseIfApplicable(target, annotation);
        }
    }

    public void parseIfApplicable(Object target, XSAnnotation xsAnnotation) throws SchemaException {
        //noinspection unchecked
        ((DefinitionFeature<Object, Object, XSAnnotation, ?>) definitionFeature).parseIfApplicable(target, xsAnnotation);
    }
}
