/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.prism.xnode.RootXNode;
import com.evolveum.midpoint.util.exception.SchemaException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.util.List;

/**
 * The same as PrismParser but has no IOException on parseXYZ methods. It is used when parsing from strings or DOM structures
 * where no IOExceptions occur.
 *
 * For methods' descriptions please see the parent interface (PrismParser).
 *
 * @author mederly
 */
public interface PrismParserNoIO extends PrismParser {

    @Override
    @NotNull
    PrismParserNoIO language(@Nullable String language);
    @Override
    @NotNull
    PrismParserNoIO xml();
    @Override
    @NotNull
    PrismParserNoIO json();
    @Override
    @NotNull
    PrismParserNoIO yaml();
    @Override
    @NotNull
    PrismParserNoIO context(@NotNull ParsingContext context);
    @Override
    @NotNull
    PrismParserNoIO strict();
    @Override
    @NotNull
    PrismParserNoIO compat();
    @Override
    @NotNull
    PrismParserNoIO definition(ItemDefinition<?> itemDefinition);
    @Override
    @NotNull
    PrismParserNoIO name(QName itemName);
    @Override
    @NotNull
    PrismParserNoIO type(QName typeName);
    @Override
    @NotNull
    PrismParserNoIO type(Class<?> typeClass);

    @Override
    @NotNull
    <O extends Objectable> PrismObject<O> parse() throws SchemaException;
    @Override
    <IV extends PrismValue, ID extends ItemDefinition> Item<IV,ID> parseItem() throws SchemaException;
    @Override
    <IV extends PrismValue> IV parseItemValue() throws SchemaException;
    @Override
    <T> T parseRealValue(Class<T> clazz) throws SchemaException;
    @Override
    <T> T parseRealValue() throws SchemaException;
    @Override
    <T> JAXBElement<T> parseRealValueToJaxbElement() throws SchemaException;
    @Override
    RootXNode parseToXNode() throws SchemaException;
    @Override
    Object parseItemOrRealValue() throws SchemaException;

    // auxiliary methods
    @Override
    @NotNull
    List<PrismObject<? extends Objectable>> parseObjects() throws SchemaException;

    @Override
    void parseObjectsIteratively(@NotNull ObjectHandler handler) throws SchemaException;

    @Override
    PrismParserNoIO convertMissingTypes();
}
