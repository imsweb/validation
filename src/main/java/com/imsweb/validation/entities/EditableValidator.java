/*
 * Copyright (C) 2011 Information Management Services, Inc.
 */
package com.imsweb.validation.entities;

import java.util.HashSet;
import java.util.NavigableSet;
import java.util.Set;

/**
 * An <code>EditableValidator</code> is a wrapper for a <code>Validator<code> that needs to be added/updated/deleted.
 * <p/>
 * Created on Jul 6, 2011 by depryf
 */
public class EditableValidator {

    /**
     * Internal ID
     */
    protected Long _validatorId;

    /**
     * ID
     */
    protected String _id;

    /**
     * Name
     */
    protected String _name;

    /**
     * Hash value for this validator
     */
    protected String _hash;

    /**
     * Releases for this validator, sorted from first release (oldest) to last one (newest)
     */
    protected NavigableSet<ValidatorRelease> _releases;

    /**
     * Set of <code>Category</code>, these should not be modified, use the updateCategory() method instead
     */
    protected Set<Category> _categories;

    /**
     * Set of <code>Condition</code>, these should not be modified, use the updateCondition() method instead
     */
    protected Set<Condition> _conditions;

    /**
     * Raw contexts; these should not be modified, use the updateContext() method instead
     */
    protected Set<ContextEntry> _rawContext;

    /**
     * Set of <code>Rules</code>, these should not be modified, use the updateRule() method instead
     */
    protected Set<Rule> _rules;

    /**
     * Default Constructor.
     * <p/>
     * Created on Jul 6, 2011 by depryf
     */
    public EditableValidator() {
        _categories = new HashSet<>();
        _conditions = new HashSet<>();
        _rawContext = new HashSet<>();
        _rules = new HashSet<>();
    }

    /**
     * Constructor.
     * <p/>
     * Created on Jul 6, 2011 by depryf
     * @param v parent validator
     */
    public EditableValidator(Validator v) {
        this();

        _validatorId = v.getValidatorId();
        _id = v.getId();
        _name = v.getName();
        _hash = v.getHash();
        _releases = v.getReleases();
        _categories = v.getCategories();
        _conditions = v.getConditions();
        _rawContext = v.getRawContext();
        _rules = v.getRules();
    }

    /**
     * @return Returns the validatorId.
     */
    public Long getValidatorId() {
        return _validatorId;
    }

    /**
     * @param validatorId The validatorId to set.
     */
    public void setValidatorId(Long validatorId) {
        this._validatorId = validatorId;
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
     * @return Returns the hash.
     */
    public String getHash() {
        return _hash;
    }

    /**
     * @param hash The hash to set.
     */
    public void setHash(String hash) {
        this._hash = hash;
    }

    /**
     * @return Returns the releases.
     */
    public NavigableSet<ValidatorRelease> getReleases() {
        return _releases;
    }

    /**
     * @param releases The releases to set.
     */
    public void setReleases(NavigableSet<ValidatorRelease> releases) {
        this._releases = releases;
    }

    /**
     * @return Returns the categories.
     */
    public Set<Category> getCategories() {
        return _categories;
    }

    /**
     * @param categories The categories to set.
     */
    public void setCategories(Set<Category> categories) {
        this._categories = categories;
    }

    /**
     * @return Returns the conditions.
     */
    public Set<Condition> getConditions() {
        return _conditions;
    }

    /**
     * @param conditions The conditions to set.
     */
    public void setConditions(Set<Condition> conditions) {
        this._conditions = conditions;
    }

    /**
     * @return Returns the rawContext.
     */
    public Set<ContextEntry> getRawContext() {
        return _rawContext;
    }

    /**
     * @param rawContext The rawContext to set.
     */
    public void setRawContext(Set<ContextEntry> rawContext) {
        this._rawContext = rawContext;
    }

    /**
     * @return Returns the rules.
     */
    public Set<Rule> getRules() {
        return _rules;
    }

    /**
     * @param rules The rules to set.
     */
    public void setRules(Set<Rule> rules) {
        this._rules = rules;
    }
}
