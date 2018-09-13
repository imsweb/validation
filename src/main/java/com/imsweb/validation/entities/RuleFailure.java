/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation.entities;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * This class represents a single rule failure returned by the validation engine.
 * <p/>
 * Created on Apr 5, 2011 by depryf
 */
public class RuleFailure {

    /**
     * Rule object (link to the parent).
     */
    protected Rule _rule;

    /**
     * Failure message (for translated edits, it might not be the message defined by the edit itself since messages can dynamically be added).
     */
    protected String _message;

    /**
     * Extra failure messages (used only by translated metafile edits); can be null or empty.
     */
    protected List<String> _extraErrorMessages;

    /**
     * Information messages (used only by translated metafile edits); can be null or empty.
     */
    protected List<String> _informationMessages;

    /**
     * Set of failing (formatted) properties
     */
    protected Set<String> _properties;

    /**
     * Identifier for the tumor that triggered this failure
     */
    protected Long _tumorIdentifier;

    /**
     * Original Groovy exception (null if no exception happened)
     */
    protected Throwable _groovyException;

    /**
     * The actual boolean result returned by the edit; it is possible for a translated edit to return true, but fail because the failing flag has been set
     * through a call to a context method. This field can be used to detect that case and allow an accurate comparison between the GeneditsPlus results and
     * the ones returned by this framework.
     */
    protected Boolean _originalResult;

    /**
     * Default constructor. This constructor is inteded for testing only; it by-passes any nullity checking!
     * <p/>
     * Created on Mar 2, 2010 by depryf
     */
    public RuleFailure() {
        // this is NOT calling the other constructors, this is on purpose...
    }

    /**
     * Constructor.
     * <p/>
     * Created on Mar 1, 2010 by depryf
     * @param rule <code>Rule</code>, can't be null
     * @param validatable <code>Validatable</code>, can't be null
     */
    public RuleFailure(Rule rule, Validatable validatable) {
        this(rule, rule.getMessage(), validatable);
    }

    /**
     * Constructor.
     * <p/>
     * Created on Mar 1, 2010 by depryf
     * @param rule <code>Rule</code>, can't be null
     * @param message error message
     * @param validatable <code>Validatable</code>, can't be null
     */
    public RuleFailure(Rule rule, String message, Validatable validatable) {
        this(rule, message, validatable, null);
    }

    /**
     * Constructor.
     * <p/>
     * Created on Mar 1, 2010 by depryf
     * @param rule <code>Rule</code>, can't be null
     * @param message error message
     * @param validatable <code>Validatable</code>, can't be null
     */
    public RuleFailure(Rule rule, String message, Validatable validatable, Throwable groovyException) {

        if (rule == null)
            throw new RuntimeException("Can't build a RuleFailure from a null rule!");
        if (validatable == null)
            throw new RuntimeException("Can't build a RuleFailure from a null validatable!");

        _rule = rule;
        _message = message == null ? "" : message; // avoid NPE on message...
        _properties = new HashSet<>(validatable.getPropertiesWithError());
        _tumorIdentifier = validatable.getCurrentTumorId();
        _groovyException = groovyException;
    }

    /**
     * Getter for the rule.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @return the accociated <code>Rule</code>
     */
    public Rule getRule() {
        return _rule;
    }

    /**
     * Setter for the rule.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param rule the associated <code>Rule</code>
     */
    public void setRule(Rule rule) {
        _rule = rule;
    }

    /**
     * Getter for the message.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @return the message
     */
    public String getMessage() {
        return _message;
    }

    /**
     * Setter for the message.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param message the message
     */
    public void setMessage(String message) {
        _message = message;
    }

    /**
     * Setter for the properties.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @return the properties
     */
    public Set<String> getProperties() {
        return _properties;
    }

    /**
     * Getter for the properties.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param properties the properties
     */
    public void setProperties(Set<String> properties) {
        _properties = properties;
    }

    /**
     * Getter for the tumor identifier.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @return tumor identifier
     */
    public Long getTumorIdentifier() {
        return _tumorIdentifier;
    }

    /**
     * Setter for the tumor identifier.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param tumorIdentifier tumor identifier
     */
    public void setTumorIdentifier(Long tumorIdentifier) {
        _tumorIdentifier = tumorIdentifier;
    }

    /**
     * Getter for the groovy exception.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @return groovy exception, null of no exception happened
     */
    public Throwable getGroovyException() {
        return _groovyException;
    }

    /**
     * Setter for the groovy exception.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param groovyException the groovy exception
     */
    public void setGroovyException(Throwable groovyException) {
        _groovyException = groovyException;
    }

    /**
     * Getter for the extra error messages
     * @return list of extra error messages, can be null or empty
     */
    public List<String> getExtraErrorMessages() {
        return _extraErrorMessages;
    }

    /**
     * Setter for the extra error messages
     * @param extraErrorMessages list of extra error messages to set
     */
    public void setExtraErrorMessages(List<String> extraErrorMessages) {
        _extraErrorMessages = extraErrorMessages;
    }

    /**
     * Getter for the information messages
     * @return list of information messages, can be null or empty
     */
    public List<String> getInformationMessages() {
        return _informationMessages;
    }

    /**
     * Setter for the information messages
     * @param informationMessages list of information messages to set
     */
    public void setInformationMessages(List<String> informationMessages) {
        _informationMessages = informationMessages;
    }

    /**
     * Getter for the original reslut
     * @return the original result, can be null if the edit threw an exception and didn't return anything
     */
    public Boolean getOriginalResult() {
        return _originalResult;
    }

    /**
     * Setter for the original result
     * @param originalResult original result to set
     */
    public void setOriginalResult(Boolean originalResult) {
        _originalResult = originalResult;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof RuleFailure))
            return false;

        RuleFailure otherResult = (RuleFailure)other;
        return new EqualsBuilder().append(_rule, otherResult.getRule()).append(_properties, otherResult.getProperties()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(_rule).append(_properties).toHashCode();
    }
}
