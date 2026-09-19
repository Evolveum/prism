/*
 * Copyright (c) 2010-2024 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;

import javax.xml.namespace.QName;

import org.testng.annotations.Test;

import com.evolveum.midpoint.prism.foo.UserType;
import com.evolveum.midpoint.prism.impl.ParsingContextImpl;
import com.evolveum.midpoint.prism.marshaller.XNodeProcessorEvaluationMode;
import com.evolveum.midpoint.util.exception.SchemaException;

/**
 * Tests for the compatibility (relaxed) mode of operation enabled only for specific object types.
 */
public class TestTypeSpecificCompatMode extends AbstractPrismTest {

    public static final String TEST_DIR = "src/test/resources/common/xml";

    public static final File WRONG_ITEM_USER_FILE = new File(TEST_DIR + "/user-wrong-item.xml");
    public static final File WRONG_ITEM_ACCOUNT_FILE = new File(TEST_DIR + "/account-wrong-item.xml");
    public static final File WRONG_NESTED_ACCOUNT_USER_FILE = new File(TEST_DIR + "/user-wrong-nested-account.xml");

    private QName getUserTypeQName() {
        return getUserTypeDefinition().getComplexTypeDefinition().getTypeName();
    }

    private QName getAccountTypeQName() {
        return new QName(PrismInternalTestUtil.NS_FOO, "AccountType");
    }

    @Test(expectedExceptions = SchemaException.class)
    public void test010StrictThrowsByDefault() throws Exception {
        // GIVEN a user containing an unknown item
        PrismContext prismContext = getPrismContext();

        // WHEN+THEN parsing in the default (strict) mode fails
        prismContext.parserFor(WRONG_ITEM_USER_FILE).parse();
    }

    @Test
    public void test020StrictWithCompatForUserTypeParses() throws Exception {
        // GIVEN a user containing an unknown item
        PrismContext prismContext = getPrismContext();

        // WHEN parsing in strict mode, with the compat mode enabled only for the user type
        PrismObject<UserType> user = prismContext.parserFor(WRONG_ITEM_USER_FILE)
                .strict()
                .compatFor(getUserTypeQName())
                .parse();

        // THEN the object is parsed
        assertNotNull(user);
        System.out.println("User:");
        System.out.println(user.debugDump());
    }

    @Test
    public void test030CompatForUserTypeRecordsWarnings() throws Exception {
        // GIVEN a user containing an unknown item
        PrismContext prismContext = getPrismContext();
        ParsingContext context = prismContext.getDefaultParsingContext();
        context.enableCompatFor(getUserTypeQName());

        // WHEN parsing with that context
        PrismObject<UserType> user = prismContext.parserFor(WRONG_ITEM_USER_FILE)
                .context(context)
                .parse();

        // THEN the object is parsed and the inconsistency is reported as a warning
        assertNotNull(user);
        assertTrue(context.hasWarnings());
    }

    @Test(expectedExceptions = SchemaException.class)
    public void test040CompatForOtherTypeDoesNotHelp() throws Exception {
        // GIVEN an account containing an unknown item
        PrismContext prismContext = getPrismContext();

        // WHEN+THEN the compat mode enabled for another type (user) does not apply; parsing fails
        prismContext.parserFor(WRONG_ITEM_ACCOUNT_FILE)
                .strict()
                .compatFor(getUserTypeQName())
                .parse();
    }

    @Test
    public void test050CompatForAccountTypeParsesAccount() throws Exception {
        // GIVEN an account containing an unknown item
        PrismContext prismContext = getPrismContext();

        // WHEN parsing with the compat mode enabled only for the account type
        PrismObject<?> account = prismContext.parserFor(WRONG_ITEM_ACCOUNT_FILE)
                .strict()
                .compatFor(getAccountTypeQName())
                .parse();

        // THEN the object is parsed
        assertNotNull(account);
        System.out.println("Account:");
        System.out.println(account.debugDump());
    }

    @Test(expectedExceptions = SchemaException.class)
    public void test060StrictThrowsForWrongNestedAccount() throws Exception {
        // GIVEN a clean user containing a composite account with an unknown item
        PrismContext prismContext = getPrismContext();

        // WHEN+THEN parsing in strict mode fails (on the nested account)
        prismContext.parserFor(WRONG_NESTED_ACCOUNT_USER_FILE).parse();
    }

    @Test
    public void test070CompatForNestedAccountTypeParses() throws Exception {
        // GIVEN a clean user containing a composite account with an unknown item
        PrismContext prismContext = getPrismContext();

        // WHEN parsing in strict mode, with the compat mode enabled only for the account type
        // (the user itself is strict; only the nested account object of the configured type is lenient)
        PrismObject<UserType> user = prismContext.parserFor(WRONG_NESTED_ACCOUNT_USER_FILE)
                .strict()
                .compatFor(getAccountTypeQName())
                .parse();

        // THEN the object is parsed
        assertNotNull(user);
        System.out.println("User:");
        System.out.println(user.debugDump());
    }

    @Test
    public void test080CompatForUserTypeCoversNestedSubtree() throws Exception {
        // GIVEN a clean user containing a composite account with an unknown item
        PrismContext prismContext = getPrismContext();

        // WHEN parsing with the compat mode enabled only for the user type
        // (the nested account is of another, non-configured type, but the whole subtree of the
        // compatible object is parsed leniently)
        PrismObject<UserType> user = prismContext.parserFor(WRONG_NESTED_ACCOUNT_USER_FILE)
                .strict()
                .compatFor(getUserTypeQName())
                .parse();

        // THEN the object is parsed
        assertNotNull(user);
        System.out.println("User:");
        System.out.println(user.debugDump());
    }

    @Test
    public void test090CompatForUserTypeParsesJson() throws Exception {
        // GIVEN a user containing an unknown item, as JSON
        PrismContext prismContext = getPrismContext();
        String json = """
                {
                  "user" : {
                    "oid": "c0c010c0-d34d-b33f-f00d-111111113333",
                    "name": "will",
                    "extension": {
                      "http://midpoint.evolveum.com/xml/ns/test/extension#stringType_Wrong": "FOObar"
                    },
                    "fullName": "Will Turner"
                  }
                }
                """;

        // WHEN+THEN parsing in strict mode with per-type compat succeeds
        PrismObject<UserType> user = prismContext.parserFor(json)
                .json()
                .strict()
                .compatFor(getUserTypeQName())
                .parse();
        assertNotNull(user);

        // AND parsing in plain strict mode fails
        try {
            prismContext.parserFor(json).json().strict().parse();
            throw new AssertionError("The strict parsing of the JSON user was supposed to fail");
        } catch (SchemaException expected) {
            // expected
        }
    }

    @Test
    public void test100ParsingContextModes() {
        // GIVEN a parsing context
        QName userType = getUserTypeQName();
        QName accountType = getAccountTypeQName();
        ParsingContext context = ParsingContextImpl.createDefault();

        // WHEN nothing is configured, the mode is strict
        assertTrue(context.isStrict());
        assertFalse(context.isCompat());
        assertEquals(XNodeProcessorEvaluationMode.STRICT, context.getEvaluationMode());

        // WHEN the compat mode is enabled for the user type
        context.enableCompatFor(userType);
        assertTrue(context.isCompatFor(userType));
        assertFalse(context.isCompatFor(accountType));
        assertFalse(context.isCompatFor(null));
        assertEquals(1, context.getCompatForTypes().size());

        // THEN the global mode remains strict
        assertTrue(context.isStrict());
        assertFalse(context.isCompat());
        assertEquals(XNodeProcessorEvaluationMode.STRICT, context.getEvaluationMode());

        // WHEN parsing an account (non-compatible type) starts
        context.pushObjectType(accountType);
        assertFalse(context.isCompat());
        assertTrue(context.isStrict());

        // WHEN a compatible user starts to be parsed (nested or top-level)
        context.pushObjectType(userType);
        assertTrue(context.isCompat());
        assertFalse(context.isStrict());
        assertEquals(XNodeProcessorEvaluationMode.COMPAT, context.getEvaluationMode());

        // THEN the mode is inherited by the whole subtree, even when another object type is on the stack
        context.pushObjectType(accountType);
        assertTrue(context.isCompat());
        context.popObjectType();
        assertTrue(context.isCompat());

        // AND after the compatible user finishes, only the (non-compatible) account remains on the stack
        context.popObjectType();
        assertFalse(context.isCompat());
        context.popObjectType();

        // AND after all objects are parsed, the mode is strict again
        assertFalse(context.isCompat());
        assertTrue(context.isStrict());
        assertEquals(XNodeProcessorEvaluationMode.STRICT, context.getEvaluationMode());

        // AND the clone preserves the configuration
        ParsingContext clone = context.clone();
        assertTrue(clone.isCompatFor(userType));
        assertEquals(context.getCompatForTypes(), clone.getCompatForTypes());

        // AND the global compat mode wins over everything
        context.compat();
        assertTrue(context.isCompat());
    }
}
