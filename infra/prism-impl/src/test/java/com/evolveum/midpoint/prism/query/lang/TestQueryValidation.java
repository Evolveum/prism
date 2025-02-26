package com.evolveum.midpoint.prism.query.lang;

import static org.assertj.core.api.Assertions.*;
import static com.evolveum.midpoint.prism.PrismInternalTestUtil.DEFAULT_NAMESPACE_PREFIX;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.foo.RoleType;
import com.evolveum.midpoint.prism.path.ItemPath;

import com.evolveum.midpoint.util.SingleLocalizableMessage;

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
    ItemDefinition<?> userDefinition;

    @BeforeSuite
    public void setupDebug() throws SchemaException, SAXException, IOException {
        PrettyPrinter.addDefaultNamespacePrefix(DEFAULT_NAMESPACE_PREFIX);
        PrismTestUtil.resetPrismContext(new PrismInternalTestUtil());
        PrismObject<RoleType> roleProxy = getPrismContext().parseObject(new File(PrismInternalTestUtil.COMMON_DIR_XML, "role-proxy.xml"));
        axiomQueryContentAssist = new AxiomQueryContentAssistImpl(getPrismContext());
        Item<?, ?> filterItem = roleProxy.findItem(ItemPath.create(new QName("authorization"), 1L, new QName("object"), 1L, new QName("filter")));
        PrismValue filterPrismValue = filterItem.getAnyValue();
        userDefinition = filterPrismValue.getSchemaContext().getItemDefinition();
    }


    @Test
    public void testValidPathComponent() {
        String query = "givenName = \"End user\"";
        List<AxiomQueryError> errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).isEmpty();
    }

    @Test
    public void testInvalidPathComponent() {
        String query = "badPath = \"End user\"";
        List<AxiomQueryError> errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidItemComponent", new String[] { "badPath" }));
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidFilterAlias", new String[] { "=" }));
    }

    @Test
    public void testValidPropFilter() {
        String query = "name endsWith \"LAST\"";
        List<AxiomQueryError> errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).isEmpty();

        query = "givenName = \"John\"";
        errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).isEmpty();

        query = "familyName startsWith \"Wo\"";
        errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).isEmpty();
    }

    @Test
    public void testInvalidPropFilter() {
        String query = "badName endsWith \"LAST\"";
        List<AxiomQueryError> errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidItemComponent", new String[] { "badName" }));
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidFilter", new String[] { "endsWith" }));

        query = "badGivenName = \"John\"";
        errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidItemComponent", new String[] { "badGivenName" }));
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidFilterAlias", new String[] { "=" }));

        query = "badFamilyName startsWith \"Wo\"";
        errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidItemComponent", new String[] { "badFamilyName" }));
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidFilter", new String[] { "startsWith" }));
    }

    @Test()
    public void testValidSelfPath() {
        String query = ". matches (targetType = RoleType)";
        List<AxiomQueryError> errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).isEmpty();

        query = ". referencedBy (@type = UserType AND @path = assignment/targetRef)";
        errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).isEmpty();

        query = ". ownedBy ( @type = AbstractRoleType and @path = inducement)";
        errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).isEmpty();
    }

    @Test()
    public void testInvalidSelfPath() {
        String query = ". equal value";
        List<AxiomQueryError> errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidFilterSelfPath", new String[] { "equal" }));
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidItemComponent", new String[] { "value" }));

        query = ". = value";
        errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidFilterAliasSelfPath", new String[] { "=" }));
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidItemComponent", new String[] { "value" }));
    }

    @Test()
    public void testValidParentPath() {
        // TODO test for parent path
    }

    @Test
    public void testValidReferenceComponent() {
        String query = "activation/validTo < \"2022-01-01\"";
        List<AxiomQueryError> errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).isEmpty();

        query = "assignment/targetRef not matches ( targetType = RoleType )";
        errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).isEmpty();

        query = "extension/indexedString contains \"mycompanyname.com\"";
        errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).isEmpty();
    }

    @Test
    public void testInvalidReferenceComponent() {
        String query = "activation/badAdministrativeStatus = \"disabled\"";
        List<AxiomQueryError> errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidItemComponent", new String[] { "badAdministrativeStatus" }));
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidFilterAlias", new String[] { "=" }));

        query = "assignment/badTargetRef = \"End user\"";
        errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidItemComponent", new String[] { "badTargetRef" }));
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidFilterAlias", new String[] { "=" }));

        query = "extension/badIndexedString contains \"mycompanyname.com\"";
        errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidItemComponent", new String[] { "badIndexedString" }));
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidFilter", new String[] { "contains" }));
    }

    @Test()
    public void testValidDereferenceComponent() {
        String query = "assignment/targetRef/@/name = \"End user\"";
        List<AxiomQueryError> errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).isEmpty();

        query = "@/archetypeRef/@/name=\"Application\"";
        errorList = this.axiomQueryContentAssist.process(userDefinition.findItemDefinition(
                ItemPath.create("assignment"), ItemDefinition.class).findItemDefinition(
                ItemPath.create("targetRef"), PrismReferenceDefinition.class), query).validate();
        assertThat(errorList).map(AxiomQueryError::message).isEmpty();

        ItemDefinition<?> localTypeDefinition = getPrismContext().getSchemaRegistry().findItemDefinitionByType(new QName("AssignmentHolderType"));
        query = "roleMembershipRef/@/name = \"End user\"";
        errorList = this.axiomQueryContentAssist.process(localTypeDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).isEmpty();

        localTypeDefinition = getPrismContext().getSchemaRegistry().findItemDefinitionByType(new QName("AssignmentType"));
        query = "@/name startsWith \"gallery\"";
        errorList = this.axiomQueryContentAssist.process(localTypeDefinition.findItemDefinition(ItemPath.create(new QName("targetRef")), PrismReferenceDefinition.class), query).validate();
        assertThat(errorList).map(AxiomQueryError::message).isEmpty();
    }

    @Test()
    public void testInvalidDereferenceComponent() {
        String query = "assignment/targetRef/@/badProp = \"End user\"";
        List<AxiomQueryError> errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidItemComponent", new String[] { "badProp" }));
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidFilterAlias", new String[] { "=" }));

        query = "@/badTypeRef/@/name=\"Application\"";
        errorList = this.axiomQueryContentAssist.process(userDefinition.findItemDefinition(
                ItemPath.create("assignment"), ItemDefinition.class).findItemDefinition(
                ItemPath.create("targetRef"), PrismReferenceDefinition.class), query).validate();
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidItemComponent", new String[] { "badTypeRef" }));
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidDereferencePathNull", new String[] {}));
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidItemComponent", new String[] { "name" }));
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidFilterAlias", new String[] { "=" }));

        ItemDefinition<?> localTypeDefinition = getPrismContext().getSchemaRegistry().findItemDefinitionByType(new QName("AssignmentHolderType"));
        query = "roleMembershipRef/@/badProp = \"End user\"";
        errorList = this.axiomQueryContentAssist.process(localTypeDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidItemComponent", new String[] { "badProp" }));
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidFilterAlias", new String[] { "=" }));

        query = "badMembershipRef/@/name = \"End user\"";
        errorList = this.axiomQueryContentAssist.process(localTypeDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidItemComponent", new String[] { "badMembershipRef" }));
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidDereferencePathNull", new String[] {}));
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidItemComponent", new String[] { "name" }));
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidFilterAlias", new String[] { "=" }));

        localTypeDefinition = getPrismContext().getSchemaRegistry().findItemDefinitionByType(new QName("AssignmentType"));
        query = "@/badProp startsWith \"gallery\"";
        errorList = this.axiomQueryContentAssist.process(localTypeDefinition.findItemDefinition(ItemPath.create(new QName("targetRef")), PrismReferenceDefinition.class), query).validate();
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidItemComponent", new String[] { "badProp" }));
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidFilter", new String[] { "startsWith" }));
    }

    @Test()
    public void testValidItemFilter() {
        // filters for prop definition
        String query = "name equal \"End user\"";
        List<AxiomQueryError> errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        query = "name less \"End user\"";
        errorList.addAll(this.axiomQueryContentAssist.process(userDefinition, query).validate());
        query = "name greater \"End user\"";
        errorList.addAll(this.axiomQueryContentAssist.process(userDefinition, query).validate());
        query = "name lessOrEqual \"End user\"";
        errorList.addAll(this.axiomQueryContentAssist.process(userDefinition, query).validate());
        query = "name greaterOrEqual \"End user\"";
        errorList.addAll(this.axiomQueryContentAssist.process(userDefinition, query).validate());
        query = "name notEqual \"End user\"";
        errorList.addAll(this.axiomQueryContentAssist.process(userDefinition, query).validate());
        query = "name exists \"End user\"";
        errorList.addAll(this.axiomQueryContentAssist.process(userDefinition, query).validate());
        query = "name levenshtein \"End user\"";
        errorList.addAll(this.axiomQueryContentAssist.process(userDefinition, query).validate());
        query = "name similarity \"End user\"";
        errorList.addAll(this.axiomQueryContentAssist.process(userDefinition, query).validate());
        query = "name anyIn \"End user\"";
        errorList.addAll(this.axiomQueryContentAssist.process(userDefinition, query).validate());
        query = "name startsWith \"End user\"";
        errorList.addAll(this.axiomQueryContentAssist.process(userDefinition, query).validate());
        query = "name endsWith \"End user\"";
        errorList.addAll(this.axiomQueryContentAssist.process(userDefinition, query).validate());
        query = "name contains \"End user\"";
        errorList.addAll(this.axiomQueryContentAssist.process(userDefinition, query).validate());
        query = "name fullText \"End user\"";
        errorList.addAll(this.axiomQueryContentAssist.process(userDefinition, query).validate());

        // filters for ref & container definition
        query = "assignment/targetRef matches (targetType=RoleType)";
        errorList.addAll(this.axiomQueryContentAssist.process(userDefinition, query).validate());
        query = ". matches (targetType=RoleType)";
        errorList.addAll(this.axiomQueryContentAssist.process(userDefinition, query).validate());
        query = """
                . referencedBy (
                    @type = AbstractRoleType
                    and @path = inducement/targetRef
                )
                """;
        errorList.addAll(this.axiomQueryContentAssist.process(userDefinition, query).validate());
        query = ". ownedBy \"End user\"";
        errorList.addAll(this.axiomQueryContentAssist.process(userDefinition, query).validate());
        query = ". inOrg \"End user\"";
        errorList.addAll(this.axiomQueryContentAssist.process(userDefinition, query).validate());
        query = ". inOid \"End user\"";
        errorList.addAll(this.axiomQueryContentAssist.process(userDefinition, query).validate());
        query = ". isRoot \"End user\"";
        errorList.addAll(this.axiomQueryContentAssist.process(userDefinition, query).validate());
        query = ". type ShadowType";
        errorList.addAll(this.axiomQueryContentAssist.process(userDefinition, query).validate());
        assertThat(errorList).map(AxiomQueryError::message).isEmpty();
    }

    @Test()
    public void testInvalidItemFilter() {
        // filters for prop definition
        String query = ". equal \"End user\"";
        List<AxiomQueryError> errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidFilterSelfPath", new String[] { "equal" }));

        query = ". less \"End user\"";
        errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidFilterSelfPath", new String[] { "less" }));

        query = ". greater \"End user\"";
        errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidFilterSelfPath", new String[] { "greater" }));

        query = ". lessOrEqual \"End user\"";
        errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidFilterSelfPath", new String[] { "lessOrEqual" }));

        query = ". greater \"End user\"";
        errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidFilterSelfPath", new String[] { "greater" }));

        query = ". lessOrEqual \"End user\"";
        errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidFilterSelfPath", new String[] { "lessOrEqual" }));

        query = ". greaterOrEqual \"End user\"";
        errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidFilterSelfPath", new String[] { "greaterOrEqual" }));

        query = ". notEqual \"End user\"";
        errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidFilterSelfPath", new String[] { "notEqual" }));

        query = ". exists \"End user\"";
        errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidFilterSelfPath", new String[] { "exists" }));

        query = ". levenshtein \"End user\"";
        errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidFilterSelfPath", new String[] { "levenshtein" }));

        query = ". similarity \"End user\"";
        errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidFilterSelfPath", new String[] { "similarity" }));

        query = ". anyIn \"End user\"";
        errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidFilterSelfPath", new String[] { "anyIn" }));

        query = ". startsWith \"End user\"";
        errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidFilterSelfPath", new String[] { "startsWith" }));

        query = ". endsWith \"End user\"";
        errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidFilterSelfPath", new String[] { "endsWith" }));

        query = ". contains \"End user\"";
        errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidFilterSelfPath", new String[] { "contains" }));

        query = ". fullText \"End user\"";
        errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidFilterSelfPath", new String[] { "fullText" }));

        // filters for ref & container definition
        query = """
                name referencedBy (
                    @type = AbstractRoleTyp
                    and @path = inducement/targetRef
                )
                """;
        errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidFilter", new String[] { "referencedBy" }));
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidInfraType", new String[] { "AbstractRoleTyp" }));
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidInfraPath", new String[] { "inducement" }));
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidInfraPath", new String[] { "targetRef" }));

        query = "name ownedBy \"End user\"";
        errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidFilter", new String[] { "ownedBy" }));

        query = "name inOrg \"End user\"";
        errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidFilter", new String[] { "inOrg" }));

        query = "name inOid \"End user\"";
        errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidFilter", new String[] { "inOid" }));

        query = "name isRoot \"End user\"";
        errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidFilter", new String[] { "isRoot" }));
    }

    // FIXME problem to find archetypeRef in UserType definition, edit foo schema
    @Test(enabled = false)
    public void testValidInfraFilter() {
        // @path & @type & @relation
        String query = ". ownedBy ( @type = AbstractRoleType and @path = inducement)";
        List<AxiomQueryError> errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).isEmpty();

        query = """
                . referencedBy (
                    @type = UserType
                    and @path = assignment/targetRef
                    and archetypeRef/@/name = "System user"
                )
                """;
        errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).isEmpty();

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
        errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).isEmpty();
    }

    @Test()
    public void testInvalidInfraFilter() {
        // @path & @type & @relation
        String query = ". ownedBy ( @type = BadAbstractRoleType and @path = inducement)";
        List<AxiomQueryError> errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidInfraType", new String[] { "BadAbstractRoleType" }));
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidInfraPath", new String[] { "inducement" }));

        query = """
                . referencedBy (
                    @type = UserTyp
                    and @path = assignment/badTargetRef
                    and badArchetypeRef/@/name = "System user"
                )
                """;
        errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidInfraType", new String[] { "UserTyp" }));
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidInfraPath", new String[] { "assignment" }));
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidInfraPath", new String[] { "badTargetRef" }));
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidItemComponent", new String[] { "badArchetypeRef" }));
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidDereferencePathNull", new String[] {}));
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidItemComponent", new String[] { "name" }));
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidFilterAlias", new String[] { "=" }));

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
        errorList = this.axiomQueryContentAssist.process(userDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidInfraType", new String[] { "BadAssignmentType" }));
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidInfraPath", new String[] { "targetRef" }));
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidFilterSelfPath", new String[] { "ownedBy" }));
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidItemComponent", new String[] { "archetypeRef" }));
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidDereferencePathNull", new String[] {}));
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidItemComponent", new String[] { "badName" }));
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidFilterAlias", new String[] { "=" }));
    }

    @Test
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
        List<AxiomQueryError> errorList = this.axiomQueryContentAssist.process(localTypeDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).isEmpty();

        query = "roleMembershipRef not matches (targetType = ShadowType)";
        errorList = this.axiomQueryContentAssist.process(localTypeDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).isEmpty();


        query = """
                roleMembershipRef not matches (
                    targetType = RoleType
                )
                AND roleMembershipRef not matches (
                    targetType = ShadowType
                )
                """;
        errorList = this.axiomQueryContentAssist.process(localTypeDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).isEmpty();

        query = """
                assignment/targetRef not matches (
                    targetType = RoleType
                )
                AND assignment/targetRef not matches (
                    targetType = ShadowType
                )
                """;
        errorList = this.axiomQueryContentAssist.process(localTypeDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).isEmpty();

        query = "assignment/targetRef matches (targetType=RoleType and relation=owner)";
        errorList = this.axiomQueryContentAssist.process(localTypeDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).isEmpty();

        query = """
                . referencedBy (
                    @type = UserType
                    AND name = "adam"
                    AND @path = assignment/targetRef
                )
                """;
        errorList = this.axiomQueryContentAssist.process(localTypeDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).isEmpty();

        query = """
                assignment/targetRef/@ matches (
                    . type RoleType
                    and identifier = "SAP555"
                )
                """;
        errorList = this.axiomQueryContentAssist.process(localTypeDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).isEmpty();
    }

    @Test
    public void testInvalidFilter() {
        ItemDefinition<?> localTypeDefinition = PrismContext.get().getSchemaRegistry().findItemDefinitionByType(new QName("FocusType"));
        var query = """
                name = "Tony" and assignment matches ( targetRef/@/name = "Foo" )
                """;
        var errorList = this.axiomQueryContentAssist.process(localTypeDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).isEmpty();
    }

    @Test
    public void testInfraMetadata() {
        ItemDefinition<?> localTypeDefinition = PrismContext.get().getSchemaRegistry().getValueMetadataDefinition();
        var query = """
                @metadata/storage/createTimestamp < "2024-12-31"
                """;
        var errorList = this.axiomQueryContentAssist.process(localTypeDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).isEmpty();

        query = """
                @metadata/badStorage/createTimestamp < "2024-12-31""
                """;
        errorList = this.axiomQueryContentAssist.process(localTypeDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidItemComponent", new String[] { "badStorage" }));
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidItemComponent", new String[] { "createTimestamp" }));
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidFilterAlias", new String[] { "<" }));

        query = """
                @metadata/storage/badCreateTimestamp < "2024-12-31""
                """;
        errorList = this.axiomQueryContentAssist.process(localTypeDefinition, query).validate();
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidItemComponent", new String[] { "badCreateTimestamp" }));
        assertThat(errorList).map(AxiomQueryError::message).contains(new SingleLocalizableMessage("QueryLanguage.contentAssist.validation.invalidFilterAlias", new String[] { "<" }));
    }
}
