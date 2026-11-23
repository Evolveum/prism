/*
 * Copyright (c) 2026 Evolveum and contributors
 *
 * This work is licensed under European Union Public License v1.2. See LICENSE file for details.
 *
 */
package com.evolveum.midpoint.prism.impl.lex;

import java.util.Arrays;
import java.util.Collection;
import javax.xml.namespace.QName;

import com.google.common.base.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.concepts.Argument;
import com.evolveum.concepts.Argument.ArgumentType;
import com.evolveum.concepts.SourceLocation;
import com.evolveum.concepts.TechnicalMessage;
import com.evolveum.concepts.ValidationLog;
import com.evolveum.concepts.ValidationLogType;
import com.evolveum.midpoint.prism.Definition;
import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.ParsingContext;
import com.evolveum.midpoint.prism.TypeDefinition;
import com.evolveum.midpoint.prism.impl.xnode.XNodeDefinition;
import com.evolveum.midpoint.prism.impl.xnode.XNodeImpl;
import com.evolveum.midpoint.prism.xnode.XNode;

/**
 * Builds validation logs for the parser. A log is created from one message template and its arguments,
 * so the message for the user and the message for technical processing cannot drift apart:
 * the former contains short renderings of the arguments, the latter the template and the values themselves.
 */
public class ValidatorUtil {

    public static final String SOURCE_LOCATION_OF_ELEMENT_KEY = "source_location";

    private static final String CANDIDATES_HINT =
            " can you clarify the definition based on the expected definitions from list: '%s'";

    public static void setPositionToXNode(ParsingContext parsingContext, XNode xNode, SourceLocation sourceLocation) {
        if (parsingContext.isValidation() && xNode != null) {
            xNode.setSourceLocation(sourceLocation);
        }
    }

    public static ValidationLog error(@Nullable SourceLocation location, @NotNull String template, Object... arguments) {
        return create(ValidationLogType.Specification.UNKNOWN, location, template, template, arguments, arguments);
    }

    /**
     * Error about an item or type that has no definition. Definitions that could have been meant are
     * provided for technical processing only, they are not part of the message for the user.
     */
    public static ValidationLog missingDefinition(@Nullable SourceLocation location,
            @Nullable Collection<? extends ItemDefinition<?>> candidates, @NotNull String template, Object... arguments) {
        if (candidates == null) {
            return create(ValidationLogType.Specification.MISSING_DEFINITION, location, template, template, arguments, arguments);
        }
        Object[] technicalArguments = Arrays.copyOf(arguments, arguments.length + 1);
        technicalArguments[arguments.length] = new Argument(candidates, ArgumentType.DEFINITION_LIST);
        return create(ValidationLogType.Specification.MISSING_DEFINITION, location,
                template, template + CANDIDATES_HINT, arguments, technicalArguments);
    }

    /** Creates argument with the type derived from the value, prism types included. */
    public static Argument argument(Object value) {
        if (value instanceof Argument argument) {
            return argument;
        } else if (value instanceof XNode) {
            return new Argument(value, ArgumentType.XNODE);
        } else if (value instanceof Definition || value instanceof XNodeDefinition) {
            return new Argument(value, ArgumentType.DEFINITION);
        } else if (value instanceof Collection<?> collection && isDefinitionList(collection)) {
            return new Argument(value, ArgumentType.DEFINITION_LIST);
        } else {
            return Argument.of(value);
        }
    }

    private static ValidationLog create(ValidationLogType.Specification specification, SourceLocation location,
            String template, String technicalTemplate, Object[] arguments, Object[] technicalArguments) {
        return new ValidationLog(
                ValidationLogType.ERROR,
                specification,
                location,
                new TechnicalMessage(technicalTemplate, toArguments(technicalArguments)),
                format(template, arguments));
    }

    private static Argument[] toArguments(Object[] values) {
        return Arrays.stream(values)
                .map(ValidatorUtil::argument)
                .toArray(Argument[]::new);
    }

    private static boolean isDefinitionList(Collection<?> collection) {
        return !collection.isEmpty() && collection.stream().allMatch(item -> item instanceof Definition);
    }

    /** Never fails, not even for wrong number of arguments. It is used when reporting another problem. */
    private static String format(String template, Object[] arguments) {
        if (arguments.length == 0) {
            return template;
        }
        Object[] rendered = Arrays.stream(arguments)
                .map(ValidatorUtil::render)
                .toArray();
        return Strings.lenientFormat(template, rendered);
    }

    /** Short form of the value, suitable for a message shown to the user. */
    private static String render(Object value) {
        if (value instanceof Argument argument) {
            return render(argument.value());
        } else if (value instanceof QName qName) {
            return qName.getLocalPart();
        } else if (value instanceof XNode) {
            return value instanceof XNodeImpl xNode && xNode.getElementName() != null
                    ? xNode.getElementName().getLocalPart()
                    : "node";
        } else if (value instanceof ItemDefinition<?> itemDefinition) {
            return itemDefinition.getItemName().getLocalPart();
        } else if (value instanceof TypeDefinition typeDefinition) {
            return typeDefinition.getTypeName().getLocalPart();
        } else if (value instanceof XNodeDefinition xNodeDefinition) {
            return xNodeDefinition.getName().getLocalPart();
        } else {
            return String.valueOf(value);
        }
    }
}
