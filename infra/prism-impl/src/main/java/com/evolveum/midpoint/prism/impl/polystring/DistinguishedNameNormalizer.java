/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
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
        LdapName normalizedDn;
        try {
            LdapName dn = new LdapName(orig);
            normalizedDn = new LdapName(dn.getRdns());
        } catch (InvalidNameException e) {
            throw new SchemaException("String '" + orig + "' is not a DN: " + e.getMessage(), e);
        }
        return normalizedDn.toString().toLowerCase();
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
