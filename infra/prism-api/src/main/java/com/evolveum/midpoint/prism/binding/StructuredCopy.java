package com.evolveum.midpoint.prism.binding;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.Containerable;
import com.evolveum.midpoint.prism.Referencable;
import com.evolveum.midpoint.prism.util.CloneUtil;
import com.evolveum.midpoint.prism.xml.XmlTypeConverter;

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
        if (value instanceof PlainStructured) {
            ret =  of((PlainStructured) value);
        } else if (value instanceof Referencable) {
            ret =  of((Referencable) value);
        } else if (value instanceof Containerable) {
            ret =  of((Containerable) value);
        } else if (value instanceof String) {
            ret = of((String) value);
        } else if (value instanceof Integer) {
            ret = of((Integer) value);
        } else if (value instanceof BigInteger) {
            ret = of((BigInteger) value);
        } else if (value instanceof BigDecimal) {
            ret = of((BigDecimal) value);
        } else if (value instanceof Double) {
            ret = of((Double) value);
        } else if (value instanceof Float) {
            ret = of((Float) value);
        } else if (value instanceof Long) {
            ret = of((Long) value);
        } else if (value instanceof Short) {
            ret = of((Short) value);
        } else if (value instanceof Byte) {
            ret = of((Byte) value);
        } else if (value instanceof Boolean) {
            ret = of((Boolean) value);
        } else if (value instanceof XMLGregorianCalendar) {
            ret = of((XMLGregorianCalendar) value);
        } else if (value instanceof Duration) {
            ret = of((Duration) value);
        } else if (value instanceof QName) {
            ret = of((QName) value);
        } else if (value instanceof byte[]) {
            ret = of((byte[]) value);
        } else if (value instanceof JAXBElement<?>) {
            ret = of((JAXBElement<?>) value);
        } else if (value instanceof Cloneable) {
            ret = clone((Cloneable) value);
        } else {
            throw new IllegalArgumentException("Value " + value + "is not supported for structured copy");
        }
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
        return (T) value.asReferenceValue().clone().asReferencable();
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
        return new JAXBElement<T>(of(value.getName()), value.getDeclaredType(), dispatch(value.getValue()));
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
