/*
 * Copyright (C) 2011 Information Management Services, Inc.
 */
package com.imsweb.validation.entities;


/**
 * An <code>EditableCondition</code> is a wrapper for a <code>Condition<code> that needs to be added/updated/deleted.
 * <p/>
 * Created on Jul 7, 2011 by depryf
 */
public class EditableCondition {

    /** Internal ID */
    protected Long _conditionId;

    /** ID */
    protected String _id;

    /** Name */
    protected String _name;

    /** Java path */
    protected String _javaPath;

    /** Groovy expression */
    protected String _expression;

    /** Description */
    protected String _description;

    /** Parent Validator ID */
    protected String _validatorId;

    /**
     * Constructor.
     * <p/>
     * Created on Jun 30, 2011 by depryf
     */
    public EditableCondition() {
    }

    /**
     * Constructor.
     * <p/>
     * Created on Jun 30, 2011 by depryf
     * @param condition <code>Condition</code>
     */
    public EditableCondition(Condition condition) {
        this();

        _conditionId = condition.getConditionId();
        _id = condition.getId();
        _validatorId = condition.getValidator().getId();
        _name = condition.getName();
        _javaPath = condition.getJavaPath();
        _expression = condition.getExpression();
        _description = condition.getDescription();
    }

    /**
     * Getter for the condition persistence ID. Must be unique within the validation engine.
     * <p/>
     * In a system persisting the rules in a database, that would represent a database primary key.
     * In a system using only memory objects loaded from XML, it should be assigned using the
     * getNextConditionSequence() method in <code>ValidationServices</code>.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return the condition persistence ID
     */
    public Long getConditionId() {
        return _conditionId;
    }

    /**
     * Setter for the condition persistence ID. Must be unique within the validation engine.
     * <p/>
     * In a system persisting the rulesets in a database, that would represent a database primary key.
     * In a system using only memory objects loaded from XML, it should be assigned using the
     * getNextConditionSequence() method in <code>ValidationServices</code>.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param conditionId condition persistence ID
     */
    public void setConditionId(Long conditionId) {
        _conditionId = conditionId;
    }

    /**
     * Getter for the ID. The ruleset ID must be unique within the validation engine.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return the ruleset ID
     */
    public String getId() {
        return _id;
    }

    /**
     * Setter for the ID. The ruleset ID must be unique within the validation engine.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param id the ruleset ID
     */
    public void setId(String id) {
        _id = id;
    }

    /**
     * Getter for the name.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return the ruleset name
     */
    public String getName() {
        return _name;
    }

    /**
     * Setter for the name.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param name the ruleset name
     */
    public void setName(String name) {
        _name = name;
    }

    /**
     * Getter for the java path.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return the ruleset java path
     */
    public String getJavaPath() {
        return _javaPath;
    }

    /**
     * Setter for the java path.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param path the java path, cannot be null or blank
     */
    public void setJavaPath(String path) {
        _javaPath = path;
    }

    /**
     * Getter for the expression (Groovy script).
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return the condition expression
     */
    public String getExpression() {
        return _expression;
    }

    /**
     * Setter for the condition (Groovy script).
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param expression the condition expression
     */
    public void setExpression(String expression) {
        _expression = expression;
    }

    /**
     * Getter for the description.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return the ruleset description
     */
    public String getDescription() {
        return _description;
    }

    /**
     * Setter for the description.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param description the ruleset description
     */
    public void setDescription(String description) {
        _description = description;
    }

    /**
     * Getter for the parent <code>Validator</code> ID.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return a <code>Validator</code> ID
     */
    public String getValidatorId() {
        return _validatorId;
    }

    /**
     * Setter for the parent <code>Valdidator</code> ID.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param validatorId the parent <code>Validator</code> ID.
     */
    public void setValidatorId(String validatorId) {
        _validatorId = validatorId;
    }
}
