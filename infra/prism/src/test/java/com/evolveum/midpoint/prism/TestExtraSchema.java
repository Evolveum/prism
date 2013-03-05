package com.evolveum.midpoint.prism;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNull;
import static com.evolveum.midpoint.prism.PrismInternalTestUtil.*;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.evolveum.midpoint.prism.foo.UserType;
import com.evolveum.midpoint.prism.schema.PrismSchema;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.prism.util.PrismAsserts;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.exception.SchemaException;

public class TestExtraSchema {
	
	private static final QName USER_EXTENSION_TYPE_QNAME = new QName(NS_USER_EXT,"UserExtensionType");
	
	/**
	 * Test is extra schema can be loaded to the schema registry and whether the file compliant to that
	 * schema can be validated.
	 */
	@Test
	public void testExtraSchema() throws SAXException, IOException, SchemaException {
		System.out.println("===[ testExtraSchema ]===");
		Document extraSchemaDoc = DOMUtil.parseFile(new File(EXTRA_SCHEMA_DIR, "root.xsd"));
		Document dataDoc = DOMUtil.parseFile(new File(COMMON_DIR_PATH, "root-foo.xml"));

		PrismContext context = constructPrismContext();
		SchemaRegistry reg = context.getSchemaRegistry();
		reg.registerSchema(extraSchemaDoc, "file root.xsd");
		reg.initialize();
		Schema javaxSchema = reg.getJavaxSchema();
		assertNotNull(javaxSchema);
		
		Validator validator = javaxSchema.newValidator();
		DOMResult validationResult = new DOMResult();
		validator.validate(new DOMSource(dataDoc),validationResult);
//		System.out.println("Validation result:");
//		System.out.println(DOMUtil.serializeDOMToString(validationResult.getNode()));
	}

	/**
	 * Test if a schema directory can be loaded to the schema registry. This contains definition of
	 * user extension, therefore check if it is applied to the user definition. 
	 */
	@Test
	public void testUserExtensionSchemaLoad() throws SAXException, IOException, SchemaException {
		System.out.println("===[ testUserExtensionSchemaLoad ]===");
		
		PrismContext context = constructPrismContext();
		SchemaRegistry reg = context.getSchemaRegistry();
		reg.registerPrismSchemasFromDirectory(EXTRA_SCHEMA_DIR);
		context.initialize();
		System.out.println("Initialized registry");
		System.out.println(reg.dump());
		
		// Try midpoint schemas by parsing a XML file
		PrismSchema schema = reg.getSchema(NS_FOO);
		System.out.println("Parsed foo schema:");
		System.out.println(schema.dump());
		
		// TODO: assert user

		schema = reg.getSchema(NS_USER_EXT);
		System.out.println("Parsed user ext schema:");
		System.out.println(schema.dump());
		
		ComplexTypeDefinition userExtComplexType = schema.findComplexTypeDefinition(USER_EXTENSION_TYPE_QNAME);
		assertEquals("Extension type ref does not match", USER_TYPE_QNAME, userExtComplexType.getExtensionForType());
		
	}
	
	@Test
	public void testUserExtensionSchemaPaseUser() throws SAXException, IOException, SchemaException {
		System.out.println("===[ testUserExtensionSchemaPaseUser ]===");
		Document dataDoc = DOMUtil.parseFile(USER_JACK_FILE);
		
		PrismContext context = constructPrismContext();
		SchemaRegistry reg = context.getSchemaRegistry();
		reg.registerPrismSchemasFromDirectory(EXTRA_SCHEMA_DIR);
		context.initialize();
		
		// Parsing user
		PrismObject<UserType> user = context.parseObject(DOMUtil.getFirstChildElement(dataDoc));
		assertNotNull("No definition for user", user.getDefinition());
	
		System.out.println("Parsed root object:");
		System.out.println(user.dump());

		// TODO: assert user

		// Try javax schemas by validating a XML file
		Schema javaxSchema = reg.getJavaxSchema();
		assertNotNull(javaxSchema);
		Validator validator = javaxSchema.newValidator();
		DOMResult validationResult = new DOMResult();
		validator.validate(new DOMSource(dataDoc),validationResult);
//		System.out.println("Validation result:");
//		System.out.println(DOMUtil.serializeDOMToString(validationResult.getNode()));
		
	}
	
	@Test
	public void testUserExtensionSchemaAsObjectSchema() throws SAXException, IOException, SchemaException {
		System.out.println("===[ testUserExtensionSchemaAsObjectSchema ]===");

		PrismContext context = constructPrismContext();
		SchemaRegistry reg = context.getSchemaRegistry();
		reg.registerPrismSchemasFromDirectory(EXTRA_SCHEMA_DIR);
		context.initialize();

		// Try to fetch object schema, the extension of UserType should be there
		PrismSchema schema = reg.getObjectSchema();
		System.out.println("Object schema:");
		System.out.println(schema.dump());
		
		PrismObjectDefinition<UserType> userDef = schema.findObjectDefinitionByType(USER_TYPE_QNAME);
		
		System.out.println("User definition:");
		System.out.println(userDef.dump());
		
		assertUserDefinition(userDef);
		
		PrismObjectDefinition<UserType> usedDefByClass = schema.findObjectDefinitionByCompileTimeClass(UserType.class);
		assertUserDefinition(usedDefByClass);
		
		PrismObjectDefinition<UserType> userDefByElement = schema.findObjectDefinitionByElementName(USER_QNAME);
		assertUserDefinition(userDefByElement);
		
	}

	@Test
	public void testUserExtensionSchemaSchemaRegistry() throws SAXException, IOException, SchemaException {
		System.out.println("===[ testUserExtensionSchemaAsObjectSchema ]===");

		PrismContext context = constructPrismContext();
		SchemaRegistry reg = context.getSchemaRegistry();
		reg.registerPrismSchemasFromDirectory(EXTRA_SCHEMA_DIR);
		context.initialize();
		
		PrismObjectDefinition<UserType> userDef = reg.findObjectDefinitionByType(USER_TYPE_QNAME);
		
		System.out.println("User definition:");
		System.out.println(userDef.dump());
		
		assertUserDefinition(userDef);
		
		PrismObjectDefinition<UserType> usedDefByClass = reg.findObjectDefinitionByCompileTimeClass(UserType.class);
		assertUserDefinition(usedDefByClass);
		
		PrismObjectDefinition<UserType> userDefByElement = reg.findObjectDefinitionByElementName(USER_QNAME);
		assertUserDefinition(userDefByElement);
	}
	
	private void assertUserDefinition(PrismObjectDefinition<UserType> userDef) {
		PrismContainerDefinition extDef = userDef.findContainerDefinition(USER_EXTENSION_QNAME);
		assertTrue("Extension is not dynamic", extDef.isRuntimeSchema());
		assertEquals("Wrong extension type", USER_EXTENSION_TYPE_QNAME, extDef.getTypeName());
		assertEquals("Wrong extension displayOrder", (Integer)1000, extDef.getDisplayOrder());

		PrismPropertyDefinition barPropDef = extDef.findPropertyDefinition(USER_EXT_BAR_ELEMENT);
		assertNotNull("No 'bar' definition in user extension", barPropDef);
		PrismAsserts.assertDefinition(barPropDef, USER_EXT_BAR_ELEMENT, DOMUtil.XSD_STRING, 1, 1);
		assertTrue("'bar' not indexed", barPropDef.isIndexed());
		
		PrismPropertyDefinition foobarPropDef = extDef.findPropertyDefinition(USER_EXT_FOOBAR_ELEMENT);
		assertNotNull("No 'foobar' definition in user extension", foobarPropDef);
		PrismAsserts.assertDefinition(foobarPropDef, USER_EXT_FOOBAR_ELEMENT, DOMUtil.XSD_STRING, 0, 1);
		assertNull("'foobar' has non-null indexed flag", foobarPropDef.isIndexed());

		PrismPropertyDefinition multiPropDef = extDef.findPropertyDefinition(USER_EXT_MULTI_ELEMENT);
		assertNotNull("No 'multi' definition in user extension", multiPropDef);
		PrismAsserts.assertDefinition(multiPropDef, USER_EXT_MULTI_ELEMENT, DOMUtil.XSD_STRING, 0, -1);
		assertFalse("'multi' not indexed", multiPropDef.isIndexed());
	}

	/**
	 * Test if a schema directory can be loaded to the schema registry. This contains definition of
	 * user extension, therefore check if it is applied to the user definition. 
	 */
	@Test
	public void testTypeOverride() throws SAXException, IOException, SchemaException {
		System.out.println("===[ testTypeOverride ]===");
		
		PrismContext context = constructPrismContext();
		SchemaRegistry reg = context.getSchemaRegistry();
		reg.registerPrismSchemasFromDirectory(EXTRA_SCHEMA_DIR);
		context.initialize();
		
		PrismSchema schema = reg.getSchema(NS_ROOT);
		System.out.println("Parsed root schema:");
		System.out.println(schema.dump());
		
		PrismContainerDefinition rootContDef = schema.findContainerDefinitionByElementName(new QName(NS_ROOT,"root"));
		assertNotNull("Not <root> definition", rootContDef);
		PrismContainerDefinition extensionContDef = rootContDef.findContainerDefinition(new QName(NS_FOO, "extension"));
		assertNotNull("Not <extension> definition", extensionContDef);
		assertEquals("Wrong <extension> type", new QName(NS_ROOT, "MyExtensionType"), extensionContDef.getTypeName());
		
	}

}
