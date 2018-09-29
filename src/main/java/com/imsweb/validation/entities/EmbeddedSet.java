/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation.entities;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * An embedded set is a ... set of edits provided within a given validator; edit IDs to reference the edits.
 * <p/>
 * It allows inclusions and exclusions and provides a utility method to use those inclusions/exclusions: <b>needToInclude()</b>.
 * <p/>
 * Created on Apr 5, 2011 by depryf
 */
public class EmbeddedSet {

    /**
     * DB ID for this set
     */
    protected Long _setId;

    /**
     * Set ID
     */
    protected String _id;

    /**
     * Set name
     */
    protected String _name;

    /**
     * Set tag
     */
    protected String _tag;

    /**
     * Validator under which this set is registered
     */
    protected Validator _validator;

    /**
     * Set description
     */
    protected String _description;

    /**
     * Inclusions
     */
    protected Set<String> _inclusions;

    /**
     * Exclusions
     */
    protected Set<String> _exclusions;

    /**
     * Ignored flag; unlike the rule ignore flags, the engine doesn't take into account the embedded set flags when executing rules.
     */
    protected Boolean _ignored;

    /**
     * The default conditions for this set; this field is not supported in the XML and can only be used/populated programmatically...
     */
    protected Set<String> _defaultConditions;

    /**
     * Constructor.
     */
    public EmbeddedSet() {
        _ignored = Boolean.FALSE;
        _defaultConditions = new HashSet<>();
    }

    /**
     * Getter for the set persistence ID. Must be unique within the validation engine.
     * <p/>
     * In a system persisting the sets in a database, that would represent a database primary key.
     * In a system using only memory objects loaded from XML, it should be assigned using the
     * getNextSetSequence() method in <code>ValidationServices</code>.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return the category persistence ID
     */
    public Long getSetId() {
        return _setId;
    }

    /**
     * Setter for the set persistence ID. Must be unique within the validation engine.
     * <p/>
     * In a system persisting the sets in a database, that would represent a database primary key.
     * In a system using only memory objects loaded from XML, it should be assigned using the
     * getNextSetSequence() method in <code>ValidationServices</code>.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param setId category persistence ID
     */
    public void setSetId(Long setId) {
        _setId = setId;
    }

    /**
     * Getter for the set ID.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @return set ID
     */
    public String getId() {
        return _id;
    }

    /**
     * Setter for the set ID.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param id set ID
     */
    public void setId(String id) {
        this._id = id;
    }

    /**
     * Getter for the set name.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @return set name
     */
    public String getName() {
        return _name;
    }

    /**
     * Setter for the set name.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param name set name
     */
    public void setName(String name) {
        this._name = name;
    }

    /**
     * Getter for the set tag.
     * <p/>
     * Created on May 19, 2017 by depryf
     * @return set tag
     */
    public String getTag() {
        return _tag;
    }

    /**
     * Setter for the set tag.
     * <p/>
     * Created on May 19, 2017 by depryf
     * @param tag set tag
     */
    public void setTag(String tag) {
        _tag = tag;
    }

    /**
     * Getter for the set description.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @return set description
     */
    public String getDescription() {
        return _description;
    }

    /**
     * Setter for the set description.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param description set description
     */
    public void setDescription(String description) {
        this._description = description;
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
     * Getter for the inclusions.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @return inclusions (set of edit IDs)
     */
    public Set<String> getInclusions() {
        return _inclusions;
    }

    /**
     * Setter for the inclusions.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param inclusions inclusions (set of edit IDs)
     */
    public void setInclusions(Set<String> inclusions) {
        this._inclusions = inclusions;
    }

    /**
     * Getter for the exclusions.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @return exclusions (set of edit IDs)
     */
    public Set<String> getExclusions() {
        return _exclusions;
    }

    /**
     * Setter for the exclusions.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param exclusions exclusions (set of edit IDs)
     */
    public void setExclusions(Set<String> exclusions) {
        this._exclusions = exclusions;
    }

    /**
     * Getter for the ignored flag.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return true if the set is ignored
     */
    public Boolean getIgnored() {
        return _ignored;
    }

    /**
     * Setter for the ignored flag.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param ignored the ignored flag to set
     */
    public void setIgnored(Boolean ignored) {
        _ignored = ignored == null ? Boolean.FALSE : ignored;
    }

    /**
     * Getter for the default conditions.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return the default conditions
     */
    public Set<String> getDefaultConditions() {
        return _defaultConditions;
    }

    /**
     * Setter for the default conditions.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param defaultConditions the default conditions
     */
    public void setDefaultConditions(Set<String> defaultConditions) {
        _defaultConditions = defaultConditions;
    }

    /**
     * Returns true if the edit with the given ID should be included or not, according to this set.
     * <p/>
     * The logic is:
     * <ol>
     * <li>include if inclusions are null/empty (meaning include everything), or if the list contains the edit ID</li>
     * <li>if edit is included in step one, but the exclusions list contains the edit ID, then return false</li>
     * <li>return true</li>
     * <ol>
     * <p/>
     * Created on Dec 9, 2010 by depryf
     * @param editId edit ID
     * @return true if the edit with the given ID should be included or not, according to this set
     */
    public boolean needToInclude(String editId) {
        boolean include = _inclusions == null || _inclusions.isEmpty() || _inclusions.contains(editId);
        if (_exclusions != null && _exclusions.contains(editId))
            include = false;

        return include;
    }

    @Override
    public String toString() {
        return getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmbeddedSet that = (EmbeddedSet)o;
        if (_setId != null && that._setId != null)
            return Objects.equals(_setId, that._setId);
        return Objects.equals(_id, that._id);
    }

    @Override
    public int hashCode() {
        if (_setId != null)
            return Objects.hash(_setId);
        return Objects.hash(_id);
    }
}
