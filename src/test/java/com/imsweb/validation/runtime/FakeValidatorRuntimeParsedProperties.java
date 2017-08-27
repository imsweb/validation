/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.validation.runtime;

import java.util.Collections;
import java.util.Set;

public class FakeValidatorRuntimeParsedProperties implements ParsedProperties {

    @Override
    public String getValidatorId() {
        return "fake-validator-runtime";
    }

    @Override
    public String getValidatorVersion() {
        return "TEST-001-01";
    }

    public Set<String> fvrt_rule1() {
        return Collections.singleton("key");
    }
}
