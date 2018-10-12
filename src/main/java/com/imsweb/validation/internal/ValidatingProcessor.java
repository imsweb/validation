/*
 * Copyright (C) 2004 Information Management Services, Inc.
 */
package com.imsweb.validation.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;

import groovy.lang.Binding;

import com.imsweb.validation.ConstructionException;
import com.imsweb.validation.EngineStats;
import com.imsweb.validation.ValidationContextFunctions;
import com.imsweb.validation.ValidationEngine;
import com.imsweb.validation.ValidationException;
import com.imsweb.validation.ValidationServices;
import com.imsweb.validation.entities.RuleFailure;
import com.imsweb.validation.entities.Validatable;
import com.imsweb.validation.runtime.RuntimeUtils;

/**
 * A <code>ValidatingProcessor</code> is a <code>Processor</code> that runs edits on a particular level of a <code>Validatable</code>.
 * <p/>
 * Created on Apr 26, 2011 by depryf
 * @author depryf
 */
@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
public class ValidatingProcessor implements Processor {

    // map of stats object to keep track of how long each polisher takes to run
    private static final Map<String, EngineStats> _STATS = new HashMap<>();

    // the current java path for this validating processor
    private String _currentJavaPath;

    // collection of child processors; guaranteed never to be null; modified only during initialization (so thread-safe)
    private Collection<IterativeProcessor> _processors = new ArrayList<>();

    // cached collection of conditions to be run by this processor (need to use a thread-safe collection)
    private List<ExecutableCondition> _conditions = new CopyOnWriteArrayList<>();

    // cached sorted rules (need to use a thread-safe collection) - rules are rarely written but are read all the time...
    private List<ExecutableRule> _rules = new CopyOnWriteArrayList<>();

    // cached base context; depends directly on the rulesets (there is no CopyOnWriteHashMap, boooh)
    private Map<String, Object> _contexts = new ConcurrentHashMap<>();

    // cached compiled forced rules (#294)
    private ValidationLRUCache<String, ExecutableRule> _cachedForcedRules = new ValidationLRUCache<>(10);

    // whether or not stats should be recorded
    private boolean _recordStats = false;

    // time out in seconds when executing an edit or a condition
    private int _timeout;

    // thread executor to be able to time out an edit
    private ExecutorService _executor;

    /**
     * Constructor.
     * <p/>
     * Created on Aug 15, 2011 by depryf
     * @param javaPath current java path for this validating processor
     * @param editExecutionTimeout timeout for edits (0 or negative means no timeout)
     */
    public ValidatingProcessor(String javaPath, int editExecutionTimeout) {
        _currentJavaPath = javaPath;
        _timeout = editExecutionTimeout;
        if (editExecutionTimeout > 0)
            _executor = Executors.newSingleThreadExecutor();
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

                boolean success;
                if (_timeout > 0) {
                    Future<Boolean> future = _executor.submit(new ExecutorCallable(condition, validatable, binding));
                    try {
                        success = future.get(_timeout, TimeUnit.SECONDS);
                    }
                    catch (InterruptedException e) {
                        throw new ValidationException(condition.getId() + ": " + ValidationEngine.INTERRUPTED_MSG);
                    }
                    catch (TimeoutException e) {
                        throw new ValidationException(condition.getId() + ": " + ValidationEngine.TIMEOUT_MSG);
                    }
                    catch (ExecutionException e) {
                        if (e.getCause() instanceof ValidationException)
                            throw (ValidationException)e.getCause();
                        throw new ValidationException(condition.getId() + ": " + ValidationEngine.EXCEPTION_MSG);
                    }
                }
                else
                    success = condition.check(validatable, binding);

                if (!success) {
                    currentConditionFailures.add(condition.getId());
                }
            }
        }

        // if this processor contains no rule, and there isn't one to be forced, we are done!
        if (!_rules.isEmpty() || toForce != null) {

            // setup the binding if it wasn't setup already for running conditions
            if (binding == null)
                binding = buildBinding(validatable);

            // pre-split the java-path since the split results is going to be used a lot
            String[] validatablePaths = StringUtils.split(validatable.getCurrentLevel(), '.');

            // we are going to keep track of the failures in this collection (also made available on the execution context)
            Set<String> currentRuleFailures = new HashSet<>();
            vContext.getFailedRuleIds().put(validatable.getCurrentLevel(), currentRuleFailures);

            // and finally, go through each rule and execute it if it needs to be executed (ignore all rules if one is to forced, but it's not for this level)
            for (ExecutableRule rule : toForce != null ? Collections.singleton(toForce) : vContext.getToForce() != null ? Collections.<ExecutableRule>emptySet() : _rules) {
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
                            if (rule.getUseAndForConditions()) { // if conditions are AND'ed, and this one fails, we are done
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

                Future<Boolean> future = null;
                try {
                    long startTime = System.currentTimeMillis();
                    boolean success;
                    if (_timeout > 0) {
                        future = _executor.submit(new ExecutorCallable(rule, validatable, binding));
                        success = future.get(_timeout, TimeUnit.SECONDS);
                    }
                    else
                        success = rule.validate(validatable, binding);
                    long endTime = System.currentTimeMillis();

                    // keep track of the stats...
                    if (_recordStats && id != null && !id.trim().isEmpty()) {
                        synchronized (_STATS) {
                            if (_STATS.containsKey(id))
                                EngineStats.reportRun(_STATS.get(id), endTime - startTime);
                            else
                                _STATS.put(id, new EngineStats(id, endTime - startTime));
                        }
                    }

                    // if failure, need to keep track of it since other depending rules might not have to run
                    if (!success) {
                        String message = ValidationServices.getInstance().fillInMessage(rule.getMessage(), validatable);

                        // translated edits can override the default error message
                        String overriddenError = (String)binding.getVariable(ValidationEngine.VALIDATOR_ERROR_MESSAGE);
                        if (overriddenError != null)
                            message = ValidationServices.getInstance().fillInMessage(overriddenError, validatable);

                        // translated edits can dynamically report error messages
                        List<String> extraErrors = ValidationServices.getInstance().fillInMessages((List<String>)binding.getVariable(ValidationEngine.VALIDATOR_EXTRA_ERROR_MESSAGES), validatable);
                        if (extraErrors != null && !extraErrors.isEmpty())
                            message = StringUtils.join(extraErrors, ". ");

                        RuleFailure failure = new RuleFailure(rule.getRule(), message, validatable);
                        failure.setExtraErrorMessages(extraErrors);
                        failure.setInformationMessages(ValidationServices.getInstance().fillInMessages((List<String>)binding.getVariable(ValidationEngine.VALIDATOR_INFORMATION_MESSAGES), validatable));
                        failure.setOriginalResult((Boolean)binding.getVariable(ValidationEngine.VALIDATOR_ORIGINAL_RESULT));
                        results.add(failure);
                        currentRuleFailures.add(id);
                    }
                }
                catch (TimeoutException e) {
                    results.add(new RuleFailure(rule.getRule(), ValidationEngine.TIMEOUT_MSG, validatable, null));
                }
                catch (InterruptedException e) {
                    results.add(new RuleFailure(rule.getRule(), ValidationEngine.INTERRUPTED_MSG, validatable, null));
                }
                catch (ExecutionException e) {
                    if (e.getCause() instanceof ValidationException)
                        results.add(new RuleFailure(rule.getRule(), ValidationEngine.EXCEPTION_MSG, validatable, e.getCause().getCause()));
                    else
                        results.add(new RuleFailure(rule.getRule(), "Edit generated an unexpected error: " + e.getCause().getMessage(), validatable, null));
                }
                catch (ValidationException e) {
                    results.add(new RuleFailure(rule.getRule(), ValidationEngine.EXCEPTION_MSG, validatable, e.getCause()));
                }
                catch (Exception e) {
                    results.add(new RuleFailure(rule.getRule(), ValidationEngine.EXCEPTION_MSG, validatable, null));
                }
                finally {
                    validatable.clearPropertiesWithError();
                    if (future != null)
                        future.cancel(true);
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
        binding.setVariable(ValidationEngine.VALIDATOR_FUNCTIONS_KEY, ValidationContextFunctions.getInstance());
        binding.setVariable(ValidationEngine.VALIDATOR_CONTEXT_KEY, _contexts); // new way of referencing contexts (using a prefix)
        for (Entry<String, Object> entry : _contexts.entrySet()) // old way of using the contexts (without a prefix); for now we still support it...
            binding.setVariable(entry.getKey(), entry.getValue());

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
     * Turns on or off the statistics.
     * <p/>
     * Created on Jun 29, 2011 by depryf
     * @param on if true the stats will be on, otherwise they will be off
     */
    public void setStatisticsOn(boolean on) {
        synchronized (_STATS) {
            _STATS.clear();
        }
        _recordStats = on;
    }

    /**
     * Dumps this processor's internal cache in the passed buffer.
     * <p/>
     * Created on Jan 14, 2008 by depryf
     * @param buffer buffer for the internal cache
     * @param path current path
     */
    public synchronized void dumpCache(StringBuilder buffer, String path) {
        buffer.append("\n").append(path).append(": ");
        for (ExecutableRule r : _rules)
            buffer.append(r.getId()).append(",");
        if (!_rules.isEmpty())
            buffer.setLength(buffer.length() - 1);
    }

    /**
     * Returns the statistics gathered so far...
     * <p/>
     * Created on Nov 30, 2007 by depryf
     * @return a collection of <code>StatsDTO</code> object, possibly empty
     */
    public static Map<String, EngineStats> getStats() {
        synchronized (_STATS) {
            return Collections.unmodifiableMap(_STATS);
        }
    }

    /**
     * Resets the statistics gathered so far...
     * <p/>
     * Created on Jun 29, 2009 by depryf
     */
    public static void resetStats() {
        synchronized (_STATS) {
            _STATS.clear();
        }
    }

    @Override
    public String toString() {
        return _currentJavaPath + " [" + (_rules == null ? "?" : _rules.size()) + " rule(s)]";
    }

    /**
     * Simple class to wrap an edit/condition execution into a thread so it can be timed out.
     */
    private static class ExecutorCallable implements Callable<Boolean> {

        private ExecutableRule _rule;
        private ExecutableCondition _condition;
        private Validatable _validatable;
        private Binding _binding;

        public ExecutorCallable(ExecutableRule rule, Validatable validatable, Binding binding) {
            _rule = rule;
            _validatable = validatable;
            _binding = binding;
        }

        public ExecutorCallable(ExecutableCondition condition, Validatable validatable, Binding binding) {
            _condition = condition;
            _validatable = validatable;
            _binding = binding;
        }

        @Override
        public Boolean call() throws Exception {
            return _rule != null ? _rule.validate(_validatable, _binding) : _condition.check(_validatable, _binding);
        }
    }
}