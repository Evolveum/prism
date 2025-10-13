/*
 * Copyright (c) 2010-2017 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism;

import org.testng.annotations.Test;

public class TestCompareXml extends TestCompare{
    @Test
    public void f() {
    }

    @Override
    protected String getSubdirName() {
        return "xml";
    }

    @Override
    protected String getFilenameSuffix() {
        return "xml";
    }
}
