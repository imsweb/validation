/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.validation.runtime;

/**
 * Interface used for pre-parsed referenced lookups.
 */
public interface ParsedLookups {

    /**
     * Returns the validator ID
     * @return validator ID, never null
     */
    String getValidatorId();

    /**
     * Returns the validator version
     * @return validator version, never null
     */
    String getValidatorVersion();
}
