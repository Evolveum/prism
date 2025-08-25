package com.evolveum.midpoint.prism.impl.lex;

import com.evolveum.concepts.SourceLocation;
import com.evolveum.midpoint.prism.ParsingContext;
import com.evolveum.midpoint.prism.TypeDefinition;
import com.evolveum.midpoint.prism.impl.xnode.XNodeDefinition;
import com.evolveum.midpoint.prism.impl.xnode.XNodeImpl;
import com.evolveum.midpoint.prism.xnode.XNode;

import javax.xml.namespace.QName;

/**
 * Created by Dominik.
 */
public class ValidatorUtil {

    public static final String SOURCE_LOCATION_OF_ELEMENT_KEY = "source_location";

    public static void setPositionToXNode(ParsingContext parsingContext, XNode xNode, SourceLocation sourceLocation) {
        if (parsingContext.isValidation() && xNode != null) {
            xNode.setSourceLocation(sourceLocation);
        }
    }

    public static String objectToString(Object object) {
        if (object instanceof QName qName) {
            return qName.getLocalPart();
        } else if (object instanceof XNodeDefinition xNodeDefinition) {
            return xNodeDefinition.getName().getLocalPart();
        }  else if (object instanceof XNodeImpl xNodeImpl) {
            return xNodeImpl.getElementName() != null ? xNodeImpl.getElementName().getLocalPart() : "";
        } else if (object instanceof XNode xNode) {
            return xNode.getTypeQName() != null ? xNode.getTypeQName().getLocalPart() : "";
        } else if (object instanceof TypeDefinition typeDefinition) {
            return typeDefinition.getTypeName().getLocalPart();
        } else {
            return "";
        }
    }
}
