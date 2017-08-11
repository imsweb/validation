/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.validation.validators;

import java.util.Map;
import java.util.Objects;

import groovy.lang.Binding;

import com.imsweb.validation.ValidatorContextFunctions;

public class Validator_test {

    public boolean fvCondition(Binding binding, Map<String, String> level1, Map<String, Object> level2) throws Exception {
        return level2.get("prop2") != "IGNORED";
    }

    public boolean fvRule1(Binding binding,Map<String, Object> level1) throws Exception {
        System.out.println("Executing fvRule1");
        return level1.get("prop") != ValidatorContextFunctions.getInstance().getContext("fake-validator", "FV_CONTEXT1");
    }

    public boolean fvRule2(Binding binding,Map<String, Object> level1, Map<String, Object> level2) throws Exception {
        System.out.println("Executing fvRule2");
        ValidatorContextFunctions.getInstance().forceFailureOnProperty(binding, "level2.otherProp");
        ValidatorContextFunctions.getInstance().ignoreFailureOnProperty(binding, "level2.prop");
        return !Objects.equals(level2.get("prop"), ValidatorContextFunctions.getInstance().getContext("fake-validator", "FV_CONTEXT1"));
    }

    public boolean fvRule3(Binding binding,Map<String, Object> level1, Map<String, Object> level2, Map<String, Object> level3) throws Exception {
        System.out.println("Executing fvRule3");
        return !Objects.equals(level3.get("prop"), ValidatorContextFunctions.getInstance().getContext("fake-validator", "FV_CONTEXT1"));
    }
}
