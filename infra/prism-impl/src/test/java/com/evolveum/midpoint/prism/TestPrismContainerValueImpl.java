/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism;

import static com.evolveum.midpoint.prism.PrismInternalTestUtil.USER_ALICE_METADATA_FILE;
import static com.evolveum.midpoint.prism.PrismInternalTestUtil.constructInitializedPrismContext;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.io.IOException;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.evolveum.midpoint.prism.impl.PrismContextImpl;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.path.ItemPathCollectionsUtil;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.exception.SchemaException;

public class TestPrismContainerValueImpl extends AbstractPrismTest {

    private PrismObject<Objectable> userWithMetadata;
    private PrismContextImpl prismContext;

    @BeforeClass
    void createPrismContext() throws SchemaException, IOException, SAXException {
        this.prismContext = constructInitializedPrismContext();
    }

    @BeforeMethod
    void initUser() throws SchemaException {
        final Document userWithMetadataXml = DOMUtil.parseFile(USER_ALICE_METADATA_FILE);
        final Element userWithMetadataXmlElement = DOMUtil.getFirstChildElement(userWithMetadataXml);
        this.userWithMetadata = prismContext.parserFor(userWithMetadataXmlElement).parse();
    }

    @Test
    void userContainsMetadata_metadataAreRemovedFromDeeperStructureUsingPath_metadataShouldNotBePresentAnymore()
            throws SchemaException {
        this.userWithMetadata.getValue().removeMetadataFromPaths(ItemPathCollectionsUtil.pathListFromStrings(
                List.of("assignment/accountConstruction"), this.prismContext));

        final PrismContainerValue<?> assignment = (PrismContainerValue<?>) this.userWithMetadata.getValue().findItem(
                ItemPath.fromString("assignment")).getValue();
        final PrismValue accountConstruction = assignment.findItem(ItemPath.fromString("accountConstruction"))
                .getValue();
        assertFalse(accountConstruction.hasValueMetadata());
    }

    @Test
    void userContainsMetadata_metadataAreRemovedFromWholeBranchUsingPath_metadataShouldNotBePresentAnymore()
            throws SchemaException {
        this.userWithMetadata.getValue().removeMetadataFromPaths(ItemPathCollectionsUtil.pathListFromStrings(
                List.of("assignment", "assignment/accountConstruction"), this.prismContext));

        final PrismContainerValue<?> assignment = (PrismContainerValue<?>) this.userWithMetadata.getValue().findItem(
                ItemPath.fromString("assignment")).getValue();
        assertFalse(assignment.hasValueMetadata());
        final PrismValue accountConstruction = assignment.findItem(ItemPath.fromString("accountConstruction"))
                .getValue();
        assertFalse(accountConstruction.hasValueMetadata());
    }

    @Test
    void userContainsMetadata_metadataAreRemovedFromSequenceUsingPath_metadataShouldNotBePresentAnymore()
            throws SchemaException {
        this.userWithMetadata.getValue().removeMetadataFromPaths(ItemPathCollectionsUtil.pathListFromStrings(
                List.of("additionalNames"), this.prismContext));

        final Item<PrismValue, ItemDefinition<?>> additionalNamesItem = this.userWithMetadata.getValue().findItem(
                ItemPath.fromString("additionalNames"));
        additionalNamesItem.getValues().forEach(additionalName -> assertFalse(additionalName.hasValueMetadata(),
                "Value metadata of \"additionalName\" property should be removed from the user"));
    }

    @Test
    void userContainsMetadata_metadataAreRemovedFromRootUsingPath_metadataShouldNotBePresentAnymore()
            throws SchemaException {
        this.userWithMetadata.getValue().removeMetadataFromPaths(ItemPathCollectionsUtil.pathListFromStrings(
                List.of("/"), this.prismContext));

        assertFalse(this.userWithMetadata.getValue().hasValueMetadata(),
                "Value metadata of \"additionalName\" property should be removed from the user");
    }

    @Test
    void pathsToRemoveAreRelatives_removeDataUsingPath_theUpperObjectShouldBeRemovedWithWholeSubtree()
            throws SchemaException {
        this.userWithMetadata.getValue().removePaths(ItemPathCollectionsUtil.pathListFromStrings(
                List.of("assignment", "assignment/description"), this.prismContext));

        assertNull(userWithMetadata.getValue().findItem(ItemPath.fromString("assignment")),
                "Assignments should be removed from the user.");
    }

    @Test
    void lastPartOfPathToRemoveIsAlsoInParent_removeDataUsingPath_theParentShouldNotBeTouched()
            throws SchemaException {
        this.userWithMetadata.getValue().removePaths(ItemPathCollectionsUtil.pathListFromStrings(
                List.of("assignment/description"), this.prismContext));


        assertNotNull(userWithMetadata.getValue().findItem(ItemPath.fromString("description")),
                "Description should not be removed from the user.");
        final Object assignmentDescription = userWithMetadata.getValue().findItem(ItemPath.fromString("assignment"))
                .getValue().find(ItemPath.fromString("description"));
        assertNull(assignmentDescription, "Description should be removed from the user's assignment.");
    }
}
