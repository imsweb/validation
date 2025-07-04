/*
 * Copyright (C) 2004 Information Management Services, Inc.
 */
package com.imsweb.validation.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang3.StringUtils;

import groovy.lang.Binding;

import com.imsweb.validation.ConstructionException;
import com.imsweb.validation.ValidatingContext;
import com.imsweb.validation.ValidationContextFunctions;
import com.imsweb.validation.ValidationException;
import com.imsweb.validation.ValidationServices;
import com.imsweb.validation.entities.RuleFailure;
import com.imsweb.validation.entities.Validatable;
import com.imsweb.validation.runtime.RuntimeUtils;

import static com.imsweb.validation.ValidationEngine.EXCEPTION_MSG;
import static com.imsweb.validation.ValidationEngine.VALIDATOR_CONTEXT_KEY;
import static com.imsweb.validation.ValidationEngine.VALIDATOR_ERROR_MESSAGE;
import static com.imsweb.validation.ValidationEngine.VALIDATOR_EXTRA_ERROR_MESSAGES;
import static com.imsweb.validation.ValidationEngine.VALIDATOR_FUNCTIONS_KEY;
import static com.imsweb.validation.ValidationEngine.VALIDATOR_INFORMATION_MESSAGES;
import static com.imsweb.validation.ValidationEngine.VALIDATOR_ORIGINAL_RESULT;

/**
 * A <code>ValidatingProcessor</code> is a <code>Processor</code> that runs edits on a particular level of a <code>Validatable</code>.
 * <p/>
 * Created on Apr 26, 2011 by depryf
 * @author depryf
 */
@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
public class ValidatingProcessor implements Processor {

    // the current java path for this validating processor
    private final String _currentJavaPath;

    // collection of child processors; guaranteed never to be null; modified only during initialization (so thread-safe)
    private final Collection<IterativeProcessor> _processors = new ArrayList<>();

    // cached collection of conditions to be run by this processor (need to use a thread-safe collection)
    private final List<ExecutableCondition> _conditions = new CopyOnWriteArrayList<>();

    // cached sorted rules (need to use a thread-safe collection) - rules are rarely written but are read all the time...
    private final List<ExecutableRule> _rules = new CopyOnWriteArrayList<>();

    // cached base context; depends directly on the rulesets (there is no CopyOnWriteHashMap, boooh)
    private final Map<String, Object> _contexts = new ConcurrentHashMap<>();

    // cached compiled forced rules (#294)
    private final ValidationLRUCache<String, ExecutableRule> _cachedForcedRules = new ValidationLRUCache<>(10);

    /**
     * Constructor.
     * <p/>
     * Created on Aug 15, 2011 by depryf
     * @param javaPath current java path for this validating processor
     */
    public ValidatingProcessor(String javaPath) {
        _currentJavaPath = javaPath;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<RuleFailure> process(Validatable validatable, ValidatingContext vContext) throws ValidationException {
        Collection<RuleFailure> results = new ArrayList<>();

        // a unique rule that needs to be executed (useful for unit tests and editor validation)
        ExecutableRule toForce = null;
        if (vContext.getToForce() != null && _currentJavaPath.equals(vContext.getToForce().getJavaPath())) {
            try {
                // have to use the expression in the key in case it was changed between two calls...
                String key = vContext.getToForce().getId() + "|" + vContext.getToForce().getExpression().hashCode();
                toForce = _cachedForcedRules.get(key);
                if (toForce == null) {
                    toForce = new ExecutableRule(vContext.getToForce(), RuntimeUtils.findCompileRules(vContext.getToForce().getValidator(), null), null);
                    _cachedForcedRules.put(key, toForce);
                }
            }
            catch (ConstructionException e) {
                throw new ValidationException(e);
            }
        }

        // pre-execute all conditions for this processor (not applicable if a unique rule is forced)
        Binding binding = null;
        if (vContext.getToForce() == null) {
            binding = buildBinding(validatable);
            Set<String> currentConditionFailures = new HashSet<>();
            vContext.getFailedConditionIds().put(validatable.getCurrentLevel(), currentConditionFailures);
            for (ExecutableCondition condition : _conditions) {
                if (!condition.check(validatable, binding))
                    currentConditionFailures.add(condition.getId());
            }
        }

        // if this processor contains no rule, and there isn't one to be forced, we are done!
        if (!_rules.isEmpty() || toForce != null) {

            // setup the binding if it wasn't setup already for running conditions
            if (binding == null)
                binding = buildBinding(validatable);

            // pre-split the java-path since the split results is going to be used a lot
            List<String> validatablePaths = new ArrayList<>();
            StringBuilder buf = new StringBuilder();
            for (String validatablePath : StringUtils.split(validatable.getCurrentLevel(), '.')) {
                if (buf.length() > 0)
                    buf.append(".");
                buf.append(validatablePath);
                validatablePaths.add(buf.toString());
            }

            // we are going to keep track of the failures in this collection (also made available on the execution context)
            Set<String> currentRuleFailures = new HashSet<>();
            vContext.getFailedRuleIds().put(validatable.getCurrentLevel(), currentRuleFailures);

            // and finally, go through each rule and execute it if it needs to be executed (ignore all rules if one is forced, but it's not for this level)
            List<ExecutableRule> toExecute;
            if (toForce != null)
                toExecute = Collections.singletonList(toForce);
            else if (vContext.getToForce() != null)
                toExecute = Collections.emptyList();
            else
                toExecute = _rules;

            for (ExecutableRule rule : toExecute) {
                String id = rule.getId();

                // if the caller forces a rule to run, then it cannot be ignored
                if (toForce == null) {

                    // *** rule could be ignored because the caller requested to dynamically ignore it
                    if ((vContext.getToExecute() != null && !vContext.getToExecute().contains(id)) || (vContext.getToIgnore() != null && vContext.getToIgnore().contains(id))) {
                        currentRuleFailures.add(id); // do not run any rules depending on a rule that is being ignored
                        continue;
                    }

                    // *** rule could be ignored because it has been flagged as being ignored
                    if (rule.getIgnored() != null && rule.getIgnored()) {
                        currentRuleFailures.add(id); // do not run any rules depending on a rule that is being ignored
                        continue;
                    }

                    // *** rule could be ignored because of a failing condition
                    if (rule.getConditions() != null && !rule.getConditions().isEmpty()) {
                        boolean conditionFailed = !rule.getUseAndForConditions();
                        for (String conditionId : rule.getConditions()) {
                            boolean thisConditionFailed = vContext.conditionFailed(validatablePaths, conditionId);
                            if (Boolean.TRUE.equals(rule.getUseAndForConditions())) { // if conditions are AND'ed, and this one fails, we are done
                                if (thisConditionFailed) {
                                    conditionFailed = true;
                                    break;
                                }
                            }
                            else { // if conditions are OR'ed, and this one passed, we are done
                                if (!thisConditionFailed) {
                                    conditionFailed = false;
                                    break;
                                }
                            }
                        }
                        if (conditionFailed) {
                            currentRuleFailures.add(id); // do not run any rules depending on a rule that failed because of its condition
                            continue;
                        }
                    }

                    // *** rule could be ignored because one of its parent rule failed (or was ignored)
                    if (vContext.atLeastOneDependencyFailed(validatablePaths, rule.getDependencies())) {
                        currentRuleFailures.add(id); // do not run any rules depending on a rule that failed because of its dependencies
                        continue;
                    }
                }

                try {
                    long startTime = System.currentTimeMillis();
                    boolean success = rule.validate(validatable, binding);
                    long endTime = System.currentTimeMillis();

                    // keep track of the stats...
                    if (vContext.computeEditsStats() && id != null && !id.isEmpty())
                        vContext.reportEditDuration(_currentJavaPath, id, endTime - startTime);

                    if (!success) {
                        String message = ValidationServices.getInstance().fillInMessage(rule.getMessage(), validatable);

                        // edits returned "passed", but the failing flag was set; we can't use the "default" error message
                        if (Boolean.TRUE.equals(binding.getVariable(VALIDATOR_ORIGINAL_RESULT))) {
                            List<String> errorMessages = (List<String>)binding.getVariable(VALIDATOR_EXTRA_ERROR_MESSAGES);
                            if (errorMessages != null && !errorMessages.isEmpty())
                                message = ValidationServices.getInstance().fillInMessage(errorMessages.removeFirst(), validatable);
                            else {
                                // there should be an "extra" error message, but if there isn't, see if the overridden default error was set and if not, just use the default message
                                String overriddenError = (String)binding.getVariable(VALIDATOR_ERROR_MESSAGE);
                                if (overriddenError != null)
                                    message = ValidationServices.getInstance().fillInMessage(overriddenError, validatable);
                            }
                        }
                        else {
                            // translated edits can override the default error message
                            String overriddenError = (String)binding.getVariable(VALIDATOR_ERROR_MESSAGE);
                            if (overriddenError != null)
                                message = ValidationServices.getInstance().fillInMessage(overriddenError, validatable);
                        }

                        RuleFailure failure = new RuleFailure(rule.getRule(), message, validatable);
                        failure.setExtraErrorMessages(ValidationServices.getInstance().fillInMessages((List<String>)binding.getVariable(VALIDATOR_EXTRA_ERROR_MESSAGES), validatable));
                        failure.setInformationMessages(ValidationServices.getInstance().fillInMessages((List<String>)binding.getVariable(VALIDATOR_INFORMATION_MESSAGES), validatable));
                        failure.setOriginalResult((Boolean)binding.getVariable(VALIDATOR_ORIGINAL_RESULT));
                        results.add(failure);
                        currentRuleFailures.add(id);
                    }
                }
                catch (ValidationException e) {
                    results.add(new RuleFailure(rule.getRule(), EXCEPTION_MSG, validatable, e.getCause()));
                }
                catch (RuntimeException e) {
                    results.add(new RuleFailure(rule.getRule(), EXCEPTION_MSG, validatable, e));
                }
                finally {
                    validatable.clearPropertiesWithError();
                }
            }
        }

        // process the children validators
        for (IterativeProcessor p : _processors)
            results.addAll(p.process(validatable, vContext));

        return results;
    }

    /**
     * Helper to build the binding that will be used for the conditions and rules
     * @param validatable current validatable
     * @return the Groovy binding
     */
    protected Binding buildBinding(Validatable validatable) {

        // it is important to use the default constructor to avoid side effect on the _context variable...
        Binding binding = new Binding();

        // add static context
        binding.setVariable(VALIDATOR_FUNCTIONS_KEY, ValidationContextFunctions.getInstance());
        binding.setVariable(VALIDATOR_CONTEXT_KEY, _contexts);

        // add dynamic context
        for (Entry<String, Object> entry : validatable.getScope().entrySet())
            binding.setVariable(entry.getKey(), entry.getValue());

        return binding;
    }

    /**
     * Returns the java path for this processor.
     * @return the java path for this processor
     */
    public String getJavaPath() {
        return _currentJavaPath;
    }

    /**
     * Adds an iterative processor to *this* processor; can be call only during initialization process.
     * <p/>
     * Created on May 1, 2007 by depryf
     * @param processor processor to add
     */
    public void addNested(IterativeProcessor processor) {
        _processors.add(processor);
    }

    /**
     * Sets the rules on this processor.
     * @param rules rules to set.
     */
    public synchronized void setRules(List<ExecutableRule> rules) {
        _rules.clear();
        _rules.addAll(rules);
    }

    /**
     * Sets the conditions on this processor.
     * @param conditions conditions to set.
     */
    public synchronized void setConditions(Collection<ExecutableCondition> conditions) {
        _conditions.clear();
        _conditions.addAll(conditions);
    }

    /**
     * Sets the contexts on this processor.
     * @param allContexts all the contexts to take into account
     */
    public void setContexts(Map<Long, Map<String, Object>> allContexts) {
        _contexts.clear();
        if (allContexts != null)
            for (Entry<Long, Map<String, Object>> entry : allContexts.entrySet())
                _contexts.putAll(entry.getValue());
    }

    /**
     * Dumps this processor's internal cache in the passed buffer.
     * <p/>
     * Created on Jan 14, 2008 by depryf
     * @param buffer buffer for the internal cache
     * @param path current path
     */
    public void dumpCache(StringBuilder buffer, String path) {
        buffer.append("\n").append(path).append(": ");
        for (ExecutableRule r : _rules)
            buffer.append(r.getId()).append(",");
        if (!_rules.isEmpty())
            buffer.setLength(buffer.length() - 1);
    }

    @Override
    public String toString() {
        return _currentJavaPath + " [" + _rules.size() + " rule(s)]";
    }
}