/*
 * Copyright (C) 2007 Information Management Services, Inc.
 */
package com.imsweb.validation.entities;

import java.util.Date;
import java.util.Objects;

/**
 * A <code>RuleHistory</code> represents a single history event for a given rule.
 * <p/>
 * Created on Nov 9, 2007 by depryf
 */
public class RuleHistory {

    /**
     * DB ID for this rule history
     */
    protected Long _ruleHistoryId;

    /**
     * Message (comment) for this rule history
     */
    protected String _message;

    /**
     * Version for this change
     */
    protected ValidatorVersion _version;

    /**
     * Username responsible for this rule history
     */
    protected String _username;

    /**
     * <code>Date</code> when this rule history was added
     */
    protected Date _date;

    /**
     * Squish reference ID
     */
    protected String _reference;

    /**
     * <code>Rule</code> for which this history is for
     */
    protected Rule _rule;

    /**
     * A String representation of the old rule values for this history entry; not used by the framework
     */
    protected String _oldRule;

    /**
     * A String representation of the new rule values for this history entry; not used by the framework
     */
    protected String _newRule;

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

    public String getOldRule() {
        return _oldRule;
    }

    public void setOldRule(String oldRule) {
        _oldRule = oldRule;
    }

    public String getNewRule() {
        return _newRule;
    }

    public void setNewRule(String newRule) {
        _newRule = newRule;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RuleHistory)) return false;
        RuleHistory that = (RuleHistory)o;
        if (_ruleHistoryId != null && that._ruleHistoryId != null)
            return Objects.equals(_ruleHistoryId, that._ruleHistoryId);
        return Objects.equals(_message, that._message) &&
                Objects.equals(_version, that._version) &&
                Objects.equals(_username, that._username) &&
                Objects.equals(_date, that._date) &&
                Objects.equals(_reference, that._reference) &&
                Objects.equals(_rule, that._rule);
    }

    @Override
    public int hashCode() {
        if (_ruleHistoryId != null)
            return Objects.hash(_ruleHistoryId);
        return Objects.hash(_message, _version, _username, _date, _reference, _rule);
    }
}
