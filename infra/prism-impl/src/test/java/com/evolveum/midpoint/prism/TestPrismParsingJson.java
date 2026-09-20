/*
 * Copyright (c) 2010-2019 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism;

import static com.evolveum.midpoint.prism.PrismInternalTestUtil.EXTRA_SCHEMA_DIR;
import static com.evolveum.midpoint.prism.PrismInternalTestUtil.NS_USER_EXT;
import static com.evolveum.midpoint.prism.PrismInternalTestUtil.NS_WEAPONS;
import static com.evolveum.midpoint.prism.PrismInternalTestUtil.NS_WEAPONS_PREFIX;
import static com.evolveum.midpoint.prism.PrismInternalTestUtil.USER_EXT_BAR_ELEMENT;
import static com.evolveum.midpoint.prism.PrismInternalTestUtil.USER_QNAME;
import static com.evolveum.midpoint.prism.PrismInternalTestUtil.constructInitializedPrismContext;
import static com.evolveum.midpoint.prism.PrismInternalTestUtil.constructPrismContext;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.evolveum.midpoint.prism.foo.UserType;
import com.evolveum.midpoint.prism.impl.PrismContextImpl;
import com.evolveum.midpoint.prism.impl.lex.json.reader.JsonReader;
import com.evolveum.midpoint.prism.impl.lex.json.writer.JsonWriter;
import com.evolveum.midpoint.prism.impl.xnode.MapXNodeImpl;
import com.evolveum.midpoint.prism.impl.xnode.PrimitiveXNodeImpl;
import com.evolveum.midpoint.prism.impl.xnode.RootXNodeImpl;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.xnode.MapXNode;
import com.evolveum.midpoint.prism.xnode.PrimitiveXNode;
import com.evolveum.midpoint.prism.xnode.XNode;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;
import com.evolveum.prism.xml.ns._public.types_3.ProtectedDataType;
import com.google.common.base.Joiner;

public class TestPrismParsingJson extends TestPrismParsing {

    @Override
    protected String getSubdirName() {
        return "json";
    }

    @Override
    protected String getFilenameSuffix() {
        return "json";
    }

    @Override
    protected String getOutputFormat() {
        return PrismContext.LANG_JSON;
    }

    @Test
    public void testPrismNamespaceContext() throws SchemaException, IOException {
        File jackContext = getFile("user-jack-object-context");

        JsonReader parser = new JsonReader(getPrismContext().getSchemaRegistry());

        @NotNull
        RootXNodeImpl rootXNode = parser.read(new ParserFileSource(jackContext), getPrismContext().getDefaultParsingContext(), null);
        assertContextJack(rootXNode);
    }

    @Test
    public void testPrismNamespaceAxiom() throws SchemaException, IOException {
        File jackContext = getFile("user-jack-object-axiom");

        JsonReader parser = new JsonReader(getPrismContext().getSchemaRegistry());

        @NotNull
        RootXNodeImpl rootXNode = parser.read(new ParserFileSource(jackContext), getPrismContext().getDefaultParsingContext(), null);
        assertContextJack(rootXNode);
    }

    @Test
    public void testItemPathValueSerializationPreservesUsedNamespaceContext() throws Exception {
        PrismContextImpl writingContext = constructPrismContext();
        writingContext.getSchemaRegistry().registerDynamicSchemaExtensions(Map.of("test dynamic user extension",
                DOMUtil.parseFile(new File(EXTRA_SCHEMA_DIR, "extension/user.xsd")).getDocumentElement()));
        writingContext.initialize();

        PrismContextImpl parsingContext = constructInitializedPrismContext();
        PrimitiveXNodeImpl<ItemPathType> pathNode = new PrimitiveXNodeImpl<>();
        pathNode.setValue(
                new ItemPathType(ItemPath.create(
                        UserType.F_EXTENSION,
                        new ItemName(NS_WEAPONS, "weapon", NS_WEAPONS_PREFIX),
                        USER_EXT_BAR_ELEMENT)),
                ItemPathType.COMPLEX_TYPE);

        MapXNodeImpl userNode = new MapXNodeImpl();
        userNode.put(new QName("path"), pathNode);

        RootXNodeImpl root = new RootXNodeImpl(USER_QNAME, userNode);
        String serialized = new JsonWriter(writingContext.getSchemaRegistry()).write(root, null);

        display("Serialized ItemPathType with dynamic namespace");
        display(serialized);

        // Dynamic schema extension namespaces have to be persisted locally, because they may not be registered when the value is parsed later.
        assertTrue(serialized.contains(NS_USER_EXT));
        assertTrue(serialized.contains(":" + USER_EXT_BAR_ELEMENT.getLocalPart()));

        // Static namespaces should continue to rely on the ambient namespace context.
        assertFalse(
                serialized.contains("\"" + NS_WEAPONS_PREFIX + "\":\"" + NS_WEAPONS + "\""),
                "Static namespace should not be serialized as local ItemPath context");

        RootXNodeImpl reparsed = (RootXNodeImpl) parsingContext
                .parserFor(serialized)
                .language(getOutputFormat())
                .parseToXNode();

        PrimitiveXNode<ItemPathType> reparsedPathNode = get(PrimitiveXNode.class, reparsed.toMapXNode(), "user", "path");
        ItemPathType reparsedPath = reparsedPathNode.getParsedValue(ItemPathType.COMPLEX_TYPE, ItemPathType.class);

        assertEquals(reparsedPath.getItemPath().lastName(), USER_EXT_BAR_ELEMENT);
    }

    private void assertContextJack(@NotNull RootXNodeImpl rootXNode) throws SchemaException {
        assertNotNull(rootXNode);
        MapXNodeImpl mapNode = rootXNode.toMapXNode();
        MapXNode password = get(MapXNode.class, mapNode, "object", "password");
        assertNotNull(password.toMap().get(ProtectedDataType.F_ENCRYPTED_DATA), "EncryptedData should be qualified");

        MapXNode accountRef = get(MapXNode.class, mapNode, "object", "accountRef");


        PrimitiveXNode<ItemPath> pathNode = get(PrimitiveXNode.class, mapNode, "object", "accountRef", "filter", "equal", "path");

        ItemPathType path = pathNode.getParsedValue(ItemPathType.COMPLEX_TYPE, ItemPathType.class);
        assertNotNull(path);
        assertEquals(path.getItemPath().firstName(), UserType.F_NAME);

        JsonWriter serializer = new JsonWriter(getPrismContext().getSchemaRegistry());
        @NotNull
        String xnodeOutput = serializer.write(rootXNode, null);
        display("Direct XNode serialization");
        display(xnodeOutput);
        @NotNull
        PrismObject<Objectable> jackOriginal = getPrismContext().parserFor(rootXNode).parse();
        @NotNull
        PrismObject<Objectable> jackXnodeSerialized = getPrismContext().parserFor(xnodeOutput).language(getOutputFormat()).parse();
        assertEquals(jackXnodeSerialized, jackOriginal);

        String prismOutput = getPrismContext().serializerFor(getOutputFormat()).serialize(jackOriginal);

        display("Prism serialization");
        display(prismOutput);

        PrismObject<Objectable> jackPrismSerialized = getPrismContext().parserFor(prismOutput).language(getOutputFormat()).parse();
        assertEquals(jackPrismSerialized, jackOriginal);

    }

    private static final <E extends T,T extends XNode> E get(Class<T> type, MapXNode root, String... path) {
        XNode current = root;
        for(String cmp : path) {
            if(current instanceof MapXNode node) {
                current = node.get(new QName(cmp));
            } else if(current != null) {
                throw new AssertionError(cmp + " should be MapXNode not " + current.getClass().getSimpleName());
            } else {
                Assert.fail("Node " + cmp + " not found.");
            }
        }
        assertNotNull(current, "Object at " + Joiner.on("/").join(path) + " should not be null");
        assertTrue(type.isInstance(current), "Current must be instanceof " + type);
        return (E) type.cast(current);
    }

}
