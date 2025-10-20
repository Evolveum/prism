/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism;

public class PrismStaticConfiguration {

    private static boolean serializationProxiesEnabled = false;
    private static int propertyIndexThreshold = 50;
    private static boolean propertyIndexEnabled = false;

    public static boolean javaSerializationProxiesEnabled() {
        return serializationProxiesEnabled;
    }

    public static int indexEnableThreshold() {
        return propertyIndexEnabled ? propertyIndexThreshold : Integer.MAX_VALUE;
    }

    public static void setJavaSerializationProxiesEnabled(boolean value) {
        serializationProxiesEnabled = value;
    }

    public static void setPropertyIndexEnabled(boolean value) {
        propertyIndexEnabled = value;
    }

    public static void setPropertyIndexThreshold(int value) {
        propertyIndexThreshold = Math.max(0, value);
    }
}
