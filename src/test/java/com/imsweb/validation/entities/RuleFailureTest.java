/*
 * Copyright (C) 2018 Information Management Services, Inc.
 */
package com.imsweb.validation.entities;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

public class RuleFailureTest {

    @Test
    public void testGetCombinedMessage() {
        Rule rule = new Rule();
        rule.setName("NAME");
        rule.setJavaPath("untrimmedlines.untrimmedline");

        RuleFailure failure = new RuleFailure();
        failure.setRule(rule);
        failure.setMessage("MSG1");
        Assert.assertEquals("NAME: MSG1", failure.getCombinedMessage());

        failure.setExtraErrorMessages(Collections.singletonList("MSG2"));
        Assert.assertEquals("NAME: MSG1 (MSG2)", failure.getCombinedMessage());

        failure.setExtraErrorMessages(Arrays.asList("MSG2", "MSG3"));
        Assert.assertEquals("NAME: MSG1 (MSG2; MSG3)", failure.getCombinedMessage());

        failure.setExtraErrorMessages(Arrays.asList("MSG2", "MSG1", "MSG2"));
        Assert.assertEquals("NAME: MSG1 (MSG2)", failure.getCombinedMessage());
    }

}