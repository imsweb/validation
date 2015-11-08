/*
 * Copyright (C) 2011 Information Management Services, Inc.
 */
package com.imsweb.validation.entities;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is a wrapper for all the test related to a particular validator.
 * <p/>
 * Created on Aug 8, 2011 by depryf
 */
public class ValidatorTests {

    /** Tested Validator ID */
    protected String _testedValidatorId;

    /** Map of tests */
    protected Map<String, RuleTest> _tests;

    /** @return Returns the testedValidatorId. */
    public String getTestedValidatorId() {
        return _testedValidatorId;
    }

    /**
     * Constructor.
     * <p/>
     * Created on Nov 21, 2011 by depryf
     */
    public ValidatorTests() {
        _tests = new HashMap<>();
    }

    /**
     * Constructor.
     * <p/>
     * Created on Nov 21, 2011 by depryf
     */
    public ValidatorTests(String testedValidatorId) {
        _testedValidatorId = testedValidatorId;
        _tests = new HashMap<>();
    }

    /** @param testedValidatorId The testedValidatorId to set. */
    public void setTestedValidatorId(String testedValidatorId) {
        this._testedValidatorId = testedValidatorId;
    }

    /** @return Returns the tests. */
    public Map<String, RuleTest> getTests() {
        return _tests;
    }

    /** @param tests The tests to set. */
    public void setTests(Map<String, RuleTest> tests) {
        this._tests = tests;
    }
}
