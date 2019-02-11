/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.validation.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.imsweb.validation.entities.Rule;

/**
 * This class is used as a context when processing a validatable. Note that it has noting to do with user-defined contexts that are made avaialable to the rules...
 * <br/><br/>
 * By overridding the conditionFailed() and atLeastOneDependencyFailed() methods, the conditions and dependencies mechanism can be customized to allow more complect features
 * (for example, to allow cross-validator conditions)...
 */
public class ValidatingContext {

    /**
     * Rule IDs to dynamically ignore (if both a collection of rule to execute and ignore are provided, the execute takes precedence).
     */
    protected Collection<String> _toIgnore;

    /**
     * Rule IDs to dynamically execute (if both a collection of rule to execute and ignore are provided, the execute takes precedence).
     */
    protected Collection<String> _toExecute;

    /**
     * Single rule to force, useful for unit tests (this takes precedence on both the execute and ignore collections of IDs).
     */
    protected Rule _toForce;

    /**
     * Rule IDs that have failed so far, mapped by validatable path.
     */
    protected Map<String, Set<String>> _failedRuleIds;

    /**
     * Condition IDs that have failed so far, mapped by validatable path.
     */
    protected Map<String, Set<String>> _failedConditionIds;

    /**
     * Constructor.
     */
    public ValidatingContext() {
        _failedRuleIds = new HashMap<>();
        _failedConditionIds = new HashMap<>();
    }

    public Collection<String> getToIgnore() {
        return _toIgnore;
    }

    public void setToIgnore(Collection<String> toIgnore) {
        _toIgnore = toIgnore;
    }

    public Collection<String> getToExecute() {
        return _toExecute;
    }

    public void setToExecute(Collection<String> toExecute) {
        _toExecute = toExecute;
    }

    public Rule getToForce() {
        return _toForce;
    }

    public void setToForce(Rule toForce) {
        _toForce = toForce;
    }

    public Map<String, Set<String>> getFailedRuleIds() {
        return _failedRuleIds;
    }

    public Map<String, Set<String>> getFailedConditionIds() {
        return _failedConditionIds;
    }

    /**
     * Resets the rule and condition failures. This method should only be used for testing purposes.
     */
    public void resetFailures() {
        _failedRuleIds.clear();
        _failedConditionIds.clear();
    }

    /**
     * Returns true if the provided condition ID has failed, false otherwise.
     * <br/><br/>
     * The default behavior of this method is to check the failed conditions for the current validatable level, and any of it's parent (so no cross-paths in the validatable tree).
     * <br/><br/>
     * Implementation: this method uses the _failedConditionsIds property to determine its result.
     * @param validatablePaths the currently processed validatable path, already split for convenience
     * @param conditionId the condition ID to check
     * @return true if the condition has failed, false otherwise
     */
    public boolean conditionFailed(List<String> validatablePaths, String conditionId) {
        for (String validatablePath : validatablePaths) {
            Set<String> failedIds = _failedConditionIds.get(validatablePath);
            if (failedIds != null && failedIds.contains(conditionId))
                return true;
        }
        return false;
    }

    /**
     * Returns true if at least one rule ID in the provided list of dependencies has failed, false otherwise.
     * <br/><br/>
     * The default behavior of this method is to check the failed rules for the current validatable level, and any of it's parent (so no cross-paths in the validatable tree).
     * <br/><br/>
     * Implementation: this method uses the _failedRuleIds property to determine its result.
     * @param validatablePaths the currently processed validatable path, already split for convenience
     * @param dependencies the rule IDs to check
     * @return true if the at least one "depends-on" rule has failed, false otherwise
     */
    public boolean atLeastOneDependencyFailed(List<String> validatablePaths, Set<String> dependencies) {
        for (String validatablePath : validatablePaths) {
            Set<String> failedIds = _failedRuleIds.get(validatablePath);
            if (failedIds != null && !Collections.disjoint(failedIds, dependencies))
                return true;
        }
        return false;
    }
}
