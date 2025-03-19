package com.evolveum.midpoint.prism.query.lang;

import java.io.IOException;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

import com.evolveum.midpoint.prism.foo.*;
import com.evolveum.midpoint.prism.impl.query.lang.Filter;
import com.evolveum.midpoint.prism.impl.query.lang.FilterProvider;
import com.evolveum.midpoint.prism.impl.xnode.XNodeImpl;
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
    List<Suggestion> suggestion = new ArrayList<>();
    SchemaRegistry schemaRegistry;

    @BeforeSuite
    public void setupDebug() throws SchemaException, SAXException, IOException {
        PrismTestUtil.resetPrismContext(new PrismInternalTestUtil());
        axiomQueryContentAssist = new AxiomQueryContentAssistImpl(getPrismContext());
        schemaRegistry = getPrismContext().getSchemaRegistry();
        userDef = schemaRegistry.findObjectDefinitionByType(UserType.COMPLEX_TYPE);
    }

    private List<Suggestion> getSuggestion(String query) {
        int position = Math.max(0, query.indexOf('^'));
        // remove caret from query string
        query = query.replaceAll("\\^", "");
        ContentAssist contentAssist = axiomQueryContentAssist.process(userDef, query, position);
        return contentAssist.autocomplete();
    }

    @Test
    public void testRootCtx() {
        suggestion = getSuggestion("^");

        assertThat(suggestion).map(Suggestion::name).containsAll(List.of(".", "@", "not"));
        userDef.getItemNames().stream().map(ItemName::first).filter(Objects::nonNull).forEach(itemName -> {
            assertThat(suggestion).map(Suggestion::name).contains(itemName.toString());
        });

        suggestion = getSuggestion("  ^");
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of(".", "@", "not"));
        userDef.getItemNames().stream().map(ItemName::first).filter(Objects::nonNull).forEach(itemName -> {
            assertThat(suggestion).map(Suggestion::name).contains(itemName.toString());
        });

        suggestion = getSuggestion("^  ");
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of(".", "@", "not"));
        userDef.getItemNames().stream().map(ItemName::first).filter(Objects::nonNull).forEach(itemName -> {
            assertThat(suggestion).map(Suggestion::name).contains(itemName.toString());
        });

        suggestion = getSuggestion("  ^  ");
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of(".", "@", "not"));
        userDef.getItemNames().stream().map(ItemName::first).filter(Objects::nonNull).forEach(itemName -> {
            assertThat(suggestion).map(Suggestion::name).contains(itemName.toString());
        });
    }

    @Test
    public void testBasicItemPath() {
        suggestion = getSuggestion("a^ equal value");
        List<String> aliases = new ArrayList<>(Arrays.stream(Filter.Alias.values()).map(Filter.Alias::getName).toList());
        assertThat(suggestion).map(Suggestion::name).containsAll(aliases);
        userDef.getItemNames().stream().map(ItemName::first).filter(Objects::nonNull).forEach(itemName -> {
            assertThat(suggestion).map(Suggestion::name).contains(itemName.toString());
        });

        suggestion = getSuggestion("name^");
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of(":", "$"));
        assertThat(suggestion).map(Suggestion::name).containsAll(aliases);

        suggestion = getSuggestion("name^ equal value");
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of(":", "$"));
        assertThat(suggestion).map(Suggestion::name).containsAll(aliases);

        suggestion = getSuggestion("name ^");
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("not"));
        assertThat(suggestion).map(Suggestion::name).containsAll(aliases);
        FilterProvider.findFilterByItemDefinition(userDef.findItemDefinition(ItemPath.create(new QName("name"))), 15).forEach(
                (name, alias) -> {
                    assertThat(suggestion).map(Suggestion::name).contains(name);
                }
        );

        suggestion = getSuggestion("name ^equal 'value'");
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("not"));
        assertThat(suggestion).map(Suggestion::name).containsAll(aliases);
        FilterProvider.findFilterByItemDefinition(userDef.findItemDefinition(ItemPath.create(new QName("name"))), 15).forEach(
                (name, alias) -> {
                    assertThat(suggestion).map(Suggestion::name).contains(name);
                }
        );
    }

    @Test
    public void testSelfPath() {
        suggestion = getSuggestion(". ^");

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

    @Test
    public void testReferenceAndDereferencePath() {
        suggestion = getSuggestion("""
                assignment/targetRef^
                """);

        PrismContainerDefinition<?> def = userDef.findItemDefinition(ItemPath.create(new QName("assignment")));
        List<String> aliases = new ArrayList<>(Arrays.stream(Filter.Alias.values()).map(Filter.Alias::getName).toList());
        assertThat(suggestion).map(Suggestion::name).containsAll(aliases);
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("#", ":", "$", "/"));
        def.getItemNames().stream().map(ItemName::first).filter(Objects::nonNull).forEach(itemName -> {
            assertThat(suggestion).map(Suggestion::name).contains(itemName.toString());
        });

        suggestion = getSuggestion("""
                assignment/^
                """);
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("#", ":", "$", ".."));
        def.getItemNames().stream().map(ItemName::first).filter(Objects::nonNull).forEach(itemName -> {
            assertThat(suggestion).map(Suggestion::name).contains(itemName.toString());
        });

        suggestion = getSuggestion("""
                assignment/targetRef^/@/name = "End user"
                """);
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of(":", "$", "/"));
        assertThat(suggestion).map(Suggestion::name).containsAll(aliases);
        PrismReferenceDefinition ref = def.findReferenceDefinition(ItemPath.create(new QName("targetRef")));
        assertThat(suggestion).map(Suggestion::name).contains(ref.getItemName().getLocalPart());

        suggestion = getSuggestion("""
                assignment/targetRef/^@/name = "End user"
                """);
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("@"));

        suggestion = getSuggestion("""
                assignment/targetRef/^
                """);
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("@"));

        suggestion = getSuggestion("""
                assignment/targetRef/@^/name = "End user"
                """);
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("/"));

        suggestion = getSuggestion("""
                assignment/targetRef/@/^
                """);
        PrismObjectDefinition<?> objDef = PrismContext.get().getSchemaRegistry().findObjectDefinitionByType(ref.getTargetTypeName());
        objDef.getItemNames().stream().map(ItemName::first).filter(Objects::nonNull).forEach(itemName -> {
            assertThat(suggestion).map(Suggestion::name).contains(itemName.toString());
        });
    }

    @Test
    public void testMetadataPath() {
        var localValMetadataDef = getPrismContext().getSchemaRegistry().getValueMetadataDefinition();

        suggestion = getSuggestion("^");
        localValMetadataDef.getDefinitions().forEach(metadata -> {
            assertThat(suggestion).map(Suggestion::name).contains("@metadata/" + metadata.getItemName().getLocalPart());
        });

        suggestion = getSuggestion("@^ ");
        localValMetadataDef.getDefinitions().forEach(metadata -> {
            assertThat(suggestion).map(Suggestion::name).contains("@metadata/" + metadata.getItemName().getLocalPart());
        });

        suggestion = getSuggestion("@meta^");
        localValMetadataDef.getDefinitions().forEach(metadata -> {
            assertThat(suggestion).map(Suggestion::name).contains("@metadata/" + metadata.getItemName().getLocalPart());
        });

        suggestion = getSuggestion("@metadata^");
        localValMetadataDef.getDefinitions().forEach(metadata -> {
            assertThat(suggestion).map(Suggestion::name).contains("@metadata/" + metadata.getItemName().getLocalPart());
        });

        suggestion = getSuggestion("@metadata/^ ");
        localValMetadataDef.getDefinitions().forEach(metadata -> {
            assertThat(suggestion).map(Suggestion::name).contains(metadata.getItemName().getLocalPart());
        });

        suggestion = getSuggestion("@metadata/storage^ ");
        PrismContainerDefinition<?> metadataDefinition = getPrismContext().getSchemaRegistry().getValueMetadataDefinition();
        metadataDefinition.getDefinitions().forEach(item -> {
            assertThat(suggestion).map(Suggestion::name).contains(item.getItemName().getLocalPart());
            if (item instanceof PrismContainerDefinition<?> containerDefinition) {
                containerDefinition.getDefinitions().forEach(childItem -> {
                    assertThat(suggestion).map(Suggestion::name).contains(item.getItemName().getLocalPart() + "/" + childItem.getItemName().getLocalPart());
                });
            }
        });

        for (Filter.Alias value : Filter.Alias.values()) {
            assertThat(suggestion).map(Suggestion::name).contains(value.getName());
        }

        suggestion = getSuggestion("@metadata/storage/^ ");
        PrismContainerDefinition<?> storageDef = metadataDefinition.findItemDefinition(ItemPath.create("storage"));
        storageDef.getDefinitions().forEach(item -> {
            assertThat(suggestion).map(Suggestion::name).contains(item.getItemName().getLocalPart());
        });

        suggestion = getSuggestion("@metadata/process/^ ");
        PrismContainerDefinition<?> processDef = metadataDefinition.findItemDefinition(ItemPath.create("process"));
        processDef.getDefinitions().forEach(item -> {
            assertThat(suggestion).map(Suggestion::name).contains(item.getItemName().getLocalPart());
        });
    }

    @Test
    public void testFilterNameAndFilterAlias() {
        suggestion = getSuggestion("assignment^ ");
        List<String> aliases = new ArrayList<>(Arrays.stream(Filter.Alias.values()).map(Filter.Alias::getName).toList());
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of(":", "$", "/", "#"));
        assertThat(suggestion).map(Suggestion::name).containsAll(aliases);

        suggestion = getSuggestion("assignment ^ ");
        PrismContainerDefinition<?> def = userDef.findItemDefinition(ItemPath.create(new QName("assignment")));
        assertThat(suggestion).map(Suggestion::name).containsAll(aliases);
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("not"));
        FilterProvider.findFilterByItemDefinition(def, 15).forEach(
                (name, alias) -> {
                    assertThat(suggestion).map(Suggestion::name).contains(name);
                }
        );

        suggestion = getSuggestion("givenName^ ");
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of(":", "$"));
        assertThat(suggestion).map(Suggestion::name).containsAll(aliases);

        suggestion = getSuggestion("givenName ^ ");
        assertThat(suggestion).map(Suggestion::name).containsAll(aliases);
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("not"));
        FilterProvider.findFilterByItemDefinition(userDef.findItemDefinition(ItemPath.create(new QName("givenName"))), 15).forEach(
                (name, alias) -> {
                    assertThat(suggestion).map(Suggestion::name).contains(name);
                }
        );

        suggestion = getSuggestion("givenName not ^ ");
        assertThat(suggestion).map(Suggestion::name).containsAll(aliases);
        FilterProvider.findFilterByItemDefinition(userDef.findItemDefinition(ItemPath.create(new QName("givenName"))), 15).forEach(
                (name, alias) -> {
                    assertThat(suggestion).map(Suggestion::name).contains(name);
                }
        );

        suggestion = getSuggestion("assignment/targetRef ^");
        PrismReferenceDefinition ref = def.findReferenceDefinition(ItemPath.create(new QName("targetRef")));
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("not"));
        assertThat(suggestion).map(Suggestion::name).containsAll(aliases);
        FilterProvider.findFilterByItemDefinition(ref, 15).forEach(
                (name, alias) -> {
                    assertThat(suggestion).map(Suggestion::name).contains(name);
                }
        );

        suggestion = getSuggestion("assignment/targetRef/@ ^");
        PrismObjectDefinition<?> objDef = PrismContext.get().getSchemaRegistry().findObjectDefinitionByType(ref.getTargetTypeName());
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("not"));
        assertThat(suggestion).map(Suggestion::name).containsAll(aliases);
        FilterProvider.findFilterByItemDefinition(objDef, 15).forEach(
                (name, alias) -> {
                    assertThat(suggestion).map(Suggestion::name).contains(name);
                }
        );

        suggestion = getSuggestion("assignment/targetRef/@/name ^");
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("not"));
        assertThat(suggestion).map(Suggestion::name).containsAll(aliases);
        FilterProvider.findFilterByItemDefinition(objDef.findItemDefinition(ItemPath.create(new QName("name"))), 15).forEach(
                (name, alias) -> {
                    assertThat(suggestion).map(Suggestion::name).contains(name);
                }
        );

        suggestion = getSuggestion("givenName =^ ");
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("[", "(", "'", "\""));

        suggestion = getSuggestion("givenName equal^ ");
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("[", "("));
        FilterProvider.findFilterByItemDefinition(userDef.findItemDefinition(ItemPath.create(new QName("givenName"))), 15).forEach(
            (name, alias) -> {
                assertThat(suggestion).map(Suggestion::name).contains(name);
            }
        );
    }

    @Test
    public void testMatchingRule() {
        suggestion = getSuggestion("givenName equal^ ");
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("[", "'", "\"", "("));

        suggestion = getSuggestion("givenName equal[^ ");
        assertThat(suggestion).map(Suggestion::name).containsAll(
                new ArrayList<>(Arrays.stream(Filter.PolyStringKeyword.MatchingRule.values()).map(Filter.PolyStringKeyword.MatchingRule::getName).toList())
        );

        suggestion = getSuggestion("givenName equal[normIgnoreCase^ ");
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("]"));
    }

    @Test
    public void testValue() {
        suggestion = getSuggestion("givenName equal ^'John' ");
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of(".", "..", "@", "'", "\"", "("));
        userDef.getItemNames().stream().map(ItemName::first).filter(Objects::nonNull).forEach(itemName -> {
            assertThat(suggestion).map(Suggestion::name).contains(itemName.toString());
        });

        suggestion = getSuggestion("givenName equal ^");
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of(".", "..", "@", "'", "\"", "("));
        userDef.getItemNames().stream().map(ItemName::first).filter(Objects::nonNull).forEach(itemName -> {
            assertThat(suggestion).map(Suggestion::name).contains(itemName.toString());
        });

        assertThat(getSuggestion("givenName = 'John'^")).map(Suggestion::name).isEmpty();
        suggestion = getSuggestion("givenName ='John' ^");
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("or", "and"));
    }

    @Test
    public void testLogicalFilter() {
        suggestion = getSuggestion("name equal value ^");
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("and", "or"));

        suggestion = getSuggestion("name= value ^");
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("and", "or"));

        suggestion = getSuggestion("name =value ^");
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("and", "or"));

        suggestion = getSuggestion("name =value and^");
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("("));

        suggestion = getSuggestion("name =value or^");
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("("));

        suggestion = getSuggestion("name =value and ^");
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("(", ".", "@", ".."));

        suggestion = getSuggestion("name =value or ^");
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("(", ".", "@", ".."));
    }

    @Test
    public void testInfraFilter() {
        suggestion = getSuggestion("""
                . referencedBy (^
                """);
        List<String> infraFilters = new ArrayList<>(Arrays.stream(Filter.Infra.values()).map(Filter.Infra::getName).toList());
        infraFilters.remove(Filter.Infra.METADATA.getName());
        assertThat(suggestion).map(Suggestion::name).containsAll(infraFilters);

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
        assertThat(suggestion).map(Suggestion::name).containsAll(infraFilters);

        suggestion = getSuggestion("""
                . referencedBy (
                   @type ^
                """);
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("="));

        suggestion = getSuggestion("""
                . referencedBy (
                   @type^ = AssignmentType
                   and @path = targetRef
                   and . ownedBy (
                      @type = UserType
                      and @path = assignment
                      and archetypeRef/@/name = "System user"
                   )
                )
                """);
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("="));

        suggestion = getSuggestion("""
                . referencedBy (
                   @type = ^
                """);
        PrismObjectDefinition<?> objectTypeDefinition = schemaRegistry.findObjectDefinitionByType(getPrismContext().getDefaultReferenceTargetType());
        schemaRegistry.findTypeDefinitionByCompileTimeClass(objectTypeDefinition.getCompileTimeClass(), TypeDefinition.class)
                .getStaticSubTypes().forEach(objSubType -> {
                    assertThat(suggestion).map(Suggestion::name).contains(objSubType.getTypeName().getLocalPart());
                });

        suggestion = getSuggestion("""
                . referencedBy (
                   @type = ^AssignmentType
                   and @path = targetRef
                   and . ownedBy (
                      @type = UserType
                      and @path = assignment
                      and archetypeRef/@/name = "System user"
                   )
                )
                """);
        schemaRegistry.findTypeDefinitionByCompileTimeClass(objectTypeDefinition.getCompileTimeClass(), TypeDefinition.class)
                .getStaticSubTypes().forEach(objSubType -> {
                    assertThat(suggestion).map(Suggestion::name).contains(objSubType.getTypeName().getLocalPart());
                });

        suggestion = getSuggestion("""
                . referencedBy (
                   @type = AssignmentType
                   and @path = targetRef
                   and . ownedBy (
                      @type = ^UserType
                      and @path = assignment
                      and archetypeRef/@/name = "System user"
                   )
                )
                """);
        schemaRegistry.findTypeDefinitionByCompileTimeClass(objectTypeDefinition.getCompileTimeClass(), TypeDefinition.class)
                .getStaticSubTypes().forEach(objSubType -> {
                    assertThat(suggestion).map(Suggestion::name).contains(objSubType.getTypeName().getLocalPart());
                });

        suggestion = getSuggestion("""
                . referencedBy (
                   @type = AssignmentType
                   and @path = targetRef
                   and . ownedBy (
                      @type = UserType
                      and @path = ^assignment
                      and archetypeRef/@/name = "System user"
                   )
                )
                """);

        userDef.getItemNames().stream().map(ItemName::first).filter(Objects::nonNull).forEach(itemName -> {
            assertThat(suggestion).map(Suggestion::name).contains(itemName.toString());
        });
    }

    @Test
    public void testSubFilter() {
        suggestion = getSuggestion("""
                . referencedBy ^
                """);
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("("));

        suggestion = getSuggestion("""
                . referencedBy (
                   @type = AssignmentType ^
                """);
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("and", "or",")"));

        suggestion = getSuggestion("""
                . referencedBy (
                   @type = AssignmentType
                   and @path = targetRef
                   and . ^ ownedBy (
                      @type = UserType
                      and @path = assignment
                      and archetypeRef/@/name = "System user"
                   )
                )
                """);
        PrismContainerDefinition<?> def = userDef.findItemDefinition(ItemPath.create(new QName("assignment")));
        FilterProvider.findFilterByItemDefinition(def, 15).forEach(
                (name, alias) -> {
                    assertThat(suggestion).map(Suggestion::name).contains(name);
                }
        );

        for (Filter.Alias value : Filter.Alias.values()) {
            assertThat(suggestion).map(Suggestion::name).isNotEqualTo(value.getName());
        }

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
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("@", "@type", "@path", "@relation", ".", ".."));

        suggestion = getSuggestion("""
                . referencedBy (
                   @type = AssignmentType
                   and @path = targetRef
                   and . ownedBy (
                      @type = UserType ^
                """);
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("and", "or", ")"));

        suggestion = getSuggestion("""
                . referencedBy (
                   @type = AssignmentType
                   and @path = targetRef
                   and . ownedBy (
                      @type = UserType ^
                      and @path = assignment
                      and archetypeRef/@/name = "System user"
                   )
                )
                """);
        assertThat(suggestion).map(Suggestion::name).containsAll(List.of("and", "or"));
    }
}
