/*
 * Copyright (c) 2010-2017 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism;

import static org.testng.AssertJUnit.*;

import static com.evolveum.midpoint.prism.PrismInternalTestUtil.*;

import java.io.IOException;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.assertj.core.api.Assertions;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.evolveum.midpoint.prism.foo.AccountType;
import com.evolveum.midpoint.prism.foo.AssignmentType;
import com.evolveum.midpoint.prism.foo.UserType;
import com.evolveum.midpoint.prism.impl.PrismContextImpl;
import com.evolveum.midpoint.prism.schema.PrismSchema;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.prism.util.PrismAsserts;
import com.evolveum.midpoint.prism.xml.DynamicNamespacePrefixMapper;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.PrettyPrinter;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;

/**
 * @author semancik
 */
public class TestPrismContext extends AbstractPrismTest {

    private static final String NS_FOO = "http://midpoint.evolveum.com/xml/ns/test/foo-1.xsd";

    @BeforeSuite
    public void setupDebug() {
        PrettyPrinter.addDefaultNamespacePrefix("http://midpoint.evolveum.com/xml/ns");
    }

    @Test
    public void testPrefixMapper() throws SchemaException, SAXException, IOException {
        // WHEN
        PrismContextImpl prismContext = constructInitializedPrismContext();

        // THEN
        assertNotNull("No prism context", prismContext);

        SchemaRegistry schemaRegistry = prismContext.getSchemaRegistry();
        assertNotNull("No schema registry in context", schemaRegistry);

        DynamicNamespacePrefixMapper prefixMapper = schemaRegistry.getNamespacePrefixMapper();
        System.out.println("Prefix mapper:");
        System.out.println(DebugUtil.dump(prefixMapper));

        assertEquals("Wrong foo prefix", "", prefixMapper.getPrefix(NS_FOO));
        assertEquals("Wrong xsd prefix", DOMUtil.NS_W3C_XML_SCHEMA_PREFIX, prefixMapper.getPrefix(XMLConstants.W3C_XML_SCHEMA_NS_URI));

    }

    @Test
    public void testBasicSchemas() throws SchemaException, SAXException, IOException {
        // WHEN
        PrismContext prismContext = constructInitializedPrismContext();

        // THEN
        assertNotNull("No prism context", prismContext);

        SchemaRegistry schemaRegistry = prismContext.getSchemaRegistry();
        assertNotNull("No schema registry in context", schemaRegistry);

        System.out.println("Schema registry:");
        System.out.println(schemaRegistry.debugDump());

        PrismSchema fooSchema = schemaRegistry.findSchemaByNamespace(NS_FOO);
        System.out.println("Foo schema:");
        System.out.println(fooSchema.debugDump());

        // Assert USER definition
        PrismObjectDefinition<UserType> userDefinition = fooSchema.findObjectDefinitionByElementName(new QName(NS_FOO, "user"));
        assertNotNull("No user definition", userDefinition);
        System.out.println("User definition:");
        System.out.println(userDefinition.debugDump());

        PrismObjectDefinition<UserType> userDefinitionByClass = schemaRegistry.findObjectDefinitionByCompileTimeClass(UserType.class);
        assertTrue("Different user def", userDefinition == userDefinitionByClass);

        assertUserDefinition(userDefinition);

        // Assert ACCOUNT definition
        PrismObjectDefinition<AccountType> accountDefinition = fooSchema.findObjectDefinitionByElementName(new QName(NS_FOO, "account"));
        assertNotNull("No account definition", accountDefinition);
        System.out.println("Account definition:");
        System.out.println(accountDefinition.debugDump());

        PrismObjectDefinition<AccountType> accountDefinitionByClass = schemaRegistry.findObjectDefinitionByCompileTimeClass(AccountType.class);
        assertTrue("Different user def", accountDefinition == accountDefinitionByClass);

        assertAccountDefinition(accountDefinition);
    }

    private void assertUserDefinition(PrismObjectDefinition<UserType> userDefinition) {

        assertEquals("Wrong compile-time class in user definition", UserType.class, userDefinition.getCompileTimeClass());
        PrismAsserts.assertPropertyDefinition(userDefinition, USER_NAME_QNAME, PolyStringType.COMPLEX_TYPE, 0, 1);
        PrismAsserts.assertItemDefinitionDisplayName(userDefinition, USER_NAME_QNAME, "ObjectType.name");
        PrismAsserts.assertItemDefinitionDisplayOrder(userDefinition, USER_NAME_QNAME, 0);
        PrismAsserts.assertEmphasized(userDefinition, USER_NAME_QNAME, true);
        PrismAsserts.assertItemDefinitionHelp(userDefinition, USER_NAME_QNAME, "Short unique name of the object");
        PrismAsserts.assertPropertyDefinition(userDefinition, USER_DESCRIPTION_QNAME, DOMUtil.XSD_STRING, 0, 1);
        PrismAsserts.assertEmphasized(userDefinition, USER_DESCRIPTION_QNAME, false);
        PrismAsserts.assertPropertyDefinition(userDefinition, USER_FULLNAME_QNAME, DOMUtil.XSD_STRING, 1, 1);
        PrismAsserts.assertEmphasized(userDefinition, USER_FULLNAME_QNAME, true);
        PrismAsserts.assertPropertyDefinition(userDefinition, USER_GIVENNAME_QNAME, DOMUtil.XSD_STRING, 0, 1);
        PrismAsserts.assertEmphasized(userDefinition, USER_GIVENNAME_QNAME, false);
        PrismAsserts.assertPropertyDefinition(userDefinition, USER_FAMILYNAME_QNAME, DOMUtil.XSD_STRING, 0, 1);
        PrismAsserts.assertPropertyDefinition(userDefinition, USER_ADDITIONALNAMES_QNAME, DOMUtil.XSD_STRING, 0, -1);
        assertFalse("User definition is marked as runtime", userDefinition.isRuntimeSchema());

        PrismContainerDefinition extensionContainer = userDefinition.findContainerDefinition(USER_EXTENSION_QNAME);
        //PrismAsserts.assertDefinition(extensionContainer, USER_EXTENSION_QNAME, DOMUtil.XSD_ANY, 0, 1);
        assertTrue("Extension is not runtime", extensionContainer.isRuntimeSchema());
        //assertTrue("Extension is not empty", extensionContainer.getDefinitions().isEmpty());
        PrismAsserts.assertItemDefinitionDisplayName(userDefinition, USER_EXTENSION_QNAME, "ObjectType.extension");
        PrismAsserts.assertItemDefinitionDisplayOrder(userDefinition, USER_EXTENSION_QNAME, 1000);
        PrismAsserts.assertItemDefinitionHelp(userDefinition, USER_EXTENSION_QNAME, "Object extension contains extra properties");

        PrismContainerDefinition activationContainer = userDefinition.findContainerDefinition(USER_ACTIVATION_QNAME);
        PrismAsserts.assertDefinition(activationContainer, USER_ACTIVATION_QNAME, ACTIVATION_TYPE_QNAME, 0, 1);
        assertFalse("Activation is runtime", activationContainer.isRuntimeSchema());
        assertTrue("Activation is NOT operational", activationContainer.isOperational());
        assertEquals("Activation size", 3, activationContainer.getDefinitions().size());
        PrismAsserts.assertPropertyDefinition(activationContainer, USER_ENABLED_QNAME, DOMUtil.XSD_BOOLEAN, 0, 1);
        PrismAsserts.assertPropertyDefinition(activationContainer, USER_VALID_FROM_QNAME, DOMUtil.XSD_DATETIME, 0, 1);
        PrismAsserts.assertPropertyDefinition(activationContainer, USER_VALID_TO_QNAME, DOMUtil.XSD_DATETIME, 0, 1);

        PrismContainerDefinition assignmentContainer = userDefinition.findContainerDefinition(USER_ASSIGNMENT_QNAME);
        PrismAsserts.assertDefinition(assignmentContainer, USER_ASSIGNMENT_QNAME, ASSIGNMENT_TYPE_QNAME, 0, -1);
        assertFalse("Assignment is runtime", assignmentContainer.isRuntimeSchema());
        assertEquals("Wrong compile time class for assignment container", AssignmentType.class, assignmentContainer.getCompileTimeClass());
        assertEquals("Assignment size", 7, assignmentContainer.getDefinitions().size());
        PrismAsserts.assertPropertyDefinition(assignmentContainer, USER_DESCRIPTION_QNAME, DOMUtil.XSD_STRING, 0, 1);
        PrismAsserts.assertPropertyDefinition(assignmentContainer, USER_ACCOUNT_CONSTRUCTION_QNAME, ACCOUNT_CONSTRUCTION_TYPE_QNAME, 0, 1);

        PrismReferenceDefinition accountRefDef = userDefinition.findItemDefinition(USER_ACCOUNTREF_QNAME, PrismReferenceDefinition.class);
        PrismAsserts.assertDefinition(accountRefDef, USER_ACCOUNTREF_QNAME, OBJECT_REFERENCE_TYPE_QNAME, 0, -1);
        assertEquals("Wrong target type in accountRef", ACCOUNT_TYPE_QNAME, accountRefDef.getTargetTypeName());
    }

    private void assertAccountDefinition(PrismObjectDefinition<AccountType> accountDefinition) {

        assertEquals("Wrong compile-time class in account definition", AccountType.class, accountDefinition.getCompileTimeClass());
        PrismAsserts.assertPropertyDefinition(accountDefinition, ACCOUNT_NAME_QNAME, PolyStringType.COMPLEX_TYPE, 0, 1);
        PrismAsserts.assertPropertyDefinition(accountDefinition, ACCOUNT_DESCRIPTION_QNAME, DOMUtil.XSD_STRING, 0, 1);
        assertFalse("Account definition is marked as runtime", accountDefinition.isRuntimeSchema());

        PrismContainerDefinition attributesContainer = accountDefinition.findContainerDefinition(ACCOUNT_ATTRIBUTES_QNAME);
        PrismAsserts.assertDefinition(attributesContainer, ACCOUNT_ATTRIBUTES_QNAME, ATTRIBUTES_TYPE_QNAME, 0, 1);
        assertTrue("Attributes is NOT runtime", attributesContainer.isRuntimeSchema());
    }

    @Test
    public void testExtensionSchema() throws SchemaException, SAXException, IOException {
        // GIVEN
        PrismContext prismContext = constructInitializedPrismContext();
        assertNotNull("No prism context", prismContext);
        SchemaRegistry schemaRegistry = prismContext.getSchemaRegistry();
        assertNotNull("No schema registry in context", schemaRegistry);

        PrismPropertyDefinition ignoredTypeDef = schemaRegistry.findPropertyDefinitionByElementName(EXTENSION_IGNORED_TYPE_ELEMENT);
        PrismAsserts.assertDefinition(ignoredTypeDef, EXTENSION_IGNORED_TYPE_ELEMENT, DOMUtil.XSD_STRING, 0, -1);
        assertTrue("Element " + EXTENSION_IGNORED_TYPE_ELEMENT + " is NOT ignored", ignoredTypeDef.isIgnored());

        PrismPropertyDefinition stringTypeDef = schemaRegistry.findPropertyDefinitionByElementName(EXTENSION_STRING_TYPE_ELEMENT);
        PrismAsserts.assertDefinition(stringTypeDef, EXTENSION_STRING_TYPE_ELEMENT, DOMUtil.XSD_STRING, 0, -1);
        assertFalse("Element " + EXTENSION_STRING_TYPE_ELEMENT + " is ignored", stringTypeDef.isIgnored());

        PrismPropertyDefinition singleStringTypeDef = schemaRegistry.findPropertyDefinitionByElementName(EXTENSION_SINGLE_STRING_TYPE_ELEMENT);
        PrismAsserts.assertDefinition(singleStringTypeDef, EXTENSION_SINGLE_STRING_TYPE_ELEMENT, DOMUtil.XSD_STRING, 0, 1);
        assertFalse("Element " + EXTENSION_SINGLE_STRING_TYPE_ELEMENT + " is ignored", singleStringTypeDef.isIgnored());

        PrismPropertyDefinition intTypeDef = schemaRegistry.findPropertyDefinitionByElementName(EXTENSION_INT_TYPE_ELEMENT);
        PrismAsserts.assertDefinition(intTypeDef, EXTENSION_INT_TYPE_ELEMENT, DOMUtil.XSD_INT, 0, -1);
        assertFalse("Element " + EXTENSION_INT_TYPE_ELEMENT + " is ignored", intTypeDef.isIgnored());

        PrismContainerDefinition meleeContextDefinition = schemaRegistry.findContainerDefinitionByElementName(EXTENSION_MELEE_CONTEXT_ELEMENT);
        PrismAsserts.assertDefinition(meleeContextDefinition, EXTENSION_MELEE_CONTEXT_ELEMENT, EXTENSION_MELEE_CONTEXT_TYPE_QNAME, 0, 1);
        assertTrue("Melee context container is NOT marked as runtime", meleeContextDefinition.isRuntimeSchema());
        PrismReferenceDefinition opponentRefDef = meleeContextDefinition.findReferenceDefinition(EXTENSION_MELEE_CONTEXT_OPPONENT_REF_ELEMENT);
        assertTrue("opponentRef definition is NOT composite", opponentRefDef.isComposite());
    }

    @Test
    public void testSchemaToDom() throws SchemaException, SAXException, IOException {
        // GIVEN
        PrismContext prismContext = constructInitializedPrismContext();
        PrismSchema fooSchema = prismContext.getSchemaRegistry().findSchemaByNamespace(NS_FOO);
        // WHEN
        Document fooXsd = fooSchema.serializeToXsd();

        // THEN
        assertNotNull("No foo XSD DOM", fooXsd);
    }

    @Test
    public void testNaturalKeyAnnotation() throws SchemaException, SAXException, IOException {
        PrismContext prismContext = constructInitializedPrismContext();
        PrismObjectDefinition objectDefinition = prismContext.getSchemaRegistry()
                .findObjectDefinitionByCompileTimeClass(UserType.class);

        PrismContainerDefinition assignmentDefinition = objectDefinition.findContainerDefinition(UserType.F_ASSIGNMENT);
        Assertions.assertThat(assignmentDefinition.getNaturalKeyConstituents())
                .hasSize(1)
                .containsExactly(AssignmentType.F_IDENTIFIER);

        PrismContainerDefinition uselessAssignmentDefinition = objectDefinition.findContainerDefinition(UserType.F_USELESS_ASSIGNMENT);
        Assertions.assertThat(uselessAssignmentDefinition.getNaturalKeyConstituents())
                .hasSize(1)
                .containsExactly(AssignmentType.F_NOTE);
    }
}
