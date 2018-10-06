/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation.entities;

import com.imsweb.validation.TestingUtils;
import com.imsweb.validation.ValidationEngine;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class SimpleMapValidatableTest {

    @Before
    public void setUp() {
        TestingUtils.init();
    }

    @Test
    public void testSimpleMapValidatable() throws Exception {
        TestingUtils.loadValidator("fake-validator");

        // I am using the 'fake-validator.xml' for this test, so my entity has three levels: level1, level2 and level3
        Map<String, Object> entity = new HashMap<>();
        entity.put("prop", "1"); // should trigger fv-rule1

        // null map
        boolean exception = false;
        try {
            new SimpleMapValidatable("ID", "level1", null);
        }
        catch (RuntimeException e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Where is the exception?");

        // empty map
        SimpleMapValidatable v = new SimpleMapValidatable("ID", "level1", entity);
        Assert.assertEquals("ID", v.getDisplayId());
        Assert.assertNull(v.getCurrentTumorId());
        Assert.assertEquals("level1", v.getRootLevel());
        Assert.assertEquals(1, ValidationEngine.getInstance().validate(v).size());

        // default root
        v = new SimpleMapValidatable(entity);
        Assert.assertEquals("?", v.getDisplayId());
        Assert.assertNull(v.getCurrentTumorId());
        Assert.assertEquals("record", v.getRootLevel());

        // add a second level
        entity.put("prop", "0"); // because of dependencies, we don't want level1 to fail anymore
        List<Map<String, Object>> secondLevel = new ArrayList<>();
        secondLevel.add(Collections.singletonMap("prop", "1"));
        secondLevel.add(Collections.singletonMap("prop", "1"));
        secondLevel.add(Collections.singletonMap("prop", "1"));
        entity.put("level2", secondLevel);
        v = new SimpleMapValidatable("ID", "level1", entity);
        Assert.assertEquals(3, ValidationEngine.getInstance().validate(v).size());

        // and finish with a third level
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> thirdLevel = new ArrayList<>();
        thirdLevel.add(Collections.singletonMap("prop", "1"));
        thirdLevel.add(Collections.singletonMap("prop", "1"));
        thirdLevel.add(Collections.singletonMap("prop", "1"));
        map.put("level3", thirdLevel);
        secondLevel.add(map);
        v = new SimpleMapValidatable("ID", "level1", entity);
        Assert.assertEquals(6, ValidationEngine.getInstance().validate(v).size());

        // test that the reported path are correct
        SortedSet<String> expected = new TreeSet<>();
        expected.add("level1.level2[0].otherProp");
        expected.add("level1.level2[1].otherProp");
        expected.add("level1.level2[2].otherProp");
        expected.add("level1.level2[3].level3[0].prop");
        expected.add("level1.level2[3].level3[1].prop");
        expected.add("level1.level2[3].level3[2].prop");

        SortedSet<String> actual = new TreeSet<>();
        for (RuleFailure f : ValidationEngine.getInstance().validate(v))
            actual.addAll(f.getProperties());

        Assert.assertEquals(expected, actual);

        TestingUtils.unloadValidator("fake-validator");
    }

    @Test
    public void testFollowCollection() throws IllegalAccessException {

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

        // create the entity and the validatable
        Map<String, Object> m1 = Collections.singletonMap("prop", "1");
        Map<String, Object> m2 = Collections.singletonMap("prop", "1");
        Map<String, Object> m3 = Collections.singletonMap("prop", "1");
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(m1);
        list.add(m2);
        list.add(m3);
        entity.put("level3", list);
        entity.put("level5", 1L);
        SimpleMapValidatable v = new SimpleMapValidatable("ID", "level1", entity);

        // default entity is created with one element on each level
        Assert.assertEquals(1, v.followCollection("level2").size());

        // level3 should not correspond to three sub-entities   
        Assert.assertEquals(3, v.followCollection("level3").size());

        // try level4 (does not correspond to any property)
        Assert.assertEquals(0, v.followCollection("level4").size());

        // try to pass a bad property
        Assert.assertEquals(0, v.followCollection("hum?").size());

        // try to pass a null property
        Assert.assertEquals(0, v.followCollection(null).size());

        // try to pass a property that does not correspond to a list 
        boolean exception = false;
        try {
            v.followCollection("level5");
        }
        catch (IllegalAccessException e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Where is the exception?");
    }

    @Test
    public void testReportFailureForProperty() throws Exception {

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
        SimpleMapValidatable v = new SimpleMapValidatable("ID", "level1", entity);

        v.reportFailureForProperty(null);
        Assert.assertEquals(0, v.getPropertiesWithError().size());

        v.reportFailureForProperty("");
        Assert.assertEquals(0, v.getPropertiesWithError().size());

        v.reportFailureForProperty("    ");
        Assert.assertEquals(0, v.getPropertiesWithError().size());

        v.reportFailureForProperty("level1.prop");
        Assert.assertEquals(1, v.getPropertiesWithError().size());

        v.reportFailureForProperty("level1.level2.prop");
        Assert.assertEquals(2, v.getPropertiesWithError().size());

        v.reportFailureForProperty("level1.level3.prop");
        Assert.assertEquals(3, v.getPropertiesWithError().size());

        v.reportFailureForProperty("level1.level2.level3.level4.prop");
        Assert.assertEquals(4, v.getPropertiesWithError().size());

        v.clearPropertiesWithError();
        Assert.assertEquals(0, v.getPropertiesWithError().size());

        v.reportFailureForProperty("level1");
        Assert.assertEquals(0, v.getPropertiesWithError().size());

        v.reportFailureForProperty("level4");
        Assert.assertEquals(0, v.getPropertiesWithError().size());

        // reporting a property that is lower than the current level should not work
        v.reportFailureForProperty("level2.prop");
        Assert.assertEquals(0, v.getPropertiesWithError().size());
    }

    @Test
    public void testForceFailureOnEntityMechanism() throws Exception {
        TestingUtils.loadValidator("fake-validator-force-failure-on-entity");

        Rule r1 = ValidationEngine.getInstance().getValidators().get("fake-validator-force-failure-on-entity").getRule("fv-unique-rule-1");
        Rule r2 = ValidationEngine.getInstance().getValidators().get("fake-validator-force-failure-on-entity").getRule("fv-unique-rule-2");

        Collection<RuleFailure> failures;
        Map<String, Object> entity = new HashMap<>();

        // null collection of level2 objects -> there should be an exception in the edit
        failures = ValidationEngine.getInstance().validate(new SimpleMapValidatable("ID", "level1", entity), r1.getId());
        Assert.assertTrue(failures.iterator().next().getProperties().isEmpty());

        // empty collection of level2 objects -> mechanism should not be used
        List<Map<String, Object>> list = new ArrayList<>();
        entity.put("level2", list);
        failures = ValidationEngine.getInstance().validate(new SimpleMapValidatable("ID", "level1", entity), r1.getId());
        Assert.assertTrue(failures.iterator().next().getProperties().isEmpty());

        // a single level2 object -> mechanism should not be used
        Map<String, Object> obj1 = Collections.singletonMap("prop", "1");
        list.add(obj1);
        failures = ValidationEngine.getInstance().validate(new SimpleMapValidatable("ID", "level1", entity), r1.getId());
        Assert.assertTrue(failures.iterator().next().getProperties().isEmpty());

        // add a second level2 object -> mechanism should be used, both object should report properties
        Map<String, Object> obj2 = new HashMap<>();
        obj2.put("prop", "1");
        obj2.put("otherProp", "2");
        list.add(obj2);
        failures = ValidationEngine.getInstance().validate(new SimpleMapValidatable("ID", "level1", entity), r1.getId());
        Assert.assertEquals(2, failures.iterator().next().getProperties().size());
        Assert.assertTrue(failures.iterator().next().getProperties().contains("level1.level2[0].prop"));
        Assert.assertTrue(failures.iterator().next().getProperties().contains("level1.level2[1].prop"));

        // add a third map
        Map<String, Object> obj3 = new HashMap<>();
        obj3.put("prop", "1");
        obj3.put("otherProp", "3");
        list.add(obj3);
        failures = ValidationEngine.getInstance().validate(new SimpleMapValidatable("ID", "level1", entity), r1.getId());
        Assert.assertEquals(2, failures.iterator().next().getProperties().size());
        Assert.assertTrue(failures.iterator().next().getProperties().contains("level1.level2[0].prop"));
        Assert.assertTrue(failures.iterator().next().getProperties().contains("level1.level2[2].prop"));

        // same test using second rule
        failures = ValidationEngine.getInstance().validate(new SimpleMapValidatable("ID", "level1", entity), r2.getId());
        Assert.assertEquals(2, failures.iterator().next().getProperties().size());
        Assert.assertTrue(failures.iterator().next().getProperties().contains("level1.level2[0].prop"));
        Assert.assertTrue(failures.iterator().next().getProperties().contains("level1.level2[2].prop"));

        TestingUtils.unloadValidator("fake-validator-force-failure-on-entity");
    }
}
