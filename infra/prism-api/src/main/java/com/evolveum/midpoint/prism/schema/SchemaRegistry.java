/*
 * Copyright (c) 2010-2017 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.schema;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.prism.schema.SchemaRegistryState.IsList;
import com.evolveum.midpoint.prism.xml.DynamicNamespacePrefixMapper;
import com.evolveum.midpoint.util.DebugDumpable;
import com.evolveum.midpoint.util.MiscUtil;
import com.evolveum.midpoint.util.annotation.Experimental;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.prism.xml.ns._public.types_3.ObjectType;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Maintains system-wide schemas that is used as source for parsing during initialize and reload.
 */
public interface SchemaRegistry extends DebugDumpable, SchemaRegistryState {

    static SchemaRegistry get() {
        return PrismContext.get().getSchemaRegistry();
    }

    void customizeNamespacePrefixMapper(Consumer<DynamicNamespacePrefixMapper> customizer);

    PrismNamespaceContext staticNamespaceContext();

    void registerInvalidationListener(InvalidationListener listener);

    String getDefaultNamespace();

    void reload() throws SchemaException;

    void initialize() throws SAXException, IOException, SchemaException;

    javax.xml.validation.Validator getJavaxSchemaValidator();

    Collection<SchemaDescription> getSchemaDescriptions();

    ItemDefinition locateItemDefinition(
            @NotNull QName itemName,
            @Nullable QName explicitTypeName,
            @Nullable ComplexTypeDefinition complexTypeDefinition,
            @Nullable Function<QName, ItemDefinition> dynamicDefinitionResolver);

    QName qualifyTypeName(QName typeName) throws SchemaException;

    PrismObjectDefinition determineReferencedObjectDefinition(@NotNull QName targetTypeName, ItemPath rest);

    Class<? extends ObjectType> getCompileTimeClassForObjectType(QName objectType);

    default @NotNull Class<? extends ObjectType> getCompileTimeClassForObjectTypeRequired(@NotNull QName objectType) {
        Class<? extends ObjectType> clazz = getCompileTimeClassForObjectType(objectType);
        if (clazz != null) {
            return clazz;
        } else {
            throw new IllegalStateException("No compile-time class for " + objectType);
        }
    }

    ItemDefinition findItemDefinitionByElementName(QName elementName, @Nullable List<String> ignoredNamespaces);

    default <T> Class<T> getCompileTimeClass(QName xsdType) {
        return determineCompileTimeClass(xsdType);
    }

    /**
     * Tries to determine type name for any class (primitive, complex one).
     * Does not use schemas (TODO explanation)
     * @param clazz
     * @return
     */
    QName determineTypeForClass(Class<?> clazz);

    @NotNull
    default QName determineTypeForClassRequired(Class<?> clazz) {
        QName typeName = determineTypeForClass(clazz);
        if (typeName != null) {
            return typeName;
        } else {
            throw new IllegalStateException("No type for " + clazz);
        }
    }

    /**
     * This method will try to locate the appropriate object definition and apply it.
     * @param container
     * @param type
     */
    <C extends Containerable> void applyDefinition(PrismContainer<C> container, Class<C> type) throws SchemaException;

    <C extends Containerable> void applyDefinition(PrismContainer<C> prismObject, Class<C> type, boolean force) throws SchemaException;

    <T extends Objectable> void applyDefinition(ObjectDelta<T> objectDelta, Class<T> type, boolean force) throws SchemaException;

    <C extends Containerable, O extends Objectable> void applyDefinition(PrismContainerValue<C> prismContainerValue,
            Class<O> type,
            ItemPath path, boolean force) throws SchemaException;

    <C extends Containerable> void applyDefinition(PrismContainerValue<C> prismContainerValue, QName typeName,
            ItemPath path, boolean force) throws SchemaException;

    <T extends ItemDefinition> T findItemDefinitionByFullPath(Class<? extends Objectable> objectClass, Class<T> defClass,
            QName... itemNames)
                            throws SchemaException;

    PrismObjectDefinition determineDefinitionFromClass(Class type);

    @NotNull PrismContainerDefinition<?> getValueMetadataDefinition();

    boolean hasImplicitTypeDefinition(@NotNull QName itemName, @NotNull QName typeName);

    ItemDefinition resolveGlobalItemDefinition(QName itemName, @Nullable ComplexTypeDefinition complexTypeDefinition);

    default <T> Class<T> determineJavaClassForType(QName type) {
        Class<?> xsdClass = MiscUtil.resolvePrimitiveIfNecessary(
                determineClassForType(type));
        if (PolyStringType.class.equals(xsdClass)) {
            //noinspection unchecked
            return (Class<T>) PolyString.class;
        } else {
            //noinspection unchecked
            return (Class<T>) xsdClass;
        }
    }

    default <T> Class<T> determineClassForTypeRequired(QName type, Class<T> expected) {
        Class<?> clazz = determineClassForTypeRequired(type);
        if (!expected.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("Expected to get " + expected + " but got " + clazz + " instead, for " + type);
        } else {
            //noinspection unchecked
            return (Class<T>) clazz;
        }
    }

    default <T> Class<T> determineClassForTypeRequired(QName type) {
        Class<T> clazz = determineClassForType(type);
        if (clazz != null) {
            return clazz;
        } else {
            throw new IllegalArgumentException("No class for " + type);
        }
    }

    // Takes XSD types into account as well
    Class<?> determineClassForItemDefinition(ItemDefinition<?> itemDefinition);

    <ID extends ItemDefinition> ID selectMoreSpecific(ID def1, ID def2)
            throws SchemaException;

    /**
     * Selects the type that is more specific of the two.
     *
     * For example, if the input is `FocusType` and `UserType`, the output is `UserType`.
     * Returns `null` if there's no such type.
     *
     * Limitations/specific handling:
     *
     * - Assumes both types have compile-time representation. May return `null` if that's not true.
     * - The treatment of `PolyStringType` vs `String` is rather strange. Should be reviewed. FIXME
     */
    QName selectMoreSpecific(@Nullable QName type1, @Nullable QName type2) throws SchemaException;

    /**
     * @return true if the typeName corresponds to statically-typed class that is Containerable.
     *
     * TODO The utility of this method is questionable. Reconsider its removal/update.
     */
    boolean isContainerable(QName typeName);
    SchemaLookup.Mutable getCurrentLookup();

    enum ComparisonResult {
        EQUAL, // types are equal
        NO_STATIC_CLASS, // static class cannot be determined
        FIRST_IS_CHILD, // first definition is a child (strict subtype) of the second
        SECOND_IS_CHILD, // second definition is a child (strict subtype) of the first
        INCOMPATIBLE // first and second are incompatible
    }
    /**
     * @return null means we cannot decide (types are different, and no compile time class for def1 and/or def2)
     */
    <ID extends ItemDefinition> ComparisonResult compareDefinitions(@NotNull ID def1, @NotNull ID def2)
            throws SchemaException;

    /**
     * BEWARE: works only with statically-defined types!
     */
    boolean isAssignableFrom(@NotNull Class<?> superType, @NotNull QName subType);

    /**
     * BEWARE: works only with statically-defined types!
     */
    boolean isAssignableFrom(@NotNull QName superType, @NotNull QName subType);

    /**
     * Crawls through the type definition tree. May be slower.
     */
    @Experimental
    boolean isAssignableFromGeneral(@NotNull QName superType, @NotNull QName subType);

    /**
     * Returns most specific common supertype for these two. If any of input params is null, it means it is ignored.
     *
     * FIXME is the implementation correct regarding this spec? E.g. for `UserType` and `RoleType` it should return
     *  `FocusType` but it returns `null` instead!
     *
     * @return null if unification cannot be done (or if both input types are null)
     */
    QName unifyTypes(QName type1, QName type2);

    ItemDefinition<?> createAdHocDefinition(QName elementName, QName typeName, int minOccurs, int maxOccurs);

    List<TypeDefinition> getAllSubTypesByTypeDefinition(List<TypeDefinition> typeClasses);

    interface InvalidationListener {
        void invalidate();
    }

    void registerDynamicSchemaExtensions(Map<String, Element> dbSchemaExtensions) throws SchemaException;
    boolean existDynamicSchemaExtensions();
}
