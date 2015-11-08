/*
 * Copyright (C) 2011 Information Management Services, Inc.
 */
package com.imsweb.validation.internal;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.groovy.control.CompilationFailedException;

import groovy.lang.Binding;
import groovy.lang.Script;

import com.imsweb.validation.ConstructionException;
import com.imsweb.validation.ValidationException;
import com.imsweb.validation.ValidatorServices;
import com.imsweb.validation.entities.Condition;
import com.imsweb.validation.entities.Validatable;

/**
 * Created on Jun 28, 2011 by depryf
 * @author depryf
 */
public class ExecutableCondition {

    /**
     * Corresponding condition
     */
    private Condition _condition;

    /**
     * ID
     */
    private String _id;

    /**
     * Internal ID
     */
    private Long _internalId;

    /**
     * Validator internal ID
     */
    private Long _internalValidatorId;

    /**
     * Java-path for this rule
     */
    private String _javaPath;

    /**
     * Potential context keys (union of all the rule ones and the ruleset ones)
     */
    private Set<String> _contextKeys;

    /**
     * Groovy script to execute
     */
    private Script _script;

    /**
     * Constructor.
     * <p/>
     * Created on Jun 28, 2011 by depryf
     * @param condition the <code>Condition</code> on which this executable ruleset is based one
     * @throws ConstructionException
     */
    public ExecutableCondition(Condition condition) throws ConstructionException {
        _condition = condition;
        _id = condition.getId();
        _internalId = condition.getConditionId();
        _internalValidatorId = condition.getValidator() != null ? condition.getValidator().getValidatorId() : null;
        _javaPath = condition.getJavaPath();

        _contextKeys = new HashSet<>();
        _contextKeys.addAll(condition.getPotentialContextEntries());

        synchronized (this) {
            try {
                _script = ValidatorServices.getInstance().compileExpression(condition.getExpression());
            }
            catch (CompilationFailedException e) {
                _script = null;
                throw new ConstructionException("Unable to compile expression for condition " + _condition.getId(), e);
            }
        }
    }

    /**
     * Constructor.
     * <p/>
     * Created on Oct 7, 2011 by depryf
     * @param condition the <code>ExecutableCondition</code> on which this executable ruleset is based one
     * @throws ConstructionException
     */
    public ExecutableCondition(ExecutableCondition condition) throws ConstructionException {
        _condition = condition._condition;
        _id = condition._id;
        _internalId = condition._internalId;
        _internalValidatorId = condition._internalValidatorId;
        _javaPath = condition._javaPath;
        _contextKeys = condition._contextKeys;

        synchronized (this) {
            _script = condition._script;
        }
    }

    /**
     * Getter for the condition.
     * <p/>
     * Created on Jun 28, 2011 by depryf
     * @return condition
     */
    public Condition getCondition() {
        return _condition;
    }

    /**
     * Getter for the ID.
     * <p/>
     * Created on Jun 29, 2011 by depryf
     * @return the ID
     */
    public String getId() {
        return _id;
    }

    /**
     * @param id The ID to set.
     */
    public void setId(String id) {
        this._id = id;
    }

    /**
     * @return Returns the internalId.
     */
    public Long getInternalId() {
        return _internalId;
    }

    /**
     * @return Returns the internalValidatorId.
     */
    public Long getInternalValidatorId() {
        return _internalValidatorId;
    }

    /**
     * @param internalValidatorId The internalValidatorId to set.
     */
    public void setInternalValidatorId(Long internalValidatorId) {
        this._internalValidatorId = internalValidatorId;
    }

    /**
     * Getter for the java path.
     * <p/>
     * Created on Jun 29, 2011 by depryf
     * @return the java path
     */
    public String getJavaPath() {
        return _javaPath;
    }

    /**
     * Getter for the context keys.
     * <p/>
     * Created on Jun 29, 2011 by depryf
     * @return rule IDs
     */
    public Set<String> getContextKeys() {
        return _contextKeys;
    }

    /**
     * Stter for the java path.
     * <p/>
     * Created on Jun 30, 2011 by depryf
     * @param javaPath java path
     */
    public void setJavaPath(String javaPath) {
        this._javaPath = javaPath;
    }

    /**
     * Setter for the condition
     * <p/>
     * Created on Jun 30, 2011 by depryf
     * @param expression expression
     * @throws ConstructionException
     */
    public void setExpression(String expression) throws ConstructionException {
        synchronized (this) {
            try {
                _script = ValidatorServices.getInstance().compileExpression(expression);
            }
            catch (CompilationFailedException e) {
                _script = null;
                throw new ConstructionException("Unable to compile expression for confdition " + _condition.getId(), e);
            }
        }
    }

    @Override
    public String toString() {
        return _id;
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Executes the expression from this condition; return true if the condition passes, false otherwise.
     * <p/>
     * Created on Nov 6, 2007 by depryf
     * @param validatable <code>Validatable</code>
     * @param binding the Groovy binding to use
     * @return true if the condition passes, false otherwise
     * @throws ValidationException
     */
    public boolean check(Validatable validatable, Binding binding) throws ValidationException {
        return checkForGroovy(validatable, binding);

    }

    /**
     * Executes the Grovvy expression from this condition; return true if the condition
     * passes, false otherwise.
     * <p/>
     * Created on Nov 6, 2007 by depryf
     * @param validatable <code>Validatable</code>
     * @param binding the Groovy binding to use
     * @return evaluation result
     * @throws ValidationException
     */
    private synchronized boolean checkForGroovy(Validatable validatable, Binding binding) throws ValidationException {
        if (_script == null)
            return true;

        boolean success = false;

        try {
            _script.setBinding(binding);

            Object result = _script.run();
            if (result instanceof Boolean)
                success = (Boolean)result;
            else {
                throw new ValidationException("result is not a boolean");
            }
        }
        catch (Exception e) {
            StringBuilder buf = new StringBuilder();
            if (_id != null) {
                buf.append("Unable to execute condition '").append(_id).append("'");
                String validated = validatable.getDisplayId();
                if (validated != null)
                    buf.append(" on ").append(validatable.getDisplayId()).append(": ");
                else
                    buf.append(": ");
            }
            else
                buf.append("Unable to execute condition: ");
            buf.append(e.getMessage() == null ? "null reference" : e.getMessage());
            throw new ValidationException(buf.toString(), e);
        }
        finally {
            _script.setBinding(null);
        }

        return success;
    }
}
