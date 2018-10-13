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
     * Returns the validator ID
     * @return validator ID, never null
     */
    String getValidatorId();

    /**
     * Returns the validator version
     * @return validator version, never null
     */
    String getValidatorVersion();

    Map<String, List<Class<?>>> getMethodParameters();
}
