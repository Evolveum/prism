/*
 * Copyright (c) 2010-2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism;

import java.io.Serializable;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.annotation.ItemDiagramSpecification;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.util.DebugDumpable;
import com.evolveum.midpoint.util.annotation.Experimental;

import org.jetbrains.annotations.Nullable;

/**
 * Common interface to access all definitions.
 */
public interface Definition
        extends PrismContextSensitive, Serializable, DebugDumpable, Revivable, Cloneable, Freezable, SmartVisitable<Definition> {

    /**
     * Returns a name of the type for this definition.
     *
     * The type can be part of the compile-time schema or it can be defined at run time.
     *
     * Examples of the former case are types like c:UserType, xsd:string, or even flexible
     * ones like c:ExtensionType or c:ShadowAttributesType.
     *
     * Examples of the latter case are types used in
     *
     * - custom extensions, like ext:LocationsType (where ext = e.g. http://example.com/extension),
     * - resource schema, like ri:inetOrgPerson (ri = http://.../resource/instance-3),
     * - connector schema, like TODO
     *
     * In XML representation that corresponds to the name of the XSD type. Although beware, the
     * run-time types do not have statically defined structure. And the resource and connector-related
     * types may even represent different kinds of objects within different contexts (e.g. two
     * distinct resources both with ri:AccountObjectClass types).
     *
     * Also note that for complex type definitions, the type name serves as a unique identifier.
     * On the other hand, for item definitions, it is just one of its attributes; primary key
     * is item name in that case.
     *
     * The type name should be fully qualified. (TODO reconsider this)
     *
     * @return the type name
     */
    @NotNull QName getTypeName();

    /**
     * This means that this particular definition (of an item or of a type) is part of the runtime schema, e.g.
     * extension schema, resource schema or connector schema or something like that. I.e. it is not defined statically.
     */
    boolean isRuntimeSchema();

    /**
     * Item definition that has this flag set should be ignored by any processing.
     * The ignored item is still part of the schema. Item instances may appear in
     * the serialized data formats (e.g. XML) or data store and the parser should
     * not raise an error if it encounters them. But any high-level processing code
     * should ignore presence of this item. E.g. it should not be displayed to the user,
     * should not be present in transformed data structures, etc.
     *
     * Note that the same item can be ignored at higher layer (e.g. presentation)
     * but not ignored at lower layer (e.g. model). This works by presenting different
     * item definitions for these layers (see LayerRefinedAttributeDefinition).
     *
     * Semantics of this flag for complex type definitions is to be defined yet.
     */
    // TODO Remove in 4.3, check whether all usages are equivalent to getProcessing() == IGNORE
    @Deprecated
    default boolean isIgnored() {
        return getProcessing() == ItemProcessing.IGNORE;
    }

    /**
     * Level of processing (ignore, minimal, auto, full) for this item/type.
     */
    ItemProcessing getProcessing();

    /**
     * For types: is the type abstract so that it should not be instantiated directly?
     *
     * For items: TODO
     */
    boolean isAbstract();

    boolean isDeprecated();

    boolean isRemoved();

    String getRemovedSince();

    boolean isOptionalCleanup();

    /**
     * Experimental functionality is not stable and it may be changed in any
     * future release without any warning. Use at your own risk.
     */
    boolean isExperimental();

    /**
     * Version of data model in which the item is likely to be removed.
     * This annotation is used for deprecated item to indicate imminent incompatibility in future versions of data model.
     */
    String getPlannedRemoval();

    /**
     * Elaborate items are complicated data structure that may deviate from
     * normal principles of the system. For example elaborate items may not
     * be supported in user interface and may only be manageable by raw edits
     * or a special-purpose tools. Elaborate items may be not fully supported
     * by authorizations, schema tools and so on.
     */
    boolean isElaborate();

    String getDeprecatedSince();

    /**
     * True for definitions that are more important than others and that should be emphasized
     * during presentation. E.g. the emphasized definitions will always be displayed in the user
     * interfaces (even if they are empty), they will always be included in the dumps, etc.
     */
    boolean isEmphasized();

    DisplayHint getDisplay();

    /**
     * Returns display name.
     *
     * Specifies the printable name of the object class or attribute. It must
     * contain a printable string. It may also contain a key to catalog file.
     *
     * Returns null if no display name is set.
     *
     * Corresponds to "displayName" XSD annotation.
     *
     * @return display name string or catalog key
     */
    String getDisplayName();

    /**
     * Specifies an order in which the item should be displayed relative to other items
     * at the same level. The items will be displayed by sorting them by the
     * values of displayOrder annotation (ascending). Items that do not have
     * any displayOrder annotation will be displayed last. The ordering of
     * values with the same displayOrder is undefined and it may be arbitrary.
     */
    Integer getDisplayOrder();

    /**
     * Returns help string.
     *
     * Specifies the help text or a key to catalog file for a help text. The
     * help text may be displayed in any suitable way by the GUI. It should
     * explain the meaning of an attribute or object class.
     *
     * Returns null if no help string is set.
     *
     * Corresponds to "help" XSD annotation.
     *
     * @return help string or catalog key
     */
    String getHelp();

    String getDocumentation();

    /**
     * Returns only a first sentence of documentation.
     */
    String getDocumentationPreview();

    default SchemaRegistry getSchemaRegistry() {
        PrismContext prismContext = getPrismContext();
        return prismContext != null ? prismContext.getSchemaRegistry() : null;
    }

    /**
     * Returns a compile-time class that is used to represent items.
     * E.g. returns String, Integer, subclasses of Objectable and Containerable and so on.
     */
    Class<?> getTypeClass();

    /**
     * Returns generic definition annotation. Annotations are a method to
     * extend schema definitions.
     * This may be annotation stored in the schema definition file (e.g. XSD)
     * or it may be a dynamic annotation determined at run-time.
     *
     * Annotation value should be a prism-supported object. E.g. a prims "bean"
     * (JAXB annotated class), prism item, prism value or something like that.
     *
     * EXPERIMENTAL. Hic sunt leones. This may change at any moment.
     *
     * Note: annotations are only partially supported now (3.8).
     * They are somehow transient. E.g. they are not serialized to
     * XSD schema definitions (yet).
     */
    @Experimental
    <A> A getAnnotation(QName qname);

    <A> void setAnnotation(QName qname, A value);

    /**
     * Returns all annotations, as unmodifiable map.
     *
     * Nullable by design, to avoid creating lots of empty maps.
     */
    @Nullable Map<QName, Object> getAnnotations();

    @Nullable List<SchemaMigration> getSchemaMigrations();

    @Experimental
    List<ItemDiagramSpecification> getDiagrams();

    @NotNull
    Definition clone();

    default String debugDump(int indent, IdentityHashMap<Definition,Object> seen) {
        return debugDump(indent);
    }

    /**
     * Returns an interface to mutate this definition.
     */
    MutableDefinition toMutable();

    // TODO reconsider/fix this
    default String getMutabilityFlag() {
        return isImmutable() ? "" : "+";
    }
}
