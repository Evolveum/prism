/*
 * Copyright (c) 2010-2013 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism;

import static com.evolveum.midpoint.prism.PrismInternalTestUtil.*;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.AssertJUnit.*;

import java.io.IOException;
import java.util.stream.Collectors;

import org.testng.annotations.Test;

import com.evolveum.midpoint.prism.foo.AccountConstructionType;
import com.evolveum.midpoint.prism.foo.AssignmentType;
import com.evolveum.midpoint.prism.foo.UserType;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.prism.util.PrismTestUtil;
import com.evolveum.midpoint.util.exception.SchemaException;

/**
 * Exercises "find-like" methods, including {@link Item#getAllValues(ItemPath)} and {@link Item#getAllItems(ItemPath)}.
 *
 * @author semancik
 */
public class TestFind extends AbstractPrismTest {

    @Test
    public void testFindString() throws SchemaException, IOException {
        // GIVEN
        PrismObject<UserType> user = parseJack();
        ItemPath path = UserType.F_DESCRIPTION;

        // WHEN
        PrismProperty<String> nameProperty = findProperty(user, path);

        // THEN
        assertEquals("Wrong property value (path=" + path + ")", USER_JACK_DESCRIPTION, nameProperty.getRealValue());
        assertSame("QName found something other", nameProperty, user.findProperty(UserType.F_DESCRIPTION));
    }

    @Test
    public void testFindPolyString() throws SchemaException, IOException {
        // GIVEN
        PrismObject<UserType> user = parseJack();
        ItemPath path = UserType.F_POLY_NAME;

        // WHEN
        PrismProperty<PolyString> nameProperty = findProperty(user, path);

        // THEN
        PrismInternalTestUtil.assertJackPolyName(nameProperty, user, true);
        assertSame("QName found something other", nameProperty, user.findProperty(UserType.F_POLY_NAME));
    }

    @Test
    public void testFindPolyStringOrig() throws SchemaException, IOException {
        // GIVEN
        ItemPath path = ItemPath.create(UserType.F_POLY_NAME, PolyString.F_ORIG);

        // WHEN
        Object found = findUser(path);

        // THEN
        assertEquals("Wrong property value (path=" + path + ")", USER_JACK_POLYNAME_ORIG, found);
    }

    @Test
    public void testFindPolyStringNorm() throws SchemaException, IOException {
        // GIVEN
        ItemPath path = ItemPath.create(UserType.F_POLY_NAME, PolyString.F_NORM);

        // WHEN
        Object found = findUser(path);

        // THEN
        assertEquals("Wrong property value (path=" + path + ")", USER_JACK_POLYNAME_NORM, found);
    }

    @Test
    public void testFindExtensionBar() throws SchemaException, IOException {
        // GIVEN
        ItemPath path = ItemPath.create(UserType.F_EXTENSION, EXTENSION_BAR_ELEMENT);

        // WHEN
        PrismProperty<String> property = findUserProperty(path);

        // THEN
        assertEquals("Wrong property value (path=" + path + ")", "BAR", property.getAnyRealValue());
    }

    @Test
    public void testFindAssignment1Description() throws SchemaException, IOException {
        // GIVEN
        ItemPath path = ItemPath.create(UserType.F_ASSIGNMENT, USER_ASSIGNMENT_1_ID, AssignmentType.F_DESCRIPTION);

        // WHEN
        PrismProperty<String> property = findUserProperty(path);

        // THEN
        assertEquals("Wrong property value (path=" + path + ")", "Assignment 1", property.getRealValue());
    }

    @Test
    public void testFindAssignment2Construction() throws SchemaException, IOException {
        // GIVEN
        ItemPath path = ItemPath.create(UserType.F_ASSIGNMENT, USER_ASSIGNMENT_2_ID, AssignmentType.F_ACCOUNT_CONSTRUCTION);

        // WHEN
        PrismProperty<AccountConstructionType> property = findUserProperty(path);

        // THEN
        assertEquals("Wrong property value (path=" + path + ")", "Just do it",
                requireNonNull(property.getRealValue()).getHowto());
    }

    @Test
    public void testFindAssignment() throws SchemaException, IOException {
        // GIVEN
        ItemPath path = UserType.F_ASSIGNMENT;

        // WHEN
        PrismContainer<AssignmentType> container = findUserContainer(path);

        // THEN
        PrismContainerValue<AssignmentType> value2 = container.getValue(USER_ASSIGNMENT_2_ID);
        assertEquals("Wrong value2 description (path=" + path + ")", "Assignment 2", value2.findProperty(AssignmentType.F_DESCRIPTION).getRealValue());
    }

    @Test
    public void testFindAllValues()  throws SchemaException, IOException {
        given("a user");
        PrismObject<UserType> user = parseJack();

        assertAllValues(user, ItemPath.EMPTY_PATH, user.getRealValue());
        assertAllValues(user, UserType.F_DESCRIPTION, "This must be the best pirate the world has ever seen");
        assertAllValues(user, ItemPath.create(UserType.F_ASSIGNMENT, AssignmentType.F_DESCRIPTION), "Assignment 1", "Assignment 2");
        assertAllValues(user, ItemPath.create("extension", "indexedString"), "alpha", "bravo");
        assertAllValues(user, ItemPath.create("extension", "multi"), "raz", "dva", "tri");
    }

    private void assertAllValues(Item<?, ?> item, ItemPath path, Object... expectedValues) {
        when("getting values of '" + path + "'");
        var allRealValues = item.getAllValues(path).stream()
                .map(PrismValue::getRealValue)
                .collect(Collectors.toSet());

        then("they are as expected");
        assertThat(allRealValues)
                .as("all real values of '" + path + "' in " + item)
                .containsExactlyInAnyOrder(expectedValues);
    }

    @Test
    public void testFindAllItems()  throws SchemaException, IOException {
        given("a user");
        PrismObject<UserType> user = parseJack();

        ItemPath indexedStringPath = ItemPath.create(UserType.F_EXTENSION, "indexedString");
        ItemPath multiPath = ItemPath.create("extension", "multi");
        ItemPath assignmentDescriptionPath = ItemPath.create(UserType.F_ASSIGNMENT, AssignmentType.F_DESCRIPTION);
        ItemPath assignment1111DescriptionPath = ItemPath.create(UserType.F_ASSIGNMENT, 1111, AssignmentType.F_DESCRIPTION);
        ItemPath assignment1112DescriptionPath = ItemPath.create(UserType.F_ASSIGNMENT, 1112, AssignmentType.F_DESCRIPTION);

        var descriptionItem = requireNonNull(user.findItem(UserType.F_DESCRIPTION));
        var assignmentItem = requireNonNull(user.findItem(UserType.F_ASSIGNMENT));
        var indexedStringItem = requireNonNull(user.findProperty(indexedStringPath));
        var multiItem = requireNonNull(user.findProperty(multiPath));
        var assignment1111Description = requireNonNull(user.findProperty(assignment1111DescriptionPath));
        var assignment1112Description = requireNonNull(user.findProperty(assignment1112DescriptionPath));

        assertAllItems(user, ItemPath.EMPTY_PATH, user);
        assertAllItems(assignmentItem, ItemPath.EMPTY_PATH, assignmentItem);
        assertAllItems(assignment1111Description, ItemPath.EMPTY_PATH, assignment1111Description);

        assertAllItems(user, UserType.F_DESCRIPTION, descriptionItem);
        assertAllItems(user, assignmentDescriptionPath, assignment1111Description, assignment1112Description);
        assertAllItems(user, indexedStringPath, indexedStringItem);
        assertAllItems(user, multiPath, multiItem);

        assertAllItems(assignmentItem, AssignmentType.F_DESCRIPTION, assignment1111Description, assignment1112Description);
    }

    private void assertAllItems(Item<?, ?> item, ItemPath path, Item<?, ?>... expectedItems) {
        when("getting items of '" + path + "'");
        var allItems = item.getAllItems(path);

        then("they are as expected");
        assertThat(allItems)
                .as("all items of '" + path + "' in " + item)
                .containsExactlyInAnyOrder(expectedItems);
    }

    private <T> T findUser(ItemPath path) throws SchemaException, IOException {
        PrismObject<UserType> user = parseJack();
        return find(user, path);
    }

    private <T> T find(PrismObject<UserType> user, ItemPath path) {
        System.out.println("Path:");
        System.out.println(path);

        // WHEN
        Object found = user.find(path);

        // THEN
        System.out.println("Found:");
        System.out.println(found);
        //noinspection unchecked
        return (T) found;
    }

    private <T> PrismProperty<T> findUserProperty(ItemPath path) throws SchemaException, IOException {
        PrismObject<UserType> user = parseJack();
        return findProperty(user, path);
    }

    private <T> PrismProperty<T> findProperty(PrismObject<UserType> user, ItemPath path) {
        System.out.println("Path:");
        System.out.println(path);

        // WHEN
        PrismProperty<T> property = user.findProperty(path);

        // THEN
        System.out.println("Found:");
        System.out.println(property);
        return property;
    }

    private <T extends Containerable> PrismContainer<T> findUserContainer(ItemPath path) throws SchemaException, IOException {
        PrismObject<UserType> user = parseJack();
        return findContainer(user, path);
    }

    private <T extends Containerable> PrismContainer<T> findContainer(PrismObject<UserType> user, ItemPath path) {
        System.out.println("Path:");
        System.out.println(path);

        // WHEN
        PrismContainer<T> container = user.findContainer(path);

        // THEN
        System.out.println("Found:");
        System.out.println(container);
        return container;
    }

    public PrismObject<UserType> parseJack() throws SchemaException, IOException {
        return PrismTestUtil.parseObject(USER_JACK_FILE_XML);
    }
}
