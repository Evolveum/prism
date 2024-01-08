package com.evolveum.midpoint.prism.query.lang;

import com.evolveum.midpoint.prism.AbstractPrismTest;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismInternalTestUtil;
import com.evolveum.axiom.lang.antlr.AxiomQueryError;
import com.evolveum.midpoint.prism.impl.query.lang.AxiomQueryLangServiceImpl;
import com.evolveum.midpoint.prism.query.AxiomQueryLangService;
import com.evolveum.midpoint.prism.util.PrismTestUtil;
import com.evolveum.midpoint.util.PrettyPrinter;
import com.evolveum.midpoint.util.exception.SchemaException;

import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.evolveum.midpoint.prism.PrismInternalTestUtil.DEFAULT_NAMESPACE_PREFIX;

import static org.testng.AssertJUnit.assertTrue;

/**
 * Created by Dominik.
 */
public class TestQueryValidation extends AbstractPrismTest {

    AxiomQueryLangService axiomQueryLangService;

    @BeforeSuite
    public void setupDebug() throws SchemaException, SAXException, IOException {
        PrettyPrinter.addDefaultNamespacePrefix(DEFAULT_NAMESPACE_PREFIX);
        PrismTestUtil.resetPrismContext(new PrismInternalTestUtil());
        this.axiomQueryLangService = new AxiomQueryLangServiceImpl(PrismContext.get());
    }

    private boolean checkingAxiomQueryErrorList(List<AxiomQueryError> errorList, List<AxiomQueryError> expectedList) {
        int errorCount = errorList.size();
        int expectedCount = expectedList.size();

        if (errorCount != expectedCount) return false;

        for(int i = 0; i < errorCount; ++i) {
            if (!errorList.get(i).equals(expectedList.get(i))) {
                return false;
            }
        }

        return true;
    }

    @Test
    public void validateEasyQuery() {
        String query = ". type UserType and givenName equal \"Jack\"";
        List<AxiomQueryError> errorList = this.axiomQueryLangService.validate(query);
        Assert.assertEquals(errorList, new ArrayList<>(), "verified query\n");
    }

    @Test
    public void invalidateBadFilterName() {
        String query = ". type UserType and givenName equal1 \"Jack\" and name = \"Jack\"";
        List<AxiomQueryError> errorList = this.axiomQueryLangService.validate(query);

        List<AxiomQueryError> expected = new ArrayList<>();
        expected.add(
            new AxiomQueryError(null,
                null,
                1, 30, 35,
                "Filter equal1 is not supported for path givenName",
                null)
        );

        assertTrue("invalid filter name\n", checkingAxiomQueryErrorList(errorList, expected));
    }

    @Test
    public void invalidateReferencedByFilterName() {
        String query = ". type UserType and givenName referencedBy \"Jack\"";
        List<AxiomQueryError> errorList = this.axiomQueryLangService.validate(query);

        List<AxiomQueryError> expected = new ArrayList<>();
        expected.add(
                new AxiomQueryError(null,
                        null,
                        1, 30, 41,
                        "Filter referencedBy is not supported for path givenName",
                        null)
        );

        assertTrue("invalid referencedBy\n", checkingAxiomQueryErrorList(errorList, expected));
    }

    @Test
    public void invalidateMatchesFilterName() {
        String query = ". type UserType and givenName matches \"Jack\"";
        List<AxiomQueryError> errorList = this.axiomQueryLangService.validate(query);

        List<AxiomQueryError> expected = new ArrayList<>();
        expected.add(
                new AxiomQueryError(null,
                        null,
                        1, 30, 36,
                        "Filter matches is not supported for path givenName",
                        null)
        );

        assertTrue("invalid matches\n", checkingAxiomQueryErrorList(errorList, expected));
    }

    @Test
    public void invalidateFilterNameAlias() {
        String query = ". type UserType and activation = \"disabled\"";
        List<AxiomQueryError> errorList = this.axiomQueryLangService.validate(query);

        List<AxiomQueryError> expected = new ArrayList<>();
        expected.add(
                new AxiomQueryError(null,
                        null,
                        1, 31, 31,
                        "Filter = is not supported for path activation",
                        null)
        );

        assertTrue("invalid filter alias\n", checkingAxiomQueryErrorList(errorList, expected));
    }

    @Test
    public void invalidateItemPath() {
        String query = ". type UserType and givenName1 equal \"Jack\"";
        List<AxiomQueryError> errorList = this.axiomQueryLangService.validate(query);

        List<AxiomQueryError> expected = new ArrayList<>();
        expected.add(
                new AxiomQueryError(null,
                        null,
                        1, 20, 29,
                        "Path givenName1 is not present in type UserType",
                        null)
        );

        assertTrue("invalid item path\n", checkingAxiomQueryErrorList(errorList, expected));
    }

    @Test
    public void invalidateObjectType() {
        String query = ". type UserType1 and givenName1 equal \"Jack\"";
        List<AxiomQueryError> errorList = this.axiomQueryLangService.validate(query);

        List<AxiomQueryError> expected = new ArrayList<>();
        expected.add(
                new AxiomQueryError(null,
                        null,
                        1, 7, 15,
                        "Does not exist type UserType1",
                        null)
        );

        expected.add(
                new AxiomQueryError(null,
                        null,
                        1, 21, 30,
                        "Missing object definition",
                        null)
        );

        assertTrue("invalid object type\n", checkingAxiomQueryErrorList(errorList, expected));
    }

    @Test
    public void invalidateComplexitiesQuery() {
        String query = ". referencedBy (\n"
                + "  @type = UserType\n"
                + "  and @path = assignment/targetRef\n"
                + "  and name = \"Administrator\"\n"
                + "  and given = \"\"\"\n"
                + "  asldkf\n"
                + "  \"\"\"\n"
                + "  and x = 1\n"
                + "  and file = true\n"
                + "  and y = 1.0\n"
                + ")";

        List<AxiomQueryError> errorList = this.axiomQueryLangService.validate(query);

        List<AxiomQueryError> expected = new ArrayList<>();
        expected.add(
                new AxiomQueryError(null,
                        null,
                        3, 50, 69,
                        "Path assignment/targetRef is not present in type UserType",
                        null)
        );

        expected.add(
                new AxiomQueryError(null,
                        null,
                        5, 106, 110,
                        "Path given is not present in type UserType",
                        null)
        );

        expected.add(
                new AxiomQueryError(null,
                        null,
                        8, 139, 139,
                        "Path x is not present in type UserType",
                        null)
        );

        expected.add(
                new AxiomQueryError(null,
                        null,
                        9, 151, 154,
                        "Path file is not present in type UserType",
                        null)
        );

        expected.add(
                new AxiomQueryError(null,
                        null,
                        10, 169, 169,
                        "Path y is not present in type UserType",
                        null)
        );

        assertTrue("invalid item path\n", checkingAxiomQueryErrorList(errorList, expected));
    }

    //    TODO tests for syntax errors
}
