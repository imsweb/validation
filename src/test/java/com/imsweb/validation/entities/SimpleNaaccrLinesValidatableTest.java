/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation.entities;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.imsweb.validation.TestingUtils;
import com.imsweb.validation.ValidationEngine;

public class SimpleNaaccrLinesValidatableTest {

    @Before
    public void setUp() throws Exception {
        TestingUtils.init();
    }

    @Test
    public void testSimpleMapValidatable() throws Exception {
        TestingUtils.loadValidator("fake-validator-naaccr-lines");

        StringWriter writer = new StringWriter();
        Map<String, Object> context = Collections.singletonMap("out", (Object)writer);

        Map<String, String> e1 = new HashMap<>();
        e1.put("primarySite", "C001"); //  // should fail first rule

        Map<String, String> e2 = new HashMap<>();
        e2.put("primarySite", "C000"); // no failure
        e2.put("patientIdNumber", "00000000");
        e2.put("tumorRecordNumber", "01");

        // null map
        boolean exception = false;
        try {
            new SimpleNaaccrLinesValidatable(null, null);
        }
        catch (RuntimeException e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Where is the exception?");

        // empty list
        SimpleNaaccrLinesValidatable v = new SimpleNaaccrLinesValidatable(Collections.singletonList(e1), context);
        Assert.assertEquals("?", v.getDisplayId());
        Assert.assertNull(v.getCurrentTumorId());
        Assert.assertEquals("lines", v.getRootLevel());
        TestingUtils.assertEditFailure(ValidationEngine.validate(v), "fvnl-rule1");

        // test that the reported paths are correct
        SortedSet<String> expected = new TreeSet<>();
        expected.add("lines.line[0].primarySite");

        SortedSet<String> actual = new TreeSet<>();
        for (RuleFailure f : ValidationEngine.validate(v))
            for (String prop : f.getProperties())
                actual.add(prop);
        Assert.assertEquals(expected, actual);

        v = new SimpleNaaccrLinesValidatable(e2);
        TestingUtils.assertEditFailure(ValidationEngine.validate(v), "fvnl-rule1"); // exception bc 'out' not defined
        TestingUtils.assertEditFailure(ValidationEngine.validate(v), "fvnl-rule2");

        List<Map<String, String>> list = new ArrayList<>();
        list.add(e2);
        list.add(e1);
        v = new SimpleNaaccrLinesValidatable(list, context);
        TestingUtils.assertNoEditFailure(ValidationEngine.validate(v), "fvnl-rule2");
        TestingUtils.assertEditFailure(ValidationEngine.validate(v), "fvnl-rule1");
        RuleFailure e1Failure = null;
        for (RuleFailure failure : ValidationEngine.validate(v))
            if (failure.getRule().getId().equals("fvnl-rule1"))
                e1Failure = failure;
        if (e1Failure != null)
            Assert.assertEquals("lines.line[1].primarySite", e1Failure.getProperties().iterator().next());

        v = new SimpleNaaccrLinesValidatable(e2);
        TestingUtils.assertEditFailure(ValidationEngine.validate(v), "fvnl-rule1"); // exception bc 'out' not defined
        TestingUtils.assertEditFailure(ValidationEngine.validate(v), "fvnl-rule2");

        // test the current level
        Assert.assertEquals("lines", v.getCurrentLevel());
        Assert.assertEquals("lines.line[0]", v.followCollection("line").get(0).getCurrentLevel());

        TestingUtils.unloadValidator("fake-validator-naaccr-lines");
    }
}
