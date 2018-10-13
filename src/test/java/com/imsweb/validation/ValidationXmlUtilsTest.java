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
public class ValidationXmlUtilsTest {

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
        Validator v = ValidationXmlUtils.loadValidatorFromXml(Thread.currentThread().getContextClassLoader().getResource("fake-validator.xml"));
        assertFakeValidator(v);

        // read using a file
        v = ValidationXmlUtils.loadValidatorFromXml(file);
        assertFakeValidator(v);

        // read from input stream
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("fake-validator.xml")) {
            v = ValidationXmlUtils.loadValidatorFromXml(is);
            assertFakeValidator(v);
        }

        // read from reader
        try (Reader reader = new FileReader(file)) {
            v = ValidationXmlUtils.loadValidatorFromXml(reader);
            assertFakeValidator(v);
        }

        // read gzipped file
        file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/fake-validator.xml.gz");
        if (!file.exists())
            Assert.fail("This test requires the file 'fake-validator.xml.gz'");
        v = ValidationXmlUtils.loadValidatorFromXml(file);
        assertFakeValidator(v);
    }

    @Test
    public void testValidatorWriteMethods() throws IOException {
        File file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/fake-validator.xml");
        if (!file.exists())
            Assert.fail("This test requires the file 'fake-validator.xml'");
        Validator v = ValidationXmlUtils.loadValidatorFromXml(file);

        File targetFile = new File(TestingUtils.TMP_DIR, "xml-validator-test.xml");

        // write using a file
        ValidationXmlUtils.writeValidatorToXml(v, targetFile);
        assertFakeValidator(targetFile);

        // write using an output stream
        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
            ValidationXmlUtils.writeValidatorToXml(v, fos);
        }
        assertFakeValidator(targetFile);

        // write using a writer
        try (FileWriter writer = new FileWriter(targetFile)) {
            ValidationXmlUtils.writeValidatorToXml(v, writer);
        }
        assertFakeValidator(targetFile);

        // write gzipped file
        targetFile = new File(TestingUtils.TMP_DIR, "xml-validator-test.xml.gz");
        ValidationXmlUtils.writeValidatorToXml(v, targetFile);
        assertFakeValidator(targetFile);
    }

    @Test(expected = IOException.class)
    public void testValidatorErrorNoId() throws IOException {
        ValidationXmlUtils.loadValidatorFromXml(Thread.currentThread().getContextClassLoader().getResource("fake-validator-no-id.xml"));
    }

    @Test(expected = IOException.class)
    public void testValidatorErrorTwoDescriptions() throws IOException {
        ValidationXmlUtils.loadValidatorFromXml(Thread.currentThread().getContextClassLoader().getResource("fake-validator-bad-xml.xml"));
    }

    @Test(expected = IOException.class)
    public void testValidatorErrorBadExpression() throws IOException {
        ValidationXmlUtils.loadValidatorFromXml(Thread.currentThread().getContextClassLoader().getResource("fake-validator-bad-groovy.xml"));
    }

    @Test(expected = IOException.class)
    public void testValidatorErrorReleaseNoDate() throws IOException {
        ValidationXmlUtils.loadValidatorFromXml(Thread.currentThread().getContextClassLoader().getResource("fake-validator-release-no-date.xml"));
    }

    @Test(expected = IOException.class)
    public void testValidatorErrorLoadNullFile() throws IOException {
        ValidationXmlUtils.loadValidatorFromXml((File)null);
    }

    @Test(expected = IOException.class)
    public void testValidatorErrorLoadNullUrl() throws IOException {
        ValidationXmlUtils.loadValidatorFromXml((URL)null);
    }

    @Test(expected = IOException.class)
    public void testValidatorErrorLoadNullInputStream() throws IOException {
        ValidationXmlUtils.loadValidatorFromXml((InputStream)null);
    }

    @Test(expected = IOException.class)
    public void testValidatorErrorLoadNullReader() throws IOException {
        ValidationXmlUtils.loadValidatorFromXml((Reader)null);
    }

    @Test(expected = IOException.class)
    public void testValidatorErrorWriteNullFile() throws IOException {
        ValidationXmlUtils.writeValidatorToXml(new Validator(), (File)null);
    }

    @Test(expected = IOException.class)
    public void testValidatorErrorWriteNullValidatorForFile() throws IOException {
        ValidationXmlUtils.writeValidatorToXml(null, new File(TestingUtils.TMP_DIR, "whatever.xml"));
    }

    @Test(expected = IOException.class)
    public void testValidatorErrorWriteNullOutputStream() throws IOException {
        ValidationXmlUtils.writeValidatorToXml(new Validator(), (OutputStream)null);
    }

    @Test(expected = IOException.class)
    public void testValidatorErrorWriteNullValidatorForOutputStream() throws IOException {
        ValidationXmlUtils.writeValidatorToXml(null, new FileOutputStream(new File(TestingUtils.TMP_DIR, "whatever.xml")));
    }

    @Test(expected = IOException.class)
    public void testValidatorErrorWriteNullWriter() throws IOException {
        ValidationXmlUtils.writeValidatorToXml(new Validator(), (Writer)null);
    }

    @Test(expected = IOException.class)
    public void testValidatorErrorWriteNullValidatorForWriter() throws IOException {
        ValidationXmlUtils.writeValidatorToXml(null, new FileWriter(new File(TestingUtils.TMP_DIR, "whatever.xml")));
    }

    @Test
    public void testValidatorEmptyData() throws IOException, ConstructionException {
        Validator v = ValidationXmlUtils.loadValidatorFromXml(Thread.currentThread().getContextClassLoader().getResource("fake-validator-empty.xml"));

        ValidationEngine.getInstance().addValidator(new EditableValidator(v));
        ValidationEngine.getInstance().deleteValidator(new EditableValidator(v));

        File targetFile = new File(TestingUtils.TMP_DIR, "xml-validator-test.xml");
        targetFile.deleteOnExit();
        ValidationXmlUtils.writeValidatorToXml(v, targetFile);
    }

    // helper
    private void assertFakeValidator(File file) throws IOException {
        Validator v = ValidationXmlUtils.loadValidatorFromXml(file);
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
        StandaloneSet s = ValidationXmlUtils.loadStandaloneSetFromXml(Thread.currentThread().getContextClassLoader().getResource("fake-set.xml"));
        assertFakeSet(s);

        // read using a file
        s = ValidationXmlUtils.loadStandaloneSetFromXml(file);
        assertFakeSet(s);

        // read from input stream
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("fake-set.xml")) {
            s = ValidationXmlUtils.loadStandaloneSetFromXml(is);
            assertFakeSet(s);
        }

        // read from reader
        try (Reader reader = new FileReader(file)) {
            s = ValidationXmlUtils.loadStandaloneSetFromXml(reader);
            assertFakeSet(s);
        }

        // read gzipped file
        file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/fake-set.xml.gz");
        if (!file.exists())
            Assert.fail("This test requires the file 'fake-set.xml.gz'");
        s = ValidationXmlUtils.loadStandaloneSetFromXml(file);
        assertFakeSet(s);
    }

    @Test
    public void testStandaloneSetWriteMethods() throws IOException {
        File file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/fake-set.xml");
        if (!file.exists())
            Assert.fail("This test requires the file 'fake-set.xml'");
        StandaloneSet s = ValidationXmlUtils.loadStandaloneSetFromXml(file);

        File targetFile = new File(TestingUtils.TMP_DIR, "xml-set-test.xml");

        // write using a file
        ValidationXmlUtils.writeStandaloneSetToXml(s, targetFile);
        assertFakeSet(targetFile);

        // write using an output stream
        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
            ValidationXmlUtils.writeStandaloneSetToXml(s, fos);
        }
        assertFakeSet(targetFile);

        // write using a writer
        try (FileWriter writer = new FileWriter(targetFile)) {
            ValidationXmlUtils.writeStandaloneSetToXml(s, writer);
        }
        assertFakeSet(targetFile);

        // write gzipped file using multi-threading parsing
        targetFile = new File(TestingUtils.TMP_DIR, "xml-set-test.xml.gz");
        ValidationXmlUtils.writeStandaloneSetToXml(s, targetFile);
        assertFakeSet(targetFile);
    }

    @Test(expected = IOException.class)
    public void testStandaloneSetErrorNoId() throws IOException {
        ValidationXmlUtils.loadStandaloneSetFromXml(Thread.currentThread().getContextClassLoader().getResource("fake-set-no-id.xml"));
    }

    @Test(expected = IOException.class)
    public void testStandaloneSetErrorLoadNullFile() throws IOException {
        ValidationXmlUtils.loadStandaloneSetFromXml((File)null);
    }

    @Test(expected = IOException.class)
    public void testStandaloneSetErrorLoadNullUrl() throws IOException {
        ValidationXmlUtils.loadStandaloneSetFromXml((URL)null);
    }

    @Test(expected = IOException.class)
    public void testStandaloneSetErrorLoadNullInputFile() throws IOException {
        ValidationXmlUtils.loadStandaloneSetFromXml((InputStream)null);
    }

    @Test(expected = IOException.class)
    public void testStandaloneSetErrorLoadNullReader() throws IOException {
        ValidationXmlUtils.loadStandaloneSetFromXml((Reader)null);
    }

    @Test(expected = IOException.class)
    public void testStandaloneSetErrorWriteNullFile() throws IOException {
        ValidationXmlUtils.writeStandaloneSetToXml(new StandaloneSet(), (File)null);
    }

    @Test(expected = IOException.class)
    public void testStandaloneSetErrorWriteNullSetForFile() throws IOException {
        ValidationXmlUtils.writeStandaloneSetToXml(null, new File(TestingUtils.TMP_DIR, "whatever.xml"));
    }

    @Test(expected = IOException.class)
    public void testStandaloneSetErrorWriteNullOutputStream() throws IOException {
        ValidationXmlUtils.writeStandaloneSetToXml(new StandaloneSet(), (OutputStream)null);
    }

    @Test(expected = IOException.class)
    public void testStandaloneSetErrorWriteNullSetForOutputStream() throws IOException {
        ValidationXmlUtils.writeStandaloneSetToXml(null, new FileOutputStream(new File(TestingUtils.TMP_DIR, "whatever.xml")));
    }

    @Test(expected = IOException.class)
    public void testStandaloneSetErrorWriteNullWriter() throws IOException {
        ValidationXmlUtils.writeStandaloneSetToXml(new StandaloneSet(), (Writer)null);
    }

    @Test(expected = IOException.class)
    public void testStandaloneSetErrorWriteNullSetForWriter() throws IOException {
        ValidationXmlUtils.writeValidatorToXml(null, new FileWriter(new File(TestingUtils.TMP_DIR, "whatever.xml")));
    }

    // helper
    private void assertFakeSet(File file) throws IOException {
        StandaloneSet s = ValidationXmlUtils.loadStandaloneSetFromXml(file);
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
        ValidatorTests t = ValidationXmlUtils.loadTestsFromXml(Thread.currentThread().getContextClassLoader().getResource("fake-tests.xml"));
        assertFakeTests(t);

        // read using a file
        t = ValidationXmlUtils.loadTestsFromXml(file);
        assertFakeTests(t);

        // read from input stream
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("fake-tests.xml")) {
            t = ValidationXmlUtils.loadTestsFromXml(is);
            assertFakeTests(t);
        }

        // read from reader
        try (Reader reader = new FileReader(file)) {
            t = ValidationXmlUtils.loadTestsFromXml(reader);
            assertFakeTests(t);
        }

        // read gzipped file
        file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/fake-tests.xml.gz");
        if (!file.exists())
            Assert.fail("This test requires the file 'fake-tests.xml.gz'");
        t = ValidationXmlUtils.loadTestsFromXml(file);
        assertFakeTests(t);
    }

    @Test
    public void testTestsWriteMethods() throws IOException {
        File file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/fake-tests.xml");
        if (!file.exists())
            Assert.fail("This test requires the file 'fake-tests.xml'");
        ValidatorTests s = ValidationXmlUtils.loadTestsFromXml(file);

        File targetFile = new File(TestingUtils.TMP_DIR, "xml-set-test.xml");

        // write using a file
        ValidationXmlUtils.writeTestsToXml(s, targetFile);
        assertFakeTests(targetFile);

        // write using an output stream
        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
            ValidationXmlUtils.writeTestsToXml(s, fos);
        }
        assertFakeTests(targetFile);

        // write using a writer
        try (FileWriter writer = new FileWriter(targetFile)) {
            ValidationXmlUtils.writeTestsToXml(s, writer);
        }
        assertFakeTests(targetFile);

        // write gzipped file using multi-threading parsing
        targetFile = new File(TestingUtils.TMP_DIR, "xml-tests-test.xml.gz");
        ValidationXmlUtils.writeTestsToXml(s, targetFile);
        assertFakeTests(targetFile);
    }

    @Test(expected = IOException.class)
    public void testTestsErrorBadScript() throws IOException {
        ValidationXmlUtils.loadStandaloneSetFromXml(Thread.currentThread().getContextClassLoader().getResource("fake-tests-exception.xml"));
    }

    @Test(expected = IOException.class)
    public void testTestsErrorLoadNullFile() throws IOException {
        ValidationXmlUtils.loadTestsFromXml((File)null);
    }

    @Test(expected = IOException.class)
    public void testTestsErrorLoadNullUrl() throws IOException {
        ValidationXmlUtils.loadTestsFromXml((URL)null);
    }

    @Test(expected = IOException.class)
    public void testTestErrorLoadNullInputStream() throws IOException {
        ValidationXmlUtils.loadTestsFromXml((InputStream)null);
    }

    @Test(expected = IOException.class)
    public void testTestsErrorLoadNullReader() throws IOException {
        ValidationXmlUtils.loadTestsFromXml((Reader)null);
    }

    @Test(expected = IOException.class)
    public void testTestsErrorWriteNullFile() throws IOException {
        ValidationXmlUtils.writeTestsToXml(new ValidatorTests(), (File)null);
    }

    @Test(expected = IOException.class)
    public void testTestsErrorWriteNullSetForFile() throws IOException {
        ValidationXmlUtils.writeTestsToXml(null, new File(TestingUtils.TMP_DIR, "whatever.xml"));
    }

    @Test(expected = IOException.class)
    public void testTestsErrorWriteNullOutputStream() throws IOException {
        ValidationXmlUtils.writeTestsToXml(new ValidatorTests(), (OutputStream)null);
    }

    @Test(expected = IOException.class)
    public void testTestsErrorWriteNullSetForOutputStream() throws IOException {
        ValidationXmlUtils.writeTestsToXml(null, new FileOutputStream(new File(TestingUtils.TMP_DIR, "whatever.xml")));
    }

    @Test(expected = IOException.class)
    public void testTestsErrorWriteNullWriter() throws IOException {
        ValidationXmlUtils.writeTestsToXml(new ValidatorTests(), (Writer)null);
    }

    @Test(expected = IOException.class)
    public void testTestsErrorWriteNullSetForWriter() throws IOException {
        ValidationXmlUtils.writeTestsToXml(null, new FileWriter(new File(TestingUtils.TMP_DIR, "whatever.xml")));
    }

    // helper
    private void assertFakeTests(File file) throws IOException {
        ValidatorTests t = ValidationXmlUtils.loadTestsFromXml(file);
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
        Assert.assertFalse(ValidationXmlUtils.targetValidatorXmlExists(null));
        Assert.assertTrue(ValidationXmlUtils.targetValidatorXmlExists(Thread.currentThread().getContextClassLoader().getResource("fake-validator.xml")));
    }

    @Test
    public void testGetXmlValidatorHash() {
        Assert.assertNull(ValidationXmlUtils.getXmlValidatorHash(null));

        URL url = Thread.currentThread().getContextClassLoader().getResource("fake-validator.xml");
        Assert.assertNotNull(ValidationXmlUtils.getXmlValidatorHash(url));

        URL url2 = Thread.currentThread().getContextClassLoader().getResource("fake-validator.xml.gz");
        Assert.assertNotNull(ValidationXmlUtils.getXmlValidatorHash(url2));

        // doesn't matter if the URL is gzipped, the hash should be the same...
        if (SystemUtils.IS_OS_WINDOWS) // this doesn't work on linux, not sure why...
            Assert.assertEquals(ValidationXmlUtils.getXmlValidatorHash(url), ValidationXmlUtils.getXmlValidatorHash(url2));
    }

    @Test
    public void testGetXmlValidatorRootAttributes() {
        Map<String, String> expected = new HashMap<>();
        expected.put(ValidationXmlUtils.ROOT_ATTR_ID, "fake-validator");
        expected.put(ValidationXmlUtils.ROOT_ATTR_NAME, "Fake Validator");
        expected.put(ValidationXmlUtils.ROOT_ATTR_VERSION, "TEST-002-01");
        expected.put(ValidationXmlUtils.ROOT_ATTR_MIN_ENGINE_VERSION, "4.0");
        expected.put(ValidationXmlUtils.ROOT_ATTR_TRANSLATED_FROM, "test");
        Assert.assertEquals(expected, ValidationXmlUtils.getXmlValidatorRootAttributes(Thread.currentThread().getContextClassLoader().getResource("fake-validator.xml")));
        Assert.assertEquals(expected, ValidationXmlUtils.getXmlValidatorRootAttributes(Thread.currentThread().getContextClassLoader().getResource("fake-validator.xml.gz")));
        Assert.assertEquals(expected, ValidationXmlUtils.getXmlValidatorRootAttributes(Thread.currentThread().getContextClassLoader().getResource("fake-validator-multi-line.xml")));

        expected.clear();
        expected.put(ValidationXmlUtils.ROOT_ATTR_NAME, "Fake Validator No ID");
        Assert.assertEquals(expected, ValidationXmlUtils.getXmlValidatorRootAttributes(Thread.currentThread().getContextClassLoader().getResource("fake-validator-no-id.xml")));

        expected.clear();
        expected.put(ValidationXmlUtils.ROOT_ATTR_ID, "fake-validator-large-prefix");
        expected.put(ValidationXmlUtils.ROOT_ATTR_NAME, "Fake Validator Large Prefix");
        Assert.assertEquals(expected, ValidationXmlUtils.getXmlValidatorRootAttributes(Thread.currentThread().getContextClassLoader().getResource("fake-validator-large-prefix-text.xml")));

        expected.clear();
        Assert.assertEquals(expected, ValidationXmlUtils.getXmlValidatorRootAttributes(Thread.currentThread().getContextClassLoader().getResource("property-parsing-test.txt")));
    }

    @Test
    public void testGetXmlValidatorId() {
        Assert.assertNull(ValidationXmlUtils.getXmlValidatorId(null));
        Assert.assertNull(ValidationXmlUtils.getXmlValidatorId(Thread.currentThread().getContextClassLoader().getResource("fake-validator-no-id.xml")));

        URL url = Thread.currentThread().getContextClassLoader().getResource("fake-validator.xml");
        Assert.assertEquals("fake-validator", ValidationXmlUtils.getXmlValidatorId(url));

        url = Thread.currentThread().getContextClassLoader().getResource("fake-validator.xml.gz");
        Assert.assertEquals("fake-validator", ValidationXmlUtils.getXmlValidatorId(url));
    }

    @Test
    public void testGetXmlValidatorName() {
        Assert.assertNull(ValidationXmlUtils.getXmlValidatorName(null));

        URL url = Thread.currentThread().getContextClassLoader().getResource("fake-validator.xml");
        Assert.assertEquals("Fake Validator", ValidationXmlUtils.getXmlValidatorName(url));

        url = Thread.currentThread().getContextClassLoader().getResource("fake-validator.xml.gz");
        Assert.assertEquals("Fake Validator", ValidationXmlUtils.getXmlValidatorName(url));
    }

    @Test
    public void testGetXmlValidatorVersion() {
        Assert.assertNull(ValidationXmlUtils.getXmlValidatorVersion(null));

        URL url = Thread.currentThread().getContextClassLoader().getResource("fake-validator.xml");
        Assert.assertEquals("TEST-002-01", ValidationXmlUtils.getXmlValidatorVersion(url));

        url = Thread.currentThread().getContextClassLoader().getResource("fake-validator.xml.gz");
        Assert.assertEquals("TEST-002-01", ValidationXmlUtils.getXmlValidatorVersion(url));
    }

    @Test
    public void testTrimEmptyLines() {
        Assert.assertNull(ValidationXmlUtils.trimEmptyLines(null, true));

        try {
            ValidationXmlUtils.enableRealignment();

            String s = " some text  ";
            String exp = "some text";
            Assert.assertEquals(exp, ValidationXmlUtils.trimEmptyLines(s, true));

            s = "\n     \n     \n\n   abc\n     ed\n\n   fh\n \n    \n     \n";
            exp = "abc\n     ed\n\n   fh";
            Assert.assertEquals(exp, ValidationXmlUtils.trimEmptyLines(s, true));

            s = "\r\n     \r\n     \n\n   abc\n     ed\n\n   fh\n \n    \r\n     \r\n";
            Assert.assertEquals(exp, ValidationXmlUtils.trimEmptyLines(s, true));

            s = "Some text with\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\nmany new lines";
            exp = "Some text with\n\n\nmany new lines";
            Assert.assertEquals(exp, ValidationXmlUtils.trimEmptyLines(s, true));

            s = "Some text with\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\nmany new lines";
            Assert.assertEquals(exp, ValidationXmlUtils.trimEmptyLines(s, true));

            ValidationXmlUtils.disableRealignment();

            s = " some text  ";
            exp = "some text";
            Assert.assertEquals(exp, ValidationXmlUtils.trimEmptyLines(s, true));

            s = "\n     \n     \n\n   abc\n     ed\n\n   fh\n \n    \n     \n";
            exp = "abc\n     ed\n\n   fh";
            Assert.assertEquals(exp, ValidationXmlUtils.trimEmptyLines(s, true));
            Assert.assertEquals(s, ValidationXmlUtils.trimEmptyLines(s, false));
        }
        finally {
            ValidationXmlUtils.enableRealignment();
        }
    }

    @Test
    public void testReAlign() {
        Assert.assertNull(ValidationXmlUtils.reAlign(null));

        try {
            ValidationXmlUtils.enableRealignment();

            String s = "some text  ";
            String exp = "some text";
            Assert.assertEquals(exp, ValidationXmlUtils.reAlign(s));

            s = "   abc \n     - abc\n     - def\n       -- gh\n     -ifk\n   lm";
            exp = "abc \n  - abc\n  - def\n    -- gh\n  -ifk\nlm";
            Assert.assertEquals(exp, ValidationXmlUtils.reAlign(s));

            s = "   abc \n     - abc\n     - def\n -- gh\n     -ifk\n   lm";
            exp = "abc \n    - abc\n    - def\n-- gh\n    -ifk\n  lm";
            Assert.assertEquals(exp, ValidationXmlUtils.reAlign(s));

            s = "   abc \r\n     - abc\r\n     - def\r\n -- gh\r\n     -ifk\r\n   lm";
            Assert.assertEquals(exp, ValidationXmlUtils.reAlign(s));

            ValidationXmlUtils.disableRealignment();

            s = "some text  ";
            Assert.assertEquals(s, ValidationXmlUtils.reAlign(s));

            s = "   abc \n     - abc\n     - def\n       -- gh\n     -ifk\n   lm";
            Assert.assertEquals(s, ValidationXmlUtils.reAlign(s));
        }
        finally {
            ValidationXmlUtils.enableRealignment();
        }
    }
}
