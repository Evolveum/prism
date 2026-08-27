/*
 * Copyright (c) 2010-2017 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.util;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.util.DebugDumpable;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.PrettyPrinter;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.logging.LoggingUtils;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.prism.xml.ns._public.types_3.DeltaSetTripleType;
import com.evolveum.prism.xml.ns._public.types_3.ItemDeltaType;
import com.evolveum.prism.xml.ns._public.types_3.ItemType;
import com.evolveum.prism.xml.ns._public.types_3.ModificationTypeType;
import com.evolveum.prism.xml.ns._public.types_3.ObjectDeltaType;
import com.evolveum.prism.xml.ns._public.types_3.RawType;

import jakarta.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PrismPrettyPrinter {

    private static final Trace LOGGER = TraceManager.getTrace(PrismPrettyPrinter.class);
    private static final String CRLF_REGEX = "(\\r|\\n|\\r\\n)+";
    private static final Pattern CRLF_PATTERN = Pattern.compile(CRLF_REGEX);

    public static String prettyPrint(RawType raw) {
        if (raw.getAlreadyParsedValue() != null) {
            return PrettyPrinter.prettyPrint(raw.getAlreadyParsedValue());
        }
        if (raw.getXnode() != null) {
            try {
                String jsonText = PrismContext.get().jsonSerializer().serialize(raw.getRootXNode(new QName("value")));
                return CRLF_PATTERN.matcher(jsonText).replaceAll("");
            } catch (Throwable t) {
                LoggingUtils.logException(LOGGER, "Couldn't serialize raw value for pretty printing, using 'toString' instead: {}", t, raw.getXnode());
            }
        }
        return PrettyPrinter.prettyPrint(raw.getXnode());
    }

    // TODO deduplicate this with prettyPrintForReport in ReportUtils
    public static String prettyPrint(PrismPropertyValue<?> ppv) {
        String retPPV;
        try {
            retPPV = PrettyPrinter.prettyPrint(ppv.getValue());
        } catch (Throwable t) {
            return "N/A"; // rare case e.g. for password-type in resource
        }
        return retPPV;
    }

    public static String prettyPrint(PrismContainerValue<?> pcv) {
        return pcv.getItems().stream()
                .map(item -> PrettyPrinter.prettyPrint(item))
                .collect(Collectors.joining(", "));
    }

    public static String prettyPrint(Item<?, ?> item) {
        String values = item.getValues().stream()
                .map(value -> PrettyPrinter.prettyPrint(value))
                .collect(Collectors.joining(", "));
        return PrettyPrinter.prettyPrint(item.getElementName()) + "={" + values + "}";
    }

    public static String prettyPrint(PrismReferenceValue prv) {
        return prettyPrint(prv, true);
    }

    public static String prettyPrint(PrismReferenceValue prv, boolean showType) {
        StringBuilder sb = new StringBuilder();
        if (showType) {
            sb.append(PrettyPrinter.prettyPrint(prv.getTargetType()));
            sb.append(": ");
        }
        if (prv.getTargetName() != null) {
            sb.append(prv.getTargetName());
        } else {
            sb.append(prv.getOid());
        }
        return sb.toString();
    }

    public static String prettyPrint(ObjectDeltaType deltaType) {
        if (deltaType == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("ObjectDeltaType(");
        sb.append(deltaType.getOid()).append(" ");
        sb.append(deltaType.getChangeType());
        sb.append(": ");
        if (deltaType.getObjectToAdd() != null) {
            sb.append(deltaType.getObjectToAdd());
        } else {
            sb.append("[");
            Iterator<ItemDeltaType> iterator = deltaType.getItemDelta().iterator();
            while (iterator.hasNext()) {
                ItemDeltaType itemDelta = iterator.next();
                shortPrettyPrint(sb, itemDelta);
                if (iterator.hasNext()) {
                    sb.append(", ");
                }
            }
            sb.append("]");
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * Formats a single item delta, e.g. {@code organizationalUnit: - guild (old: workshop, guild)}.
     */
    public static String prettyPrint(ItemDeltaType deltaType) {
        if (deltaType == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        if (deltaType.getPath() != null && !deltaType.getPath().getItemPath().isEmpty()) {
            sb.append(deltaType.getPath()).append(": ");
        }
        appendModificationSymbol(sb, deltaType.getModificationType());
        if (!deltaType.getValue().isEmpty()) {
            sb.append(' ');
            appendValues(sb, deltaType.getValue());
        }
        if (!deltaType.getEstimatedOldValue().isEmpty()) {
            sb.append(" (old: ");
            Iterator<RawType> iterator = deltaType.getEstimatedOldValue().iterator();
            while (iterator.hasNext()) {
                iterator.next().shortDump(sb);
                if (iterator.hasNext()) {
                    sb.append(", ");
                }
            }
            sb.append(')');
        }
        return sb.toString();
    }

    public static String prettyPrint(ItemType itemType) {
        if (itemType == null) {
            return "null";
        }
        String values = itemType.getValue().stream()
                .map(PrismPrettyPrinter::prettyPrintValue)
                .collect(Collectors.joining(", "));
        if (itemType.getName() == null) {
            return values;
        }
        return itemType.getName().getLocalPart() + ": " + values;
    }

    public static String prettyPrint(DeltaSetTripleType triple) {
        if (triple == null) {
            return "null";
        }
        List<String> components = new ArrayList<>();
        addSet(components, "Plus", triple.getPlus());
        addSet(components, "Minus", triple.getMinus());
        addSet(components, "Zero", triple.getZero());
        return String.join("; ", components);
    }

    private static void addSet(List<String> components, String label, List<Object> values) {
        if (!values.isEmpty()) {
            components.add(label + ": " + values.stream()
                    .map(PrismPrettyPrinter::prettyPrintValue)
                    .collect(Collectors.joining(", ")));
        }
    }

    /**
     * Renders a single value of {@link ItemType} or {@link DeltaSetTripleType}. Such a value
     * may be a {@link RawType} (in that case the parsed real value is used) or a plain real
     * value. Note that {@link PrettyPrinter#prettyPrint(Object)} cannot be used directly on
     * a {@link RawType} holding a parsed value, because the printer registry matches printers
     * by exact class and the parsed value is wrapped in a {@link com.evolveum.midpoint.prism.PrismPropertyValue}
     * implementation whose fallback is its "toString".
     */
    private static String prettyPrintValue(Object value) {
        if (value instanceof RawType raw) {
            PrismValue parsed = raw.getAlreadyParsedValue();
            if (parsed != null) {
                return PrettyPrinter.prettyPrint(parsed.getRealValueOrRawType());
            }
        }
        return PrettyPrinter.prettyPrint(value);
    }

    private static void appendModificationSymbol(StringBuilder sb, ModificationTypeType modificationType) {
        if (modificationType == ModificationTypeType.ADD) {
            sb.append('+');
        } else if (modificationType == ModificationTypeType.DELETE) {
            sb.append('-');
        } else if (modificationType == ModificationTypeType.REPLACE) {
            sb.append('=');
        }
    }

    private static void appendValues(StringBuilder sb, List<RawType> values) {
        if (values.isEmpty()) {
            sb.append("[]");
        } else if (values.size() == 1) {
            values.get(0).shortDump(sb);
        } else {
            sb.append("[");
            Iterator<RawType> iterator = values.iterator();
            while (iterator.hasNext()) {
                iterator.next().shortDump(sb);
                if (iterator.hasNext()) {
                    sb.append(", ");
                }
            }
            sb.append("]");
        }
    }

    private static void shortPrettyPrint(StringBuilder sb, ItemDeltaType deltaType) {
        ModificationTypeType modificationType = deltaType.getModificationType();
        if (modificationType == ModificationTypeType.ADD) {
            sb.append("(+)");
        } else if (modificationType == ModificationTypeType.DELETE) {
            sb.append("(-)");
        } else if (modificationType == ModificationTypeType.REPLACE) {
            sb.append("(=)");
        }
        sb.append(deltaType.getPath());
        sb.append(": ");
        appendValues(sb, deltaType.getValue());
    }

    public static String debugDump(ObjectDeltaType deltaType, int indent) {
        StringBuilder sb = DebugUtil.createTitleStringBuilder(ObjectDeltaType.class, indent);
        if (deltaType == null) {
            sb.append("null");
            return sb.toString();
        }
        sb.append(deltaType.getOid()).append(" ");
        sb.append(deltaType.getChangeType());
        if (deltaType.getObjectToAdd() != null) {
            sb.append("\n");
            sb.append(deltaType.getObjectToAdd().asPrismObject().debugDump(indent + 1));
        } else {
            Iterator<ItemDeltaType> iterator = deltaType.getItemDelta().iterator();
            while (iterator.hasNext()) {
                sb.append("\n");
                ItemDeltaType itemDelta = iterator.next();
                DebugUtil.indentDebugDump(sb, indent + 1);
                shortPrettyPrint(sb, itemDelta);
            }
        }
        return sb.toString();
    }

    static {
        PrettyPrinter.registerPrettyPrinter(PrismPrettyPrinter.class);
    }

    public static void initialize() {
        // nothing to do here, we just make sure static initialization will take place
    }

    public static String debugDumpValue(int indent, Object value, QName elementName, String defaultLanguage) {
        StringBuilder sb = new StringBuilder();
        DebugUtil.indentDebugDump(sb, indent);
        debugDumpValue(sb, indent, value, elementName, defaultLanguage);
        return sb.toString();
    }

    // TODO a better place? cannot be in DebugUtil, because of the missing dependency on prismContext
    // Note that expectedIndent applies only to lines after the first one. The caller is responsible for preparing
    // indentation for the first line.
    public static void debugDumpValue(StringBuilder sb, int expectedIndent, Object value, QName elementName, String defaultLanguage) {
        if (value instanceof DebugDumpable dumpable) {
            sb.append(dumpable.debugDump(expectedIndent));
            return;
        }
        String formatted;
        String language = DebugUtil.getPrettyPrintBeansAs() != null ? DebugUtil.getPrettyPrintBeansAs() : defaultLanguage;
        if (elementName == null) {
            elementName = new QName("value");
        }
        if (language != null && value != null && !(value instanceof Enum)
                && value.getClass().getAnnotation(XmlType.class) != null) {
            try {
                formatted = PrismContext.get().serializerFor(language).serializeRealValue(value, elementName);
            } catch (SchemaException e) {
                formatted = PrettyPrinter.prettyPrint(value);
            }
        } else {
            formatted = PrettyPrinter.prettyPrint(value);
        }
        sb.append(DebugUtil.fixIndentInMultiline(expectedIndent, DebugDumpable.INDENT_STRING, formatted));
    }
}
