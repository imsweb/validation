/*
 * Copyright (C) 2007 Information Management Services, Inc.
 */
package com.imsweb.validation.entities;

import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * A <code>RuleHistory</code> represents a single history event for a given rule.
 * <p/>
 * Created on Nov 9, 2007 by depryf
 */
public class RuleHistory {

    /** DB ID for this rule history */
    protected Long _ruleHistoryId;

    /** Message (comment) for this rule histroy */
    protected String _message;

    /** Version for this change */
    protected ValidatorVersion _version;

    /** Username responsible for this rule history */
    protected String _username;

    /** <code>Date</code> when this rule history was added */
    protected Date _date;

    /** Squish reference ID */
    protected String _reference;

    /** <code>Rule</code> for which this history is for */
    protected Rule _rule;

    /**
     * Getter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @return change database ID
     */
    public Long getRuleHistoryId() {
        return _ruleHistoryId;
    }

    /**
     * Setter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @param id change database ID
     */
    public void setRuleHistoryId(Long id) {
        _ruleHistoryId = id;
    }

    /**
     * Getter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @return rule
     */
    public Rule getRule() {
        return _rule;
    }

    /**
     * Setter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @param rule rule
     */
    public void setRule(Rule rule) {
        _rule = rule;
    }

    /**
     * Getter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @return change version
     */
    public ValidatorVersion getVersion() {
        return _version;
    }

    /**
     * Setter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @param version change version
     */
    public void setVersion(ValidatorVersion version) {
        _version = version;
    }

    /**
     * Getter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @return change message
     */
    public String getMessage() {
        return _message;
    }

    /**
     * Setter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @param message change message
     */
    public void setMessage(String message) {
        _message = message;
    }

    /**
     * Getter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @return change username
     */
    public String getUsername() {
        return _username;
    }

    /**
     * Setter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @param username chagne username
     */
    public void setUsername(String username) {
        _username = username;
    }

    /**
     * Getter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @return change date
     */
    public Date getDate() {
        return _date;
    }

    /**
     * Setter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @param date change date
     */
    public void setDate(Date date) {
        _date = date;
    }

    /**
     * Getter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @return change reference
     */
    public String getReference() {
        return _reference;
    }

    /**
     * Setter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @param reference change reference
     */
    public void setReference(String reference) {
        _reference = reference;
    }

    /* (non-Javadoc)
     * 
     * Created on Nov 6, 2007 by depryf
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof RuleHistory))
            return false;
        RuleHistory oth = (RuleHistory)other;
        if (_rule == null)
            return new EqualsBuilder().append(_date, oth.getDate()).append(_message, oth.getMessage()).isEquals();
        return new EqualsBuilder().append(_rule.getId(), oth.getRule().getId()).append(_date, oth.getDate()).append(_message, oth.getMessage()).isEquals();
    }

    /* (non-Javadoc)
     * 
     * Created on Nov 6, 2007 by depryf
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        if (_rule == null)
            return new HashCodeBuilder().append(_date).append(_message).toHashCode();
        return new HashCodeBuilder().append(_rule.getId()).append(_date).append(_message).toHashCode();
    }
}
