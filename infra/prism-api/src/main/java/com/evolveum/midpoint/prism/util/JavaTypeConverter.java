/*
 * Copyright (c) 2013 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.Date;
import jakarta.xml.bind.annotation.XmlEnum;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.PrismPropertyValue;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.prism.xml.XmlTypeConverter;
import com.evolveum.midpoint.util.QNameUtil;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;

/**
 * Generic universal type converter. It is supposed to covert anything to anything as long
 * as there is any way to convert it. This means converting string containing a decimal representation
 * of a number to int, PolyString to string, etc. This is supposed to work in a fashion similar to
 * many scripting languages (e.g. perl) where the programmer does not really care about the type
 * and the type conversion is done automatically.
 * <p>
 * TODO clean this up as it is now part of prism-api!
 *
 * @author Radovan Semancik
 */
public class JavaTypeConverter {

    private static final String[] DATE_FORMATS = {
            "yyyy-MM-dd'T'HH:mm:ss.SSSX",
            "yyyy-MM-dd'T'HH:mm:ssz",
            "EEE, dd MMM yyyy HH:mm:ss z",
            "EEE, dd MMM yyyy HH:mm zzzz",
            "EEE MMM dd HH:mm:ss z yyyy",
            "yyyy-MM-dd'T'HH:mm:ssZ",
            "yyyy-MM-dd'T'HH:mm:ss.SSSzzzz",
            "yyyy-MM-dd'T'HH:mm:sszzzz",
            "yyyy-MM-dd'T'HH:mm:ss z",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HHmmss.SSSz",
            "yyyy-MM-dd"
    };

    @SuppressWarnings("unchecked")
    public static <T> T convert(Class<T> expectedType, Object rawValue) {
        return (T) convert(expectedType, rawValue, true);
    }

    public static Object convert(Class<?> expectedType, Object rawValue, boolean failIfImpossible) {
        if (rawValue == null || expectedType.isInstance(rawValue)) {
            return rawValue;
        }
        if (rawValue instanceof PrismPropertyValue<?>) {
            rawValue = ((PrismPropertyValue<?>) rawValue).getValue();
        }
        // This really needs to be checked twice
        if (rawValue == null || expectedType.isInstance(rawValue)) {
            return rawValue;
        }

        // Primitive types

        // boolean
        if (expectedType == boolean.class && rawValue instanceof Boolean) {
            return rawValue;
        }
        if ((expectedType == Boolean.class || expectedType == boolean.class) && rawValue instanceof String) {
            return Boolean.parseBoolean((((String) rawValue)).trim());
        }
        if ((expectedType == Boolean.class || expectedType == boolean.class) && rawValue instanceof PolyString) {
            return Boolean.parseBoolean((rawValue.toString().trim()));
        }
        if (expectedType == String.class && rawValue instanceof Boolean) {
            return rawValue.toString();
        }

        // int
        if (expectedType == Integer.class || expectedType == int.class) {
            if (rawValue instanceof Integer) {
                return rawValue;
            }
            if (rawValue instanceof Number number) {
                return number.intValue();
            }
            if (rawValue instanceof String stringValue) {
                return Integer.parseInt(stringValue.trim());
            }
        }
        if (expectedType == String.class && rawValue instanceof Integer) {
            return rawValue.toString();
        }

        // long
        if (expectedType == Long.class || expectedType == long.class) {
            if (rawValue instanceof Long) {
                return rawValue;
            }
            if (rawValue instanceof Number number) {
                return number.longValue();
            }
            if (rawValue instanceof String stringValue) {
                return Long.parseLong(stringValue.trim());
            }
        }
        if (expectedType == String.class && rawValue instanceof Long) {
            return rawValue.toString();
        }

        if (expectedType == float.class && rawValue instanceof Float) {
            return rawValue;
        }
        if (expectedType == Float.class && rawValue instanceof String) {
            return Float.parseFloat(((String) rawValue).trim());
        }
        if (expectedType == float.class && rawValue instanceof String) {
            return Float.parseFloat(((String) rawValue).trim());
        }
        if (expectedType == String.class && rawValue instanceof Float) {
            return rawValue.toString();
        }
        if ((expectedType == float.class || expectedType == Float.class) && rawValue instanceof Number) {
            return ((Number) rawValue).floatValue();
        }

        if (expectedType == double.class && rawValue instanceof Double) {
            return rawValue;
        }
        if (expectedType == Double.class && rawValue instanceof String) {
            return Double.parseDouble(((String) rawValue).trim());
        }
        if (expectedType == double.class && rawValue instanceof String) {
            return Double.parseDouble(((String) rawValue).trim());
        }
        if ((expectedType == double.class || expectedType == Double.class) && rawValue instanceof Number) {
            return ((Number) rawValue).doubleValue();
        }
        if (expectedType == String.class && rawValue instanceof Double) {
            return rawValue.toString();
        }

        if (expectedType == byte.class && rawValue instanceof Byte) {
            return rawValue;
        }
        if (expectedType == Byte.class && rawValue instanceof String) {
            return Byte.parseByte(((String) rawValue));
        }
        if (expectedType == byte.class && rawValue instanceof String) {
            return Byte.parseByte(((String) rawValue));
        }
        if (expectedType == String.class && rawValue instanceof Byte) {
            return rawValue.toString();
        }

        if (expectedType == BigInteger.class && rawValue instanceof String) {
            return new BigInteger(((String) rawValue).trim());
        }
        if (expectedType == String.class && rawValue instanceof BigInteger) {
            return rawValue.toString().trim();
        }
        if (expectedType == BigInteger.class && rawValue instanceof Integer) {
            return BigInteger.valueOf((int) rawValue);
        }
        if (expectedType == BigInteger.class && rawValue instanceof Long) {
            return BigInteger.valueOf((long) rawValue);
        }
        if ((expectedType == Integer.class || expectedType == int.class) && rawValue instanceof BigInteger) {
            return ((BigInteger) rawValue).intValueExact(); // we must throw an exception if the conversion is not possible
        }
        if ((expectedType == Long.class || expectedType == long.class) && rawValue instanceof BigInteger) {
            return ((BigInteger) rawValue).longValueExact(); // we must throw an exception if the conversion is not possible
        }

        if (expectedType == BigDecimal.class && rawValue instanceof String) {
            return new BigDecimal(((String) rawValue).trim());
        }
        if (expectedType == BigDecimal.class && rawValue instanceof Integer) {
            return BigDecimal.valueOf((int) rawValue);
        }
        if (expectedType == BigDecimal.class && rawValue instanceof Long) {
            return BigDecimal.valueOf((long) rawValue);
        }
        if (expectedType == BigDecimal.class && rawValue instanceof Number) {
            // https://stackoverflow.com/questions/16216248/convert-java-number-to-bigdecimal-best-way
            return new BigDecimal(rawValue.toString());
        }
        if (expectedType == String.class && rawValue instanceof BigDecimal) {
            return rawValue.toString().trim();
        }

        if (expectedType == PolyString.class && rawValue instanceof String) {
            return new PolyString((String) rawValue);
        }
        if (expectedType == PolyStringType.class && rawValue instanceof String) {
            PolyStringType polyStringType = new PolyStringType();
            polyStringType.setOrig((String) rawValue);
            return polyStringType;
        }
        if (expectedType == String.class && rawValue instanceof PolyString) {
            return ((PolyString) rawValue).getOrig();
        }
        if (expectedType == String.class && rawValue instanceof PolyStringType) {
            return ((PolyStringType) rawValue).getOrig();
        }
        if (expectedType == PolyString.class && rawValue instanceof PolyStringType) {
            return ((PolyStringType) rawValue).toPolyString();
        }
        if (expectedType == PolyString.class && rawValue instanceof Integer) {
            return new PolyString(((Integer) rawValue).toString());
        }
        if (expectedType == PolyStringType.class && rawValue instanceof PolyString) {
            return new PolyStringType((PolyString) rawValue);
        }
        if (expectedType == PolyStringType.class && rawValue instanceof Integer) {
            return new PolyStringType(((Integer) rawValue).toString());
        }

        // Date and time
        if (expectedType == XMLGregorianCalendar.class && rawValue instanceof Long) {
            return XmlTypeConverter.createXMLGregorianCalendar((Long) rawValue);
        }
        if (expectedType == XMLGregorianCalendar.class && rawValue instanceof String) {
            return magicDateTimeParse((String) rawValue);
        }
        if (expectedType == String.class && rawValue instanceof XMLGregorianCalendar) {
            return ((XMLGregorianCalendar) rawValue).toXMLFormat();
        }
        if ((expectedType == Long.class || expectedType == long.class) && rawValue instanceof XMLGregorianCalendar) {
            return XmlTypeConverter.toMillis((XMLGregorianCalendar) rawValue);
        }

        // XML Enums (JAXB)
        if (expectedType.isEnum() && expectedType.getAnnotation(XmlEnum.class) != null && rawValue instanceof String) {
            return XmlTypeConverter.toXmlEnum(expectedType, ((String) rawValue).trim());
        }
        if (expectedType == String.class && rawValue.getClass().isEnum() && rawValue.getClass().getAnnotation(XmlEnum.class) != null) {
            return XmlTypeConverter.fromXmlEnum(rawValue);
        }

        // Java Enums
        if (expectedType.isEnum() && rawValue instanceof String) {
            //noinspection unchecked,rawtypes
            return Enum.valueOf((Class<Enum>) expectedType, ((String) rawValue).trim());
        }
        if (expectedType == String.class && rawValue.getClass().isEnum()) {
            return rawValue.toString();
        }

        //QName
        if (expectedType == QName.class && rawValue instanceof QName) {
            return rawValue;
        }
        if (expectedType == QName.class && rawValue instanceof String) {
            return QNameUtil.uriToQName(((String) rawValue).trim(), true);
        }

        if (failIfImpossible) {
            throw new IllegalArgumentException("Expected " + expectedType + " type, but got " + rawValue.getClass());
        } else {
            return rawValue;
        }
    }

    private static XMLGregorianCalendar magicDateTimeParse(String stringDate) {
        try {
            return XmlTypeConverter.createXMLGregorianCalendar(stringDate);
        } catch (IllegalArgumentException e) {
            // No XML format. This is still quite OK. It we will try other formats ...
        }
        Date date;
        try {
            date = DateUtils.parseDate(stringDate, DATE_FORMATS);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        return XmlTypeConverter.createXMLGregorianCalendar(date);
    }

    public static <T> boolean isTypeCompliant(@Nullable T value, @Nullable Class<?> expectedClass) {
        if (value == null || expectedClass == null) {
            return true;
        }
        Class<?> wrapped = ClassUtils.primitiveToWrapper(expectedClass);
        return wrapped.isAssignableFrom(value.getClass()); // auto-boxing of primitive types

        // TODO PolyString vs String - should be treated here?
        // TODO int vs long vs ...
    }

}
