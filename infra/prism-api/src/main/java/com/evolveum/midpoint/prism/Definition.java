/*
 * Copyright (c) 2010-2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism;

import java.io.Serializable;
import java.util.IdentityHashMap;
import java.util.Map;
import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.util.DebugDumpable;
import com.evolveum.midpoint.util.annotation.Experimental;

/**
 * Common interface to access all definitions.
 */
public interface Definition
        extends
        PrismPresentationDefinition, PrismLifecycleDefinition,
        Serializable, DebugDumpable, Revivable, Cloneable, Freezable, SmartVisitable<Definition> {

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
     * For types: is the type abstract so that it should not be instantiated directly?
     *
     * For items: TODO
     */
    boolean isAbstract();

    /**
     * Marks item that could be ignored by SCM tools (e.g. Git), or removed before commit.
     */
    boolean isOptionalCleanup();

    /**
     * Elaborate items are complicated data structure that may deviate from
     * normal principles of the system. For example elaborate items may not
     * be supported in user interface and may only be manageable by raw edits
     * or a special-purpose tools. Elaborate items may be not fully supported
     * by authorizations, schema tools and so on.
     */
    boolean isElaborate();


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

    /**
     * Returns all annotations, as unmodifiable map.
     *
     * Nullable by design, to avoid creating lots of empty maps.
     */
    @Nullable Map<QName, Object> getAnnotations();

    @NotNull Definition clone();

    default String debugDump(int indent, IdentityHashMap<Definition,Object> seen) {
        return debugDump(indent);
    }

    /**
     * Returns an interface to mutate this definition.
     */
    DefinitionMutator mutator();

    // TODO reconsider/fix this
    default String getMutabilityFlag() {
        return isImmutable() ? "" : "+";
    }

    default void checkMutableOnExposing() {
        if (isImmutable()) {
            throw new IllegalStateException("Definition couldn't be exposed as mutable because it is immutable: " + this);
        }
    }
    /**
     * An interface that provides an ability to modify a definition.
     */
    interface DefinitionMutator
            extends
            PrismPresentationDefinition.Mutable,
            PrismLifecycleDefinition.Mutable {

        void setOptionalCleanup(boolean optionalCleanup);

        void setRuntimeSchema(boolean value);

        <A> void setAnnotation(QName qname, A value);
    }

    interface DefinitionBuilder extends DefinitionFragmentBuilder {
    }
}
