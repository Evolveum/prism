package com.evolveum.midpoint.prism.query.lang;

import java.io.IOException;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser;
import com.evolveum.midpoint.prism.foo.*;
import com.evolveum.midpoint.prism.impl.query.lang.Filter;
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

/**
 * Created by Dominik.
 */
public class TestQueryCompletion extends AbstractPrismTest {

    AxiomQueryContentAssist axiomQueryContentAssist;
    private PrismObjectDefinition<UserType> userDef;

    List<String> expected = new ArrayList<>();

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
        query = query.replaceAll("\\^", "");
        ContentAssist contentAssist = axiomQueryContentAssist.process(userDef, query, position);
        return contentAssist.autocomplete();
    }

    private void assertSuggestionsMatch(List<Suggestion> suggestions, List<String> expected) {
        expected.forEach(e -> assertThat(suggestions).map(Suggestion::name).contains(e));
    }

    @Test
    public void testRootCtx() {
        expected = new ArrayList<>(userDef.getItemNames().stream().map(ItemName::getLocalPart).filter(Objects::nonNull).toList());
        expected.addAll(List.of(".", "@", "not", "("));

        assertThat(getSuggestion("^")).map(Suggestion::name).containsAll(expected);
        assertThat(getSuggestion("  ^")).map(Suggestion::name).containsAll(expected);
        assertThat(getSuggestion("^  ")).map(Suggestion::name).containsAll(expected);
        assertThat(getSuggestion("  ^  ")).map(Suggestion::name).containsAll(expected);
    }

    @Test
    public void testItemPath() {
        expected = new ArrayList<>(userDef.getItemNames().stream().map(ItemName::getLocalPart).filter(Objects::nonNull).toList());
        expected.addAll(Arrays.stream(Filter.Alias.values()).map(Filter.Alias::getName).toList());
        assertThat(getSuggestion("name^")).map(Suggestion::name).containsAll(expected);
        assertThat(getSuggestion("name^ equal value")).map(Suggestion::name).containsAll(expected);

        expected = new ArrayList<>(FilterProvider.findFilterByItemDefinition(userDef.findItemDefinition(ItemPath.create(new QName("name"))), 15).keySet().stream().toList());
        expected.add("not");
        assertThat(getSuggestion("name ^")).map(Suggestion::name).containsAll(expected);
        assertThat(getSuggestion("name ^equal 'value'")).map(Suggestion::name).containsAll(expected);
    }

    @Test
    public void testReferenceAndDereferencePath() {
        PrismContainerDefinition<?> containerDefinition = userDef.findItemDefinition(ItemPath.create(new QName("assignment")), PrismContainerDefinition.class);
        expected = new ArrayList<>(containerDefinition.getItemNames().stream().map(ItemName::getLocalPart).filter(Objects::nonNull).toList());
        expected.addAll(List.of("@", "#", ":"));

        assertSuggestionsMatch(getSuggestion("""
                targetRef^
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                targetRef^/@/name = "End user"
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                assignment/^targetRef/@/name = "End user"
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                assignment/^
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                assignment/targetRef^/@/name = "End user"
                """), List.of("/"));

        assertSuggestionsMatch(getSuggestion("""
                assignment/targetRef/^@/name = "End user"
                """), List.of("@", "#", ":"));

        PrismContainerDefinition<?> def = userDef.findItemDefinition(ItemPath.create(new QName("assignment")));
        PrismReferenceDefinition ref = def.findReferenceDefinition(ItemPath.create(new QName("targetRef")));
        PrismObjectDefinition<?> objDef = PrismContext.get().getSchemaRegistry().findObjectDefinitionByType(ref.getTargetTypeName());
        assertSuggestionsMatch(getSuggestion("""
                assignment/targetRef/@^ eq 'value'
                """), objDef.getItemNames().stream().map(ItemName::getLocalPart).filter(Objects::nonNull).toList());

        assertSuggestionsMatch(getSuggestion("""
                assignment/targetRef/@ ^'
                """), FilterProvider.findFilterByItemDefinition(ref, 15).keySet().stream().toList());

        assertSuggestionsMatch(getSuggestion("""
                assignment/targetRef/@/ ^'
                """), objDef.getItemNames().stream().map(ItemName::getLocalPart).filter(Objects::nonNull).toList());

        assertSuggestionsMatch(getSuggestion("assignment/targetRef/@^"), List.of("/"));
    }

    @Test
    public void testSelfPath() {
        assertThat(getSuggestion(".^")).isEmpty();

        expected = new ArrayList<>(FilterProvider.findFilterByItemDefinition(userDef, 15).keySet().stream().toList());
        expected.add("not");
        assertThat(getSuggestion(". ^")).map(Suggestion::name).containsAll(expected);

        // unexpected
        List<String> suggestion = getSuggestion(". ^").stream().map(Suggestion::name).toList();
        for (Filter.Alias value : Filter.Alias.values()) {
            assertThat(suggestion).doesNotContain(value.getName());
        }
    }

    @Test
    public void testFilterAlias() {
        expected = Arrays.stream(Filter.Alias.values()).map(Filter.Alias::getName).toList();

        assertSuggestionsMatch(getSuggestion("""
                    givenName^ =
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                    givenName^
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                    givenName ^
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                    givenName not^
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                    givenName not ^
                """), expected);
    }

    @Test
    public void testFilterName() {
        expected = null;

        assertSuggestionsMatch(getSuggestion("""
                    givenName^
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                    givenName^ equal
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                    givenName^=
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                    givenName^ =[origIgnoreCase] "Adam"
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                    emailAddress ^endsWith[stringIgnoreCase] "@test.com"
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                    roleMembershipRef ^
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                    roleMembershipRef^
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                    roleMembershipRef ^matches (
                        oid = "bc3f7659-e8d8-4f56-a647-2a352eead720"
                        and relation = manager
                        and targetType = OrgType
                    )
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                    linkRef/@ ^
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                    linkRef/@^
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                    linkRef/@ ^matches (
                        . type ShadowType
                        and resourceRef/@/name = "LDAP"
                        and intent = "default"
                    )
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                    roleMembershipRef not ^
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                    roleMembershipRef not^
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                    roleMembershipRef not ^matches (targetType = ServiceType)
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                    . ^
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                    . referencedBy ^
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                    . referencedBy^
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                    . referencedBy ^(
                        @type = UserType AND
                        name = "adam" AND
                        @path = assignment/targetRef
                    )
                """), expected);


        assertSuggestionsMatch(getSuggestion("""
                . ownedBy ^
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                . ownedBy^
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                . ownedBy ^( @type = AbstractRoleType and @path = inducement)
                """), expected);
    }

    @Test
    public void testMatchingFilter() {
        expected = null;

        assertSuggestionsMatch(getSuggestion("""
                    locality =[origIgnoreCase] "Edinburgh"
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                    givenName =[origIgnoreCase] "Adam"
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                    emailAddress endsWith[stringIgnoreCase] "@test.com"
                """), expected);
    }

    @Test
    public void testValue() {
        expected = List.of("'", "\"");
        assertThat(getSuggestion("givenName equal ^'John' ")).map(Suggestion::name).containsAll(expected);

        expected = List.of("or", "and");
        assertThat(getSuggestion("givenName ='John' ^")).map(Suggestion::name).containsAll(expected);
        assertThat(getSuggestion("givenName = 'John'^")).map(Suggestion::name).isEmpty();
    }

    @Test
    public void testNegation() {
        expected = new ArrayList<>(Arrays.stream(Filter.Alias.values()).map(Filter.Alias::getName).toList());

        assertSuggestionsMatch(getSuggestion("""
                name not^
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                name not^=
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                name not^ equal
                """), expected);

//        expected.add("(");
        expected.addAll(FilterProvider.findFilterByItemDefinition(userDef.findItemDefinition(ItemPath.create(new QName("name"))),15).keySet());
        assertSuggestionsMatch(getSuggestion("""
                name not ^
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                name not ^equal
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                not^ (name not exists)
                """), List.of("SEP"));

        assertSuggestionsMatch(getSuggestion("""
                not ^(name not exists)
                """), List.of("("));

        assertSuggestionsMatch(getSuggestion("""
                . referencedBy not^ (@type = AbstractRoleType and @path = inducement/targetRef )
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                . referencedBy not ^(@type = AbstractRoleType and @path = inducement/targetRef )
                """), expected);
    }

    @Test
    public void testItemFilter() {
        expected = new ArrayList<>();

        assertSuggestionsMatch(getSuggestion("""
                . referencedBy (
                    and na^
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                . referencedBy (
                   @type = AssignmentType ^
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                . referencedBy (
                   @type = AssignmentType
                   and @path = targetRef
                   and . ^ownedBy (
                      @type = UserType
                      and @path = assignment
                      and archetypeRef/@/name = "System user"
                   )
                )
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                . referencedBy (
                   @type = AssignmentType
                   and @path = targetRef
                   and . ownedBy (
                      ^@type = UserType
                      and @path = assignment
                      and archetypeRef/@/name = "System user"
                   )
                )
                """), expected);
    }

    @Test
    public void testLogicalFilter() {
        expected = List.of("and", "or");
        assertThat(getSuggestion("name equal value ^")).map(Suggestion::name).containsAll(expected);
        assertThat(getSuggestion("name= value ^")).map(Suggestion::name).containsAll(expected);
        assertThat(getSuggestion("name =value ^")).map(Suggestion::name).containsAll(expected);

        expected = List.of("(", ".", "@", ":", "$", "..", "#");
        assertThat(getSuggestion("name =value and ^")).map(Suggestion::name).containsAll(expected);
        assertThat(getSuggestion("name =value or ^")).map(Suggestion::name).containsAll(expected);
        assertThat(getSuggestion("name =value or^")).map(Suggestion::name).isEmpty();
        assertThat(getSuggestion("name =value and^")).map(Suggestion::name).isEmpty();
    }

    @Test
    public void testSubFilter() {
        expected = null;

        assertSuggestionsMatch(getSuggestion("""
                . referencedBy ^(
                   @type = AssignmentType
                   and @path = targetRef
                   and . ownedBy (
                      @type = UserType
                      and @path = assignment
                      and archetypeRef/@/name = "System user"
                   )
                )
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                . referencedBy (^
                   @type = AssignmentType
                   and @path = targetRef
                   and . ownedBy (
                      @type = UserType
                      and @path = assignment
                      and archetypeRef/@/name = "System user"
                   )
                )
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                . referencedBy ( ^
                   @type = AssignmentType
                   and @path = targetRef
                   and . ownedBy (
                      @type = UserType
                      and @path = assignment
                      and archetypeRef/@/name = "System user"
                   )
                )
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                . referencedBy (
                   @type = AssignmentType
                   and @path = targetRef
                   and . ownedBy ^(
                      @type = UserType
                      and @path = assignment
                      and archetypeRef/@/name = "System user"
                   )
                )
                """), expected);

        assertSuggestionsMatch(getSuggestion("""
                . referencedBy (
                   @type = AssignmentType
                   and @path = targetRef
                   and . ownedBy (^
                      @type = UserType
                      and @path = assignment
                      and archetypeRef/@/name = "System user"
                   )
                )
                """), expected);
    }
}
