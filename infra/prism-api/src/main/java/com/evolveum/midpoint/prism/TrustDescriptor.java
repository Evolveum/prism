/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism;

import java.io.Serializable;

/**
 * A marker interface for client-side trust descriptors that can be attached to selected generated beans via
 * explicit setters and getters.
 *
 * The idea is to be able to attach some client-side trust information to selected data values, e.g. to mark them
 * as "trusted" or "untrusted" and later to treat them accordingly. The actual implementation of the trust
 * descriptor is up to the client code.
 *
 * Unlike {@link ValueMetadata}, trust descriptors are NOT serialized into external representations of prism values
 * (XML, JSON, YAML). They are meant to be used only between client code components, and not to be persisted or transmitted.
 *
 * Expected use case (midPoint): expressions can be trusted to some degree, depending on whether they were
 * provided by a trusted source (e.g. a system administrator) or by an untrusted source (e.g. a regular user).
 * The trust is determined indirectly by looking at the type and archetype of the object that holds the expression.
 * See https://docs.evolveum.com/midpoint/reference/master/expressions/expressions/profiles/. MidPoint trust descriptors
 * therefore embed the information about the originating object type and archetype(s). The execution engine then uses this
 * information to determine the appropriate expression execution profile.
 *
 * These descriptors are meant to be immutable, serializable, and lightweight.
 *
 * When cloning, the trust descriptor itself is not cloned, but the reference is copied. This is because the trust descriptor
 * is expected to be immutable and shared across multiple prism values.
 */
public interface TrustDescriptor extends Serializable {
}
