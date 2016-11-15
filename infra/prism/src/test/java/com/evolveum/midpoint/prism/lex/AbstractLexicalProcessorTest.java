/*
 * Copyright (c) 2014 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.evolveum.midpoint.prism.lex;

import static org.testng.AssertJUnit.assertTrue;
import static com.evolveum.midpoint.prism.PrismInternalTestUtil.*;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.delta.*;
import com.evolveum.midpoint.prism.foo.EventHandlerChainType;
import com.evolveum.midpoint.prism.foo.EventHandlerType;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;
import com.evolveum.prism.xml.ns._public.types_3.SchemaDefinitionType;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.evolveum.midpoint.prism.foo.ResourceType;
import com.evolveum.midpoint.prism.foo.UserType;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.prism.util.PrismAsserts;
import com.evolveum.midpoint.prism.util.PrismTestUtil;
import com.evolveum.midpoint.prism.xnode.MapXNode;
import com.evolveum.midpoint.prism.xnode.RootXNode;
import com.evolveum.midpoint.prism.xnode.XNode;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.PrettyPrinter;
import com.evolveum.midpoint.util.exception.SchemaException;

/**
 * @author semancik
 *
 */
public abstract class AbstractLexicalProcessorTest {
	
	private static final QName XSD_COMPLEX_TYPE_ELEMENT_NAME 
			= new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "complexType");

    public static final String EVENT_HANDLER_FILE_BASENAME = "event-handler";

    @BeforeSuite
	public void setupDebug() throws SchemaException, SAXException, IOException {
		PrettyPrinter.setDefaultNamespacePrefix(DEFAULT_NAMESPACE_PREFIX);
		PrismTestUtil.resetPrismContext(new PrismInternalTestUtil());
	}
	
	protected abstract String getSubdirName();
	
	protected abstract String getFilenameSuffix();
	
	protected File getCommonSubdir() {
		return new File(COMMON_DIR_PATH, getSubdirName());
	}
	
	protected File getFile(String baseName) {
		return new File(getCommonSubdir(), baseName+"."+getFilenameSuffix());
	}
	
	protected abstract LexicalProcessor<String> createParser();
	

	@Test
    public void testParseUserToPrism() throws Exception {
		final String TEST_NAME = "testParseUserToPrism";
		displayTestTitle(TEST_NAME);
		
		// GIVEN
		LexicalProcessor lexicalProcessor = createParser();
		PrismContext prismContext = PrismTestUtil.getPrismContext();

		// WHEN (parse to xnode)
		RootXNode xnode = lexicalProcessor.read(getFileSource(USER_JACK_FILE_BASENAME), ParsingContext.createDefault());
		System.out.println("XNode after parsing:");
		System.out.println(xnode.debugDump());
		
		// WHEN (parse to prism)
		PrismObject<UserType> user = prismContext.parserFor(xnode).parse();
		
		// THEN
		System.out.println("Parsed user:");
		System.out.println(user.debugDump());

		assertUserJackXNodeOrdering("serialized xnode", xnode);
		
		assertUserJack(user, true);
		
	}

	private ParserSource getFileSource(String basename) {
		return new ParserFileSource(getFile(basename));
	}

	@Test
    public void testParseUserRoundTrip() throws Exception {
		final String TEST_NAME = "testParseUserRoundTrip";
		displayTestTitle(TEST_NAME);
		
		// GIVEN
		LexicalProcessor<String> lexicalProcessor = createParser();
		PrismContext prismContext = PrismTestUtil.getPrismContext();

		// WHEN (parse)
		RootXNode xnode = lexicalProcessor.read(getFileSource(USER_JACK_FILE_BASENAME), ParsingContext.createDefault());
		System.out.println("\nParsed xnode:");
		System.out.println(xnode.debugDump());
		PrismObject<UserType> user = prismContext.parserFor(xnode).parse();
		
		// THEN
		System.out.println("\nParsed user:");
		System.out.println(user.debugDump());
		
		assertUserJack(user, true);
		
		// WHEN (re-serialize to XNode)
		RootXNode serializedXNode = prismContext.xnodeSerializer()
				.options(SerializationOptions.createSerializeCompositeObjects())
				.serialize(user);
		String serializedString = lexicalProcessor.write(serializedXNode, new QName(NS_FOO, "user"), null);
		
		// THEN
		System.out.println("\nXNode after re-serialization:");
		System.out.println(serializedXNode.debugDump());
		System.out.println("\nRe-serialized string:");
		System.out.println(serializedString);

		String whenSerialized = getWhenItemSerialized();
		assertTrue("Serialized form does not contain " + whenSerialized, serializedString.contains(whenSerialized));
		
		assertUserJackXNodeOrdering("serialized xnode", serializedXNode);
		
		validateUserSchema(serializedString, prismContext);
		
		// WHEN (re-parse)
		RootXNode reparsedXnode = lexicalProcessor.read(new ParserStringSource(serializedString), ParsingContext.createDefault());
		PrismObject<UserType> reparsedUser = prismContext.parserFor(reparsedXnode).parse();
		
		// THEN
		System.out.println("\nXNode after re-parsing:");
		System.out.println(reparsedXnode.debugDump());
		System.out.println("\nRe-parsed user:");
		System.out.println(reparsedUser.debugDump());

		assertUserJackXNodeOrdering("serialized xnode", reparsedXnode);
				
		ObjectDelta<UserType> diff = DiffUtil.diff(user, reparsedUser);
		System.out.println("\nDiff:");
		System.out.println(diff.debugDump());
		
		PrismObject accountRefObjOrig = findObjectFromAccountRef(user);
		PrismObject accountRefObjRe = findObjectFromAccountRef(reparsedUser);
		
		ObjectDelta<UserType> accountRefObjDiff = DiffUtil.diff(accountRefObjOrig, accountRefObjRe);
		System.out.println("\naccountRef object diff:");
		System.out.println(accountRefObjDiff.debugDump());
		
		assertTrue("Re-parsed object in accountRef does not match: "+accountRefObjDiff, accountRefObjDiff.isEmpty());
		
		assertTrue("Re-parsed user does not match: "+diff, diff.isEmpty());
	}

	// to check if timestamps are serialized correctly
	protected abstract String getWhenItemSerialized();

	@Test
    public void testParseResourceRumToPrism() throws Exception {
		final String TEST_NAME = "testParseResourceRumToPrism";
		displayTestTitle(TEST_NAME);
		
		// GIVEN
		LexicalProcessor lexicalProcessor = createParser();
		PrismContext prismContext = PrismTestUtil.getPrismContext();

		// WHEN (parse to xnode)
		RootXNode xnode = lexicalProcessor.read(getFileSource(RESOURCE_RUM_FILE_BASENAME), ParsingContext.createDefault());
		System.out.println("XNode after parsing:");
		System.out.println(xnode.debugDump());
		
		// WHEN (parse to prism)
		PrismObject<ResourceType> resource = prismContext.parserFor(xnode).parse();
		
		// THEN
		System.out.println("Parsed resource:");
		System.out.println(resource.debugDump());
		
		assertResourceRum(resource);		
		
	}

	@Test
    public void testParseResourceRoundTrip() throws Exception {
		final String TEST_NAME = "testParseResourceRoundTrip";
		displayTestTitle(TEST_NAME);
		
		// GIVEN
		LexicalProcessor<String> lexicalProcessor = createParser();
		PrismContext prismContext = PrismTestUtil.getPrismContext();

		// WHEN (parse)
		RootXNode xnode = lexicalProcessor.read(getFileSource(RESOURCE_RUM_FILE_BASENAME), ParsingContext.createDefault());
		PrismObject<ResourceType> resource = prismContext.parserFor(xnode).parse();
		
		// THEN
		System.out.println("\nParsed resource:");
		System.out.println(resource.debugDump());
		
		assertResourceRum(resource);
		
		// WHEN (re-serialize to XNode)
		XNode serializedXNode = prismContext.xnodeSerializer()
				.options(SerializationOptions.createSerializeCompositeObjects())
				.serialize(resource);
		String serializedString = lexicalProcessor.write(serializedXNode, new QName(NS_FOO, "resource"), null);
				
		// THEN
		System.out.println("\nXNode after re-serialization:");
		System.out.println(serializedXNode.debugDump());
		System.out.println("\nRe-serialized string:");
		System.out.println(serializedString);
		
		validateResourceSchema(serializedString, prismContext);
		
		// WHEN (re-parse)
		RootXNode reparsedXnode = lexicalProcessor.read(new ParserStringSource(serializedString), ParsingContext.createDefault());
		PrismObject<ResourceType> reparsedResource = prismContext.parserFor(reparsedXnode).parse();
		
		// THEN
		System.out.println("\nXNode after re-parsing:");
		System.out.println(reparsedXnode.debugDump());
		System.out.println("\nRe-parsed resource:");
		System.out.println(reparsedResource.debugDump());
		
		ObjectDelta<ResourceType> diff = DiffUtil.diff(resource, reparsedResource);
		System.out.println("\nDiff:");
		System.out.println(diff.debugDump());
				
		assertTrue("Re-parsed user does not match: "+diff, diff.isEmpty());
	}	


	private void assertResourceRum(PrismObject<ResourceType> resource) throws SchemaException {
		resource.checkConsistence();
		resource.assertDefinitions("test");
		
		assertEquals("Wrong oid", RESOURCE_RUM_OID, resource.getOid());
		PrismAsserts.assertObjectDefinition(resource.getDefinition(), RESOURCE_QNAME, RESOURCE_TYPE_QNAME, ResourceType.class);
		PrismAsserts.assertParentConsistency(resource);
		
		assertPropertyValue(resource, "name", new PolyString("Rum Delivery System", "rum delivery system"));
		assertPropertyDefinition(resource, "name", PolyStringType.COMPLEX_TYPE, 0, 1);
		
		PrismProperty<SchemaDefinitionType> propSchema = resource.findProperty(ResourceType.F_SCHEMA);
		assertNotNull("No schema property in resource", propSchema);
		PrismPropertyDefinition<SchemaDefinitionType> propSchemaDef = propSchema.getDefinition();
		assertNotNull("No definition of schema property in resource", propSchemaDef);
		SchemaDefinitionType schemaDefinitionType = propSchema.getRealValue();
		assertNotNull("No value of schema property in resource", schemaDefinitionType);
		
		Element schemaElement = schemaDefinitionType.getSchema();
		assertNotNull("No schema element in schema property in resource", schemaElement);
		System.out.println("Resource schema:");
		System.out.println(DOMUtil.serializeDOMToString(schemaElement));
		assertEquals("Bad schema element name", DOMUtil.XSD_SCHEMA_ELEMENT, DOMUtil.getQName(schemaElement));
		Element complexTypeElement = DOMUtil.getChildElement(schemaElement, XSD_COMPLEX_TYPE_ELEMENT_NAME);
		assertNotNull("No complexType element in schema element in schema property in resource", complexTypeElement);
		String complexTypeName = complexTypeElement.getAttribute("name");
		assertEquals("Wrong name of complex type", "BarrelType", complexTypeName);
	}

	private PrismObject findObjectFromAccountRef(PrismObject<UserType> user) {
		for (PrismReferenceValue rval: user.findReference(UserType.F_ACCOUNT_REF).getValues()) {
			if (rval.getObject() != null) {
				return rval.getObject();
			}
		}
		return null;
	}

	protected <X extends XNode> X getAssertXNode(String message, XNode xnode, Class<X> expectedClass) {
		assertNotNull(message+" is null", xnode);
		assertTrue(message+", expected "+expectedClass.getSimpleName()+", was "+xnode.getClass().getSimpleName(),
				expectedClass.isAssignableFrom(xnode.getClass()));
		return (X) xnode;
	}
	
	protected <X extends XNode> X getAssertXMapSubnode(String message, MapXNode xmap, QName key, Class<X> expectedClass) {
		XNode xsubnode = xmap.get(key);
		assertNotNull(message+" no key "+key, xsubnode);
		return getAssertXNode(message+" key "+key, xsubnode, expectedClass);
	}
	
	protected void assertUserJackXNodeOrdering(String message, XNode xnode) {
		if (xnode instanceof RootXNode) {
			xnode = ((RootXNode)xnode).getSubnode();
		}
		MapXNode xmap = getAssertXNode(message+": top", xnode, MapXNode.class);
		Set<Entry<QName, XNode>> reTopMapEntrySet = xmap.entrySet();
		Iterator<Entry<QName, XNode>> reTopMapEntrySetIter = reTopMapEntrySet.iterator();
		Entry<QName, XNode> reTopMapEntry0 = reTopMapEntrySetIter.next();
		assertEquals(message+": Wrong entry 0, the xnodes were shuffled", "oid", reTopMapEntry0.getKey().getLocalPart());
		Entry<QName, XNode> reTopMapEntry1 = reTopMapEntrySetIter.next();
		assertEquals(message+": Wrong entry 1, the xnodes were shuffled", "version", reTopMapEntry1.getKey().getLocalPart());
		Entry<QName, XNode> reTopMapEntry2 = reTopMapEntrySetIter.next();
		assertEquals(message+": Wrong entry 2, the xnodes were shuffled", UserType.F_NAME, reTopMapEntry2.getKey());
		Entry<QName, XNode> reTopMapEntry3 = reTopMapEntrySetIter.next();
		assertEquals(message+": Wrong entry 3, the xnodes were shuffled", UserType.F_DESCRIPTION, reTopMapEntry3.getKey());

	}
	
	protected void validateUserSchema(String dataString, PrismContext prismContext) throws SAXException, IOException {
		// Nothing to do by default
	}
	
	protected void validateResourceSchema(String dataString, PrismContext prismContext) throws SAXException, IOException {
		// Nothing to do by default
	}

    // The following is not supported now (and probably won't be in the future).
    // Enable it if that changes.
    @Test(enabled = false)
    public void testParseEventHandler() throws Exception {
        final String TEST_NAME = "testParseEventHandler";
        displayTestTitle(TEST_NAME);

        // GIVEN
        LexicalProcessor lexicalProcessor = createParser();
		PrismContext prismContext = PrismTestUtil.getPrismContext();

        // WHEN (parse to xnode)
        RootXNode xnode = lexicalProcessor.read(getFileSource(EVENT_HANDLER_FILE_BASENAME), ParsingContext.createDefault());
        System.out.println("XNode after parsing:");
        System.out.println(xnode.debugDump());

        // WHEN (parse to prism)
		EventHandlerType eventHandlerType = prismContext.parserFor(xnode).parseRealValue(EventHandlerChainType.class);
//		EventHandlerType eventHandlerType = prismContext.getBeanConverter().unmarshall((MapXNode) , EventHandlerChainType.class,
//				ParsingContext.createDefault());

        // THEN
        System.out.println("Parsed object:");
        System.out.println(eventHandlerType);

        // WHEN2 (marshalling)
        MapXNode marshalled = (MapXNode) (prismContext.xnodeSerializer().serializeRealValue(eventHandlerType).getSubnode());

        System.out.println("XNode after unmarshalling and marshalling back:");
        System.out.println(marshalled.debugDump());

    }

}
