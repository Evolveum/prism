package com.evolveum.midpoint.prism.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.lang.Validate;
import org.w3c.dom.Element;

import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.PrismContainerDefinition;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObjectDefinition;
import com.evolveum.midpoint.prism.PrismPropertyValue;
import com.evolveum.midpoint.prism.PrismReferenceValue;
import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.prism.PropertyPath;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.Dumpable;
import com.evolveum.midpoint.util.exception.SchemaException;

public class EqualsFilter extends PropertyValueFilter {

	EqualsFilter(PropertyPath path, ItemDefinition definition, List<PrismValue> values) {
		super(path, definition, values);
	}

	EqualsFilter(PropertyPath path, ItemDefinition definition, PrismValue value) {
		super(path, definition, value);
	}
	
	EqualsFilter(PropertyPath path, ItemDefinition definition, Element expression) {
		super(path, definition, expression);
	}

	public static EqualsFilter createEqual(PropertyPath path, ItemDefinition itemDef, PrismValue value) {
		Validate.notNull(itemDef, "Item definition in the equals filter must not be null");
		return new EqualsFilter(path, itemDef, value);
	}

	public static EqualsFilter createEqual(PropertyPath path, ItemDefinition itemDef, List<PrismValue> values) {
		Validate.notNull(itemDef, "Item definition in the equals filter must not be null");
		return new EqualsFilter(path, itemDef, values);
	}

	public static EqualsFilter createEqual(PropertyPath path, ItemDefinition itemDef, Element expression) {
		Validate.notNull(itemDef, "Item definition in the equals filter must not be null");
		return new EqualsFilter(path, itemDef, expression);
	}

	public static EqualsFilter createEqual(PropertyPath path, ItemDefinition item, Object realValue) {

		if (List.class.isAssignableFrom(realValue.getClass())) {
			List<PrismValue> prismValues = new ArrayList<PrismValue>();
			for (Object o : (List) realValue) {
				if (o instanceof PrismPropertyValue) {
					prismValues.add((PrismPropertyValue) o);
				} else {
					PrismPropertyValue val = new PrismPropertyValue(o);
					prismValues.add(val);
				}
			}
			return createEqual(path, item, prismValues);
		}
		PrismPropertyValue value = new PrismPropertyValue(realValue);
		return createEqual(path, item, value);
	}

	
	public static EqualsFilter createEqual(PropertyPath path, PrismContainerDefinition containerDef,
			QName propertyName, PrismValue... values) throws SchemaException {
		ItemDefinition itemDef = containerDef.findItemDefinition(propertyName);
		if (itemDef == null) {
			throw new SchemaException("No definition for item " + propertyName + " in container definition "
					+ containerDef);
		}

		return createEqual(path, itemDef, values);
	}

	public static EqualsFilter createEqual(PropertyPath path, PrismContainerDefinition containerDef,
			QName propertyName, Object realValue) throws SchemaException {
		ItemDefinition itemDef = containerDef.findItemDefinition(propertyName);
		if (itemDef == null) {
			throw new SchemaException("No definition for item " + propertyName + " in container definition "
					+ containerDef);
		}

		return createEqual(path, itemDef, realValue);
	}

	public static EqualsFilter createEqual(Class type, PrismContext prismContext, QName propertyName, Object realValue)
			throws SchemaException {
		PrismObjectDefinition objDef = prismContext.getSchemaRegistry().findObjectDefinitionByCompileTimeClass(type);
		return createEqual(null, objDef, propertyName, realValue);
	}

	@Override
	public String dump() {
		return debugDump(0);
	}

	@Override
	public String debugDump() {
		return debugDump(0);
	}

	@Override
	public String debugDump(int indent) {
		StringBuilder sb = new StringBuilder();
		DebugUtil.indentDebugDump(sb, indent);
		sb.append("EQUALS: \n");
		
		if (getPath() != null){
			DebugUtil.indentDebugDump(sb, indent+1);
			sb.append("PATH: ");
			sb.append(getPath().toString());
			sb.append("\n");
		} 
		DebugUtil.indentDebugDump(sb, indent+1);
		sb.append("DEF: ");
		if (getDefinition() != null) {
			sb.append(getDefinition().debugDump(indent));
			sb.append("\n");
		} else {
			DebugUtil.indentDebugDump(sb, indent);
			sb.append("null\n");
		}
		DebugUtil.indentDebugDump(sb, indent+1);
		sb.append("VALUE: ");
		if (getValues() != null) {
			indent += 1;
			for (PrismValue val : getValues()) {
				sb.append(val.debugDump(indent));
			}
		} else {
			DebugUtil.indentDebugDump(sb, indent);
			sb.append("null\n");
		}
		return sb.toString();
	}

}
