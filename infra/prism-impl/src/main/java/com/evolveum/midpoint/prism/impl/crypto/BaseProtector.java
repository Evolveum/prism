/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.impl.crypto;

import com.evolveum.midpoint.prism.crypto.EncryptionException;
import com.evolveum.midpoint.prism.crypto.ProtectedData;
import com.evolveum.midpoint.prism.crypto.Protector;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.prism.xml.ns._public.types_3.ProtectedStringType;

/**
 *
 */
public abstract class BaseProtector implements Protector {

    @Override
    public <T> void decrypt(ProtectedData<T> protectedData) throws EncryptionException, SchemaException {
        if (protectedData.isEncrypted()) {
            byte[] decryptedData = decryptBytes(protectedData);
            protectedData.setClearBytes(decryptedData);
            protectedData.setEncryptedData(null);
        } else if (protectedData.isExternal()) {
            throw new EncryptionException("This protector implementation can't resolve external data");
        }
    }

    protected abstract <T> byte[] decryptBytes(ProtectedData<T> protectedData) throws SchemaException, EncryptionException;

    @Override
    public String decryptString(ProtectedData<String> protectedString) throws EncryptionException {
        try {
            if (protectedString.isEncrypted()) {
                byte[] clearBytes = decryptBytes(protectedString);
                return ProtectedStringType.bytesToString(clearBytes);
            } else if (protectedString.isExternal()) {
                throw new EncryptionException("This protector implementation can't resolve external data");
            }

            return protectedString.getClearValue();
        } catch (SchemaException ex) {
            throw new EncryptionException(ex);
        }
    }

    @Override
    public ProtectedStringType encryptString(String text) throws EncryptionException {
        ProtectedStringType protectedString = new ProtectedStringType();
        protectedString.setClearValue(text);
        encrypt(protectedString);
        return protectedString;
    }

}
