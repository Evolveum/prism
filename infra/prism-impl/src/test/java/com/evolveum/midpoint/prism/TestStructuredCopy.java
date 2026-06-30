/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism;

import static org.testng.AssertJUnit.*;

import java.util.List;
import javax.xml.namespace.QName;

import org.testng.annotations.Test;

import com.evolveum.midpoint.prism.binding.StructuredCopy;
import com.evolveum.midpoint.prism.impl.PrismReferenceValueImpl;
import com.evolveum.midpoint.prism.polystring.PolyString;

public class TestStructuredCopy extends AbstractPrismTest {

    private static final String OID = "target-oid";
    private static final QName TARGET_TYPE = new QName("http://example.com/test", "OrgType");
    private static final QName RELATION = new QName("http://example.com/test", "default");
    private static final PolyString TARGET_NAME = new PolyString("target-name");

    /**
     * Verifies that Prism reference values can be copied when they appear in
     * generated structured values, e.g. script execution input for async policy actions.
     */
    @Test
    public void testCopyListWithPrismReferenceValue() {
        PrismReferenceValue original = new PrismReferenceValueImpl(OID, TARGET_TYPE);
        original.setRelation(RELATION);
        original.setTargetName(TARGET_NAME);

        List<Object> copy = StructuredCopy.ofList(List.of(original));

        assertEquals("Wrong copy size", 1, copy.size());
        assertNotSame("Reference value should be cloned", original, copy.get(0));
        assertTrue("Wrong copied value type", copy.get(0) instanceof PrismReferenceValue);

        PrismReferenceValue copiedReference = (PrismReferenceValue) copy.get(0);
        assertEquals("Wrong copied OID", OID, copiedReference.getOid());
        assertEquals("Wrong copied target type", TARGET_TYPE, copiedReference.getTargetType());
        assertEquals("Wrong copied relation", RELATION, copiedReference.getRelation());
        assertEquals("Wrong copied target name", TARGET_NAME, copiedReference.getTargetName());
    }
}
