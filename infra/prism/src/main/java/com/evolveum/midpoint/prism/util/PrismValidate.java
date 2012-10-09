/**
 * Copyright (c) 2012 Evolveum
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1 or
 * CDDLv1.0.txt file in the source code distribution.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 *
 * Portions Copyrighted 2012 [name of copyright owner]
 */
package com.evolveum.midpoint.prism.util;

import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.prism.xml.ns._public.types_2.PolyStringType;

/**
 * @author semancik
 *
 */
public class PrismValidate {
	
	public static void notEmpty(PolyString value, String message) {
		if (value == null || value.isEmpty()) {
			throw new IllegalArgumentException(message);
		}
	}

	public static void notEmpty(PolyStringType value, String message) {
		if (value == null || PrismUtil.isEmpty(value)) {
			throw new IllegalArgumentException(message);
		}
	}

}
