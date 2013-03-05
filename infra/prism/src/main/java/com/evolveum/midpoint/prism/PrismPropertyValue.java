/*
 * Copyright (c) 2012 Evolveum
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
 * Portions Copyrighted 2012 [name of copyright owner]
 */

package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.prism.dom.ElementPrismPropertyImpl;
import com.evolveum.midpoint.prism.dom.PrismDomProcessor;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.prism.polystring.PolyStringNormalizer;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.DebugDumpable;
import com.evolveum.midpoint.util.Dumpable;
import com.evolveum.midpoint.util.PrettyPrinter;
import com.evolveum.midpoint.util.exception.SchemaException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.w3c.dom.Element;

/**
 * @author lazyman
 */
public class PrismPropertyValue<T> extends PrismValue implements Dumpable, DebugDumpable, Serializable {

    private T value;
    // The rawElement is set during a schema-less parsing, e.g. during a dumb JAXB parsing or XML parsing without a
    // definition.
    // We can't do anything smarter, as we don't have definition nor prism context. So we store the raw
    // elements here and process them later (e.g. during applyDefinition or getting a value with explicit type).
    private Object rawElement;

    public PrismPropertyValue(T value) {
        this(value, null, null);
    }

    public PrismPropertyValue(T value, OriginType type, Objectable source) {
    	super(type,source);
        if (value instanceof PrismPropertyValue) {
            throw new IllegalArgumentException("Probably problem somewhere, encapsulating property " +
                    "value object to another property value.");
        }
        this.value = value;
    }

    public void setValue(T value) {
        this.value = value;
        // To make sure there is no stale value
        clearDomElement();
    }

    public T getValue() {
    	if (rawElement != null) {
    		ItemDefinition def = null;
    		Itemable parent = getParent();
    		if (parent != null && parent.getDefinition() != null) {
    			def = getParent().getDefinition();
    		}
    		if (def == null) {
        		// We are weak now. If there is no better definition for this we assume a default definition and process
        		// the attribute now. But we should rather do this: TODO:
        		// throw new IllegalStateException("Attempt to get value withot a type from raw value of property "+getParent());
    			if (parent != null && parent.getPrismContext() != null) {
    				def = SchemaRegistry.createDefaultItemDefinition(parent.getName(), parent.getPrismContext());
    			} else {
    				if (rawElement instanceof Element) {
        				// Do the most stupid thing possible. Assume string value. And there will be no definition.
    					value = (T) ((Element)rawElement).getTextContent();
    				} else {
    					throw new IllegalStateException("No parent or prism context in property value "+this+", cannot create default definition." +
    							"The element is also not a DOM element but it is "+rawElement.getClass()+". Epic fail.");
    				}
    			}
    		}
    		if (def != null) {
				try {
					applyDefinition(def);
				} catch (SchemaException e) {
					throw new IllegalStateException(e.getMessage(),e);
				}
    		}
    	}
        return value;
    }
    
    public static <T> Collection<T> getValues(Collection<PrismPropertyValue<T>> pvals) {
    	Collection<T> realValues = new ArrayList<T>(pvals.size());
		for (PrismPropertyValue<T> pval: pvals) {
			realValues.add(pval.getValue());
		}
		return realValues;
    }
    
    public Object getRawElement() {
		return rawElement;
	}

	public void setRawElement(Object rawElement) {
		this.rawElement = rawElement;
	}
	
	@Override
	public boolean isRaw() {
		return rawElement != null;
	}
	
	@Override
	public void applyDefinition(ItemDefinition definition) throws SchemaException {
		if (definition != null && rawElement !=null) {
			value = parseRawElementToNewRealValue(this, (PrismPropertyDefinition) definition);
			rawElement = null;
		}
	}
	
	@Override
	public void applyDefinition(ItemDefinition definition, boolean force) throws SchemaException {
		applyDefinition(definition);
	}

	@Override
	public void recompute(PrismContext prismContext) {
		if (isRaw()) {
			return;
		}
		T realValue = getValue();
		if (realValue == null) {
			return;
		}
		// TODO: switch to Recomputable interface instead of PolyString
		if (realValue instanceof PolyString && prismContext != null) {
			PolyStringNormalizer polyStringNormalizer = prismContext.getDefaultPolyStringNormalizer();
			if (polyStringNormalizer != null) {
				((PolyString)realValue).recompute(polyStringNormalizer);
			}
		}
	}

	@Override
	public Object find(ItemPath path) {
		if (path == null || path.isEmpty()) {
			return this;
		}
		T value = getValue();
		if (value instanceof Structured) {
			return ((Structured)value).resolve(path);
		} else {
			throw new IllegalArgumentException("Attempt to resolve sub-path '"+path+"' on non-structured property value "+value);
		}
	}

	@Override
	public <X extends PrismValue> PartiallyResolvedValue<X> findPartial(ItemPath path) {
		if (path == null || path.isEmpty()) {
			return new PartiallyResolvedValue<X>((Item<X>)getParent(), null);
		}
		T value = getValue();
		if (value instanceof Structured) {
			return new PartiallyResolvedValue<X>((Item<X>)getParent(), path);
		} else {
			throw new IllegalArgumentException("Attempt to resolve sub-path '"+path+"' on non-structured property value "+value);
		}
	}

	@Override
	protected Element createDomElement() {
		return new ElementPrismPropertyImpl<T>(this);
	}
	
    @Override
	public void checkConsistenceInternal(Itemable rootItem, ItemPath parentPath, boolean requireDefinitions, boolean prohibitRaw) {
    	ItemPath myPath = getParent().getPath(parentPath);
    	if (prohibitRaw && rawElement != null) {
    		throw new IllegalStateException("Raw element in property value "+this+" ("+myPath+" in "+rootItem+")");
    	}
    	if (value == null && rawElement == null) {
			throw new IllegalStateException("Neither value nor raw element specified in property value "+this+" ("+myPath+" in "+rootItem+")");
		}
    	if (value != null && rawElement != null) {
			throw new IllegalStateException("Both value and raw element specified in property value "+this+" ("+myPath+" in "+rootItem+")");
		}
	}

	@Override
	public boolean isEmpty() {
		return value == null;
	}

	@Override
    public PrismPropertyValue<T> clone() {
        PrismPropertyValue clone = new PrismPropertyValue(null, getOriginType(), getOriginObject());
        copyValues(clone);
        return clone;
    }
	
	protected void copyValues(PrismPropertyValue clone) {
		super.copyValues(clone);
		clone.value = this.value;
		clone.rawElement = this.rawElement;
	}
	
	public static boolean containsRealValue(Collection<PrismPropertyValue<?>> collection, PrismPropertyValue<?> value) {
		for (PrismPropertyValue<?> colVal: collection) {
			if (value.equalsRealValue(colVal)) {
				return true;
			}
		}
		return false;
	}
	
	public static <T> Collection<PrismPropertyValue<T>> createCollection(Collection<T> realValueCollection) {
		Collection<PrismPropertyValue<T>> pvalCol = new ArrayList<PrismPropertyValue<T>>(realValueCollection.size());
		for (T realValue: realValueCollection) {
			PrismPropertyValue<T> pval = new PrismPropertyValue<T>(realValue);
			pvalCol.add(pval);
		}
		return pvalCol;
	}

	public static <T> Collection<PrismPropertyValue<T>> createCollection(T[] realValueArray) {
		Collection<PrismPropertyValue<T>> pvalCol = new ArrayList<PrismPropertyValue<T>>(realValueArray.length);
		for (T realValue: realValueArray) {
			PrismPropertyValue<T> pval = new PrismPropertyValue<T>(realValue);
			pvalCol.add(pval);
		}
		return pvalCol;
	}

	/**
	 * Takes the definition from the definitionSource parameter and uses it to parse raw elements in origValue.
	 * It returns a new parsed value without touching the original value.
	 */
	private PrismPropertyValue<T> parseRawElementToNewValue(PrismPropertyValue<T> origValue, PrismPropertyValue<T> definitionSource) throws SchemaException {
		if (definitionSource.getParent() != null && definitionSource.getParent().getDefinition() != null) {
			T parsedRealValue = parseRawElementToNewRealValue(origValue, 
					(PrismPropertyDefinition) definitionSource.getParent().getDefinition());
			PrismPropertyValue<T> newPVal = new PrismPropertyValue<T>(parsedRealValue);
			return newPVal;
		} else {
			throw new IllegalArgumentException("Attempt to use property " + origValue.getParent() + 
					" values in a raw parsing state (raw elements) with parsed value that has no definition");
		}
	}
	
	private T parseRawElementToNewRealValue(PrismPropertyValue<T> prismPropertyValue, PrismPropertyDefinition definition) 
				throws SchemaException {
		PrismDomProcessor domProcessor = definition.getPrismContext().getPrismDomProcessor();
		return (T) domProcessor.parsePrismPropertyRealValue(prismPropertyValue.rawElement, (PrismPropertyDefinition) definition);
	}


	@Override
	public boolean equalsComplex(PrismValue other, boolean ignoreMetadata, boolean isLiteral) {
		if (other == null || !(other instanceof PrismPropertyValue)) {
			return false;
		}
		return equalsComplex((PrismPropertyValue<?>)other, ignoreMetadata, isLiteral);
	}
	
	public boolean equalsComplex(PrismPropertyValue<?> other, boolean ignoreMetadata, boolean isLiteral) {
		if (!super.equalsComplex(other, ignoreMetadata, isLiteral)) {
			return false;
		}
		
        if (this.rawElement != null && other.rawElement != null) {
        	return equalsRawElements((PrismPropertyValue<T>)other);
        }
        
        PrismPropertyValue<T> otherProcessed = (PrismPropertyValue<T>) other;
		PrismPropertyValue<T> thisProcessed = this;
		if (this.rawElement != null || other.rawElement != null) {
			try {
				if (this.rawElement == null) {
					otherProcessed = parseRawElementToNewValue((PrismPropertyValue<T>) other, this);
				} else if (other.rawElement == null) {
					thisProcessed = parseRawElementToNewValue(this, (PrismPropertyValue<T>) other);
				}
			} catch (SchemaException e) {
				// TODO: Maybe just return false?
				throw new IllegalArgumentException("Error parsing the value of property "+getParent()+" using the 'other' definition "+
						"during a compare: "+e.getMessage(),e);
			}
		}
        
        T otherRealValue = otherProcessed.getValue();
        T thisRealValue = thisProcessed.getValue();
        if (otherRealValue == null && thisRealValue == null) {
        	return true;
        }
        if (otherRealValue == null || thisRealValue == null) {
        	return false;
        }

		if (thisRealValue instanceof Element && 
				otherRealValue instanceof Element) {
			return DOMUtil.compareElement((Element)thisRealValue, (Element)otherRealValue, isLiteral);
		}
		return thisRealValue.equals(otherRealValue);
	}
	
	private boolean equalsRawElements(PrismPropertyValue<T> other) {
		if (this.rawElement instanceof Element && other.rawElement instanceof Element) {
			return DOMUtil.compareElement((Element)this.rawElement, (Element)other.rawElement, false);
		}
		return this.rawElement.equals(other.rawElement);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		PrismPropertyValue other = (PrismPropertyValue) obj;
		return equalsComplex(other, false, false);
	}
	
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		if (value != null && value instanceof Element) {
			// We need special handling here. We haven't found out the proper way now.
			// so we just do not include this in the hashcode now.
		} else {
			result = prime * result + ((value == null) ? 0 : value.hashCode());
		}
		return result;
	}  
	
	@Override
    public String debugDump() {
        return toString();
    }

    @Override
    public String debugDump(int indent) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            sb.append(INDENT_STRING);
        }
                
        sb.append("PPV(");
        dumpSuffix(sb);
        sb.append("):");
        // getValue() must not be here. getValue() contains exception that in turn causes a call to toString()
        if (value != null) {
        	sb.append(value.getClass().getSimpleName()).append(":");
        	if (value instanceof DebugDumpable) {
        		sb.append("\n");
        		sb.append(((DebugDumpable)value).debugDump(indent + 1));
        	} else {
        		sb.append(PrettyPrinter.prettyPrint(value));
        	}
        } else {
            sb.append("null");
        }
        
        return sb.toString();
    }

    @Override
    public String dump() {
        return toString();
    }
    
	@Override
	public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PPV(");
        // getValue() must not be here. getValue() contains exception that in turn causes a call to toString()
        if (value != null) {
        	builder.append(value.getClass().getSimpleName()).append(":");
        	builder.append(PrettyPrinter.prettyPrint(value));
        } else {
            builder.append("null");
        }
        dumpSuffix(builder);
        builder.append(")");

        return builder.toString();
    }

	private void dumpSuffix(StringBuilder builder) {
        if (getOriginType() != null || getOriginObject() != null) {
	        builder.append(", origin: ");
	        builder.append(getOriginType());
	        builder.append(":");
	        builder.append(getOriginObject());
        }
        if (getRawElement() != null) {
	        builder.append(", raw element: ");
	        builder.append(PrettyPrinter.prettyPrint(getRawElement()));
        }
	}

	public Object getHumanReadableDump() {
		return PrettyPrinter.prettyPrint(value);
	}

}
