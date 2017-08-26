/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.validation.runtime;

import java.util.List;
import java.util.Map;

public interface CompiledRules {

    String getValidatorId();

    String getValidatorVersion();

    Map<String, List<Class<?>>> getMethodParameters();
}
