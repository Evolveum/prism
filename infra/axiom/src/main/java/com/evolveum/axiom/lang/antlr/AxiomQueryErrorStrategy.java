package com.evolveum.axiom.lang.antlr;

import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.misc.IntervalSet;

import java.util.*;

/**
 * Created by Dominik.
 */
public class AxiomQueryErrorStrategy extends DefaultErrorStrategy {

    private final ErrorTokenContext errorTokenContext = new ErrorTokenContext();
    public IntervalSet recognitionsSet = null;


    @Override
    public void recover(Parser recognizer, RecognitionException e) {

        System.out.println("LDSLDL>>> " + recognizer.getRuleContext().getClass());

        recognitionsSet = recognizer.getATN().getExpectedTokens(recognizer.getState(), recognizer.getContext());

        // Get the resynchronization set
//        IntervalSet resyncSet = getResynchronizationSet(recognizer);

//        ATNTraverseHelper.findTokenContextByRecognizer(recognizer).forEach(s -> System.out.println("Resynchronization Set: " + s.token() + " - " + s.ruleContext()));

        // Analyze the rule context for each token in the resynchronization set
//        for (int tokenType : resyncSet.toArray()) {
//            String tokenName = recognizer.getVocabulary().getSymbolicName(tokenType);
//            RuleContext context = findRuleContextForToken(recognizer, tokenType);
//            System.out.println("Token: " + tokenName + ", Rule Context: " + AxiomQueryParser.ruleNames[context.getRuleIndex()]);
//        }

        // Call the default recovery method
        super.recover(recognizer, e);
    }

//    protected IntervalSet getResynchronizationSet(Parser recognizer) {
//        // This method retrieves the resynchronization set
//        return recognizer.getATN().getExpectedTokens(recognizer.getState(), recognizer.getContext());
//    }

//    protected RuleContext findRuleContextForToken(Parser recognizer, int tokenType) {
//        // Manually traverse the rule context to determine where this token might fit
//        RuleContext context = recognizer.getContext();
//
//        // Walk up the context to check where this token could belong
//        while (context != null) {
//            if (isTokenExpectedInRule(context, tokenType, recognizer)) {
//                return context;
//            }
//            context = context.getParent();
//        }
//
//        return null; // If no specific rule context found
//    }

//    protected boolean isTokenExpectedInRule(RuleContext context, int tokenType, Parser recognizer) {
//        // Check if the token type is expected in the current rule
//        IntervalSet expectedTokens = recognizer.getATN().getExpectedTokens(context.invokingState, context);
//        return expectedTokens.contains(tokenType);
//    }


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

    private void reportPossibleRules(Parser recognizer) {
        ATNState state = recognizer.getInterpreter().atn.states.get(recognizer.getState());

        Set<Integer> possibleRules = new HashSet<>();

        // Traverse possible transitions from the current state
        for (Transition transition : state.getTransitions()) {
            collectPossibleRules(transition.target, possibleRules);
        }

        // Print possible rules
        System.err.println("Possible rules at error location:");
        for (int ruleIndex : possibleRules) {
            System.err.println(recognizer.getRuleNames()[ruleIndex]);
        }
    }

    private void collectPossibleRules(ATNState state, Set<Integer> possibleRules) {

        Stack<Integer> stack = new Stack<>();

        if (state instanceof RuleStopState) {
            possibleRules.add(state.ruleIndex);
            return;
        }

        stack.push(state.stateNumber);

        for (Transition transition : state.getTransitions()) {
            if (!stack.contains(transition.target.stateNumber)) {
                collectPossibleRules(transition.target, possibleRules);
            }
        }
    }
}
