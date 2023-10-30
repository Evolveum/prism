package com.evolveum.midpoint.prism.query.lang;

import com.evolveum.midpoint.prism.AbstractPrismTest;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismInternalTestUtil;
import com.evolveum.midpoint.prism.foo.UserType;
import com.evolveum.midpoint.prism.impl.query.lang.AxiomQueryError;
import com.evolveum.midpoint.prism.impl.query.lang.AxiomQueryLangServiceImpl;
import com.evolveum.midpoint.prism.util.PrismTestUtil;
import com.evolveum.midpoint.util.PrettyPrinter;
import com.evolveum.midpoint.util.exception.SchemaException;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

import static com.evolveum.midpoint.prism.PrismInternalTestUtil.DEFAULT_NAMESPACE_PREFIX;

/**
 * Created by Dominik.
 */
public class TestQueryValidation extends AbstractPrismTest {

    private AxiomQueryLangServiceImpl axiomQueryLangServiceImpl = new AxiomQueryLangServiceImpl(PrismContext.get(), UserType.class);

    @BeforeSuite
    public void setupDebug() throws SchemaException, SAXException, IOException {
        PrettyPrinter.setDefaultNamespacePrefix(DEFAULT_NAMESPACE_PREFIX);
        PrismTestUtil.resetPrismContext(new PrismInternalTestUtil());
    }

    private void displayErrorList(List<AxiomQueryError> errorList) {
        for (AxiomQueryError axiomQueryError : errorList) {
            System.out.println("Error message: " + axiomQueryError.getMessage());
        }
    }

    @Test
    public void validateEasyQuery() {
        String query = ". type UserType and givenName equal \"Jack\"";
        List<AxiomQueryError> errorList = this.axiomQueryLangServiceImpl.validate(query);
        displayErrorList(errorList);
    }

    @Test
    public void invalidateBadFilterName() {
        String query = ". type UserType and givenName equal1 \"Jack\" and name = \"Jack\"";
        List<AxiomQueryError> errorList = this.axiomQueryLangServiceImpl.validate(query);
        displayErrorList(errorList);
    }

    @Test
    public void invalidateReferencedByFilterName() {
        String query = ". type UserType and givenName referencedBy \"Jack\"";
        List<AxiomQueryError> errorList = this.axiomQueryLangServiceImpl.validate(query);
        displayErrorList(errorList);
    }

    @Test
    public void invalidateMatchesFilterName() {
        String query = ". type UserType and givenName matches \"Jack\"";
        List<AxiomQueryError> errorList = this.axiomQueryLangServiceImpl.validate(query);
        displayErrorList(errorList);
    }

    @Test
    public void invalidateFilterNameAlias() {
        String query = ". type UserType and activation = \"disabled\"";
        List<AxiomQueryError> errorList = this.axiomQueryLangServiceImpl.validate(query);
        displayErrorList(errorList);
    }

    @Test
    public void invalidateItemPath() {
        String query = ". type UserType and givenName1 equal \"Jack\"";
        List<AxiomQueryError> errorList = this.axiomQueryLangServiceImpl.validate(query);
        displayErrorList(errorList);
    }

    @Test
    public void invalidateObjectType() {
        String query = ". type UserType1 and givenName1 equal \"Jack\"";
        List<AxiomQueryError> errorList = this.axiomQueryLangServiceImpl.validate(query);
        displayErrorList(errorList);
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

        List<AxiomQueryError> errorList = this.axiomQueryLangServiceImpl.validate(query);
        displayErrorList(errorList);
    }
}
