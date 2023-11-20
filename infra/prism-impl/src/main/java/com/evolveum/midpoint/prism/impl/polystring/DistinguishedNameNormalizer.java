/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.polystring;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.PrismConstants;
import com.evolveum.midpoint.util.exception.SchemaException;

/** Normalizer for LDAP Distinguished names. */
public class DistinguishedNameNormalizer extends BaseStringNormalizer {

    private static final DistinguishedNameNormalizer INSTANCE = new DistinguishedNameNormalizer();

    @Override
    public String normalize(String orig) throws SchemaException {
        if (orig == null) {
            return null;
        }
        if (orig.isBlank()) {
            return "";
        }
        LdapName dn;
        try {
            dn = new LdapName(orig);
        } catch (InvalidNameException e) {
            throw new SchemaException("String '" + orig + "' is not a DN: " + e.getMessage(), e);
        }
        return dn.toString().toLowerCase();
    }

    @Override
    public boolean match(String a, String b) throws SchemaException {
        if (StringUtils.isBlank(a) && StringUtils.isBlank(b)) {
            return true;
        }
        if (StringUtils.isBlank(a) || StringUtils.isBlank(b)) {
            return false;
        }
        LdapName dnA;
        try {
            dnA = new LdapName(a);
        } catch (InvalidNameException e) {
            throw new SchemaException("String '" + a + "' is not a DN: " + e.getMessage(), e);
        }
        LdapName dnB;
        try {
            dnB = new LdapName(b);
        } catch (InvalidNameException e) {
            throw new SchemaException("String '" + b + "' is not a DN: " + e.getMessage(), e);
        }
        return dnA.equals(dnB);
    }

    @Override
    public boolean matchRegex(String a, String regex) throws SchemaException {

        if (a == null) {
            return false;
        }

        a = DistinguishedNameNormalizer.instance().normalize(a);

        // Simple case-insensitive match
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(a);
        return matcher.matches();
    }

    @Override
    public @NotNull QName getName() {
        return PrismConstants.DISTINGUISHED_NAME_NORMALIZER;
    }

    public static @NotNull DistinguishedNameNormalizer instance() {
        return INSTANCE;
    }
}
