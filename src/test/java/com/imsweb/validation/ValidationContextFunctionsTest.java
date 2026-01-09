/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import groovy.lang.Binding;

import com.imsweb.validation.internal.ExtraPropertyEntityHandlerDto;

public class ValidationContextFunctionsTest {

    private final ValidationContextFunctions _functions = new ValidationContextFunctions();

    @Before
    public void setUp() {
        TestingUtils.init();
    }

    @Test
    public void testDocumentation() {
        List<ContextFunctionDocDto> list = ValidationContextFunctions.getMethodsDocumentation();
        Assert.assertFalse(list.isEmpty());
        for (ContextFunctionDocDto dto : list) {
            Assert.assertNotNull(dto.getMethodName());
            Assert.assertNotNull(dto.getDescription());
            Assert.assertFalse(dto.getDescription().trim().isEmpty());
            Assert.assertNotNull(dto.getExample());
            Assert.assertFalse(dto.getExample().trim().isEmpty());
            Assert.assertEquals(dto.getParams().size(), dto.getParamNames().size());
        }
    }

    @Test
    @SuppressWarnings({"unchecked"})
    public void testForceFailureOnEntity() {
        Binding binding = new Binding();
        binding.setVariable(ValidationEngine.VALIDATOR_FORCE_FAILURE_ENTITY_KEY, null);
        binding.setVariable(ValidationEngine.VALIDATOR_FORCE_FAILURE_PROPERTY_KEY, null);
        binding.setVariable(ValidationEngine.VALIDATOR_IGNORE_FAILURE_PROPERTY_KEY, null);
        Map<String, String> entity = new HashMap<>();

        // null binding
        _functions.forceFailureOnEntity(null, entity);
        Assert.assertNull(binding.getVariables().get(ValidationEngine.VALIDATOR_FORCE_FAILURE_ENTITY_KEY));

        // null entity
        _functions.forceFailureOnEntity(binding, null);
        Assert.assertNull(binding.getVariables().get(ValidationEngine.VALIDATOR_FORCE_FAILURE_ENTITY_KEY));

        // no properties
        _functions.forceFailureOnEntity(binding, entity);
        Assert.assertTrue(binding.getVariables().containsKey(ValidationEngine.VALIDATOR_FORCE_FAILURE_ENTITY_KEY));
        Set<ExtraPropertyEntityHandlerDto> set = (Set<ExtraPropertyEntityHandlerDto>)binding.getVariables().get(ValidationEngine.VALIDATOR_FORCE_FAILURE_ENTITY_KEY);
        Assert.assertEquals(1, set.size());
        Assert.assertEquals(entity, set.iterator().next().getEntity());
        Assert.assertNull(set.iterator().next().getProperties());

        // one property
        _functions.forceFailureOnEntity(binding, entity, "prop1");
        set = (Set<ExtraPropertyEntityHandlerDto>)binding.getVariables().get(ValidationEngine.VALIDATOR_FORCE_FAILURE_ENTITY_KEY);
        Assert.assertEquals(2, set.size());
        binding.setVariable(ValidationEngine.VALIDATOR_FORCE_FAILURE_ENTITY_KEY, null);
        binding.setVariable(ValidationEngine.VALIDATOR_FORCE_FAILURE_PROPERTY_KEY, null);
        binding.setVariable(ValidationEngine.VALIDATOR_IGNORE_FAILURE_PROPERTY_KEY, null);

        // two properties
        _functions.forceFailureOnEntity(binding, entity, "prop1", "prop2");
        set = (Set<ExtraPropertyEntityHandlerDto>)binding.getVariables().get(ValidationEngine.VALIDATOR_FORCE_FAILURE_ENTITY_KEY);
        Assert.assertEquals(1, set.size());
        Assert.assertEquals(entity, set.iterator().next().getEntity());
        Assert.assertEquals(2, set.iterator().next().getProperties().size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testForceFailureOnProperty() {
        Binding binding = new Binding();
        binding.setVariable(ValidationEngine.VALIDATOR_FORCE_FAILURE_ENTITY_KEY, null);
        binding.setVariable(ValidationEngine.VALIDATOR_FORCE_FAILURE_PROPERTY_KEY, null);
        binding.setVariable(ValidationEngine.VALIDATOR_IGNORE_FAILURE_PROPERTY_KEY, null);

        // null binding
        _functions.forceFailureOnProperty(null);
        Assert.assertNull(binding.getVariables().get(ValidationEngine.VALIDATOR_FORCE_FAILURE_PROPERTY_KEY));

        // no properties
        _functions.forceFailureOnProperty(binding);
        Assert.assertNull(binding.getVariables().get(ValidationEngine.VALIDATOR_FORCE_FAILURE_PROPERTY_KEY));

        // one property
        _functions.forceFailureOnProperty(binding, "prop1");
        Set<String> set = (Set<String>)binding.getVariables().get(ValidationEngine.VALIDATOR_FORCE_FAILURE_PROPERTY_KEY);
        Assert.assertEquals(1, set.size());

        // two properties
        _functions.forceFailureOnProperty(binding, "prop1", "prop2");
        set = (Set<String>)binding.getVariables().get(ValidationEngine.VALIDATOR_FORCE_FAILURE_PROPERTY_KEY);
        Assert.assertEquals(2, set.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testIgnoreFailureOnProperty() {
        Binding binding = new Binding();
        binding.setVariable(ValidationEngine.VALIDATOR_FORCE_FAILURE_ENTITY_KEY, null);
        binding.setVariable(ValidationEngine.VALIDATOR_FORCE_FAILURE_PROPERTY_KEY, null);
        binding.setVariable(ValidationEngine.VALIDATOR_IGNORE_FAILURE_PROPERTY_KEY, null);

        // null binding
        _functions.ignoreFailureOnProperty(null);
        Assert.assertNull(binding.getVariables().get(ValidationEngine.VALIDATOR_IGNORE_FAILURE_PROPERTY_KEY));

        // no properties
        _functions.ignoreFailureOnProperty(binding);
        Assert.assertNull(binding.getVariables().get(ValidationEngine.VALIDATOR_IGNORE_FAILURE_PROPERTY_KEY));

        // one property
        _functions.ignoreFailureOnProperty(binding, "prop1");
        Set<String> set = (Set<String>)binding.getVariables().get(ValidationEngine.VALIDATOR_IGNORE_FAILURE_PROPERTY_KEY);
        Assert.assertEquals(1, set.size());

        // two properties
        _functions.ignoreFailureOnProperty(binding, "prop1", "prop2");
        set = (Set<String>)binding.getVariables().get(ValidationEngine.VALIDATOR_IGNORE_FAILURE_PROPERTY_KEY);
        Assert.assertEquals(2, set.size());
    }

    @Test
    public void testGetContext() throws ValidationException {
        TestingUtils.loadValidator("fake-validator");

        // no validator ID
        boolean exception = false;
        try {
            _functions.getContext(null, "FV_CONTEXT1");
        }
        catch (ValidationException e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Was expecting an exception!");
        exception = false;

        // no context key
        try {
            _functions.getContext("fake-validator", null);
        }
        catch (ValidationException e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Was expecting an exception!");
        exception = false;

        // bad validator ID
        try {
            _functions.getContext("?", "FV_CONTEXT1");
        }
        catch (ValidationException e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Was expecting an exception!");
        exception = false;

        // bad context key
        try {
            _functions.getContext("fake-validator", "?");
        }
        catch (ValidationException e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Was expecting an exception!");

        // good validator ID and context key
        Assert.assertNotNull(_functions.getContext("fake-validator", "FV_CONTEXT1"));

        TestingUtils.unloadValidator("fake-validator");
    }

    @Test
    public void testFetchLookup() {

        // null ID
        boolean exception = false;
        try {
            _functions.fetchLookup(null);
        }
        catch (ValidationException e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Was expecting an exception!");
        exception = false;

        // non-null ID -> null (default implementation)
        try {
            Assert.assertNull(_functions.fetchLookup("lkup_id"));
        }
        catch (ValidationException e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Was expecting an exception!");
    }

    @Test
    public void testFetchConfVariable() throws ValidationException {

        // null ID        
        boolean exception = false;
        try {
            _functions.fetchConfVariable(null);
        }
        catch (ValidationException e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Was expecting an exception!");

        // non-null ID -> null (default implementation)
        Assert.assertNull(_functions.fetchConfVariable("id"));
    }

    @Test
    public void testLog() {
        _functions.log("info!");
        _functions.logWarning("warning!");
        _functions.logError("error!");
        TestingUtils.assertLogMessage("info!");
        TestingUtils.assertLogMessage("warning!");
        TestingUtils.assertLogMessage("error!");
    }

    @Test
    public void testAsInt() {
        Assert.assertEquals(1, _functions.asInt(1).intValue());
        Assert.assertEquals(-1, _functions.asInt(-1).intValue());
        Assert.assertEquals(2, _functions.asInt("2").intValue());
        Assert.assertNull(_functions.asInt(null));
        Assert.assertNull(_functions.asInt(""));
        Assert.assertNull(_functions.asInt("1A"));
    }

    @Test
    public void testBetween() {
        Assert.assertFalse(_functions.between(null, 1, 3));
        Assert.assertFalse(_functions.between(2, null, 3));
        Assert.assertFalse(_functions.between(2, 1, null));
        Assert.assertTrue(_functions.between(2, 1, 3));
        Assert.assertFalse(_functions.between(4, 1, 3));
        Assert.assertTrue(_functions.between(1.5, 1.0, 2.0));
        Assert.assertTrue(_functions.between("B", "A", "C"));
        Assert.assertFalse(_functions.between("D", "A", "C"));
        Assert.assertTrue(_functions.between("8002", "8000", "8004"));
        Assert.assertFalse(_functions.between("9999", "8000", "8004"));
        Assert.assertTrue(_functions.between("8002", 8000, 8004));
        Assert.assertTrue(_functions.between((short)8002, 8000, 8004));
        Assert.assertFalse(_functions.between("198", 1970, 1990));
        Assert.assertFalse(_functions.between("198", "1970", "1990"));
    }

    @Test
    public void testGetCurrentDay() {
        Assert.assertTrue(_functions.getCurrentDay() > 0);
    }

    @Test
    public void testGetCurrentMonth() {
        Assert.assertTrue(_functions.getCurrentMonth() > 0);
    }

    @Test
    public void testGetCurrentYear() {
        Assert.assertTrue(_functions.getCurrentYear() > 0);
    }

    @Test
    public void testMatches() {
        _functions.disableRegexCaching();
        Assert.assertFalse(_functions.matches("A", null));
        Assert.assertFalse(_functions.matches(null, "[A-Z]"));
        Assert.assertFalse(_functions.matches("", "[A-Z]"));
        Assert.assertFalse(_functions.matches(" ", "[A-Z]"));
        Assert.assertFalse(_functions.matches("1", "[A-Z]"));
        Assert.assertFalse(_functions.matches(1, "[A-Z]"));
        Assert.assertTrue(_functions.matches("A", "[A-Z]"));

        try {
            _functions.enableRegexCaching();
            Assert.assertFalse(_functions.matches("A", null));
            Assert.assertFalse(_functions.matches(null, "[A-Z]"));
            Assert.assertFalse(_functions.matches("", "[A-Z]"));
            Assert.assertFalse(_functions.matches(" ", "[A-Z]"));
            Assert.assertFalse(_functions.matches("1", "[A-Z]"));
            Assert.assertFalse(_functions.matches(1, "[A-Z]"));
            Assert.assertTrue(_functions.matches("A", "[A-Z]"));
            Assert.assertEquals(1, _functions.getNumRegexCacheMiss()); // first time is always a miss
            Assert.assertEquals(4, _functions.getNumRegexCacheHit()); // any other times is hit (null value/regex doesn't count)
            // same value, same regex (hit)
            Assert.assertTrue(_functions.matches("A", "[A-Z]"));
            Assert.assertEquals(1, _functions.getNumRegexCacheMiss());
            Assert.assertEquals(5, _functions.getNumRegexCacheHit());
            // different value, same regex (hit)
            Assert.assertTrue(_functions.matches("B", "[A-Z]"));
            Assert.assertEquals(1, _functions.getNumRegexCacheMiss());
            Assert.assertEquals(6, _functions.getNumRegexCacheHit());
            // different value, different regex (miss)
            Assert.assertTrue(_functions.matches("0", "[0-9]"));
            Assert.assertEquals(2, _functions.getNumRegexCacheMiss());
            Assert.assertEquals(6, _functions.getNumRegexCacheHit());
            _functions.disableRegexCaching();
            Assert.assertEquals(0, _functions.getNumRegexCacheMiss());
            Assert.assertEquals(0, _functions.getNumRegexCacheHit());
        }
        finally {
            _functions.disableRegexCaching();
        }
    }

    @Test
    public void testDifferenceInDays() {
        Assert.assertEquals(-1, _functions.differenceInDays(1, 1, null, 1, 1, 2000));
        Assert.assertEquals(-1, _functions.differenceInDays(1, null, 2000, 1, 1, 2000));
        Assert.assertEquals(-1, _functions.differenceInDays(1, 1, 2000, 1, 1, null));
        Assert.assertEquals(-1, _functions.differenceInDays(1, 1, 2000, 1, null, 2000));
        Assert.assertEquals(-1, _functions.differenceInDays(1, 1, 2000, 1, 1, 9999));
        Assert.assertEquals(-1, _functions.differenceInDays(1, 1, 2000, 1, 99, 2000));
        Assert.assertEquals(-1, _functions.differenceInDays(1, 1, "A", 1, 1, 2000));
        Assert.assertEquals(-1, _functions.differenceInDays(1, "A", 2000, 1, 1, 2000));
        Assert.assertEquals(-1, _functions.differenceInDays(1, 1, 2000, 1, 1, "A"));
        Assert.assertEquals(-1, _functions.differenceInDays(1, 1, 2000, 1, "A", 2000));

        Assert.assertEquals(0, _functions.differenceInDays(1, 1, 2000, 1, 1, 2000));
        Assert.assertEquals(0, _functions.differenceInDays(null, 1, 2000, null, 1, 2000));
        Assert.assertEquals(1, _functions.differenceInDays("A", 1, 2000, 2, 1, 2000));
        Assert.assertEquals(31, _functions.differenceInDays(1, 1, 2000, 1, 2, 2000));
        Assert.assertEquals(366, _functions.differenceInDays(1, 1, 2000, 1, 1, 2001));
        Assert.assertEquals(2, _functions.differenceInDays(31, 12, 2000, 2, 1, 2001));
        Assert.assertEquals(87, _functions.differenceInDays(null, 1, 2000, 88, 1, 2000));
        Assert.assertEquals(0, _functions.differenceInDays(99, 1, 2000, 1, 1, 2000));

        Assert.assertEquals(-1, _functions.differenceInDays(2, 1, 2000, 1, 1, 2000));
        Assert.assertEquals(-31, _functions.differenceInDays(1, 2, 2000, 1, 1, 2000));
        Assert.assertEquals(-366, _functions.differenceInDays(1, 1, 2001, 1, 1, 2000));

    }
}
