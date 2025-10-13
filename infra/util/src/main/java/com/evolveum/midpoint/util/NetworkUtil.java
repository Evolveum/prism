/*
 * Copyright (c) 2019 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.util;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetworkUtil {

    @Nullable
    public static String getLocalHostNameFromOperatingSystem() throws UnknownHostException {
        // Not entirely correct. But we have no other option here
        // other than go native or execute a "hostname" shell command.
        // We do not want to do neither.
        InetAddress localHost = InetAddress.getLocalHost();
        if (localHost == null) {
            String hostname = System.getenv("HOSTNAME");        // Unix
            if (StringUtils.isNotEmpty(hostname)) {
                return hostname;
            }
            hostname = System.getenv("COMPUTERNAME");           // Windows
            if (StringUtils.isNotEmpty(hostname)) {
                return hostname;
            }
            return null;
        }

        String hostname = localHost.getCanonicalHostName();
        if (StringUtils.isNotEmpty(hostname)) {
            return hostname;
        }
        hostname = localHost.getHostName();
        if (StringUtils.isNotEmpty(hostname)) {
            return hostname;
        }
        return localHost.getHostAddress();
    }
}
