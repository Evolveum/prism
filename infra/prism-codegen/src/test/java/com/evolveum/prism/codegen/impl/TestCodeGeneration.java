/*
 * Copyright (c) 2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.codegen.impl;

import java.io.File;
import java.io.IOException;

import com.sun.codemodel.writer.FileCodeWriter;
import org.testng.annotations.Test;

import com.evolveum.midpoint.prism.AbstractPrismTest;
import com.evolveum.midpoint.prism.ComplexTypeDefinition;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismInternalTestUtil;
import com.evolveum.midpoint.tools.testng.UnusedTestElement;
import com.evolveum.prism.codegen.binding.BindingContext;
import com.evolveum.prism.codegen.binding.TypeBinding;

@UnusedTestElement // TODO do we want this in testng-unit.xml?
public class TestCodeGeneration extends AbstractPrismTest {

    private static final File TARGET = new File("target/test-code");
    private static final String TEST_JAVA_NS = "com.evolveum.prism.example.foo";

    @Test
    public void test000Mapping() throws IOException, CodeGenerationException {
        TARGET.mkdirs();
        PrismContext prismContext = getPrismContext();

        ComplexTypeDefinition userType = prismContext.getSchemaRegistry().findComplexTypeDefinitionByType(PrismInternalTestUtil.USER_TYPE_QNAME);
        BindingContext generator = new BindingContext();
        generator.addSchemas(prismContext.getSchemaRegistry().getSchemas());
        generator.addNamespaceMapping(PrismInternalTestUtil.NS_FOO, TEST_JAVA_NS);

        FileCodeWriter writer = new FileCodeWriter(TARGET);
        BindingContext context = generator.process();

        var codeGen = new CodeGenerator(writer, generator);

        TypeBinding binding = generator.requireBinding(userType.getTypeName());

        codeGen.process(generator.requireBinding(PrismInternalTestUtil.OBJECT_TYPE_QNAME));
        codeGen.process(binding);

        codeGen.process();

        codeGen.write();
    }

}
