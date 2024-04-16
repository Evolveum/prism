/*
 * Copyright (c) 2010-2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.crypto;

import com.evolveum.midpoint.util.LocalizableMessage;
import com.evolveum.midpoint.util.exception.CommonException;

public class EncryptionException extends CommonException {

    private static final long serialVersionUID = 8289563205061329615L;

    public EncryptionException(String message) {
        super(message);
    }

    public EncryptionException(LocalizableMessage userFriendlyMessage) {
        super(userFriendlyMessage);
    }

    public EncryptionException(Throwable cause) {
        super(cause);
    }

    public EncryptionException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getErrorTypeMessage() {
        return "Encryption error";
    }
}
