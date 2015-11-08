/*
 * Copyright (C) 2004 Information Management Services, Inc.
 */
package com.imsweb.validation;

/**
 * Base class for all of the validation engine exceptions.
 * <p/>
 * Created on Apr 26, 2011 by depryf
 */
public class ValidationException extends Exception {

    /** Class UID */
    private static final long serialVersionUID = 1L;

    /**
     * Created on Apr 19, 2010 by depryf
     * @param msg message
     */
    public ValidationException(String msg) {
        super(msg);
    }

    /**
     * Created on Apr 19, 2010 by depryf
     * @param msg message
     */
    public ValidationException(Throwable msg) {
        super(msg);
    }

    /**
     * Created on Apr 19, 2010 by depryf
     * @param msg message
     * @param cause cause
     */
    public ValidationException(String msg, Throwable cause) {
        super(msg, cause);
    }

}