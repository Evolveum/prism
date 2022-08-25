/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.match;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.*;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.evolveum.midpoint.prism.foo.UserType;
import com.evolveum.midpoint.prism.impl.match.MatchingRuleRegistryFactory;
import com.evolveum.midpoint.prism.query.ObjectFilter;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.exception.SchemaException;

public class FuzzyStringMatchFilterImplTest extends AbstractPrismTest {

    QName QNAME_GIVEN_NAME = QName.valueOf("givenName");
    QName QNAME_DATE_BIRTH = QName.valueOf("dateOfBirth");
    QName QNAME_NATIONAL_ID = QName.valueOf("nationalId");
    QName QNAME_FAMILY_NAME = QName.valueOf("familyName");
    QName QNAME_FAMILY_NAME_3 = QName.valueOf("familyName.3");

    PrismPropertyDefinition<?> defNationalId;
    PrismPropertyDefinition<?> defDateOfBirth;
    PrismPropertyDefinition<?> defSubFamilyName;

    @BeforeTest
    public void initialize() {
        defNationalId = PrismContext.get().definitionFactory()
                .createPropertyDefinition(QNAME_NATIONAL_ID, DOMUtil.XSD_STRING, null, null);
        defDateOfBirth = PrismContext.get().definitionFactory()
                .createPropertyDefinition(QNAME_DATE_BIRTH, DOMUtil.XSD_STRING, null, null);
        defSubFamilyName = PrismContext.get().definitionFactory()
                .createPropertyDefinition(QNAME_FAMILY_NAME_3, DOMUtil.XSD_STRING, null, null);
    }

    @Test
    public void fuzzyStringMatchFilterMarryTest() throws SchemaException {
        PrismContext prismContext = PrismContext.get();
        MatchingRuleRegistry matchingRuleRegistry = MatchingRuleRegistryFactory.createRegistry();

        given("prismContainerValue for match operation");
        PrismContainerValue<?> prismContainerValue = generatePrismContainerValue("1", "Marry", "Jane",
                "2006-04-10", "060410/1993", "Mar");

        String comparedNationalId = "060410/1992";
        String comparedGivenName = "Mari";
        String comparedFamilyName = "Jane";
        String comparedSubFamilyNam = "Mar";

        var filterNationalIdExclusive01 = prismContext.queryFor(UserType.class)
                .itemWithDef(defNationalId, QNAME_NATIONAL_ID).fuzzyString(comparedNationalId).levenshteinExclusive(1)
                .buildFilter();
        Assert.assertFalse(filterNationalIdExclusive01.match(prismContainerValue, matchingRuleRegistry));

        var filterNationalIdInclusive01 = prismContext.queryFor(UserType.class)
                .itemWithDef(defNationalId, QNAME_NATIONAL_ID).fuzzyString(comparedNationalId).levenshteinInclusive(1)
                .buildFilter();
        Assert.assertTrue(filterNationalIdInclusive01.match(prismContainerValue, matchingRuleRegistry));

        var filterNationalIdExclusive02 = prismContext.queryFor(UserType.class)
                .itemWithDef(defNationalId, QNAME_NATIONAL_ID).fuzzyString(comparedNationalId).levenshtein(1, false)
                .buildFilter();
        Assert.assertFalse(filterNationalIdExclusive02.match(prismContainerValue, matchingRuleRegistry));

        var filterNationalIdInclusive02 = prismContext.queryFor(UserType.class)
                .itemWithDef(defNationalId, QNAME_NATIONAL_ID).fuzzyString(comparedNationalId).levenshtein(1, true)
                .buildFilter();
        Assert.assertTrue(filterNationalIdInclusive02.match(prismContainerValue, matchingRuleRegistry));

        var filterGivenNameExclusive01 = prismContext.queryFor(UserType.class)
                .item(UserType.F_GIVEN_NAME).fuzzyString(comparedGivenName).similarityExclusive((float) 0.375)
                .buildFilter();
        Assert.assertFalse(filterGivenNameExclusive01.match(prismContainerValue, matchingRuleRegistry));

        var filterGivenNameInclusive01 = prismContext.queryFor(UserType.class)
                .item(UserType.F_GIVEN_NAME).fuzzyString(comparedGivenName).similarityInclusive((float) 0.375)
                .buildFilter();
        Assert.assertTrue(filterGivenNameInclusive01.match(prismContainerValue, matchingRuleRegistry));

        var filterGivenNameExclusive02 = prismContext.queryFor(UserType.class)
                .item(UserType.F_GIVEN_NAME).fuzzyString(comparedGivenName).similarity((float) 0.375, false)
                .buildFilter();
        Assert.assertFalse(filterGivenNameExclusive02.match(prismContainerValue, matchingRuleRegistry));

        var filterGivenNameInclusive02 = prismContext.queryFor(UserType.class)
                .item(UserType.F_GIVEN_NAME).fuzzyString(comparedGivenName).similarity((float) 0.375, true)
                .buildFilter();
        Assert.assertTrue(filterGivenNameInclusive02.match(prismContainerValue, matchingRuleRegistry));

        var filterFamilyName = prismContext.queryFor(UserType.class)
                .item(UserType.F_FAMILY_NAME).fuzzyString(comparedFamilyName).similarityInclusive((float) 1)
                .buildFilter();
        Assert.assertTrue(filterFamilyName.match(prismContainerValue, matchingRuleRegistry));

        String comparedDateOfBirth = "2006-04-10";
        var filterDateOfBirth = prismContext.queryFor(UserType.class)
                .itemWithDef(defDateOfBirth, QNAME_DATE_BIRTH).fuzzyString(comparedDateOfBirth).similarityInclusive((float) 1)
                .buildFilter();
        Assert.assertTrue(filterDateOfBirth.match(prismContainerValue, matchingRuleRegistry));

        var filterSubFamilyName = prismContext.queryFor(UserType.class)
                .itemWithDef(defSubFamilyName, QNAME_FAMILY_NAME_3).eq(comparedSubFamilyNam)
                .buildFilter();
        Assert.assertTrue(filterSubFamilyName.match(prismContainerValue, matchingRuleRegistry));

        ObjectFilter queryFilter01 = prismContext.queryFor(UserType.class)
                .filter(filterNationalIdExclusive01).or()
                .filter(filterGivenNameExclusive01).and()
                .filter(filterFamilyName).or()
                .filter(filterDateOfBirth).and()
                .filter(filterSubFamilyName)
                .buildFilter();

        when("query filter queryFilter01 created and all simple filters are successfully matched");
        display("ObjectFilter01:  \n  " + queryFilter01);
        then("execute queryFilter01 match");
        Assert.assertTrue(queryFilter01.match(prismContainerValue, matchingRuleRegistry));

        ObjectFilter queryFilter02 = prismContext.queryFor(UserType.class)
                .filter(filterNationalIdExclusive01).and()
                .filter(filterGivenNameExclusive01).and()
                .filter(filterFamilyName).or()
                .filter(filterDateOfBirth).and()
                .filter(filterNationalIdExclusive01).or()
                .filter(filterFamilyName).and()
                .filter(filterDateOfBirth)
                .buildFilter();

        when("query filter queryFilter02 created and all simple filters are successfully matched + queryFilter01");
        display("ObjectFilter02:  \n  " + queryFilter02);
        then("execute queryFilter02 match");
        Assert.assertTrue(queryFilter02.match(prismContainerValue, matchingRuleRegistry));
    }

    @Test
    public void fuzzyStringMatchFilterJohnTest() throws SchemaException {
        PrismContext prismContext = PrismContext.get();
        MatchingRuleRegistry matchingRuleRegistry = MatchingRuleRegistryFactory.createRegistry();

        given("prismContainerValue for match operation");
        PrismContainerValue<?> prismContainerValue = generatePrismContainerValue("2", "John", "Smith",
                "2004-02-06", "040206/1328", "Joh");

        String comparedNationalId = "040206-1328";
        String comparedGivenName = "Johns";
        String comparedFamilyName = "Smith";
        String comparedDateOfBirth = "2004-02-06";
        String comparedSubFamilyNam = "Ian";

        var filterNationalIdInclusive01 = prismContext.queryFor(UserType.class)
                .itemWithDef(defNationalId, QNAME_NATIONAL_ID).fuzzyString(comparedNationalId).levenshtein(1, true)
                .buildFilter();
        Assert.assertTrue(filterNationalIdInclusive01.match(prismContainerValue, matchingRuleRegistry));

        var filterNationalIdExclusive01 = prismContext.queryFor(UserType.class)
                .itemWithDef(defNationalId, QNAME_NATIONAL_ID).fuzzyString(comparedNationalId).levenshtein(1, false)
                .buildFilter();
        Assert.assertFalse(filterNationalIdExclusive01.match(prismContainerValue, matchingRuleRegistry));

        var filterNationalIdInclusive02 = prismContext.queryFor(UserType.class)
                .itemWithDef(defNationalId, QNAME_NATIONAL_ID).fuzzyString(comparedNationalId).levenshteinInclusive(1)
                .buildFilter();
        Assert.assertTrue(filterNationalIdInclusive02.match(prismContainerValue, matchingRuleRegistry));

        var filterNationalIdExclusive02 = prismContext.queryFor(UserType.class)
                .itemWithDef(defNationalId, QNAME_NATIONAL_ID).fuzzyString(comparedNationalId).levenshteinExclusive(1)
                .buildFilter();
        Assert.assertFalse(filterNationalIdExclusive02.match(prismContainerValue, matchingRuleRegistry));

        var filterGivenNameInclusive01 = prismContext.queryFor(UserType.class)
                .item(UserType.F_GIVEN_NAME).fuzzyString(comparedGivenName).similarity(0.5714286f, true)
                .buildFilter();
        Assert.assertTrue(filterGivenNameInclusive01.match(prismContainerValue, matchingRuleRegistry));

        var filterGivenNameExclusive01 = prismContext.queryFor(UserType.class)
                .item(UserType.F_GIVEN_NAME).fuzzyString(comparedGivenName).similarity(0.5714286f, false)
                .buildFilter();
        Assert.assertFalse(filterGivenNameExclusive01.match(prismContainerValue, matchingRuleRegistry));

        var filterGivenNameInclusive02 = prismContext.queryFor(UserType.class)
                .item(UserType.F_GIVEN_NAME).fuzzyString(comparedGivenName).similarityInclusive(0.5714286f)
                .buildFilter();
        Assert.assertTrue(filterGivenNameInclusive02.match(prismContainerValue, matchingRuleRegistry));

        var filterGivenNameExclusive02 = prismContext.queryFor(UserType.class)
                .item(UserType.F_GIVEN_NAME).fuzzyString(comparedGivenName).similarityExclusive(0.5714286f)
                .buildFilter();
        Assert.assertFalse(filterGivenNameExclusive02.match(prismContainerValue, matchingRuleRegistry));

        var filterFamilyName = prismContext.queryFor(UserType.class)
                .item(UserType.F_FAMILY_NAME).fuzzyString(comparedFamilyName).similarityInclusive((float) 1)
                .buildFilter();
        Assert.assertTrue(filterFamilyName.match(prismContainerValue, matchingRuleRegistry));

        var filterDateOfBirth = prismContext.queryFor(UserType.class)
                .itemWithDef(defDateOfBirth, QNAME_DATE_BIRTH).fuzzyString(comparedDateOfBirth).similarityInclusive((float) 1)
                .buildFilter();
        Assert.assertTrue(filterDateOfBirth.match(prismContainerValue, matchingRuleRegistry));

        var filterSubFamilyName = prismContext.queryFor(UserType.class)
                .itemWithDef(defSubFamilyName, QNAME_FAMILY_NAME_3).eq(comparedSubFamilyNam)
                .buildFilter();
        Assert.assertFalse(filterSubFamilyName.match(prismContainerValue, matchingRuleRegistry));

        ObjectFilter queryFilter01 = prismContext.queryFor(UserType.class)
                .filter(filterNationalIdExclusive01).or()
                .filter(filterGivenNameInclusive01).and()
                .filter(filterFamilyName).and()
                .filter(filterDateOfBirth).or()
                .filter(filterSubFamilyName)
                .buildFilter();

        when("query filter queryFilter01 created and simple filters are successfully matched");
        display("ObjectFilter01:  \n  " + queryFilter01);
        then("execute queryFilter01 match");
        Assert.assertTrue(queryFilter01.match(prismContainerValue, matchingRuleRegistry));

        ObjectFilter queryFilter02 = prismContext.queryFor(UserType.class)
                .filter(filterNationalIdExclusive01).and()
                .filter(filterGivenNameInclusive01).or()
                .filter(filterFamilyName).and()
                .filter(filterDateOfBirth).and()
                .filter(filterSubFamilyName)
                .buildFilter();

        when("query filter queryFilter02 created and all simple filters are successfully matched + queryFilter01");
        display("ObjectFilter02:  \n  " + queryFilter02);
        then("execute queryFilter02 match");
        Assert.assertFalse(queryFilter02.match(prismContainerValue, matchingRuleRegistry));
    }

    private PrismContainerValue<?> generatePrismContainerValue(
            String id, String givenname, String familyName, String dateOfBirth, String nationalId, String subFamilyName)
            throws SchemaException {
        PrismContainerValue<?> prismContainerValue = PrismContext.get().itemFactory().createContainerValue();
        prismContainerValue.setId(Long.valueOf(id));

        prismContainerValue.setPropertyRealValue(QNAME_GIVEN_NAME, givenname, PrismContext.get());
        prismContainerValue.setPropertyRealValue(QNAME_FAMILY_NAME, familyName, PrismContext.get());
        prismContainerValue.setPropertyRealValue(QNAME_DATE_BIRTH, dateOfBirth, PrismContext.get());
        prismContainerValue.setPropertyRealValue(QNAME_NATIONAL_ID, nationalId, PrismContext.get());
        prismContainerValue.setPropertyRealValue(QNAME_FAMILY_NAME_3, subFamilyName, PrismContext.get());

        display("PrismContainerValue: \n  " + prismContainerValue.debugDump());
        return prismContainerValue;
    }
}
