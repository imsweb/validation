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

public class RuleEntityTest {

    @Before
    public void setUp() {
        TestingUtils.init();
    }

    @Test
    public void testSetExpressionWithParsing() throws ConstructionException {
        Rule rule = new Rule();
        rule.setId("rule-id");
        rule.setExpression("return line.prop != Context.SOME_CONTEXT");

        Assert.assertEquals(Collections.singleton("line.prop"), rule.getUsedProperties());
        Assert.assertEquals(Collections.singleton("SOME_CONTEXT"), rule.getUsedContextKeys());
        Assert.assertTrue(rule.getUsedLookupIds().isEmpty());
    }

    @Test
    public void testSetExpressionWithoutParsing() {
        // the collections are deliberately not the ones that parsing the expression would return; that's how we know they were used as-is
        Set<String> properties = Collections.singleton("line.otherProp");
        Set<String> contextKeys = Collections.singleton("OTHER_CONTEXT");
        Set<String> lookupIds = Collections.singleton("other-lookup");

        Rule rule = new Rule();
        rule.setId("rule-id");
        rule.setExpression("return line.prop != Context.SOME_CONTEXT", properties, contextKeys, lookupIds);

        Assert.assertEquals("return line.prop != Context.SOME_CONTEXT", rule.getExpression());
        Assert.assertEquals(properties, rule.getUsedProperties());
        Assert.assertEquals(contextKeys, rule.getUsedContextKeys());
        Assert.assertEquals(lookupIds, rule.getUsedLookupIds());
    }

    @Test
    public void testSetExpressionWithoutParsingDoesNotCompile() {
        // an invalid expression would make the parsing setter fail; this one doesn't parse, so it shouldn't complain
        Rule rule = new Rule();
        rule.setId("rule-id");
        rule.setExpression("!@$%^", Collections.emptySet(), Collections.emptySet(), Collections.emptySet());

        Assert.assertEquals("!@$%^", rule.getExpression());

        // ...but the regular setter should still fail on an invalid expression
        boolean exception = false;
        try {
            rule.setExpression("&*()");
        }
        catch (ConstructionException e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Was expecting an exception but didn't get it");
    }

    @Test
    public void testSetExpressionWithoutParsingOverridesPreviousValues() throws ConstructionException {
        Rule rule = new Rule();
        rule.setId("rule-id");
        rule.setExpression("return line.prop != Context.SOME_CONTEXT");
        Assert.assertEquals(Collections.singleton("line.prop"), rule.getUsedProperties());

        rule.setExpression("return line.otherProp != null", Collections.singleton("line.otherProp"), Collections.emptySet(), Collections.emptySet());
        Assert.assertEquals(Collections.singleton("line.otherProp"), rule.getUsedProperties());
        Assert.assertTrue(rule.getUsedContextKeys().isEmpty());
        Assert.assertTrue(rule.getUsedLookupIds().isEmpty());

        // and going back to the parsing setter shouldn't leave any of the provided values behind
        rule.setExpression("return line.prop != null");
        Assert.assertEquals(Collections.singleton("line.prop"), rule.getUsedProperties());
    }

    @Test
    public void testSetExpressionWithoutParsingCopiesTheCollections() throws ConstructionException {
        // the provided collections can be immutable, and mutating them later shouldn't affect the rule
        Set<String> properties = new HashSet<>(Collections.singleton("line.otherProp"));

        Rule rule = new Rule();
        rule.setId("rule-id");
        rule.setExpression("return line.otherProp != null", properties, Collections.emptySet(), Collections.singleton("some-lookup"));

        properties.add("line.yetAnotherProp");
        Assert.assertEquals(Collections.singleton("line.otherProp"), rule.getUsedProperties());

        // the immutable collections shouldn't make the parsing setter fail when it clears them
        rule.setExpression("return line.prop != null");
        Assert.assertEquals(Collections.singleton("line.prop"), rule.getUsedProperties());
        Assert.assertTrue(rule.getUsedLookupIds().isEmpty());
    }

    @Test
    public void testSetExpressionWithoutParsingHandlesNullCollections() {
        Rule rule = new Rule();
        rule.setId("rule-id");
        // the cast is needed because Rule also has a setExpression overload taking the pre-parsed runtime objects
        rule.setExpression("return true", (Set<String>)null, null, null);

        Assert.assertTrue(rule.getUsedProperties().isEmpty());
        Assert.assertTrue(rule.getUsedContextKeys().isEmpty());
        Assert.assertTrue(rule.getUsedLookupIds().isEmpty());
    }
}
