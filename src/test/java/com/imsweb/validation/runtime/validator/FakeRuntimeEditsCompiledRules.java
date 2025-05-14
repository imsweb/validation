/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.validation.runtime.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import groovy.lang.Binding;

import com.imsweb.validation.ValidationContextFunctions;
import com.imsweb.validation.runtime.CompiledRules;

@SuppressWarnings("unused")
public class FakeRuntimeEditsCompiledRules implements CompiledRules {

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
        parameters.add(ValidationContextFunctions.class);
        parameters.add(Map.class);
        return Collections.singletonMap("runtime", parameters);
    }

    /**
     * Fake rule #1
     * @param binding binding
     * @param context context
     * @param functions functions
     * @param runtime runtime
     * @return pass/fail
     */
    public boolean fvrtRule1(Binding binding, Map<String, Object> context, ValidationContextFunctions functions, Map<String, Object> runtime) {
        return "value".equals(runtime.get("key"));
    }

    /**
     * Fake rule #2
     * @param binding binding
     * @param context context
     * @param functions functions
     * @param runtime runtime
     * @return pass/fail
     */
    public boolean fvrtRule2(Binding binding, Map<String, Object> context, ValidationContextFunctions functions, Map<String, Object> runtime) {
        return "other".equals(runtime.get("key"));
    }
}
