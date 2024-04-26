/*
 * Copyright (c) 2010-2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.schema;

import java.util.Collection;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.path.ItemPath;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.xml.DynamicNamespacePrefixMapper;
import com.evolveum.midpoint.util.DebugDumpable;
import com.evolveum.midpoint.util.exception.SchemaException;

/**
 * Maintains system-wide parsed schemas.
 */
public interface SchemaRegistryState extends DebugDumpable, GlobalDefinitionsStore {

    Collection<PrismSchema> getSchemas();

    PrismSchema getPrismSchema(String namespace);

    javax.xml.validation.Schema getJavaxSchema();

    /**
     //     * @return System-wide "standard prefixes" registry.
     //     */
    DynamicNamespacePrefixMapper getNamespacePrefixMapper();

    // TODO fix this temporary and inefficient implementation
    QName resolveUnqualifiedTypeName(QName type) throws SchemaException;

    // current implementation tries to find all references to the child CTD and select those that are able to resolve path of 'rest'
    // fails on ambiguity
    // it's a bit fragile, as adding new references to child CTD in future may break existing code
    ComplexTypeDefinition determineParentDefinition(@NotNull ComplexTypeDefinition child, @NotNull ItemPath rest);

    <T> Class<T> determineCompileTimeClass(QName typeName);

    PrismSchema findSchemaByCompileTimeClass(@NotNull Class<?> compileTimeClass);

    SchemaDescription findSchemaDescriptionByNamespace(String namespaceURI);

    SchemaDescription findSchemaDescriptionByPrefix(String prefix);

    PrismSchema findSchemaByNamespace(String namespaceURI);

    // Takes XSD types into account as well
    <T> Class<T> determineClassForType(QName type);

    <T> Class<T> determineCompileTimeClassInternal(QName type, boolean cacheAlsoNegativeResults);

    Collection<Package> getCompileTimePackages();

    enum IsList {
        YES, NO, MAYBE
    }

    /**
     * Checks whether element with given (declared) xsi:type and name can be a heterogeneous list.
     *
     * @return YES if it is a list,
     *         NO if it's not,
     *         MAYBE if it probably is a list but some further content-based checks are needed
     */
    @NotNull
    IsList isList(@Nullable QName xsiType, @NotNull QName elementName);
}
