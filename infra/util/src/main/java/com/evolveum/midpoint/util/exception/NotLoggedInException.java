/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.util.exception;

import com.evolveum.midpoint.util.annotation.Experimental;

/**
 * No user is logged in, although it is expected.
 *
 * The main purpose of this exception is to allow redirection to the login page if there's no currently logged-in user.
 */
@Experimental
public class NotLoggedInException extends SecurityViolationException {

}
