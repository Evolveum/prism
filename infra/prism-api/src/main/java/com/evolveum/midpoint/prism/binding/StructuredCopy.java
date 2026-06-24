/*
 * Copyright (c) 2022 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.binding;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.xml.bind.JAXBElement;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.Containerable;
import com.evolveum.midpoint.prism.Referencable;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.prism.util.CloneUtil;
import com.evolveum.midpoint.prism.xml.XmlTypeConverter;

/**
 *
 * Utility class intended to simplify implementation of clone / copy contract
 * for generated {@link PlainStructured} objects.
 *
 */
public class StructuredCopy {

    public static <T> List<T> ofList(List<T> values) {
        if (values == null) {
            return null;
        }
        List<T> ret = new ArrayList<>();
        for(T value : values) {
            ret.add(dispatch(value));
        }
        return ret;
    }

    public static Object of(Object value) {
        return dispatch(value);
    }

    private static <T> T dispatch(T value) {
        if (value == null) {
            return null;
        }
        Object ret;
        if (value instanceof PlainStructured structured) {
            ret = of(structured);
        } else if (value instanceof PolyString string1) {
            ret = of(string1);
        } else if (value instanceof Referencable referencable) {
            ret = of(referencable);
        } else if (value instanceof Containerable containerable) {
            ret = of(containerable);
        } else if (value instanceof String string) {
            ret = of(string);
        } else if (value instanceof Integer integer) {
            ret = of(integer);
        } else if (value instanceof BigInteger integer) {
            ret = of(integer);
        } else if (value instanceof BigDecimal decimal) {
            ret = of(decimal);
        } else if (value instanceof Double casted) {
            ret = of(casted);
        } else if (value instanceof Float casted) {
            ret = of(casted);
        } else if (value instanceof Long casted) {
            ret = of(casted);
        } else if (value instanceof Short casted) {
            ret = of(casted);
        } else if (value instanceof Byte casted) {
            ret = of(casted);
        } else if (value instanceof Boolean casted) {
            ret = of(casted);
        } else if (value instanceof XMLGregorianCalendar calendar) {
            ret = of(calendar);
        } else if (value instanceof Duration duration) {
            ret = of(duration);
        } else if (value instanceof QName name) {
            ret = of(name);
        } else if (value instanceof byte[] bytes) {
            ret = of(bytes);
        } else if (value instanceof JAXBElement<?> element) {
            ret = of(element);
        } else if (value instanceof Cloneable cloneable) {
            ret = clone(cloneable);
        } else if (value instanceof Enum<?>) {
            ret = value;
        } else {
            throw new IllegalArgumentException("Value '" + value + "' of type " + value.getClass().getName()
                    + " is not supported for structured copy");
        }
        //noinspection unchecked
        return (T) ret;
    }

    private static <T extends Cloneable> T clone(T value) {
        return CloneUtil.clone(value);
    }

    @SuppressWarnings("unchecked")
    public static <T extends PlainStructured> T of(T value) {
        if (value == null) {
            return null;
        }
        return (T) value.clone();
    }

    public static byte[] of(byte[] array) {
        if (array == null) {
            return null;
        }
        return Arrays.copyOf(array, array.length);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Containerable> T of(T value) {
        if (value == null) {
            return null;
        }
        return (T) value.asPrismContainerValue().clone().asContainerable();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Referencable> T of(T value) {
        if (value == null) {
            return null;
        }
        return (T) value.clone();
    }

    public static PolyString of(PolyString value) {
        if (value == null) {
            return null;
        }
        return value.copy();
    }

    public static Duration of(Duration value) {
        if (value == null) {
            return null;
        }
        return XmlTypeConverter.createDuration(value);
    }

    public static <T> JAXBElement<T> of(JAXBElement<T> value) {
        if(value == null) {
            return null;
        }
        return new JAXBElement<>(of(value.getName()), value.getDeclaredType(), dispatch(value.getValue()));
    }

    public static QName of(QName name) {
        // Should we copy QNames also?
        return name;
    }

    public static XMLGregorianCalendar of(XMLGregorianCalendar value) {
        if (value == null) {
            return null;
        }
        return XmlTypeConverter.createXMLGregorianCalendar(value);
    }

    // Known immutables

    public static String of(String value) {
        return value;
    }

    public static <T extends Enum<T>> T of(T value) {
        return value;
    }


    public static Boolean of(Boolean value) {
        return value;
    }


    public static Integer of(Integer value) {
        return value;
    }


    public static Long of(Long value) {
        return value;
    }


    public static BigInteger of(BigInteger value) {
        return value;
    }


    public static Short of(Short value) {
        return value;
    }


    public static Float of(Float value) {
        return value;
    }


    public static Byte of(Byte value) {
        return value;
    }


    public static Double of(Double value) {
        return value;
    }

    public static BigDecimal of(BigDecimal value) {
        return value;
    }

}
