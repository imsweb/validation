/*
 * Copyright (C) 2007 Information Management Services, Inc.
 */
package com.imsweb.validation.entities;

import java.util.HashSet;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * A <code>Validator</code> is a wrapper for a set of {@link Condition}.
 * <p/>
 * Created on Nov 9, 2007 by depryf
 */
public class Validator {

    /**
     * DB ID for this validator
     */
    protected Long _validatorId;

    /**
     * Application ID (same role as a display ID) for this validator
     */
    protected String _id;

    /**
     * Name for this validator
     */
    protected String _name;

    /**
     * Current raw version for this validator (corresponds to the latest release)
     */
    protected String _version;

    /**
     * Minimum version of the validation engine (using the SEER*Utils version since the engine itself is not versioned)
     */
    protected String _minEngineVersion;

    /**
     * From which file this validator was translated from (optional)
     */
    protected String _translatedFrom;

    /**
     * Releases for this vaildator, sorted from first release (oldest) to last one (newest)
     */
    protected NavigableSet<ValidatorRelease> _releases;

    /**
     * Hash value for this validator
     */
    protected String _hash;

    /**
     * Set of <code>Category</code> registered with this validator
     */
    protected Set<Category> _categories;

    /**
     * Set of <code>Condition</code> registered with this validator
     */
    protected Set<Condition> _conditions;

    /**
     * Set of <code>Rule</code> registered with this validator
     */
    protected Set<Rule> _rules;

    /**
     * Non-expanded context for this validator as it is persisted in the DB
     */
    protected Set<ContextEntry> _rawContext;

    /**
     * Deleted rule histories
     */
    protected Set<DeletedRuleHistory> _deletedRuleHistories;

    /**
     * Set of <code>EmbeddedSet</code> registered with this validator
     */
    protected Set<EmbeddedSet> _sets;

    /**
     * Constructor.
     * <p/>
     * Created on Nov 9, 2007 by depryf
     */
    public Validator() {
        _releases = new TreeSet<>();
        _rawContext = new HashSet<>();
        _deletedRuleHistories = new HashSet<>();
        _conditions = new HashSet<>();
        _categories = new HashSet<>();
        _rules = new HashSet<>();
        _sets = new HashSet<>();
    }

    /**
     * Getter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @return validator database ID
     */
    public Long getValidatorId() {
        return _validatorId;
    }

    /**
     * Setter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @param id validator database ID
     */
    public void setValidatorId(Long id) {
        _validatorId = id;
    }

    /**
     * Getter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @return validator ID
     */
    public String getId() {
        return _id;
    }

    /**
     * Setter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @param id validator ID
     */
    public void setId(String id) {
        _id = id;
    }

    /**
     * Getter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @return validator name
     */
    public String getName() {
        return _name;
    }

    /**
     * Setter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @param name validator name
     */
    public void setName(String name) {
        _name = name;
    }

    /**
     * Getter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @return validator current version
     */
    public String getVersion() {
        return _version;
    }

    /**
     * Setter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @param version validator current version
     */
    public void setVersion(String version) {
        _version = version;
    }

    /**
     * Getter.
     * @return the minimum version of the engine (actually of SEER*Utils) required for this validator to work properly
     */
    public String getMinEngineVersion() {
        return _minEngineVersion;
    }

    /**
     * Setter.
     * @param minEngineVersion the minimum version of the engine (actually of SEER*Utils) required for this validator to work properly
     */
    public void setMinEngineVersion(String minEngineVersion) {
        _minEngineVersion = minEngineVersion;
    }

    /**
     * Getter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @return translated from
     */
    public String getTranslatedFrom() {
        return _translatedFrom;
    }

    /**
     * Setter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @param translatedFrom translated from
     */
    public void setTranslatedFrom(String translatedFrom) {
        _translatedFrom = translatedFrom;
    }

    /**
     * Getter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @return validator releases
     */
    public NavigableSet<ValidatorRelease> getReleases() {
        return _releases;
    }

    /**
     * Setter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @param releases validator releases
     */
    public void setReleases(NavigableSet<ValidatorRelease> releases) {
        _releases = releases;
    }

    /**
     * Getter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @return validator hash
     */
    public String getHash() {
        return _hash;
    }

    /**
     * Setter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @param hash validator hash
     */
    public void setHash(String hash) {
        _hash = hash;
    }

    /**
     * Getter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @return validator raw contexts
     */
    public Set<ContextEntry> getRawContext() {
        return _rawContext;
    }

    /**
     * Returns the context entry corresponding to the provided key, null if not found.
     * <p/>
     * Created on Jun 13, 2011 by depryf
     * @param key context key
     * @return the context entry corresponding to the provided key, null if not found
     */
    public ContextEntry getRawContext(String key) {
        if (key == null)
            return null;

        for (ContextEntry entry : _rawContext)
            if (key.equalsIgnoreCase(entry.getKey()))
                return entry;

        return null;
    }

    /**
     * Setter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @param rawContext validator raw contexts
     */
    public void setRawContext(Set<ContextEntry> rawContext) {
        _rawContext = rawContext;
    }

    /**
     * Getter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @return the conditions
     */
    public Set<Category> getCategories() {
        return _categories;
    }

    /**
     * Setter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @param categories categories
     */
    public void setCategories(Set<Category> categories) {
        _categories = categories;
    }

    /**
     * Helper method to return the category corresponding to the passed ID.
     * The comparison is not case-sensitive.
     * <p/>
     * Created on Nov 9, 2007 by depryf
     * @param id category ID
     * @return a <code>Category</code>, null if none correspond to the passed ID.
     */
    public Category getCategory(String id) {
        if (id == null)
            return null;

        for (Category cat : _categories)
            if (id.equalsIgnoreCase(cat.getId()))
                return cat;

        return null;
    }

    /**
     * Getter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @return the conditions
     */
    public Set<Condition> getConditions() {
        return _conditions;
    }

    /**
     * Setter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @param conditions conditions
     */
    public void setConditions(Set<Condition> conditions) {
        _conditions = conditions;
    }

    /**
     * Helper method to return the condition corresponding to the passed ID.
     * The comparison is not case-sensitive.
     * <p/>
     * Created on Nov 9, 2007 by depryf
     * @param id condition ID
     * @return a <code>Condition</code>, null if none correspond to the passed ID.
     */
    public Condition getCondition(String id) {
        if (id == null)
            return null;

        for (Condition cond : _conditions)
            if (id.equalsIgnoreCase(cond.getId()))
                return cond;

        return null;
    }

    /**
     * Getter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @return the rules
     */
    public Set<Rule> getRules() {
        return _rules;
    }

    /**
     * Setter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @param rules rules
     */
    public void setRules(Set<Rule> rules) {
        _rules = rules;
    }

    /**
     * Helper method to return the rule corresponding to the passed ID.
     * The comparison is not case-sensitive.
     * <p/>
     * Created on Nov 9, 2007 by depryf
     * @param ruleId rule ID
     * @return a <code>Rule</code>, null if none correspond to the passed ID.
     */
    public Rule getRule(String ruleId) {
        if (ruleId == null)
            return null;

        for (Rule rule : _rules)
            if (ruleId.equalsIgnoreCase(rule.getId()))
                return rule;

        return null;
    }

    /**
     * Setter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @param deletedRuleHistories deleted rule histories
     */
    public void setDeletedRuleHistories(Set<DeletedRuleHistory> deletedRuleHistories) {
        _deletedRuleHistories = deletedRuleHistories;
    }

    /**
     * Getter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @return deleted rule histories
     */
    public Set<DeletedRuleHistory> getDeletedRuleHistories() {
        return _deletedRuleHistories;
    }

    /**
     * Getter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @return the sets
     */
    public Set<EmbeddedSet> getSets() {
        return _sets;
    }

    /**
     * Setter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @param sets sets
     */
    public void setSets(Set<EmbeddedSet> sets) {
        _sets = sets;
    }

    /**
     * Helper method to return the set corresponding to the passed ID.
     * The comparison is not case-sensitive.
     * <p/>
     * Created on Nov 9, 2007 by depryf
     * @param id set ID
     * @return a <code>EmbeddedSet</code>, null if none correspond to the passed ID.
     */
    public EmbeddedSet getSet(String id) {
        if (id == null)
            return null;

        for (EmbeddedSet set : _sets)
            if (id.equalsIgnoreCase(set.getId()))
                return set;

        return null;
    }

    @Override
    public String toString() {
        return getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Validator validator = (Validator)o;
        if (_validatorId != null && validator._validatorId != null)
            return Objects.equals(_validatorId, validator._validatorId);
        return Objects.equals(_id, validator._id);
    }

    @Override
    public int hashCode() {
        if (_validatorId != null)
            return Objects.hash(_validatorId);
        return Objects.hash(_id);
    }
}
