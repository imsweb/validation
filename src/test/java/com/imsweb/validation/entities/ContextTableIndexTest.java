/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.validation.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class ContextTableIndexTest {

    @Test
    public void testFind() {
        List<List<String>> tableData = new ArrayList<>();
        tableData.add(Arrays.asList("header1", "header2", "header3"));
        tableData.add(Arrays.asList("v1", "v2", "v3"));
        tableData.add(Arrays.asList(" 1", " 2", " 3"));
        tableData.add(Arrays.asList("v ", "v ", "v3"));

        ContextTable table = new ContextTable("table", tableData);

        ContextTableIndex idx1 = new ContextTableIndex("idx1",  table, Collections.singletonList("header1"));
        Assert.assertTrue(idx1.hasUniqueKeys());
        Assert.assertEquals(-1, idx1.find("?"));
        Assert.assertEquals(-1, idx1.find("1"));
        Assert.assertEquals(-1, idx1.find("v"));
        Assert.assertEquals(-1, idx1.find(" v"));
        Assert.assertEquals(-1, idx1.find("V1"));
        Assert.assertEquals(0, idx1.find("v1"));
        Assert.assertEquals(1, idx1.find(" 1"));
        Assert.assertEquals(2, idx1.find("v "));

        ContextTableIndex idx2 = new ContextTableIndex("idx2",  table, Collections.singletonList("header3"));
        Assert.assertFalse(idx2.hasUniqueKeys());
        Assert.assertEquals(-1, idx2.find("?"));
        Assert.assertEquals(-1, idx2.find("3"));
        Assert.assertEquals(-1, idx2.find("v"));
        Assert.assertEquals(-1, idx2.find(" v"));
        Assert.assertEquals(-1, idx2.find("V2"));
        Assert.assertEquals(0, idx2.find("v3"));
        Assert.assertEquals(1, idx2.find(" 3"));

        ContextTableIndex idx3 = new ContextTableIndex("idx3",  table, Arrays.asList("header1", "header3"));
        Assert.assertTrue(idx3.hasUniqueKeys());
        Assert.assertEquals(-1, idx3.find("?"));
        Assert.assertEquals(-1, idx3.find("    "));
        Assert.assertEquals(-1, idx3.find("1 3 "));
        Assert.assertEquals(-1, idx3.find("v v "));
        Assert.assertEquals(0, idx3.find("v1v3"));
        Assert.assertEquals(1, idx3.find(" 1 3"));
        Assert.assertEquals(2, idx3.find("v v3"));

        ContextTableIndex idx4 = new ContextTableIndex("idx4",  table, Arrays.asList("header1", "header2", "header3"));
        Assert.assertTrue(idx4.hasUniqueKeys());
        Assert.assertEquals(-1, idx4.find("?"));
        Assert.assertEquals(-1, idx4.find("      "));
        Assert.assertEquals(-1, idx4.find("1 2 3 "));
        Assert.assertEquals(-1, idx4.find("v v v "));
        Assert.assertEquals(0, idx4.find("v1v2v3"));
        Assert.assertEquals(1, idx4.find(" 1 2 3"));
        Assert.assertEquals(2, idx4.find("v v v3"));
    }

    @Test
    public void testFindFloor() {
        List<List<String>> tableData = new ArrayList<>();
        tableData.add(Arrays.asList("header1", "header2", "header3"));
        tableData.add(Arrays.asList("b1", "b2", "b3"));
        tableData.add(Arrays.asList("d1", "d2", "b3"));
        tableData.add(Arrays.asList("f1", "f2", "f3"));

        ContextTable table = new ContextTable("table", tableData);

        ContextTableIndex idx1 = new ContextTableIndex("idx1",  table, Collections.singletonList("header1"));
        Assert.assertTrue(idx1.hasUniqueKeys());
        Assert.assertEquals(-1, idx1.findFloor("a1"));
        Assert.assertEquals(-1, idx1.findFloor("b0"));
        Assert.assertEquals(0, idx1.findFloor("b1"));
        Assert.assertEquals(0, idx1.findFloor("c1"));
        Assert.assertEquals(1, idx1.findFloor("d1"));
        Assert.assertEquals(1, idx1.findFloor("e1"));
        Assert.assertEquals(2, idx1.findFloor("f1"));
        Assert.assertEquals(2, idx1.findFloor("g1"));

        ContextTableIndex idx2 = new ContextTableIndex("idx2",  table, Collections.singletonList("header3"));
        Assert.assertFalse(idx2.hasUniqueKeys());
        Assert.assertEquals(-1, idx2.findFloor("a1"));
        Assert.assertEquals(-1, idx2.findFloor("b2"));
        Assert.assertEquals(0, idx2.findFloor("b3"));
        Assert.assertEquals(1, idx2.findFloor("c3"));
        Assert.assertEquals(1, idx2.findFloor("e3"));
        Assert.assertEquals(2, idx2.findFloor("f3"));
        Assert.assertEquals(2, idx2.findFloor("g3"));

        ContextTableIndex idx3 = new ContextTableIndex("idx3",  table, Arrays.asList("header1", "header3"));
        Assert.assertTrue(idx3.hasUniqueKeys());
        Assert.assertEquals(-1, idx3.findFloor("a1  "));
        Assert.assertEquals(-1, idx3.findFloor("b1b2"));
        Assert.assertEquals(0, idx3.findFloor("b1b3"));
        Assert.assertEquals(0, idx3.findFloor("c1b3"));
        Assert.assertEquals(1, idx3.findFloor("d1b3"));
        Assert.assertEquals(1, idx3.findFloor("e1e3"));
        Assert.assertEquals(2, idx3.findFloor("f1f3"));
        Assert.assertEquals(2, idx3.findFloor("g1g3"));
    }
}
