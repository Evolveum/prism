/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism;

import org.jetbrains.annotations.Nullable;

public interface TrustDescriptorAware {

    @Nullable TrustDescriptor getTrustDescriptor();

    void setTrustDescriptor(@Nullable TrustDescriptor trustDescriptor);
}
