package com.evolveum.axiom.lang.antlr;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.misc.IntervalSet;

import java.util.*;

/**
 * Created by Dominik.
 */
public class AxiomQueryErrorStrategy extends DefaultErrorStrategy {

    private final ErrorTokenContext errorTokenContext = new ErrorTokenContext();

//     @Override
//    public void recover(Parser recognizer, RecognitionException e) {
//        ATN atn = recognizer.getATN();
//        Token errorToken = recognizer.getCurrentToken();
//        ATNState currentState = atn.states.get(e.getOffendingState());
//        errorTokenContext.setErrorToken(errorToken);
//        errorTokenContext.setRules(ATNTraverseHelper.findRuleContextByToken(currentState, errorToken));
//
//        if (e instanceof NoViableAltException noviable) {
//            Set<IntervalSet> exceptedTokens = new HashSet<>();
//
//            for (var deadEnd : noviable.getDeadEndConfigs().getStates()) {
//                var stateNumber = deadEnd.stateNumber;
//                var state = atn.states.get(stateNumber);
//
//                for (Transition t : state.getTransitions()) {
//                    if (t instanceof RuleTransition) {
//                        break;
//                    } else if (t instanceof PredicateTransition) {
//                        break;
//                    } else if (t instanceof WildcardTransition) {
//                        break;
//                    } else {
//                        if (!t.isEpsilon()) {
//                            exceptedTokens.add(t.label());
//                        }
//                    }
//                }
//            }
//            errorTokenContext.setExpectedTokens(exceptedTokens);
//        }
//
//        super.recover(recognizer, e);
//    }

    @Override
    public void reportError(Parser recognizer, RecognitionException e) {
        List<AxiomQueryError> errors = new ArrayList<>();
        errorTokenContext.setSyntaxErrors(errors);
        super.reportError(recognizer, e);
    }

    public ErrorTokenContext getErrorTokenContext() {
        return errorTokenContext;
    }

    public static class ErrorTokenContext {
        private Token errorToken;
        private List<AxiomQueryError> syntaxErrors = new ArrayList<>();
        private Set<IntervalSet> expectedTokens = new HashSet<IntervalSet>();
        private List<Integer> rules = new ArrayList<>();

        public Token getErrorToken() {
            return errorToken;
        }

        private void setErrorToken(Token errorToken) {
            this.errorToken = errorToken;
        }

        public List<AxiomQueryError> getSyntaxErrors() {
            return syntaxErrors;
        }

        private void setSyntaxErrors(List<AxiomQueryError> syntaxErrors) {
            this.syntaxErrors = syntaxErrors;
        }

        public Set<IntervalSet> getExpectedTokens() {
            return expectedTokens;
        }

        private void setExpectedTokens(Set<IntervalSet> expectedTokens) {
            this.expectedTokens = expectedTokens;
        }

        public List<Integer> getRules() {
            return rules;
        }

        private void setRules(List<Integer> rules) {
            this.rules = rules;
        }
    }
}
