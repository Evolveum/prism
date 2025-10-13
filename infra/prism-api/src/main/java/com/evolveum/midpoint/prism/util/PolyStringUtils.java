/*
 * Copyright (c) 2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.util;

import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;

/**
 * @author semancik
 */
public class PolyStringUtils {

    public static boolean isEmpty(PolyString polyString) {
        if (polyString == null) {
            return true;
        }
        return polyString.isEmpty();
    }

    public static boolean isNotEmpty(PolyString polyString) {
        if (polyString == null) {
            return false;
        }
        return !polyString.isEmpty();
    }

    public static boolean isEmpty(PolyStringType polyString) {
        if (polyString == null) {
            return true;
        }
        return polyString.isEmpty();
    }

    public static boolean isNotEmpty(PolyStringType polyString) {
        if (polyString == null) {
            return false;
        }
        return !polyString.isEmpty();
    }

}
