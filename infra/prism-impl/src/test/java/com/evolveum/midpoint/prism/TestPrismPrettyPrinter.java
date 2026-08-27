/*
 * Copyright (c) 2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism;

import static org.testng.AssertJUnit.assertEquals;

import javax.xml.namespace.QName;

import org.testng.annotations.Test;

import com.evolveum.midpoint.prism.util.PrismPrettyPrinter;
import com.evolveum.midpoint.util.PrettyPrinter;
import com.evolveum.prism.xml.ns._public.types_3.DeltaSetTripleType;
import com.evolveum.prism.xml.ns._public.types_3.ItemDeltaType;
import com.evolveum.prism.xml.ns._public.types_3.ItemType;
import com.evolveum.prism.xml.ns._public.types_3.ModificationTypeType;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;
import com.evolveum.prism.xml.ns._public.types_3.RawType;

/**
 * Tests of {@link PrismPrettyPrinter} for delta and item structures.
 *
 * See issue 11195 - raw 'toString' output in trace viewers.
 */
public class TestPrismPrettyPrinter extends AbstractPrismTest {

    private static final QName ORGANIZATIONAL_UNIT = new QName("http://midpoint.evolveum.com/xml/ns/public/common/common-3", "organizationalUnit");

    private RawType raw(String value) {
        return new RawType(getPrismContext().itemFactory().createPropertyValue(value), PolyStringType.COMPLEX_TYPE);
    }

    private ItemDeltaType itemDelta(ModificationTypeType modificationType, String path, String... values) {
        ItemDeltaType rv = new ItemDeltaType();
        rv.setModificationType(modificationType);
        if (path != null) {
            rv.setPath(getPrismContext().itemPathParser().asItemPathType(path));
        }
        for (String value : values) {
            rv.getValue().add(raw(value));
        }
        return rv;
    }

    @Test
    public void testItemDeltaType() {
        ItemDeltaType delta = itemDelta(ModificationTypeType.DELETE, "organizationalUnit", "guild");
        delta.getEstimatedOldValue().add(raw("workshop"));
        delta.getEstimatedOldValue().add(raw("guild"));

        assertEquals("organizationalUnit: - guild (old: workshop, guild)", PrismPrettyPrinter.prettyPrint(delta));
    }

    @Test
    public void testItemDeltaTypeAdd() {
        ItemDeltaType delta = itemDelta(ModificationTypeType.ADD, "fullName", "Leonardo da Vinci");

        assertEquals("fullName: + Leonardo da Vinci", PrismPrettyPrinter.prettyPrint(delta));
    }

    @Test
    public void testItemDeltaTypeWithoutPath() {
        ItemDeltaType delta = itemDelta(ModificationTypeType.REPLACE, null, "a", "b");

        assertEquals("= [a, b]", PrismPrettyPrinter.prettyPrint(delta));
    }

    @Test
    public void testItemType() {
        ItemType item = new ItemType();
        item.setName(ORGANIZATIONAL_UNIT);
        item.getValue().add(raw("workshop"));
        item.getValue().add(raw("guild"));

        assertEquals("organizationalUnit: workshop, guild", PrismPrettyPrinter.prettyPrint(item));
    }

    @Test
    public void testDeltaSetTripleType() {
        DeltaSetTripleType triple = new DeltaSetTripleType();
        triple.getZero().add(raw("workshop"));
        triple.getMinus().add(raw("workshop"));
        triple.getMinus().add(raw("guild"));

        assertEquals("Minus: workshop, guild; Zero: workshop", PrismPrettyPrinter.prettyPrint(triple));
    }

    @Test
    public void testPrettyPrinterDispatch() {
        // registry-based path used by e.g. midpoint-studio trace view
        ItemDeltaType delta = itemDelta(ModificationTypeType.DELETE, "organizationalUnit", "guild");
        delta.getEstimatedOldValue().add(raw("workshop"));

        assertEquals(PrismPrettyPrinter.prettyPrint(delta), PrettyPrinter.prettyPrint(delta));

        ItemType item = new ItemType();
        item.setName(ORGANIZATIONAL_UNIT);
        item.getValue().add(raw("workshop"));

        assertEquals(PrismPrettyPrinter.prettyPrint(item), PrettyPrinter.prettyPrint(item));
    }
}
