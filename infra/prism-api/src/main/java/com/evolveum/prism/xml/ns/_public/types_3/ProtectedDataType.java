/*
 * Copyright (c) 2010-2019 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.xml.ns._public.types_3;

import java.io.Serializable;
import java.util.*;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.*;
import javax.xml.namespace.QName;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;

import com.evolveum.midpoint.prism.JaxbVisitable;
import com.evolveum.midpoint.prism.binding.PlainStructured;
import com.evolveum.midpoint.prism.crypto.ProtectedData;
import com.evolveum.midpoint.prism.crypto.Protector;
import com.evolveum.midpoint.prism.util.CloneUtil;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.QNameUtil;

/**
 * This class was originally generated. But it was heavily modified by hand.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProtectedDataType", propOrder = {
        "content"
})
@XmlSeeAlso({
        ProtectedByteArrayType.class,
        ProtectedStringType.class
})
public abstract class ProtectedDataType<T> implements ProtectedData<T>, PlainStructured.WithoutStrategy, JaxbVisitable {
    private static final long serialVersionUID = 1L;

    public static final QName COMPLEX_TYPE = new QName("http://prism.evolveum.com/xml/ns/public/types-3", "ProtectedDataType");
    public static final QName F_ENCRYPTED_DATA = new QName("http://prism.evolveum.com/xml/ns/public/types-3", "encryptedData");
    public static final QName F_HASHED_DATA = new QName("http://prism.evolveum.com/xml/ns/public/types-3", "hashedData");
    public static final QName F_EXTERNAL_DATA = new QName("http://prism.evolveum.com/xml/ns/public/types-3", "externalData");
    public static final QName F_CLEAR_VALUE = new QName("http://prism.evolveum.com/xml/ns/public/types-3", "clearValue");

    public static final String NS_XML_ENC = "http://www.w3.org/2001/04/xmlenc#";
    public static final String NS_XML_DSIG = "http://www.w3.org/2000/09/xmldsig#";
    public static final QName F_XML_ENC_ENCRYPTED_DATA = new QName(NS_XML_ENC, "EncryptedData");
    public static final QName F_XML_ENC_ENCRYPTION_METHOD = new QName(NS_XML_ENC, "EncryptionMethod");
    public static final String ATTRIBUTE_XML_ENC_ALGORITHM = "Algorithm";
    public static final QName F_XML_ENC_ALGORITHM = new QName(NS_XML_ENC, ATTRIBUTE_XML_ENC_ALGORITHM);
    public static final QName F_XML_ENC_CIPHER_DATA = new QName(NS_XML_ENC, "CipherData");
    public static final QName F_XML_ENC_CIPHER_VALUE = new QName(NS_XML_ENC, "CipherValue");
    public static final QName F_XML_DSIG_KEY_INFO = new QName(NS_XML_DSIG, "KeyInfo");
    public static final QName F_XML_DSIG_KEY_NAME = new QName(NS_XML_DSIG, "KeyName");

    @XmlTransient
    private EncryptedDataType encryptedDataType;

    @XmlTransient
    private HashedDataType hashedDataType;

    @XmlTransient
    private ExternalDataType externalDataType;

    @XmlTransient
    private T clearValue;

    @XmlElementRef(name = "encryptedData", namespace = "http://prism.evolveum.com/xml/ns/public/types-3", type = JAXBElement.class)
    @XmlMixed
    @XmlAnyElement(lax = true)
    protected List<Object> content;

    /**
     * TODO
     * May be either encrypted or hashed or provided in the clear (e.g. for debugging).
     * <p>
     * This type is marked as "mixed" because it may have alternative representation where
     * just the plaintext value is presented as the only value.
     * <p>
     * This is considered to be primitive built-in type for prism objects.
     * Gets the value of the content property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the content property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getContent().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * {@link String }
     * {@link JAXBElement }{@code <}{@link EncryptedDataType }{@code >}
     */
    public List<Object> getContent() {
        if (content == null) {
            content = new ContentList();
        }
        return this.content;
    }

    @Override
    public ExternalDataType getExternalData() {
        return externalDataType;
    }

    @Override
    public void setExternalData(ExternalDataType externalDataType) {
        this.externalDataType = externalDataType;
    }

    @Override
    public EncryptedDataType getEncryptedDataType() {
        return encryptedDataType;
    }

    @Override
    public void setEncryptedData(EncryptedDataType encryptedDataType) {
        this.encryptedDataType = encryptedDataType;
    }

    @Override
    public boolean isEncrypted() {
        return encryptedDataType != null;
    }

    @Override
    public HashedDataType getHashedDataType() {
        return hashedDataType;
    }

    @Override
    public void setHashedData(HashedDataType hashedDataType) {
        this.hashedDataType = hashedDataType;
    }

    @Override
    public boolean isHashed() {
        return hashedDataType != null;
    }

    @Override
    public T getClearValue() {
        return clearValue;
    }

    @Override
    public void setClearValue(T clearValue) {
        this.clearValue = clearValue;
    }

    public ProtectedDataType<T> clearValue(T clearValue) {
        setClearValue(clearValue);
        return this;
    }

    @Override
    public boolean canGetCleartext() {
        return clearValue != null || encryptedDataType != null;
    }

    @Override
    public void destroyCleartext() {
        // Not perfect. But OK for now.
        clearValue = null;
    }

    private JAXBElement<EncryptedDataType> toJaxbElement(EncryptedDataType encryptedDataType) {
        return new JAXBElement<>(F_ENCRYPTED_DATA, EncryptedDataType.class, encryptedDataType);
    }

    private JAXBElement<HashedDataType> toJaxbElement(HashedDataType hashedDataType) {
        return new JAXBElement<>(F_ENCRYPTED_DATA, HashedDataType.class, hashedDataType);
    }

    private JAXBElement<ExternalDataType> toJaxbElement(ExternalDataType externalDataType) {
        return new JAXBElement<>(F_EXTERNAL_DATA, ExternalDataType.class, externalDataType);
    }

    public void clear() {
        clearValue = null;
        encryptedDataType = null;
        hashedDataType = null;
        externalDataType = null;
    }

    private boolean addContent(Object newObject) {
        if (newObject instanceof String) {
            String s = (String) newObject;
            if (StringUtils.isNotBlank(s)) {
                clearValue = (T) s;
            }
            return true;
        } else if (newObject instanceof JAXBElement<?>) {
            JAXBElement<?> jaxbElement = (JAXBElement<?>) newObject;
            if (QNameUtil.match(F_ENCRYPTED_DATA, jaxbElement.getName())) {
                encryptedDataType = (EncryptedDataType) jaxbElement.getValue();
                return true;
            } else if (QNameUtil.match(F_HASHED_DATA, jaxbElement.getName())) {
                hashedDataType = (HashedDataType) jaxbElement.getValue();
                return true;
            } else {
                throw new IllegalArgumentException("Attempt to add unknown JAXB element " + jaxbElement);
            }
        } else if (newObject instanceof Element) {
            Element element = (Element) newObject;
            QName elementName = DOMUtil.getQName(element);
            if (QNameUtil.match(F_XML_ENC_ENCRYPTED_DATA, elementName)) {
                encryptedDataType = convertXmlEncToEncryptedDate(element);
                return true;
            } else if (QNameUtil.match(F_CLEAR_VALUE, elementName)) {
                clearValue = (T) element.getTextContent();
                return true;
            } else {
                throw new IllegalArgumentException("Attempt to add unknown DOM element " + elementName);
            }
        } else {
            throw new IllegalArgumentException("Attempt to add unknown object " + newObject + " (" + newObject.getClass() + ")");
        }
    }

    private EncryptedDataType convertXmlEncToEncryptedDate(Element eEncryptedData) {
        EncryptedDataType encryptedDataType = new EncryptedDataType();
        Element eEncryptionMethod = DOMUtil.getChildElement(eEncryptedData, F_XML_ENC_ENCRYPTION_METHOD);
        if (eEncryptionMethod != null) {
            String algorithm = eEncryptionMethod.getAttribute(ATTRIBUTE_XML_ENC_ALGORITHM);
            EncryptionMethodType encryptionMethodType = new EncryptionMethodType();
            encryptionMethodType.setAlgorithm(algorithm);
            encryptedDataType.setEncryptionMethod(encryptionMethodType);
        }
        Element eKeyInfo = DOMUtil.getChildElement(eEncryptedData, F_XML_DSIG_KEY_INFO);
        if (eKeyInfo != null) {
            KeyInfoType keyInfoType = new KeyInfoType();
            encryptedDataType.setKeyInfo(keyInfoType);
            Element eKeyName = DOMUtil.getChildElement(eKeyInfo, F_XML_DSIG_KEY_NAME);
            if (eKeyName != null) {
                keyInfoType.setKeyName(eKeyName.getTextContent());
            }
        }
        Element eCipherData = DOMUtil.getChildElement(eEncryptedData, F_XML_ENC_CIPHER_DATA);
        if (eCipherData != null) {
            CipherDataType cipherDataType = new CipherDataType();
            encryptedDataType.setCipherData(cipherDataType);
            Element eCipherValue = DOMUtil.getChildElement(eCipherData, F_XML_ENC_CIPHER_VALUE);
            if (eCipherValue != null) {
                String cipherValue = eCipherValue.getTextContent();
                byte[] cipherValueBytes = Base64.decodeBase64(cipherValue);
                cipherDataType.setCipherValue(cipherValueBytes);
            }
        }
        return encryptedDataType;
    }

    public boolean isEmpty() {
        return encryptedDataType == null && hashedDataType == null && externalDataType == null && clearValue == null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(encryptedDataType, hashedDataType, externalDataType, clearValue);
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * This is standard Java equality comparison. I.e. it will return true if
     * the Java objects contain the same data. This means that both object must
     * use the same protection mechanism (enctyption,hash), same keys must be used,
     * ciphertext or hashes must be the same and so on. If this method returns true
     * then obviously also the cleartext data are the same. However, if this method
     * returns false then no information about the cleartext data can be inferred.
     * Cleartext data may still be the same in both objects. Therefore this method
     * is not suitable for almost any practical purpose. It is here mostly just to keep
     * the Java interface contract.
     * <p>
     * See the methods of Protector for a more practical comparison algorithms.
     *
     * @see Protector#compareCleartext(ProtectedStringType, ProtectedStringType)
     * @see Protector#areEquivalent(ProtectedStringType, ProtectedStringType)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ProtectedDataType<?> that = (ProtectedDataType<?>) o;
        return Objects.equals(encryptedDataType, that.encryptedDataType)
                && Objects.equals(hashedDataType, that.hashedDataType)
                && Objects.equals(clearValue, that.clearValue)
                && Objects.equals(externalDataType, that.externalDataType);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName());
        sb.append("(");
        if (encryptedDataType != null) {
            sb.append("encrypted=");
            sb.append(encryptedDataType);
        }
        if (hashedDataType != null) {
            sb.append("hashed=");
            sb.append(hashedDataType);
        }
        if (clearValue != null) {
            sb.append("clearValue=");
            sb.append(clearValue);
        }
        if (externalDataType != null) {
            sb.append("external=");
            sb.append(externalDataType);
        }
        sb.append(")");
        return sb.toString();
    }

    protected void cloneTo(ProtectedDataType<T> cloned) {
        cloned.clearValue = CloneUtil.clone(clearValue);
        cloned.encryptedDataType = CloneUtil.clone(encryptedDataType);
        cloned.hashedDataType = CloneUtil.clone(hashedDataType);
        cloned.externalDataType = CloneUtil.clone(externalDataType);

        // content is virtual, there is no point in copying it
    }

    @Override
    public abstract ProtectedDataType<T> clone();

    class ContentList implements List<Object>, Serializable {

        @Override
        public int size() {
            if (encryptedDataType != null || hashedDataType != null || externalDataType != null) {
                return 1;
            } else {
                return 0;
            }
        }

        @Override
        public boolean isEmpty() {
            return encryptedDataType == null && hashedDataType == null && externalDataType == null;
        }

        @Override
        public boolean contains(Object o) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public @NotNull Iterator<Object> iterator() {
            return new Iterator<>() {
                private int index = 0;

                @Override
                public boolean hasNext() {
                    return index == 0;
                }

                @Override
                public Object next() {
                    if (index == 0) {
                        index++;
                        if (encryptedDataType != null) {
                            return toJaxbElement(encryptedDataType);
                        } else if (hashedDataType != null) {
                            return toJaxbElement(hashedDataType);
                        } else {
                            return toJaxbElement(externalDataType);
                        }
                    } else {
                        return null;
                    }
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        public Object[] toArray() {
            if (encryptedDataType == null && hashedDataType == null) {
                return new Object[0];
            } else {
                Object[] a = new Object[1];
                if (encryptedDataType == null) {
                    a[0] = toJaxbElement(hashedDataType);
                } else {
                    a[0] = toJaxbElement(encryptedDataType);
            }
                return a;
            }
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return (T[]) toArray();
        }

        @Override
        public boolean add(Object e) {
            return addContent(e);
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            for (Object e : c) {
                if (!contains(e)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean addAll(Collection<?> c) {
            boolean changed = false;
            for (Object e : c) {
                if (add(e)) {
                    changed = true;
                }
            }
            return changed;
        }

        @Override
        public boolean addAll(int index, Collection<?> c) {
            throw new UnsupportedOperationException("we are too lazy for this");
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            // We would normally throw an exception here. but JAXB is actually using it.
            ProtectedDataType.this.clear();
        }

        @Override
        public Object get(int index) {
            if (index == 0) {
                // what if encryptedDataType is null and clearValue is set? [pm]
                if (encryptedDataType == null) {
                    return toJaxbElement(hashedDataType);
                } else {
                    return toJaxbElement(encryptedDataType);
                }
            } else {
                return null;
            }
        }

        @Override
        public Object set(int index, Object element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(int index, Object element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object remove(int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int indexOf(Object o) {
            throw new UnsupportedOperationException("we are too lazy for this");
        }

        @Override
        public int lastIndexOf(Object o) {
            throw new UnsupportedOperationException("we are too lazy for this");
        }

        @Override
        public ListIterator<Object> listIterator() {
            throw new UnsupportedOperationException("we are too lazy for this");
        }

        @Override
        public ListIterator<Object> listIterator(int index) {
            throw new UnsupportedOperationException("we are too lazy for this");
        }

        @Override
        public List<Object> subList(int fromIndex, int toIndex) {
            throw new UnsupportedOperationException("we are too lazy for this");
        }
    }
}
