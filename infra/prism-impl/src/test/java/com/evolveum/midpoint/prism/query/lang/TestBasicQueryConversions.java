/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.query.lang;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import static com.evolveum.midpoint.prism.PrismInternalTestUtil.EXTENSION_DATETIME_ELEMENT;
import static com.evolveum.midpoint.prism.PrismInternalTestUtil.EXTENSION_NUM_ELEMENT;

import java.io.File;
import java.io.IOException;
import java.util.function.BiFunction;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.foo.AccountType;
import com.evolveum.midpoint.prism.foo.ActivationType;
import com.evolveum.midpoint.prism.foo.AssignmentType;
import com.evolveum.midpoint.prism.foo.UserType;
import com.evolveum.midpoint.prism.impl.match.MatchingRuleRegistryFactory;
import com.evolveum.midpoint.prism.impl.query.*;
import com.evolveum.midpoint.prism.impl.query.lang.PrismQuerySerializerImpl;
import com.evolveum.midpoint.prism.match.MatchingRuleRegistry;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.prism.query.FullTextFilter;
import com.evolveum.midpoint.prism.query.ObjectFilter;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.prism.query.PrismQuerySerialization;
import com.evolveum.midpoint.prism.query.PrismQuerySerialization.NotSupportedException;
import com.evolveum.midpoint.prism.util.PrismTestUtil;
import com.evolveum.midpoint.prism.xml.XmlTypeConverter;
import com.evolveum.midpoint.prism.xnode.MapXNode;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.exception.SchemaException;

public class TestBasicQueryConversions extends AbstractPrismTest {

    public static final File FILE_USER_JACK_FILTERS =
            new File(PrismInternalTestUtil.COMMON_DIR_XML, "user-jack-filters.xml");

    private static final MatchingRuleRegistry MATCHING_RULE_REGISTRY =
            MatchingRuleRegistryFactory.createRegistry();

    private static final QName A_RELATION = new QName("a-relation");

    private PrismObject<UserType> parseUserJacky() throws SchemaException, IOException {
        return PrismTestUtil.parseObject(FILE_USER_JACK_FILTERS);
    }

    private ObjectFilter parse(String query) throws SchemaException {
        return queryParser().parseFilter(UserType.class, query);
    }

    private void verify(String query, ObjectFilter original) throws SchemaException, IOException {
        verify(query, original, PrismTestUtil.parseObject(FILE_USER_JACK_FILTERS));
    }

    private void verify(String query, ObjectFilter original, boolean checkToString) throws SchemaException, IOException {
        verify(query, original, PrismTestUtil.parseObject(FILE_USER_JACK_FILTERS), checkToString);
    }

    private void verify(String query, ObjectFilter original, PrismObject<?> user) throws SchemaException {
        verify(query, original, user, true);
    }

    private void verify(Class<? extends Containerable> type, String query, ObjectFilter expectedFilter) throws SchemaException, NotSupportedException {
        ObjectFilter dslFilter = queryParser().parseFilter(type, query);
        assertFilterEquals(dslFilter, expectedFilter);

        PrismQuerySerialization toAxiom = getPrismContext().querySerializer().serialize(dslFilter, PrismNamespaceContext.of(UserType.COMPLEX_TYPE.getNamespaceURI()));
        assertEquals(toAxiom.filterText(), query);
        MapXNode xnodes = getPrismContext().getQueryConverter().serializeFilter(expectedFilter);
        ObjectFilter xnodeFilter = getPrismContext().getQueryConverter().parseFilter(xnodes, type);
        assertFilterEquals(xnodeFilter, expectedFilter);
    }



    private void assertFilterEquals(ObjectFilter actual, ObjectFilter expectedFilter) {
        if (!expectedFilter.equals(actual, false)) {
            throw new AssertionError("Filters not equal. Expected: " + expectedFilter + " Actual: " + actual);
        }
    }

    private void verify(String query, ObjectFilter original, PrismObject<?> user, boolean checkToString) throws SchemaException {
        ObjectFilter dslFilter = parse(query);
        //String javaSerialized = serialize(original);
        verify(dslFilter, original, user, checkToString);
        String dslSerialized = serialize(dslFilter);

        //assertEquals(javaSerialized, query);
        if (checkToString) {
            assertEquals(dslSerialized, query);
        }
    }
    private void verify(ObjectFilter dslFilter, ObjectFilter original, PrismObject<?> user, boolean checkToString) throws SchemaException {
        boolean javaResult = ObjectQuery.match(user, original, MATCHING_RULE_REGISTRY);
        boolean dslResult = ObjectQuery.match(user, dslFilter, MATCHING_RULE_REGISTRY);
        if (checkToString) {
            assertEquals(dslFilter.toString(), original.toString());
        }
        assertEquals(dslResult, javaResult, "Filters do not match.");
    }

    private String serialize(ObjectFilter original) {
        PrismQuerySerialization serialization;
        try {
            serialization = new PrismQuerySerializerImpl().serialize(original);
            display(serialization.filterText());
            return serialization.filterText();
        } catch (NotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    public void testMatchAndFilter() throws SchemaException, IOException {
        ObjectFilter filter =
                getPrismContext().queryFor(UserType.class)
                        .item(UserType.F_GIVEN_NAME).eq("Jack").matchingCaseIgnore()
                        .and().item(UserType.F_FULL_NAME).contains("arr")
                        .buildFilter();
        verify("givenName =[stringIgnoreCase] 'Jack' and fullName contains 'arr'", filter);
        verify("givenName =[stringIgnoreCase] 'Jack' AND fullName contains 'arr'", filter, false);
    }

    @Test
    public void testEscapings() throws SchemaException, IOException {
        ObjectFilter filter =
                getPrismContext().queryFor(UserType.class)
                        .item(UserType.F_GIVEN_NAME).eq("Jack").matchingCaseIgnore()
                        .and().item(UserType.F_FULL_NAME).contains("'Arr")
                        .buildFilter();
        verify("givenName =[stringIgnoreCase] 'Jack' and fullName contains '\\'Arr'", filter);
        verify("givenName =[stringIgnoreCase] 'Jack' AND fullName contains '\\'Arr'", filter, false);
    }

    @Test
    public void testPathComparison() throws SchemaException, IOException {
        ObjectFilter dslFilter = parse("fullName not equal givenName");
        boolean match = ObjectQuery.match(parseUserJacky(), dslFilter, MATCHING_RULE_REGISTRY);
        assertTrue(match);
        verify("fullName != givenName", dslFilter);
        verify("fullName!= givenName", dslFilter, false);
        verify("fullName!=givenName", dslFilter, false);

    }

    @Test
    public void testFullText() throws SchemaException {
        FullTextFilter filter = FullTextFilterImpl.createFullText("jack");
        ObjectFilter dslFilter = parse(". fullText 'jack'");
        assertEquals(dslFilter.toString(), filter.toString());

    }

    @Test   // MID-4173
    public void testExistsPositive() throws Exception {
        ObjectFilter filter = getPrismContext().queryFor(UserType.class)
                .exists(UserType.F_ASSIGNMENT)
                .item(AssignmentType.F_DESCRIPTION).eq("Assignment 2")
                .buildFilter();
        verify("assignment matches (description = 'Assignment 2')", filter);
    }

    @Test
    public void testMatchSubstringAgainstEmptyItem() throws Exception {
        // jack has no locality
        ObjectFilter filter = getPrismContext().queryFor(UserType.class)
                .item(UserType.F_LOCALITY).startsWith("C")
                .buildFilter();
        verify("locality startsWith 'C'", filter);
    }

    @Test
    public void testMatchOrFilter() throws Exception {
        ObjectFilter filter = getPrismContext().queryFor(UserType.class)
                .item(UserType.F_GIVEN_NAME).eq("Jack")
                .or().item(UserType.F_GIVEN_NAME).eq("Jackie")
                .buildFilter();

        verify("givenName = 'Jack' or givenName = 'Jackie'", filter);
        verify("givenName ='Jack' or givenName= 'Jackie'", filter, false);
    }

    @Test
    public void testDontMatchEqualFilter() throws Exception {
        ObjectFilter filter = getPrismContext().queryFor(UserType.class)
                .item(UserType.F_GIVEN_NAME).eq("Jackie")
                .buildFilter();
        verify("givenName = 'Jackie'", filter);
        verify("givenName= 'Jackie'", filter, false);
        verify("givenName ='Jackie'", filter, false);
    }

    @Test
    public void testMatchEqualMultivalue() throws Exception {
        MutablePrismPropertyDefinition<?> def = getPrismContext().definitionFactory().createPropertyDefinition(new QName("indexedString"), DOMUtil.XSD_STRING);
        ObjectFilter filter = getPrismContext().queryFor(UserType.class)
                .item(ItemPath.create(UserType.F_EXTENSION, "indexedString"), def).eq("alpha")
                .buildFilter();
        verify("extension/indexedString = 'alpha'", filter);
    }

    @Test
    public void testMatchEqualNonEmptyAgainstEmptyItem() throws Exception {
        // jack has no locality
        ObjectFilter filter = getPrismContext().queryFor(UserType.class)
                .item(UserType.F_LOCALITY).eq("some")
                .buildFilter();
        verify("locality = 'some'", filter);
    }

    @Test
    public void testMatchEqualEmptyAgainstEmptyItem() throws Exception {
        // jack has no locality
        ObjectFilter filter = getPrismContext().queryFor(UserType.class)
                .item(UserType.F_LOCALITY).isNull()
                .buildFilter();
        verify("locality not exists", filter);
    }

    @Test
    public void testMatchEqualEmptyAgainstNonEmptyItem() throws Exception {
        ObjectFilter filter = getPrismContext().queryFor(UserType.class)
                .item(UserType.F_NAME).isNull()
                .buildFilter();
        verify("name not exists", filter);
    }

    @Test
    public void testComplexMatch() throws Exception {
        ObjectFilter filter =
                getPrismContext().queryFor(UserType.class)
                        .item(UserType.F_FAMILY_NAME).eq("Sparrow")
                        .and().item(UserType.F_FULL_NAME).contains("arr")
                        .and()
                        .block()
                        .item(UserType.F_GIVEN_NAME).eq("Jack")
                        .or().item(UserType.F_GIVEN_NAME).eq("Jackie")
                        .endBlock()
                        .buildFilter();
        verify("familyName = 'Sparrow' and fullName contains 'arr' "
                + "and (givenName = 'Jack' or givenName = 'Jackie')", filter);
        verify("familyName = 'Sparrow' AND fullName contains 'arr' "
                + "and (givenName = 'Jack' OR givenName = 'Jackie')", filter, false);
    }

    @Test(enabled = false)
    public void testTypeAndMatch() throws Exception {
        ObjectFilter filter =
                getPrismContext().queryFor(UserType.class)
                        .type(UserType.class)
                        .item(UserType.F_FAMILY_NAME).eq("Sparrow")
                        .and().item(UserType.F_FULL_NAME).contains("arr")
                        .and()
                        .block()
                        .item(UserType.F_GIVEN_NAME).eq("Jack")
                        .or().item(UserType.F_GIVEN_NAME).eq("Jackie")
                        .endBlock()
                        .buildFilter();
        verify(". type UserType and familyName = 'Sparrow' and fullName contains 'arr' "
                + "and (givenName = 'Jack' or givenName = 'Jackie')", filter);
    }

    @Test
    public void testPolystringMatchEqualFilter() throws Exception {
        PolyString name = new PolyString("jack", "jack");
        ObjectFilter filter = getPrismContext().queryFor(UserType.class)
                .item(UserType.F_NAME).eq(name).matchingStrict()
                .buildFilter();
        verify("name matches (orig = 'jack' and norm = 'jack')", filter);
        verify("name matches (norm = 'jack')",
                getPrismContext().queryFor(UserType.class)
                        .item(UserType.F_NAME).eq(name).matchingNorm()
                        .buildFilter());
        verify("name matches (orig = 'jack')",
                getPrismContext().queryFor(UserType.class)
                        .item(UserType.F_NAME).eq(name).matchingOrig()
                        .buildFilter());

    }

    @Test   // MID-4173
    public void testExistsNegative() throws Exception {
        PrismObject<UserType> user = parseUserJacky();
        ObjectFilter filter = getPrismContext().queryFor(UserType.class)
                .exists(UserType.F_ASSIGNMENT)
                .item(AssignmentType.F_DESCRIPTION).eq("Assignment NONE")
                .buildFilter();
        verify("assignment matches (description = 'Assignment NONE')", filter, user);
    }

    @Test   // MID-4173
    public void testExistsAnyNegative() throws Exception {
        PrismObject<UserType> user = parseUserJacky();
        user.removeContainer(UserType.F_ASSIGNMENT);
        ObjectFilter filter = getPrismContext().queryFor(UserType.class)
                .exists(UserType.F_ASSIGNMENT)
                .buildFilter();

        verify("assignment exists", filter, user);
    }

    @Test   // MID-4173
    public void testExistsAnyPositive() throws Exception {
        ObjectFilter filter = getPrismContext().queryFor(UserType.class)
                .exists(UserType.F_ASSIGNMENT)
                .buildFilter();
        verify("assignment exists", filter);

    }

    @Test   // MID-4217
    public void testMultiRootPositive() throws Exception {
        ObjectFilter filter = getPrismContext().queryFor(UserType.class)
                .item(UserType.F_ASSIGNMENT, AssignmentType.F_DESCRIPTION).eq("Assignment 2")
                .buildFilter();
        verify("assignment/description = 'Assignment 2'", filter);
    }

    @Test   // MID-4217
    public void testMultiRootNegative() throws Exception {
        ObjectFilter filter = getPrismContext().queryFor(UserType.class)
                .item(UserType.F_ASSIGNMENT, AssignmentType.F_DESCRIPTION).eq("Assignment XXXXX")
                .buildFilter();

        verify("assignment/description = 'Assignment XXXXX'", filter);
    }

    @Test   // MID-4217
    public void testRefPositive() throws Exception {
        ObjectFilter filter = getPrismContext().queryFor(UserType.class)
                .item(UserType.F_ACCOUNT_REF).ref("c0c010c0-d34d-b33f-f00d-aaaaaaaa1113")
                .buildFilter();
        verify("accountRef matches (oid = 'c0c010c0-d34d-b33f-f00d-aaaaaaaa1113')", filter);
        verify("accountRef matches (oid ='c0c010c0-d34d-b33f-f00d-aaaaaaaa1113')", filter, false);
        verify("accountRef matches (oid='c0c010c0-d34d-b33f-f00d-aaaaaaaa1113')", filter, false);
        verify("accountRef matches ( oid= 'c0c010c0-d34d-b33f-f00d-aaaaaaaa1113')", filter, false);
        verify("  accountRef matches ( oid = 'c0c010c0-d34d-b33f-f00d-aaaaaaaa1113') ", filter, false);
        verify("accountRef matches ( oid ='c0c010c0-d34d-b33f-f00d-aaaaaaaa1113')", filter, false);
        verify("accountRef matches ( oid =\"c0c010c0-d34d-b33f-f00d-aaaaaaaa1113\")", filter, false);
    }

    @Test
    public void testOidIn() throws Exception {
        ObjectFilter filter = getPrismContext().queryFor(UserType.class)
                .id("c0c010c0-d34d-b33f-f00d-aaaaaaaa1113", "c0c010c0-d34d-b33f-f00d-aaaaaaaa1114", "c0c010c0-d34d-b33f-f00d-aaaaaaaa1115")
                .buildFilter();

        verify(". inOid ('c0c010c0-d34d-b33f-f00d-aaaaaaaa1113', 'c0c010c0-d34d-b33f-f00d-aaaaaaaa1114', 'c0c010c0-d34d-b33f-f00d-aaaaaaaa1115')", filter);

    }

    @Test
    public void testRefBy() throws Exception {
        XMLGregorianCalendar earlier = XmlTypeConverter.createXMLGregorianCalendar("2020-07-06T00:00:00.000+02:00");
        var validToPath = ItemPath.create(UserType.F_ACTIVATION, ActivationType.F_VALID_TO);
        var validToDef = getPrismContext().getSchemaRegistry().findComplexTypeDefinitionByType(UserType.COMPLEX_TYPE)
                .findPropertyDefinition(validToPath);

        @NotNull
        var innerFilter = LessFilterImpl.createLess(validToPath, validToDef, null, earlier, false, getPrismContext());
        ObjectFilter filter = ReferencedByFilterImpl.create(UserType.COMPLEX_TYPE, UserType.F_ACCOUNT_REF, innerFilter, A_RELATION);

        ObjectFilter javaFilter = getPrismContext().queryFor(AccountType.class)
                .referencedBy(UserType.class, UserType.F_ACCOUNT_REF, A_RELATION)
                .item(validToPath).lt(earlier)
                .buildFilter();

        assertEquals(filter, javaFilter);

        verify(AccountType.class, ". referencedBy (@type = UserType"
                + " and @path = accountRef"
                + " and @relation = a-relation"
                + " and activation/validTo < '2020-07-06T00:00:00.000+02:00')", filter);
    }

    @Test
    public void testFuzzyLevenshtein() throws Exception {
        var filter = getPrismContext().queryFor(UserType.class)
                .item(UserType.F_FAMILY_NAME)
                .fuzzyString("smith").levenshteinInclusive(2)
                .buildFilter();
        verify(UserType.class, "familyName levenshtein ('smith', 2, true)", filter);
    }

    @Test
    public void testFuzzySimilarity() throws Exception {
        var filter = getPrismContext().queryFor(UserType.class)
                .item(UserType.F_FAMILY_NAME)
                .fuzzyString("smith").similarityInclusive(0.8f)
                .buildFilter();
        verify(UserType.class, "familyName similarity ('smith', 0.8, true)", filter);
    }

    @Test
    public void testRefByMultipleConditions() throws Exception {
        XMLGregorianCalendar earlier = XmlTypeConverter.createXMLGregorianCalendar("2020-07-06T00:00:00.000+02:00");
        var validFromPath = ItemPath.create(UserType.F_ACTIVATION, ActivationType.F_VALID_FROM);
        var validToPath = ItemPath.create(UserType.F_ACTIVATION, ActivationType.F_VALID_TO);
        var validToDef = getPrismContext().getSchemaRegistry().findComplexTypeDefinitionByType(UserType.COMPLEX_TYPE)
                .findPropertyDefinition(validToPath);
        var validFromDef = getPrismContext().getSchemaRegistry().findComplexTypeDefinitionByType(UserType.COMPLEX_TYPE)
                .findPropertyDefinition(validFromPath);

        @NotNull
        var innerFilter = AndFilterImpl.createAnd(
                LessFilterImpl.createLess(validToPath, validToDef, null, earlier, false, getPrismContext()),
                GreaterFilterImpl.createGreater(validFromPath, validFromDef, null, earlier, false, getPrismContext())
        );
        ObjectFilter filter = ReferencedByFilterImpl.create(UserType.COMPLEX_TYPE, UserType.F_ACCOUNT_REF, innerFilter, null);

        ObjectFilter javaFilter = getPrismContext().queryFor(AccountType.class)
                .referencedBy(UserType.class, UserType.F_ACCOUNT_REF)
                .block()
                .item(validToPath).lt(earlier)
                .and().item(validFromPath).gt(earlier)
                .endBlock()
                .buildFilter();

        assertEquals(filter, javaFilter);

        verify(AccountType.class, ". referencedBy (@type = UserType and @path = accountRef"
                        + " and activation/validTo < '2020-07-06T00:00:00.000+02:00'"
                        + " and activation/validFrom > '2020-07-06T00:00:00.000+02:00')"
                , filter);
    }

    @Test
    public void testRefByOrConditions() throws Exception {
        XMLGregorianCalendar earlier = XmlTypeConverter.createXMLGregorianCalendar("2020-07-06T00:00:00.000+02:00");
        var validFromPath = ItemPath.create(UserType.F_ACTIVATION, ActivationType.F_VALID_FROM);
        var validToPath = ItemPath.create(UserType.F_ACTIVATION, ActivationType.F_VALID_TO);
        var validToDef = getPrismContext().getSchemaRegistry().findComplexTypeDefinitionByType(UserType.COMPLEX_TYPE)
                .findPropertyDefinition(validToPath);
        var validFromDef = getPrismContext().getSchemaRegistry().findComplexTypeDefinitionByType(UserType.COMPLEX_TYPE)
                .findPropertyDefinition(validFromPath);

        @NotNull
        var innerFilter = OrFilterImpl.createOr(
                LessFilterImpl.createLess(validToPath, validToDef, null, earlier, false, getPrismContext()),
                GreaterFilterImpl.createGreater(validFromPath, validFromDef, null, earlier, false, getPrismContext())
        );
        ObjectFilter filter = ReferencedByFilterImpl.create(UserType.COMPLEX_TYPE, UserType.F_ACCOUNT_REF, innerFilter, null);

        ObjectFilter javaFilter = getPrismContext().queryFor(AccountType.class)
                .referencedBy(UserType.class, UserType.F_ACCOUNT_REF)
                .block()
                .item(validToPath).lt(earlier)
                .or().item(validFromPath).gt(earlier)
                .endBlock()
                .buildFilter();

        assertEquals(filter, javaFilter);

        verify(AccountType.class, ". referencedBy (@type = UserType and @path = accountRef"
                        + " and (activation/validTo < '2020-07-06T00:00:00.000+02:00'"
                        + " or activation/validFrom > '2020-07-06T00:00:00.000+02:00'))"
                , filter);

        try {
            verifyPlaceholders(AccountType.class, filter, ". referencedBy (@type = UserType and @path = accountRef"
                            + " and (activation/validTo < ? or activation/validFrom > ?))",
                    "2020-07-06T00:00:00.000+02:00", "2020-07-06T00:00:00.000+02:00"
            );
        } catch (SchemaException e) {
            assertTrue(e.getMessage().contains(XMLGregorianCalendar.class.getSimpleName()), "error message must mention incorrect type");
        }
        XMLGregorianCalendar date = XmlTypeConverter.createXMLGregorianCalendar("2020-07-06T00:00:00.000+02:00");
        verifyPlaceholders(AccountType.class, filter, ". referencedBy (@type = UserType and @path = accountRef"
                        + " and (activation/validTo < ? or activation/validFrom > ?))",date, date);
    }

    private void verifyPlaceholders(Class<? extends Containerable> typeClass, ObjectFilter filter, String query, Object... args) throws SchemaException {
        var prepared = getPrismContext().createQueryParser().parse(typeClass, query);
        assertTrue(!prepared.allPlaceholdersBound(), "None of placeholders should be bound.");
        for (var arg: args) {
            prepared.bindValue(arg);
        }
        var queryFilter = prepared.toFilter();
        assertFilterEquals(queryFilter, filter);

    }

    @Test   // MID-4217
    public void testRefNegative() throws Exception {
        ObjectFilter filter = getPrismContext().queryFor(UserType.class)
                .item(UserType.F_ACCOUNT_REF).ref("xxxxxxxxxxxxxx")
                .buildFilter();
        verify("accountRef matches (oid = 'xxxxxxxxxxxxxx')", filter);
    }

    @Test
    public void testRefWithNested() throws Exception {
        RefFilterImpl filter = (RefFilterImpl) getPrismContext().queryFor(UserType.class)
                .item(UserType.F_ACCOUNT_REF).refRelation(new QName("a-relation"))
                .buildFilter();
        filter.setFilter(getPrismContext().queryFor(AccountType.class).exists(AccountType.F_ATTRIBUTES).buildFilter());

        verify(UserType.class, "accountRef matches (relation = a-relation and @ matches (attributes exists))", filter);
    }

    @Test
    public void testRefRelationNegative() throws Exception {
        ObjectFilter filter = getPrismContext().queryFor(UserType.class)
                .item(UserType.F_ACCOUNT_REF).refRelation(new QName("a-relation"))
                .buildFilter();
        verify("accountRef matches (relation = a-relation)", filter);
        verify("accountRef matches (relation=a-relation)", filter, false);
    }

    @Test // MID-6487
    public void testGtFilter() throws Exception {
        ObjectFilter filter =
                getPrismContext().queryFor(UserType.class)
                        .item(UserType.F_NAME).gt(new PolyString("j")).matchingOrig()
                        .buildFilter();
        verify("name >[polyStringOrig] 'j'", filter);
        verify("name>[polyStringOrig] 'j'", filter, false);
    }

    @Test // MID-6487
    public void testLtFilter() throws Exception {
        ObjectFilter filter =
                getPrismContext().queryFor(UserType.class)
                        .item(UserType.F_NAME).lt(new PolyString("j")).matchingNorm()
                        .buildFilter();
        verify("name <[polyStringNorm] 'j'", filter);
    }

    @Test // MID-6487
    public void testNumericFilters() throws Exception {
        assertNumGeFilter(42);
        assertNumGeFilter(44);
        assertNumGeFilter(40);

        assertNumLtFilter(42);
        assertNumLtFilter(44);
        assertNumLtFilter(40);
    }

    @Test // MID-6577
    public void testDateTimeFilters() throws Exception {
        XMLGregorianCalendar equal = XmlTypeConverter.createXMLGregorianCalendar("2020-07-07T00:00:00.000+02:00");
        XMLGregorianCalendar earlier = XmlTypeConverter.createXMLGregorianCalendar("2020-07-06T00:00:00.000+02:00");
        XMLGregorianCalendar later = XmlTypeConverter.createXMLGregorianCalendar("2020-07-08T00:00:00.000+02:00");

        assertDateTimeGeFilter(equal);
        assertDateTimeGeFilter(later);
        assertDateTimeGeFilter(earlier);

        assertDateTimeLeFilter(equal);
        assertDateTimeLeFilter(later);
        assertDateTimeLeFilter(earlier);

        assertDateTimeGtFilter(equal);
        assertDateTimeGtFilter(later);
        assertDateTimeGtFilter(earlier);

        assertDateTimeLtFilter(equal);
        assertDateTimeLtFilter(later);
        assertDateTimeLtFilter(earlier);
    }

    @Test(enabled = false) // MID-6601
    public void testOidGtFilter() throws Exception {
        ObjectFilter filter =
                getPrismContext().queryFor(UserType.class)
                        .item(PrismConstants.T_ID).gt("00")
                        .buildFilter();
        /* TODO fix and re-enable the test
        Expected :GREATER: id, PPV(String:00)
        Actual   :GREATER: #, PPV(String:00)
         */
        verify("# > '00'", filter);
    }

    @Test // MID-6601
    public void testOidSubstringFilter() throws Exception {
        PrismObject<UserType> user = parseUserJacky();
        ObjectFilter filter =
                getPrismContext().queryFor(UserType.class)
                        .item(PrismConstants.T_ID).startsWith("c0c0")
                        .buildFilter();
        boolean match = ObjectQuery.match(user, filter, MATCHING_RULE_REGISTRY);
        AssertJUnit.assertTrue("filter does not match object", match);
    }

    @Test
    public void testRefSearchWithOwnedByOnly() throws Exception {
        ObjectFilter objectFilter = queryParser().parseFilter(Referencable.class,
                ". ownedBy (@type = UserType and @path = accountRef)");
        Assertions.assertThat(objectFilter).hasToString(
                "OWNED-BY(CTD ({.../test/foo-1.xsd}UserType),accountRef,null)");
    }

    @Test
    public void testRefSearchWithOwnedByWithAdditionalOwnerCondition() throws Exception {
        ObjectFilter objectFilter = queryParser().parseFilter(Referencable.class,
                ". ownedBy (@type = UserType and @path = accountRef and name = 'xy')");
        Assertions.assertThat(objectFilter).hasToString(
                "OWNED-BY(CTD ({.../test/foo-1.xsd}UserType),accountRef,EQUAL: name, PPV(PolyString:xy))");
    }

    @Test
    public void testRefSearchWithOwnedByAndRefFilter() throws Exception {
        ObjectFilter objectFilter = queryParser().parseFilter(Referencable.class,
                ". ownedBy (@type = UserType and @path = accountRef)"
                        + " and . matches (oid = 'c0c010c0-d34d-b33f-f00d-aaaaaaaa1113')");
        Assertions.assertThat(objectFilter).hasToString("AND("
                + "REF: , PRV(oid=c0c010c0-d34d-b33f-f00d-aaaaaaaa1113, targetType=null), targetFilter=null;"
                + " OWNED-BY(CTD ({.../test/foo-1.xsd}UserType),accountRef,null))");
    }

    private void assertNumGeFilter(Object value) throws SchemaException, IOException {
        assertGeFilter(EXTENSION_NUM_ELEMENT, DOMUtil.XSD_INT, value);
    }

    private void assertNumLtFilter(Object value) throws SchemaException, IOException {
        assertLtFilter(EXTENSION_NUM_ELEMENT, DOMUtil.XSD_INT, value);
    }

    private void assertDateTimeGeFilter(Object value) throws SchemaException, IOException {
        assertGeFilter(EXTENSION_DATETIME_ELEMENT, DOMUtil.XSD_DATETIME, value);
    }

    private void assertDateTimeLeFilter(Object value) throws SchemaException, IOException {
        assertLeFilter(EXTENSION_DATETIME_ELEMENT, DOMUtil.XSD_DATETIME, value);
    }

    private void assertDateTimeGtFilter(Object value) throws SchemaException, IOException {
        assertGtFilter(EXTENSION_DATETIME_ELEMENT, DOMUtil.XSD_DATETIME, value);
    }

    private void assertDateTimeLtFilter(Object value) throws SchemaException, IOException {
        assertLtFilter(EXTENSION_DATETIME_ELEMENT, DOMUtil.XSD_DATETIME, value);
    }

    private String toText(Object value) {
        if (value instanceof XMLGregorianCalendar) {
            return "'" + value + "'";
        }
        return value.toString();
    }

    private void assertGeFilter(ItemName itemName, QName itemType, Object value) throws SchemaException, IOException {
        ObjectFilter filter = createExtensionFilter(itemName, itemType,
                (path, definition) -> getPrismContext().queryFor(UserType.class)
                        .item(path, definition).ge(value)
                        .buildFilter());
        verify("extension/" + itemName.getLocalPart() + " >= " + toText(value), filter);
    }

    private void assertLeFilter(ItemName itemName, QName itemType, Object value) throws SchemaException, IOException {
        ObjectFilter filter = createExtensionFilter(itemName, itemType,
                (path, definition) -> getPrismContext().queryFor(UserType.class)
                        .item(path, definition).le(value)
                        .buildFilter());
        verify("extension/" + itemName.getLocalPart() + " <= " + toText(value), filter);
    }

    private void assertGtFilter(ItemName itemName, QName itemType, Object value) throws SchemaException, IOException {
        ObjectFilter filter = createExtensionFilter(itemName, itemType,
                (path, definition) -> getPrismContext().queryFor(UserType.class)
                        .item(path, definition).gt(value)
                        .buildFilter());
        verify("extension/" + itemName.getLocalPart() + " > " + toText(value), filter);
    }

    private void assertLtFilter(ItemName itemName, QName itemType, Object value) throws SchemaException, IOException {
        ObjectFilter filter = createExtensionFilter(itemName, itemType,
                (path, definition) -> getPrismContext().queryFor(UserType.class)
                        .item(path, definition).lt(value)
                        .buildFilter());
        verify("extension/" + itemName.getLocalPart() + " < " + toText(value), filter);
    }

    private ObjectFilter createExtensionFilter(
            ItemName itemName, QName itemType, BiFunction<ItemPath, PrismPropertyDefinition<Integer>, ObjectFilter> filterSupplier) {
        ItemPath path = ItemPath.create(UserType.F_EXTENSION, itemName);
        PrismPropertyDefinition<Integer> definition = getPrismContext().definitionFactory()
                .createPropertyDefinition(itemName, itemType);
        ObjectFilter filter = filterSupplier.apply(path, definition);
        return filter;
    }

}
