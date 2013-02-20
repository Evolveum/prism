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

import static org.testng.AssertJUnit.assertFalse;
import static com.evolveum.midpoint.prism.PrismInternalTestUtil.*;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertEquals;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.evolveum.midpoint.prism.foo.ObjectFactory;
import com.evolveum.midpoint.prism.foo.UserType;
import com.evolveum.midpoint.prism.schema.PrismSchema;
import com.evolveum.midpoint.prism.util.PrismAsserts;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.PrettyPrinter;
import com.evolveum.midpoint.util.exception.SchemaException;

/**
 * @author semancik
 *
 */
public class TestPrismSchemaConstruction {
	
	private static final String NS_MY_SCHEMA = "http://midpoint.evolveum.com/xml/ns/test/my-1";
	private static final String WEAPON_TYPE_LOCAL_NAME = "WeaponType";
	private static final QName WEAPON_TYPE_QNAME = new QName(NS_MY_SCHEMA, WEAPON_TYPE_LOCAL_NAME);
	private static final QName WEAPON_KIND_QNAME = new QName(NS_MY_SCHEMA, "kind");
	private static final String WEAPON_LOCAL_NAME = "weapon";
	private static final String WEAPON_BRAND_LOCAL_NAME = "brand";
	private static final int SCHEMA_ROUNDTRIP_LOOP_ATTEMPTS = 10;
	private static final String WEAPON_PASSWORD_LOCAL_NAME = "password";
	private static final String WEAPON_BLADE_LOCAL_NAME = "blade";
	
	
	@BeforeSuite
	public void setupDebug() {
		PrettyPrinter.setDefaultNamespacePrefix(DEFAULT_NAMESPACE_PREFIX);
	}

	@Test
	public void testConstructSchema() throws SchemaException, SAXException, IOException {
		System.out.println("===[ testConstructSchema ]===");
		
		// GIVEN
		PrismContext ctx = constructInitializedPrismContext();
		
		// WHEN
		PrismSchema schema = constructSchema(ctx);
		
		// THEN
		System.out.println("Constructed schema");
		System.out.println(schema.dump());
		assertSchema(schema);
	}
	
	@Test
	public void testSchemaRoundtrip() throws Exception {
		System.out.println("===[ testSchemaRoundtrip ]===");
		
		// GIVEN
		PrismContext ctx = constructInitializedPrismContext();
		
		schemaRoundtrip(ctx);
	}
	
	@Test
	public void testSchemaRoundtripLoopShareContext() throws Exception {
		System.out.println("===[ testSchemaRoundtripLoopShareContext ]===");
		
		PrismContext ctx = constructInitializedPrismContext();
		for(int i=0; i < SCHEMA_ROUNDTRIP_LOOP_ATTEMPTS; i++) {
			System.out.println("\n--- attempt "+i+"---");
			schemaRoundtrip(ctx);
		}
	}

	@Test
	public void testSchemaRoundtripLoopNewContext() throws Exception {
		System.out.println("===[ testSchemaRoundtripLoopNewContext ]===");
		
		for(int i=0; i < SCHEMA_ROUNDTRIP_LOOP_ATTEMPTS; i++) {
			System.out.println("\n--- attempt "+i+"---");
			PrismContext ctx = constructInitializedPrismContext();
			schemaRoundtrip(ctx);
		}
	}

	
	private void schemaRoundtrip(PrismContext ctx) throws SchemaException, SAXException, IOException {
		
		PrismSchema schema = constructSchema(ctx);
		assertSchema(schema);
		
		// WHEN
		Document xsdDocument = schema.serializeToXsd();
		
		// THEN
		Element xsdElement = DOMUtil.getFirstChildElement(xsdDocument);
		System.out.println("Serialized schema");
		System.out.println(DOMUtil.serializeDOMToString(xsdElement));
		
		assertPrefix("xsd", xsdElement);
		Element displayNameElement = DOMUtil.findElementRecursive(xsdElement, PrismConstants.A_DISPLAY_NAME);
		assertPrefix(PrismConstants.PREFIX_NS_ANNOTATION, displayNameElement);
		
		// re-parse
		PrismSchema reparsedSchema = PrismSchema.parse(xsdElement, "serialized schema", ctx);
		System.out.println("Re-parsed schema");
		System.out.println(reparsedSchema.dump());
		assertSchema(reparsedSchema);
	}

	private PrismSchema constructSchema(PrismContext prismContext) {
		PrismSchema schema = new PrismSchema(NS_MY_SCHEMA, prismContext);
		
		ComplexTypeDefinition weaponTypeDef = schema.createComplexTypeDefinition(WEAPON_TYPE_QNAME);
		PrismPropertyDefinition kindPropertyDef = weaponTypeDef.createPropertyDefinifion(WEAPON_KIND_QNAME, DOMUtil.XSD_STRING);
		kindPropertyDef.setDisplayName("Weapon kind");
		weaponTypeDef.createPropertyDefinition(WEAPON_BRAND_LOCAL_NAME, PrismInternalTestUtil.WEAPONS_WEAPON_BRAND_TYPE_QNAME);
		weaponTypeDef.createPropertyDefinition(WEAPON_PASSWORD_LOCAL_NAME, PrismInternalTestUtil.DUMMY_PROTECTED_STRING_TYPE);
		weaponTypeDef.createPropertyDefinition(WEAPON_BLADE_LOCAL_NAME, PrismInternalTestUtil.EXTENSION_BLADE_TYPE_QNAME);
		
		schema.createPropertyContainerDefinition(WEAPON_LOCAL_NAME, WEAPON_TYPE_LOCAL_NAME);
		
		return schema;
	}
	
	private void assertSchema(PrismSchema schema) {
		assertNotNull("Schema is null", schema);		
		assertEquals("Wrong schema namespace", NS_MY_SCHEMA, schema.getNamespace());
		Collection<Definition> definitions = schema.getDefinitions();
		assertNotNull("Null definitions", definitions);
		assertFalse("Empty definitions", definitions.isEmpty());
		assertEquals("Unexpected number of definitions in schema", 2, definitions.size());
		
		Iterator<Definition> schemaDefIter = definitions.iterator();
		ComplexTypeDefinition weaponTypeDef = (ComplexTypeDefinition)schemaDefIter.next();
		assertEquals("Unexpected number of definitions in weaponTypeDef", 4, weaponTypeDef.getDefinitions().size());
		Iterator<ItemDefinition> weaponTypeDefIter = weaponTypeDef.getDefinitions().iterator();
		PrismPropertyDefinition kindPropertyDef = (PrismPropertyDefinition) weaponTypeDefIter.next();
		PrismAsserts.assertDefinition(kindPropertyDef, WEAPON_KIND_QNAME, DOMUtil.XSD_STRING, 1, 1);
		assertEquals("Wrong kindPropertyDef displayName", "Weapon kind", kindPropertyDef.getDisplayName());
		
		PrismPropertyDefinition brandPropertyDef = (PrismPropertyDefinition) weaponTypeDefIter.next();
		PrismAsserts.assertDefinition(brandPropertyDef, new QName(NS_MY_SCHEMA, WEAPON_BRAND_LOCAL_NAME), 
				PrismInternalTestUtil.WEAPONS_WEAPON_BRAND_TYPE_QNAME, 1, 1);
		
		PrismPropertyDefinition passwordPropertyDef = (PrismPropertyDefinition) weaponTypeDefIter.next();
		PrismAsserts.assertDefinition(passwordPropertyDef, new QName(NS_MY_SCHEMA, WEAPON_PASSWORD_LOCAL_NAME), 
				PrismInternalTestUtil.DUMMY_PROTECTED_STRING_TYPE, 1, 1);
		
		PrismPropertyDefinition bladePropertyDef = (PrismPropertyDefinition) weaponTypeDefIter.next();
		PrismAsserts.assertDefinition(bladePropertyDef, new QName(NS_MY_SCHEMA, WEAPON_BLADE_LOCAL_NAME), 
				PrismInternalTestUtil.EXTENSION_BLADE_TYPE_QNAME, 1, 1);
		
		PrismContainerDefinition<?> weaponContDef = (PrismContainerDefinition<?>)schemaDefIter.next();
		assertEquals("Wrong complex type def in weaponContDef", weaponTypeDef, weaponContDef.getComplexTypeDefinition());
		// TODO: more asserts
		
	}
	
	private void assertPrefix(String expectedPrefix, Element element) {
		assertEquals("Wrong prefix on element "+DOMUtil.getQName(element), expectedPrefix, element.getPrefix());
	}	
	
}
