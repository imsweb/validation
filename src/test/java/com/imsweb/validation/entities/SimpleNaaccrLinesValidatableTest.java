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
import com.imsweb.validation.TestingUtils.TestingValidationContextFunctions;
import com.imsweb.validation.ValidationContextFunctions;
import com.imsweb.validation.ValidationEngine;
import com.imsweb.validation.functions.StagingContextFunctions;

public class SimpleNaaccrLinesValidatableTest {

    @Before
    public void setUp() {
        TestingUtils.init();
    }

    @Test
    public void testSimpleMapValidatable() throws Exception {
        TestingUtils.loadValidator("fake-validator-naaccr-lines");

        StringWriter writer = new StringWriter();
        Map<String, Object> context = Collections.singletonMap("out", writer);

        Map<String, String> e1 = new HashMap<>();
        e1.put("primarySite", "C001"); //  // should fail first rule

        Map<String, String> e2 = new HashMap<>();
        e2.put("primarySite", "C000"); // no failure
        e2.put("patientIdNumber", "00000000");
        e2.put("tumorRecordNumber", "01");

        // empty list
        SimpleNaaccrLinesValidatable v = new SimpleNaaccrLinesValidatable(Collections.singletonList(e1), context);
        Assert.assertEquals("?", v.getDisplayId());
        Assert.assertNull(v.getCurrentTumorId());
        Assert.assertEquals("lines", v.getRootLevel());
        TestingUtils.assertEditFailure(ValidationEngine.getInstance().validate(v), "fvnl-rule1");

        // test that the reported paths are correct
        SortedSet<String> expected = new TreeSet<>();
        expected.add("lines.line[0].primarySite");

        SortedSet<String> actual = new TreeSet<>();
        for (RuleFailure f : ValidationEngine.getInstance().validate(v))
            actual.addAll(f.getProperties());
        Assert.assertEquals(expected, actual);

        v = new SimpleNaaccrLinesValidatable(e2);
        TestingUtils.assertEditFailure(ValidationEngine.getInstance().validate(v), "fvnl-rule1"); // exception bc 'out' not defined
        TestingUtils.assertEditFailure(ValidationEngine.getInstance().validate(v), "fvnl-rule2");
        RuleFailure rf = ValidationEngine.getInstance().validate(v).stream().filter(f -> "fvnl-rule1".equals(f.getRule().getId())).findFirst().orElse(null);
        Assert.assertNotNull(rf);
        Assert.assertEquals(1, rf.getTumorIdentifier().intValue());

        List<Map<String, String>> list = new ArrayList<>();
        list.add(e2);
        list.add(e1);
        v = new SimpleNaaccrLinesValidatable(list, context);
        TestingUtils.assertNoEditFailure(ValidationEngine.getInstance().validate(v), "fvnl-rule2");
        TestingUtils.assertEditFailure(ValidationEngine.getInstance().validate(v), "fvnl-rule1");
        RuleFailure e1Failure = null;
        for (RuleFailure failure : ValidationEngine.getInstance().validate(v))
            if (failure.getRule().getId().equals("fvnl-rule1"))
                e1Failure = failure;
        if (e1Failure != null)
            Assert.assertEquals("lines.line[1].primarySite", e1Failure.getProperties().iterator().next());

        v = new SimpleNaaccrLinesValidatable(e2);
        TestingUtils.assertEditFailure(ValidationEngine.getInstance().validate(v), "fvnl-rule1"); // exception bc 'out' not defined
        TestingUtils.assertEditFailure(ValidationEngine.getInstance().validate(v), "fvnl-rule2");

        // test the current level
        Assert.assertEquals("lines", v.getCurrentLevel());
        Assert.assertEquals("lines.line[0]", v.followCollection("line").getFirst().getCurrentLevel());

        TestingUtils.unloadValidator("fake-validator-naaccr-lines");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetCsSchemaId() throws IllegalAccessException {
        // This test needs StagingContextFunctions instead of TestingValidatorContextFunctions
        ValidationContextFunctions.initialize(new StagingContextFunctions(TestingUtils.getCsStaging(), TestingUtils.getTnmStaging(), TestingUtils.getEodStaging()));

        try {
            Map<String, String> rec = new HashMap<>();
            SimpleNaaccrLinesValidatable v = new SimpleNaaccrLinesValidatable(rec);
            List<Validatable> validatables = v.followCollection("line");
            Map<String, String> line = (Map<String, String>)validatables.getFirst().getScope().get("line");
            Assert.assertNull(line.get("csSiteSpecificFactor25"));
            Assert.assertNull(line.get("_tnmSchemaId"));
            Assert.assertNull(line.get("_csSchemaId"));
            Assert.assertNull(line.get("_eodSchemaId"));

            rec.put("dateOfDiagnosisYear", "2018");
            rec.put("primarySite", "C111");
            rec.put("histologicTypeIcdO3", "8000");
            v = new SimpleNaaccrLinesValidatable(rec);
            validatables = v.followCollection("line");
            line = (Map<String, String>)validatables.getFirst().getScope().get("line");
            Assert.assertNull(line.get("csSiteSpecificFactor25"));
            Assert.assertNull(line.get("_tnmSchemaId"));
            Assert.assertNull(line.get("_csSchemaId"));
            Assert.assertNull(line.get("_eodSchemaId"));

            rec.put("csSiteSpecificFactor25", "010");
            rec.put("schemaDiscriminator1", "1");
            v = new SimpleNaaccrLinesValidatable(rec);
            validatables = v.followCollection("line");
            line = (Map<String, String>)validatables.getFirst().getScope().get("line");
            Assert.assertEquals("010", line.get("csSiteSpecificFactor25"));
            Assert.assertEquals("nasopharynx", line.get("_tnmSchemaId"));
            Assert.assertEquals("nasopharynx", line.get("_csSchemaId"));
            Assert.assertEquals("nasopharynx", line.get("_eodSchemaId"));

            rec.put("primarySite", "C481");
            rec.put("csSiteSpecificFactor25", null);
            rec.put("schemaDiscriminator1", null);
            rec.put("sexAssignedAtBirth", "1");
            v = new SimpleNaaccrLinesValidatable(rec);
            validatables = v.followCollection("line");
            line = (Map<String, String>)validatables.getFirst().getScope().get("line");
            Assert.assertNull(line.get("csSiteSpecificFactor25")); // the code used to assign the SSF25 based on the TNM schema and sex value; this was removed (#36)
            Assert.assertEquals("peritoneum", line.get("_tnmSchemaId"));
            Assert.assertNull(line.get("_csSchemaId")); // the code used to assign the SSF25 based on the TNM schema and sex value; this was removed (#36)
            Assert.assertEquals("retroperitoneum", line.get("_eodSchemaId"));

            v = new SimpleNaaccrLinesValidatable(Collections.singletonList(rec), null, true);
            validatables = v.followCollection("untrimmedline");
            line = (Map<String, String>)validatables.getFirst().getScope().get("untrimmedline");
            Assert.assertNull(line.get("csSiteSpecificFactor25")); // the code used to assign the SSF25 based on the TNM schema and sex value; this was removed (#36)
            Assert.assertEquals("peritoneum", line.get("_tnmSchemaId"));
            Assert.assertNull(line.get("_csSchemaId")); // the code used to assign the SSF25 based on the TNM schema and sex value; this was removed (#36)
            Assert.assertEquals("retroperitoneum", line.get("_eodSchemaId"));
        }
        finally {
            ValidationContextFunctions.initialize(new TestingValidationContextFunctions());
        }
    }
}
