/*
 * Copyright (c) 2010-2017 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.schema;

import java.util.Collection;
import java.util.List;
import javax.xml.namespace.QName;

import org.apache.commons.collections4.MultiValuedMap;
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

    MultiValuedMap<String, SchemaDescription> getParsedSchemas();

    javax.xml.validation.Schema getJavaxSchema();

    /**
     //     * @return System-wide "standard prefixes" registry.
     //     */
    DynamicNamespacePrefixMapper getNamespacePrefixMapper();

    // TODO fix this temporary and inefficient implementation
    QName resolveUnqualifiedTypeName(QName type) throws SchemaException;

    <T> Class<T> determineCompileTimeClass(QName typeName);

    PrismSchema findSchemaByCompileTimeClass(@NotNull Class<?> compileTimeClass);

    SchemaDescription findSchemaDescriptionByNamespace(String namespaceURI);

    SchemaDescription findSchemaDescriptionByPrefix(String prefix);

    PrismSchema findSchemaByNamespace(String namespaceURI);

    // Takes XSD types into account as well
    <T> Class<T> determineClassForType(QName type);

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

    <TD extends TypeDefinition> TD resolveGlobalTypeDefinitionWithoutNamespace(String typeLocalName, Class<TD> definitionClass);

    <TD extends TypeDefinition> Collection<TD> resolveGlobalTypeDefinitionsWithoutNamespace(String typeLocalName, Class<TD> definitionClass);

    <ID extends ItemDefinition> List<ID> resolveGlobalItemDefinitionsWithoutNamespace(String localPart, Class<ID> definitionClass, @Nullable List<String> ignoredNamespaces);

    <ID extends ItemDefinition> List<ID> resolveGlobalItemDefinitionsWithoutNamespace(String localPart, Class<ID> definitionClass);

    <ID extends ItemDefinition> ID resolveGlobalItemDefinitionWithoutNamespace(
            String localPart, Class<ID> definitionClass, boolean exceptionIfAmbiguous, @Nullable List<String> ignoredNamespaces);
}
