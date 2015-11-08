/*
 * Copyright (C) 2011 Information Management Services, Inc.
 */
package com.imsweb.validation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.imsweb.validation.entities.Rule;
import com.imsweb.validation.entities.RuleFailure;
import com.imsweb.validation.entities.RuleTest;
import com.imsweb.validation.entities.RuleTestResult;
import com.imsweb.validation.entities.SimpleMapValidatable;
import com.imsweb.validation.entities.SimpleNaaccrLinesValidatable;
import com.imsweb.validation.entities.Validatable;
import com.imsweb.validation.shared.ContextFunctionDocAnnotation;

/**
 * Context available to the testing framework. The testing Groovy scripts can access the methdos of this context
 * using the "Testing." prefix. The only methods that should be accessed are the <b>assertPass()</b> and <b>assertFail()</b>
 * methods. The <b>getTestsResults()</b> method is used by the testing framework to gather the assertion results and
 * provide them back to the application.
 * <p/>
 * This class supports the <b>NAACCR line notation</b> (line.primarySite), which is pretty standard. For applications
 * requiring to support other data types, this class should be extended (in particular the <b>createValidatable()</b>
 * method, and the extended version should be provided to the <b>ruleTest.executeTest()</b> method.
 * <p/>
 * Created on Aug 8, 2011 by depryf
 * @author depryf
 */
public class TestingContextFunctions {

    /**
     * Test assertion types.
     * <p/>
     * Created on Oct 2, 2011 by Fabian
     * @author Fabian
     */
    public enum AssertionType {
        /**
         * Use this when expecting the edit to pass
         */
        PASS,
        /**
         * Use this when expecting the edit to fail
         */
        FAIL
    }

    /**
     * Tested rule ID
     */
    protected String _ruleId;

    /**
     * Tested Rule
     */
    protected Rule _rule;

    /**
     * Map of tests, key is the line number in the script, value is a list of tests (list will have more then one element in repetitions like for loops)
     */
    protected Map<Integer, List<RuleTestResult>> _tests;

    /**
     * Constructor.
     * <p/>
     * Created on Oct 2, 2011 by Fabian
     * @param test <code>RuleTest</code>, cannot be null
     * @throws Exception if the test does not correspond to an existing rule
     */
    public TestingContextFunctions(RuleTest test, Rule rule) throws Exception {
        _ruleId = test.getTestedRuleId();
        _rule = rule;
        _tests = new TreeMap<>();
    }

    /**
     * Asserts that a given test passes.
     * <p/>
     * Created on Jun 6, 2011 by murphyr
     * @param lineNumber test line number
     * @param dataObj test inputs
     */
    @ContextFunctionDocAnnotation(desc = "TODO")
    public void assertPass(int lineNumber, Object dataObj) {
        assertPass(lineNumber, dataObj, null);
    }

    /**
     * Asserts that a given test passes.
     * <p/>
     * Created on Jul 21, 2011 by murphyr
     * @param lineNumber test line number
     * @param dataObj test inputs
     * @param context extra context for the test
     */
    @ContextFunctionDocAnnotation(desc = "TODO")
    public void assertPass(int lineNumber, Object dataObj, Map<String, Object> context) {

        // redirect the output
        OutputStream output = new OutputStream() {
            private StringBuilder _buf = new StringBuilder();

            @Override
            public void write(int b) throws IOException {
                _buf.append((char)b);
            }

            @Override
            public String toString() {
                return _buf.toString();
            }
        };
        if (context == null)
            context = new HashMap<>();
        context.put("out", output);

        try {
            Collection<RuleFailure> results = runTest(dataObj, context);

            // a test runs for one edit, so there should be 0 or 1 failure (anything else is ignored)
            RuleFailure failure = results.isEmpty() ? null : results.iterator().next();

            // it's a success if there was no failure
            boolean success = failure == null;

            // add testing result
            insertTestingResult(lineNumber, AssertionType.PASS, success, failure, dataObj, context, null, null, output);
        }
        catch (ConstructionException e) {
            insertTestingResult(lineNumber, AssertionType.FAIL, false, null, dataObj, context, new ValidationException(e), null, output);
        }
        catch (ValidationException e) {
            insertTestingResult(lineNumber, AssertionType.PASS, false, null, dataObj, context, e, null, output);
        }
    }

    /**
     * Asserts that a given test fails.
     * <p/>
     * Created on Jun 6, 2011 by murphyr
     * @param lineNumber test line number
     * @param dataObj test inputs
     * @param failingProperties array of properties that needs to appear in the list of failing properties for this failure
     */
    @ContextFunctionDocAnnotation(desc = "TODO")
    public void assertFail(int lineNumber, Object dataObj, String... failingProperties) {
        assertFail(lineNumber, dataObj, null, failingProperties);
    }

    /**
     * Asserts that a given test fails.
     * <p/>
     * Created on Jul 21, 2011 by murphyr
     * @param lineNumber test line number
     * @param dataObj test inputs
     * @param failingProperties array of properties that needs to appear in the list of failing properties for this failure
     * @param context extra context for the test
     */
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    @ContextFunctionDocAnnotation(desc = "TODO")
    public void assertFail(int lineNumber, Object dataObj, Map<String, Object> context, String... failingProperties) {
        Set<String> props = new HashSet<>();
        if (failingProperties != null)
            props.addAll(Arrays.asList(failingProperties));

        // redirect the output
        OutputStream output = new OutputStream() {
            private StringBuilder _buf = new StringBuilder();

            @Override
            public void write(int b) throws IOException {
                _buf.append((char)b);
            }

            @Override
            public String toString() {
                return _buf.toString();
            }
        };
        if (context == null)
            context = new HashMap<>();
        context.put("out", output);

        try {
            Collection<RuleFailure> results = runTest(dataObj, context);

            // a test runs for one edit, so there should be 0 or 1 failure (anything else is ignored)
            RuleFailure failure = results.isEmpty() ? null : results.iterator().next();

            // it's a success if there was a failure, but not an exception
            boolean success = failure != null && failure.getGroovyException() == null && failure.getProperties().containsAll(props);

            // add testing result
            insertTestingResult(lineNumber, AssertionType.FAIL, success, failure, dataObj, context, null, props, output);
        }
        catch (ConstructionException e) {
            insertTestingResult(lineNumber, AssertionType.FAIL, false, null, dataObj, context, new ValidationException(e), props, output);
        }
        catch (ValidationException e) {
            insertTestingResult(lineNumber, AssertionType.FAIL, false, null, dataObj, context, e, props, output);
        }
    }

    /**
     * Creation method.
     * <p/>
     * Created on Nov 18, 2011 by depryf
     * @return created entity
     */
    @ContextFunctionDocAnnotation(desc = "TODO")
    public List<Map<String, String>> createLines() {
        return new ArrayList<>();
    }

    /**
     * Creation method.
     * <p/>
     * Created on Nov 18, 2011 by depryf
     * @return created entity
     */
    @ContextFunctionDocAnnotation(desc = "TODO")
    public Map<String, String> createLine() {
        return new HashMap<>();
    }

    /**
     * Creation method.
     * <p/>
     * Created on Nov 18, 2011 by depryf
     * @return created entity
     */
    @ContextFunctionDocAnnotation(desc = "TODO")
    public Map<String, String> createLine(List<Map<String, String>> lines) {
        Map<String, String> line = createLine();
        lines.add(line);
        return line;
    }

    /**
     * Creation method.
     * <p/>
     * Created on Nov 18, 2011 by depryf
     * @return created entity
     */
    @ContextFunctionDocAnnotation(desc = "TODO")
    public List<Map<String, String>> createUntrimmedlines() {
        return new ArrayList<>();
    }

    /**
     * Creation method.
     * <p/>
     * Created on Nov 18, 2011 by depryf
     * @return created entity
     */
    @ContextFunctionDocAnnotation(desc = "TODO")
    public Map<String, String> createUntrimmedline() {
        return new HashMap<>();
    }

    /**
     * Creation method.
     * <p/>
     * Created on Nov 18, 2011 by depryf
     * @return created entity
     */
    @ContextFunctionDocAnnotation(desc = "TODO")
    public Map<String, String> createUntrimmedline(List<Map<String, String>> untrimmedlines) {
        Map<String, String> untrimmedline = createUntrimmedline();
        untrimmedlines.add(untrimmedline);
        return untrimmedline;
    }

    /**
     * Returns the test results gathered so far. This method is not meant to be called from a testing Groovy script.
     * <p/>
     * Created on Jun 6, 2011 by murphyr
     * @return test results gathered so far.
     */
    public Map<Integer, List<RuleTestResult>> getTestsResults() {
        return Collections.unmodifiableMap(_tests);
    }

    /**
     * Runs the tests using the provided data and extra context.
     * <p/>
     * Created on Jun 6, 2011 by murphyr
     * @param data data object being validated
     * @param context extra context, may be null
     * @return the test results, a collection of <code>RuleFailure</code>
     */
    protected Collection<RuleFailure> runTest(Object data, Map<String, Object> context) throws ConstructionException, ValidationException {
        if (_rule != null)
            return ValidationEngine.validate(createValidatable(data, context), _rule);
        else
            return ValidationEngine.validate(createValidatable(data, context), _ruleId);
    }

    /**
     * Creates a validatable object from the incoming data and context.
     * <p/>
     * Created on Oct 2, 2011 by Fabian
     * @param data data object being validated
     * @param context extra context, may be null
     * @return a <code>Validatable</code> to use in the Groovy test, never null
     */
    @SuppressWarnings("unchecked")
    protected Validatable createValidatable(Object data, Map<String, Object> context) {
        Validatable result;

        if (data == null)
            throw new RuntimeException("Invalid testing data structure: cannot run edit on null object");

        Rule r = _rule;
        if (r == null)
            r = ValidationEngine.getRule(_ruleId);
        if (r != null) {
            String javaPath = r.getJavaPath();
            if (javaPath != null) {

                if (javaPath.equals("lines") || javaPath.equals("untrimmedlines") || javaPath.equals("lines.line") || javaPath.equals("untrimmedlines.untrimmedline")) {
                    boolean useUntrimmedNotation = r.getJavaPath().startsWith("untrimmedlines.");
                    if ("lines".equals(javaPath) || "untrimmedlines".equals(javaPath)) {
                        if (data instanceof List)
                            result = new SimpleNaaccrLinesValidatable((List<Map<String, String>>)data, context, useUntrimmedNotation);
                        else
                            throw new RuntimeException("Invalid testing data structure: expected List, got " + data.getClass().getSimpleName());
                    }
                    else {
                        if (data instanceof Map)
                            result = new SimpleNaaccrLinesValidatable(Collections.singletonList((Map<String, String>)data), context, useUntrimmedNotation);
                        else
                            throw new RuntimeException("Invalid testing data structure: expected Map, got " + data.getClass().getSimpleName());
                    }
                }
                else if (data instanceof Map)
                    result = new SimpleMapValidatable("?", javaPath, (Map<String, Object>)data, context);
                else
                    throw new RuntimeException("Invalid testing data structure: expected Map, got " + data.getClass().getSimpleName());
            }
            else
                throw new RuntimeException("Rule '" + r.getId() + "' doesn't define a java-path");
        }
        else
            throw new RuntimeException("Unable to find rule '" + _ruleId + "'");

        return result;
    }

    /**
     * Creates a test result, adds it to the list of existing result for the provided line number.
     * <p/>
     * Created on Jul 21, 2011 by murphyr
     * @param lineNum line number
     * @param type expected assertion type (PASS or FAIL)
     * @param success whether the assertion was successful or not
     * @param failure rule failure (non-null only if the edit actually failed)
     * @param values data that was validated
     * @param contextValues values of the extra context used for the validation, if any
     * @param exc validation exception (if the validation engine was unable to run the edit
     * @param f asserted failing properties
     * @param os redirected output
     */
    protected void insertTestingResult(int lineNum, AssertionType type, boolean success, RuleFailure failure, Object values, Map<String, Object> contextValues, ValidationException exc, Set<String> f, OutputStream os) {

        // get the list of results for this line number
        List<RuleTestResult> list = _tests.get(lineNum);
        if (list == null) {
            list = new ArrayList<>();
            _tests.put(lineNum, list);
        }

        // build the log content from the output stream
        List<String> log = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new StringReader(os.toString()));
            String line = reader.readLine();
            while (line != null) {
                log.add(line);
                line = reader.readLine();
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Unable to redirect output", e);
        }

        // add a new result to that list
        list.add(new RuleTestResult(lineNum, list.size() + 1, type, success, failure, cloneData(values), contextValues, exc, f, log));
    }

    /**
     * This methods clone the data. It currently supports only combinations of maps, lists and simple types.
     * <p/>
     * Created on Nov 1, 2011 by depryf
     * @param data data to clone
     * @return cloned data
     */
    @SuppressWarnings("unchecked")
    protected Object cloneData(Object data) {
        if (data == null)
            return null;

        Object result;

        if (data instanceof Map)
            result = cloneMap((Map<String, Object>)data);
        else if (data instanceof List)
            result = cloneList((List<Object>)data);
        else if (isSimpleType(data.getClass()))
            result = data;
        else
            throw new RuntimeException("Unsupported data type: " + data.getClass().getName());

        return result;
    }

    /**
     * Clones the provided map.
     * <p/>
     * Created on Nov 1, 2011 by depryf
     * @param data map to clone
     * @return cloned map
     */
    @SuppressWarnings("unchecked")
    protected Map<String, Object> cloneMap(Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();

        // used to remove special properties automatically added by the validation engine
        Set<String> usedProperties = null;
        Rule r = _rule;
        if (r == null)
            r = ValidationEngine.getRule(_ruleId);
        if (r != null && r.getJavaPath() != null) {
            String javaPath = r.getJavaPath();
            if (javaPath.startsWith("lines") || javaPath.startsWith("untrimmedlines")) {
                usedProperties = new HashSet<>();
                for (String s : r.getRawProperties())
                    usedProperties.add(javaPath.startsWith("lines") ? s.replace("line.", "") : s.replace("untrimmedline.", ""));
            }
        }

        for (Entry<String, Object> entry : data.entrySet()) {
            Object obj = entry.getValue();

            if (obj == null)
                result.put(entry.getKey(), null);
            else if (obj instanceof Map)
                result.put(entry.getKey(), cloneMap((Map<String, Object>)obj));
            else if (obj instanceof List)
                result.put(entry.getKey(), cloneList((List<Object>)obj));
            else if (isSimpleType(obj.getClass())) {
                if (!entry.getKey().startsWith("_") || (usedProperties != null && usedProperties.contains(entry.getKey())))
                    result.put(entry.getKey(), obj);
            }
            else
                throw new RuntimeException("Unsupported data type: " + obj.getClass().getName());
        }

        return result;
    }

    /**
     * Clones the provided list.
     * <p/>
     * Created on Nov 1, 2011 by depryf
     * @param data list to clone
     * @return cloned list
     */
    @SuppressWarnings("unchecked")
    protected List<Object> cloneList(List<Object> data) {
        List<Object> result = new ArrayList<>();

        for (Object obj : data) {
            if (obj == null)
                result.add(null);
            else if (obj instanceof Map)
                result.add(cloneMap((Map<String, Object>)obj));
            else if (obj instanceof List)
                result.add(cloneList((List<Object>)obj));
            else if (isSimpleType(obj.getClass())) {
                result.add(obj);
            }
            else
                throw new RuntimeException("Unsupported data type: " + obj.getClass().getName());
        }

        return result;
    }

    /**
     * Returns true if the provided class represent a "simple" type, false otherwise.
     * <p/>
     * Created on Nov 1, 2011 by depryf
     * @param clazz class
     * @return true if the provided class represent a "simple" type, false otherwise
     */
    protected boolean isSimpleType(Class<?> clazz) {
        return String.class.equals(clazz) || Number.class.isAssignableFrom(clazz) || Boolean.class.equals(clazz) || Date.class.equals(clazz);
    }

    /**
     * Returns a testing string of passed length. The generated string will be composed of
     * uppercase letters, in order. When Z is reached, A starts again...
     * <p/>
     * For example getTestingString(3) = ABC
     * <p/>
     * Created on Jul 25, 2006 by depryf
     * @param length requested length
     * @return testing string
     */
    public static String createString(int length) {
        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < length; i++)
            buf.append((char)((i % 26) + 65));

        return buf.toString();
    }

    /**
     * Returns current date.
     * <p/>
     * Created on Jan 19, 2012 by murphyr
     * @return current date
     */
    public static Date createDate() {
        return new Date();
    }

    /**
     * Returns the date based on the numbers passed in.  Month is (1-12) and day is (1-31).
     * <p/>
     * Created on Jan 19, 2012 by murphyr
     * @param year year to set
     * @param month month to set
     * @param day day to set
     * @return corresponding date
     */
    public static Date createDate(int year, int month, int day) {
        return new GregorianCalendar(year, month - 1, day).getTime();
    }
}
