/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism;

import java.io.IOException;
import javax.xml.namespace.QName;

import org.assertj.core.api.Assertions;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import com.evolveum.midpoint.prism.impl.PrismContextImpl;
import com.evolveum.midpoint.prism.impl.PrismPropertyDefinitionImpl;
import com.evolveum.midpoint.prism.impl.PrismPropertyValueImpl;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.exception.SchemaException;

public class TestPrismPropertyValueImpl extends AbstractPrismTest {
    @DataProvider
    private Object[][] conversionParameters() {
        return new Object[][] {
                {DOMUtil.XSD_DOUBLE, Double.class},
                {DOMUtil.XSD_FLOAT, Float.class}
        };
    }

    @Test(dataProvider = "conversionParameters")
    void valueIsInteger_applyDefinitionWithDouble_conversionShouldPass(QName definitionType, Class<?> convertedType)
            throws SchemaException, IOException, SAXException {
        final PrismPropertyValueImpl<Integer> value = new PrismPropertyValueImpl<>(10);

        final PrismContextImpl prismContext = PrismInternalTestUtil.constructInitializedPrismContext();
        final PrismPropertyDefinitionImpl<Object> definitionWithDouble = prismContext.definitionFactory()
                .newPropertyDefinition(PrismInternalTestUtil.EXTENSION_NUM_ELEMENT, definitionType);

        final Object realValue = value.applyDefinition(definitionWithDouble).getRealValue();
        Assertions.assertThat(realValue).isInstanceOf(convertedType);
    }
}
