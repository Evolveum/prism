package com.evolveum.midpoint.prism.query.lang;

import static org.testng.AssertJUnit.assertTrue;

import static com.evolveum.midpoint.prism.PrismInternalTestUtil.DEFAULT_NAMESPACE_PREFIX;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.foo.RoleType;
import com.evolveum.midpoint.prism.foo.UserType;
import com.evolveum.midpoint.prism.path.ItemPath;

import org.testng.Assert;
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
        axiomQueryContentAssist = new AxiomQueryContentAssistImpl(PrismContext.get());
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
        assertTrue(errorList.containsAll(List.of(
                new AxiomQueryError(1, 1, 0, 7, "Invalid item component 'badPath' definition."),
                new AxiomQueryError(1, 1, 8, 9, "Invalid '=' filter alias.")))
        );
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
        assertTrue(errorList.containsAll(List.of(
                new AxiomQueryError(1, 1, 0, 7, "Invalid item component 'badName' definition."),
                new AxiomQueryError(1, 1, 8, 16, "Invalid 'endsWith' filter.")))
        );

        query = "badGivenName = \"John\"";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertTrue(errorList.containsAll(List.of(
                new AxiomQueryError(1, 1, 0, 12, "Invalid item component 'badGivenName' definition."),
                new AxiomQueryError(1, 1, 13, 14, "Invalid '=' filter alias.")))
        );

        query = "badFamilyName startsWith \"Wo\"";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertTrue(errorList.containsAll(List.of(
                new AxiomQueryError(1, 1, 0, 13, "Invalid item component 'badFamilyName' definition."),
                new AxiomQueryError(1, 1, 14, 24, "Invalid 'startsWith' filter.")))
        );
    }

    @Test(enabled = false)
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

    @Test
    public void testInvalidSelfPath() {
        String query = ". equal value";
        List<AxiomQueryError> errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertTrue(errorList.contains(
                new AxiomQueryError(1, 1, 2, 7, "Invalid 'equal' filter for self path."))
        );

        query = ". = value";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertTrue(errorList.contains(
                new AxiomQueryError(1, 1, 2, 3, "Invalid '=' filter alias for self path."))
        );
    }

    @Test(enabled = false)
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
        assertTrue(errorList.containsAll(List.of(
                new AxiomQueryError(1, 1, 11, 34, "Invalid item component 'badAdministrativeStatus' definition."),
                new AxiomQueryError(1, 1, 35, 36, "Invalid '=' filter alias.")
        )));

        query = "assignment/badTargetRef = \"End user\"";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertTrue(errorList.containsAll(List.of(
                new AxiomQueryError(1, 1, 11, 23, "Invalid item component 'badTargetRef' definition."),
                new AxiomQueryError(1, 1, 24, 25, "Invalid '=' filter alias.")
        )));

        query = "extension/badIndexedString contains \"mycompanyname.com\"";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertTrue(errorList.containsAll(List.of(
                new AxiomQueryError(1, 1, 10, 26, "Invalid item component 'badIndexedString' definition."),
                new AxiomQueryError(1, 1, 27, 35, "Invalid 'contains' filter.")
        )));
    }

    @Test(enabled = false)
    public void testValidDereferenceComponent() {
        String query = "assignment/targetRef/@/name = \"End user\"";
        List<AxiomQueryError> errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        assertTrue(errorList.isEmpty());
    }

    @Test(enabled = false)
    public void testItemFilter() {
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
//        query = ". fullText \"End user\"";
//        errorList.addAll(this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate());
        assertTrue(errorList.isEmpty());

        // filters for ref & container definition
        query = "assignment/targetRef matches \"End user\"";
        errorList.addAll(this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate());
        query = ". referencedBy (\n"
                + "  @type = AbstractRoleType\n"
                + "  and @path = inducement/targetRef\n"
                + ")";
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
        errorList.forEach(s -> System.out.println(s.getMessage() + " --- " + s.getLineStart() + " - "  + s.getLineStop() + " -- " + s.getCharPositionStart() + " - "  + s.getCharPositionStop()));
        assertTrue(errorList.isEmpty());
    }

    @Test(enabled = false)
    public void testItemFilterAlias() {
        Assert.assertTrue(true, "String message");
    }

    @Test(enabled = false)
    public void testMetaFilter() {
        // @path & @type & @relation
        String query = ". referencedBy (@type = UserType"
                + " and @path = assignment/targetRef "
                + " and @relation = a-relation"
                + " and activation/validTo < '2020-07-06T00:00:00.000+02:00')";

        List<AxiomQueryError> errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        errorList.forEach(c -> System.out.println(c.getCharPositionStop() + " : " + c.getMessage()));
    }

    @Test(enabled = false)
    public void testValidSubFilterSpec() {
        typeDefinition = PrismContext.get().getSchemaRegistry().findItemDefinitionByType(new QName("FocusType"));

        String query = "linkRef/@ matches (. type ShadowType and resourceRef matches (oid = \"093ba5b5-7b15-470a-a147-889d09c2850f\") and intent = \"default\" )";
        List<AxiomQueryError> errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        errorList.forEach(c -> System.out.println(c.getCharPositionStop() + " : " + c.getMessage()));

        query = "roleMembershipRef not matches (targetType = ServiceType)";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        errorList.forEach(c -> System.out.println(c.getCharPositionStop() + " : " + c.getMessage()));

        query = "roleMembershipRef not matches (targetType = RoleType) AND roleMembershipRef not matches (targetType = ServiceType)";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        errorList.forEach(c -> System.out.println(c.getCharPositionStop() + " : " + c.getMessage()));

        query = "assignment/targetRef not matches ( targetType = RoleType) AND assignment/targetRef not matches ( targetType = ServiceType)";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        errorList.forEach(c -> System.out.println(c.getCharPositionStop() + " : " + c.getMessage()));

        query = "assignment/targetRef matches (targetType=RoleType and relation=owner)";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        errorList.forEach(c -> System.out.println(c.getCharPositionStop() + " : " + c.getMessage()));

        query = ". referencedBy (@type = UserType AND name = \"adam\" AND @path = assignment/targetRef)";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        errorList.forEach(c -> System.out.println(c.getCharPositionStop() + " : " + c.getMessage()));

        query = "assignment/targetRef/@ matches ( type RoleType and extension/sapType=\"SAP555\")";
        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
        errorList.forEach(c -> System.out.println(c.getCharPositionStop() + " : " + c.getMessage()));
    }
}
