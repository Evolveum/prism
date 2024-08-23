package com.evolveum.midpoint.prism.query.lang;

import java.io.IOException;
import java.util.*;

import com.evolveum.midpoint.prism.foo.*;

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

    private AxiomQueryContentAssistImpl axiomQueryLangServiceImpl;
    private SchemaRegistry schemaRegistry;
    private PrismObjectDefinition<UserType> userDef;

    @BeforeSuite
    public void setupDebug() throws SchemaException, SAXException, IOException {
        PrismTestUtil.resetPrismContext(new PrismInternalTestUtil());
        axiomQueryLangServiceImpl = new AxiomQueryContentAssistImpl(PrismContext.get());
        schemaRegistry = getPrismContext().getSchemaRegistry();
        userDef = schemaRegistry.findObjectDefinitionByType(UserType.COMPLEX_TYPE);
    }

    @Test
    public void testTesting() {
        String query = "name equal and";
        List<Suggestion> suggestions = axiomQueryLangServiceImpl.process(userDef, query, 3).autocomplete();

        System.out.println("RESULT_TESTING: " );
        suggestions.forEach(s -> System.out.println(s));
    }

}
