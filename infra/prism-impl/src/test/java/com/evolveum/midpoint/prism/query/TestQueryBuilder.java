/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.query;

import static com.evolveum.midpoint.prism.PrismInternalTestUtil.*;
import static com.evolveum.midpoint.prism.util.PrismTestUtil.getSchemaRegistry;

import java.util.List;
import javax.xml.namespace.QName;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.foo.AccountType;
import com.evolveum.midpoint.prism.foo.ActivationType;
import com.evolveum.midpoint.prism.foo.AssignmentType;
import com.evolveum.midpoint.prism.foo.UserType;
import com.evolveum.midpoint.prism.impl.query.*;
import com.evolveum.midpoint.prism.path.ItemPath;

/**
 * Here are the most simple tests for Query Builder.
 * More advanced tests are part of QueryInterpreterTest in repo-sql-impl-test.
 * <p>
 * These tests are strict in the sense they check the exact structure of created queries.
 * Repo tests check just the HQL outcome.
 */
public class TestQueryBuilder extends AbstractPrismTest {

    public static final QName ASSIGNMENT_TYPE_QNAME = new QName(NS_FOO, "AssignmentType");

    @Test
    public void test100EmptyFilter() {
        ObjectQuery actual = getPrismContext().queryFor(UserType.class).build();
        ObjectQuery expected = ObjectQueryImpl.createObjectQuery((ObjectFilter) null);
        compare(actual, expected);
    }

    @Test
    public void test110EmptyBlock() {
        ObjectQuery actual = getPrismContext().queryFor(UserType.class).block().endBlock().build();
        ObjectQuery expected = ObjectQueryImpl.createObjectQuery((ObjectFilter) null);
        compare(actual, expected);
    }

    @Test
    public void test112EmptyBlocks() {
        ObjectQuery actual = getPrismContext().queryFor(UserType.class)
                .block()
                .block()
                .block()
                .endBlock()
                .endBlock()
                .endBlock()
                .build();
        ObjectQuery expected = ObjectQueryImpl.createObjectQuery((ObjectFilter) null);
        compare(actual, expected);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void test113BlocksLeftOpen() {
        getPrismContext().queryFor(UserType.class)
                .block()
                .block()
                .block()
                .endBlock()
                .endBlock()
                .build();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void test114NoOpenBlocks() {
        getPrismContext().queryFor(UserType.class)
                .block()
                .block()
                .endBlock()
                .endBlock()
                .endBlock()
                .build();
    }

    @Test
    public void test120All() {
        ObjectQuery actual = getPrismContext().queryFor(UserType.class).all().build();
        ObjectQuery expected = ObjectQueryImpl.createObjectQuery(AllFilterImpl.createAll());
        compare(actual, expected);
    }

    @Test
    public void test122AllInBlock() {
        ObjectQuery actual = getPrismContext().queryFor(UserType.class)
                .block().all().endBlock()
                .build();
        ObjectQuery expected = ObjectQueryImpl.createObjectQuery(AllFilterImpl.createAll());
        compare(actual, expected);
    }

    @Test
    public void test130SingleEquals() {
        ObjectQuery actual = getPrismContext().queryFor(UserType.class)
                .item(UserType.F_LOCALITY).eq("Caribbean")
                .build();
        ObjectQuery expected = ObjectQueryImpl.createObjectQuery(
                createEqual(UserType.F_LOCALITY, UserType.class, null, "Caribbean"));
        compare(actual, expected);
    }

    @Test
    public void test132SingleAnd() {
        ObjectQuery actual = getPrismContext().queryFor(UserType.class)
                .item(UserType.F_LOCALITY).eq("Caribbean")
                .and().item(UserType.F_DESCRIPTION).eq("desc")
                .build();
        ObjectQuery expected = ObjectQueryImpl.createObjectQuery(
                AndFilterImpl.createAnd(
                        createEqual(UserType.F_LOCALITY, UserType.class, null, "Caribbean"),
                        createEqual(UserType.F_DESCRIPTION, UserType.class, null, "desc")
                )
        );
        compare(actual, expected);
    }

    @Test
    public void test134SingleOrWithNot() {
        ObjectQuery actual = getPrismContext().queryFor(UserType.class)
                .item(UserType.F_LOCALITY).eq("Caribbean")
                .or().not().item(UserType.F_DESCRIPTION).eq("desc")
                .build();
        ObjectQuery expected = ObjectQueryImpl.createObjectQuery(
                OrFilterImpl.createOr(
                        createEqual(UserType.F_LOCALITY, UserType.class, null, "Caribbean"),
                        NotFilterImpl.createNot(
                                createEqual(UserType.F_DESCRIPTION, UserType.class, null, "desc")
                        )
                )
        );
        compare(actual, expected);
    }

    @Test
    public void test136AndOrNotSequence() {
        ObjectQuery actual = getPrismContext().queryFor(UserType.class)
                .item(UserType.F_LOCALITY).eq("Caribbean")
                .or()
                .not().item(UserType.F_DESCRIPTION).eq("desc")
                .and().all()
                .and().none()
                .or()
                .undefined()
                .and().not().none()
                .build();
        ObjectQuery expected = ObjectQueryImpl.createObjectQuery(
                OrFilterImpl.createOr(
                        createEqual(UserType.F_LOCALITY, UserType.class, null, "Caribbean"),
                        AndFilterImpl.createAnd(
                                NotFilterImpl.createNot(
                                        createEqual(UserType.F_DESCRIPTION, UserType.class, null, "desc")
                                ),
                                AllFilterImpl.createAll(),
                                NoneFilterImpl.createNone()
                        ),
                        AndFilterImpl.createAnd(
                                UndefinedFilterImpl.createUndefined(),
                                NotFilterImpl.createNot(
                                        NoneFilterImpl.createNone()
                                )
                        )
                )
        );
        compare(actual, expected);
    }

    @Test
    public void test140TypeWithEquals() {
        ObjectQuery actual = getPrismContext().queryFor(UserType.class)
                .type(UserType.class)
                .item(UserType.F_LOCALITY).eq("Caribbean")
                .build();
        ObjectQuery expected = ObjectQueryImpl.createObjectQuery(
                TypeFilterImpl.createType(
                        USER_TYPE_QNAME,
                        createEqual(UserType.F_LOCALITY, UserType.class, null, "Caribbean")
                )
        );
        compare(actual, expected);
    }

    @Test
    public void test142TypeWithEqualsInBlock() {
        ObjectQuery actual = getPrismContext().queryFor(UserType.class)
                .type(UserType.class)
                .block()
                .item(UserType.F_LOCALITY).eq("Caribbean")
                .endBlock()
                .build();
        ObjectQuery expected = ObjectQueryImpl.createObjectQuery(
                TypeFilterImpl.createType(
                        USER_TYPE_QNAME,
                        createEqual(UserType.F_LOCALITY, UserType.class, null, "Caribbean")
                )
        );
        compare(actual, expected);
    }

    @Test
    public void test144TypeWithEqualsAndAllInBlock() {
        ObjectQuery actual = getPrismContext().queryFor(UserType.class)
                .type(UserType.class)
                .block()
                .item(UserType.F_LOCALITY).eq("Caribbean")
                .and().all()
                .endBlock()
                .build();
        ObjectQuery expected = ObjectQueryImpl.createObjectQuery(
                TypeFilterImpl.createType(
                        USER_TYPE_QNAME,
                        AndFilterImpl.createAnd(
                                createEqual(UserType.F_LOCALITY, UserType.class, null, "Caribbean"),
                                AllFilterImpl.createAll()
                        )
                )
        );
        compare(actual, expected);
    }

    @Test
    public void test144TypeInTypeInType() {
        ObjectQuery actual = getPrismContext().queryFor(UserType.class)
                .type(UserType.class)
                .type(AssignmentType.class)
                .type(UserType.class)
                .all()
                .build();
        ObjectQuery expected = ObjectQueryImpl.createObjectQuery(
                TypeFilterImpl.createType(
                        USER_TYPE_QNAME,
                        TypeFilterImpl.createType(
                                ASSIGNMENT_TYPE_QNAME,
                                TypeFilterImpl.createType(
                                        USER_TYPE_QNAME,
                                        AllFilterImpl.createAll()
                                )
                        )
                )
        );
        compare(actual, expected);
    }

    @Test
    public void test146TypeAndSomething() {
        ObjectQuery actual = getPrismContext().queryFor(UserType.class)
                .type(UserType.class)
                .none()
                .and().all()
                .build();
        ObjectQuery expected = ObjectQueryImpl.createObjectQuery(
                AndFilterImpl.createAnd(
                        TypeFilterImpl.createType(
                                USER_TYPE_QNAME,
                                NoneFilterImpl.createNone()
                        ),
                        AllFilterImpl.createAll()
                )
        );
        compare(actual, expected);
    }

    @Test
    public void test148TypeEmpty() {
        ObjectQuery actual = getPrismContext().queryFor(UserType.class)
                .type(UserType.class)
                .block().endBlock()
                .build();
        ObjectQuery expected = ObjectQueryImpl.createObjectQuery(
                TypeFilterImpl.createType(
                        USER_TYPE_QNAME,
                        null
                )
        );
        compare(actual, expected);
    }

    @Test
    public void test149TypeEmptyAndAll() {
        ObjectQuery actual = getPrismContext().queryFor(UserType.class)
                .type(UserType.class)
                .block().endBlock()
                .and().all()
                .build();
        ObjectQuery expected = ObjectQueryImpl.createObjectQuery(
                AndFilterImpl.createAnd(
                        TypeFilterImpl.createType(
                                USER_TYPE_QNAME,
                                null
                        ),
                        AllFilterImpl.createAll()
                )
        );
        compare(actual, expected);
    }

    @Test
    public void test150ExistsWithEquals() {
        ObjectQuery actual = getPrismContext().queryFor(UserType.class)
                .exists(UserType.F_ASSIGNMENT)
                .item(AssignmentType.F_DESCRIPTION).startsWith("desc1")
                .build();
        ComplexTypeDefinition assCtd = getPrismContext().getSchemaRegistry()
                .findComplexTypeDefinitionByCompileTimeClass(AssignmentType.class);
        PrismContainerDefinition<?> userPcd = getPrismContext().getSchemaRegistry()
                .findContainerDefinitionByCompileTimeClass(UserType.class);
        ObjectQuery expected = ObjectQueryImpl.createObjectQuery(
                ExistsFilterImpl.createExists(
                        UserType.F_ASSIGNMENT,
                        userPcd,
                        SubstringFilterImpl.createSubstring(
                                AssignmentType.F_DESCRIPTION,
                                assCtd.findPropertyDefinition(AssignmentType.F_DESCRIPTION),
                                null, "desc1", true, false)));
        compare(actual, expected);
    }

    @Test
    public void test151ExistsWithEquals2() {
        ObjectQuery actual = getPrismContext().queryFor(UserType.class)
                .exists(UserType.F_ASSIGNMENT)
                .item(AssignmentType.F_NOTE).endsWith("DONE.")
                .build();
        ComplexTypeDefinition assCtd = getPrismContext().getSchemaRegistry()
                .findComplexTypeDefinitionByCompileTimeClass(AssignmentType.class);
        PrismContainerDefinition<?> userPcd = getPrismContext().getSchemaRegistry()
                .findContainerDefinitionByCompileTimeClass(UserType.class);
        ObjectQuery expected = ObjectQueryImpl.createObjectQuery(
                ExistsFilterImpl.createExists(
                        UserType.F_ASSIGNMENT,
                        userPcd,
                        SubstringFilterImpl.createSubstring(
                                AssignmentType.F_NOTE,
                                assCtd.findPropertyDefinition(AssignmentType.F_NOTE),
                                null, "DONE.", false, true)));
        compare(actual, expected);
    }

    @Test
    public void test152ExistsWithEqualsInBlock() {
        ObjectQuery actual = getPrismContext().queryFor(UserType.class)
                .exists(UserType.F_ASSIGNMENT)
                .block()
                .item(AssignmentType.F_NOTE).endsWith("DONE.")
                .endBlock()
                .build();
        ComplexTypeDefinition assCtd = getPrismContext().getSchemaRegistry()
                .findComplexTypeDefinitionByCompileTimeClass(AssignmentType.class);
        PrismContainerDefinition<?> userPcd = getPrismContext().getSchemaRegistry()
                .findContainerDefinitionByCompileTimeClass(UserType.class);
        ObjectQuery expected = ObjectQueryImpl.createObjectQuery(
                ExistsFilterImpl.createExists(
                        UserType.F_ASSIGNMENT,
                        userPcd,
                        SubstringFilterImpl.createSubstring(
                                AssignmentType.F_NOTE,
                                assCtd.findPropertyDefinition(AssignmentType.F_NOTE),
                                null, "DONE.", false, true)));
        compare(actual, expected);
    }

    @Test
    public void test154ExistsWithEqualsAndAllInBlock() {
        ObjectQuery actual = getPrismContext().queryFor(UserType.class)
                .exists(UserType.F_ASSIGNMENT)
                .block()
                .item(AssignmentType.F_NOTE).endsWith("DONE.")
                .and().all()
                .endBlock()
                .build();
        ComplexTypeDefinition assCtd = getPrismContext().getSchemaRegistry()
                .findComplexTypeDefinitionByCompileTimeClass(AssignmentType.class);
        PrismContainerDefinition<?> userPcd = getPrismContext().getSchemaRegistry()
                .findContainerDefinitionByCompileTimeClass(UserType.class);
        ObjectQuery expected = ObjectQueryImpl.createObjectQuery(
                ExistsFilterImpl.createExists(
                        UserType.F_ASSIGNMENT,
                        userPcd,
                        AndFilterImpl.createAnd(
                                SubstringFilterImpl.createSubstring(
                                        AssignmentType.F_NOTE,
                                        assCtd.findPropertyDefinition(AssignmentType.F_NOTE),
                                        null, "DONE.", false, true),
                                AllFilterImpl.createAll())));
        compare(actual, expected);
    }

    @Test
    public void test156ExistsAndSomething() {
        ObjectQuery actual = getPrismContext().queryFor(UserType.class)
                .exists(UserType.F_ASSIGNMENT)
                .none()
                .and().all()
                .build();

        PrismContainerDefinition<?> userPcd = getPrismContext().getSchemaRegistry()
                .findContainerDefinitionByCompileTimeClass(UserType.class);
        ObjectQuery expected = ObjectQueryImpl.createObjectQuery(
                AndFilterImpl.createAnd(
                        ExistsFilterImpl.createExists(
                                UserType.F_ASSIGNMENT,
                                userPcd,
                                NoneFilterImpl.createNone()),
                        AllFilterImpl.createAll()));
        compare(actual, expected);
    }

    @Test
    public void test200OrderByName() {
        ObjectQuery actual = getPrismContext().queryFor(UserType.class)
                .asc(UserType.F_NAME)
                .build();
        ObjectQuery expected = ObjectQueryImpl.createObjectQuery(
                ObjectPagingImpl.createPaging(UserType.F_NAME, OrderDirection.ASCENDING)
        );
        compare(actual, expected);
    }

    @Test
    public void test210OrderByNameAndId() {
        ObjectQuery actual = getPrismContext().queryFor(UserType.class)
                .item(UserType.F_LOCALITY).eq("Caribbean")
                .asc(UserType.F_NAME)
                .desc(PrismConstants.T_ID)
                .build();
        ObjectPaging paging = ObjectPagingImpl.createEmptyPaging();
        paging.addOrderingInstruction(UserType.F_NAME, OrderDirection.ASCENDING);
        paging.addOrderingInstruction(ItemPath.create(PrismConstants.T_ID), OrderDirection.DESCENDING);
        ObjectQuery expected = ObjectQueryImpl.createObjectQuery(
                createEqual(UserType.F_LOCALITY, UserType.class, null, "Caribbean"),
                paging);
        compare(actual, expected);
    }

    @Test
    public void test300EqualItem() {
        ObjectQuery actual = getPrismContext().queryFor(UserType.class)
                .item(UserType.F_LOCALITY).eq().item(UserType.F_NAME)
                .build();
        PrismContainerDefinition<?> userPcd = getPrismContext().getSchemaRegistry()
                .findContainerDefinitionByCompileTimeClass(UserType.class);
        ObjectQuery expected = ObjectQueryImpl.createObjectQuery(
                EqualFilterImpl.createEqual(
                        UserType.F_LOCALITY,
                        userPcd.findPropertyDefinition(UserType.F_LOCALITY),
                        null,
                        UserType.F_NAME,
                        null));
        compare(actual, expected);
    }

    @Test
    public void test310LessThanItem() {
        PrismObjectDefinition<?> userDef =
                getSchemaRegistry().findObjectDefinitionByCompileTimeClass(UserType.class);
        PrismPropertyDefinition<?> nameDef = userDef.findPropertyDefinition(UserType.F_NAME);
        PrismPropertyDefinition<?> localityDef = userDef.findPropertyDefinition(UserType.F_LOCALITY);
        ObjectQuery actual = getPrismContext().queryFor(UserType.class)
                .item(UserType.F_LOCALITY, localityDef)
                .le()
                .item(UserType.F_NAME, nameDef)
                .build();
        ObjectQuery expected = ObjectQueryImpl.createObjectQuery(
                LessFilterImpl.createLess(UserType.F_LOCALITY, localityDef, null,
                        UserType.F_NAME, nameDef, true)
        );
        compare(actual, expected);
    }

    @Test
    public void test400ExtendedRefFilter() {
        given("proper definitions");
        PrismObjectDefinition<?> userDef =
                getSchemaRegistry().findObjectDefinitionByCompileTimeClass(UserType.class);
        PrismReferenceDefinition accountRefDef = userDef.findReferenceDefinition(UserType.F_ACCOUNT_REF);

        when("reference filter is created with no value with build() shortcut (no need for block().endBlock())");
        ObjectQuery actual = getPrismContext().queryFor(UserType.class)
                .ref(UserType.F_ACCOUNT_REF)
                .build();

        then("filter is built without error and is equal to simple ref filter with no value");
        ObjectQuery expected = ObjectQueryImpl.createObjectQuery(
                RefFilterImpl.createReferenceEqual(UserType.F_ACCOUNT_REF, accountRefDef, List.of()));
        compare(actual, expected);
    }

    @Test
    public void test401ExtendedRefFilterWithSimpleTargetFilter() {
        given("proper definitions");
        PrismObjectDefinition<?> userDef =
                getSchemaRegistry().findObjectDefinitionByCompileTimeClass(UserType.class);
        PrismReferenceDefinition accountRefDef = userDef.findReferenceDefinition(UserType.F_ACCOUNT_REF);
        PrismPropertyDefinition<?> nameDefinition = userDef.findPropertyDefinition(UserType.F_NAME); // works for account name too

        when("reference filter with nested target filter is created");
        ObjectQuery actual = getPrismContext().queryFor(UserType.class)
                .ref(UserType.F_ACCOUNT_REF, ACCOUNT_TYPE_QNAME, null)
                .item(AccountType.F_NAME).eq("account-name")
                .build();

        then("filter is built without error and matches expected structure");
        ObjectQuery expected = ObjectQueryImpl.createObjectQuery(
                RefFilterImpl.createReferenceEqual(UserType.F_ACCOUNT_REF, accountRefDef,
                        List.of(getPrismContext().itemFactory().createReferenceValue(null, ACCOUNT_TYPE_QNAME)),
                        EqualFilterImpl.createEqual(AccountType.F_NAME, nameDefinition, null, "account-name")));
        compare(actual, expected);
    }

    @Test
    public void test402ExtendedRefFilterWithImmediateAnd() {
        given("proper definitions");
        PrismObjectDefinition<?> userDef =
                getSchemaRegistry().findObjectDefinitionByCompileTimeClass(UserType.class);
        PrismReferenceDefinition accountRefDef = userDef.findReferenceDefinition(UserType.F_ACCOUNT_REF);
        PrismPropertyDefinition<?> nameDefinition = userDef.findPropertyDefinition(UserType.F_NAME); // works for account name too

        when("reference filter and another filter (not nested)");
        ObjectQuery actual = getPrismContext().queryFor(UserType.class)
                .ref(UserType.F_ACCOUNT_REF)
                .and()
                .item(UserType.F_NAME).eq("user-name")
                .build();

        then("filter is built without error and matches expected structure");
        ObjectQuery expected = ObjectQueryImpl.createObjectQuery(
                AndFilterImpl.createAnd(
                        RefFilterImpl.createReferenceEqual(UserType.F_ACCOUNT_REF, accountRefDef, List.of()),
                        EqualFilterImpl.createEqual(UserType.F_NAME, nameDefinition, null, "user-name")));
        compare(actual, expected);
    }

    @Test
    public void test403ExtendedRefFilterWithNestedFilterFollowedByAnd() {
        given("proper definitions");
        PrismObjectDefinition<?> userDef =
                getSchemaRegistry().findObjectDefinitionByCompileTimeClass(UserType.class);
        PrismReferenceDefinition accountRefDef = userDef.findReferenceDefinition(UserType.F_ACCOUNT_REF);
        PrismPropertyDefinition<?> nameDefinition = userDef.findPropertyDefinition(UserType.F_NAME); // works for account name too

        when("reference filter with nested filter and another filter outside nested filter because block() is not used");
        ObjectQuery actual = getPrismContext().queryFor(UserType.class)
                .ref(UserType.F_ACCOUNT_REF)
                .item(AccountType.F_NAME).eq("account-name")
                .and()
                .item(UserType.F_NAME).eq("user-name")
                .build();

        then("filter is built without error and matches expected structure");
        ObjectQuery expected = ObjectQueryImpl.createObjectQuery(
                AndFilterImpl.createAnd(
                        RefFilterImpl.createReferenceEqual(UserType.F_ACCOUNT_REF, accountRefDef, List.of(),
                                EqualFilterImpl.createEqual(AccountType.F_NAME, nameDefinition, null, "account-name")),
                        EqualFilterImpl.createEqual(UserType.F_NAME, nameDefinition, null, "user-name")));
        compare(actual, expected);
    }

    @Test
    public void test410OwnedBy() {
        given("proper definitions");
        PrismObjectDefinition<?> userDef =
                getSchemaRegistry().findObjectDefinitionByCompileTimeClass(UserType.class);
        PrismPropertyDefinition<?> nameDefinition = userDef.findPropertyDefinition(UserType.F_NAME);
        PrismPropertyDefinition<?> activationDefinition =
                userDef.findContainerDefinition(UserType.F_ACTIVATION)
                        .findPropertyDefinition(ActivationType.F_ENABLED);

        when("owned-by filter with nested filter and another filter outside nested filter because block() is not used");
        ObjectQuery actual = getPrismContext().queryFor(ActivationType.class)
                .ownedBy(UserType.class, UserType.F_ACTIVATION)
                .item(UserType.F_NAME).eq("user-name")
                .and()
                .item(ActivationType.F_ENABLED).eq(true)
                .build();

        then("filter is built without error and matches expected structure");
        ObjectQuery expected = ObjectQueryImpl.createObjectQuery(
                AndFilterImpl.createAnd(
                        OwnedByFilterImpl.create(USER_TYPE_QNAME, UserType.F_ACTIVATION,
                                EqualFilterImpl.createEqual(UserType.F_NAME, nameDefinition, null, "user-name")),
                        EqualFilterImpl.createEqual(ActivationType.F_ENABLED, activationDefinition, null, true)));
        compare(actual, expected);
    }

    @Test
    public void test420ReferencedBy() {
        given("proper definitions");
        PrismObjectDefinition<?> userDef =
                getSchemaRegistry().findObjectDefinitionByCompileTimeClass(UserType.class);
        PrismPropertyDefinition<?> nameDefinition = userDef.findPropertyDefinition(UserType.F_NAME); // works for account name too
        PrismPropertyDefinition<?> fullNameDefinition = userDef.findPropertyDefinition(UserType.F_FULL_NAME);
        QName someRelation = QName.valueOf("some-relation");

        when("referenced-by filter with nested complex filter and another filter on root");
        ObjectQuery actual = getPrismContext().queryFor(AccountType.class)
                .referencedBy(UserType.class, UserType.F_ACCOUNT_REF, someRelation)
                .block()
                .item(UserType.F_NAME).eq("user-name")
                .or()
                .item(UserType.F_FULL_NAME).eq("full-name")
                .endBlock()
                .and()
                .item(AccountType.F_NAME).eq("account-name")
                .build();

        then("filter is built without error and matches expected structure");
        ObjectQuery expected = ObjectQueryImpl.createObjectQuery(
                AndFilterImpl.createAnd(
                        ReferencedByFilterImpl.create(USER_TYPE_QNAME, UserType.F_ACCOUNT_REF,
                                OrFilterImpl.createOr(
                                        EqualFilterImpl.createEqual(UserType.F_NAME, nameDefinition, null, "user-name"),
                                        EqualFilterImpl.createEqual(UserType.F_FULL_NAME, fullNameDefinition, null, "full-name")),
                                someRelation),
                        EqualFilterImpl.createEqual(AccountType.F_NAME, nameDefinition, null, "account-name")));
        compare(actual, expected);
    }

    @Test
    public void test500ReferenceSearchFilterSimple() {
        when("reference search is created without further conditions");
        ObjectQuery actual = getPrismContext().queryForReferenceOwnedBy(UserType.class, UserType.F_ACCOUNT_REF)
                .build();

        then("filter is built without error and is equal to simple owned by filter");
        ObjectQuery expected = ObjectQueryImpl.createObjectQuery(
                OwnedByFilterImpl.create(USER_TYPE_QNAME, UserType.F_ACCOUNT_REF, null));
        compare(actual, expected);
    }

    @Test
    public void test501ReferenceSearchFilterWithRefFilter() {
        given("proper definitions");
        PrismObjectDefinition<?> userDef =
                getSchemaRegistry().findObjectDefinitionByCompileTimeClass(UserType.class);
        PrismPropertyDefinition<?> nameDefinition = userDef.findPropertyDefinition(UserType.F_NAME); // works for account name too
        PrismReferenceDefinition accountRefDef = userDef.findReferenceDefinition(UserType.F_ACCOUNT_REF);
        QName someRelation = QName.valueOf("some-relation");

        when("reference search is created with owned by condition and ref filter");
        ObjectQuery actual = getPrismContext().queryForReferenceOwnedBy(UserType.class, UserType.F_ACCOUNT_REF)
                .id("user-oid") // filtering the owner of the searched reference
                .and() // IMPORTANT to get out of ownedBy filter!
                .ref(ItemPath.SELF_PATH, ACCOUNT_TYPE_QNAME, someRelation)
                .item(AccountType.F_NAME).eq("account-name")
                .build();

        then("filter is built without error and matches expected structure");
        ObjectQuery expected = ObjectQueryImpl.createObjectQuery(
                AndFilterImpl.createAnd(
                        OwnedByFilterImpl.create(USER_TYPE_QNAME, UserType.F_ACCOUNT_REF,
                                InOidFilterImpl.createInOid("user-oid")),
                        RefFilterImpl.createReferenceEqual(ItemPath.SELF_PATH, accountRefDef,
                                List.of(getPrismContext().itemFactory().createReferenceValue(null, ACCOUNT_TYPE_QNAME)
                                        .relation(someRelation)),
                                EqualFilterImpl.createEqual(UserType.F_NAME, nameDefinition, null, "account-name"))));
        compare(actual, expected);
    }

    @Test
    public void test502SelfQNameIsConvertedToSelfPath() {
        given("proper definitions");
        PrismObjectDefinition<?> userDef =
                getSchemaRegistry().findObjectDefinitionByCompileTimeClass(UserType.class);
        PrismReferenceDefinition accountRefDef = userDef.findReferenceDefinition(UserType.F_ACCOUNT_REF);

        when("reference search is created using T_SELF constant (where SELF_PATH is preferred)");
        ObjectQuery actual = getPrismContext().queryForReferenceOwnedBy(UserType.class, UserType.F_ACCOUNT_REF)
                .and()
                .item(PrismConstants.T_SELF).ref(null, ACCOUNT_TYPE_QNAME, null)
                .build();

        then("filter is built without error and matches expected structure");
        ObjectQuery expected = ObjectQueryImpl.createObjectQuery(
                AndFilterImpl.createAnd(
                        OwnedByFilterImpl.create(USER_TYPE_QNAME, UserType.F_ACCOUNT_REF, null),
                        RefFilterImpl.createReferenceEqual(ItemPath.SELF_PATH, accountRefDef,
                                List.of(getPrismContext().itemFactory().createReferenceValue(null, ACCOUNT_TYPE_QNAME)))));
        compare(actual, expected);
    }

    protected void compare(ObjectQuery actual, ObjectQuery expected) {
        String exp = expected.debugDump();
        String act = actual.debugDump();
        System.out.println("Generated query:\n" + act);
        AssertJUnit.assertEquals("queries do not match", exp, act);
    }

    private <C extends Containerable, T> EqualFilter<T> createEqual(
            ItemPath propertyPath, Class<C> type, QName matchingRule, T realValue) {
        //noinspection unchecked
        PrismPropertyDefinition<T> propertyDefinition = (PrismPropertyDefinition<T>)
                FilterImplUtil.findItemDefinition(propertyPath, type, getPrismContext());
        return EqualFilterImpl.createEqual(
                propertyPath, propertyDefinition, matchingRule, realValue);
    }
}
