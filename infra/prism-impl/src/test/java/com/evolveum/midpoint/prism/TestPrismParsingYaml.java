/*
 * Copyright (c) 2010-2019 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism;

public class TestPrismParsingYaml extends TestPrismParsing {

    @Override
    protected String getSubdirName() {
        return "yaml";
    }

    @Override
    protected String getFilenameSuffix() {
        return "yaml";
    }

    @Override
    protected String getOutputFormat() {
        return PrismContext.LANG_YAML;
    }

}
