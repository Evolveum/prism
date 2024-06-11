package com.evolveum.midpoint.prism.query.lang;

import static com.evolveum.midpoint.prism.PrismInternalTestUtil.DEFAULT_NAMESPACE_PREFIX;

import java.io.IOException;
import java.util.*;
import javax.xml.namespace.QName;

import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.foo.UserType;
import com.evolveum.midpoint.prism.impl.query.lang.AxiomQueryLangServiceImpl;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.prism.util.PrismTestUtil;
import com.evolveum.midpoint.util.PrettyPrinter;
import com.evolveum.midpoint.util.exception.SchemaException;

/**
 * Created by Dominik.
 */
public class TestQueryCompletion extends AbstractPrismTest {

    private AxiomQueryLangServiceImpl axiomQueryLangServiceImpl;

    private SchemaRegistry schemaRegistry;

    @BeforeSuite
    public void setupDebug() throws SchemaException, SAXException, IOException {
        PrettyPrinter.addDefaultNamespacePrefix(DEFAULT_NAMESPACE_PREFIX);
        PrismTestUtil.resetPrismContext(new PrismInternalTestUtil());
        axiomQueryLangServiceImpl = new AxiomQueryLangServiceImpl(PrismContext.get());
        schemaRegistry = PrismContext.get().getSchemaRegistry();
    }

    // Basic filters
    @Test
    public void testQueryCompletionDot() {
        String query = ". ";
        Map<String, String> suggestions = axiomQueryLangServiceImpl.queryCompletion(null, query);
        Map<String, String> expected = new HashMap<>();
        expected.put("isRoot", null);
        expected.put("inOrg", null);
        expected.put("referencedBy", null);
        expected.put("matches", null);
        expected.put("ownedBy", null);
        expected.put("inOid", null);

        Assert.assertEquals(suggestions.keySet().stream().sorted().toList(), expected.keySet().stream().sorted().toList());
    }

    @Test
    public void testQueryCompletionTypesOfUserType() {
        String query = ". type ";
        TypeDefinition typeDefinition = schemaRegistry.findTypeDefinitionByType(new QName("UserType"));
        Map<String, String> suggestions = axiomQueryLangServiceImpl.queryCompletion(null, query);
        List<String> objectTypes = schemaRegistry.getAllSubTypesByTypeDefinition(Collections.singletonList(typeDefinition)).stream().map(item -> item.getTypeName().getLocalPart()).sorted().toList();
        Assert.assertEquals(suggestions.keySet().stream().sorted().toList(), objectTypes);
    }

    @Test
    public void testQueryCompletionBasePathsOfUserType() {
        String query = ". type UserType and ";
        TypeDefinition typeDefinition = schemaRegistry.findTypeDefinitionByType(new QName("UserType"));
        PrismObjectDefinition<?> objectDefinition = schemaRegistry.findObjectDefinitionByCompileTimeClass((Class) typeDefinition.getCompileTimeClass());
        List<String> itemPaths = new ArrayList<>(objectDefinition.getItemNames().stream().map(QName::getLocalPart).toList());
        itemPaths.add(".");
        Map<String, String> suggestions = axiomQueryLangServiceImpl.queryCompletion(null, query);
        Assert.assertEquals(suggestions.keySet().stream().sorted().toList(), itemPaths.stream().sorted().toList());
    }

    @Test(enabled = false)
    public void testQueryCompletionBaseFilterName() {
        String query = ". type UserType and givenName ";

        List<String> expected = new ArrayList<String>();
        expected.add("levenshtein");
        expected.add("greaterOrEqual");
        expected.add("isRoot");
        expected.add("inOrg");
        expected.add("lessOrEqual");
        expected.add("notEqual");
        expected.add("fullText");
        expected.add("less");
        expected.add("type");
        expected.add("equal");
        expected.add("contains");
        expected.add("ownedByOid");
        expected.add("similarity");
        expected.add("endsWith");
        expected.add("exists");
        expected.add("anyIn");
        expected.add("greater");
        expected.add("ownedBy");
        expected.add("inOid");
        expected.add("startsWith");
        expected.add("not");

        Map<String, String> suggestions = axiomQueryLangServiceImpl.queryCompletion(null, query);
        Assert.assertEquals(suggestions.keySet().stream().sorted().toList(), expected.stream().sorted().toList());
    }

    @Test
    public void testQueryCompletionBaseSubFilter() {
        String query = ". type UserType and givenName equal \"Jack\" ";
        Map<String, String> suggestions = axiomQueryLangServiceImpl.queryCompletion(null, query);
        Assert.assertEquals(suggestions.keySet().stream().sorted().toList(), List.of("and", "or"));
    }

    // Advanced filters
    @Test(enabled = false)
    public void testUserExtensionItemPath() {
        PrismObjectDefinition<UserType> userDef = schemaRegistry.findObjectDefinitionByType(UserType.COMPLEX_TYPE);
        String query = "extension/";
        List<String> suggestions = axiomQueryLangServiceImpl.queryCompletion(userDef, query)
                .keySet().stream().sorted().toList();

        List<String> expected =
                userDef.getExtensionDefinition().getItemNames().stream()
                        .map(i -> i.getLocalPart())
                        .sorted()
                        .toList();

        Assert.assertEquals(expected, suggestions);
    }
}
