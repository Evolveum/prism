/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.marshaller;

import java.lang.reflect.Field;

import com.evolveum.prism.xml.ns._public.types_3.*;

import jakarta.xml.bind.annotation.XmlValue;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.ParsingContext;
import com.evolveum.midpoint.prism.impl.xnode.MapXNodeImpl;
import com.evolveum.midpoint.prism.impl.xnode.PrimitiveXNodeImpl;
import com.evolveum.midpoint.prism.impl.xnode.RootXNodeImpl;
import com.evolveum.midpoint.prism.impl.xnode.XNodeImpl;
import org.apache.commons.lang3.StringUtils;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.crypto.EncryptionException;
import com.evolveum.midpoint.prism.crypto.Protector;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.exception.SchemaException;

public class XNodeProcessorUtil {

    public static <T> void parseProtectedType(ProtectedDataType<T> protectedType, MapXNodeImpl xmap, ParsingContext pc) throws SchemaException {
        RootXNodeImpl xEncryptedData = xmap.getEntryAsRoot(ProtectedDataType.F_ENCRYPTED_DATA);
        if (xEncryptedData != null) {
            if (!(xEncryptedData.getSubnode() instanceof MapXNodeImpl)) {
                throw new SchemaException("Cannot parse encryptedData from "+xEncryptedData);
            }
            EncryptedDataType encryptedDataType = PrismContext.get().parserFor(xEncryptedData).context(pc).parseRealValue(EncryptedDataType.class);
            protectedType.setEncryptedData(encryptedDataType);
        } else {
            // Check for legacy EncryptedData
            RootXNodeImpl xLegacyEncryptedData = xmap.getEntryAsRoot(ProtectedDataType.F_XML_ENC_ENCRYPTED_DATA);
            if (xLegacyEncryptedData != null) {
                if (!(xLegacyEncryptedData.getSubnode() instanceof MapXNodeImpl)) {
                    throw new SchemaException("Cannot parse EncryptedData from "+xEncryptedData);
                }
                RootXNodeImpl xConvertedEncryptedData = (RootXNodeImpl) xLegacyEncryptedData.cloneTransformKeys(in -> {
                    String elementName = StringUtils.uncapitalize(in.getLocalPart());
                    if (elementName.equals("type")) {
                        // this is rubbish, we don't need it, we don't want it
                        return null;
                    }
                    return new QName(null, elementName);
                });

                EncryptedDataType encryptedDataType = PrismContext.get().parserFor(xConvertedEncryptedData).context(pc).parseRealValue(EncryptedDataType.class);
                protectedType.setEncryptedData(encryptedDataType);

                if (protectedType instanceof ProtectedStringType){
                    transformEncryptedValue(protectedType);
                }
            }
        }
        RootXNodeImpl xHashedData = xmap.getEntryAsRoot(ProtectedDataType.F_HASHED_DATA);
        if (xHashedData != null) {
            if (!(xHashedData.getSubnode() instanceof MapXNodeImpl)) {
                throw new SchemaException("Cannot parse hashedData from "+xHashedData);
            }
            HashedDataType hashedDataType = PrismContext.get().parserFor(xHashedData).context(pc).parseRealValue(HashedDataType.class);
            protectedType.setHashedData(hashedDataType);
        }
        RootXNodeImpl xExternalData = xmap.getEntryAsRoot(ProtectedDataType.F_EXTERNAL_DATA);
        if (xExternalData != null) {
            if (!(xExternalData.getSubnode() instanceof MapXNodeImpl)) {
                throw new SchemaException("Cannot parse externalData from "+xExternalData);
            }
            ExternalDataType externalDataType = PrismContext.get().parserFor(xExternalData).context(pc).parseRealValue(ExternalDataType.class);
            protectedType.setExternalData(externalDataType);
        }
        // protected data empty..check for clear value
        if (protectedType.isEmpty()){
            XNodeImpl xClearValue = xmap.get(ProtectedDataType.F_CLEAR_VALUE);
            if (xClearValue == null){
                //TODO: try to use common namespace (only to be compatible with previous versions)
                //FIXME maybe add some warning, info...
                xClearValue = xmap.get(new QName(ProtectedDataType.F_CLEAR_VALUE.getLocalPart()));
            }
            if (xClearValue == null){
                return;
            }
            if (!(xClearValue instanceof PrimitiveXNodeImpl)){
                //this is maybe not good..
                throw new SchemaException("Cannot parse clear value from " + xClearValue);
            }
            // TODO: clearValue
            T clearValue = (T) ((PrimitiveXNodeImpl)xClearValue).getParsedValue(DOMUtil.XSD_STRING, String.class);
            protectedType.setClearValue(clearValue);
        }

    }

    private static void transformEncryptedValue(ProtectedDataType protectedType) throws SchemaException{
        Protector protector = PrismContext.get().getDefaultProtector();
        if (protector == null) {
            return;
        }
        try {
            protector.decrypt(protectedType);
            Object clearValue = protectedType.getClearValue();
            if (clearValue instanceof String){
                String clear = (String) clearValue;
                if (clear.startsWith("<value>") && clear.endsWith("</value>")){
                    clear = clear.replace("<value>","").replace("</value>", "");
                    clearValue = (String) clear;
                }
                protectedType.setClearValue(clearValue);
                protector.encrypt(protectedType);
            }
        } catch (EncryptionException ex) {
            throw new IllegalArgumentException("Failed to transform encrypted value: " + ex.getMessage(), ex);
        }
    }

    public static <T> Field findXmlValueField(Class<T> beanClass) {
        for (Field field: beanClass.getDeclaredFields()) {
            XmlValue xmlValue = field.getAnnotation(XmlValue.class);
            if (xmlValue != null) {
                return field;
            }
        }
        return null;
    }
}
