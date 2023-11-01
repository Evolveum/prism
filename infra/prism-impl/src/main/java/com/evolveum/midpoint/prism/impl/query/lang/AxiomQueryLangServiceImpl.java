package com.evolveum.midpoint.prism.impl.query.lang;

import com.evolveum.axiom.lang.antlr.AxiomQuerySource;
import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.query.AxiomQueryLangService;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;

import static com.evolveum.axiom.lang.antlr.query.AxiomQueryParser.*;

import java.util.List;

/**
 * Created by Dominik.
 */
public class AxiomQueryLangServiceImpl implements AxiomQueryLangService {
    private PrismContext prismContext;
    private Class userType;

    public AxiomQueryLangServiceImpl(PrismContext prismContext, Class userType) {
        this.prismContext = prismContext;
        this.userType = userType;
    }

    public List<AxiomQueryError> validate(String query) {
        AxiomQueryValidationVisitor axiomQueryValidationVisitor = new AxiomQueryValidationVisitor(this.prismContext, userType);
        AxiomQuerySource.from(query).root().accept(axiomQueryValidationVisitor);
        return axiomQueryValidationVisitor.errorList;
    }

    public List<String> queryCompletion(String query) {
        AxiomQuerySource axiomQuerySource = AxiomQuerySource.from(query);
        AxiomQueryCompletionVisitor axiomQueryCompletionVisitor = new AxiomQueryCompletionVisitor();
        axiomQueryCompletionVisitor.visit(axiomQuerySource.root());
        // last type in tree
        axiomQueryCompletionVisitor.getLastType();
        // last tokens in tree
        axiomQueryCompletionVisitor.getLastNode().getParent().getParent().getParent().getText();

        return null;
    }
}
