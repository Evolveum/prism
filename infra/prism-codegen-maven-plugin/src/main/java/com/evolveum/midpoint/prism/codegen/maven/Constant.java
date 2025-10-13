/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.codegen.maven;

import org.apache.maven.plugins.annotations.Parameter;

import com.evolveum.prism.codegen.binding.NamespaceConstantMapping;

public class Constant implements NamespaceConstantMapping {

    @Parameter(required = true)
    public String namespace;

    @Parameter(required = true)
    public String prefix;


    @Parameter(required = true)
    public String name;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }
}
