package com.evolveum.midpoint.prism.impl.query.lang;

import com.evolveum.axiom.lang.antlr.query.AxiomQueryLexer;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser;

import com.evolveum.axiom.reactor.Rule;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;

import java.util.*;

/**
 * Created by Dominik.
 */
public class AxiomQueryATNTraverse {

    AxiomQueryParser parser;

    public AxiomQueryATNTraverse(AxiomQueryParser parser, List<LangService> langServices) {
        this.parser = parser;
    }

    public boolean traverseATN(CommonTokenStream tokens) {
        Token token;
        int currentTokenIndex = 0;

        while ((token = tokens.get(currentTokenIndex)).getType() != AxiomQueryLexer.EOF) {
            // skip SEP token
            if (token.getType() != AxiomQueryLexer.SEP) {
                executeRuleATN(parser.getATN().ruleToStartState[0], token);
            }

            currentTokenIndex++;
        }

        return false;
    }

    private void executeRuleATN(ATNState startState, Token token) {
        Stack<ATNState> stateStack = new Stack<>();
        Stack<ATNState> alreadyPassed = new Stack<>();
        stateStack.push(startState);

        while (!stateStack.isEmpty()) {
            ATNState state = stateStack.pop();

            if (alreadyPassed.contains(state)) {
                stateStack.remove(state);
                continue;
            }

            for (Transition transition : state.getTransitions()) {
                if (transition instanceof RuleTransition ruleTransition) {
                    stateStack.push(ruleTransition.target);
                } else if (transition instanceof EpsilonTransition) {
                    stateStack.push(transition.target);
                } else if (transition instanceof NotSetTransition) {
                    // TODO
                } else if (transition instanceof AtomTransition || transition instanceof SetTransition) {
                    if (transition.label().contains(token.getType())) {
                        stateStack.push(transition.target);
                        stateStack.clear();
                    }
                } else if (transition instanceof WildcardTransition) {
                    // TODO
                }
            }

            alreadyPassed.add(state);
        }
    }
}
