/*
 * Copyright (c) 2010-2013 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.util;

import javax.xml.namespace.QName;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.evolveum.midpoint.tools.testng.AbstractUnitTest;

/**
 * @author semancik
 */
public class QNameUtilTest extends AbstractUnitTest {

    @Test
    public void uriToQName1() {
        // Given

        String uri = "http://foo.com/bar#baz";

        // When

        QName qname = QNameUtil.uriToQName(uri);

        // Then

        AssertJUnit.assertEquals(new QName("http://foo.com/bar", "baz"), qname);
    }

    @Test
    public void uriToQName2() {
        // Given

        String uri = "http://foo.com/bar/baz";

        // When

        QName qname = QNameUtil.uriToQName(uri);

        // Then

        AssertJUnit.assertEquals(new QName("http://foo.com/bar", "baz"), qname);
    }

    @Test
    public void qNameToUri1() {
        // Given

        QName qname = new QName("http://foo.com/bar", "baz");

        // When

        String uri = QNameUtil.qNameToUri(qname);

        // Then

        AssertJUnit.assertEquals("http://foo.com/bar#baz", uri);

    }

    @Test
    public void qNameToUri2() {
        // Given

        QName qname = new QName("http://foo.com/bar/", "baz");

        // When

        String uri = QNameUtil.qNameToUri(qname);

        // Then

        AssertJUnit.assertEquals("http://foo.com/bar/baz", uri);

    }

    @Test
    public void qNameToUri3() {
        // Given

        QName qname = new QName("http://foo.com/bar#", "baz");

        // When

        String uri = QNameUtil.qNameToUri(qname);

        // Then

        AssertJUnit.assertEquals("http://foo.com/bar#baz", uri);
    }
}
