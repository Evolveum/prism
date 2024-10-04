package com.evolveum.midpoint.prism.query.lang;

import java.io.IOException;
import java.util.*;

import com.beust.ah.A;

import com.evolveum.midpoint.prism.foo.*;

import com.evolveum.midpoint.prism.impl.query.lang.AxiomQueryContentAssistantVisitor;
import com.evolveum.midpoint.prism.query.AxiomQueryContentAssist;
import com.evolveum.midpoint.prism.query.ContentAssist;
import com.evolveum.midpoint.prism.query.Suggestion;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.impl.query.lang.AxiomQueryContentAssistImpl;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.prism.util.PrismTestUtil;
import com.evolveum.midpoint.util.exception.SchemaException;

/**
 * Created by Dominik.
 */
public class TestQueryCompletion extends AbstractPrismTest {

    AxiomQueryContentAssist axiomQueryContentAssist;
    private SchemaRegistry schemaRegistry;
    private PrismObjectDefinition<UserType> userDef;

    @BeforeSuite
    public void setupDebug() throws SchemaException, SAXException, IOException {
        PrismTestUtil.resetPrismContext(new PrismInternalTestUtil());
        axiomQueryContentAssist = new AxiomQueryContentAssistImpl(getPrismContext());
        schemaRegistry = getPrismContext().getSchemaRegistry();
        userDef = schemaRegistry.findObjectDefinitionByType(UserType.COMPLEX_TYPE);
    }

    @Test
    public void testTesting() {
        String query = "name eq value or ";

//        query = "name equal and ";
//        query = "name ";
//        query = "name equal";
//        query = "name equal ";
//        query = "name equal value";
//        query = """
//                . referencedBy ( \s
//                   @type = A@ssignmentType\s
//                   and @path = targetRef
//                   and . ownedBy (
//                      @atype = UserType
//                      and @path = assignment
//                      and archetypeRef/@/name = "System user"
//                   )
//                )
//                """;

        List<Suggestion> suggestions = axiomQueryContentAssist.process(userDef, query, 13, null).autocomplete();
//        System.out.println("RESULT_TESTING: ");
//        suggestions.forEach(s -> System.out.println(s));
    }
}
