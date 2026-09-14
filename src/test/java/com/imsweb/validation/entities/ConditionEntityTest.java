/*
 * Copyright (C) 2026 Information Management Services, Inc.
 */
package com.imsweb.validation.entities;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.imsweb.validation.ConstructionException;
import com.imsweb.validation.TestingUtils;

public class ConditionEntityTest {

    @Before
    public void setUp() {
        TestingUtils.init();
    }

    @Test
    public void testSetExpressionWithParsing() throws ConstructionException {
        Condition condition = new Condition();
        condition.setId("condition-id");
        condition.setExpression("return line.prop != Context.SOME_CONTEXT");

        Assert.assertEquals(Collections.singleton("line.prop"), condition.getUsedProperties());
        Assert.assertEquals(Collections.singleton("SOME_CONTEXT"), condition.getUsedContextKeys());
        Assert.assertTrue(condition.getUsedLookupIds().isEmpty());
    }

    @Test
    public void testSetExpressionWithoutParsing() {
        // the collections are deliberately not the ones that parsing the expression would return; that's how we know they were used as-is
        Set<String> properties = Collections.singleton("line.otherProp");
        Set<String> contextKeys = Collections.singleton("OTHER_CONTEXT");
        Set<String> lookupIds = Collections.singleton("other-lookup");

        Condition condition = new Condition();
        condition.setId("condition-id");
        condition.setExpression("return line.prop != Context.SOME_CONTEXT", properties, contextKeys, lookupIds);

        Assert.assertEquals("return line.prop != Context.SOME_CONTEXT", condition.getExpression());
        Assert.assertEquals(properties, condition.getUsedProperties());
        Assert.assertEquals(contextKeys, condition.getUsedContextKeys());
        Assert.assertEquals(lookupIds, condition.getUsedLookupIds());
    }

    @Test
    public void testSetExpressionWithoutParsingDoesNotCompile() {
        // an invalid expression would make the parsing setter fail; this one doesn't parse, so it shouldn't complain
        Condition condition = new Condition();
        condition.setId("condition-id");
        condition.setExpression("!@$%^", Collections.emptySet(), Collections.emptySet(), Collections.emptySet());

        Assert.assertEquals("!@$%^", condition.getExpression());

        // ...but the regular setter should still fail on an invalid expression
        boolean exception = false;
        try {
            condition.setExpression("&*()");
        }
        catch (ConstructionException e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Was expecting an exception but didn't get it");
    }

    @Test
    public void testSetExpressionWithoutParsingOverridesPreviousValues() throws ConstructionException {
        Condition condition = new Condition();
        condition.setId("condition-id");
        condition.setExpression("return line.prop != Context.SOME_CONTEXT");
        Assert.assertEquals(Collections.singleton("line.prop"), condition.getUsedProperties());

        condition.setExpression("return line.otherProp != null", Collections.singleton("line.otherProp"), Collections.emptySet(), Collections.emptySet());
        Assert.assertEquals(Collections.singleton("line.otherProp"), condition.getUsedProperties());
        Assert.assertTrue(condition.getUsedContextKeys().isEmpty());
        Assert.assertTrue(condition.getUsedLookupIds().isEmpty());

        // and going back to the parsing setter shouldn't leave any of the provided values behind
        condition.setExpression("return line.prop != null");
        Assert.assertEquals(Collections.singleton("line.prop"), condition.getUsedProperties());
    }

    @Test
    public void testSetExpressionWithoutParsingCopiesTheCollections() throws ConstructionException {
        // the provided collections can be immutable, and mutating them later shouldn't affect the condition
        Set<String> properties = new HashSet<>(Collections.singleton("line.otherProp"));

        Condition condition = new Condition();
        condition.setId("condition-id");
        condition.setExpression("return line.otherProp != null", properties, Collections.emptySet(), Collections.singleton("some-lookup"));

        properties.add("line.yetAnotherProp");
        Assert.assertEquals(Collections.singleton("line.otherProp"), condition.getUsedProperties());

        // the immutable collections shouldn't make the parsing setter fail when it clears them
        condition.setExpression("return line.prop != null");
        Assert.assertEquals(Collections.singleton("line.prop"), condition.getUsedProperties());
        Assert.assertTrue(condition.getUsedLookupIds().isEmpty());
    }

    @Test
    public void testSetExpressionWithoutParsingHandlesNullCollections() {
        Condition condition = new Condition();
        condition.setId("condition-id");
        condition.setExpression("return true", null, null, null);

        Assert.assertTrue(condition.getUsedProperties().isEmpty());
        Assert.assertTrue(condition.getUsedContextKeys().isEmpty());
        Assert.assertTrue(condition.getUsedLookupIds().isEmpty());
    }
}
