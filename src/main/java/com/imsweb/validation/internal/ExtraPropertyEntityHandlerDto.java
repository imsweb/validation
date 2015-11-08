/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * This class is an internal class used to allow an edit to report an error on a particular entity. It can be useful
 * when looping over two entities and reporting an error on two particular ones.
 * <p/>
 * Created on Apr 5, 2011 by depryf
 * @author depryf
 */
public class ExtraPropertyEntityHandlerDto {

    /** Entity on which the error needs to be reported */
    private Object _entity;

    /** Properties on which the error needs to be reported */
    private Set<String> _properties;

    /**
     * Constructor.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param entity entity on which the error needs to be reported (cannot be null)
     * @param properties properties on which the error needs to be reported (can be null or empty)
     */
    public ExtraPropertyEntityHandlerDto(Object entity, String[] properties) {
        _entity = entity;
        if (properties != null && properties.length > 0)
            _properties = new HashSet<>(Arrays.asList(properties));
    }

    /**
     * Getter for the entity.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @return the entity
     */
    public Object getEntity() {
        return _entity;
    }

    /**
     * Getter for the properties.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @return the properties
     */
    public Set<String> getProperties() {
        return _properties;
    }

    /**
     * Setter for the entity.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param entity the entity to set (cannot be null)
     */
    public void setEntity(Object entity) {
        _entity = entity;
    }

    /**
     * Setter for the properties.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param properties the properties (can be null or empty)
     */
    public void setProperties(Set<String> properties) {
        _properties = properties;
    }

    /* (non-Javadoc)
     * 
     * Created on Apr 5, 2011 by depryf
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ExtraPropertyEntityHandlerDto))
            return false;
        ExtraPropertyEntityHandlerDto castOther = (ExtraPropertyEntityHandlerDto)other;
        return new EqualsBuilder().append(getEntity(), castOther.getEntity()).append(getProperties(), castOther.getProperties()).isEquals();
    }

    /* (non-Javadoc)
     * 
     * Created on Apr 5, 2011 by depryf
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getEntity()).append(getProperties()).toHashCode();
    }
}
