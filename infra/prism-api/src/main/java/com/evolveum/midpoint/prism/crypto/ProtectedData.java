/*
 * Copyright (c) 2014-2017 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.crypto;

import com.evolveum.prism.xml.ns._public.types_3.EncryptedDataType;
import com.evolveum.prism.xml.ns._public.types_3.ExternalDataType;
import com.evolveum.prism.xml.ns._public.types_3.HashedDataType;

/**
 * @author Radovan Semancik
 */
public interface ProtectedData<T> {

    byte[] getClearBytes();

    void setClearBytes(byte[] bytes);

    T getClearValue();

    void setClearValue(T data);

    void destroyCleartext();

    boolean canGetCleartext();

    EncryptedDataType getEncryptedDataType();

    void setEncryptedData(EncryptedDataType encryptedDataType);

    ExternalDataType getExternalData();

    void setExternalData(ExternalDataType externalDataType);

    boolean isEncrypted();

    boolean isExternal();

    HashedDataType getHashedDataType();

    void setHashedData(HashedDataType hashedDataType);

    boolean isHashed();

    boolean canSupportType(Class<?> type);
}
