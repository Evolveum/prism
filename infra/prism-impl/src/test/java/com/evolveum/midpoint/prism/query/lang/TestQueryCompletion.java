package com.evolveum.midpoint.prism.query.lang;

import java.io.IOException;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

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

    @Test()
    public void testRootCtx() {
        List<Suggestion> suggestion = getSuggestion("^");
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of(".", "@", "not"));
        userDef.getItemNames().stream().map(ItemName::first).filter(Objects::nonNull).forEach(itemName -> {
            assertThat(suggestion).map(Suggestion::name).contains(itemName.toString());
        });

        List<Suggestion> suggestion1 = getSuggestion("  ^");
        assertThat(suggestion1).map(Suggestion::name).containsAll(List.of(".", "@", "not"));
        userDef.getItemNames().stream().map(ItemName::first).filter(Objects::nonNull).forEach(itemName -> {
            assertThat(suggestion1).map(Suggestion::name).contains(itemName.toString());
        });

        List<Suggestion> suggestion2 = getSuggestion("^  ");
        assertThat(suggestion2).map(Suggestion::name).containsAll(List.of(".", "@", "not"));
        userDef.getItemNames().stream().map(ItemName::first).filter(Objects::nonNull).forEach(itemName -> {
            assertThat(suggestion2).map(Suggestion::name).contains(itemName.toString());
        });

        List<Suggestion> suggestion3 = getSuggestion("  ^  ");
        assertThat(suggestion3).map(Suggestion::name).containsAll(List.of(".", "@", "not"));
        userDef.getItemNames().stream().map(ItemName::first).filter(Objects::nonNull).forEach(itemName -> {
            assertThat(suggestion3).map(Suggestion::name).contains(itemName.toString());
        });
    }

    @Test()
    public void testItemPath() {
        List<Suggestion> suggestion = getSuggestion("name^");
        List<String> aliases = new ArrayList<>(Arrays.stream(Filter.Alias.values()).map(Filter.Alias::getName).toList());
        assertThat(suggestion).map(Suggestion::name).containsAll(aliases);
        userDef.getItemNames().stream().map(ItemName::first).filter(Objects::nonNull).forEach(itemName -> {
            assertThat(suggestion).map(Suggestion::name).contains(itemName.toString());
        });

        List<Suggestion> suggestion2 = getSuggestion("name^ equal value");
        assertThat(suggestion2).map(Suggestion::name).containsAll(aliases);
        userDef.getItemNames().stream().map(ItemName::first).filter(Objects::nonNull).forEach(itemName -> {
            assertThat(suggestion2).map(Suggestion::name).contains(itemName.toString());
        });

        List<Suggestion> suggestion1 = getSuggestion("name ^");
        aliases.add("not");
        assertThat(suggestion1).map(Suggestion::name).containsAll(aliases);
        FilterProvider.findFilterByItemDefinition(userDef.findItemDefinition(ItemPath.create(new QName("name"))), 15).forEach(
                (name, alias) -> {
                    assertThat(suggestion1).map(Suggestion::name).contains(name);
                }
        );

        List<Suggestion> suggestion3 = getSuggestion("name ^equal 'value'");
        assertThat(suggestion3).map(Suggestion::name).containsAll(aliases);
        FilterProvider.findFilterByItemDefinition(userDef.findItemDefinition(ItemPath.create(new QName("name"))), 15).forEach(
                (name, alias) -> {
                    assertThat(suggestion3).map(Suggestion::name).contains(name);
                }
        );
    }

    @Test()
    public void testSelfPath() {
        List<Suggestion> suggestion = getSuggestion(". ^");

        FilterProvider.findFilterByItemDefinition(userDef, 15).forEach(
                (name, alias) -> {
                    assertThat(suggestion).map(Suggestion::name).contains(name);
                }
        );

        for (Filter.Alias value : Filter.Alias.values()) {
            assertThat(suggestion).map(Suggestion::name).isNotEqualTo(value.getName());
        }
    }

    @Test(enabled = false)
    public void testParentPath() {
        // TODO
    }

    @Test(enabled = false)
    public void testAxiomPath() {
        // TODO
    }

    @Test(enabled = false)
    public void testContainerPath() {
        // TODO
    }

    @Test()
    public void testReferenceAndDereferencePath() {
        List<Suggestion> suggestion = getSuggestion("""
                assignment/^targetRef/@/name = "End user"
                """);
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("@", "#", ":"));
        userDef.getItemNames().stream().map(ItemName::first).filter(Objects::nonNull).forEach(itemName -> {
            assertThat(suggestion).map(Suggestion::name).contains(itemName.toString());
        });

        List<Suggestion> suggestion1 = getSuggestion("""
                assignment/targetRef^/@/name = "End user"
                """);
        assertThat(suggestion1).map(Suggestion::name).contains("/");

        List<Suggestion> suggestion2 = getSuggestion("""
                assignment/^
                """);
        assertThat(suggestion2).map(Suggestion::name).containsAll(List.of("@", "#", ":", ".."));
        userDef.getItemNames().stream().map(ItemName::first).filter(Objects::nonNull).forEach(itemName -> {
            assertThat(suggestion2).map(Suggestion::name).contains(itemName.toString());
        });

        List<Suggestion> suggestion3 = getSuggestion("""
                assignment/targetRef/^@/name = "End user"
                """);
        assertThat(suggestion3).map(Suggestion::name).containsAll(List.of("@", "#", ":"));
        userDef.getItemNames().stream().map(ItemName::first).filter(Objects::nonNull).forEach(itemName -> {
            assertThat(suggestion3).map(Suggestion::name).contains(itemName.toString());
        });

        List<Suggestion> suggestion4 = getSuggestion("""
                assignment/targetRef/@/^
                """);

        PrismContainerDefinition<?> def = userDef.findItemDefinition(ItemPath.create(new QName("assignment")));
        PrismReferenceDefinition ref = def.findReferenceDefinition(ItemPath.create(new QName("targetRef")));
        PrismObjectDefinition<?> objDef = PrismContext.get().getSchemaRegistry().findObjectDefinitionByType(ref.getTargetTypeName());
        objDef.getItemNames().stream().map(ItemName::first).filter(Objects::nonNull).forEach(itemName -> {
            assertThat(suggestion4).map(Suggestion::name).contains(itemName.toString());
        });

        assertThat(getSuggestion("@^")).map(Suggestion::name).isEmpty();
    }

    @Test
    public void testValue() {
        // FIXME discussion of what to generate for value
//        List<Suggestion> suggestion = getSuggestion("givenName equal ^'John' ");
//        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("'", "\""));

        assertThat(getSuggestion("givenName = 'John'^")).map(Suggestion::name).isEmpty();

        List<Suggestion> suggestion1 = getSuggestion("givenName ='John' ^");
        assertThat(suggestion1).map(Suggestion::name).containsAll(List.of("or", "and"));
    }

    @Test(enabled = false)
    public void testItemFilter() {
//        getSuggestion("name not^");
//        getSuggestion("name not ^");
//        getSuggestion("name not^= ");
//        getSuggestion("name not^ equal");
//        getSuggestion("name not ^equal");

//        suggestion = getSuggestion("""
//                . referencedBy (
//                    and na^
//                """);

//        suggestion = getSuggestion("""
//                . referencedBy (
//                   @type = AssignmentType ^
//                """);
//
//        suggestion = getSuggestion("""
//                . referencedBy (
//                   @type = AssignmentType
//                   and @path = targetRef
//                   and . ^ownedBy (
//                      @type = UserType
//                      and @path = assignment
//                      and archetypeRef/@/name = "System user"
//                   )
//                )
//                """);
//
//        suggestion = getSuggestion("""
//                . referencedBy (
//                   @type = AssignmentType
//                   and @path = targetRef
//                   and . ownedBy (
//                      ^@type = UserType
//                      and @path = assignment
//                      and archetypeRef/@/name = "System user"
//                   )
//                )
//                """);
    }

    @Test()
    public void testLogicalFilter() {
        List<Suggestion> suggestion = getSuggestion("name equal value ^");
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("and", "or"));

        suggestion = getSuggestion("name= value ^");
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("and", "or"));

        suggestion = getSuggestion("name =value ^");
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("and", "or"));

        suggestion = getSuggestion("name =value and^");
        assertThat(suggestion).map(Suggestion::name).isEmpty();

        suggestion = getSuggestion("name =value and ^");
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("(", ".", "@", ":", "$", "..", "#"));

        suggestion = getSuggestion("name =value or^");
         assertThat(suggestion).map(Suggestion::name).isEmpty();

        suggestion = getSuggestion("name =value or ^");
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("(", ".", "@", ":", "$", "..", "#"));
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
}
