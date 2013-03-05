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

import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.delta.PropertyDelta;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.util.DebugDumpable;
import com.evolveum.midpoint.util.Dumpable;
import com.evolveum.midpoint.util.MiscUtil;
import com.evolveum.midpoint.util.PrettyPrinter;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.SystemException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Item is a common abstraction of Property and PropertyContainer.
 * <p/>
 * This is supposed to be a superclass for all items. Items are things
 * that can appear in property containers, which generally means only a property
 * and property container itself. Therefore this is in fact superclass for those
 * two definitions.
 *
 * @author Radovan Semancik
 */
public abstract class Item<V extends PrismValue> implements Itemable, Dumpable, DebugDumpable, Visitable, Serializable {

    private static final long serialVersionUID = 510000191615288733L;

    // The object should basically work without definition and prismContext. This is the
	// usual case when it is constructed "out of the blue", e.g. as a new JAXB object
	// It may not work perfectly, but basic things should work
    protected QName name;
    protected PrismValue parent;
    protected ItemDefinition definition;
    private List<V> values = new ArrayList<V>();
    private transient Map<String,Object> userData;
    
    protected transient PrismContext prismContext;

    /**
     * This is used for definition-less construction, e.g. in JAXB beans.
     * 
     * The constructors should be used only occasionally (if used at all).
     * Use the factory methods in the ResourceObjectDefintion instead.
     */
    Item(QName name) {
        super();
        this.name = name;
        this.userData = new HashMap<String, Object>();
    }

    /**
     * The constructors should be used only occasionally (if used at all).
     * Use the factory methods in the ResourceObjectDefintion instead.
     */
    Item(QName name, ItemDefinition definition, PrismContext prismContext) {
        super();
        this.name = name;
        this.definition = definition;
        this.prismContext = prismContext;
        this.userData = new HashMap<String, Object>();
    }
        
    /**
     * Returns applicable property definition.
     * <p/>
     * May return null if no definition is applicable or the definition is not
     * know.
     *
     * @return applicable property definition
     */
    public ItemDefinition getDefinition() {
        return definition;
    }
    
	public boolean hasCompleteDefinition() {
		return getDefinition() != null;
	}


    /**
     * Returns the name of the property.
     * <p/>
     * The name is a QName. It uniquely defines a property.
     * <p/>
     * The name may be null, but such a property will not work.
     * <p/>
     * The name is the QName of XML element in the XML representation.
     *
     * @return property name
     */
    @Override
    public QName getName() {
        return name;
    }

    /**
     * Sets the name of the property.
     * <p/>
     * The name is a QName. It uniquely defines a property.
     * <p/>
     * The name may be null, but such a property will not work.
     * <p/>
     * The name is the QName of XML element in the XML representation.
     *
     * @param name the name to set
     */
    public void setName(QName name) {
        this.name = name;
    }

    /**
     * Sets applicable property definition.
     *
     * @param definition the definition to set
     */
    public void setDefinition(ItemDefinition definition) {
    	checkDefinition(definition);
        this.definition = definition;
    }

	/**
     * Returns a display name for the property type.
     * <p/>
     * Returns null if the display name cannot be determined.
     * <p/>
     * The display name is fetched from the definition. If no definition
     * (schema) is available, the display name will not be returned.
     *
     * @return display name for the property type
     */
    public String getDisplayName() {
        return getDefinition() == null ? null : getDefinition().getDisplayName();
    }

    /**
     * Returns help message defined for the property type.
     * <p/>
     * Returns null if the help message cannot be determined.
     * <p/>
     * The help message is fetched from the definition. If no definition
     * (schema) is available, the help message will not be returned.
     *
     * @return help message for the property type
     */
    public String getHelp() {
        return getDefinition() == null ? null : getDefinition().getHelp();
    }
    
    @Override
    public PrismContext getPrismContext() {
    	return prismContext;
    }
    
    public void setPrismContext(PrismContext prismContext) {
		this.prismContext = prismContext;
	}

	public PrismValue getParent() {
    	return parent;
    }
    
    public void setParent(PrismValue parentValue) {
    	if (this.parent != null && parentValue != null && this.parent != parentValue) {
    		throw new IllegalStateException("Attempt to reset parent of item "+this+" from "+this.parent+" to "+parentValue);
    	}
    	this.parent = parentValue;
    }
    
    @Override
    public ItemPath getPath(ItemPath pathPrefix) {
    	if (pathPrefix == null) {
    		return new ItemPath(getName());
    	}
    	return pathPrefix.subPath(getName());
    }
    
    public Map<String, Object> getUserData() {
		return userData;
	}
    
    public Object getUserData(String key) {
		return userData.get(key);
	}
    
    public void setUserData(String key, Object value) {
    	userData.put(key, value);
    }

	public List<V> getValues() {
		return values;
	}
    
    public V getValue(int index) {
    	if (index < 0) {
    		index = values.size() + index;
    	}
		return values.get(index);
	}
    
    public boolean hasValue(PrismValue value, boolean ignoreMetadata) {
    	return (findValue(value, ignoreMetadata) != null);
    }
    
    public boolean hasValue(PrismValue value) {
        return hasValue(value, false);
    }
    
    public boolean hasRealValue(PrismValue value) {
    	return hasValue(value, true);
    }
    
    public boolean isSingleValue() {
		// We are not sure about multiplicity if there is no definition or the definition is dynamic
		if (getDefinition() != null && !getDefinition().isDynamic()) {
    		if (getDefinition().isMultiValue()) {
    			return false;
    		}
    	}
		if (values == null || values.size() < 2) {
        	return true;
        }
		return false;
	}
    
    /**
     * Returns value that is equal or equivalent to the provided value.
     * The returned value is an instance stored in this item, while the
     * provided value argument may not be.
     */
    public PrismValue findValue(PrismValue value, boolean ignoreMetadata) {
        for (PrismValue myVal : getValues()) {
            if (myVal.equalsComplex(value, ignoreMetadata, false)) {
                return myVal;
            }
        }
        return null;
    }
    
    /**
     * Returns value that is previous to the specified value.
     * Note that the order is semantically insignificant and this is used only
     * for presentation consistency in order-sensitive formats such as XML or JSON.
     */
    public PrismValue getPreviousValue(PrismValue value) {
    	PrismValue previousValue = null;
    	for (PrismValue myVal : getValues()) {
    		if (myVal == value) {
    			return previousValue;
    		}
    		previousValue = myVal;
    	}
    	throw new IllegalStateException("The value "+value+" is not any of "+this+" values, therefore cannot determine previous value");
    }

    /**
     * Returns values that is following the specified value.
     * Note that the order is semantically insignificant and this is used only
     * for presentation consistency in order-sensitive formats such as XML or JSON.
     */
    public PrismValue getNextValue(PrismValue value) {
    	Iterator<V> iterator = getValues().iterator();
    	while (iterator.hasNext()) {
    		PrismValue myVal = iterator.next();
    		if (myVal == value) {
    			if (iterator.hasNext()) {
    				return iterator.next();
    			} else {
    				return null;
    			}
    		}
    	}
    	throw new IllegalStateException("The value "+value+" is not any of "+this+" values, therefore cannot determine next value");
    }

    public Collection<V> getClonedValues() {
    	Collection<V> clonedValues = new ArrayList<V>(getValues().size());
    	for (V val: getValues()) {
    		clonedValues.add((V)val.clone());
    	}
		return clonedValues;
	}
    
    public boolean contains(V value) {
    	return contains(value, false);
    }
    
    public boolean containsEquivalentValue(V value) {
    	return contains(value, true);
    }
    
    public boolean contains(V value, boolean ignoreMetadata) {
    	for (V myValue: getValues()) {
    		if (myValue.equals(value, ignoreMetadata)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public boolean containsRealValue(V value) {
    	for (V myValue: getValues()) {
    		if (myValue.equalsRealValue(value)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public int size() {
    	return values.size();
    }
    
    public boolean addAll(Collection<V> newValues) throws SchemaException {
    	boolean changed = false;
    	for (V val: newValues) {
    		if (add(val)) {
    			changed = true;
    		}
    	}
    	return changed;
    }
    
    public boolean add(V newValue) throws SchemaException {
    	newValue.setParent(this);
    	if (containsEquivalentValue(newValue)) {
    		return false;
    	}	
    	if (getDefinition() != null) {
    		newValue.applyDefinition(getDefinition(), false);
    	}
    	return values.add(newValue);
    }
    
    public boolean removeAll(Collection<V> newValues) {
    	boolean changed = false;
    	for (V val: newValues) {
    		if (remove(val)) {
    			changed = true;
    		}
    	}
    	return changed;
    }

    public boolean remove(V newValue) {
    	boolean changed = false;
    	Iterator<V> iterator = values.iterator();
    	while (iterator.hasNext()) {
    		V val = iterator.next();
    		if (val.representsSameValue(newValue) || val.equalsRealValue(newValue)) {
    			iterator.remove();
    			changed = true;
    		}
    	}
    	return changed;
    }
    
    public V remove(int index) {
    	return values.remove(index);
    }

    public void replaceAll(Collection<V> newValues) throws SchemaException {
    	values.clear();
    	addAll(newValues);
    }

    public void replace(V newValue) {
    	values.clear();
    	values.add(newValue);
    }
    
    public void clear() {
    	values.clear();
    }
    
    public void normalize() {
    	Iterator<V> iterator = values.iterator();
    	while (iterator.hasNext()) {
    		V value = iterator.next();
    		value.normalize();
    		if (value.isEmpty()) {
    			iterator.remove();
    		}
    	}
    }

    public List<Element> asDomElements() {
    	List<Element> elements = new ArrayList<Element>();
    	for (PrismValue pval: getValues()) {
    		elements.add(pval.asDomElement());
    	}
    	return elements;
    }
    
    public abstract Object find(ItemPath path);
    
    public abstract <X extends PrismValue> PartiallyResolvedValue<X> findPartial(ItemPath path);
    
    public Collection<? extends ItemDelta> diff(Item<V> other) {
    	return diff(other, true, false);
    }
        
    public Collection<? extends ItemDelta> diff(Item<V> other, boolean ignoreMetadata, boolean isLiteral) {
    	return diff(other, null, ignoreMetadata, isLiteral);
    }
    
    public Collection<? extends ItemDelta> diff(Item<V> other, ItemPath pathPrefix, boolean ignoreMetadata, boolean isLiteral) {
    	Collection<? extends ItemDelta> itemDeltas = new ArrayList<ItemDelta>();
		diffInternal(other, pathPrefix, itemDeltas, ignoreMetadata, isLiteral);
		return itemDeltas;
    }
        
    protected void diffInternal(Item<V> other, ItemPath pathPrefix, Collection<? extends ItemDelta> deltas, 
    		boolean ignoreMetadata, boolean isLiteral) {
    	ItemPath deltaPath = getPath(pathPrefix);
    	ItemDelta delta = null;
    	if (deltaPath != null && !deltaPath.isEmpty()) {
    		// DeltaPath can be empty in objects. But in that case we don't expect to create any delta at this level anyway.
    		delta = createDelta(deltaPath);
    	}
    	if (other == null) {
    		//other doesn't exist, so delta means delete all values
            for (PrismValue value : getValues()) {
            	PrismValue valueClone = value.clone();
                delta.addValueToDelete(valueClone);
            }
    	} else {
    		// the other exists, this means that we need to compare the values one by one
    		Collection<PrismValue> outstandingOtheValues = new ArrayList<PrismValue>(other.getValues().size());
    		outstandingOtheValues.addAll(other.getValues());
    		for (PrismValue thisValue : getValues()) {
    			Iterator<PrismValue> iterator = outstandingOtheValues.iterator();
    			boolean found = false;
    			while (iterator.hasNext()) {
    				PrismValue otherValue = iterator.next();
    				if (thisValue.representsSameValue(otherValue) || delta == null) {
    					found = true;
    					// Matching IDs, look inside to figure out internal deltas
    					thisValue.diffMatchingRepresentation(otherValue, thisValue.getPath(pathPrefix), deltas, 
    							ignoreMetadata, isLiteral);
    					// No need to process this value again
    					iterator.remove();
    					break;
    				} else if (thisValue.equalsComplex(otherValue, ignoreMetadata, isLiteral)) {
    					found = true;
    					// same values. No delta
    					// No need to process this value again
    					iterator.remove();
    					break;
    				}
    			}
				if (!found) {
					// We have the value and the other does not, this is delete of the entire value
					delta.addValueToDelete(thisValue.clone());
				}
            }
    		// outstandingOtheValues are those values that the other has and we could not
    		// match them to any of our values. These must be new values to add
    		for (PrismValue outstandingOtherValue : outstandingOtheValues) {
    			delta.addValueToAdd(outstandingOtherValue.clone());
            }
    		// Some deltas may need to be polished a bit. E.g. transforming
    		// add/delete delta to a replace delta.
    		delta = fixupDelta(delta, other, pathPrefix, ignoreMetadata);
    	}
    	if (delta != null && !delta.isEmpty()) {
    		((Collection)deltas).add(delta);
    	}
    }
    
	protected ItemDelta fixupDelta(ItemDelta delta, Item<V> other, ItemPath pathPrefix,
			boolean ignoreMetadata) {
		return delta;
	}

	/**
     * Creates specific sublcass of ItemDelta appropriate for type of item that this definition
     * represents (e.g. PropertyDelta, ContainerDelta, ...)
     */
	public abstract ItemDelta<V> createDelta(ItemPath path);

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
		for(PrismValue value: getValues()) {
			value.accept(visitor);
		}
	}
	
	public void applyDefinition(ItemDefinition definition) throws SchemaException {
		applyDefinition(definition, true);
	}
	
	public void applyDefinition(ItemDefinition definition, boolean force) throws SchemaException {
		if (definition != null) {
			checkDefinition(definition);
		}
		if (this.prismContext == null && definition != null) {
			this.prismContext = definition.getPrismContext();
		}
		this.definition = definition;
		for (PrismValue pval: getValues()) {
			pval.applyDefinition(definition, force);
		}
	}
    
    public void revive(PrismContext prismContext) {
    	if (this.prismContext != null) {
    		return;
    	}
    	this.prismContext = prismContext;
    	if (definition != null) {
    		definition.revive(prismContext);
    	}
    	if (values != null) {
    		for (V value: values) {
    			value.revive(prismContext);
    		}
    	}
    }

    public abstract Item clone();

    protected void copyValues(Item clone) {
        clone.name = this.name;
        clone.definition = this.definition;
        clone.prismContext = this.prismContext;
        // Do not clone parent so the cloned item can be safely placed to
        // another item
        clone.parent = null;
    }
    
    public static <T extends Item> Collection<T> cloneCollection(Collection<T> items) {
    	Collection<T> clones = new ArrayList<T>(items.size());
    	for (T item: items) {
    		clones.add((T)item.clone());
    	}
    	return clones;
    }
    
    /**
     * Sets all parents to null. This is good if the items are to be "transplanted" into a
     * different Containerable.
     */
	public static <T extends Item> Collection<T> resetParentCollection(Collection<T> items) {
    	for (T item: items) {
    		item.setParent(null);
    	}
    	return items;
	}
    
    public static <T extends Item> T createNewDefinitionlessItem(QName name, Class<T> type) {
    	T item = null;
		try {
			Constructor<T> constructor = type.getConstructor(QName.class);
			item = constructor.newInstance(name);
		} catch (Exception e) {
			throw new SystemException("Error creating new definitionless "+type.getSimpleName()+": "+e.getClass().getName()+" "+e.getMessage(),e);
		}
    	return item;
    }
    
    public void checkConsistence(boolean requireDefinitions) {
    	checkConsistenceInternal(this, ItemPath.EMPTY_PATH, requireDefinitions, false);
    }
    
    public void checkConsistence(boolean requireDefinitions, boolean prohibitRaw) {
    	checkConsistenceInternal(this, ItemPath.EMPTY_PATH, requireDefinitions, prohibitRaw);
    }
    
    public void checkConsistence() {
    	checkConsistenceInternal(this, ItemPath.EMPTY_PATH, false, false);
    }
    
    public void checkConsistenceInternal(Itemable rootItem, ItemPath path, boolean requireDefinitions, boolean prohibitRaw) {
    	if (name == null) {
    		throw new IllegalStateException("Item "+this+" has no name ("+path+" in "+rootItem+")");
    	}
    	
    	if (definition != null) {
    		checkDefinition(definition);
    	} else if (requireDefinitions && !isRaw()) {
    		throw new IllegalStateException("No definition in item "+this+" ("+path+" in "+rootItem+")");
    	}
    	if (values != null) {
    		for(V val: values) {
    			if (prohibitRaw && val.isRaw()) {
    				throw new IllegalStateException("Raw value "+val+" in item "+this+" ("+path+" in "+rootItem+")");
    			}
    			if (val == null) {
    				throw new IllegalStateException("Null value in item "+this+" ("+path+" in "+rootItem+")");
    			}
    			if (val.getParent() == null) {
    				throw new IllegalStateException("Null parent for value "+val+" in item "+this+" ("+path+" in "+rootItem+")");
    			}
    			if (val.getParent() != this) {
    				throw new IllegalStateException("Wrong parent for value "+val+" in item "+this+" ("+path+" in "+rootItem+"), "+
    						"bad parent: " + val.getParent());
    			}
    			val.checkConsistenceInternal(rootItem, path, requireDefinitions, prohibitRaw);
    		}
    	}
    }
    
	protected abstract void checkDefinition(ItemDefinition def);
	
    public void assertDefinitions() throws SchemaException {
    	assertDefinitions("");
    }

	public void assertDefinitions(String sourceDescription) throws SchemaException {
		assertDefinitions(false, sourceDescription);
	}
	
	public void assertDefinitions(boolean tolarateRawValues, String sourceDescription) throws SchemaException {
		if (tolarateRawValues && isRaw()) {
			return;
		}
		if (definition == null) {
			throw new SchemaException("No definition in "+this+" in "+sourceDescription);
		}
	}
	
	/**
	 * Returns true is all the values are raw.
	 */
	public boolean isRaw() {
		for (V val: getValues()) {
			if (!val.isRaw()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Returns true is at least one of the values is raw.
	 */
	public boolean hasRaw() {
		for (V val: getValues()) {
			if (val.isRaw()) {
				return true;
			}
		}
		return false;
	}

	public boolean isEmpty() {
        return (getValues() == null || getValues().isEmpty());
    }

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((values == null) ? 0 : MiscUtil.unorderedCollectionHashcode(values));
		return result;
	}

	public boolean equalsRealValue(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Item<?> other = (Item<?>) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		// Do not compare parent at all. This is not relevant.
		if (values == null) {
			if (other.values != null)
				return false;
		} else if (!equalsRealValues(this.values, other.values))
			return false;
		return true;
	}

	private boolean equalsRealValues(List<V> thisValue, List<?> otherValues) {
		Comparator<?> comparator = new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				if (o1 instanceof PrismValue && o2 instanceof PrismValue) {
					PrismValue v1 = (PrismValue)o1;
					PrismValue v2 = (PrismValue)o2;
					return v1.equalsRealValue(v2) ? 0 : 1;
				} else {
					return -1;
				}
			}
		};
		return MiscUtil.unorderedCollectionEquals(thisValue, otherValues, comparator);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Item<?> other = (Item<?>) obj;
		if (definition == null) {
			if (other.definition != null)
				return false;
		} else if (!definition.equals(other.definition))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		// Do not compare parent at all. This is not relevant.
		if (values == null) {
			if (other.values != null)
				return false;
		} else if (!MiscUtil.unorderedCollectionEquals(this.values, other.values))
			return false;
		return true;
	}

	@Override
    public String toString() {
        return getClass().getSimpleName() + "(" + getName() + ")";
    }

    @Override
    public String dump() {
        return toString();
    }

    /**
     * Provide terse and readable dump of the object suitable for log (at debug level).
     */
    public String debugDump() {
        return debugDump(0);
    }

    public String debugDump(int indent) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            sb.append(INDENT_STRING);
        }
        sb.append(getDebugDumpClassName()).append(": ").append(PrettyPrinter.prettyPrint(getName()));
        return sb.toString();
    }

    /**
     * Return a human readable name of this class suitable for logs.
     */
    protected String getDebugDumpClassName() {
        return "Item";
    }
    
}
