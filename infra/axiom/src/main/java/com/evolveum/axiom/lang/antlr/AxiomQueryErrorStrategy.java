package com.evolveum.axiom.lang.antlr;

import org.antlr.v4.runtime.*;
import java.util.*;

/**
 * Created by Dominik.
 */
public class AxiomQueryErrorStrategy extends DefaultErrorStrategy {

    public Map<ParserRuleContext, RecognitionsSet> recognitionsSet = new HashMap<>();

    @Override
    public void recover(Parser recognizer, RecognitionException e) {
        recognitionsSet.put(recognizer.getRuleContext(),
                new RecognitionsSet(
                        recognizer.getState(),
                        recognizer.getATN().getExpectedTokens(recognizer.getState(), recognizer.getContext())
                )
        );
        super.recover(recognizer, e);
    }

    @Override
    public Token recoverInline(Parser recognizer) throws RecognitionException {
        recognitionsSet.put(recognizer.getRuleContext(),
                new RecognitionsSet(
                        recognizer.getState(),
                        recognizer.getATN().getExpectedTokens(recognizer.getState(), recognizer.getContext())
                )
        );
        return super.recoverInline(recognizer);
    }

    @Override
    public void reportError(Parser recognizer, RecognitionException e) {
        List<AxiomQueryError> errors = new ArrayList<>();
        super.reportError(recognizer, e);
    }
}
