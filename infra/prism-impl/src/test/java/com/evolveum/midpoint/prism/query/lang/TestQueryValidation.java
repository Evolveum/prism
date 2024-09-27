package com.evolveum.midpoint.prism.query.lang;

import static org.assertj.core.api.Assertions.*;
import static com.evolveum.midpoint.prism.PrismInternalTestUtil.DEFAULT_NAMESPACE_PREFIX;

import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.foo.RoleType;
import com.evolveum.midpoint.prism.path.ItemPath;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import com.evolveum.axiom.lang.antlr.AxiomQueryError;
import com.evolveum.midpoint.prism.impl.query.lang.AxiomQueryContentAssistImpl;
import com.evolveum.midpoint.prism.query.AxiomQueryContentAssist;
import com.evolveum.midpoint.prism.util.PrismTestUtil;
import com.evolveum.midpoint.util.PrettyPrinter;
import com.evolveum.midpoint.util.exception.SchemaException;

import javax.xml.namespace.QName;

/**
 * Created by Dominik.
 */
public class TestQueryValidation extends AbstractPrismTest {
    AxiomQueryContentAssist axiomQueryContentAssist;
    ItemDefinition<?> typeDefinition;

    @BeforeSuite
    public void setupDebug() throws SchemaException, SAXException, IOException {
        PrettyPrinter.addDefaultNamespacePrefix(DEFAULT_NAMESPACE_PREFIX);
        PrismTestUtil.resetPrismContext(new PrismInternalTestUtil());
        PrismObject<RoleType> roleProxy = getPrismContext().parseObject(new File(PrismInternalTestUtil.COMMON_DIR_XML, "role-proxy.xml"));
        axiomQueryContentAssist = new AxiomQueryContentAssistImpl(getPrismContext());
        Item<?, ?> filterItem = roleProxy.findItem(ItemPath.create(new QName("authorization"), 1L, new QName("object"), 1L, new QName("filter")));
        PrismValue filterPrismValue = filterItem.getAnyValue();
        typeDefinition = filterPrismValue.getSchemaContext().getItemDefinition();
    }


    @Test
    public void testValidPathComponent() {
        String query = "givenName = \"End user\"";
        List<AxiomQueryError> errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertTrue(errorList.isEmpty());
    }

    @Test
    public void testInvalidPathComponent() {
        String query = "badPath = \"End user\"";
        List<AxiomQueryError> errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 0, 7, "Invalid item component 'badPath' definition."));
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 8, 9, "Invalid '=' filter alias."));
    }

    @Test
    public void testValidPropFilter() {
        String query = "name endsWith \"LAST\"";
        List<AxiomQueryError> errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertTrue(errorList.isEmpty());

        query = "givenName = \"John\"";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertTrue(errorList.isEmpty());

        query = "familyName startsWith \"Wo\"";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertTrue(errorList.isEmpty());
    }

    @Test
    public void testInvalidPropFilter() {
        String query = "badName endsWith \"LAST\"";
        List<AxiomQueryError> errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 0, 7, "Invalid item component 'badName' definition."));
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 8, 16, "Invalid 'endsWith' filter."));

        query = "badGivenName = \"John\"";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 0, 12, "Invalid item component 'badGivenName' definition."));
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 13, 14, "Invalid '=' filter alias."));

        query = "badFamilyName startsWith \"Wo\"";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 0, 13, "Invalid item component 'badFamilyName' definition."));
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 14, 24, "Invalid 'startsWith' filter."));
    }

    @Test()
    public void testValidSelfPath() {
        String query = ". matches (targetType = RoleType)";
        List<AxiomQueryError> errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertTrue(errorList.isEmpty());

        query = ". referencedBy (@type = UserType AND @path = assignment/targetRef)";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertTrue(errorList.isEmpty());

        query = ". ownedBy ( @type = AbstractRoleType and @path = inducement)";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertTrue(errorList.isEmpty());
    }

    @Test()
    public void testInvalidSelfPath() {
        String query = ". equal value";
        List<AxiomQueryError> errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 2, 7, "Invalid 'equal' filter for self path."));

        query = ". = value";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 2, 3, "Invalid '=' filter alias for self path."));
    }

    @Test()
    public void testValidParentPath() {
        // TODO test for parent path
    }

    @Test
    public void testValidReferenceComponent() {
        String query = "activation/validTo < \"2022-01-01\"";
        List<AxiomQueryError> errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertTrue(errorList.isEmpty());

        query = "assignment/targetRef not matches ( targetType = RoleType )";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertTrue(errorList.isEmpty());

        query = "extension/indexedString contains \"mycompanyname.com\"";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertTrue(errorList.isEmpty());
    }

    @Test
    public void testInvalidReferenceComponent() {
        String query = "activation/badAdministrativeStatus = \"disabled\"";
        List<AxiomQueryError> errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 11, 34, "Invalid item component 'badAdministrativeStatus' definition."));
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 35, 36, "Invalid '=' filter alias."));

        query = "assignment/badTargetRef = \"End user\"";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 11, 23, "Invalid item component 'badTargetRef' definition."));
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 24, 25, "Invalid '=' filter alias."));

        query = "extension/badIndexedString contains \"mycompanyname.com\"";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 10, 26, "Invalid item component 'badIndexedString' definition."));
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 27, 35, "Invalid 'contains' filter."));
    }

    @Test()
    public void testValidDereferenceComponent() {
        String query = "assignment/targetRef/@/name = \"End user\"";
        List<AxiomQueryError> errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertThat(errorList).isEmpty();

        query = "@/archetypeRef/@/name=\"Application\"";
        errorList = this.axiomQueryContentAssist.process(typeDefinition.findItemDefinition(
                ItemPath.create("assignment"), ItemDefinition.class).findItemDefinition(
                ItemPath.create("targetRef"), PrismReferenceDefinition.class), query, 0).validate();
        assertThat(errorList).isEmpty();

        ItemDefinition<?> localTypeDefinition = getPrismContext().getSchemaRegistry().findItemDefinitionByType(new QName("AssignmentHolderType"));
        query = "roleMembershipRef/@/name = \"End user\"";
        errorList = this.axiomQueryContentAssist.process(localTypeDefinition, query, 0).validate();
        assertThat(errorList).isEmpty();

        localTypeDefinition = getPrismContext().getSchemaRegistry().findItemDefinitionByType(new QName("AssignmentType"));
        query = "@/name startsWith \"gallery\"";
        errorList = this.axiomQueryContentAssist.process(localTypeDefinition.findItemDefinition(ItemPath.create(new QName("targetRef")), PrismReferenceDefinition.class), query, 0).validate();
        assertThat(errorList).isEmpty();
    }

    @Test()
    public void testInvalidDereferenceComponent() {
        String query = "assignment/targetRef/@/badProp = \"End user\"";
        List<AxiomQueryError> errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 23, 30, "Invalid item component 'badProp' definition."));
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 31, 32, "Invalid '=' filter alias."));

        query = "@/badTypeRef/@/name=\"Application\"";
        errorList = this.axiomQueryContentAssist.process(typeDefinition.findItemDefinition(
                ItemPath.create("assignment"), ItemDefinition.class).findItemDefinition(
                ItemPath.create("targetRef"), PrismReferenceDefinition.class), query, 0).validate();
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 2, 12, "Invalid item component 'badTypeRef' definition."));
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 13, 14, "Invalid dereference path because reference definition is null."));
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 15, 19, "Invalid item component 'name' definition."));
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 19, 20, "Invalid '=' filter alias."));

        ItemDefinition<?> localTypeDefinition = getPrismContext().getSchemaRegistry().findItemDefinitionByType(new QName("AssignmentHolderType"));
        query = "roleMembershipRef/@/badProp = \"End user\"";
        errorList = this.axiomQueryContentAssist.process(localTypeDefinition, query, 0).validate();
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 20, 27, "Invalid item component 'badProp' definition."));
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 28, 29, "Invalid '=' filter alias."));

        query = "badMembershipRef/@/name = \"End user\"";
        errorList = this.axiomQueryContentAssist.process(localTypeDefinition, query, 0).validate();
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 0, 16, "Invalid item component 'badMembershipRef' definition."));
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 17, 18, "Invalid dereference path because reference definition is null."));
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 19, 23, "Invalid item component 'name' definition."));
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 24, 25, "Invalid '=' filter alias."));

        localTypeDefinition = getPrismContext().getSchemaRegistry().findItemDefinitionByType(new QName("AssignmentType"));
        query = "@/badProp startsWith \"gallery\"";
        errorList = this.axiomQueryContentAssist.process(localTypeDefinition.findItemDefinition(ItemPath.create(new QName("targetRef")), PrismReferenceDefinition.class), query, 0).validate();
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 2, 9, "Invalid item component 'badProp' definition."));
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 10, 20, "Invalid 'startsWith' filter."));
    }

    @Test()
    public void testValidItemFilter() {
        // filters for prop definition
        String query = "name equal \"End user\"";
        List<AxiomQueryError> errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        query = "name less \"End user\"";
        errorList.addAll(this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate());
        query = "name greater \"End user\"";
        errorList.addAll(this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate());
        query = "name lessOrEqual \"End user\"";
        errorList.addAll(this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate());
        query = "name greaterOrEqual \"End user\"";
        errorList.addAll(this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate());
        query = "name notEqual \"End user\"";
        errorList.addAll(this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate());
        query = "name exists \"End user\"";
        errorList.addAll(this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate());
        query = "name levenshtein \"End user\"";
        errorList.addAll(this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate());
        query = "name similarity \"End user\"";
        errorList.addAll(this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate());
        query = "name ownedByOid \"End user\"";
        errorList.addAll(this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate());
        query = "name anyIn \"End user\"";
        errorList.addAll(this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate());
        query = "name startsWith \"End user\"";
        errorList.addAll(this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate());
        query = "name endsWith \"End user\"";
        errorList.addAll(this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate());
        query = "name contains \"End user\"";
        errorList.addAll(this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate());
        query = "name fullText \"End user\"";
        errorList.addAll(this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate());

        // filters for ref & container definition
        query = "assignment/targetRef matches (targetType=RoleType)";
        errorList.addAll(this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate());
        query = ". matches (targetType=RoleType)";
        errorList.addAll(this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate());
        query = """
                . referencedBy (
                    @type = AbstractRoleType
                    and @path = inducement/targetRef
                )
                """;
        errorList.addAll(this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate());
        query = ". ownedBy \"End user\"";
        errorList.addAll(this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate());
        query = ". inOrg \"End user\"";
        errorList.addAll(this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate());
        query = ". inOid \"End user\"";
        errorList.addAll(this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate());
        query = ". isRoot \"End user\"";
        errorList.addAll(this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate());
        query = ". type ShadowType";
        errorList.addAll(this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate());
        assertTrue(errorList.isEmpty());
    }

    @Test()
    public void testInvalidItemFilter() {
        // filters for prop definition
        String query = ". equal \"End user\"";
        List<AxiomQueryError> errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 2, 7, "Invalid 'equal' filter for self path."));

        query = ". less \"End user\"";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 2, 6, "Invalid 'less' filter for self path."));

        query = ". greater \"End user\"";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 2, 9, "Invalid 'greater' filter for self path."));

        query = ". lessOrEqual \"End user\"";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 2, 13, "Invalid 'lessOrEqual' filter for self path."));

        query = ". greater \"End user\"";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 2, 9, "Invalid 'greater' filter for self path."));

        query = ". lessOrEqual \"End user\"";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 2, 13, "Invalid 'lessOrEqual' filter for self path."));

        query = ". greaterOrEqual \"End user\"";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 2, 16, "Invalid 'greaterOrEqual' filter for self path."));

        query = ". notEqual \"End user\"";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 2, 10, "Invalid 'notEqual' filter for self path."));

        query = ". exists \"End user\"";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 2, 8, "Invalid 'exists' filter for self path."));

        query = ". levenshtein \"End user\"";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 2, 13, "Invalid 'levenshtein' filter for self path."));

        query = ". similarity \"End user\"";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 2, 12, "Invalid 'similarity' filter for self path."));

        query = ". ownedByOid \"End user\"";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 2, 12, "Invalid 'ownedByOid' filter for self path."));

        query = ". anyIn \"End user\"";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 2, 7, "Invalid 'anyIn' filter for self path."));

        query = ". startsWith \"End user\"";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 2, 12, "Invalid 'startsWith' filter for self path."));

        query = ". endsWith \"End user\"";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 2, 10, "Invalid 'endsWith' filter for self path."));

        query = ". contains \"End user\"";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 2, 10, "Invalid 'contains' filter for self path."));

        query = ". fullText \"End user\"";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 2, 10, "Invalid 'fullText' filter for self path."));

        // filters for ref & container definition
        query = """
                name referencedBy (
                    @type = AbstractRoleTyp
                    and @path = inducement/targetRef
                )
                """;
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 5, 17, "Invalid 'referencedBy' filter."));

        query = "name ownedBy \"End user\"";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 5, 12, "Invalid 'ownedBy' filter."));

        query = "name inOrg \"End user\"";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 5, 10, "Invalid 'inOrg' filter."));

        query = "name inOid \"End user\"";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 5, 10, "Invalid 'inOid' filter."));

        query = "name isRoot \"End user\"";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertThat(errorList).contains(new AxiomQueryError(1, 1, 5, 11, "Invalid 'isRoot' filter."));
        // FIXME type item filter only for selfPath ???
//        query = "name type ShadowType";
//        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
//        assertThat()(errorList.contains(
//                new AxiomQueryError(1, 1, 5, 9, "Invalid 'type' filter.")
//        ));
    }

    // FIXME problem to find archetypeRef in UserType definition, edit foo schema
    @Test(enabled = false)
    public void testValidInfraFilter() {
        // @path & @type & @relation
        String query = ". ownedBy ( @type = AbstractRoleType and @path = inducement)";
        List<AxiomQueryError> errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertTrue(errorList.isEmpty());

        query = """
                . referencedBy (
                    @type = UserType
                    and @path = assignment/targetRef
                    and archetypeRef/@/name = "System user"
                )
                """;
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertTrue(errorList.isEmpty());

        query = """
                . referencedBy (
                   @type = AssignmentType
                   and @path = targetRef
                   and . ownedBy (
                      @type = UserType
                      and @path = assignment
                      and archetypeRef/@/name = "System user"
                   )
                )
                """;
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertTrue(errorList.isEmpty());
    }

    @Test()
    public void testInvalidInfraFilter() {
        // @path & @type & @relation
        String query = ". ownedBy ( @type = BadAbstractRoleType and @path = inducement)";
        List<AxiomQueryError> errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertThat(errorList.contains(new AxiomQueryError(1, 1, 20, 39, "Invalid meta type 'BadAbstractRoleType'.")));
        assertThat(errorList.contains(new AxiomQueryError(1, 1, 52, 62, "Invalid meta path 'inducement'.")));

        query = """
                . referencedBy (
                    @type = UserTyp
                    and @path = assignment/badTargetRef
                    and badArchetypeRef/@/name = "System user"
                )
                """;
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertThat(errorList.contains(new AxiomQueryError(3, 3, 61, 73, "Invalid meta path 'badTargetRef'.")));
        assertThat(errorList.contains(new AxiomQueryError(4, 4, 80, 81, "Invalid dereference path because reference definition is null.")));
        assertThat(errorList.contains(new AxiomQueryError(4, 4, 82, 97, "Invalid item component 'badArchetypeRef' definition.")));
        assertThat(errorList.contains(new AxiomQueryError(4, 4, 98, 99, "Invalid dereference path because reference definition is null.")));
        assertThat(errorList.contains(new AxiomQueryError(4, 4, 100, 104, "Invalid item component 'name' definition.")));
        assertThat(errorList.contains(new AxiomQueryError(4, 4, 105, 106, "Invalid '=' filter alias.")));


        query = """
                . referencedBy (
                   @type = BadAssignmentType
                   and @path = targetRef
                   and . ownedBy (
                      @type = UserType
                      and @path = assignment
                      and archetypeRef/@/badName = "System user"
                   )
                )
                """;
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertThat(errorList.contains(new AxiomQueryError(2, 2, 28, 45, "Invalid meta type 'BadAssignmentType'.")));
        assertThat(errorList.contains(new AxiomQueryError(3, 3, 61, 70, "Invalid meta path 'targetRef'.")));
        assertThat(errorList.contains(new AxiomQueryError(4, 4, 80, 87, "Invalid 'ownedBy' filter for self path.")));
        assertThat(errorList.contains(new AxiomQueryError(7, 7, 164, 171, "Invalid item component 'badName' definition.")));
        assertThat(errorList.contains(new AxiomQueryError(7, 7, 172, 173, "Invalid '=' filter alias.")));
    }

    // FIXME solve order andFilters
    @Test(enabled = false)
    public void testValidSubFilterSpec() {
        ItemDefinition<?> localTypeDefinition = PrismContext.get().getSchemaRegistry().findItemDefinitionByType(new QName("FocusType"));
        String query = """
                linkRef/@ matches (
                    . type ShadowType
                    and resourceRef matches (
                        oid = "093ba5b5-7b15-470a-a147-889d09c2850f"
                    )
                    and intent = "default"
                )
                """;
        List<AxiomQueryError> errorList = this.axiomQueryContentAssist.process(localTypeDefinition, query, 0).validate();
        assertTrue(errorList.isEmpty());

        query = "roleMembershipRef not matches (targetType = ServiceType)";
        errorList = this.axiomQueryContentAssist.process(localTypeDefinition, query, 0).validate();
        assertTrue(errorList.isEmpty());

        query = """
                roleMembershipRef not matches (
                    targetType = RoleType
                )
                AND roleMembershipRef not matches (
                    targetType = ServiceType
                )
                """;
        errorList = this.axiomQueryContentAssist.process(localTypeDefinition, query, 0).validate();
        assertTrue(errorList.isEmpty());

        query = """
                assignment/targetRef not matches (
                    targetType = RoleType
                )
                AND assignment/targetRef not matches (
                    targetType = ServiceType
                )
                """;
        errorList = this.axiomQueryContentAssist.process(localTypeDefinition, query, 0).validate();
        assertTrue(errorList.isEmpty());

        query = "assignment/targetRef matches (targetType=RoleType and relation=owner)";
        errorList = this.axiomQueryContentAssist.process(localTypeDefinition, query, 0).validate();
        assertTrue(errorList.isEmpty());

        query = """
                . referencedBy (
                    @type = UserType
                    AND name = "adam"
                    AND @path = assignment/targetRef
                )
                """;
        errorList = this.axiomQueryContentAssist.process(localTypeDefinition, query, 0).validate();
        assertTrue(errorList.isEmpty());

        query = """
                assignment/targetRef/@ matches (
                    type RoleType
                    and extension/sapType="SAP555"
                )
                """;
        errorList = this.axiomQueryContentAssist.process(localTypeDefinition, query, 0).validate();
        assertTrue(errorList.isEmpty());
    }
}
