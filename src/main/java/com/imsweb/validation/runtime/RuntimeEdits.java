/*
 * Copyright (C) 2018 Information Management Services, Inc.
 */
package com.imsweb.validation.runtime;

/**
 * Interface used for pre-compiled and pre-parsed validators.
 */
public interface RuntimeEdits {

    /**
     * Returns the pre-compiled rules.
     * @return pre-compiled rules, or null if those are not available
     */
    CompiledRules getCompiledRules();

    /**
     * Returns the pre-parsed properties.
     * @return pre-parsed properties, or null if those are not available
     */
    ParsedProperties getParsedProperties();

    /**
     * Returns the pre-parsed context entries.
     * @return pre-parsed context entries, or null if those are not available
     */
    ParsedContexts getParsedContexts();

    /**
     * Returns the pre-parsed referenced lookups.
     * @return pre-parsed referenced lookups, or null if those are not available
     */
    ParsedLookups getParsedLookups();
}
