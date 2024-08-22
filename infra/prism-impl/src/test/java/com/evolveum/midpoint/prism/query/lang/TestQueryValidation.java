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
    public void testInvalidDereferenceComponent() {
        String query = "@/displayName startsWith \"gallery\"";
        query = "@/archetypeRef/@/name=\"Application\"";
        query = "archetypeRef/@/name = \"External Users\" and givenName = \"John\"";
        query = "assignment/targetRef/@ matches (\n"
                + ". type RoleType and extension/sapType=\"SAP555\")";
    }

    @Test(enabled = false)
    public void testInvalidPropFilter() {
        String query = "name startsWith \"gallery\"";
    }

    @Test(enabled = false)
    public void testInvalidContainerFilter() {
        String query = ". startsWith \"gallery\"";
    }

    @Test(enabled = false)
    public void testInvalidReferenceFilter() {
        String query = "targetRef startsWith \"gallery\"";
    }

    @Test(enabled = false)
    public void testItemFilter() {
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
    public void testSubFilterSpec() {
        typeDefinition = PrismContext.get().getSchemaRegistry().findItemDefinitionByType(new QName("FocusType"));

//        String query = "linkRef/@ matches (. type ShadowType and resourceRef matches (oid = \"093ba5b5-7b15-470a-a147-889d09c2850f\") and intent = \"default\" )";
//        List<AxiomQueryError> errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
//        errorList.forEach(c -> System.out.println(c.getCharPositionStop() + " : " + c.getMessage()));

//        query = "roleMembershipRef not matches (targetType = ServiceType)";
//        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
//        errorList.forEach(c -> System.out.println(c.getCharPositionStop() + " : " + c.getMessage()));

//        query = "roleMembershipRef not matches (targetType = RoleType) AND roleMembershipRef not matches (targetType = ServiceType)";
//        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
//        errorList.forEach(c -> System.out.println(c.getCharPositionStop() + " : " + c.getMessage()));
//
//        query = "assignment/targetRef not matches ( targetType = RoleType) AND assignment/targetRef not matches ( targetType = ServiceType)";
//        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
//        errorList.forEach(c -> System.out.println(c.getCharPositionStop() + " : " + c.getMessage()));
//
//        query = "assignment/targetRef matches (targetType=RoleType and relation=owner)";
//        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
//        errorList.forEach(c -> System.out.println(c.getCharPositionStop() + " : " + c.getMessage()));
//
//        query = ". referencedBy (@type = UserType AND name = \"adam\" AND @path = assignment/targetRef)";
//        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
//        errorList.forEach(c -> System.out.println(c.getCharPositionStop() + " : " + c.getMessage()));
//
//        query = "assignment/targetRef/@ matches ( type RoleType and extension/sapType=\"SAP555\")";
//        errorList = this.axiomQueryContentAssist.process(typeDefinition, query, 0).validate();
//        errorList.forEach(c -> System.out.println(c.getCharPositionStop() + " : " + c.getMessage()));
    }
}
