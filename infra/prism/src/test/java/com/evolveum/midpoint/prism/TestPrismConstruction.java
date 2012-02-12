/**
 * Copyright (c) 2011 Evolveum
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1 or
 * CDDLv1.0.txt file in the source code distribution.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * Portions Copyrighted 2011 [name of copyright owner]
 */
package com.evolveum.midpoint.prism;

import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertEquals;
import java.io.IOException;

import javax.xml.namespace.QName;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import com.evolveum.midpoint.prism.foo.ObjectFactory;
import com.evolveum.midpoint.prism.foo.UserType;
import com.evolveum.midpoint.schema.exception.SchemaException;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.DebugUtil;

/**
 * @author semancik
 *
 */
public class TestPrismConstruction {
	
	private static final String NS_FOO = "http://midpoint.evolveum.com/xml/ns/test/foo-1.xsd";
	private static final String NS_BAR = "http://www.example.com/bar";
	private static final String USER_OID = "1234567890";
	
	private static final QName USER_FULLNAME_QNAME = new QName(NS_FOO,"fullName");
	private static final QName USER_ACTIVATION_QNAME = new QName(NS_FOO,"activation");
	private static final QName USER_ENABLED_QNAME = new QName(NS_FOO,"enabled");
	private static final PropertyPath USER_ENABLED_PATH = new PropertyPath(USER_ACTIVATION_QNAME, USER_ENABLED_QNAME);
	
	private static final QName ACTIVATION_TYPE_QNAME = new QName(NS_FOO,"ActivationType");
	
	@BeforeSuite
	public void setupDebug() {
		DebugUtil.setDefaultNamespacePrefix("http://midpoint.evolveum.com/xml/ns");
	}

	/**
	 * Construct object with schema. Starts by instantiating a definition and working downwards.
	 * All the items in the object should have proper definition. 
	 */
	@Test
	public void testConstructionWithSchema() throws SchemaException, SAXException, IOException {
		System.out.println("===[ testConstructionWithSchema ]===");
		
		// GIVEN
		PrismContext ctx = constructPrismContext();
		PrismObjectDefinition<UserType> userDefinition = ctx.getSchemaRegistry().getObjectSchema().findObjectDefinitionByElementName(new QName(NS_FOO,"user"));
		
		// WHEN
		PrismObject<UserType> user = userDefinition.instantiate();
		user.setOid(USER_OID);
		
		// fullName
		PrismProperty fullNameProperty = user.findOrCreateProperty(USER_FULLNAME_QNAME);
		assertEquals(USER_FULLNAME_QNAME, fullNameProperty.getName());
		assertDefinition(fullNameProperty.getDefinition(), DOMUtil.XSD_STRING, 1, 1);
		fullNameProperty.setValue(new PrismPropertyValue<String>("Sir Fancis Drake"));
		PrismProperty fullNamePropertyAgain = user.findOrCreateProperty(USER_FULLNAME_QNAME);
		// The "==" is there by purpose. We really want to make sure that is the same *instance*, that is was not created again
		assertTrue("Property not the same", fullNameProperty == fullNamePropertyAgain);
		
		// activation
		PrismContainer activationContainer = user.findOrCreatePropertyContainer(USER_ACTIVATION_QNAME);
		assertEquals(USER_ACTIVATION_QNAME, activationContainer.getName());
		assertDefinition(activationContainer.getDefinition(), ACTIVATION_TYPE_QNAME, 0, 1);
		PrismContainer activationContainerAgain = user.findOrCreatePropertyContainer(USER_ACTIVATION_QNAME);
		// The "==" is there by purpose. We really want to make sure that is the same *instance*, that is was not created again
		assertTrue("Property not the same", activationContainer == activationContainerAgain);
		
		// activation/enabled
		PrismProperty enabledProperty = user.findOrCreateProperty(USER_ENABLED_PATH);
		assertEquals(USER_ENABLED_QNAME, enabledProperty.getName());
		assertDefinition(enabledProperty.getDefinition(), DOMUtil.XSD_BOOLEAN, 1, 1);
		enabledProperty.setValue(new PrismPropertyValue<Boolean>(true));
		PrismProperty enabledPropertyAgain = activationContainer.findOrCreateProperty(USER_ENABLED_QNAME);
		// The "==" is there by purpose. We really want to make sure that is the same *instance*, that is was not created again
		assertTrue("Property not the same", enabledProperty == enabledPropertyAgain);
		
		// TODO
		
		// THEN
		System.out.println("User:");
		System.out.println(user.dump());
		
		assertEquals("Wrong OID", USER_OID, user.getOid());
		// fullName
		fullNameProperty = user.findProperty(USER_FULLNAME_QNAME);
		assertDefinition(fullNameProperty.getDefinition(), DOMUtil.XSD_STRING, 1, 1);
		assertEquals("Wrong fullname", "Sir Fancis Drake", fullNameProperty.getValue().getValue());
		// activation
		activationContainer = user.findPropertyContainer(USER_ACTIVATION_QNAME);
		assertEquals(USER_ACTIVATION_QNAME, activationContainer.getName());
		assertDefinition(activationContainer.getDefinition(), ACTIVATION_TYPE_QNAME, 0, 1);
		// activation/enabled
		enabledProperty = user.findProperty(USER_ENABLED_PATH);
		assertEquals(USER_ENABLED_QNAME, enabledProperty.getName());
		assertDefinition(enabledProperty.getDefinition(), DOMUtil.XSD_BOOLEAN, 1, 1);
		assertEquals("Wrong enabled", true, enabledProperty.getValue().getValue());
	}

	private void assertDefinition(ItemDefinition definition, QName type, int minOccurs, int maxOccurs) {
		assertEquals("Wrong definition type", type, definition.getTypeName());
		assertEquals("Wrong definition minOccurs", minOccurs, definition.getMinOccurs());
		assertEquals("Wrong definition maxOccurs", maxOccurs, definition.getMaxOccurs());
	}

	/**
	 * Construct object without schema. Starts by creating object "out of the blue" and
	 * the working downwards. 
	 */
	@Test
	public void testDefinitionlessConstruction() throws SchemaException, SAXException, IOException {
		System.out.println("===[ testDefinitionlessConstruction ]===");
		
		// GIVEN
		// No context needed
		// PrismContext ctx = constructPrismContext();
		
		// WHEN
		// TODO
	}

	private PrismContext constructPrismContext() throws SchemaException, SAXException, IOException {
		SchemaRegistry schemaRegistry = new SchemaRegistry();
		DynamicNamespacePrefixMapper prefixMapper = new GlobalDynamicNamespacePrefixMapper();
		// Set default namespace?
		schemaRegistry.setNamespacePrefixMapper(prefixMapper);
		schemaRegistry.registerPrismSchemaResource("xml/ns/test/foo-1.xsd", "foo", ObjectFactory.class.getPackage());
		schemaRegistry.setObjectSchemaNamespace(NS_FOO);
		schemaRegistry.initialize();
		
		PrismContext context = PrismContext.create(schemaRegistry);
		return context;
	}
}
