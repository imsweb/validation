/*
 * Copyright (C) 2011 Information Management Services, Inc.
 */
package com.imsweb.validation.internal.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created on Oct 4, 2011 by murphyr
 * @author murphyr
 */
public class JavaContextParserTest {

    /**
     * Some basic tests.
     * <p/>
     * Created on Oct 6, 2011 by murphyr
     * @throws Exception
     */
    @Test
    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public void testParseContext() throws Exception {
        Map<String, Object> currentContext = new HashMap<>();

        List<String> list1 = new ArrayList<>();
        list1.add("01");
        list1.add("44");
        list1.add("45");
        list1.add("46");
        list1.add("47");
        list1.add("48");
        list1.add("49");
        list1.add("50");
        list1.add("51");
        Assert.assertEquals(list1, JavaContextParser.parseContext("['01','44','45','46','47','48','49','50','51']", currentContext));
        currentContext.put("list1", list1);

        Map<String, Object> map = new HashMap<>();
        map.put("125", "990100");
        map.put("163", list1);
        map.put("265", Collections.singletonList(990200));
        Assert.assertEquals(map, JavaContextParser.parseContext("['125' : '990100', '163' : list1, '265' : [990200]]", currentContext));
        Assert.assertEquals(HashMap.class, JavaContextParser.parseContext("['125' : '990100', '163' : list1, '265' : [990200]]", currentContext).getClass());
        Assert.assertEquals(TreeMap.class, JavaContextParser.parseContext("['125' : '990100', '163' : list1, '265' : [990200]] as java.util.TreeMap", currentContext).getClass());

        Map<Object, Object> map2 = new HashMap<>();
        map2.put(125, 5);
        map2.put(5, 5);
        map2.put(6, 5);
        map2.put(100, 6);
        Assert.assertEquals(map2, JavaContextParser.parseContext("[[[125,5], 6] : 5, 100 : 6]", currentContext));

        List<Integer> list4 = new ArrayList<>();
        list4.add(33);
        list4.add(34);
        list4.add(35);
        Assert.assertEquals(list4, JavaContextParser.parseContext("33..35", currentContext));
        Assert.assertEquals(55, JavaContextParser.parseContext("55", currentContext));
        Assert.assertEquals("HEY", JavaContextParser.parseContext("'HEY'", currentContext));
        Assert.assertEquals("asdfsad ' agasd", JavaContextParser.parseContext("'asdfsad \\' agasd'", currentContext));
        currentContext.put("MY_ARRAY", Collections.singletonList(1));
        JavaContextParser.parseContext("[1 : Context.MY_ARRAY, 2 : Context.MY_ARRAY]", currentContext);
    }
}
