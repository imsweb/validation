/*
 * Copyright (C) 2011 Information Management Services, Inc.
 */
package com.imsweb.validation.entities;

import com.imsweb.validation.ValidationException;
import com.imsweb.validation.functions.TestingContextFunctions.AssertionType;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A rule test result.
 * <p/>
 * Created on Aug 8, 2011 by depryf
 */
public class RuleTestResult {

    /**
     * Line number for this result (this is not unique per assertion because of for/while loops)
     */
    protected Integer _lineNumber;

    /**
     * Assertion index (1-based) for this result (line number and assertion index makes an assertion unique)
     */
    protected Integer _assertionIndex;

    /**
     * Assertion type
     */
    protected AssertionType _assertionType;

    /**
     * Properties that need to be in the failing properties returned by the validation engine
     */
    protected Set<String> _assertedFailingProperties;

    /**
     * Whether this test was a success or not (true if the assertion was correct, false otherwise)
     */
    protected Boolean _isSuccess;

    /**
     * Rule failure, null if the edit passed
     */
    protected RuleFailure _ruleFailure;

    /**
     * Values of the variables at assertion
     */
    protected Object _values;

    /**
     * Extra context that was provided to the validatable
     */
    protected Map<String, Object> _contextValues;

    /**
     * Validation exception (if the validation engine was unable to run the edit)
     */
    protected ValidationException _validationException;

    /**
     * Content of the redirected output, can be used to add log messages from the edit's logic using the <b>out &lt;&lt; 'my message'</b> notation
     */
    protected List<String> _log;

    /**
     * Constructor.
     * <p/>
     * Created on Aug 8, 2011 by depryf
     * @param lineNum line number
     * @param idx assertion index (useful for loops)
     * @param type expected assertion type (PASS or FAIL)
     * @param success whether the assertion was successful or not
     * @param failure rule failure (non-null only if the edit actually failed)
     * @param values data that was validated
     * @param contextValues values of the extra context used for the validation, if any
     * @param exc validation exception (if the validation engine was unable to run the edit
     * @param f asserted failing properties (optional)
     * @param l the content of the redirected output (System.out); might be empty but never null
     */
    public RuleTestResult(int lineNum, int idx, AssertionType type, boolean success, RuleFailure failure, Object values, Map<String, Object> contextValues, ValidationException exc, Set<String> f, List<String> l) {
        _lineNumber = lineNum;
        _assertionIndex = idx;
        _assertionType = type;
        _isSuccess = success;
        _ruleFailure = failure;
        _values = values;
        _contextValues = contextValues;
        _validationException = exc;
        _assertedFailingProperties = f;
        _log = l;
    }

    /**
     * Getter.
     * <p/>
     * Created on Aug 8, 2011 by depryf
     * @return the test's line number (1-based)
     */
    public Integer getLineNumber() {
        return _lineNumber;
    }

    /**
     * Getter.
     * <p/>
     * Created on Aug 8, 2011 by depryf
     * @return the test's assertion index (1-based)
     */
    public Integer getAssertionIndex() {
        return _assertionIndex;
    }

    /**
     * Getter.
     * <p/>
     * Created on Oct 2, 2011 by Fabian
     * @return the expected assertion type (PASS OR FAIL)
     */
    public AssertionType getAssertionType() {
        return _assertionType;
    }

    /**
     * Getter.
     * Created on Oct 7, 2011 by depryf
     * @return the asserted failing properties, might be non-null only for assertion type FAIL
     */
    public Set<String> getAssertedFailingProperties() {
        return _assertedFailingProperties;
    }

    /**
     * Getter.
     * <p/>
     * Created on Aug 8, 2011 by depryf
     * @return whether the test was successful or not
     */
    public Boolean isSuccess() {
        return _isSuccess;
    }

    /**
     * Getter.
     * <p/>
     * Created on Oct 2, 2011 by Fabian
     * @return the rule failure (non-null only if the rule actually failed)
     */
    public RuleFailure getRuleFailure() {
        return _ruleFailure;
    }

    /**
     * Getter.
     * <p/>
     * Created on Oct 2, 2011 by Fabian
     * @return the values of the object that has been validated
     */
    public Object getValues() {
        return _values;
    }

    /**
     * Getter.
     * <p/>
     * Created on Oct 2, 2011 by Fabian
     * @return the values of the extra context used during the validation
     */
    public Map<String, Object> getContextValues() {
        return _contextValues;
    }

    /**
     * Getter.
     * <p/>
     * Created on Oct 2, 2011 by Fabian
     * @return the validation exception (non-null only if the validation engine was unable to run the edit)
     */
    public ValidationException getValidationException() {
        return _validationException;
    }

    /**
     * Getter.
     * <p/>
     * The output contains the log messages from the edit's logic using the <b>out &lt;&lt; 'my message'</b> notation
     * <p/>
     * Created on Nov 1, 2011 by depryf
     * @return the content of the redirected output (System.out)
     */
    public List<String> getOutput() {
        return _log;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RuleTestResult other = (RuleTestResult)obj;
        if (_assertionIndex == null) {
            if (other._assertionIndex != null)
                return false;
        }
        else if (!_assertionIndex.equals(other._assertionIndex))
            return false;
        if (_lineNumber == null) {
            if (other._lineNumber != null)
                return false;
        }
        else if (!_lineNumber.equals(other._lineNumber))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + ((_assertionIndex == null) ? 0 : _assertionIndex.hashCode());
        result = prime * result + ((_lineNumber == null) ? 0 : _lineNumber.hashCode());
        return result;
    }

    @Override
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public String toString() {
        StringBuilder buf = new StringBuilder();

        buf.append("Line ").append(_lineNumber).append(" (Iteration #").append(_assertionIndex).append("): ");
        if (_validationException != null)
            buf.append("ENGINE EXCEPTION - ").append(_validationException.getMessage());
        else if (_ruleFailure != null && _ruleFailure.getGroovyException() != null)
            buf.append("EDIT EXCEPTION - ").append(_ruleFailure.getGroovyException().getMessage());
        else if (_isSuccess)
            buf.append("SUCCESS");
        else if (_assertionType == AssertionType.PASS)
            buf.append("FAILURE - ").append("expected edit to PASS but it FAILED");
        else if (_ruleFailure == null)
            buf.append("FAILURE - ").append("expected edit to FAIL but it PASSED");
        else { // failures must be because of the asserted failing properties
            Set<String> propertiesNotFound = new HashSet<>();
            for (String prop : _ruleFailure.getProperties())
                if (!_assertedFailingProperties.contains(prop))
                    propertiesNotFound.add(prop);
            buf.append("FAILURE - ").append("expected following ");
            if (propertiesNotFound.size() == 1)
                buf.append("property");
            else
                buf.append("properties");
            buf.append("to be in the failing properties, but didn't find it; got ").append(_ruleFailure.getProperties());
        }

        return buf.toString();
    }
}
