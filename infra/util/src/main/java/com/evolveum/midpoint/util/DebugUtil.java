/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;

import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;

import static com.evolveum.midpoint.util.MiscUtil.emptyIfNull;

/**
 *
 * @author semancik
 */
public class DebugUtil {

    private static boolean detailedDebugDump = false;
    private static String prettyPrintBeansAs = null;

    public static boolean isDetailedDebugDump() {
        return detailedDebugDump;
    }

    public static boolean setDetailedDebugDump(boolean detailedDebugDump) {
        var previous = DebugUtil.detailedDebugDump;
        DebugUtil.detailedDebugDump = detailedDebugDump;
        return previous;
    }

    public static String getPrettyPrintBeansAs() {
        return prettyPrintBeansAs;
    }

    public static String getPrettyPrintBeansAs(String defaultLanguage) {
        return prettyPrintBeansAs != null ? prettyPrintBeansAs : defaultLanguage;
    }

    // Experimental. To be used e.g. in tests, for dumps to be easier to read. YAML looks like a good option here.
    // (It would be nice to serialize it with some 'no namespaces' option.)
    public static void setPrettyPrintBeansAs(String language) {
        DebugUtil.prettyPrintBeansAs = language;
    }

    public static String formatElementName(QName elementName) {
        if (elementName == null) {
            return "null";
        }
        if (detailedDebugDump) {
            return PrettyPrinter.prettyPrint(elementName);
        } else {
            return elementName.getLocalPart();
        }
    }

    public static String dump(DebugDumpable dumpable) {
        if (dumpable == null) {
            return "null";
        }
        return dumpable.debugDump();
    }

    public static String dump(Object object) {
        if (object == null) {
            return "null";
        }
        if (object instanceof DebugDumpable) {
            return ((DebugDumpable)object).debugDump();
        }
        if (object instanceof Map) {
            StringBuilder sb = new StringBuilder();
            debugDumpMapMultiLine(sb, (Map)object, 0);
            return sb.toString();
        }
        if (object instanceof Collection) {
            return debugDump((Collection)object);
        }
        return object.toString();
    }

    public static String debugDump(Collection<?> dumpables) {
        return debugDump(dumpables, 0);
    }

    public static String debugDump(Collection<?> dumpables, int indent) {
        return debugDump(dumpables, indent, true);
    }

    public static String debugDump(Collection<?> dumpables, int indent, boolean openCloseSymbols) {
        StringBuilder sb = new StringBuilder();
        debugDump(sb, dumpables, indent, openCloseSymbols);
        return sb.toString();
    }

    public static String debugDump(Map<?,?> dumpables, int indent) {
        StringBuilder sb = new StringBuilder();
        debugDumpMapMultiLine(sb, dumpables, indent, true);
        return sb.toString();
    }

    public static void debugDump(StringBuilder sb, Collection<?> dumpables, int indent, boolean openCloseSymbols) {
        debugDump(sb, dumpables, indent, openCloseSymbols, null);
    }

    public static void debugDump(StringBuilder sb, Collection<?> dumpables, int indent, boolean openCloseSymbols, String dumpSuffix) {
        if (dumpables == null) {
            return;
        }

        if (openCloseSymbols) {
            indentDebugDump(sb, indent);
            sb.append(getCollectionOpeningSymbol(dumpables));
            if (dumpSuffix != null) {
                sb.append(dumpSuffix);
            }
            sb.append("\n");
        }
        Iterator<?> iterator = dumpables.iterator();
        while (iterator.hasNext()) {
            Object item = iterator.next();
            if (item == null) {
                indentDebugDump(sb, indent + 1);
                sb.append("null");
            } else if (item instanceof DebugDumpable) {
                sb.append(((DebugDumpable)item).debugDump(indent + 1));
            } else {
                indentDebugDump(sb, indent + 1);
                sb.append(item.toString());
            }
            if (iterator.hasNext()) {
                sb.append("\n");
            }
        }
        if (openCloseSymbols) {
            if (!dumpables.isEmpty()) {
                sb.append("\n");
            }
            indentDebugDump(sb, indent);
            sb.append(getCollectionClosingSymbol(dumpables));
        }
    }

    public static String debugDump(DebugDumpable dd) {
        return debugDump(dd, 0);
    }

    public static String debugDump(DebugDumpable dd, int indent) {
        if (dd == null) {
            StringBuilder sb = new StringBuilder();
            indentDebugDump(sb, indent);
            sb.append("null");
            return sb.toString();
        } else {
            return dd.debugDump(indent);
        }
    }

    public static String debugDump(Object object, int indent) {
        if (object == null) {
            StringBuilder sb = new StringBuilder();
            indentDebugDump(sb, indent);
            sb.append("null");
            return sb.toString();
        }
        if (object instanceof DebugDumpable) {
            return ((DebugDumpable)object).debugDump(indent);
        } else if (object instanceof Map) {
            return debugDump((Map)object, indent);
        } else if (object instanceof Collection) {
            return debugDump((Collection<?>)object, indent);
        } else {
            StringBuilder sb = new StringBuilder();
            indentDebugDump(sb, indent);
            sb.append(PrettyPrinter.prettyPrint(object));
            return sb.toString();
        }
    }

    public static void debugDumpLabel(StringBuilder sb, String label, int indent) {
        indentDebugDump(sb, indent);
        sb.append(label).append(":");
    }

    public static void debugDumpLabelLn(StringBuilder sb, String label, int indent) {
        debugDumpLabel(sb, label, indent);
        sb.append("\n");
    }

    public static void debugDumpWithLabelLn(StringBuilder sb, String label, DebugDumpable dd, int indent) {
        debugDumpWithLabel(sb,label,dd,indent);
        sb.append("\n");
    }

    public static void debugDumpWithLabel(StringBuilder sb, String label, DebugDumpable dd, int indent) {
        debugDumpLabel(sb, label, indent);
        if (dd == null) {
            sb.append(" null");
        } else {
            sb.append("\n");
            sb.append(dd.debugDump(indent + 1));
        }
    }

    public static void debugDumpShortWithLabelLn(StringBuilder sb, String label, ShortDumpable sd, int indent) {
        debugDumpShortWithLabel(sb, label, sd, indent);
        sb.append("\n");
    }

    public static void debugDumpShortWithLabel(StringBuilder sb, String label, ShortDumpable sd, int indent) {
        debugDumpLabel(sb, label, indent);
        if (sd == null) {
            sb.append(" null");
        } else {
            sb.append(" ");
            sd.shortDump(sb);
        }
    }

    public static void debugDumpWithLabel(StringBuilder sb, String label, String val, int indent) {
        debugDumpLabel(sb, label, indent);
        sb.append(" ");
        sb.append(val);
    }

    public static void debugDumpWithLabelLn(StringBuilder sb, String label, String val, int indent) {
        debugDumpWithLabel(sb, label, val, indent);
        sb.append("\n");
    }

    public static void debugDumpWithLabelLn(StringBuilder sb, String label, Enum<?> val, int indent) {
        debugDumpWithLabelLn(sb, label, String.valueOf(val), indent);
    }

    public static void debugDumpWithLabel(StringBuilder sb, String label, Enum<?> val, int indent) {
        debugDumpWithLabel(sb, label, String.valueOf(val), indent);
    }

    public static void debugDumpWithLabel(StringBuilder sb, String label, QName val, int indent) {
        debugDumpLabel(sb, label, indent);
        sb.append(" ");
        sb.append(PrettyPrinter.prettyPrint(val));
    }

    public static void debugDumpWithLabelLn(StringBuilder sb, String label, QName val, int indent) {
        debugDumpWithLabel(sb, label, val, indent);
        sb.append("\n");
    }

    public static void debugDumpWithLabel(StringBuilder sb, String label, Boolean val, int indent) {
        debugDumpLabel(sb, label, indent);
        sb.append(" ");
        sb.append(val);
    }

    public static void debugDumpWithLabelLn(StringBuilder sb, String label, Boolean val, int indent) {
        debugDumpWithLabel(sb, label, val, indent);
        sb.append("\n");
    }

    public static void debugDumpWithLabel(StringBuilder sb, String label, Number val, int indent) {
        debugDumpLabel(sb, label, indent);
        sb.append(" ");
        sb.append(val);
    }

    public static void debugDumpWithLabelLn(StringBuilder sb, String label, Number val, int indent) {
        debugDumpWithLabel(sb, label, val, indent);
        sb.append("\n");
    }

    public static void debugDumpWithLabel(StringBuilder sb, String label, Class val, int indent) {
        debugDumpLabel(sb, label, indent);
        sb.append(" ");
        sb.append(val);
    }

    public static void debugDumpWithLabelLn(StringBuilder sb, String label, Class val, int indent) {
        debugDumpWithLabel(sb, label, val, indent);
        sb.append("\n");
    }

    public static void debugDumpWithLabel(StringBuilder sb, String label, Collection<?> values, int indent) {
        debugDumpLabel(sb, label, indent);
        if (values == null) {
            sb.append(" null");
        } else if (values.isEmpty()) {
            sb.append(" ");
            sb.append(getCollectionOpeningSymbol(values));
            sb.append(getCollectionClosingSymbol(values));
        } else {
            sb.append(" (").append(values.size()).append(")");
            sb.append("\n");
            debugDump(sb, values, indent + 1, false);
        }
    }

    public static void debugDumpWithLabelLn(StringBuilder sb, String label, Collection<?> values, int indent) {
        debugDumpWithLabel(sb, label, values, indent);
        sb.append("\n");
    }

    public static <K, V> void debugDumpWithLabel(StringBuilder sb, String label, Map<K, V> map, int indent) {
        debugDumpLabel(sb, label, indent);
        if (map == null) {
            sb.append(" null");
        } else {
            sb.append("\n");
            debugDumpMapMultiLine(sb, map, indent + 1);
        }
    }

    public static <K, V> void debugDumpWithLabelLn(StringBuilder sb, String label, Map<K, V> map, int indent) {
        debugDumpWithLabel(sb, label, map, indent);
        sb.append("\n");
    }

    public static void debugDumpWithLabelToString(StringBuilder sb, String label, Object object, int indent) {
        debugDumpLabel(sb, label, indent);
        if (object == null) {
            sb.append(" null");
        } else {
            sb.append(" ");
            sb.append(object);
        }
    }

    public static void debugDumpWithLabelToStringLn(StringBuilder sb, String label, Object object, int indent) {
        debugDumpWithLabelToString(sb, label, object, indent);
        sb.append("\n");
    }

    public static void debugDumpWithLabelShortDump(StringBuilder sb, String label, ShortDumpable object, int indent) {
        debugDumpLabel(sb, label, indent);
        if (object == null) {
            sb.append(" null");
        } else {
            sb.append(" ");
            object.shortDump(sb);
        }
    }

    public static void debugDumpWithLabelShortDumpLn(StringBuilder sb, String label, ShortDumpable object, int indent) {
        debugDumpWithLabelShortDump(sb, label, object, indent);
        sb.append("\n");
    }

    public static String getCollectionOpeningSymbol(Collection<?> col) {
        if (col instanceof List) {
            return "[";
        }
        if (col instanceof Set) {
            return "{";
        }
        return col.getClass().getSimpleName()+"(";
    }

    public static String getCollectionClosingSymbol(Collection<?> col) {
        if (col instanceof List) {
            return "]";
        }
        if (col instanceof Set) {
            return "}";
        }
        return ")";
    }

    public static void indentDebugDump(StringBuilder sb, int indent) {
        sb.append(
                DebugDumpable.INDENT_STRING.repeat(Math.max(0, indent)));
    }

    public static StringBuilder createIndentedStringBuilder(int indent) {
        StringBuilder sb = new StringBuilder();
        indentDebugDump(sb, indent);
        return sb;
    }

    public static StringBuilder createTitleStringBuilderLn(Class<?> titleClass, int indent) {
        StringBuilder sb = createTitleStringBuilder(titleClass, indent);
        sb.append("\n");
        return sb;
    }

    public static StringBuilder createTitleStringBuilder(Class<?> titleClass, int indent) {
        return createTitleStringBuilder(titleClass.getSimpleName(), indent);
    }

    public static StringBuilder createTitleStringBuilder(String label, int indent) {
        StringBuilder sb = createIndentedStringBuilder(indent);
        sb.append(label);
        return sb;
    }

    public static <K, V> String debugDumpMapMultiLine(Map<K, V> map) {
        StringBuilder sb = new StringBuilder();
        debugDumpMapMultiLine(sb, map, 0);
        return sb.toString();
    }

    public static <K, V> Object debugDumpMapMultiLineLazily(Map<K, V> map) {
        if (map == null) {
            return null;
        }
        return new Object() {
            @Override
            public String toString() {
                return debugDumpMapMultiLine(map);
            }
        };
    }

    public static <K, V> void debugDumpMapMultiLine(StringBuilder sb, Map<K, V> map, int indent) {
        debugDumpMapMultiLine(sb, map, indent, false);
    }

    public static <K, V> void debugDumpMapMultiLine(StringBuilder sb, Map<K, V> map, int indent, boolean openCloseSymbols) {
        debugDumpMapMultiLine(sb, map, indent, openCloseSymbols, null);
    }

    public static <K, V> void debugDumpMapMultiLine(StringBuilder sb, Map<K, V> map, int indent, boolean openCloseSymbols, String dumpSuffix) {
        int inindent = indent;
        if (openCloseSymbols) {
            indentDebugDump(sb,indent);
            sb.append("(");
            if (dumpSuffix != null) {
                sb.append(dumpSuffix);
            }
            sb.append("\n");
            inindent++;
        }
        Iterator<Entry<K, V>> i = map.entrySet().iterator();
        while (i.hasNext()) {
            Entry<K,V> entry = i.next();
            indentDebugDump(sb,inindent);
            sb.append(PrettyPrinter.prettyPrint(entry.getKey()));
            sb.append(" => ");
            V value = entry.getValue();
            if (value == null) {
                sb.append("null");
            } else if (value instanceof DebugDumpable) {
                sb.append("\n");
                sb.append(((DebugDumpable)value).debugDump(inindent+1));
            } else {
                sb.append(PrettyPrinter.prettyPrint(value));
            }
            if (i.hasNext()) {
                sb.append("\n");
            }
        }
        if (openCloseSymbols) {
            sb.append("\n");
            indentDebugDump(sb,indent);
            sb.append(")");
        }
    }

    public static <K, V> void debugDumpMapSingleLine(StringBuilder sb, Map<K, V> map, int indent) {
        Iterator<Entry<K, V>> i = map.entrySet().iterator();
        while (i.hasNext()) {
            Entry<K,V> entry = i.next();
            indentDebugDump(sb,indent);
            sb.append(PrettyPrinter.prettyPrint(entry.getKey()));
            sb.append(" => ");
            V value = entry.getValue();
            if (value == null) {
                sb.append("null");
            } else {
                sb.append(value);
            }
            if (i.hasNext()) {
                sb.append("\n");
            }
        }
    }

    public static <T> String valueAndClass(T value) {
        if (value == null) {
            return "null";
        }
        return value.getClass().getSimpleName()+":"+value.toString();
    }

    public static String formatDate(Long millis) {
        if (millis == null) {
            return "null";
        }
        Date date = new Date(millis);
        return PrettyPrinter.prettyPrint(date);
    }

    public static String excerpt(String input, int maxChars) {
        if (input == null) {
            return null;
        }
        int eolIndex = input.indexOf('\n');
        if (eolIndex >= 0) {
            maxChars = eolIndex;
        }
        if (input.length() <= maxChars) {
            return input;
        }
        return input.substring(0, maxChars)+"...";
    }

    public static String fixIndentInMultiline(int indent, String indentString, String s) {
        if (s == null) {
            return null;
        }
        int cr = s.indexOf('\r');
        int lf = s.indexOf('\n');
        String searchFor;
        if (cr < 0 && lf < 0) {
            return s;
        } else if (cr >= 0 && lf >= 0) {
            searchFor = "\r\n";
        } else if (cr >= 0 && lf < 0) {
            searchFor = "\r";
        } else {
            searchFor = "\n";
        }

        StringBuilder indentation = new StringBuilder();
        for(int i = 0; i < indent; i++) {
            indentation.append(indentString);
        }
        String indentationString = indentation.toString();

        return s.replace(searchFor, System.lineSeparator() + indentationString);
    }

    public static int estimateObjectSize(Serializable o) {
        if (o == null) {
            return 0;
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(o);
            oos.close();
            return baos.size();
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static void dumpObjectSizeEstimateLn(StringBuilder sb, String label, Serializable o, int indent) {
        dumpObjectSizeEstimate(sb, label, o, indent);
        sb.append("\n");
    }

    public static void dumpObjectSizeEstimate(StringBuilder sb, String label, Serializable o, int indent) {
        indentDebugDump(sb, indent);
        sb.append(label).append(": ");
        sb.append(estimateObjectSize(o));
    }

    public static String dumpObjectFieldsSizeEstimate(final Serializable o) {
        final StringBuilder sb = new StringBuilder();
        sb.append(o).append(": ").append(estimateObjectSize(o)).append("\n");
        ReflectionUtils.doWithFields(o.getClass(), new FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                int mods = field.getModifiers();
                if (Modifier.isStatic(mods) && Modifier.isFinal(mods)) {
                    return;
                }
                if (Modifier.isTransient(mods)) {
                    return;
                }
                field.setAccessible(true);
                sb.append("\n");
                DebugUtil.indentDebugDump(sb, 1);
                sb.append(field.getName());
                if (Modifier.isStatic(mods)) {
                    sb.append(" (static)");
                }
                sb.append(": ");
                Object value = field.get(o);
                if (value == null) {
                    sb.append("null");
                } else if (value instanceof Serializable) {
                    sb.append(estimateObjectSize((Serializable)value));
                } else {
                    sb.append("non-serializable ("+value.getClass()+")");
                }
            }
        });
        return sb.toString();
    }

    public static Object debugDumpLazily(DebugDumpable dumpable) {
        return debugDumpLazily(dumpable, 0);
    }

    public static Object debugDumpLazily(DebugDumpable dumpable, int indent) {
        return new Object() {
            @Override
            public String toString() {
                return debugDump(dumpable, indent);
            }
        };
    }

    public static Object toStringLazily(Object object) {
        if (object == null) {
            return null;
        }
        return new Object() {
            @Override
            public String toString() {
                return object.toString();
            }
        };
    }

    public static Object lazy(Supplier<Object> supplier) {
        return new Object() {
            @Override
            public String toString() {
                return String.valueOf(supplier.get());
            }
        };
    }

    public static Object debugDumpLazily(Collection<?> dumpables) {
        if (dumpables == null || dumpables.isEmpty()) {
            return dumpables;
        }
        return new Object() {
            @Override
            public String toString() {
                return debugDump(dumpables);
            }
        };
    }

    public static Object debugDumpLazily(Collection<?> dumpables, int indent) {
        return new Object() {
            @Override
            public String toString() {
                return debugDump(dumpables, indent, true);
            }
        };
    }

    public static Object debugDumpLazily(Map<?, ?> dumpables, int indent) {
        return new Object() {
            @Override
            public String toString() {
                return debugDump(dumpables, indent);
            }
        };
    }

    public static String shortDump(ShortDumpable sd) {
        if (sd == null) {
            return null;
        } else {
            return sd.shortDump();
        }
    }

    public static void shortDump(StringBuilder sb, ShortDumpable sd) {
        if (sd != null) {
            sd.shortDump(sb);
        }
    }

    public static void shortDump(StringBuilder sb, Collection<? extends ShortDumpable> sds) {
        if (sds == null) {
            return;
        }
        sb.append("[");
        Iterator<? extends ShortDumpable> iterator = sds.iterator();
        while (iterator.hasNext()) {
            ShortDumpable sd = iterator.next();
            if (sd == null) {
                sb.append("null");
            } else {
                sd.shortDump(sb);
            }
            if (iterator.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append("]");
    }

    public static Object shortDumpLazily(ShortDumpable dumpable) {
        if (dumpable == null) {
            return null;
        }
        return new Object() {
            @Override
            public String toString() {
                return dumpable.shortDump();
            }
        };
    }

    public static String shortenUrl(String prefix, String fullUrl) {
        if (fullUrl == null) {
            return null;
        }
        if (fullUrl.startsWith(prefix)) {
            return "..."+fullUrl.substring(prefix.length());
        } else {
            return fullUrl;
        }
    }

    public static void shortDumpAppendProperty(StringBuilder sb, String propName, Object propValue) {
        if (propValue != null) {
            sb.append(propName).append("=").append(propValue).append(",");
        }
    }

    public static void shortDumpRemoveLastComma(StringBuilder sb) {
        if (sb.length() == 0) {
            return;
        }
        char lastChar = sb.charAt(sb.length() - 1);
        if (lastChar == ',') {
            sb.setLength(sb.length() - 1);
        }
    }

    public static <T> void shortDumpCollectionPrettyPrintOptionalBrackets(StringBuilder sb, List<T> values) {
        if (values == null) {
            sb.append("null");
            return;
        }
        if (values.isEmpty()) {
            return;
        }
        if (values.size() == 1) {
            sb.append(PrettyPrinter.prettyPrint(values.get(0)));
            return;
        }
        sb.append("[");
        Iterator<T> iterator = values.iterator();
        while (iterator.hasNext()) {
            T value = iterator.next();
            sb.append(PrettyPrinter.prettyPrint(value));
            if (iterator.hasNext()) {
                sb.append(",");
            }
        }
        sb.append("]");
    }

    /**
     * Dumps a throwable. This is aligned with the need of operation result dumping, so it may not be universally
     * applicable. But it should be good enough.
     */
    public static void dumpThrowable(StringBuilder sb, String label, Throwable throwable, int indent, boolean printStackTrace) {
        indentDebugDump(sb, indent);
        sb.append(label);
        sb.append(throwable.getClass().getSimpleName());
        sb.append(":");
        sb.append(throwable.getMessage());
        sb.append("\n");
        if (printStackTrace) {
            dumpStackTrace(sb, throwable.getStackTrace(), indent + 2);
            dumpInnerCauses(sb, throwable.getCause(), indent + 1);
        }
    }

    private static void dumpStackTrace(StringBuilder sb, StackTraceElement[] stackTrace, int indent) {
        for (StackTraceElement stackTraceElement : stackTrace) {
            indentDebugDump(sb, indent);
            sb.append(stackTraceElement.toString());
            sb.append("\n");
        }
    }

    private static void dumpInnerCauses(StringBuilder sb, Throwable innerCause, int indent) {
        if (innerCause == null) {
            return;
        }
        indentDebugDump(sb, indent);
        sb.append("Caused by ");
        sb.append(innerCause.getClass().getName());
        sb.append(": ");
        sb.append(innerCause.getMessage());
        sb.append("\n");
        dumpStackTrace(sb, innerCause.getStackTrace(), indent + 1);
        dumpInnerCauses(sb, innerCause.getCause(), indent);
    }

    public static void toStringCollection(StringBuilder sb, Collection<?> values, int indent) {
        boolean first = true;
        for (Object value : emptyIfNull(values)) {
            if (first) {
                first = false;
            } else {
                sb.append('\n');
            }
            indentDebugDump(sb, indent);
            sb.append(value);
        }
    }

    public static @NotNull String toStringCollection(Collection<?> values, int indent) {
        StringBuilder sb = new StringBuilder();
        toStringCollection(sb, values, indent);
        return sb.toString();
    }

    public static @NotNull Object toStringCollectionLazy(Collection<?> values, int indent) {
        return lazy(() -> toStringCollection(values, indent));
    }

    /** Facilitates implementing common-case debugDump() method. Very primitive implementation for now. */
    public static String standardDebugDump(Object object, int indent, Object... params) {
        var sb = createTitleStringBuilder(object.getClass(), indent);
        for (int i = 0; i < params.length; i += 2) {
            Object label = params[i];
            Object value = params[i + 1];
            sb.append("\n");
            if (value instanceof DebugDumpable dumpable) {
                debugDumpWithLabel(sb, String.valueOf(label), dumpable, indent + 1);
            } else {
                debugDumpWithLabel(sb, String.valueOf(label), String.valueOf(value), indent + 1);
            }
        }
        return sb.toString();
    }
}
