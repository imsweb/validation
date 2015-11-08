/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation.entities;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A standalone set is a ... set of edits. It is independent of a particular edits XML file, it uses
 * edit IDs to reference the edits. It also requires the validator ID.
 * <p/>
 * It allows inclusions and exclusions and provides a utility method to use those inclusions/exclusions: <b>needToInclude()</b>.
 * <p/>
 * Created on Apr 5, 2011 by depryf
 */
public class StandaloneSet {

    /**
     * Set ID
     */
    protected String _id;

    /**
     * Set name
     */
    protected String _name;

    /**
     * Set description
     */
    protected String _description;

    /**
     * Inclusions
     */
    protected Map<String, List<String>> _inclusions;

    /**
     * Exclusions
     */
    protected Map<String, List<String>> _exclusions;

    /**
     * All validator IDs referenced in the sets
     */
    protected Set<String> _referencedValidatorIds;

    /**
     * Constructor.
     */
    public StandaloneSet() {
        _referencedValidatorIds = new HashSet<>();
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
     * Getter for the inclusions.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @return inclusions (list of edit ID per validaor ID)
     */
    public Map<String, List<String>> getInclusions() {
        return _inclusions;
    }

    /**
     * Setter for the inclusions.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param inclusions inclusions (list of edit ID per validaor ID)
     */
    public void setInclusions(Map<String, List<String>> inclusions) {
        this._inclusions = inclusions;
    }

    /**
     * Getter for the exclusions.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @return exclusions (list of edit ID per validaor ID)
     */
    public Map<String, List<String>> getExclusions() {
        return _exclusions;
    }

    /**
     * Setter for the exclusions.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param exclusions exclusions (list of edit ID per validaor ID)
     */
    public void setExclusions(Map<String, List<String>> exclusions) {
        this._exclusions = exclusions;
    }

    /**
     * Adds a referenced validator ID.
     * @param validatorId validator ID
     */
    public void addReferencedValidatorId(String validatorId) {
        _referencedValidatorIds.add(validatorId);
    }

    /**
     * Returns all the referenced validator ID.
     * @return all the referenced validator ID; maybe empty but never null
     */
    public Set<String> getReferencedValidatorIds() {
        return _referencedValidatorIds;
    }

    /**
     * Returns true if the edit with the given ID from the given validator ID should be included or not, according to this set.
     * <p/>
     * The logic is:
     * <ol>
     * <li>include if inclusions are null/empty (meaning include everything), or if the list contains the edit ID</li>
     * <li>if edit is included in step one, but the exclusions list contains the edit ID, then return false</li>
     * <li>return true</li>
     * <ol>
     * <p/>
     * Created on Dec 9, 2010 by depryf
     * @param validatorId validator ID
     * @param editId edit ID
     * @return true if the edit with the given ID from the given validator ID should be included or not, according to this set
     */
    public boolean needToInclude(String validatorId, String editId) {
        List<String> inclusions = _inclusions.get(validatorId);
        List<String> exclusions = _exclusions.get(validatorId);

        boolean include = inclusions == null || inclusions.isEmpty() || inclusions.contains(editId);
        if (exclusions != null && exclusions.contains(editId))
            include = false;

        return include;
    }

    /* (non-Javadoc)
     * 
     * Created on Apr 5, 2011 by depryf
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + ((_id == null) ? 0 : _id.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * 
     * Created on Apr 5, 2011 by depryf
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
        StandaloneSet other = (StandaloneSet)obj;
        if (_id == null) {
            if (other._id != null)
                return false;
        }
        else if (!_id.equals(other._id))
            return false;
        return true;
    }
}
