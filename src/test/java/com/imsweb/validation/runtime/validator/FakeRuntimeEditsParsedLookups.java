/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.validation.runtime.validator;

import java.util.Collections;
import java.util.Set;

import com.imsweb.validation.runtime.ParsedLookups;

public class FakeRuntimeEditsParsedLookups implements ParsedLookups {

    @Override
    public String getValidatorId() {
        return "fake-validator-runtime";
    }

    @Override
    public String getValidatorVersion() {
        return "TEST-001-01";
    }

    public Set<String> fvrtRule1() {
        return Collections.singleton("fake-lookup");
    }
}
