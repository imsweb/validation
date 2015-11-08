/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation.internal;

import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * This class is an internal class used to allow an edit to report or ignore an error on specific properties. It is mainly
 * used to ignore some properties like flags, that are not really part of the edit logic, but still used in it. It can also
 * bu used to force the engine to report an error on a property that would have not been parsed correctly upon initialization.
 * <p/>
 * Created on Apr 5, 2011 by depryf
 * @author depryf
 */
public class ExtraPropertyHandlerDto {

    /** The entities on which the properties should be ignored/forced */
    private Set<ExtraPropertyEntityHandlerDto> _forcedEntities;

    /** Properties to be forced */
    private Set<String> _forcedProperties;

    /** Properties to be ignored */
    private Set<String> _ignoredProperties;

    /**
     * Getter for the entities on which the properties should be ignored/forced.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @return set of <code>ExtraPropertyEntityHandlerDto</code>, maybe null or empty
     */
    public Set<ExtraPropertyEntityHandlerDto> getForcedEntities() {
        return _forcedEntities;
    }

    /**
     * Setter for the entities on which the properties should be ignored/forced.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param forcedEntities set of <code>ExtraPropertyEntityHandlerDto</code>, maybe null or empty
     */
    public void setForcedEntities(Set<ExtraPropertyEntityHandlerDto> forcedEntities) {
        _forcedEntities = forcedEntities;
    }

    /**
     * Getter for the forced properties.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @return forced properties
     */
    public Set<String> getForcedProperties() {
        return _forcedProperties;
    }

    /**
     * Setter for the forced properties.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param forcedProperties forced properties
     */
    public void setForcedProperties(Set<String> forcedProperties) {
        _forcedProperties = forcedProperties;
    }

    /**
     * Getter for the ignored properties.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @return ignored properties
     */
    public Set<String> getIgnoredProperties() {
        return _ignoredProperties;
    }

    /**
     * Setter for the ignored properties.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param ignoredProperties ignored properties
     */
    public void setIgnoredProperties(Set<String> ignoredProperties) {
        _ignoredProperties = ignoredProperties;
    }

    /* (non-Javadoc)
     * 
     * Created on Apr 5, 2011 by depryf
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ExtraPropertyHandlerDto))
            return false;
        ExtraPropertyHandlerDto castOther = (ExtraPropertyHandlerDto)other;
        return new EqualsBuilder().append(getForcedEntities(), castOther.getForcedEntities()).append(getForcedProperties(), castOther.getForcedProperties()).append(getIgnoredProperties(), castOther.getIgnoredProperties()).isEquals();
    }

    /* (non-Javadoc)
     * 
     * Created on Apr 5, 2011 by depryf
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getForcedEntities()).append(getForcedProperties()).append(getIgnoredProperties()).toHashCode();
    }
}
