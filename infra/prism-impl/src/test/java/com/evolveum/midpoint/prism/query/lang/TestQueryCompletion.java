package com.evolveum.midpoint.prism.query.lang;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.evolveum.axiom.lang.antlr.AxiomQueryError;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser;
import com.evolveum.midpoint.prism.foo.*;
import com.evolveum.midpoint.prism.impl.query.lang.FilterProvider;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.path.ItemPath;
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

import javax.xml.namespace.QName;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by Dominik.
 */
public class TestQueryCompletion extends AbstractPrismTest {

    AxiomQueryContentAssist axiomQueryContentAssist;
    private PrismObjectDefinition<UserType> userDef;

    @BeforeSuite
    public void setupDebug() throws SchemaException, SAXException, IOException {
        PrismTestUtil.resetPrismContext(new PrismInternalTestUtil());
        axiomQueryContentAssist = new AxiomQueryContentAssistImpl(getPrismContext());
        SchemaRegistry schemaRegistry = getPrismContext().getSchemaRegistry();
        userDef = schemaRegistry.findObjectDefinitionByType(UserType.COMPLEX_TYPE);
    }

    private List<Suggestion> getSuggestion(String query) {
        int position = Math.max(0, query.indexOf('^'));
        // remove caret from query string
        query = query.substring(0, position);

        ContentAssist contentAssist = axiomQueryContentAssist.process(userDef, query, position);
        return contentAssist.autocomplete();
    }

    @Test(enabled = false)
    public void testRootCtx() {
        List<Suggestion> suggestion = getSuggestion("^");

        assertThat(suggestion).map(Suggestion::name).containsAll(List.of(".", "@", "not"));
        userDef.getItemNames().stream().map(ItemName::first).filter(Objects::nonNull).forEach(itemName -> {
            assertThat(suggestion).map(Suggestion::name).contains(itemName.toString());
        });
    }

    @Test(enabled = false)
    public void testItemPath() {
        List<Suggestion> suggestion = getSuggestion("name^");
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("<", "<=", ">", ">=", "=", "!="));
        userDef.getItemNames().stream().map(ItemName::first).filter(Objects::nonNull).forEach(itemName -> {
            assertThat(suggestion).map(Suggestion::name).contains(itemName.toString());
        });

        List<Suggestion> suggestion1 = getSuggestion("name ^");
        assertThat(suggestion1).map(Suggestion::name).containsAll(List.of("<", "<=", ">", ">=", "=", "!=", "not"));
        FilterProvider.findFilterByItemDefinition(userDef.findItemDefinition(ItemPath.create(new QName("name"))), 15).forEach(
                (name, alias) -> {
                    assertThat(suggestion1).map(Suggestion::name).contains(name);
                }
        );
    }

    @Test(enabled = false)
    public void testSelfPath() {
        List<Suggestion> suggestion = getSuggestion(". ^");
        FilterProvider.findFilterByItemDefinition(userDef, 15).forEach(
                (name, alias) -> {
                    assertThat(suggestion).map(Suggestion::name).contains(name);
                }
        );
    }

    @Test(enabled = false)
    public void testItemFilter() {
        List<Suggestion> suggestion = getSuggestion("""
                . referencedBy ^
                """);

        suggestion = getSuggestion("""
                . referencedBy (
                   @type = AssignmentType ^
                """);

        suggestion = getSuggestion("""
                . referencedBy (
                   @type = AssignmentType
                   and @path = targetRef
                   and . ^ownedBy (
                      @type = UserType
                      and @path = assignment
                      and archetypeRef/@/name = "System user"
                   )
                )
                """);

        suggestion = getSuggestion("""
                . referencedBy (
                   @type = AssignmentType
                   and @path = targetRef
                   and . ownedBy (
                      ^@type = UserType
                      and @path = assignment
                      and archetypeRef/@/name = "System user"
                   )
                )
                """);
    }

    @Test(enabled = false)
    public void testLogicalFilter() {
        List<Suggestion> suggestion = getSuggestion("name equal value ^");
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("and", "or"));

        suggestion = getSuggestion("name= value ^");
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("and", "or"));

        suggestion = getSuggestion("name =value ^");
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("and", "or"));

        suggestion = getSuggestion("name =value and^");
        suggestion = getSuggestion("name =value and ^");

        suggestion = getSuggestion("name =value or^");
        suggestion = getSuggestion("name =value or ^");
    }

    @Test(enabled = false)
    public void testSubFilter() {
        List<Suggestion> suggestion = getSuggestion("""
                . referencedBy ^(
                   @type = AssignmentType
                   and @path = targetRef
                   and . ownedBy (
                      @type = UserType
                      and @path = assignment
                      and archetypeRef/@/name = "System user"
                   )
                )
                """);

        suggestion = getSuggestion("""
                . referencedBy (^
                   @type = AssignmentType
                   and @path = targetRef
                   and . ownedBy (
                      @type = UserType
                      and @path = assignment
                      and archetypeRef/@/name = "System user"
                   )
                )
                """);

        suggestion = getSuggestion("""
                . referencedBy ( ^
                   @type = AssignmentType
                   and @path = targetRef
                   and . ownedBy (
                      @type = UserType
                      and @path = assignment
                      and archetypeRef/@/name = "System user"
                   )
                )
                """);

        suggestion = getSuggestion("""
                . referencedBy (
                   @type = AssignmentType
                   and @path = targetRef
                   and . ownedBy ^(
                      @type = UserType
                      and @path = assignment
                      and archetypeRef/@/name = "System user"
                   )
                )
                """);

        suggestion = getSuggestion("""
                . referencedBy (
                   @type = AssignmentType
                   and @path = targetRef
                   and . ownedBy (^
                      @type = UserType
                      and @path = assignment
                      and archetypeRef/@/name = "System user"
                   )
                )
                """);
    }


    @Test(enabled = false)
    public void testMatchesFilter() {
        // TODO unit test for matches filters
    }

    @Test(enabled = false)
    public void testReferenceAndDereferenceFilter() {
        List<Suggestion> suggestion = getSuggestion("""
                assignment^/targetRef/@/name = "End user"
                """);

        suggestion = getSuggestion("""
                assignment/^targetRef/@/name = "End user"
                """);

        suggestion = getSuggestion("""
                assignment/targetRef^/@/name = "End user"
                """);

        suggestion = getSuggestion("""
                assignment/targetRef/^@/name = "End user"
                """);

        suggestion = getSuggestion("""
                assignment/targetRef/@^/name = "End user"
                """);

        suggestion = getSuggestion("""
                assignment/targetRef/@/^name = "End user"
                """);

        suggestion = getSuggestion("""
                linkRef^/@ matches (
                . type ShadowType
                and resourceRef matches (oid = "093ba5b5-7b15-470a-a147-889d09c2850f")
                and intent = "default" )
                """);

        suggestion = getSuggestion("""
                linkRef/^@ matches (
                . type ShadowType
                and resourceRef matches (oid = "093ba5b5-7b15-470a-a147-889d09c2850f")
                and intent = "default" )
                """);
    }
}
