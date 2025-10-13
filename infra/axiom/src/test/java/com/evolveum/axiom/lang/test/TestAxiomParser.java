/*
 * Copyright (c) 2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.axiom.lang.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Optional;

import org.testng.annotations.Test;

import com.evolveum.axiom.api.schema.AxiomItemDefinition;
import com.evolveum.axiom.api.schema.AxiomSchemaContext;
import com.evolveum.axiom.api.schema.AxiomTypeDefinition;
import com.evolveum.axiom.lang.api.AxiomBuiltIn.Item;
import com.evolveum.axiom.lang.api.AxiomBuiltIn.Type;
import com.evolveum.axiom.lang.impl.ModelReactorContext;
import com.evolveum.axiom.lang.spi.AxiomSyntaxException;

public class TestAxiomParser extends AbstractReactorTest {

    private static final String BASE_EXAMPLE = "base-example.axiom";
    private static final String COMMON_CORE = "common-core.axiom";
    private static final String SCRIPTING = "scripting.axiom";

    @Test
    public void axiomSelfDescribingTest() throws IOException, AxiomSyntaxException {
        ModelReactorContext bootstrapContext = ModelReactorContext.boostrapReactor();
        AxiomSchemaContext modelContext = bootstrapContext.computeSchemaContext();
        assertTypedefBasetype(modelContext.getType(Type.TYPE_DEFINITION.name()));

        AxiomItemDefinition modelDef = modelContext.getRoot(Item.MODEL_DEFINITION.name()).get();
        assertEquals(modelDef.name(), Item.MODEL_DEFINITION.name());

        // Default reactor has Axiom model already loaded
        ModelReactorContext folowupContext = ModelReactorContext.reactor(modelContext);
        //folowupContext.loadModelFromSource(statementSource);
        AxiomSchemaContext selfparsedContext = folowupContext.computeSchemaContext();
        assertNotNull(selfparsedContext.getRoot(Item.MODEL_DEFINITION.name()));
        assertTrue(selfparsedContext.getType(Type.IDENTIFIER_DEFINITION.name()).get().itemDefinition(Item.ID_MEMBER.name()).get().required());
    }


    private void assertTypedefBasetype(Optional<AxiomTypeDefinition> optional) {
        AxiomTypeDefinition typeDef = optional.get();
        assertNotNull(typeDef);
        assertEquals(typeDef.superType().get().name(), Type.BASE_DEFINITION.name());
    }

    private void assertInstanceOf(Class<?> clz, Object value) {
        assertTrue(clz.isInstance(value));
    }

}
