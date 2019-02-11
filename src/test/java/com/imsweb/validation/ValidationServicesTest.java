/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.imsweb.validation.entities.ContextTable;
import com.imsweb.validation.entities.ContextTableIndex;
import com.imsweb.validation.entities.SimpleMapValidatable;
import com.imsweb.validation.entities.Validatable;

/**
 * Created on Feb 24, 2011 by depryf
 * @author depryf
 */
public class ValidationServicesTest {

    @Before
    public void setUp() {
        TestingUtils.init();
    }

    /**
     * Created on Aug 27, 2010 by depryf
     */
    @Test
    public void testGetAliasForJavaPath() {
        Assert.assertNull(ValidationServices.getInstance().getAliasForJavaPath(null));
        Assert.assertNull(ValidationServices.getInstance().getAliasForJavaPath("something"));
        Assert.assertNotNull(ValidationServices.getInstance().getAliasForJavaPath("lines.line"));
    }

    /**
     * Created on Aug 27, 2010 by depryf
     */
    @Test
    public void testGetJavaPathForAlias() {
        Assert.assertNull(ValidationServices.getInstance().getJavaPathForAlias(null));
        Assert.assertNull(ValidationServices.getInstance().getJavaPathForAlias("something"));
        Assert.assertNotNull(ValidationServices.getInstance().getJavaPathForAlias("line"));
    }

    /**
     * Created on Apr 5, 2011 by depryf
     */
    @Test
    public void testGetAllJavaPaths() {
        Assert.assertNotNull(ValidationServices.getInstance().getAllJavaPaths());
    }

    /**
     * Created on Aug 27, 2010 by depryf
     */
    @Test
    public void testGetLookupById() {
        Assert.assertNull(ValidationServices.getInstance().getLookupById("id"));
    }

    /**
     * Created on Aug 27, 2010 by depryf
     */
    @Test
    public void testGetConfVariable() {
        Assert.assertNull(ValidationServices.getInstance().getConfVariable("id"));
    }

    /**
     * Created on Aug 27, 2010 by depryf
     */
    @Test
    public void testGetNextRuleSequence() {
        Assert.assertTrue(ValidationServices.getInstance().getNextRuleSequence() > 0);
    }

    /**
     * Created on Aug 27, 2010 by depryf
     */
    @Test
    public void testGetNextRulesetSequence() {
        Assert.assertTrue(ValidationServices.getInstance().getNextRuleSequence() > 0);
    }

    /**
     * Created on Aug 27, 2010 by depryf
     */
    @Test
    public void testAddContextExpression() throws ConstructionException {
        Map<String, Object> context = new HashMap<>();

        Assert.assertEquals(1, ValidationServices.getInstance().addContextExpression("1", context, "key1", "groovy"));
        Assert.assertEquals(1, context.size());

        ValidationServices.getInstance().addContextExpression("[1,2]", context, "key2", "java");
        Assert.assertEquals(2, context.size());

        ValidationServices.getInstance().addContextExpression("[1:'A']", context, "key3", "java");
        Assert.assertEquals(3, context.size());

        ValidationServices.getInstance().addContextExpression("[[1,1],[2,2]]", context, "key4", "java");
        Assert.assertEquals(4, context.size());

        ValidationServices.getInstance().addContextExpression("123", context, "key5", "java");
        Assert.assertEquals(5, context.size());

        ValidationServices.getInstance().addContextExpression("123", context, "key6", "java");
        Assert.assertEquals(6, context.size());
    }

    /**
     * Created on Aug 27, 2010 by depryf
     */
    @Test
    public void testAddGroovyContextExpression() throws ConstructionException {
        Map<String, Object> context = new HashMap<>();

        ValidationServices.getInstance().addGroovyContextExpression("return 1", context, "key1");
        Assert.assertEquals(1, context.size());

        ValidationServices.getInstance().addGroovyContextExpression("return [1, 2]", context, "key2");
        Assert.assertEquals(2, context.size());

        ValidationServices.getInstance().addGroovyContextExpression("return [1:'A', 2:'B']", context, "key3");
        Assert.assertEquals(3, context.size());

        ValidationServices.getInstance().addGroovyContextExpression("return {x, y -> x + y}", context, "key4");
        Assert.assertEquals(4, context.size());
    }

    @Test
    public void testAddTableContextExpression() throws ConstructionException {
        Map<String, Object> context = new HashMap<>();

        ValidationServices.getInstance().addTableContextExpression("[['header1', 'header2'], ['val1', 'val2']]", context, "table1");
        Assert.assertTrue(((ContextTable)context.get("table1")).getHeaders().contains("header2"));

        ValidationServices.getInstance().addTableContextExpression("[['header'], ['val1'], ['val2'], ['val3']]", context, "table2");
        Assert.assertEquals(3, ((ContextTable)context.get("table2")).getData().size());
    }

    @Test
    public void testAddTableIndexDefContextExpression() throws ConstructionException {
        Map<String, Object> context = new HashMap<>();

        // indexes require a table
        ValidationServices.getInstance().addTableContextExpression("[['header1', 'header2'], ['val1', 'val2'], ['val1', 'val3']]", context, "tableX");

        // index based on first column doesn't hav unique values -> should use a list
        ValidationServices.getInstance().addTableIndexDefContextExpression("['table': 'tableX', 'columns' : 'header1']", context, "index1");
        Assert.assertFalse(((ContextTableIndex)context.get("index1")).hasUniqueKeys());
        Assert.assertEquals(0, ((ContextTableIndex)context.get("index1")).find("val1")); // first row with the value should be returned

        // index based on second column has unique values -> should use a map
        ValidationServices.getInstance().addTableIndexDefContextExpression("['table': 'tableX', 'columns' : 'header2']", context, "index2");
        Assert.assertTrue(((ContextTableIndex)context.get("index2")).hasUniqueKeys());
        Assert.assertEquals(1, ((ContextTableIndex)context.get("index2")).find("val3"));

        // index based on both column has unique values -> should use a map
        ValidationServices.getInstance().addTableIndexDefContextExpression("['table': 'tableX', 'columns' : 'header1,header2']", context, "index3");
        Assert.assertTrue(((ContextTableIndex)context.get("index3")).hasUniqueKeys());
        Assert.assertEquals(0, ((ContextTableIndex)context.get("index3")).find("val1val2"));

        // spacing doesn't matter in the columns
        ValidationServices.getInstance().addTableIndexDefContextExpression("['table': 'tableX', 'columns' : ' header1,  header2 ']", context, "index4");
        Assert.assertTrue(((ContextTableIndex)context.get("index4")).hasUniqueKeys());
        Assert.assertEquals(0, ((ContextTableIndex)context.get("index4")).find("val1val2"));

        // a table is required before the index is added
        try {
            ValidationServices.getInstance().addTableIndexDefContextExpression("['table': 'tableY', 'columns' : 'header']", context, "index");
            Assert.fail("Was expecting an exception here!");
        }
        catch (ConstructionException e) {
            // expected
        }
    }

    /**
     * Created on Oct 5, 2010 by depryf
     */
    @Test
    public void testAddJavaContextExpression() throws ConstructionException {
        Map<String, Object> context = new HashMap<>();

        // integer
        ValidationServices.getInstance().addJavaContextExpression("-1", context, "key");
        Assert.assertEquals(-1, context.get("key"));

        // string
        ValidationServices.getInstance().addJavaContextExpression("'a'", context, "key");
        Assert.assertEquals("a", context.get("key"));

        // empty list
        List<Object> list = new ArrayList<>();
        ValidationServices.getInstance().addJavaContextExpression("[]", context, "key");
        Assert.assertEquals(list, context.get("key"));

        // simple list
        list.add(1);
        list.add(2);
        list.add(3);
        ValidationServices.getInstance().addJavaContextExpression("[1,2,3]", context, "key");
        Assert.assertEquals(list, context.get("key"));

        // complex list
        list.add("4");
        list.add(5);
        list.add(6);
        list.add(7);
        List<Object> list2 = new ArrayList<>();
        list2.add(8);
        list2.add(10);
        list2.add(11);
        list2.add(12);
        list2.add(14);
        list.add(list2);
        Map<Object, Object> m = new HashMap<>();
        m.put(1, "a");
        list.add(m);
        list.add(20);
        ValidationServices.getInstance().addJavaContextExpression("[1,2,3, '4',5..7,[8,10 .. 12,14], [1:'a'],20]", context, "key");
        Assert.assertEquals(list, context.get("key"));

        // empty map
        Map<Object, Object> map = new HashMap<>();
        ValidationServices.getInstance().addJavaContextExpression("[:]", context, "key");
        Assert.assertEquals(map, context.get("key"));

        // simple map
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        ValidationServices.getInstance().addJavaContextExpression("['a':1,'b':2,'c':3]", context, "key");
        Assert.assertEquals(map, context.get("key"));

        // complex map (if the key is a list, it is automatically explosed into its elements)
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        map.put(1, list);
        map.put(3, list);
        map.put(4, list);
        map.put(5, list);
        map.put(7, list);
        map.put("d", 4);
        ValidationServices.getInstance().addJavaContextExpression("['a':1,'b':2,'c':3, [1,3..5,7] : [1,2,3, '4',5..7,[8,10 .. 12,14], [1:'a'],20] ,'d':4]", context, "key");
        Assert.assertEquals(map, context.get("key"));

        // reference to other context entries (called variable in JFlex)
        context.put("OTHER_KEY", 3);
        List<Object> intList = new ArrayList<>();
        intList.add(1);
        intList.add(2);
        intList.add(3);
        ValidationServices.getInstance().addJavaContextExpression("[1,2,OTHER_KEY]", context, "key");
        Assert.assertEquals(intList, context.get("key"));

        // a more complicated reference
        context.put("mapKey", map);
        intList.add(map);
        intList.add("a");
        ValidationServices.getInstance().addJavaContextExpression("[1,2,OTHER_KEY, mapKey ,'a']", context, "key");
        Assert.assertEquals(intList, context.get("key"));

        // a reference to a non-existing key
        boolean exception = false;
        try {
            ValidationServices.getInstance().addJavaContextExpression("[1,2,OTHER_KEY, mapKey ,'a', UNKNOWN]", context, "key");
        }
        catch (ConstructionException e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("Was excepting exception, didn't get it!");
    }

    /**
     * Created on Aug 27, 2010 by depryf
     */
    @Test
    public void testFillInMessage() {
        Map<String, Object> map = new HashMap<>();
        map.put("vitalStatus", "1");
        map.put("nameLast", "LAST");
        map.put("dateOfDiagnosis", "20110623");
        map.put("dateOfDiagnosisMinusDay", "201106");
        map.put("dateOfDiagnosisMinusDayMonth", "2011");
        map.put("dateOfDiagnosisBlank", "");
        Validatable v = new SimpleMapValidatable("TEST", "line", map);

        Assert.assertEquals("", ValidationServices.getInstance().fillInMessage(null, v));
        Assert.assertEquals("", ValidationServices.getInstance().fillInMessage("", v));
        Assert.assertEquals("Something", ValidationServices.getInstance().fillInMessage("Something", v));
        Assert.assertEquals("Something with a value of <BLANK>", ValidationServices.getInstance().fillInMessage("Something with a value of ${line.whatever}", v));
        Assert.assertEquals("Something with a value of 1", ValidationServices.getInstance().fillInMessage("Something with a value of ${line.vitalStatus}", v));
        Assert.assertEquals("Something with a value of 1 and 'LAST'", ValidationServices.getInstance().fillInMessage("Something with a value of ${line.vitalStatus} and '${line.nameLast}'", v));
        Assert.assertEquals("LAST and something with a value of 1 and 'LAST'", ValidationServices.getInstance().fillInMessage(
                "${line.nameLast} and something with a value of ${line.vitalStatus} and '${line.nameLast}'", v));

        Assert.assertEquals("Wrong date (Y:2011 M:06 D:23)!!!", ValidationServices.getInstance().fillInMessage("Wrong date (${line.dateOfDiagnosis.formatDate()})!!!", v));
        Assert.assertEquals("Wrong date (Y:2011 M:06 D:)!!!", ValidationServices.getInstance().fillInMessage("Wrong date (${line.dateOfDiagnosisMinusDay.formatDate()})!!!", v));
        Assert.assertEquals("Wrong date (Y:2011 M:   D:)!!!", ValidationServices.getInstance().fillInMessage("Wrong date (${line.dateOfDiagnosisMinusDayMonth.formatDate()})!!!", v));
        Assert.assertEquals("Wrong date (Y:     M:   D:)!!!", ValidationServices.getInstance().fillInMessage("Wrong date (${line.dateOfDiagnosisBlank.formatDate()})!!!", v));
    }

    @Test
    public void testFillInMessags() {
        Map<String, Object> map = new HashMap<>();
        map.put("vitalStatus", "1");
        map.put("nameLast", "LAST");
        Validatable v = new SimpleMapValidatable("TEST", "line", map);

        Assert.assertNull(ValidationServices.getInstance().fillInMessages(null, v));
        Assert.assertEquals(new ArrayList<>(), ValidationServices.getInstance().fillInMessages(new ArrayList<>(), v));
        Assert.assertEquals(Arrays.asList("Something", "Something else"), ValidationServices.getInstance().fillInMessages(Arrays.asList("Something", "Something else"), v));
        Assert.assertEquals(Arrays.asList("Value 1", "Value 'LAST'"), ValidationServices.getInstance().fillInMessages(Arrays.asList("Value ${line.vitalStatus}", "Value '${line.nameLast}'"), v));
    }

    @Test
    public void testCompareEngineVersions() {
        ValidationServices services = ValidationServices.getInstance();

        // first version is null
        Assert.assertTrue(services.compareEngineVersions(null, null) < 0);
        Assert.assertTrue(services.compareEngineVersions(null, "") < 0);
        Assert.assertTrue(services.compareEngineVersions(null, "ABC") < 0);
        Assert.assertTrue(services.compareEngineVersions(null, "1.0") < 0);

        // second version is null
        Assert.assertTrue(services.compareEngineVersions("", null) > 0);
        Assert.assertTrue(services.compareEngineVersions("ABC", null) > 0);
        Assert.assertTrue(services.compareEngineVersions("1.0", null) > 0);

        // first version is not valid
        Assert.assertTrue(services.compareEngineVersions("ABC", "") > 0);
        Assert.assertEquals(0, services.compareEngineVersions("ABC", "ABC"));
        Assert.assertTrue(services.compareEngineVersions("ABC", "1.0") > 0);
        Assert.assertTrue(services.compareEngineVersions("1.1.1", "1.0") > 0);

        // second version is not valid
        Assert.assertTrue(services.compareEngineVersions("", "ABC") < 0);
        Assert.assertEquals(0, services.compareEngineVersions("ABC", "ABC"));
        Assert.assertTrue(services.compareEngineVersions("1.0", "ABC") < 0);
        Assert.assertTrue(services.compareEngineVersions("1.0", "1.1.1") < 0);

        // versions are both valid
        Assert.assertEquals(0, services.compareEngineVersions("1.0", "1.0"));
        Assert.assertEquals(0, services.compareEngineVersions("1.1", "1.1"));
        Assert.assertTrue(services.compareEngineVersions("1.0", "1.1") < 0);
        Assert.assertTrue(services.compareEngineVersions("1.1", "1.0") > 0);
        Assert.assertTrue(services.compareEngineVersions("1.0", "2.0") < 0);
        Assert.assertTrue(services.compareEngineVersions("2.0", "1.0") > 0);
        Assert.assertTrue(services.compareEngineVersions("1.0", "2.1") < 0);
        Assert.assertTrue(services.compareEngineVersions("2.1", "1.0") > 0);
        Assert.assertTrue(services.compareEngineVersions("5.9", "5.10") < 0);
        Assert.assertTrue(services.compareEngineVersions("5.10", "5.9") > 0);
        Assert.assertEquals(0, services.compareEngineVersions("5.10", "5.10"));
        Assert.assertTrue(services.compareEngineVersions("10.0", "10.1") < 0);
        Assert.assertTrue(services.compareEngineVersions("10.1", "10.0") > 0);
        Assert.assertEquals(0, services.compareEngineVersions("10.1", "10.1"));
    }
}
