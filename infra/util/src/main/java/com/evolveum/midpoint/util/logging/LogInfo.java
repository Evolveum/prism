/*
 * Copyright (c) 2010-2013 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.util.logging;

import java.beans.ConstructorProperties;

/**
 *
 * @author Vilo Repan
 */
public class LogInfo {

    private String packageName;
    private int level;

    @ConstructorProperties({"packageName", "level"})
    public LogInfo(String packageName, int level) {
        if (packageName == null || packageName.isEmpty()) {
            throw new IllegalArgumentException("Package name can't be null nor empty.");
        }
        this.packageName = packageName;
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getPackageName() {
        return packageName;
    }

    @Override
    public int hashCode() {
       return packageName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof LogInfo)) {
            return false;
        }

        LogInfo info = (LogInfo) obj;
        return packageName == null ? info.getPackageName() == null
                : packageName.equals(info.getPackageName());
    }
}
