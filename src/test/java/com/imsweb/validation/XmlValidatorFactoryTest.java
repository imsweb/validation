/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.imsweb.validation.entities.Category;
import com.imsweb.validation.entities.Condition;
import com.imsweb.validation.entities.DeletedRuleHistory;
import com.imsweb.validation.entities.EditableValidator;
import com.imsweb.validation.entities.EmbeddedSet;
import com.imsweb.validation.entities.Rule;
import com.imsweb.validation.entities.RuleHistory;
import com.imsweb.validation.entities.RuleTest;
import com.imsweb.validation.entities.StandaloneSet;
import com.imsweb.validation.entities.Validator;
import com.imsweb.validation.entities.ValidatorTests;

/**
 * Created on Feb 23, 2011 by depryf
 * @author depryf
 */
public class XmlValidatorFactoryTest {

    @BeforeClass
    public static void setUp() {
        TestingUtils.init();
    }

    @Test
    public void testValidatorLoadMethods() throws IOException {
        File file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/fake-validator.xml");
        if (!file.exists())
            Assert.fail("This test requires the file 'fake-validator.xml'");

        // read using a URL
        Validator v = XmlValidatorFactory.loadValidatorFromXml(Thread.currentThread().getContextClassLoader().getResource("fake-validator.xml"));
        assertFakeValidator(v);

        // read using a file
        v = XmlValidatorFactory.loadValidatorFromXml(file);
        assertFakeValidator(v);

        // read from input stream
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("fake-validator.xml")) {
            v = XmlValidatorFactory.loadValidatorFromXml(is);
            assertFakeValidator(v);
        }

        // read from reader
        try (Reader reader = new FileReader(file)) {
            v = XmlValidatorFactory.loadValidatorFromXml(reader);
            assertFakeValidator(v);
        }

        // read gzipped file using multi-threading parsing
        file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/fake-validator.xml.gz");
        if (!file.exists())
            Assert.fail("This test requires the file 'fake-validator.xml.gz'");
        XmlValidatorFactory.enableMultiThreadedParsing(2);
        try {
            v = XmlValidatorFactory.loadValidatorFromXml(file);
            assertFakeValidator(v);
        }
        finally {
            XmlValidatorFactory.enableMultiThreadedParsing(1);
        }
    }

    @Test
    public void testValidatorWriteMethods() throws IOException {
        File file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/fake-validator.xml");
        if (!file.exists())
            Assert.fail("This test requires the file 'fake-validator.xml'");
        Validator v = XmlValidatorFactory.loadValidatorFromXml(file);

        File targetFile = new File(TestingUtils.TMP_DIR, "xml-validator-test.xml");

        // write using a file
        XmlValidatorFactory.writeValidatorToXml(v, targetFile);
        assertFakeValidator(targetFile);

        // write using an output stream
        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
            XmlValidatorFactory.writeValidatorToXml(v, fos);
        }
        assertFakeValidator(targetFile);

        // write using a writer
        try (FileWriter writer = new FileWriter(targetFile)) {
            XmlValidatorFactory.writeValidatorToXml(v, writer);
        }
        assertFakeValidator(targetFile);

        // write gzipped file using multi-threading parsing
        targetFile = new File(TestingUtils.TMP_DIR, "xml-validator-test.xml.gz");
        XmlValidatorFactory.enableMultiThreadedParsing(2);
        try {
            XmlValidatorFactory.writeValidatorToXml(v, targetFile);
            assertFakeValidator(targetFile);
        }
        finally {
            XmlValidatorFactory.enableMultiThreadedParsing(1);
        }
    }

    @Test(expected = IOException.class)
    public void testValidatorErrorNoId() throws IOException {
        XmlValidatorFactory.loadValidatorFromXml(Thread.currentThread().getContextClassLoader().getResource("fake-validator-no-id.xml"));
    }

    @Test(expected = IOException.class)
    public void testValidatorErrorTwoDescriptions() throws IOException {
        XmlValidatorFactory.loadValidatorFromXml(Thread.currentThread().getContextClassLoader().getResource("fake-validator-bad-xml.xml"));
    }

    @Test(expected = IOException.class)
    public void testValidatorErrorBadExpression() throws IOException {
        XmlValidatorFactory.enableMultiThreadedParsing(2);
        try {
            XmlValidatorFactory.loadValidatorFromXml(Thread.currentThread().getContextClassLoader().getResource("fake-validator-bad-groovy.xml"));
        }
        finally {
            XmlValidatorFactory.enableMultiThreadedParsing(1);
        }
    }

    @Test(expected = IOException.class)
    public void testValidatorErrorReleaseNoDate() throws IOException {
        XmlValidatorFactory.loadValidatorFromXml(Thread.currentThread().getContextClassLoader().getResource("fake-validator-release-no-date.xml"));
    }

    @Test(expected = IOException.class)
    public void testValidatorErrorLoadNullFile() throws IOException {
        XmlValidatorFactory.loadValidatorFromXml((File)null);
    }

    @Test(expected = IOException.class)
    public void testValidatorErrorLoadNullUrl() throws IOException {
        XmlValidatorFactory.loadValidatorFromXml((URL)null);
    }

    @Test(expected = IOException.class)
    public void testValidatorErrorLoadNullInputStream() throws IOException {
        XmlValidatorFactory.loadValidatorFromXml((InputStream)null);
    }

    @Test(expected = IOException.class)
    public void testValidatorErrorLoadNullReader() throws IOException {
        XmlValidatorFactory.loadValidatorFromXml((Reader)null);
    }

    @Test(expected = IOException.class)
    public void testValidatorErrorWriteNullFile() throws IOException {
        XmlValidatorFactory.writeValidatorToXml(new Validator(), (File)null);
    }

    @Test(expected = IOException.class)
    public void testValidatorErrorWriteNullValidatorForFile() throws IOException {
        XmlValidatorFactory.writeValidatorToXml(null, new File(TestingUtils.TMP_DIR, "whatever.xml"));
    }

    @Test(expected = IOException.class)
    public void testValidatorErrorWriteNullOutputStream() throws IOException {
        XmlValidatorFactory.writeValidatorToXml(new Validator(), (OutputStream)null);
    }

    @Test(expected = IOException.class)
    public void testValidatorErrorWriteNullValidatorForOutputStream() throws IOException {
        XmlValidatorFactory.writeValidatorToXml(null, new FileOutputStream(new File(TestingUtils.TMP_DIR, "whatever.xml")));
    }

    @Test(expected = IOException.class)
    public void testValidatorErrorWriteNullWriter() throws IOException {
        XmlValidatorFactory.writeValidatorToXml(new Validator(), (Writer)null);
    }

    @Test(expected = IOException.class)
    public void testValidatorErrorWriteNullValidatorForWriter() throws IOException {
        XmlValidatorFactory.writeValidatorToXml(null, new FileWriter(new File(TestingUtils.TMP_DIR, "whatever.xml")));
    }

    @Test
    public void testValidatorEmptyData() throws IOException, ConstructionException {
        Validator v = XmlValidatorFactory.loadValidatorFromXml(Thread.currentThread().getContextClassLoader().getResource("fake-validator-empty.xml"));

        ValidationEngine.addValidator(new EditableValidator(v));
        ValidationEngine.deleteValidator(new EditableValidator(v));

        File targetFile = new File(TestingUtils.TMP_DIR, "xml-validator-test.xml");
        targetFile.deleteOnExit();
        XmlValidatorFactory.writeValidatorToXml(v, targetFile);
    }

    // helper
    private void assertFakeValidator(File file) throws IOException {
        Validator v = XmlValidatorFactory.loadValidatorFromXml(file);
        assertFakeValidator(v);
        if (!file.delete())
            Assert.fail("Unable to delete temp file");
    }

    // helper
    private void assertFakeValidator(Validator v) {
        Assert.assertEquals("fake-validator", v.getId());
        Assert.assertEquals("Fake Validator", v.getName());
        Assert.assertEquals("TEST-002-01", v.getVersion());
        Assert.assertEquals("4.0", v.getMinEngineVersion());
        Assert.assertEquals("test", v.getTranslatedFrom());

        Assert.assertEquals(3, v.getReleases().size());
        Assert.assertEquals("TEST-001-01", v.getReleases().first().getVersion().getRawString());
        Assert.assertEquals("TEST-002-01", v.getReleases().last().getVersion().getRawString());

        Assert.assertEquals(1, v.getDeletedRuleHistories().size());
        DeletedRuleHistory drh1 = v.getDeletedRuleHistories().iterator().next();
        Assert.assertEquals("OLD-ID", drh1.getDeletedRuleId());
        Assert.assertEquals("OLD-NAME", drh1.getDeletedRuleName());
        Assert.assertEquals("TEST-001-02", drh1.getVersion().getRawString());
        Assert.assertEquals("test", drh1.getUsername());
        Calendar c = Calendar.getInstance();
        c.setTime(drh1.getDate());
        Assert.assertEquals(2000, c.get(Calendar.YEAR));
        Assert.assertEquals(0, c.get(Calendar.MONTH));
        Assert.assertEquals(15, c.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals("666", drh1.getReference());
        Assert.assertEquals("Edit was deleted.", drh1.getMessage());

        Assert.assertEquals(1, v.getCategories().size());
        Assert.assertNotNull(v.getCategory("fv-category"));
        Assert.assertNull(v.getCategory("hum?"));

        Category cat = v.getCategory("fv-category");
        Assert.assertEquals("Category", cat.getName());
        Assert.assertEquals("description for the category", cat.getDescription());

        Assert.assertEquals(1, v.getConditions().size());
        Assert.assertNotNull(v.getCondition("fv-condition"));
        Assert.assertNull(v.getCondition("hum?"));

        Condition cond = v.getCondition("fv-condition");
        Assert.assertEquals("Condition", cond.getName());
        Assert.assertEquals("level1.level2", cond.getJavaPath());
        Assert.assertEquals("return level2.prop2 != 'IGNORED'", cond.getExpression().trim());
        Assert.assertEquals("description for the condition", cond.getDescription());

        Rule r1 = v.getRule("fv-rule1");
        Assert.assertEquals("fv-rule1", r1.getId());
        Assert.assertEquals("Rule 1", r1.getName());
        Assert.assertEquals("tag1", r1.getTag());
        Assert.assertTrue(r1.getDependencies().isEmpty());
        Assert.assertEquals(1, r1.getSeverity().intValue());
        Assert.assertEquals("test", r1.getAgency());
        Assert.assertEquals("return level1.prop != Context.FV_CONTEXT1", r1.getExpression().trim());
        Assert.assertEquals("message1", r1.getMessage());
        Assert.assertEquals("description1", r1.getDescription());
        Assert.assertEquals(1, r1.getHistories().size());
        RuleHistory hist = r1.getHistories().iterator().next();
        Assert.assertEquals("TEST-001-02", hist.getVersion().getRawString());
        Assert.assertEquals("test", hist.getUsername());
        c = Calendar.getInstance();
        c.setTime(hist.getDate());
        Assert.assertEquals(2001, c.get(Calendar.YEAR));
        Assert.assertEquals(0, c.get(Calendar.MONTH));
        Assert.assertEquals(15, c.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals("999999", hist.getReference());
        Assert.assertEquals(1, r1.getRawProperties().size());

        Rule r2 = v.getRule("fv-rule2");
        Assert.assertEquals("fv-rule2", r2.getId());

        Assert.assertEquals(1, v.getRawContext().size());

        Assert.assertEquals(2, v.getSets().size());
        Assert.assertNotNull(v.getSet("fv-set1"));
        Assert.assertNotNull(v.getSet("fv-set2"));
        Assert.assertNull(v.getSet("hum?"));
        EmbeddedSet s1 = v.getSet("fv-set1");
        Assert.assertEquals("fv-set1", s1.getId());
        Assert.assertEquals("Set 1", s1.getName());
        Assert.assertEquals("tag1", s1.getTag());
        Assert.assertEquals("description", s1.getDescription());
        Assert.assertFalse(s1.needToInclude("fv-rule1"));
        Assert.assertTrue(s1.needToInclude("fv-rule2"));
        Assert.assertFalse(s1.needToInclude("fv-rule3"));
        EmbeddedSet s2 = v.getSet("fv-set2");
        Assert.assertEquals("fv-set2", s2.getId());
        Assert.assertTrue(s2.needToInclude("fv-rule1"));
        Assert.assertFalse(s2.needToInclude("fv-rule2"));
        Assert.assertTrue(s2.needToInclude("fv-rule3"));
    }

    @Test
    public void testStandaloneSetLoadMethods() throws IOException {
        File file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/fake-set.xml");
        if (!file.exists())
            Assert.fail("This test requires the file 'fake-set.xml'");

        // read using a URL
        StandaloneSet s = XmlValidatorFactory.loadStandaloneSetFromXml(Thread.currentThread().getContextClassLoader().getResource("fake-set.xml"));
        assertFakeSet(s);

        // read using a file
        s = XmlValidatorFactory.loadStandaloneSetFromXml(file);
        assertFakeSet(s);

        // read from input stream
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("fake-set.xml")) {
            s = XmlValidatorFactory.loadStandaloneSetFromXml(is);
            assertFakeSet(s);
        }

        // read from reader
        try (Reader reader = new FileReader(file)) {
            s = XmlValidatorFactory.loadStandaloneSetFromXml(reader);
            assertFakeSet(s);
        }

        // read gzipped file
        file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/fake-set.xml.gz");
        if (!file.exists())
            Assert.fail("This test requires the file 'fake-set.xml.gz'");
        s = XmlValidatorFactory.loadStandaloneSetFromXml(file);
        assertFakeSet(s);
    }

    @Test
    public void testStandaloneSetWriteMethods() throws IOException {
        File file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/fake-set.xml");
        if (!file.exists())
            Assert.fail("This test requires the file 'fake-set.xml'");
        StandaloneSet s = XmlValidatorFactory.loadStandaloneSetFromXml(file);

        File targetFile = new File(TestingUtils.TMP_DIR, "xml-set-test.xml");

        // write using a file
        XmlValidatorFactory.writeStandaloneSetToXml(s, targetFile);
        assertFakeSet(targetFile);

        // write using an output stream
        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
            XmlValidatorFactory.writeStandaloneSetToXml(s, fos);
        }
        assertFakeSet(targetFile);

        // write using a writer
        try (FileWriter writer = new FileWriter(targetFile)) {
            XmlValidatorFactory.writeStandaloneSetToXml(s, writer);
        }
        assertFakeSet(targetFile);

        // write gzipped file using multi-threading parsing
        targetFile = new File(TestingUtils.TMP_DIR, "xml-set-test.xml.gz");
        XmlValidatorFactory.writeStandaloneSetToXml(s, targetFile);
        assertFakeSet(targetFile);
    }

    @Test(expected = IOException.class)
    public void testStandaloneSetErrorNoId() throws IOException {
        XmlValidatorFactory.loadStandaloneSetFromXml(Thread.currentThread().getContextClassLoader().getResource("fake-set-no-id.xml"));
    }

    @Test(expected = IOException.class)
    public void testStandaloneSetErrorLoadNullFile() throws IOException {
        XmlValidatorFactory.loadStandaloneSetFromXml((File)null);
    }

    @Test(expected = IOException.class)
    public void testStandaloneSetErrorLoadNullUrl() throws IOException {
        XmlValidatorFactory.loadStandaloneSetFromXml((URL)null);
    }

    @Test(expected = IOException.class)
    public void testStandaloneSetErrorLoadNullInputFile() throws IOException {
        XmlValidatorFactory.loadStandaloneSetFromXml((InputStream)null);
    }

    @Test(expected = IOException.class)
    public void testStandaloneSetErrorLoadNullReader() throws IOException {
        XmlValidatorFactory.loadStandaloneSetFromXml((Reader)null);
    }

    @Test(expected = IOException.class)
    public void testStandaloneSetErrorWriteNullFile() throws IOException {
        XmlValidatorFactory.writeStandaloneSetToXml(new StandaloneSet(), (File)null);
    }

    @Test(expected = IOException.class)
    public void testStandaloneSetErrorWriteNullSetForFile() throws IOException {
        XmlValidatorFactory.writeStandaloneSetToXml(null, new File(TestingUtils.TMP_DIR, "whatever.xml"));
    }

    @Test(expected = IOException.class)
    public void testStandaloneSetErrorWriteNullOutputStream() throws IOException {
        XmlValidatorFactory.writeStandaloneSetToXml(new StandaloneSet(), (OutputStream)null);
    }

    @Test(expected = IOException.class)
    public void testStandaloneSetErrorWriteNullSetForOutputStream() throws IOException {
        XmlValidatorFactory.writeStandaloneSetToXml(null, new FileOutputStream(new File(TestingUtils.TMP_DIR, "whatever.xml")));
    }

    @Test(expected = IOException.class)
    public void testStandaloneSetErrorWriteNullWriter() throws IOException {
        XmlValidatorFactory.writeStandaloneSetToXml(new StandaloneSet(), (Writer)null);
    }

    @Test(expected = IOException.class)
    public void testStandaloneSetErrorWriteNullSetForWriter() throws IOException {
        XmlValidatorFactory.writeValidatorToXml(null, new FileWriter(new File(TestingUtils.TMP_DIR, "whatever.xml")));
    }

    // helper
    private void assertFakeSet(File file) throws IOException {
        StandaloneSet s = XmlValidatorFactory.loadStandaloneSetFromXml(file);
        assertFakeSet(s);
        if (!file.delete())
            Assert.fail("Unable to delete temp file");
    }

    // helper
    private void assertFakeSet(StandaloneSet set) {
        Assert.assertEquals("test-set", set.getId());
        Assert.assertEquals("Test Set", set.getName());
        Assert.assertEquals("Test Desc", set.getDescription());
        Assert.assertEquals(1, set.getReferencedValidatorIds().size());
        Assert.assertTrue(set.getReferencedValidatorIds().contains("id"));

        Assert.assertEquals(1, set.getInclusions().size());
        List<String> inclusions = set.getInclusions().get("id");
        Assert.assertEquals(3, inclusions.size());
        Assert.assertTrue(inclusions.contains("Edit1"));
        Assert.assertTrue(inclusions.contains("Edit2"));
        Assert.assertTrue(inclusions.contains("Edit3"));

        Assert.assertEquals(1, set.getExclusions().size());
        List<String> exclusions = set.getExclusions().get("id");
        Assert.assertEquals(1, exclusions.size());
        Assert.assertTrue(exclusions.contains("Edit2"));
    }

    @Test
    public void testTestsLoadMethods() throws IOException {
        File file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/fake-tests.xml");
        if (!file.exists())
            Assert.fail("This test requires the file 'fake-tests.xml'");

        // read using a URL
        ValidatorTests t = XmlValidatorFactory.loadTestsFromXml(Thread.currentThread().getContextClassLoader().getResource("fake-tests.xml"));
        assertFakeTests(t);

        // read using a file
        t = XmlValidatorFactory.loadTestsFromXml(file);
        assertFakeTests(t);

        // read from input stream
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("fake-tests.xml")) {
            t = XmlValidatorFactory.loadTestsFromXml(is);
            assertFakeTests(t);
        }

        // read from reader
        try (Reader reader = new FileReader(file)) {
            t = XmlValidatorFactory.loadTestsFromXml(reader);
            assertFakeTests(t);
        }

        // read gzipped file
        file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/fake-tests.xml.gz");
        if (!file.exists())
            Assert.fail("This test requires the file 'fake-tests.xml.gz'");
        t = XmlValidatorFactory.loadTestsFromXml(file);
        assertFakeTests(t);
    }

    @Test
    public void testTestsWriteMethods() throws IOException {
        File file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/fake-tests.xml");
        if (!file.exists())
            Assert.fail("This test requires the file 'fake-tests.xml'");
        ValidatorTests s = XmlValidatorFactory.loadTestsFromXml(file);

        File targetFile = new File(TestingUtils.TMP_DIR, "xml-set-test.xml");

        // write using a file
        XmlValidatorFactory.writeTestsToXml(s, targetFile);
        assertFakeTests(targetFile);

        // write using an output stream
        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
            XmlValidatorFactory.writeTestsToXml(s, fos);
        }
        assertFakeTests(targetFile);

        // write using a writer
        try (FileWriter writer = new FileWriter(targetFile)) {
            XmlValidatorFactory.writeTestsToXml(s, writer);
        }
        assertFakeTests(targetFile);

        // write gzipped file using multi-threading parsing
        targetFile = new File(TestingUtils.TMP_DIR, "xml-tests-test.xml.gz");
        XmlValidatorFactory.writeTestsToXml(s, targetFile);
        assertFakeTests(targetFile);
    }

    @Test(expected = IOException.class)
    public void testTestsErrorBadScript() throws IOException {
        XmlValidatorFactory.loadStandaloneSetFromXml(Thread.currentThread().getContextClassLoader().getResource("fake-tests-exception.xml"));
    }

    @Test(expected = IOException.class)
    public void testTestsErrorLoadNullFile() throws IOException {
        XmlValidatorFactory.loadTestsFromXml((File)null);
    }

    @Test(expected = IOException.class)
    public void testTestsErrorLoadNullUrl() throws IOException {
        XmlValidatorFactory.loadTestsFromXml((URL)null);
    }

    @Test(expected = IOException.class)
    public void testTestErrorLoadNullInputStream() throws IOException {
        XmlValidatorFactory.loadTestsFromXml((InputStream)null);
    }

    @Test(expected = IOException.class)
    public void testTestsErrorLoadNullReader() throws IOException {
        XmlValidatorFactory.loadTestsFromXml((Reader)null);
    }

    @Test(expected = IOException.class)
    public void testTestsErrorWriteNullFile() throws IOException {
        XmlValidatorFactory.writeTestsToXml(new ValidatorTests(), (File)null);
    }

    @Test(expected = IOException.class)
    public void testTestsErrorWriteNullSetForFile() throws IOException {
        XmlValidatorFactory.writeTestsToXml(null, new File(TestingUtils.TMP_DIR, "whatever.xml"));
    }

    @Test(expected = IOException.class)
    public void testTestsErrorWriteNullOutputStream() throws IOException {
        XmlValidatorFactory.writeTestsToXml(new ValidatorTests(), (OutputStream)null);
    }

    @Test(expected = IOException.class)
    public void testTestsErrorWriteNullSetForOutputStream() throws IOException {
        XmlValidatorFactory.writeTestsToXml(null, new FileOutputStream(new File(TestingUtils.TMP_DIR, "whatever.xml")));
    }

    @Test(expected = IOException.class)
    public void testTestsErrorWriteNullWriter() throws IOException {
        XmlValidatorFactory.writeTestsToXml(new ValidatorTests(), (Writer)null);
    }

    @Test(expected = IOException.class)
    public void testTestsErrorWriteNullSetForWriter() throws IOException {
        XmlValidatorFactory.writeTestsToXml(null, new FileWriter(new File(TestingUtils.TMP_DIR, "whatever.xml")));
    }

    // helper
    private void assertFakeTests(File file) throws IOException {
        ValidatorTests t = XmlValidatorFactory.loadTestsFromXml(file);
        assertFakeTests(t);
        if (!file.delete())
            Assert.fail("Unable to delete temp file");
    }

    // helper
    private void assertFakeTests(ValidatorTests tests) {
        Assert.assertEquals("fake-validator-naaccr-lines", tests.getTestedValidatorId());
        Assert.assertEquals(3, tests.getTests().size());

        RuleTest test1 = tests.getTests().get("fvnl-rule1");
        Assert.assertEquals("fvnl-rule1", test1.getTestedRuleId());
        Assert.assertEquals(4, test1.getAssertions().size());
        Map<Integer, Boolean> assertions1 = new HashMap<>();
        assertions1.put(3, Boolean.TRUE);
        assertions1.put(5, Boolean.FALSE);
        assertions1.put(10, Boolean.TRUE);
        assertions1.put(14, Boolean.TRUE);
        Assert.assertEquals(assertions1, test1.getAssertions());

        RuleTest test2 = tests.getTests().get("fvnl-rule2");
        Assert.assertEquals("fvnl-rule2", test2.getTestedRuleId());
        Assert.assertEquals(5, test2.getAssertions().size());
    }

    @Test
    public void testTargetValidatorXmlExists() {
        Assert.assertFalse(XmlValidatorFactory.targetValidatorXmlExists(null));
        Assert.assertTrue(XmlValidatorFactory.targetValidatorXmlExists(Thread.currentThread().getContextClassLoader().getResource("fake-validator.xml")));
    }

    @Test
    public void testGetXmlValidatorHash() {
        Assert.assertNull(XmlValidatorFactory.getXmlValidatorHash(null));

        URL url = Thread.currentThread().getContextClassLoader().getResource("fake-validator.xml");
        Assert.assertNotNull(XmlValidatorFactory.getXmlValidatorHash(url));

        URL url2 = Thread.currentThread().getContextClassLoader().getResource("fake-validator.xml.gz");
        Assert.assertNotNull(XmlValidatorFactory.getXmlValidatorHash(url2));

        // doesn't matter if the URL is gzipped, the hash should be the same...
        if (SystemUtils.IS_OS_WINDOWS) // this doesn't work on linux, not sure why...
            Assert.assertEquals(XmlValidatorFactory.getXmlValidatorHash(url), XmlValidatorFactory.getXmlValidatorHash(url2));
    }

    @Test
    public void testGetXmlValidatorRootAttributes() {
        Map<String, String> expected = new HashMap<>();
        expected.put(XmlValidatorFactory.ROOT_ATTR_ID, "fake-validator");
        expected.put(XmlValidatorFactory.ROOT_ATTR_NAME, "Fake Validator");
        expected.put(XmlValidatorFactory.ROOT_ATTR_VERSION, "TEST-002-01");
        expected.put(XmlValidatorFactory.ROOT_ATTR_MIN_ENGINE_VERSION, "4.0");
        expected.put(XmlValidatorFactory.ROOT_ATTR_TRANSLATED_FROM, "test");
        Assert.assertEquals(expected, XmlValidatorFactory.getXmlValidatorRootAttributes(Thread.currentThread().getContextClassLoader().getResource("fake-validator.xml")));
        Assert.assertEquals(expected, XmlValidatorFactory.getXmlValidatorRootAttributes(Thread.currentThread().getContextClassLoader().getResource("fake-validator.xml.gz")));
        Assert.assertEquals(expected, XmlValidatorFactory.getXmlValidatorRootAttributes(Thread.currentThread().getContextClassLoader().getResource("fake-validator-multi-line.xml")));

        expected.clear();
        expected.put(XmlValidatorFactory.ROOT_ATTR_NAME, "Fake Validator No ID");
        Assert.assertEquals(expected, XmlValidatorFactory.getXmlValidatorRootAttributes(Thread.currentThread().getContextClassLoader().getResource("fake-validator-no-id.xml")));

        expected.clear();
        expected.put(XmlValidatorFactory.ROOT_ATTR_ID, "fake-validator-large-prefix");
        expected.put(XmlValidatorFactory.ROOT_ATTR_NAME, "Fake Validator Large Prefix");
        Assert.assertEquals(expected, XmlValidatorFactory.getXmlValidatorRootAttributes(Thread.currentThread().getContextClassLoader().getResource("fake-validator-large-prefix-text.xml")));

        expected.clear();
        Assert.assertEquals(expected, XmlValidatorFactory.getXmlValidatorRootAttributes(Thread.currentThread().getContextClassLoader().getResource("property-parsing-test.txt")));
    }

    @Test
    public void testGetXmlValidatorId() {
        Assert.assertNull(XmlValidatorFactory.getXmlValidatorId(null));
        Assert.assertNull(XmlValidatorFactory.getXmlValidatorId(Thread.currentThread().getContextClassLoader().getResource("fake-validator-no-id.xml")));

        URL url = Thread.currentThread().getContextClassLoader().getResource("fake-validator.xml");
        Assert.assertEquals("fake-validator", XmlValidatorFactory.getXmlValidatorId(url));

        url = Thread.currentThread().getContextClassLoader().getResource("fake-validator.xml.gz");
        Assert.assertEquals("fake-validator", XmlValidatorFactory.getXmlValidatorId(url));
    }

    @Test
    public void testGetXmlValidatorName() {
        Assert.assertNull(XmlValidatorFactory.getXmlValidatorName(null));

        URL url = Thread.currentThread().getContextClassLoader().getResource("fake-validator.xml");
        Assert.assertEquals("Fake Validator", XmlValidatorFactory.getXmlValidatorName(url));

        url = Thread.currentThread().getContextClassLoader().getResource("fake-validator.xml.gz");
        Assert.assertEquals("Fake Validator", XmlValidatorFactory.getXmlValidatorName(url));
    }

    @Test
    public void testGetXmlValidatorVersion() {
        Assert.assertNull(XmlValidatorFactory.getXmlValidatorVersion(null));

        URL url = Thread.currentThread().getContextClassLoader().getResource("fake-validator.xml");
        Assert.assertEquals("TEST-002-01", XmlValidatorFactory.getXmlValidatorVersion(url));

        url = Thread.currentThread().getContextClassLoader().getResource("fake-validator.xml.gz");
        Assert.assertEquals("TEST-002-01", XmlValidatorFactory.getXmlValidatorVersion(url));
    }

    @Test
    public void testTrimEmptyLines() {
        Assert.assertNull(XmlValidatorFactory.trimEmptyLines(null, true));

        String s = " some text  ";
        String exp = "some text";
        Assert.assertEquals(exp, XmlValidatorFactory.trimEmptyLines(s, true));

        s = "\n     \n     \n\n   abc\n     ed\n\n   fh\n \n    \n     \n";
        exp = "abc\n     ed\n\n   fh";
        Assert.assertEquals(exp, XmlValidatorFactory.trimEmptyLines(s, true));

    }

    @Test
    public void testReAlign() {
        Assert.assertNull(XmlValidatorFactory.reAlign(null));

        String s = "some text  ";
        String exp = "some text";
        Assert.assertEquals(exp, XmlValidatorFactory.reAlign(s));

        s = "   abc \n     - abc\n     - def\n       -- gh\n     -ifk\n   lm";
        exp = "abc \n  - abc\n  - def\n    -- gh\n  -ifk\nlm";
        Assert.assertEquals(exp, XmlValidatorFactory.reAlign(s));

        s = "   abc \n     - abc\n     - def\n -- gh\n     -ifk\n   lm";
        exp = "abc \n    - abc\n    - def\n-- gh\n    -ifk\n  lm";
        Assert.assertEquals(exp, XmlValidatorFactory.reAlign(s));
    }
}
