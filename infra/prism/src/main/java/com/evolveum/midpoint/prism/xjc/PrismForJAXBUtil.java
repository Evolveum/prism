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

package com.evolveum.midpoint.prism.xjc;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.util.exception.SchemaException;

import com.evolveum.midpoint.util.exception.SystemException;
import org.apache.commons.lang.Validate;
import org.w3c.dom.Element;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.namespace.QName;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author lazyman
 */
public final class PrismForJAXBUtil {

    private PrismForJAXBUtil() {
    }

    public static <T> List<T> getPropertyValues(PrismContainer container, QName name, Class<T> clazz) {
        Validate.notNull(container, "Container must not be null.");
        Validate.notNull(name, "QName must not be null.");
        Validate.notNull(clazz, "Class type must not be null.");

        PrismProperty property;
		try {
			property = container.findOrCreateProperty(name);
		} catch (SchemaException e) {
			// This should not happen. Code generator and compiler should take care of that.
			throw new IllegalStateException("Internal schema error: "+e.getMessage(),e);
		}
        return new PropertyArrayList<T>(property);
    }

    public static <T> T getPropertyValue(PrismContainerValue container, QName name, Class<T> clazz) {
        Validate.notNull(container, "Container must not be null.");
        Validate.notNull(name, "QName must not be null.");
        Validate.notNull(clazz, "Class type must not be null.");

        PrismProperty property = container.findProperty(name);
        return getPropertyValue(property, clazz);
    }

    public static <T> T getPropertyValue(PrismContainer container, QName name, Class<T> clazz) {
        Validate.notNull(container, "Container must not be null.");
        Validate.notNull(name, "QName must not be null.");
        Validate.notNull(clazz, "Class type must not be null.");

        PrismProperty property = container.findProperty(name);
        return getPropertyValue(property, clazz);
    }

    private static <T> T getPropertyValue(PrismProperty<?> property, Class<T> requestedType) {
        if (property == null) {
            return null;
        }

        PrismPropertyValue<?> pvalue = property.getValue();
        if (pvalue == null) {
            return null;
        }
        
        Object propertyRealValue = pvalue.getValue();
        
        if (propertyRealValue instanceof Element) {
        	if (requestedType.isAssignableFrom(Element.class)) {
        		return (T) propertyRealValue;
        	}
        	Field anyField = getAnyField(requestedType);
        	if (anyField == null) {
        		throw new IllegalArgumentException("Attempt to read raw property "+property+" while the requested class ("+requestedType+") does not have 'any' field");
        	}
        	anyField.setAccessible(true);
        	Collection<?> anyElementList = property.getRealValues();
        	T requestedTypeInstance;
			try {
				requestedTypeInstance = requestedType.newInstance();
				anyField.set(requestedTypeInstance, anyElementList);
			} catch (InstantiationException e) {
				throw new IllegalArgumentException("Instantiate error while reading raw property "+property+", requested class ("+requestedType+"):"
						+e.getMessage(), e);
			} catch (IllegalAccessException e) {
				throw new IllegalArgumentException("Illegal access error while reading raw property "+property+", requested class ("+requestedType+")"
						+", field "+anyField+": "+e.getMessage(), e);
			}
			return requestedTypeInstance;
        }
        
        return JaxbTypeConverter.mapPropertyRealValueToJaxb(propertyRealValue);
    }
    
    private static <T> Field getAnyField(Class<T> clazz) {
    	for (Field field: clazz.getDeclaredFields()) {
    		XmlAnyElement xmlAnyElementAnnotation = field.getAnnotation(XmlAnyElement.class);
    		if (xmlAnyElementAnnotation != null) {
    			return field;
    		}
    	}
    	return null;
    }
    
    public static <T> List<T> getPropertyValues(PrismContainerValue container, QName name, Class<T> clazz) {
        Validate.notNull(container, "Container must not be null.");
        Validate.notNull(name, "QName must not be null.");
        Validate.notNull(clazz, "Class type must not be null.");

        PrismProperty property;
		try {
			property = container.findOrCreateProperty(name);
		} catch (SchemaException e) {
			// This should not happen. Code generator and compiler should take care of that.
			throw new IllegalStateException("Internal schema error: "+e.getMessage(),e);
		}
        return new PropertyArrayList<T>(property);
    }

    
    public static <T> void setPropertyValue(PrismContainerValue<?> container, QName name, T value) {
        Validate.notNull(container, "Container must not be null.");
        Validate.notNull(name, "QName must not be null.");

        if (value == null) {
        	container.removeProperty(name);
        } else {
	        PrismProperty<?> property;
			try {
				property = container.findOrCreateProperty(name);
			} catch (SchemaException e) {
				// This should not happen. Code generator and compiler should take care of that.
				throw new IllegalStateException("Internal schema error: "+e.getMessage(),e);
			}
	    	Object propertyRealValue = JaxbTypeConverter.mapJaxbToPropertyRealValue(value);
	    	if (propertyRealValue == null) {
	    		container.removeProperty(name);
	    	} else {
	    		property.setValue(new PrismPropertyValue(propertyRealValue));
	    	}
        }
    }

    public static <T> void setPropertyValue(PrismContainer container, QName name, T value) {
    	setPropertyValue(container.getValue(), name, value);
    }

    public static <T extends Containerable> PrismContainerValue<T> getFieldContainerValue(PrismContainer<?> parent, QName fieldName) {
        Validate.notNull(parent, "Container must not be null.");
        Validate.notNull(fieldName, "Field QName must not be null.");

        return getFieldContainerValue(parent.getValue(), fieldName);
    }

    public static <T extends Containerable> PrismContainerValue<T> getFieldContainerValue(PrismContainerValue<?> parent, QName fieldName) {
        Validate.notNull(parent, "Container value must not be null.");
        Validate.notNull(fieldName, "Field QName must not be null.");

        PrismContainer<T> container = parent.findItem(fieldName, PrismContainer.class);
        return container != null ? container.getValue() : null;
    }

    public static <T extends Containerable> T getFieldSingleContainerable(PrismContainerValue<?> parent, QName fieldName, Class<T> fieldClass) {
    	PrismContainerValue<T> fieldContainerValue = getFieldContainerValue(parent, fieldName);
    	if (fieldContainerValue == null) {
    		return null;
    	}
    	return fieldContainerValue.asContainerable(fieldClass);
    }

    public static <T extends PrismContainer<?>> T getContainer(PrismContainerValue value, QName name) {
        Validate.notNull(value, "Container must not be null.");
        Validate.notNull(name, "QName must not be null.");

        return getContainer(value.getContainer(), name);
    }

    public static <T extends PrismContainer<?>> T getContainer(PrismContainer<?> parent, QName name) {
        Validate.notNull(parent, "Container must not be null.");
        Validate.notNull(name, "QName must not be null.");

        try {
            return (T) parent.findOrCreateContainer(name);
        } catch (SchemaException ex) {
            throw new SystemException(ex.getMessage(),  ex);
        }
    }

    public static <T extends Containerable> boolean setFieldContainerValue(PrismContainerValue<?> parent, QName fieldName, 
    		PrismContainerValue<T> fieldContainerValue) {
        Validate.notNull(parent, "Prism container value must not be null.");
        Validate.notNull(fieldName, "QName must not be null.");

        try {
	        PrismContainer<T> fieldContainer = null;
	        if (fieldContainerValue == null) {
	        	parent.removeContainer(fieldName);
	        } else {
	        	if (fieldContainerValue.getParent() != null && fieldContainerValue.getParent() != parent) {
	        		// This value is already part of another prism. We need to clone it to add it here.
	        		fieldContainerValue = fieldContainerValue.clone();
	        	}
	            fieldContainer = new PrismContainer<T>(fieldName);
	            fieldContainer.add(fieldContainerValue);
	            if (parent.getContainer() == null) {
	                parent.add(fieldContainer);
	            } else {
                    parent.addReplaceExisting(fieldContainer);
	            }
	        }
//	        // Make sure that the definition from parent is applied to new field container
//	        if (fieldContainer.getDefinition() == null) {
//	        	PrismContainer<?> parentContainer = parent.getContainer();
//	        	if (parentContainer != null) {
//		        	PrismContainerDefinition<?> parentDefinition = parentContainer.getDefinition();
//		        	if (parentDefinition != null) {
//		        		PrismContainerDefinition<T> fieldDefinition = parentDefinition.findContainerDefinition(fieldName);
//		        		fieldContainer.setDefinition(fieldDefinition);
//		        	}
//	        	}
//	        }
        } catch (SchemaException e) {
        	// This should not happen. Code generator and compiler should take care of that.
			throw new IllegalStateException("Internal schema error: "+e.getMessage(),e);
        }
        return true;
    }

    public static boolean setFieldContainerValue(PrismContainer<?> parent, QName fieldName, PrismContainerValue<?> fieldContainerValue) {
        return setFieldContainerValue(parent.getValue(), fieldName, fieldContainerValue);
    }

    public static PrismReferenceValue getReferenceValue(PrismContainerValue<?> parent, QName name) {
        Validate.notNull(parent, "Prism container value must not be null.");
        Validate.notNull(name, "QName must not be null.");

        PrismReference reference = parent.findItem(name, PrismReference.class);
        return reference != null ? reference.getValue() : null;
    }

    public static PrismReferenceValue getReferenceValue(PrismContainer parent, QName name) {
        Validate.notNull(parent, "Prism container must not be null.");
        Validate.notNull(name, "QName must not be null.");

        PrismReference reference = getReference(parent, name);
        return reference != null ? reference.getValue() : null;
    }

    public static PrismReference getReference(PrismContainer parent, QName name) {
        Validate.notNull(parent, "Prism container must not be null.");
        Validate.notNull(name, "QName must not be null.");

        return parent.findReference(name);
    }

    /**
     * This method must merge new value with potential existing value of the reference.
     * E.g. it is possible to call setResource(..) and then setResourceRef(..) with the
     * same OID. In that case the result should be one reference that has both OID/type/filter
     * and object.
     * Assumes single-value reference
     */
    public static void setReferenceValueAsRef(PrismContainerValue<?> parentValue, QName referenceName,
            PrismReferenceValue value) {
        Validate.notNull(parentValue, "Prism container value must not be null.");
        Validate.notNull(referenceName, "QName must not be null.");

        PrismReference reference;
		try {
			reference = parentValue.findOrCreateItem(referenceName, PrismReference.class);
		} catch (SchemaException e) {
			// This should not happen. Code generator and compiler should take care of that.
			throw new IllegalStateException("Internal schema error: "+e.getMessage(),e);
		}
		if (reference == null) {
        	throw new IllegalArgumentException("No reference "+referenceName+" in "+parentValue);
        }
    	if (reference.isEmpty()) {
    		if (value.getParent() != null) {
    			value = value.clone();
    		}
    		reference.add(value);
    	} else {
            if (value == null) {
                parentValue.remove(reference);
//                reference.getValue().setOid(null);
//                reference.getValue().setTargetType(null);
//                reference.getValue().setFilter(null);
//                reference.getValue().setDescription(null);
	        } else {
                reference.getValue().setOid(value.getOid());
                reference.getValue().setTargetType(value.getTargetType());
                reference.getValue().setFilter(value.getFilter());
                reference.getValue().setDescription(value.getDescription());
	        }
    	}
    }

    public static void setReferenceValueAsRef(PrismContainer parent, QName name, PrismReferenceValue value) {
        setReferenceValueAsRef(parent.getValue(), name, value);
    }

    /**
     * This method must merge new value with potential existing value of the reference.
     * E.g. it is possible to call setResource(..) and then setResourceRef(..) with the
     * same OID. In that case the result should be one reference that has both OID/type/filter
     * and object.
     * Assumes single-value reference
     */
    public static void setReferenceValueAsObject(PrismContainerValue parentValue, QName referenceQName, PrismObject targetObject) {
        Validate.notNull(parentValue, "Prism container value must not be null.");
        Validate.notNull(referenceQName, "QName must not be null.");

        PrismReference reference;
		try {
			reference = parentValue.findOrCreateReference(referenceQName);
		} catch (SchemaException e) {
			// This should not happen. Code generator and compiler should take care of that.
			throw new IllegalStateException("Internal schema error: "+e.getMessage(),e);
		}
        if (reference == null) {
        	throw new IllegalArgumentException("No reference "+referenceQName+" in "+parentValue);
        }
        PrismReferenceValue referenceValue = reference.getValue();
        referenceValue.setObject(targetObject);
    }

    // Assumes single-value reference
    public static void setReferenceValueAsObject(PrismContainer parent, QName referenceQName, PrismObject targetObject) {
    	setReferenceValueAsObject(parent.getValue(), referenceQName, targetObject);
    }

    public static <T extends Objectable> PrismReferenceValue objectableAsReferenceValue(T objectable, PrismReference reference ) {
    	PrismObject<T> object = objectable.asPrismObject();
        for (PrismReferenceValue refValue: reference.getValues()) {
            if (object == refValue.getObject()) {
                return refValue;
            }
        }
        PrismReferenceValue referenceValue = new PrismReferenceValue();
        referenceValue.setObject(object);
        return referenceValue;
    }

    public static <T extends Containerable> List<PrismContainerValue<T>> getContainerValues(PrismContainerValue<T> parent, QName name, Class<T> clazz) {
        return getContainerValues(parent.getContainer(), name, clazz);
    }

    public static <T extends Containerable> List<PrismContainerValue<T>> getContainerValues(PrismContainer<T> parent, QName name, Class<T> clazz) {
        Validate.notNull(parent, "Container must not be null.");
        Validate.notNull(name, "QName must not be null.");

        PrismContainer container;
		try {
			container = parent.findOrCreateContainer(name);
		} catch (SchemaException e) {
			// This should not happen. Code generator and compiler should take care of that.
			throw new IllegalStateException("Internal schema error: "+e.getMessage(),e);
		}
        return container.getValues();
    }

    public static <T> List<T> getAny(PrismContainerValue value, Class<T> clazz) {
    	return new AnyArrayList(value);
    }

	public static PrismObject setupContainerValue(PrismObject prismObject, PrismContainerValue containerValue) {
		PrismContainerable parent = containerValue.getParent();
		if (parent != null && parent instanceof PrismObject) {
			return (PrismObject)parent;
		}
		try {
			prismObject.setValue(containerValue);
			return prismObject;
		} catch (SchemaException e) {
			// This should not happen. Code generator and compiler should take care of that.
			throw new IllegalStateException("Internal schema error: "+e.getMessage(),e);
		}
	}

	public static PrismReference getReference(PrismContainerValue parent, QName fieldName) {
		try {
			return parent.findOrCreateReference(fieldName);
		} catch (SchemaException e) {
			// This should not happen. Code generator and compiler should take care of that.
			throw new IllegalStateException("Internal schema error: "+e.getMessage(),e);
		}
	}

}
