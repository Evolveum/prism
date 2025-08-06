package com.evolveum.midpoint.prism.impl.lex;

import com.evolveum.concepts.SourceLocation;
import com.evolveum.midpoint.prism.ParsingContext;
import com.evolveum.midpoint.prism.xnode.XNode;

/**
 * Created by Dominik.
 */
public class ValidatorUtil {

    public static final String SOURCE_LOCATION_OF_ELEMENT_KEY = "source_location";

    public static void setPositionToXNode(ParsingContext parsingContext, XNode xNode, SourceLocation sourceLocation) {
        setPositionToXNode(parsingContext.isValidation(), xNode, sourceLocation);
    }

    public static void setPositionToXNode(boolean isValidation, XNode xNode, SourceLocation sourceLocation) {
        if (isValidation && xNode != null) {
            xNode.setSourceLocation(sourceLocation);
        }
    }
}
