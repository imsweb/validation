/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.validation.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import groovy.lang.Binding;

import com.imsweb.validation.ValidatorContextFunctions;

public class FakeValidatorRuntimeCompiledRules implements CompiledRules {

    @Override
    public String getValidatorId() {
        return "fake-validator-runtime";
    }

    @Override
    public String getValidatorVersion() {
        return "TEST-001-01";
    }

    @Override
    public Map<String, List<Class<?>>> getMethodParameters() {
        List<Class<?>> parameters = new ArrayList<>();
        parameters.add(Binding.class);
        parameters.add(Map.class);
        parameters.add(ValidatorContextFunctions.class);
        parameters.add(Map.class);
        return Collections.singletonMap("level-runtime", parameters);
    }

    public boolean fvrtRule1(Binding binding, Map<String, Object> context, ValidatorContextFunctions functions, Map<String, Object> data) {
        return "value".equals(data.get("key"));
    }
}
