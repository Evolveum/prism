package com.evolveum.midpoint.prism.impl.lex;

import com.evolveum.concepts.SourceLocation;
import com.evolveum.concepts.ValidationMessageType;
import com.evolveum.concepts.ValidationMessage;
import com.evolveum.midpoint.prism.ParsingContext;
import com.evolveum.midpoint.prism.impl.lex.dom.SaxElementHandler;
import com.evolveum.midpoint.prism.xnode.XNode;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Dominik.
 */
public class ValidatorUtil {

    public static final String SOURCE_LOCATION_OF_ELEMENT_KEY = "source_location";

    public static void registerRecord(ParsingContext parsingContext, ValidationMessageType type, String msg, String technicalMsg, SourceLocation sourceLocation) {
        if (parsingContext.isValidation()) {
            if (type == null) {
                type = ValidationMessageType.ERROR;
            }

            parsingContext.getValidationRecordList().add(new ValidationMessage(type, msg, technicalMsg, sourceLocation));
        }
    }

    public static void setPositionToXNode(ParsingContext parsingContext, XNode xNode, SourceLocation sourceLocation) {
        setPositionToXNode(parsingContext.isValidation(), xNode, sourceLocation);
    }

    public static void setPositionToXNode(boolean isValidation, XNode xNode, SourceLocation sourceLocation) {
        if (isValidation && xNode != null) {
            xNode.setSourceLocation(sourceLocation);
        }
    }

    public static Document saxParsing(InputStream is) throws ParserConfigurationException, SAXException, IOException {
        XMLReader reader = XMLReaderFactory.createXMLReader();
        SaxElementHandler elementHandler = new SaxElementHandler(SOURCE_LOCATION_OF_ELEMENT_KEY);
        reader.setContentHandler(elementHandler);
        reader.parse(new InputSource(is));

        return elementHandler.getDocument();
    }
}
