/*
 * Copyright (C) 2011 Information Management Services, Inc.
 */
package com.imsweb.validation.internal;

import org.codehaus.groovy.control.CompilationFailedException;

import groovy.lang.Binding;
import groovy.lang.Script;

import com.imsweb.validation.ConstructionException;
import com.imsweb.validation.ValidationException;
import com.imsweb.validation.ValidationServices;
import com.imsweb.validation.entities.Condition;
import com.imsweb.validation.entities.Validatable;

/**
 * Created on Jun 28, 2011 by depryf
 * @author depryf
 */
public class ExecutableCondition {

    // corresponding condition
    private Condition _condition;

    // ID
    private String _id;

    // internal ID
    private Long _internalId;

    // java-path for this rule
    private String _javaPath;

    // groovy script to execute
    private Script _script;

    // lock used for executing groovy scripts (since their interaction with the Binding objects is not thread-safe)
    private final Object _scriptLock = new Object();

    /**
     * Constructor.
     * <p/>
     * Created on Jun 28, 2011 by depryf
     * @param condition the <code>Condition</code> on which this executable ruleset is based one
     */
    public ExecutableCondition(Condition condition) throws ConstructionException {
        _condition = condition;
        _id = condition.getId();
        _internalId = condition.getConditionId();
        _javaPath = condition.getJavaPath();

        try {
            _script = ValidationServices.getInstance().compileExpression(condition.getExpression());
        }
        catch (CompilationFailedException e) {
            _script = null;
            throw new ConstructionException("Unable to compile expression for condition " + _condition.getId(), e);
        }
    }

    /**
     * Constructor.
     * <p/>
     * Created on Oct 7, 2011 by depryf
     * @param condition the <code>ExecutableCondition</code> on which this executable ruleset is based one
     */
    public ExecutableCondition(ExecutableCondition condition) {
        _condition = condition._condition;
        _id = condition._id;
        _internalId = condition._internalId;
        _javaPath = condition._javaPath;
        _script = condition._script;
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
        _id = id;
    }

    /**
     * @return Returns the internalId.
     */
    public Long getInternalId() {
        return _internalId;
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
     * Stter for the java path.
     * <p/>
     * Created on Jun 30, 2011 by depryf
     * @param javaPath java path
     */
    public void setJavaPath(String javaPath) {
        _javaPath = javaPath;
    }

    /**
     * Setter for the condition
     * <p/>
     * Created on Jun 30, 2011 by depryf
     * @param expression expression
     */
    public void setExpression(String expression) throws ConstructionException {
        try {
            _script = ValidationServices.getInstance().compileExpression(expression);
        }
        catch (CompilationFailedException e) {
            throw new ConstructionException("Unable to compile expression for condition " + _condition.getId(), e);
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
     */
    public boolean check(Validatable validatable, Binding binding) throws ValidationException {
        if (_script == null)
            return true;

        boolean success;

        synchronized (_scriptLock) {
            _script.setBinding(binding);
            try {
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
        }

        return success;
    }
}
