/*
 * Copyright (C) 2019 Information Management Services, Inc.
 */
package com.imsweb.validation;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.junit.Assert;
import org.junit.Test;

public class ValidatingContextTest {

    @Test
    public void testConditionFailed() {
        ValidatingContext context = new ValidatingContext();
        Assert.assertFalse(context.conditionFailed(null, null));

        context.getFailedConditionIds().computeIfAbsent("ROOT", k -> new HashSet<>()).add("COND1");
        Assert.assertTrue(context.conditionFailed(Collections.singletonList("ROOT"), "COND1"));
        Assert.assertTrue(context.conditionFailed(Arrays.asList("ROOT", "ROOT.COLLECTION[0]"), "COND1"));
        Assert.assertTrue(context.conditionFailed(Arrays.asList("ROOT", "ROOT.COLLECTION[1]"), "COND1"));

        context.getFailedConditionIds().computeIfAbsent("ROOT.COLLECTION[1]", k -> new HashSet<>()).add("COND2");
        Assert.assertFalse(context.conditionFailed(Collections.singletonList("ROOT"), "COND2"));
        Assert.assertFalse(context.conditionFailed(Arrays.asList("ROOT", "ROOT.COLLECTION[0]"), "COND2"));
        Assert.assertTrue(context.conditionFailed(Arrays.asList("ROOT", "ROOT.COLLECTION[1]"), "COND2"));
    }

    @Test
    public void testAtLeastOneDependencyFailed() {
        ValidatingContext context = new ValidatingContext();
        Assert.assertFalse(context.atLeastOneDependencyFailed(null, null));

        context.getFailedRuleIds().computeIfAbsent("ROOT", k -> new HashSet<>()).add("RULE1");
        Assert.assertTrue(context.atLeastOneDependencyFailed(Collections.singletonList("ROOT"), Collections.singleton("RULE1")));
        Assert.assertTrue(context.atLeastOneDependencyFailed(Arrays.asList("ROOT", "ROOT.COLLECTION[0]"), Collections.singleton("RULE1")));
        Assert.assertTrue(context.atLeastOneDependencyFailed(Arrays.asList("ROOT", "ROOT.COLLECTION[1]"), Collections.singleton("RULE1")));
        Assert.assertTrue(context.atLeastOneDependencyFailed(Arrays.asList("ROOT", "ROOT.COLLECTION[1]"), new HashSet<>(Arrays.asList("RULE1", "RULE2"))));
        Assert.assertFalse(context.atLeastOneDependencyFailed(Arrays.asList("ROOT", "ROOT.COLLECTION[1]"), Collections.emptySet()));

        context.getFailedRuleIds().computeIfAbsent("ROOT.COLLECTION[1]", k -> new HashSet<>()).add("RULE2");
        Assert.assertFalse(context.atLeastOneDependencyFailed(Collections.singletonList("ROOT"), Collections.singleton("RULE2")));
        Assert.assertFalse(context.atLeastOneDependencyFailed(Arrays.asList("ROOT", "ROOT.COLLECTION[0]"), Collections.singleton("RULE2")));
        Assert.assertTrue(context.atLeastOneDependencyFailed(Arrays.asList("ROOT", "ROOT.COLLECTION[1]"), Collections.singleton("RULE2")));
        Assert.assertTrue(context.atLeastOneDependencyFailed(Arrays.asList("ROOT", "ROOT.COLLECTION[1]"), new HashSet<>(Arrays.asList("RULE1", "RULE2"))));
        Assert.assertTrue(context.atLeastOneDependencyFailed(Arrays.asList("ROOT", "ROOT.COLLECTION[1]"), new HashSet<>(Arrays.asList("RULE1", "RULE3"))));
        Assert.assertFalse(context.atLeastOneDependencyFailed(Arrays.asList("ROOT", "ROOT.COLLECTION[1]"), new HashSet<>(Arrays.asList("RULE3", "RULE4"))));
        Assert.assertFalse(context.atLeastOneDependencyFailed(Arrays.asList("ROOT", "ROOT.COLLECTION[1]"), Collections.emptySet()));
    }
}
