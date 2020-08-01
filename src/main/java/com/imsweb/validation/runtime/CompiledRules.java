/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.validation.runtime;

import java.util.List;
import java.util.Map;

/**
 * Interface used for pre-compiled rules.
 */
public interface CompiledRules {

    /**
     * Returns the validator ID for the compiled edits contained in this class.
     * @return validator ID, never null
     */
    String getValidatorId();

    /**
     * Returns the validator version for the compiled edits contained in this class.
     * @return validator version, never null
     */
    String getValidatorVersion();

    /**
     * Returns the list of parameter types for all java-paths supported by this class.
     * @return the list of parameter types per java-path
     */
    Map<String, List<Class<?>>> getMethodParameters();

    /**
     * Returns true if this class has a method (a compiled edit) for the requested rule ID.
     * @param id rule ID
     * @return true if this class can handle the requested rule ID, false otherwise
     */
    default boolean containsRuleId(String id) {
        return true;
    }
}
