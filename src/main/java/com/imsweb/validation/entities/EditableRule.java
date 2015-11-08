/*
 * Copyright (C) 2011 Information Management Services, Inc.
 */
package com.imsweb.validation.entities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * An <code>EditableRule</code> is a wrapper for a <code>Rule<code> that needs to be added/updated/deleted.
 * <p/>
 * Note that this class was made serializable for SEER*DMS, but this will be removed eventually...
 * Created on Jun 29, 2011 by depryf
 */
public class EditableRule implements Serializable {

    /**
     * Class UID
     */
    private static final long serialVersionUID = 1L;

    protected Long _ruleId;

    protected String _id;

    protected String _validatorId;

    protected String _name;

    protected String _javaPath;

    protected Set<String> _conditions;

    protected Boolean _useAndForConditions;

    protected String _category;

    protected String _message;

    protected Integer _severity;

    protected String _expression;

    protected String _description;

    protected Boolean _ignored;

    protected Set<String> _dependencies;

    protected transient Set<RuleHistory> _histories;

    /**
     * Created on Jun 29, 2011 by depryf
     */
    public EditableRule() {
        _dependencies = new HashSet<>();
        _histories = new HashSet<>();
        _conditions = new HashSet<>();
        _useAndForConditions = Boolean.TRUE;
    }

    /**
     * Created on Jun 29, 2011 by depryf
     * @param rule parent rule
     */
    public EditableRule(Rule rule) {
        this();

        _ruleId = rule.getRuleId();
        _id = rule.getId();
        _validatorId = rule.getValidator().getId();
        _javaPath = rule.getJavaPath();
        _category = rule.getCategory();
        _conditions = rule.getConditions();
        _name = rule.getName();
        _message = rule.getMessage();
        _severity = rule.getSeverity();
        _expression = rule.getExpression();
        _description = rule.getDescription();
        _ignored = rule.getIgnored();
        _dependencies.addAll(rule.getDependencies());
        _histories.addAll(rule.getHistories());
    }

    /**
     * @return Returns the ruleId.
     */
    public Long getRuleId() {
        return _ruleId;
    }

    /**
     * @param ruleId The ruleId to set.
     */
    public void setRuleId(Long ruleId) {
        this._ruleId = ruleId;
    }

    /**
     * @return Returns the id.
     */
    public String getId() {
        return _id;
    }

    /**
     * @param id The id to set.
     */
    public void setId(String id) {
        this._id = id;
    }

    /**
     * @return Returns the javaPath.
     */
    public String getJavaPath() {
        return _javaPath;
    }

    /**
     * @param javaPath The javaPath to set.
     */
    public void setJavaPath(String javaPath) {
        this._javaPath = javaPath;
    }

    /**
     * @return Returns the category.
     */
    public String getCategory() {
        return _category;
    }

    /**
     * @param category The category to set.
     */
    public void setCategory(String category) {
        this._category = category;
    }

    /**
     * Getter for the condition operator.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return true if the conditions should be AND'ed, false otherwise
     */
    public Boolean getUseAndForConditions() {
        return _useAndForConditions;
    }

    /**
     * Setter for the condition operator.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param useAndForConditions true if the conditions should be AND'ed, false otherwise
     */
    public void setUseAndForConditions(Boolean useAndForConditions) {
        _useAndForConditions = useAndForConditions == null ? Boolean.TRUE : useAndForConditions;
    }

    /**
     * @return Returns the condition IDs.
     */
    public Set<String> getConditions() {
        return _conditions;
    }

    /**
     * @param conditions The condition IDs to set.
     */
    public void setConditions(Set<String> conditions) {
        if (conditions == null)
            this._conditions.clear();
        else
            this._conditions = conditions;
    }

    /**
     * @return Returns the validatorId.
     */
    public String getValidatorId() {
        return _validatorId;
    }

    /**
     * @param validatorId The validatorId to set.
     */
    public void setValidatorId(String validatorId) {
        this._validatorId = validatorId;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return _name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this._name = name;
    }

    /**
     * @return Returns the message.
     */
    public String getMessage() {
        return _message;
    }

    /**
     * @param message The message to set.
     */
    public void setMessage(String message) {
        this._message = message;
    }

    /**
     * @return Returns the severity.
     */
    public Integer getSeverity() {
        return _severity;
    }

    /**
     * @param severity The severity to set.
     */
    public void setSeverity(Integer severity) {
        this._severity = severity;
    }

    /**
     * @return Returns the expression.
     */
    public String getExpression() {
        return _expression;
    }

    /**
     * @param expression The expression to set.
     */
    public void setExpression(String expression) {
        this._expression = expression;
    }

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return _description;
    }

    /**
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this._description = description;
    }

    /**
     * @return Returns the ignored.
     */
    public Boolean getIgnored() {
        return _ignored;
    }

    /**
     * @param ignored The ignored to set.
     */
    public void setIgnored(Boolean ignored) {
        this._ignored = ignored;
    }

    /**
     * @return Returns the dependencies.
     */
    public Set<String> getDependencies() {
        return _dependencies;
    }

    /**
     * @param dependencies The dependencies to set.
     */
    public void setDependencies(Set<String> dependencies) {
        if (dependencies == null)
            this._dependencies.clear();
        else
            this._dependencies = dependencies;
    }

    /**
     * @return Returns the histories.
     */
    public Set<RuleHistory> getHistories() {
        return _histories;
    }

    /**
     * @param histories The histories to set.
     */
    public void setHistories(Set<RuleHistory> histories) {
        if (histories == null)
            this._histories.clear();
        else
            this._histories = histories;
    }

}
