/*
 * Copyright (C) 2011 Information Management Services, Inc.
 */
package com.imsweb.validation.entities;

import com.imsweb.validation.ValidationContextFunctions;
import com.imsweb.validation.ValidationEngine;
import com.imsweb.validation.ValidationServices;
import com.imsweb.validation.functions.TestingContextFunctions;
import groovy.lang.Binding;
import groovy.lang.Script;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A single rule test (a Groovy script).
 * <p/>
 * Created on Aug 7, 2011 by depryf
 */
public class RuleTest {

    /** Tested Rule ID */
    protected String _testedRuleId;

    /** Testing Script text */
    protected String _scriptText;

    /** Translated Script text (we dynamically add line number to the assertions) */
    protected String _translatedScriptText;

    /** Assertions (key is line number, value is true if it's an assertPass, false if it's an assertFail) */
    protected Map<Integer, Boolean> _assertions;

    /**
     * Created on Aug 8, 2011 by depryf
     * @param testedRuleId tested Rule ID
     */
    public void setTestedRuleId(String testedRuleId) {
        _testedRuleId = testedRuleId;
    }

    /** @param scriptText The sScriptText to set. */
    public void setScriptText(String scriptText) throws IOException {
        this._scriptText = scriptText;
        this._translatedScriptText = scriptText;
        this._assertions = new HashMap<>();

        if (scriptText != null && !scriptText.isEmpty()) {
            StringBuilder buf = new StringBuilder();

            LineNumberReader reader = new LineNumberReader(new StringReader(scriptText));
            String line = reader.readLine();
            while (line != null) {
                int count = StringUtils.countMatches(line, "Testing.assert");
                if (count > 1)
                    throw new IOException("Line " + reader.getLineNumber() + " contains several assertions!");

                if (count > 0) {
                    int lineNum = reader.getLineNumber(); // line num must be 1-base, but lineNumber is already 1-based...
                    if (line.contains("Testing.assertPass")) {
                        line = line.replace("Testing.assertPass(", "Testing.assertPass(" + lineNum + ", ");
                        this._assertions.put(lineNum, Boolean.TRUE);
                    }
                    else {
                        line = line.replace("Testing.assertFail(", "Testing.assertFail(" + lineNum + ", ");
                        this._assertions.put(lineNum, Boolean.FALSE);
                    }
                }

                buf.append(line).append("\n");
                line = reader.readLine();
            }

            this._translatedScriptText = buf.toString();
        }
    }

    /** @return Returns the testedRuleId. */
    public String getTestedRuleId() {
        return _testedRuleId;
    }

    /** @return Returns the originalScriptText. */
    public String getScriptText() {
        return _scriptText;
    }

    /** @return Returns the assertions. */
    public Map<Integer, Boolean> getAssertions() {
        return _assertions;
    }

    /**
     * Executes the tests using the default testing context object (which supports the NAACCR line notation).
     * <p/>
     * Created on Aug 8, 2011 by depryf
     * @return a map of Integer (line number) -&gt list of <code>RuleTestResult</code>, the list indexes determines the assertion indexes
     */
    public Map<Integer, List<RuleTestResult>> executeTest() {
        return executeTest(new TestingContextFunctions(this, null), null);
    }

    /**
     * Executes the tests using the default testing context object (which supports the NAACCR line notation).
     * <p/>
     * Created on Aug 8, 2011 by depryf
     * @param rule a <code>Rule</code> to use for the test (useful for testing modified rules or new ones)
     * @return a map of Integer (line number) -&gt list of <code>RuleTestResult</code>, the list indexes determines the assertion indexes
     */
    public Map<Integer, List<RuleTestResult>> executeTest(Rule rule) {
        return executeTest(new TestingContextFunctions(this, rule), rule);
    }

    /**
     * Executes the tests using the provided testing context object.
     * <p/>
     * Created on Aug 8, 2011 by depryf
     * @param context a <code>TestingContextFunctions</code>, can be nulls
     * @return a map of Integer (line number) -&gt list of <code>RuleTestResult</code>, the list indexes determines the assertion indexes
     */
    public Map<Integer, List<RuleTestResult>> executeTest(TestingContextFunctions context) {
        return executeTest(context, null);
    }

    /**
     * Executes the tests using the provided testing context object.
     * <p/>
     * Created on Aug 8, 2011 by depryf
     * @param context a <code>TestingContextFunctions</code>, can be nulls
     * @param rule a <code>Rule</code> to use for the test (useful for testing modified rules or new ones)
     * @return a map of Integer (line number) -&gt list of <code>RuleTestResult</code>, the list indexes determines the assertion indexes
     */
    public Map<Integer, List<RuleTestResult>> executeTest(TestingContextFunctions context, Rule rule) {

        // better safe than sorry
        if (context == null)
            context = new TestingContextFunctions(this, rule);

        // create Groovy script and set its binding
        Script script = ValidationServices.getInstance().compileExpression(_translatedScriptText);
        Binding binding = new Binding();
        binding.setVariable(ValidationEngine.VALIDATOR_TESTING_FUNCTIONS_KEY, context);
        binding.setVariable(ValidationEngine.VALIDATOR_FUNCTIONS_KEY, ValidationContextFunctions.getInstance());
        script.setBinding(binding);

        // execute the script
        script.run();

        return context.getTestsResults();
    }
}
