/*
 * Copyright (c) 2010-2013 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism;

/**
 * This enum defines source from where a change in property value occurred.
 *
 * @author lazyman
 *
 * FIXME: Abstraction leak
 * TODO: Metadata candidate
 */
public enum OriginType {

    SYNC_ACTION, RECONCILIATION, INBOUND, OUTBOUND, ASSIGNMENTS, ACTIVATIONS, CREDENTIALS, USER_ACTION, USER_POLICY;
}
