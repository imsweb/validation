/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation.entities;

/**
 * A context entry is one context variable in a {@link Validator} object.
 * <p/>
 * Created on Apr 5, 2011 by depryf
 */
public class ContextEntry {

    /** DB ID for this context entry */
    protected Long _contextEntryId;

    /** Context key (must be unique in the entire validation engine) */
    protected String _key;

    /** Context expression */
    protected String _expression;

    /** Parent <code>Validator</code> */
    protected Validator _validator;

    /** Context type ("java" or "groovy") */
    protected String _type;

    /**
     * Getter for the context entry persistence ID. Must be unique within the validation engine.
     * <p/>
     * In a system persisting the context entries in a database, that would represent a database primary key.
     * In a system using only memory objects loaded from XML, it should be assigned using the
     * getNextContextEntrySequence() method in <code>ValidatorServices</code>.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return the contextEntryId persistence ID
     */
    public Long getContextEntryId() {
        return _contextEntryId;
    }

    /**
     * Setter for the context entry persistence ID. Must be unique within the validation engine.
     * <p/>
     * In a system persisting the context entries in a database, that would represent a database primary key.
     * In a system using only memory objects loaded from XML, it should be assigned using the
     * getNextContextEntrySequence() method in <code>ValidatorServices</code>.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param contextEntryId rule persistence ID
     */
    public void setContextEntryId(Long contextEntryId) {
        _contextEntryId = contextEntryId;
    }

    /**
     * Getter for the context key.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @return context key
     */
    public String getKey() {
        return _key;
    }

    /**
     * Setter for the context key.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param key context key
     */
    public void setKey(String key) {
        this._key = key;
    }

    /**
     * Getter for the expression.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @return expression
     */
    public String getExpression() {
        return _expression;
    }

    /**
     * Setter for the expression.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param expression expression
     */
    public void setExpression(String expression) {
        this._expression = expression;
    }

    /**
     * Getter for the validator.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @return the <code>Validator</code>
     */
    public Validator getValidator() {
        return _validator;
    }

    /**
     * Setter for the validator.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param validator the <code>Validator</code>
     */
    public void setValidator(Validator validator) {
        this._validator = validator;
    }

    /**
     * Getter for the type.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @return the type
     */
    public String getType() {
        return _type;
    }

    /**
     * Setter for the type.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param type the type
     */
    public void setType(String type) {
        this._type = type;
    }

    /* (non-Javadoc)
     * 
     * Created on Nov 4, 2011 by depryf
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ContextEntry [" + _key + " in " + _validator.getId() + "]";
    }

    /* (non-Javadoc)
     * 
     * Created on Nov 8, 2011 by depryf
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ContextEntry other = (ContextEntry)obj;
        if (_contextEntryId == null) {
            if (other._contextEntryId != null)
                return false;
        }
        else if (!_contextEntryId.equals(other._contextEntryId))
            return false;
        return true;
    }

    /* (non-Javadoc)
     * 
     * Created on Nov 8, 2011 by depryf
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + ((_contextEntryId == null) ? 0 : _contextEntryId.hashCode());
        return result;
    }

}
