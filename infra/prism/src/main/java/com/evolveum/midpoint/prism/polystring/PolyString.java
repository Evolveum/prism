/**
 * Copyright (c) 2011 Evolveum
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
 * Portions Copyrighted 2011 [name of copyright owner]
 */
package com.evolveum.midpoint.prism.polystring;

import com.evolveum.midpoint.prism.Recomputable;
import com.evolveum.midpoint.prism.Structured;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.path.ItemPathSegment;
import com.evolveum.midpoint.prism.path.NameItemPathSegment;
import com.evolveum.midpoint.util.DebugDumpable;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.Dumpable;
import com.evolveum.prism.xml.ns._public.types_2.PolyStringType;

import java.io.Serializable;

import javax.xml.namespace.QName;

/**
 * Polymorphic string. String that may have more than one representation at
 * the same time. The primary representation is the original version that is
 * composed of the full Unicode character set. The other versions may be
 * normalized to trim it, normalize character case, normalize spaces,
 * remove national characters or even transliterate the string.
 * 
 * PolyString is (almost) immutable. The original value is immutable, but the
 * other normalized values can be changed. However the only way to change them
 * is to recompute them from the original value.
 * 				
 * @author Radovan Semancik
 */
public class PolyString implements Recomputable, Structured, Dumpable, DebugDumpable, Serializable {

	private final String orig;
	private String norm = null;
	
	public PolyString(String orig) {
		super();
		if (orig == null) {
			throw new IllegalArgumentException("Cannot create PolyString with null orig");
		}
		this.orig = orig;
	}
	
	public PolyString(String orig, String norm) {
		super();
		if (orig == null) {
			throw new IllegalArgumentException("Cannot create PolyString with null orig");
		}
		this.orig = orig;
		this.norm = norm;
	}

	public String getOrig() {
		return orig;
	}

	public String getNorm() {
		return norm;
	}
	
	public boolean isEmpty() {
		if (orig == null) {
			return true;
		}
		return orig.isEmpty();
	}
	
	public void recompute(PolyStringNormalizer normalizer) {
		norm = normalizer.normalize(orig);
	}
	
	public boolean isComputed() {
		return !(norm == null);
	}
	
	@Override
	public Object resolve(ItemPath subpath) {
		if (subpath == null || subpath.isEmpty()) {
			return this;
		}
		if (subpath.size() > 1) {
			throw new IllegalArgumentException("Cannot resolve path "+subpath+" on polystring "+this+", the path is too deep");
		}
		if (!(subpath.first() instanceof NameItemPathSegment)) {
			throw new IllegalArgumentException("Cannot resolve non-name path "+subpath+" on polystring "+this);
		}
		QName itemName = ((NameItemPathSegment)subpath.first()).getName();
		if ("orig".equals(itemName.getLocalPart())) {
			return orig;
		} else if ("norm".equals(itemName.getLocalPart())) {
			return norm;
		} else {
			throw new IllegalArgumentException("Unknown path segment "+itemName);
		}
	}
	
	// Groovy operator overload
	public PolyString plus(PolyString other) {
		return new PolyString(this.orig + other.orig);
	}

	// Groovy operator overload
	public PolyString plus(String other) {
		return new PolyString(this.orig + other);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((norm == null) ? 0 : norm.hashCode());
		result = prime * result + ((orig == null) ? 0 : orig.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PolyString other = (PolyString) obj;
		if (norm == null) {
			if (other.norm != null)
				return false;
		} else if (!norm.equals(other.norm))
			return false;
		if (orig == null) {
			if (other.orig != null)
				return false;
		} else if (!orig.equals(other.orig))
			return false;
		return true;
	}

	@Override
	public boolean equalsOriginalValue(Recomputable obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PolyString other = (PolyString) obj;
		if (orig == null) {
			if (other.orig != null)
				return false;
		} else if (!orig.equals(other.orig))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return orig;
	}
	
	@Override
	public String dump() {
		return debugDump();
	}

	@Override
	public String debugDump() {
		return debugDump(0);
	}

	@Override
	public String debugDump(int indent) {
		StringBuilder sb = new StringBuilder();
		DebugUtil.indentDebugDump(sb, indent);
		sb.append("PolyString(");
		sb.append(orig);
		if (norm != null) {
			sb.append(",");
			sb.append(norm);
		}
		sb.append(")");
		return sb.toString();
	}

    public static String getOrig(PolyString s) {
        return s != null ? s.getOrig() : null;
    }

    public static String getOrig(PolyStringType s) {
        return s != null ? s.getOrig() : null;
    }
	
}
