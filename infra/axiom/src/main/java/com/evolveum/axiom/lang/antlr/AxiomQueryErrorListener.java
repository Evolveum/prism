package com.evolveum.axiom.lang.antlr;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dominik.
 */
public class AxiomQueryErrorListener extends BaseErrorListener {

    public List<AxiomQueryError> errorList = new ArrayList<>();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
            int line, int charPositionInLine,
            String msg, RecognitionException e)
    {
        String sourceName = recognizer.getInputStream().getSourceName();

        if (!sourceName.isEmpty()) {
            sourceName = String.format("%s:%d:%d: ", sourceName, line, charPositionInLine);
        }

        errorList.add(new AxiomQueryError(recognizer, offendingSymbol, line, charPositionInLine, msg, e));
    }
}
