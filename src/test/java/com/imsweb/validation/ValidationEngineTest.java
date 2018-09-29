/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation;

import com.imsweb.validation.entities.Condition;
import com.imsweb.validation.entities.EditableCondition;
import com.imsweb.validation.entities.EditableRule;
import com.imsweb.validation.entities.EditableValidator;
import com.imsweb.validation.entities.Rule;
import com.imsweb.validation.entities.RuleFailure;
import com.imsweb.validation.entities.RuleHistory;
import com.imsweb.validation.entities.SimpleMapValidatable;
import com.imsweb.validation.entities.Validatable;
import com.imsweb.validation.entities.Validator;
import com.imsweb.validation.internal.ValidatingContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
public class ValidationEngineTest {

    @Before
    public void setUp() throws Exception {
        TestingUtils.init();
    }

    @Test
    public void testInitialize() throws Exception {

        // make sure we start clean
        ValidationEngine.uninitialize();
        Assert.assertFalse(ValidationEngine.isInitialized());

        // initialize with no validator
        ValidationEngine.initialize();
        Assert.assertTrue(ValidationEngine.isInitialized());
        Assert.assertTrue(ValidationEngine.getValidators().isEmpty());
        Assert.assertNotNull(ValidationEngine.getEngineVersion());
        ValidationEngine.uninitialize();
        Assert.assertFalse(ValidationEngine.isInitialized());

        // initialize with a null validator
        ValidationEngine.initialize((Validator)null);
        Assert.assertTrue(ValidationEngine.isInitialized());
        Assert.assertTrue(ValidationEngine.getValidators().isEmpty());
        ValidationEngine.uninitialize();
        Assert.assertFalse(ValidationEngine.isInitialized());

        // initialize with an empty list
        ValidationEngine.initialize(Collections.emptyList());
        Assert.assertTrue(ValidationEngine.isInitialized());
        Assert.assertTrue(ValidationEngine.getValidators().isEmpty());
        ValidationEngine.uninitialize();
        Assert.assertFalse(ValidationEngine.isInitialized());

        // initialize with a regular validator
        Validator v = ValidationXmlUtils.loadValidatorFromXml(Thread.currentThread().getContextClassLoader().getResource("fake-validator.xml"));
        ValidationEngine.initialize(v);
        Assert.assertTrue(ValidationEngine.isInitialized());
        Assert.assertFalse(ValidationEngine.getValidators().isEmpty());
        Assert.assertTrue(ValidationEngine.getSupportedJavaPathRoots().size() > 0); // the roots come from the services...
        Assert.assertTrue(ValidationEngine.getSupportedJavaPathRoots().contains("level1"));
        Rule r1 = ValidationEngine.getValidators().get("fake-validator").getRule("fv-rule1");
        Assert.assertTrue(r1.getDependencies().isEmpty());
        Assert.assertEquals(Collections.singleton("fv-rule2"), r1.getInvertedDependencies());
        Rule r2 = ValidationEngine.getValidators().get("fake-validator").getRule("fv-rule2");
        Assert.assertEquals(Collections.singleton("fv-rule1"), r2.getDependencies());
        Assert.assertEquals(Collections.singleton("fv-rule3"), r2.getInvertedDependencies());
        Rule r3 = ValidationEngine.getValidators().get("fake-validator").getRule("fv-rule3");
        Assert.assertEquals(Collections.singleton("fv-rule2"), r3.getDependencies());
        Assert.assertTrue(r3.getInvertedDependencies().isEmpty());
        Assert.assertFalse(ValidationEngine.dumpInternalState().isEmpty());
        ValidationEngine.uninitialize();
        Assert.assertFalse(ValidationEngine.isInitialized());

        // initialize with a collection of validator, using multi-threaded compilation
        ValidationEngine.enableMultiThreadedCompilation(2);
        ValidationEngine.initialize(Collections.singletonList(v));
        Assert.assertTrue(ValidationEngine.isInitialized());
        Assert.assertFalse(ValidationEngine.getValidators().isEmpty());
        ValidationEngine.uninitialize();
        Assert.assertFalse(ValidationEngine.isInitialized());
        ValidationEngine.enableMultiThreadedCompilation(1);

        // initialize with a bad validator
        boolean exception = false;
        v.getRule("fv-rule1").setDependencies(Collections.singleton("fv-rule3"));
        try {
            ValidationEngine.initialize(Collections.singletonList(v));
        }
        catch (Exception e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Was excpecting an exception but didn't get it");
        ValidationEngine.uninitialize();

        // initialize with a bad validator using multi-threaded compilation
        ValidationEngine.enableMultiThreadedCompilation(2);
        exception = false;
        try {
            ValidationEngine.initialize(Collections.singletonList(v));
        }
        catch (Exception e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Was expecting an exception but didn't get it");
        ValidationEngine.uninitialize();
        ValidationEngine.enableMultiThreadedCompilation(1);
    }

    @Test
    public void testGetters() {
        TestingUtils.loadValidator("fake-validator");

        Assert.assertFalse(ValidationEngine.getValidators().isEmpty());
        Assert.assertTrue(ValidationEngine.getSupportedJavaPathRoots().size() > 0); // roots come from services...

        Assert.assertNotNull(ValidationEngine.getValidator("fake-validator"));
        Assert.assertNotNull(ValidationEngine.getValidators().get("fake-validator"));
        Assert.assertNull(ValidationEngine.getValidator(null));
        Assert.assertNull(ValidationEngine.getValidator(""));
        Assert.assertNull(ValidationEngine.getValidator("?"));

        Assert.assertNotNull(ValidationEngine.getCategory("fv-category"));
        Assert.assertNotNull(ValidationEngine.getCategory("fv-category", "fake-validator"));
        Assert.assertNull(ValidationEngine.getCategory(null));
        Assert.assertNull(ValidationEngine.getCategory(null, null));
        Assert.assertNull(ValidationEngine.getCategory("?", null));
        Assert.assertNull(ValidationEngine.getCategory(null, "?"));
        Assert.assertNull(ValidationEngine.getCategory(""));
        Assert.assertNull(ValidationEngine.getCategory("?"));
        Assert.assertNull(ValidationEngine.getCategory("?", "fake-validator"));
        Assert.assertNull(ValidationEngine.getCategory("fv-category", "?"));

        Assert.assertNotNull(ValidationEngine.getCondition("fv-condition"));
        Assert.assertNotNull(ValidationEngine.getCondition("fv-condition", "fake-validator"));
        Assert.assertNull(ValidationEngine.getCondition(null));
        Assert.assertNull(ValidationEngine.getCondition(null, null));
        Assert.assertNull(ValidationEngine.getCondition("?", null));
        Assert.assertNull(ValidationEngine.getCondition(null, "?"));
        Assert.assertNull(ValidationEngine.getCondition(""));
        Assert.assertNull(ValidationEngine.getCondition("?"));
        Assert.assertNull(ValidationEngine.getCondition("?", "fake-validator"));
        Assert.assertNull(ValidationEngine.getCondition("fv-condition", "?"));

        Assert.assertNotNull(ValidationEngine.getRule("fv-rule1"));
        Assert.assertNotNull(ValidationEngine.getRule("fv-rule1", "fake-validator"));
        Assert.assertNull(ValidationEngine.getRule(null));
        Assert.assertNull(ValidationEngine.getRule(null, null));
        Assert.assertNull(ValidationEngine.getRule("?", null));
        Assert.assertNull(ValidationEngine.getRule(null, "?"));
        Assert.assertNull(ValidationEngine.getRule(""));
        Assert.assertNull(ValidationEngine.getRule("?"));
        Assert.assertNull(ValidationEngine.getRule("?", "fake-validator"));
        Assert.assertNull(ValidationEngine.getRule("fv-rule1", "?"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testValidate() throws Exception {
        TestingUtils.loadValidator("fake-validator");
        ValidationEngine.turnStatisticsOn();

        Map<String, Object> entity = new HashMap<>();
        List<Map<String, Object>> level2List = new ArrayList<>();
        Map<String, Object> level2 = new HashMap<>();
        level2List.add(level2);
        entity.put("level2", level2List);
        List<Map<String, Object>> level3List = new ArrayList<>();
        Map<String, Object> level3 = new HashMap<>();
        level3.put("prop", "1");
        level3List.add(level3);
        level2.put("level3", level3List);
        Validatable validatable = new SimpleMapValidatable("ID", "level1", entity);

        // default validation: only rule3 should fail
        TestingUtils.assertNoEditFailure(ValidationEngine.validate(validatable), "fv-rule1");
        TestingUtils.assertNoEditFailure(ValidationEngine.validate(validatable), "fv-rule2");
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable), "fv-rule3");

        // test forcing the rules
        TestingUtils.assertNoEditFailure(ValidationEngine.validate(validatable), "fv-rule1");
        TestingUtils.assertNoEditFailure(ValidationEngine.validate(validatable), "fv-rule2");
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable), "fv-rule3");

        // test forcing a rule that does not exist
        Rule tmpRule = new Rule();
        tmpRule.setId("JUST_TESTING");
        tmpRule.setJavaPath("level1.level2.level3");
        tmpRule.setExpression("return level3.prop != Context.FV_CONTEXT1");
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable, tmpRule), "JUST_TESTING");
        tmpRule.getConditions().add("fv-ruleset3");
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable, tmpRule), "JUST_TESTING");

        // test ignoring the rules
        TestingUtils.assertNoEditFailure(ValidationEngine.validate(validatable, Collections.emptyList()), "fv-rule1");
        TestingUtils.assertNoEditFailure(ValidationEngine.validate(validatable, Collections.emptyList()), "fv-rule2");
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable, Collections.emptyList()), "fv-rule3");
        // rule3 depends on rule2 which depends on rule1, so ignore either 1 or 2 should disable 3
        TestingUtils.assertNoEditFailure(ValidationEngine.validate(validatable, Collections.singletonList("fv-rule1")), "fv-rule3");
        TestingUtils.assertNoEditFailure(ValidationEngine.validate(validatable, Collections.singletonList("fv-rule2")), "fv-rule3");
        TestingUtils.assertNoEditFailure(ValidationEngine.validate(validatable, Collections.singletonList("fv-rule3")), "fv-rule3");
        // same test, but we are executing only a specific rule, instead of ignoring it
        TestingUtils.assertNoEditFailure(ValidationEngine.validate(validatable, null, Collections.singletonList("fv-rule1")), "fv-rule3");
        TestingUtils.assertNoEditFailure(ValidationEngine.validate(validatable, null, Collections.singletonList("fv-rule2")), "fv-rule3");
        TestingUtils.assertNoEditFailure(ValidationEngine.validate(validatable, null, Collections.singletonList("fv-rule3")), "fv-rule3");
        // executing all the rules should make fv-rule3 fail
        List<String> list = new ArrayList<>();
        list.add("fv-rule1");
        list.add("fv-rule2");
        list.add("fv-rule3");
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable, null, list), "fv-rule3");
        // ignoring a bad edit should have no effect
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable, Collections.singletonList("?")), "fv-rule3");
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable, (List<String>)null), "fv-rule3");

        // make sure forcing/ignoring rules dynamically doesn't change the state
        TestingUtils.assertEditFailures(ValidationEngine.validate(validatable));

        // let's make the first level edit fail, the third level has a dependency on it and should not fail anymore
        entity.put("prop", "1");
        validatable = new SimpleMapValidatable("ID", "level1", entity);
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable), "fv-rule1");
        TestingUtils.assertNoEditFailure(ValidationEngine.validate(validatable), "fv-rule3");
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable, "fv-rule1"), "fv-rule1");
        TestingUtils.assertNoEditFailure(ValidationEngine.validate(validatable, "fv-rule1"), "fv-rule3");
        TestingUtils.assertNoEditFailure(ValidationEngine.validate(validatable, Collections.singletonList("fv-rule1")), "fv-rule1");
        // rule3 depends on rule1, which is being ignore -> rule3 should not run either
        TestingUtils.assertNoEditFailure(ValidationEngine.validate(validatable, Collections.singletonList("fv-rule1")), "fv-rule3");
        entity.put("prop", "0");

        // let's make the second level fail, that rule overrides the returned properties
        ((List<Map<String, Object>>)entity.get("level2")).get(0).put("prop", "1");
        validatable = new SimpleMapValidatable("ID", "level1", entity);
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable), "fv-rule2");
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable, "fv-rule2"), "fv-rule2");
        Assert.assertEquals(1, ValidationEngine.validate(validatable).iterator().next().getProperties().size());
        Assert.assertTrue(ValidationEngine.validate(validatable).iterator().next().getProperties().contains("level1.level2[0].otherProp"));
        ((List<Map<String, Object>>)entity.get("level2")).get(0).put("prop", "0");

        TestingUtils.unloadValidator("fake-validator");

        // test an exception in the Groovy expression
        TestingUtils.loadValidator("fake-validator-exception-groovy");
        entity.clear();
        entity.put("prop", "1");
        RuleFailure rf = ValidationEngine.validate(new SimpleMapValidatable("ID", "level1", entity)).iterator().next();
        Assert.assertEquals(ValidationEngine.EXCEPTION_MSG, rf.getMessage());
        Assert.assertNotNull(rf.getGroovyException());
        Assert.assertEquals("TEST", rf.getGroovyException().getMessage());
        TestingUtils.unloadValidator("fake-validator-exception-groovy");

        // test a timeout
        TestingUtils.loadValidator("fake-validator-exception-timeout");
        entity.clear();
        entity.put("prop", "1");
        rf = ValidationEngine.validate(new SimpleMapValidatable("ID", "level1", entity)).iterator().next();
        Assert.assertEquals(ValidationEngine.TIMEOUT_MSG, rf.getMessage());
        Assert.assertNull(rf.getGroovyException());
        TestingUtils.unloadValidator("fake-validator-exception-timeout");

        // test a rule based on a condition defined on a higher level (rule on 'level1.level2' depends on a condition defined on 'level1')
        TestingUtils.loadValidator("fake-validator-parent-condition");
        entity.clear();
        level2 = new HashMap<>();
        level2.put("prop", "A");
        entity.put("level2", Collections.singletonList(level2));
        TestingUtils.assertEditFailure(ValidationEngine.validate(new SimpleMapValidatable("ID", "level1", entity)), "fvpc-rule"); // regular failure of the rule
        // add condition1
        EditableRule er = new EditableRule(ValidationEngine.getRule("fvpc-rule"));
        er.getConditions().add("fvpc-condition1");
        ValidationEngine.updateRule(er);
        entity.put("prop", "1"); // this should make condition1 fail...
        TestingUtils.assertNoEditFailure(ValidationEngine.validate(new SimpleMapValidatable("ID", "level1", entity)), "fvpc-rule");
        er.getConditions().clear();
        er.getConditions().add("fvpc-condition2");
        ValidationEngine.updateRule(er);
        entity.put("prop", "2"); // this should make condition2 fail...
        TestingUtils.assertNoEditFailure(ValidationEngine.validate(new SimpleMapValidatable("ID", "level1", entity)), "fvpc-rule");
        er.getConditions().clear();
        // this requires that both condition must pass (defaults is AND); since this is not the case, the condition is deemed failed, and the rule shouldn't be executed
        er.getConditions().add("fvpc-condition1");
        er.getConditions().add("fvpc-condition2");
        ValidationEngine.updateRule(er);
        TestingUtils.assertNoEditFailure(ValidationEngine.validate(new SimpleMapValidatable("ID", "level1", entity)), "fvpc-rule");
        // this requires that either condition must pass; this this is the case, the condition is deemed pass, and the rule should be executed
        er.setUseAndForConditions(false); // force to use OR
        ValidationEngine.updateRule(er);
        TestingUtils.assertEditFailure(ValidationEngine.validate(new SimpleMapValidatable("ID", "level1", entity)), "fvpc-rule");
        TestingUtils.unloadValidator("fake-validator-parent-condition");

        // try to validate, passing an entity that does not correspond to any processor
        Assert.assertTrue(ValidationEngine.validate(new SimpleMapValidatable("ID", "bad-level", entity)).isEmpty());

        // try to validate when the engine is not initialized -> no results
        ValidationEngine.uninitialize();
        Assert.assertTrue(ValidationEngine.validate(validatable).isEmpty());
        Assert.assertTrue(ValidationEngine.validate(new SimpleMapValidatable("ID", "bad-level", entity)).isEmpty());

        // after running some edits, there should be some stats available...
        Assert.assertFalse(ValidationEngine.getStats().isEmpty());
        ValidationEngine.resetStats();
        Assert.assertTrue(ValidationEngine.getStats().isEmpty());
        ValidationEngine.turnStatisticsOff();
    }

    @Test
    public void testRuntimeValidation() throws ConstructionException, ValidationException {
        Validator v = TestingUtils.loadValidator("fake-validator-runtime");

        // the logic in the XML doesn't reference any lookup, but the runtime Java class returns one...
        Assert.assertTrue(v.getRule("fvrt-rule1").getUsedLookupIds().contains("fake-lookup"));

        Map<String, Object> data = new HashMap<>();
        Validatable validatable = new SimpleMapValidatable("level-runtime", data);

        EngineInitStats stats = ValidationEngine.initialize(v);
        Assert.assertEquals(1, stats.getNumEditsLoaded());
        Assert.assertEquals(1, stats.getNumEditsPreCompiled());
        Assert.assertEquals(0, stats.getNumEditsCompiled());
        try {
            data.put("key", "value");
            TestingUtils.assertNoEditFailure(ValidationEngine.validate(validatable), "fvrt-rule1");
            data.put("key", "other");
            TestingUtils.assertEditFailure(ValidationEngine.validate(validatable), "fvrt-rule1");
        }
        finally {
            ValidationEngine.uninitialize();
        }
    }

    @Test
    public void testCrossRootValidation() throws Exception {

        // this test uses one validator with two validatable roots; DMS actually uses different validators, but the idea is the same...

        EditableValidator v = new EditableValidator();
        v.setId("fvcr");
        ValidationEngine.addValidator(v);

        EditableCondition c = new EditableCondition();
        c.setId("fvcr-condition");
        c.setJavaPath("level1");
        c.setExpression("return false");
        c.setValidatorId(v.getId());
        ValidationEngine.addCondition(c);

        EditableRule r = new EditableRule();
        r.setId("fvcr-rule");
        r.setJavaPath("root.repeatedObjects");
        r.setExpression("return repeatedObject.prop != 'A'");
        r.setMessage("message");
        r.setValidatorId(v.getId());
        ValidationEngine.addRule(r);

        Map<String, Object> root = new HashMap<>();
        List<Map<String, Object>> repeatedObjects = new ArrayList<>();
        root.put("repeatedObjects", repeatedObjects);
        Map<String, Object> repeatedObject0 = new HashMap<>();
        repeatedObject0.put("prop", "A");
        repeatedObjects.add(repeatedObject0);

        ValidatingContext vContext = new ValidatingContext();
        Collection<RuleFailure> results = new HashSet<>();

        // regular case, the rule is not tied to the condition, so it should fail
        results.addAll(ValidationEngine.validate(new SimpleMapValidatable("level1", root), vContext));
        results.addAll(ValidationEngine.validate(new SimpleMapValidatable("root", root), vContext));
        TestingUtils.assertEditFailure(results, "fvcr-rule");
        Assert.assertTrue(vContext.getFailedConditionIds().get("level1").contains("fvcr-condition"));
        Assert.assertTrue(vContext.getFailedRuleIds().get("root.repeatedObjects[0]").contains("fvcr-rule"));
        vContext.resetFailures();
        results.clear();

        // let's tied the rule and condition, but since the condition uses a different root level, the rule should still fail...
        r.setRuleId(ValidationEngine.getRule("fvcr-rule").getRuleId());
        r.getConditions().add("fvcr-condition");
        ValidationEngine.updateRule(r);
        results.addAll(ValidationEngine.validate(new SimpleMapValidatable("level1", root), vContext));
        results.addAll(ValidationEngine.validate(new SimpleMapValidatable("root", root), vContext));
        TestingUtils.assertEditFailure(results, "fvcr-rule");
        vContext.resetFailures();
        results.clear();

        // now let's use a special context that implements the cross-root checking; the rule shouldn't fail anymore because of the condition
        ValidatingContext specialContext = new ValidatingContext() {
            @Override
            public boolean conditionFailed(String[] validatablePaths, String conditionId) {
                StringBuilder buf = new StringBuilder();
                for (String validatablePath : validatablePaths) {
                    if (buf.length() > 0)
                        buf.append(".");
                    buf.append(validatablePath);
                    Set<String> failedIds = _failedConditionIds.get(buf.toString());
                    if (failedIds != null && failedIds.contains(conditionId))
                        return true;
                    // conceptually, we map "root" to "level1"; this could be done in a more generic way than this...
                    if ("root".equals(buf.toString())) {
                        failedIds = _failedConditionIds.get("level1");
                        if (failedIds != null && failedIds.contains(conditionId))
                            return true;
                    }
                }
                return false;
            }
        };
        results.addAll(ValidationEngine.validate(new SimpleMapValidatable("level1", root), specialContext));
        results.addAll(ValidationEngine.validate(new SimpleMapValidatable("root", root), specialContext));
        TestingUtils.assertNoEditFailure(results, "fvcr-rule");
        specialContext.resetFailures();
        results.clear();

        // now let's run the root before level1; obviously the root won't detect the level1 condition failure, and the rule will fail again...
        results.addAll(ValidationEngine.validate(new SimpleMapValidatable("root", root), specialContext));
        results.addAll(ValidationEngine.validate(new SimpleMapValidatable("level1", root), specialContext));
        TestingUtils.assertEditFailure(results, "fvcr-rule");
        specialContext.resetFailures();
        results.clear();

        ValidationEngine.deleteValidator("fvcr");
    }

    @Test
    public void testValidationException() throws Exception {
        TestingUtils.loadValidator("fake-validator-exception");

        Map<String, Object> entity = new HashMap<>();
        entity.put("prop", true);
        Validatable validatable = new SimpleMapValidatable("ID", "level1", entity);

        Collection<RuleFailure> failures = ValidationEngine.validate(validatable);
        Assert.assertEquals(1, failures.size());
        RuleFailure failure = failures.iterator().next();
        Assert.assertTrue(failure.getMessage().contains("exception"));
        Assert.assertFalse(failure.getProperties().isEmpty());

        TestingUtils.unloadValidator("fake-validator-exception");
    }

    @Test
    public void testModifyRule() throws Exception {
        TestingUtils.loadValidator("fake-validator");

        Map<String, Object> entity = new HashMap<>();
        List<Map<String, Object>> level2List = new ArrayList<>();
        Map<String, Object> level2 = new HashMap<>();
        level2List.add(level2);
        entity.put("level2", level2List);
        List<Map<String, Object>> level3List = new ArrayList<>();
        Map<String, Object> level3 = new HashMap<>();
        level3.put("prop", "1");
        level3List.add(level3);
        level2.put("level3", level3List);
        Validatable validatable = new SimpleMapValidatable("ID", "level1", entity);

        // rules need to be updated through editable objects
        Rule r1 = ValidationEngine.getRule("fv-rule1");
        EditableRule e1 = new EditableRule(r1);
        Rule r2 = ValidationEngine.getRule("fv-rule2");
        EditableRule e2 = new EditableRule(r2);
        Rule r3 = ValidationEngine.getRule("fv-rule3");
        EditableRule e3 = new EditableRule(r3);

        // by default the level3 rule should fail
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable), "fv-rule3");

        // updating the rule with the original editable rule should have no effect
        ValidationEngine.updateRule(e3);
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable), "fv-rule3");

        // *** update regular properties
        e3.setName("Another name");
        e3.setDescription("Another description");
        e3.setMessage("Another message");
        e3.setSeverity(3);
        RuleHistory hist = new RuleHistory();
        hist.setDate(new Date());
        hist.setUsername("depryf");
        e3.getHistories().add(hist);
        ValidationEngine.updateRule(e3);
        Assert.assertEquals("Another name", ValidationEngine.getRule("fv-rule3").getName());
        Assert.assertEquals("Another description", ValidationEngine.getRule("fv-rule3").getDescription());
        Assert.assertEquals("Another message", ValidationEngine.getRule("fv-rule3").getMessage());
        Assert.assertEquals(3, ValidationEngine.getRule("fv-rule3").getSeverity().intValue());
        Assert.assertFalse(ValidationEngine.getRule("fv-rule3").getHistories().isEmpty());
        Assert.assertEquals("Another message", ValidationEngine.validate(validatable).iterator().next().getMessage());

        // *** update expression
        String expression = r3.getExpression();
        r3.setExpression("return true");
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable), "fv-rule3");
        r3.setExpression(expression);
        e3.setExpression("return true");
        ValidationEngine.updateRule(e3);
        TestingUtils.assertNoEditFailure(ValidationEngine.validate(validatable), "fv-rule3");
        Assert.assertEquals("return true", ValidationEngine.getRule("fv-rule3").getExpression());
        e3.setExpression(expression);
        ValidationEngine.updateRule(e3);
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable), "fv-rule3");

        // *** update ignored flag
        r3.setIgnored(Boolean.TRUE);
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable), "fv-rule3");
        r3.setIgnored(Boolean.FALSE);
        e3.setIgnored(Boolean.TRUE);
        ValidationEngine.updateRule(e3);
        TestingUtils.assertNoEditFailure(ValidationEngine.validate(validatable), "fv-rule3");
        Assert.assertTrue(ValidationEngine.getRule("fv-rule3").getIgnored());
        e3.setIgnored(Boolean.FALSE);
        ValidationEngine.updateRule(e3);
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable), "fv-rule3");
        Assert.assertFalse(ValidationEngine.getRule("fv-rule3").getIgnored());

        // *** update condition
        Assert.assertTrue(r2.getConditions().contains("fv-condition"));
        level2.put("prop", "1"); // should make rule2 fail
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable), "fv-rule2");
        // make condition fail, rule shouldn't fail anymore
        level2.put("prop2", "IGNORED");
        TestingUtils.assertNoEditFailure(ValidationEngine.validate(validatable), "fv-rule2");
        // reset the condition, should be fine, rule should fail again
        e2.setConditions(null);
        ValidationEngine.updateRule(e2);
        Assert.assertTrue(r2.getConditions().isEmpty());
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable), "fv-rule2");
        // try to make rule3 depend on that condition; should be fine (a rule can depend on a condition defined on a higher level)
        e3.getConditions().add("fv-condition");
        ValidationEngine.updateRule(e3);
        e3.getConditions().clear();
        ValidationEngine.updateRule(e3);
        // try to make rule1 depend on that condition; should fail (a rule cannot depend on a condition that is defined on a lower level)
        boolean exception = false;
        try {
            e1.getConditions().add("fv-condition");
            ValidationEngine.updateRule(e1);
        }
        catch (ConstructionException e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Was expecting an exception but didn't get it");
        e1.getConditions().clear();
        // reset all the values
        level2.remove("prop");
        level2.remove("prop2");
        e2.getConditions().add("fv-condition");
        ValidationEngine.updateRule(e2);

        // *** update dependencies
        Set<String> dependencies = new HashSet<>(r3.getDependencies());
        // rule3 depends on rul2, so if rule2 is ignored, rule3 should not fail
        TestingUtils.assertNoEditFailure(ValidationEngine.validate(validatable, Collections.singletonList("fv-rule2")), "fv-rule3");
        Assert.assertTrue(ValidationEngine.getRule("fv-rule2").getInvertedDependencies().contains("fv-rule3"));
        e3.setDependencies(null);
        ValidationEngine.updateRule(e3);
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable, Collections.singletonList("fv-rule2")), "fv-rule3");
        Assert.assertTrue(ValidationEngine.getRule("fv-rule3").getDependencies().isEmpty());
        Assert.assertFalse(ValidationEngine.getRule("fv-rule2").getInvertedDependencies().contains("fv-rule3"));
        e3.setDependencies(dependencies);
        ValidationEngine.updateRule(e3);
        TestingUtils.assertNoEditFailure(ValidationEngine.validate(validatable, Collections.singletonList("fv-rule2")), "fv-rule3");
        Assert.assertTrue(ValidationEngine.getRule("fv-rule2").getInvertedDependencies().contains("fv-rule3"));
        // make level1 depend on level3 -> dependency exception
        e1.setDependencies(Collections.singleton("fv-rule3"));
        exception = false;
        try {
            ValidationEngine.updateRule(e1);
        }
        catch (ConstructionException e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Was expecting an exception but didn't get it");
        exception = false;
        // the update fail, there should be no change applied
        Assert.assertTrue(ValidationEngine.getRule("fv-rule1").getDependencies().isEmpty());
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable), "fv-rule3");

        // add another rule on level1; make a circular dependency
        EditableRule e1b = new EditableRule();
        e1b.setId("fv-rule11");
        e1b.setJavaPath(r1.getJavaPath());
        e1b.setMessage("msg");
        e1b.setValidatorId("fake-validator");
        e1b.setDependencies(Collections.singleton("fv-rule1"));
        ValidationEngine.addRule(e1b);
        Assert.assertNotNull(ValidationEngine.getRule("fv-rule11"));
        e1.setDependencies(Collections.singleton("fv-rule11"));
        try {
            ValidationEngine.updateRule(e1);
        }
        catch (ConstructionException e) {
            exception = true;
            Assert.assertTrue(e.getMessage().startsWith("Circular dependency detected"));
        }
        if (!exception)
            Assert.fail("Was expecting an exception but didn't get it");
        exception = false;
        ValidationEngine.deleteRule("fv-rule11");
        Assert.assertNull(ValidationEngine.getRule("fv-rule11"));

        // delete rule2 (we can't because rule3 depends on it)
        try {
            ValidationEngine.deleteRule(e2);
        }
        catch (ConstructionException e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Was expecting an exception but didn't get it");
        exception = false;
        Assert.assertNotNull(ValidationEngine.getRule("fv-rule2"));
        e3.setDependencies(null);
        ValidationEngine.updateRule(e3);
        ValidationEngine.deleteRule(e2);
        Assert.assertNull(ValidationEngine.getRule("fv-rule2"));

        // change the ID of rule3
        e3.setId("fv-rule3-changed");
        ValidationEngine.updateRule(e3);
        Assert.assertNull(ValidationEngine.getRule("fv-rule3"));
        TestingUtils.assertNoEditFailure(ValidationEngine.validate(validatable), "fv-rule3");
        Assert.assertNotNull(ValidationEngine.getRule("fv-rule3-changed"));
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable), "fv-rule3-changed");

        // add a new rule that causes some issue
        EditableRule e3b = new EditableRule();
        e3b.setId("fv-rule3-other");
        e3b.setJavaPath(r3.getJavaPath());
        e3b.setMessage("test");
        e3b.setValidatorId("fake-validator");
        // no ID
        e3b.setId(null);
        try {
            ValidationEngine.addRule(e3b);
        }
        catch (ConstructionException e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Was expecting an exception but didn't get it");
        exception = false;
        e3b.setId("fv-rule3-other");
        // no message
        e3b.setMessage(null);
        try {
            ValidationEngine.addRule(e3b);
        }
        catch (ConstructionException e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Was expecting an exception but didn't get it");
        exception = false;
        e3b.setMessage("test");
        // bad condition
        e3b.getConditions().add("?");
        try {
            ValidationEngine.addRule(e3b);
        }
        catch (ConstructionException e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Was expecting an exception but didn't get it");
        exception = false;
        e3b.getConditions().clear();
        // bad validator
        e3b.setValidatorId("?");
        try {
            ValidationEngine.addRule(e3b);
        }
        catch (ConstructionException e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Was expecting an exception but didn't get it");
        exception = false;
        e3b.setValidatorId("fake-validator");
        // duplicate ID
        e3b.setId("fv-rule3-changed");
        try {
            ValidationEngine.addRule(e3b);
        }
        catch (ConstructionException e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Was expecting an exception but didn't get it");
        exception = false;
        e3b.setId("fv-rule3-other");
        // test that the inverted dependencies are correctly updated when adding a rule
        Assert.assertFalse(ValidationEngine.getRule("fv-rule1").getInvertedDependencies().contains("fv-rule3-other"));
        e3b.setDependencies(Collections.singleton("fv-rule1"));
        ValidationEngine.addRule(e3b);
        Assert.assertTrue(ValidationEngine.getRule("fv-rule1").getInvertedDependencies().contains("fv-rule3-other"));
        ValidationEngine.deleteRule("fv-rule3-other");
        Assert.assertFalse(ValidationEngine.getRule("fv-rule1").getInvertedDependencies().contains("fv-rule3-other"));

        // delete a non-existing rule
        try {
            ValidationEngine.deleteRule("?");
        }
        catch (ConstructionException e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Was expecting an exception but didn't get it");

        TestingUtils.unloadValidator("fake-validator");
    }

    @Test
    public void testSeerdmsUsage() throws Exception {
        Validator v = TestingUtils.loadValidator("fake-validator");
        // add new rule parent
        EditableRule parent = new EditableRule();
        parent.setId("parent");
        parent.setMessage("msg");
        parent.setValidatorId("fake-validator");
        parent.setJavaPath("level1");
        ValidationEngine.addRule(parent);
        Rule parentRule = v.getRule("parent");
        Assert.assertNotNull(parentRule);

        // add new rule child, depends on parent
        EditableRule child = new EditableRule();
        child.setId("child");
        child.setMessage("msg");
        child.setValidatorId("fake-validator");
        child.setJavaPath("level1");
        child.setDependencies(Collections.singleton("parent"));
        ValidationEngine.addRule(child);
        Rule childRule = v.getRule("child");
        Assert.assertNotNull(childRule);

        // try to ignore parent (in SEER*DMS, that corresponds to a delete) -> error
        boolean exception = false;
        try {
            ValidationEngine.deleteRule("parent");
        }
        catch (ConstructionException e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Was expecting an exception, didn't get it!");

        // ignore the child -> OK
        ValidationEngine.deleteRule("child");

        // ignore the parent -> OK (since we deleted the child)
        ValidationEngine.deleteRule("parent");

        // try to active the child (in SEER*DMS, that corresponds to an add) -> error
        exception = false;
        try {
            ValidationEngine.addRule(new EditableRule(childRule));
        }
        catch (ConstructionException e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Was expecting an exception, didn't get it!");

        // active the parent -> OK
        ValidationEngine.addRule(new EditableRule(parentRule));

        // active the child -> OK
        ValidationEngine.addRule(new EditableRule(childRule));

        TestingUtils.unloadValidator("fake-validator");
    }

    @Test
    public void testModifyCondition() throws Exception {
        TestingUtils.loadValidator("fake-validator");

        Map<String, Object> entity = new HashMap<>();
        List<Map<String, Object>> level2List = new ArrayList<>();
        Map<String, Object> level2 = new HashMap<>();
        level2.put("prop", "1");
        level2List.add(level2);
        entity.put("level2", level2List);
        List<Map<String, Object>> level3List = new ArrayList<>();
        Map<String, Object> level3 = new HashMap<>();
        level3.put("prop", "1");
        level3List.add(level3);
        level2.put("level3", level3List);
        Validatable validatable = new SimpleMapValidatable("ID", "level1", entity);

        // rules need to be updated through editable objects
        Condition rs2 = ValidationEngine.getCondition("fv-condition");
        EditableCondition e2 = new EditableCondition(rs2);

        // by default the level2 rule should fail
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable), "fv-rule2");

        // *** update condition        
        String expression = rs2.getExpression();
        // make sure there is no side effect if updating the ruleset itself
        rs2.setExpression("return false");
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable), "fv-rule2");
        rs2.setExpression(expression);
        // regular update (if the ruleset fails, rule3 should not run anymore)
        e2.setExpression("return false");
        ValidationEngine.updateCondition(e2);
        TestingUtils.assertNoEditFailure(ValidationEngine.validate(validatable), "fv-rule2");
        Assert.assertEquals("return false", ValidationEngine.getCondition("fv-condition").getExpression());
        e2.setExpression(expression);
        ValidationEngine.updateCondition(e2);
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable), "fv-rule2");
        Assert.assertEquals(expression, ValidationEngine.getCondition("fv-condition").getExpression());
        // test bad condition
        e2.setExpression("!@$%^");
        boolean exception = false;
        try {
            ValidationEngine.updateCondition(e2);
        }
        catch (ConstructionException e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Was expecting an exception but didn't get it");
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable), "fv-rule2");
        Assert.assertEquals(expression, ValidationEngine.getCondition("fv-condition").getExpression());
        e2.setExpression(expression);

        // *** update java-path
        String path = rs2.getJavaPath();
        // make sure there is no side effect if updating the rule itself
        rs2.setJavaPath("level1.level2.level3");
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable), "fv-rule2");
        rs2.setJavaPath(path);
        // regular update (if the condition fails, rule3 should not run anymore)
        e2.setJavaPath("level1.level2.level3");
        ValidationEngine.updateCondition(e2);
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable), "fv-rule2");
        //assertNotNull(ValidationEngine.validate(validatable).iterator().next().getGroovyException());
        Assert.assertEquals("level1.level2.level3", ValidationEngine.getCondition("fv-condition").getJavaPath());
        e2.setJavaPath(path);
        ValidationEngine.updateCondition(e2);
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable), "fv-rule2");
        Assert.assertEquals(expression, ValidationEngine.getCondition("fv-condition").getExpression());
        // test bad path
        e2.setJavaPath("!@$%^");
        exception = false;
        try {
            ValidationEngine.updateCondition(e2);
        }
        catch (ConstructionException e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Was expecting an exception but didn't get it");
        exception = false;
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable), "fv-rule2");
        Assert.assertEquals(expression, ValidationEngine.getCondition("fv-condition").getExpression());
        e2.setJavaPath(path);

        // update ID
        e2.setId("fv-condition-changed");
        ValidationEngine.updateCondition(e2);
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable), "fv-rule2");
        Assert.assertNull(ValidationEngine.getCondition("fv-condition"));
        Assert.assertNotNull(ValidationEngine.getCondition("fv-condition-changed"));
        e2.setId("fv-condition");
        ValidationEngine.updateCondition(e2);

        // add a new co0ndition
        EditableCondition e4 = new EditableCondition();
        e4.setId("fv-condition-other");
        e4.setValidatorId("fake-validator");
        e4.setName("name");
        e4.setDescription("description");
        e4.setExpression("return false");
        e4.setJavaPath("level1.level2.level3");
        // missing ID
        e4.setId(null);
        try {
            ValidationEngine.addCondition(e4);
        }
        catch (ConstructionException e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Was expecting an exception but didn't get it");
        exception = false;
        // duplicate ID
        e4.setId("fv-condition");
        try {
            ValidationEngine.addCondition(e4);
        }
        catch (ConstructionException e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Was expecting an exception but didn't get it");
        exception = false;
        e4.setId("fv-ruleset4");
        // bad expression
        e4.setExpression("@#$%^");
        try {
            ValidationEngine.addCondition(e4);
        }
        catch (ConstructionException e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Was expecting an exception but didn't get it");
        exception = false;
        e4.setExpression("return false");
        // missing java path
        e4.setJavaPath(null);
        try {
            ValidationEngine.addCondition(e4);
        }
        catch (ConstructionException e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Was expecting an exception but didn't get it");
        exception = false;
        e4.setJavaPath("level1.level2.level3");
        // bad java path
        e4.setJavaPath("hum?");
        try {
            ValidationEngine.addCondition(e4);
        }
        catch (ConstructionException e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Was expecting an exception but didn't get it");
        exception = false;
        e4.setJavaPath("level1.level2.level3");

        // successful addition
        ValidationEngine.addCondition(e4);
        Assert.assertNotNull(ValidationEngine.getCondition("fv-ruleset4"));

        // deletion
        ValidationEngine.deleteCondition("fv-ruleset4");
        Assert.assertNull(ValidationEngine.getCondition("fv-ruleset4"));

        // bad deletion
        try {
            ValidationEngine.deleteCondition("hum?");
        }
        catch (ConstructionException e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Was expecting an exception but didn't get it");

        TestingUtils.unloadValidator("fake-validator");
    }

    @Test
    public void testModifyValidator() throws Exception {

        Validator v = TestingUtils.loadValidator("fake-validator");
        Assert.assertNotNull(ValidationEngine.getValidator("fake-validator"));

        Map<String, Object> entity = new HashMap<>();
        List<Map<String, Object>> level2List = new ArrayList<>();
        Map<String, Object> level2 = new HashMap<>();
        level2List.add(level2);
        entity.put("level2", level2List);
        List<Map<String, Object>> level3List = new ArrayList<>();
        Map<String, Object> level3 = new HashMap<>();
        level3.put("prop", "1");
        level3List.add(level3);
        level2.put("level3", level3List);
        Validatable validatable = new SimpleMapValidatable("ID", "level1", entity);

        // by default the level3 rule should fail
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable), "fv-rule3");

        EditableValidator editableValidator = new EditableValidator(v);
        editableValidator.setName("OTHER");
        editableValidator.setHash("123");
        editableValidator.setId("fake-validator-changed");
        ValidationEngine.updateValidator(editableValidator);
        Assert.assertNull(ValidationEngine.getValidator("fake-validator"));
        Assert.assertNotNull(ValidationEngine.getValidator("fake-validator-changed"));
        Assert.assertEquals("OTHER", ValidationEngine.getValidator("fake-validator-changed").getName());
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable), "fv-rule3");

        ValidationEngine.deleteValidator("fake-validator-changed");
        Assert.assertNull(ValidationEngine.getValidator("fake-validator"));
        TestingUtils.assertNoEditFailure(ValidationEngine.validate(validatable), "fv-rule3");
    }

    @Test
    public void testModifyContext() throws Exception {

        TestingUtils.loadValidator("fake-validator");
        Assert.assertEquals(1, ValidationEngine.getValidator("fake-validator").getRawContext().size());
        Assert.assertNotNull(ValidationEngine.getContext("FV_CONTEXT1", "fake-validator"));

        Map<String, Object> entity = new HashMap<>();
        List<Map<String, Object>> level2List = new ArrayList<>();
        Map<String, Object> level2 = new HashMap<>();
        level2List.add(level2);
        entity.put("level2", level2List);
        List<Map<String, Object>> level3List = new ArrayList<>();
        Map<String, Object> level3 = new HashMap<>();
        level3.put("prop", "1");
        level3List.add(level3);
        level2.put("level3", level3List);
        Validatable validatable = new SimpleMapValidatable("ID", "level1", entity);

        // by default the level3 rule should fail
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable), "fv-rule3");

        ValidationEngine.addContext(null, "test", "fake-validator", "[1,2,3]", "java");
        Assert.assertNotNull(ValidationEngine.getContext("test"));
        Assert.assertEquals(2, ValidationEngine.getValidator("fake-validator").getRawContext().size());

        // add a bad context
        boolean exception = false;
        try {
            ValidationEngine.addContext(null, "test2", "fake-validator", "[1,2,3", "java");
        }
        catch (ConstructionException e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Was expecting an exception but didn't get it");
        exception = false;
        // duplicate key
        try {
            ValidationEngine.addContext(null, "test", "fake-validator", "[1,2,3]", "java");
        }
        catch (ConstructionException e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Was expecting an exception but didn't get it");
        exception = false;
        // bad type
        try {
            ValidationEngine.addContext(null, "test2", "fake-validator", "[1,2,3]", "?");
        }
        catch (ConstructionException e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Was expecting an exception but didn't get it");
        exception = false;
        // unknown validator ID
        try {
            ValidationEngine.addContext(null, "test2", "?", "[1,2,3]", "java");
        }
        catch (ConstructionException e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Was expecting an exception but didn't get it");
        exception = false;

        // delete new context
        ValidationEngine.deleteContext("test", "fake-validator");
        Assert.assertNull(ValidationEngine.getContext("test"));
        Assert.assertEquals(1, ValidationEngine.getValidator("fake-validator").getRawContext().size());

        // bad deletion
        try {
            ValidationEngine.deleteContext("?", "fake-validator");
        }
        catch (ConstructionException e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Was expecting an exception but didn't get it");
        exception = false;
        try {
            ValidationEngine.deleteContext("test", "?");
        }
        catch (ConstructionException e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Was expecting an exception but didn't get it");
        exception = false;

        // update existing context (which is used by rule3)
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable), "fv-rule3");
        ValidationEngine.updateContext("FV_CONTEXT1", "fake-validator", "return '0'", "groovy");
        TestingUtils.assertNoEditFailure(ValidationEngine.validate(validatable), "fv-rule3");
        Assert.assertEquals("return '0'", ValidationEngine.getValidator("fake-validator").getRawContext("FV_CONTEXT1").getExpression());

        // bad update
        try {
            ValidationEngine.updateContext("FV_CONTEXT1", "fake-validator", "return '0'", "java");
        }
        catch (ConstructionException e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Was expecting an exception but didn't get it");

        TestingUtils.unloadValidator("fake-validator");
    }

    @Test
    public void testContextPrefix() throws ValidationException {

        // test old syntax (no prefix)
        TestingUtils.loadValidator("fake-validator-context-in-context-old-syntax");
        try {
            Assert.assertEquals(3, ValidationEngine.getValidator("fake-validator-context-in-context-old-syntax").getRawContext().size());
            Validatable validatable = new SimpleMapValidatable("ID", "level1", new HashMap<>());
            TestingUtils.assertNoEditFailure(ValidationEngine.validate(validatable), "fvcc-rule1");
        }
        finally {
            TestingUtils.unloadValidator("fake-validator-context-in-context-old-syntax");
        }

        // test new syntax (prefix)
        TestingUtils.loadValidator("fake-validator-context-in-context");
        try {
            Assert.assertEquals(3, ValidationEngine.getValidator("fake-validator-context-in-context").getRawContext().size());
            Validatable validatable = new SimpleMapValidatable("ID", "level1", new HashMap<>());
            TestingUtils.assertNoEditFailure(ValidationEngine.validate(validatable), "fvcc-rule1");
        }
        finally {
            TestingUtils.unloadValidator("fake-validator-context-in-context");
        }
    }

    @Test
    public void testMassUpdateIgnoreFlags() throws Exception {
        TestingUtils.loadValidator("fake-validator");

        // remove the dependencies
        for (int i = 1; i <= 3; i++) {
            EditableRule r = new EditableRule(ValidationEngine.getRule("fv-rule" + i));
            r.setDependencies(null);
            ValidationEngine.updateRule(r);
        }

        // create a validatable where the three rules are going to fail
        Map<String, Object> entity = new HashMap<>();
        entity.put("prop", "1");
        List<Map<String, Object>> level2List = new ArrayList<>();
        Map<String, Object> level2 = new HashMap<>();
        level2.put("prop", "1");
        level2List.add(level2);
        entity.put("level2", level2List);
        List<Map<String, Object>> level3List = new ArrayList<>();
        Map<String, Object> level3 = new HashMap<>();
        level3.put("prop", "1");
        level3List.add(level3);
        level2.put("level3", level3List);
        Validatable validatable = new SimpleMapValidatable("ID", "level1", entity);

        // default validation: the three rules should fail
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable), "fv-rule1");
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable), "fv-rule2");
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable), "fv-rule3");

        // turn the three edits off
        List<String> idsToIgnore = new ArrayList<>();
        idsToIgnore.add("fv-rule1");
        idsToIgnore.add("fv-rule2");
        idsToIgnore.add("fv-rule3");
        ValidationEngine.massUpdateIgnoreFlags(idsToIgnore, null);
        TestingUtils.assertNoEditFailure(ValidationEngine.validate(validatable), "fv-rule1");
        TestingUtils.assertNoEditFailure(ValidationEngine.validate(validatable), "fv-rule2");
        TestingUtils.assertNoEditFailure(ValidationEngine.validate(validatable), "fv-rule3");

        // turn them on one-by-one
        List<String> idsToStopIgnore = new ArrayList<>();
        ValidationEngine.massUpdateIgnoreFlags(null, idsToStopIgnore);
        TestingUtils.assertNoEditFailure(ValidationEngine.validate(validatable), "fv-rule1");
        TestingUtils.assertNoEditFailure(ValidationEngine.validate(validatable), "fv-rule2");
        TestingUtils.assertNoEditFailure(ValidationEngine.validate(validatable), "fv-rule3");
        idsToStopIgnore.add("fv-rule1");
        ValidationEngine.massUpdateIgnoreFlags(null, idsToStopIgnore);
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable), "fv-rule1");
        TestingUtils.assertNoEditFailure(ValidationEngine.validate(validatable), "fv-rule2");
        TestingUtils.assertNoEditFailure(ValidationEngine.validate(validatable), "fv-rule3");
        idsToStopIgnore.add("fv-rule2");
        ValidationEngine.massUpdateIgnoreFlags(null, idsToStopIgnore);
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable), "fv-rule1");
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable), "fv-rule2");
        TestingUtils.assertNoEditFailure(ValidationEngine.validate(validatable), "fv-rule3");
        idsToStopIgnore.add("fv-rule3");
        ValidationEngine.massUpdateIgnoreFlags(null, idsToStopIgnore);
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable), "fv-rule1");
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable), "fv-rule2");
        TestingUtils.assertEditFailure(ValidationEngine.validate(validatable), "fv-rule3");

        TestingUtils.unloadValidator("fake-validator");
    }

    @Test
    public void testForcedRule() throws Exception {

        // forced rules are also tested in the testValidate() method, this is testing a more specific case...

        // we want to test a fake edit with alias (and root) "level" when no other edits exist for that level...
        Rule r = new Rule();
        r.setId("tmp");
        r.setJavaPath("level");
        r.setExpression("return false");
        TestingUtils.assertEditFailure(ValidationEngine.validate(new SimpleMapValidatable("ID", "level", new HashMap<>()), r), "tmp");

        // if the validatable uses a wrong path, that's fine
        TestingUtils.assertNoEditFailure(ValidationEngine.validate(new SimpleMapValidatable("ID", "whatever", new HashMap<>()), r), "tmp");

        // but the forced rule must have a valid java path!
        try {
            r.setJavaPath("whatever");
            ValidationEngine.validate(new SimpleMapValidatable("ID", "level", new HashMap<>()), r);
        }
        catch (ValidationException e) {
            return;
        }
        Assert.fail("Was expecting an exception, didn't get it...");
    }
}
