/*
 * Copyright (C) 2007 Information Management Services, Inc.
 */
package com.imsweb.validation.entities;

import java.util.Date;
import java.util.Objects;

/**
 * A <code>DeletedRuleHistory</code> represents a single history event for a deleted rule within a validator.
 */
public class DeletedRuleHistory {

    /**
     * DB ID for this rule history
     */
    protected Long _ruleHistoryId;

    /**
     * Deleted rule ID
     */
    protected String _deletedRuleId;

    /**
     * Deleted rule name
     */
    protected String _deletedRuleName;

    /**
     * Message (comment) for this rule histroy
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
     * <code>Validator</code> for which this deleted history is for
     */
    protected Validator _validator;

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
     * @return validator
     */
    public Validator getValidator() {
        return _validator;
    }

    /**
     * Setter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @param validator validator
     */
    public void setValidator(Validator validator) {
        _validator = validator;
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
     * @param message message
     */
    public void setMessage(String message) {
        _message = message;
    }

    /**
     * Getter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @return delete rule ID
     */
    public String getDeletedRuleId() {
        return _deletedRuleId;
    }

    /**
     * Setter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @param deletedRuleId deleted rule ID
     */
    public void setDeletedRuleId(String deletedRuleId) {
        _deletedRuleId = deletedRuleId;
    }

    /**
     * Getter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @return delete rule name
     */
    public String getDeletedRuleName() {
        return _deletedRuleName;
    }

    /**
     * Setter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @param deletedRuleName deleted rule name
     */
    public void setDeletedRuleName(String deletedRuleName) {
        _deletedRuleName = deletedRuleName;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeletedRuleHistory)) return false;
        DeletedRuleHistory that = (DeletedRuleHistory)o;
        if (_ruleHistoryId != null && that._ruleHistoryId != null)
            return Objects.equals(_ruleHistoryId, that._ruleHistoryId);
        return Objects.equals(_deletedRuleId, that._deletedRuleId) &&
                Objects.equals(_deletedRuleName, that._deletedRuleName) &&
                Objects.equals(_message, that._message) &&
                Objects.equals(_version, that._version) &&
                Objects.equals(_username, that._username) &&
                Objects.equals(_date, that._date) &&
                Objects.equals(_reference, that._reference) &&
                Objects.equals(_validator, that._validator);
    }

    @Override
    public int hashCode() {
        if (_ruleHistoryId != null)
            return Objects.hash(_ruleHistoryId);
        return Objects.hash(_deletedRuleId, _deletedRuleName, _message, _version, _username, _date, _reference, _validator);
    }
}
