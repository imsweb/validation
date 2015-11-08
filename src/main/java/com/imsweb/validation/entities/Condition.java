/*
 * Copyright (C) 2007 Information Management Services, Inc.
 */
package com.imsweb.validation.entities;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.groovy.control.CompilationFailedException;

import com.imsweb.validation.ConstructionException;
import com.imsweb.validation.ValidatorServices;

/**
 * A <code>Condition</code> allows a pre-condition to be set for one or several rules.
 * <p/>
 * Created on Nov 9, 2007 by depryf
 */
public class Condition {

    /**
     * DB ID for this condition
     */
    protected Long _conditionId;

    /**
     * Application ID (same as a display ID) for this condition
     */
    protected String _id;

    /**
     * Name for this condition
     */
    protected String _name;

    /**
     * Java path for this condition
     */
    protected String _javaPath;

    /**
     * Groovy expression for this condition
     */
    protected String _expression;

    /**
     * Description for this condition
     */
    protected String _description;

    /**
     * Validator under which this condition is registered
     */
    protected Validator _validator;

    /**
     * Set of properties contained in the expression
     */
    protected Set<String> _rawProperties;

    /**
     * Set of lookup IDs used in the expression
     */
    protected Set<String> _usedLookupIds;

    /**
     * Set of potiental context entries (they are potential because they might not all be context entries; but if a context entry is used, it will be in this list...
     */
    protected Set<String> _potentialContextEntries;

    /**
     * Constructor.
     * <p/>
     * Created on Nov 9, 2007 by depryf
     */
    public Condition() {
        _rawProperties = new HashSet<>();
        _usedLookupIds = new HashSet<>();
        _potentialContextEntries = new HashSet<>();
    }

    /**
     * Getter for the condition persistence ID. Must be unique within the validation engine.
     * <p/>
     * In a system persisting the conditions in a database, that would represent a database primary key.
     * In a system using only memory objects loaded from XML, it should be assigned using the
     * getNextConditionSequence() method in <code>ValidatorServices</code>.
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
     * In a system persisting the conditions in a database, that would represent a database primary key.
     * In a system using only memory objects loaded from XML, it should be assigned using the
     * getNextConditionSequence() method in <code>ValidatorServices</code>.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param conditionId condition persistence ID
     */
    public void setConditionId(Long conditionId) {
        _conditionId = conditionId;
    }

    /**
     * Getter for the ID. The condition ID must be unique within the validation engine.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return the condition ID
     */
    public String getId() {
        return _id;
    }

    /**
     * Setter for the ID. The condition ID must be unique within the validation engine.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param id the condition ID
     */
    public void setId(String id) {
        _id = id;
    }

    /**
     * Getter for the name.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return the condition name
     */
    public String getName() {
        return _name;
    }

    /**
     * Setter for the name.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param name the condition name
     */
    public void setName(String name) {
        _name = name;
    }

    /**
     * Getter for the java path.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return the condition java path
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
     * Setter for the expression (Groovy script).
     * <p/>
     * This method will compile the script and gather the used properties.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param expression the condition expression
     * @throws ConstructionException if the expression is not valid Groovy
     */
    public void setExpression(String expression) throws ConstructionException {
        _expression = expression;

        if (expression != null && !expression.trim().isEmpty()) {
            synchronized (this) {
                try {
                    ValidatorServices.getInstance().parseExpression("condition", _expression, _rawProperties, _potentialContextEntries, _usedLookupIds);
                }
                catch (CompilationFailedException e) {
                    throw new ConstructionException("Unable to parse condition " + getId(), e);
                }
            }
        }
    }

    /**
     * Getter for the description.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return the condition description
     */
    public String getDescription() {
        return _description;
    }

    /**
     * Setter for the description.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param description the condition description
     */
    public void setDescription(String description) {
        _description = description;
    }

    /**
     * Getter for the parent <code>Validator</code>.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return a <code>Validator</code>
     */
    public Validator getValidator() {
        return _validator;
    }

    /**
     * Setter for the parent <code>Valdidator</code>.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param validator the parent <code>Validator</code>.
     */
    public void setValidator(Validator validator) {
        _validator = validator;
    }

    /**
     * Getter for the properties used in the condition.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return the set of properties, maybe empty but never null
     */
    public Set<String> getRawProperties() {
        return _rawProperties;
    }

    /**
     * Getter for the lookup IDs used in the condition.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return the set of used lookup IDs, maybe empty but never null
     */
    public Set<String> getUsedLookupIds() {
        return _usedLookupIds;
    }

    /**
     * Getter for the potential context entries referenced in the condition.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return the set of potential context entries, maybe empty but never null
     */
    public Set<String> getPotentialContextEntries() {
        return _potentialContextEntries;
    }

    @Override
    public String toString() {
        return getId();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Condition))
            return false;
        Condition castOther = (Condition)other;
        if (getConditionId() != null)
            return new EqualsBuilder().append(getConditionId(), castOther.getConditionId()).isEquals();
        else
            return new EqualsBuilder().append(getId(), castOther.getId()).isEquals();
    }

    @Override
    public int hashCode() {
        if (getConditionId() != null)
            return new HashCodeBuilder().append(getConditionId()).toHashCode();
        else
            return new HashCodeBuilder().append(getId()).toHashCode();
    }
}
