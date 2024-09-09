package com.evolveum.axiom.lang.antlr;

import com.evolveum.axiom.lang.antlr.query.AxiomQueryLexer;

import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;

/**
 * Helpers methods for traversing ATN rule networks.
 *
 * Created by Dominik.
 */
public class ATNTraverseHelper {

    /**
     * Method find all possible following rules based on the cursor position.
     * @param atn rule network
     * @param positionCtx position node
     * @return rule index list
     */
    public static List<Integer> findFollowingRulesByPositionContext(ATN atn, PositionContext positionCtx) {
        List<Integer> rules = new ArrayList<>();
        int positionCtxIndex = positionCtx.cursorIndex();
        ParseTree positionNode = positionCtx.node();
        ParseTree node = positionNode.getChild(positionCtxIndex);

        if (node instanceof TerminalNode terminalNode) {
            // if position token is SEPARATOR then find following rule for previous token
            if (terminalNode.getSymbol().getType() == AxiomQueryLexer.SEP) {
                node = positionNode.getChild(positionCtxIndex - 1);
                if (node instanceof TerminalNode terminalNode2) {
                    node = terminalNode2.getParent();
                }
            } else {
                node = terminalNode.getParent();
            }
        }

        if (node instanceof RuleContext ruleContext) {
//            ParseTree currentNode = ruleContext.getChild(ruleContext.getChildCount() - 1);
            ParseTree currentNode = ruleContext;
            while (currentNode.getChildCount() > 0) {
                if (currentNode instanceof RuleContext currentRuleContext) {
                    findFollowingRulesInATN(atn, currentRuleContext, getTerminalNode(positionNode.getChild(positionCtxIndex)), rules);
                }
                currentNode = currentNode.getChild(currentNode.getChildCount() - 1);
            }
        }

        return rules;
    }

    public static List<TokenContextPair> findTokenContextByRecognizer(ATN atn, RecognitionsSet recognizer) {
        List<TokenContextPair> listTokenContextPair = new ArrayList<>();
        Stack<ATNState> states = new Stack<>();
        Stack<ATNState> passedStates = new Stack<>();
        ATNState nextState;

        for (Interval interval : recognizer.recognizedTokens().getIntervals()) {
            for (int i = interval.a; i <= interval.b; i++) {

                states.push(atn.states.get(recognizer.startState()));
                while (!states.isEmpty()) {
                    nextState = states.pop();
                    passedStates.push(nextState);
                    for (Transition transition : nextState.getTransitions()) {
                        // check looping
                        if (!passedStates.contains(transition.target)) {
                            states.push(transition.target);
                        }

                        if (transition instanceof RuleTransition ruleTransition) {
                            states.push(ruleTransition.target);
                            states.push(ruleTransition.followState);
                        } else if (transition instanceof AtomTransition atomTransition) {
                            if (atomTransition.label == i) {
                                listTokenContextPair.add(new TokenContextPair(
                                    i, nextState.ruleIndex
                                ));
                                states.clear();
                                passedStates.clear();
                            }
                        } else if (transition instanceof SetTransition setTransition) {
                            if (setTransition.label().contains(i)) {
                                listTokenContextPair.add(new TokenContextPair(
                                    i, nextState.ruleIndex
                                ));
                                states.clear();
                                passedStates.clear();
                            }
                        }
                    }
                }
            }
        }

        return listTokenContextPair;
    }

    private static void findFollowingRulesInATN(ATN atn, RuleContext ruleContext, TerminalNode nextTerminalNode, List<Integer> rules) {
        Stack<ATNState> states = new Stack<>();
        Stack<ATNState> passedStates = new Stack<>();
        ATNState nextState;
        int invokingState;

        if (ruleContext.invokingState == -1) {
            invokingState = 0;
        } else {
            invokingState = ruleContext.invokingState;
        }

        states.push(atn.states.get(invokingState));

        while (!states.isEmpty()) {
            nextState = states.pop();
            passedStates.push(nextState);

            if (nextState instanceof RuleStopState) {
                continue;
            }

            if (nextState instanceof BlockEndState endState) {
                if (findStartBlockStateItemFilterRule(atn.states, ruleContext.getRuleIndex(), invokingState) instanceof BasicBlockStartState startBlockState) {
                    if (startBlockState.stateNumber + 1 == endState.stateNumber) {
                        continue;
                    }
                }

//                Optional<ATNState> startState = passedStates.stream().filter(s -> s instanceof BasicBlockStartState).findFirst();
//                if (startState.isPresent()) {
//                    if (startState.get() instanceof BasicBlockStartState && startState.get().stateNumber == endState.stateNumber - 1) {
//                        continue;
//                    }
//                }
            }


            for (Transition transition : nextState.getTransitions()) {
                if (transition instanceof RuleTransition ruleTransition) {
                    states.add(ruleTransition.followState);
                    if (ruleTransition.ruleIndex != ruleContext.getRuleIndex()) {
                        rules.add(ruleTransition.ruleIndex);
                    }
                } else if (transition instanceof AtomTransition atomTransition) {
                    if (nextTerminalNode.getSymbol().getType() == atomTransition.label) {
                        states.push(atomTransition.target);
                    }
                } else {
                    // check looping
                    if (!passedStates.contains(transition.target)) {
                        states.push(transition.target);
                    }
                }
            }
        }
    }

    private static ParseTree findPreviousNode(ParseTree node) {
        if (node instanceof TerminalNode) {
            return null;
        }

        ParseTree parentNode;
        int count = node.getChildCount();

        while (count > 0) {
            node = node.getChild(count - 1);
            if (node instanceof TerminalNode terminalNode) {
                parentNode = terminalNode.getParent();
                while (parentNode.getChildCount() == 1) {
                    node = parentNode;
                    parentNode = node.getParent();
                    if (parentNode.getChildCount() > 1) {
                        return node;
                    }
                }
            }
            count = node.getChildCount();
        }
        return node;
    }

    private static TerminalNode getTerminalNode(ParseTree parseTree) {
        if (parseTree instanceof TerminalNode terminalNode) {
            return terminalNode;
        }

        if (parseTree != null) {
            while (parseTree.getChildCount() > 0) {
                parseTree = parseTree.getChild(parseTree.getChildCount() - 1);

                if (parseTree instanceof TerminalNode node) {
                    return node;
                }
            }
        }

        return null;
    }

    private static ATNState findStartBlockStateItemFilterRule(List<ATNState> states, int ruleCtx, int state) {
        // custom solution for find end block state in itemFilter rule
        if (states.get(state).ruleIndex == AxiomQueryParser.RULE_itemFilter) {
            if (ruleCtx == AxiomQueryParser.RULE_path || ruleCtx == AxiomQueryParser.RULE_negation) {
                return states.get(309);
            }
        }

//        Stack<BasicBlockStartState> blockStartStates = new Stack<>();
//        for (ATNState s : states) {
//            if (s instanceof BasicBlockStartState basicBlockStartState) {
//                    blockStartStates.add(basicBlockStartState);
//            }
//
//            if (s.stateNumber == state) {
//                return blockStartStates.peek();
//            }
//        }

        return null;
    }
}
