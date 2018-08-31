/*
 * Copyright (C) 2011 Information Management Services, Inc.
 */
package com.imsweb.validation.entities;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.imsweb.validation.TestingUtils;
import com.imsweb.validation.XmlValidatorFactory;

/**
 * Created on Aug 8, 2011 by depryf
 * @author depryf
 */
public class RuleTestTest {

    @Before
    public void setUp() {
        TestingUtils.init();
    }

    @Test
    public void testTesting() throws Exception {
        TestingUtils.loadValidator("fake-validator-naaccr-lines");

        ValidatorTests allTests = XmlValidatorFactory.loadTestsFromXml(Thread.currentThread().getContextClassLoader().getResource("fake-tests.xml"));
        for (RuleTest test : allTests.getTests().values()) {
            Map<Integer, List<RuleTestResult>> results = test.executeTest();
            if ("fvnl-rule1".equals(test.getTestedRuleId())) {
                Assert.assertEquals(4, results.size());

                List<RuleTestResult> list1 = results.get(3);
                Assert.assertEquals(1, list1.size());
                Assert.assertEquals(3, list1.get(0).getLineNumber().intValue());
                Assert.assertEquals(1, list1.get(0).getAssertionIndex().intValue());
                Assert.assertTrue(list1.get(0).isSuccess());
                Assert.assertFalse(list1.get(0).getOutput().isEmpty());

                List<RuleTestResult> list2 = results.get(5);
                Assert.assertEquals(1, list2.size());
                Assert.assertEquals(5, list2.get(0).getLineNumber().intValue());
                Assert.assertEquals(1, list2.get(0).getAssertionIndex().intValue());
                Assert.assertTrue(list2.get(0).isSuccess());
                Assert.assertFalse(list2.get(0).getOutput().isEmpty());

                List<RuleTestResult> list3 = results.get(10);
                Assert.assertEquals(3, list3.size());
                Assert.assertEquals(10, list3.get(0).getLineNumber().intValue());
                Assert.assertEquals(1, list3.get(0).getAssertionIndex().intValue());
                Assert.assertTrue(list3.get(0).isSuccess());
                Assert.assertEquals(10, list3.get(1).getLineNumber().intValue());
                Assert.assertEquals(2, list3.get(1).getAssertionIndex().intValue());
                Assert.assertTrue(list3.get(1).isSuccess());
                Assert.assertEquals(10, list3.get(2).getLineNumber().intValue());
                Assert.assertEquals(3, list3.get(2).getAssertionIndex().intValue());
                Assert.assertTrue(list3.get(2).isSuccess());
                Assert.assertFalse(list3.get(0).getOutput().isEmpty());

                List<RuleTestResult> list4 = results.get(14);
                Assert.assertEquals(1, list4.size());
                Assert.assertEquals(14, list4.get(0).getLineNumber().intValue());
                Assert.assertEquals(1, list4.get(0).getAssertionIndex().intValue());
                Assert.assertFalse(list4.get(0).isSuccess());
                Assert.assertFalse(list4.get(0).getOutput().isEmpty());
            }
            else if ("fvnl-rule2".equals(test.getTestedRuleId())) {
                Assert.assertEquals(5, results.size());

                // a regular assertion
                List<RuleTestResult> assertions = results.get(2);
                Assert.assertEquals(1, assertions.size());
                Assert.assertEquals(2, assertions.get(0).getLineNumber().intValue());
                Assert.assertEquals(1, assertions.get(0).getAssertionIndex().intValue());
                Assert.assertTrue(assertions.get(0).isSuccess());

                // another regular assertion, testing the line numbers and the number of assertion on the line (a single one)
                assertions = results.get(4);
                Assert.assertEquals(1, assertions.size());
                Assert.assertEquals(4, assertions.get(0).getLineNumber().intValue());
                Assert.assertEquals(1, assertions.get(0).getAssertionIndex().intValue());
                Assert.assertTrue(assertions.get(0).isSuccess());

                // test for loop, the assertion is in loop repeated 10 times
                assertions = results.get(8);
                Assert.assertEquals(10, assertions.size());
                for (int i = 0; i < assertions.size(); i++) {
                    Assert.assertEquals(8, assertions.get(i).getLineNumber().intValue());
                    Assert.assertEquals(i + 1, assertions.get(i).getAssertionIndex().intValue()); // assertion indexes are 1-based
                    Assert.assertTrue(assertions.get(i).isSuccess());
                }

                // test closure, the assertion is in loop repeated 10 times
                assertions = results.get(12);
                Assert.assertEquals(10, assertions.size());
                for (int i = 0; i < assertions.size(); i++) {
                    Assert.assertEquals(12, assertions.get(i).getLineNumber().intValue());
                    Assert.assertEquals(i + 1, assertions.get(i).getAssertionIndex().intValue()); // assertion indexes are 1-based
                    Assert.assertTrue(assertions.get(i).isSuccess());
                }

                // test while loop, the assertion is in loop repeated 10 times
                assertions = results.get(17);
                Assert.assertEquals(10, assertions.size());
                for (int i = 0; i < assertions.size(); i++) {
                    Assert.assertEquals(17, assertions.get(i).getLineNumber().intValue());
                    Assert.assertEquals(i + 1, assertions.get(i).getAssertionIndex().intValue()); // assertion indexes are 1-based
                    Assert.assertTrue(assertions.get(i).isSuccess());
                }
            }
            else if ("fvnl-rule3".equals(test.getTestedRuleId())) {
                Assert.assertEquals(2, results.size());
                Assert.assertTrue(results.get(3).get(0).isSuccess());
                Assert.assertTrue(results.get(5).get(0).isSuccess());
            }
            else
                Assert.fail("This test is not supported: " + test.getTestedRuleId());
        }

        TestingUtils.unloadValidator("fake-validator-naaccr-lines");
    }
}
