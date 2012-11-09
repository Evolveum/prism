/*
 * Copyright (c) 2011 Evolveum
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1 or
 * CDDLv1.0.txt file in the source code distribution.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 *
 * Portions Copyrighted 2011 [name of copyright owner]
 * Portions Copyrighted 2010 Forgerock
 */

package com.evolveum.midpoint.util;

import java.util.*;
import java.util.Map.Entry;

/**
 *
 * @author semancik
 */
public class DebugUtil {

	private static int SHOW_LIST_MEMBERS = 3;
	public static String dump(Dumpable dumpable) {
		if (dumpable == null) {
			return "null";
		}
		return dumpable.dump();
	}
	
	public static String dump(Object object) {
		if (object == null) {
			return "null";
		}
		if (object instanceof Dumpable) {
			return ((Dumpable)object).dump();
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
		return debugDump(dumpables,0);
	}

	public static String debugDump(Collection<?> dumpables, int indent) {
		StringBuilder sb = new StringBuilder();
		debugDump(sb, dumpables, indent, true);
		return sb.toString();
	}
	
	public static void debugDump(StringBuilder sb, Collection<?> dumpables, int indent, boolean openCloseSymbols) {
		if (openCloseSymbols) {
			indentDebugDump(sb, indent);
			sb.append(getCollectionOpeningSymbol(dumpables));
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
	
	public static String debugDump(Object object, int indent) {
		if (object instanceof DebugDumpable) {
			return ((DebugDumpable)object).debugDump(indent);
		} else if (object instanceof Collection) {
			return debugDump((Collection<?>)object, indent);
		} else {
			StringBuilder sb = new StringBuilder();
			indentDebugDump(sb, indent + 1);
			sb.append(object.toString());
			return sb.toString();
		}
	}
	
	public static void debugDumpLabel(StringBuilder sb, String label, int indent) {
		indentDebugDump(sb, indent);
		sb.append(label).append(":");
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
	
	public static <K, V extends DebugDumpable> void debugDumpWithLabel(StringBuilder sb, String label, Map<K, V> map, int indent) {
		debugDumpLabel(sb, label, indent);
		if (map == null) {
			sb.append(" null");
		} else {
			sb.append("\n");
			debugDumpMapMultiLine(sb, map, indent + 1);
		}
	}
	
	public static void debugDumpWithLabelToString(StringBuilder sb, String label, Object object, int indent) {
		debugDumpLabel(sb, label, indent);
		if (object == null) {
			sb.append(" null");
		} else {
			sb.append(" ");
			sb.append(object.toString());
		}
	}
	
	public static void debugDumpWithLabelToStringLn(StringBuilder sb, String label, Object object, int indent) {
		debugDumpWithLabelToString(sb, label, object, indent);
		sb.append("\n");
	}

	public static String debugDumpXsdAnyProperties(Collection<?> xsdAnyCollection, int indent) {
		StringBuilder sb = new StringBuilder();
		indentDebugDump(sb, indent);
		sb.append(getCollectionOpeningSymbol(xsdAnyCollection));
		for (Object element : xsdAnyCollection) {
			sb.append("\n");
			indentDebugDump(sb, indent+1);
			sb.append(PrettyPrinter.prettyPrintElementAsProperty(element));
		}
		sb.append("\n");
		indentDebugDump(sb, indent);
		sb.append(getCollectionClosingSymbol(xsdAnyCollection));
		return sb.toString();
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
		for(int i = 0; i < indent; i++) {
			sb.append(DebugDumpable.INDENT_STRING);
		}
	}

	public static <K, V> void debugDumpMapMultiLine(StringBuilder sb, Map<K, V> map, int indent) {
		Iterator<Entry<K, V>> i = map.entrySet().iterator();
		while (i.hasNext()) {
			Entry<K,V> entry = i.next();
			indentDebugDump(sb,indent);
			sb.append(PrettyPrinter.prettyPrint(entry.getKey()));
			sb.append(" => ");
			V value = entry.getValue();
			if (value == null) {
				sb.append("null");
			} else if (value instanceof DebugDumpable) {
				sb.append("\n");
				sb.append(((DebugDumpable)value).debugDump(indent+1));
			} else {
				sb.append(value);
			}
			if (i.hasNext()) {
				sb.append("\n");
			}
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

//	public static String prettyPrint(ObjectType object, boolean showContent) {
//
//		if (object instanceof AccountShadowType) {
//			return prettyPrint((AccountShadowType) object, showContent);
//		}
//
//		if (object == null) {
//			return "null";
//		}
//
//		StringBuilder sb = new StringBuilder();
//		sb.append(object.getClass().getSimpleName());
//		sb.append("(");
//		sb.append(object.getOid());
//		sb.append(",");
//		sb.append(object.getName());
//
//		if (showContent) {
//			// This is just a fallback. Methods with more specific signature
//			// should be used instead
//			for (PropertyDescriptor desc : PropertyUtils.getPropertyDescriptors(object)) {
//				if (!"oid".equals(desc.getName()) && !"name".equals(desc.getName())) {
//					try {
//						Object value = PropertyUtils.getProperty(object, desc.getName());
//						sb.append(desc.getName());
//						sb.append("=");
//						sb.append(value);
//						sb.append(",");
//					} catch (IllegalAccessException ex) {
//						sb.append(desc.getName());
//						sb.append(":");
//						sb.append(ex.getClass().getSimpleName());
//						sb.append(",");
//					} catch (InvocationTargetException ex) {
//						sb.append(desc.getName());
//						sb.append(":");
//						sb.append(ex.getClass().getSimpleName());
//						sb.append(",");
//					} catch (NoSuchMethodException ex) {
//						sb.append(desc.getName());
//						sb.append(":");
//						sb.append(ex.getClass().getSimpleName());
//						sb.append(",");
//					}
//				}
//			}
//		}
//		sb.append(")");
//		return sb.toString();
//	}
//	
//	public static String prettyPrint(ProtectedStringType protectedStringType) {
//		if (protectedStringType == null) {
//			return "null";
//		}
//		StringBuilder sb = new StringBuilder("ProtectedStringType(");
//		
//		if (protectedStringType.getEncryptedData() != null) {
//			sb.append("[encrypted data]");
//		}
//
//		if (protectedStringType.getClearValue() != null) {
//			sb.append("\"");
//			sb.append(protectedStringType.getClearValue());
//			sb.append("\"");
//		}
//
//		sb.append(")");
//		return sb.toString();
//	}


}
