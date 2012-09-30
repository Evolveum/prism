package com.evolveum.midpoint.prism.query;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.PrismContainerDefinition;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObjectDefinition;
import com.evolveum.midpoint.prism.PrismPropertyValue;
import com.evolveum.midpoint.prism.PrismReferenceValue;
import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.prism.PropertyPath;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.exception.SchemaException;

public class RefFilter extends PropertyValueFilter{
	
	RefFilter(PropertyPath path, ItemDefinition definition, List<PrismReferenceValue> values) {
		super(path, definition, values);
		// TODO Auto-generated constructor stub
	}
		
	RefFilter(PropertyPath path, ItemDefinition definition, PrismReferenceValue value) {
		super(path, definition, value);
		// TODO Auto-generated constructor stub
	}
	
	RefFilter(PropertyPath path, ItemDefinition definition, Element expression) {
		super(path, definition, expression);
		// TODO Auto-generated constructor stub
	}
	
	public static RefFilter createReferenceEqual(PropertyPath path, ItemDefinition definition, List<PrismReferenceValue> values){
		return new RefFilter(path, definition, values);
	}
	
	public static RefFilter createReferenceEqual(PropertyPath path, ItemDefinition definition, Element expression){
		return new RefFilter(path, definition, expression);
	}
	
	public static RefFilter createReferenceEqual(PropertyPath path, ItemDefinition definition, PrismReferenceValue value){
		return new RefFilter(path, definition, value);
	}
	
	public static RefFilter createReferenceEqual(PropertyPath path, ItemDefinition item, String oid) {
		PrismReferenceValue value = new PrismReferenceValue(oid);
		return createReferenceEqual(path, item, value);
	}

	public static RefFilter createReferenceEqual(Class type, QName propertyName, PrismContext prismContext,
			String oid) throws SchemaException {
		PrismObjectDefinition objDef = prismContext.getSchemaRegistry().findObjectDefinitionByCompileTimeClass(type);
		return createReferenceEqual(null, objDef, propertyName, oid);

	}

	public static RefFilter createReferenceEqual(PropertyPath path, PrismContainerDefinition containerDef,
			QName propertyName, String realValue) throws SchemaException {
		ItemDefinition itemDef = containerDef.findItemDefinition(propertyName);
		if (itemDef == null) {
			throw new SchemaException("No definition for item " + propertyName + " in container definition "
					+ containerDef);
		}
		return createReferenceEqual(path, itemDef, realValue);
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
