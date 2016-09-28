/*
 * Copyright (C) 2004 Information Management Services, Inc.
 */
package com.imsweb.validation.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang3.StringUtils;

import groovy.lang.Binding;

import com.imsweb.validation.ConstructionException;
import com.imsweb.validation.ValidationEngine;
import com.imsweb.validation.ValidationEngineStats;
import com.imsweb.validation.ValidationException;
import com.imsweb.validation.ValidatorContextFunctions;
import com.imsweb.validation.ValidatorServices;
import com.imsweb.validation.entities.RuleFailure;
import com.imsweb.validation.entities.Validatable;

/**
 * A <code>ValidatingProcessor</code> is a <code>Processor</code> that runs edits on a particular level of a <code>Validatable</code>.
 * <p/>
 * Created on Apr 26, 2011 by depryf
 * @author depryf
 */
@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
public class ValidatingProcessor implements Processor {

    // map of stats object to keep track of how long each polisher takes to run
    private static final Map<String, ValidationEngineStats> _STATS = new HashMap<>();

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
    private ValidatingProcessorLRUCache<String, ExecutableRule> _cachedForcedRules = new ValidatingProcessorLRUCache<>(10);

    // whether or not stats should be recorded
    private boolean _recordStats = false;

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
                    toForce = new ExecutableRule(vContext.getToForce());
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
            for (ExecutableCondition condition : _conditions)
                if (!condition.check(validatable, binding))
                    currentConditionFailures.add(condition.getId());
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

                try {
                    long startTime = System.currentTimeMillis();
                    boolean success = rule.validate(validatable, binding);
                    long endTime = System.currentTimeMillis();

                    // keep track of the stats...
                    if (_recordStats && id != null && !id.trim().isEmpty()) {
                        synchronized (_STATS) {
                            if (_STATS.containsKey(id))
                                ValidationEngineStats.reportRun(_STATS.get(id), endTime - startTime);
                            else
                                _STATS.put(id, new ValidationEngineStats(id, endTime - startTime));
                        }
                    }

                    // if failure, need to keep track of it since other depending rules might not have to run
                    if (!success) {
                        String msg = rule.getMessage();
                        String overriddenMsg = (String)binding.getVariable(ValidationEngine.VALIDATOR_ERROR_MESSAGE);
                        if (overriddenMsg != null)
                            msg = overriddenMsg;

                        RuleFailure failure = new RuleFailure(rule.getRule(), ValidatorServices.getInstance().fillInMessage(msg, validatable), validatable);
                        // extra error messages are used by translated edits only...
                        failure.setExtraErrorMessages((List<String>)binding.getVariable(ValidationEngine.VALIDATOR_EXTRA_ERROR_MESSAGES));
                        // information messages are used by translated edits only...
                        failure.setInformationMessages((List<String>)binding.getVariable(ValidationEngine.VALIDATOR_INFORMATION_MESSAGES));
                        // keep track of the original result
                        failure.setOriginalResult((Boolean)binding.getVariable(ValidationEngine.VALIDATOR_ORIGINAL_RESULT));
                        results.add(failure);
                        currentRuleFailures.add(id);
                    }
                }
                catch (ValidationException e) {
                    results.add(new RuleFailure(rule.getRule(), ValidationEngine.EXCEPTION_MSG, validatable, e.getCause()));
                }
                catch (Exception e) {
                    results.add(new RuleFailure(rule.getRule(), ValidationEngine.EXCEPTION_MSG, validatable, null));
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
        binding.setVariable(ValidationEngine.VALIDATOR_FUNCTIONS_KEY, ValidatorContextFunctions.getInstance());
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
     * @param rules
     */
    public synchronized void setRules(List<ExecutableRule> rules) {
        _rules.clear();
        _rules.addAll(rules);
    }

    /**
     * Sets the conditions on this processor.
     * @param conditions
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
    public static Map<String, ValidationEngineStats> getStats() {
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
     * Simple implementation of a LRU cache based on a LinkedHashMap.
     * @param <A>
     * @param <B>
     */
    private static class ValidatingProcessorLRUCache<A, B> extends LinkedHashMap<A, B> {

        private static final long serialVersionUID = 4701170688038236784L;

        private final int _maxEntries;

        /**
         * Constructor.
         * @param maxEntries the maximum number of entries to keep in the cache
         */
        public ValidatingProcessorLRUCache(int maxEntries) {
            super(maxEntries + 1, 1.0f, true);
            _maxEntries = maxEntries;
        }

        /**
         * Returns <tt>true</tt> if this <code>LruCache</code> has more entries than the maximum specified when it was
         * created.
         * <p/>
         * <p>
         * This method <em>does not</em> modify the underlying <code>Map</code>; it relies on the implementation of
         * <code>LinkedHashMap</code> to do that, but that behavior is documented in the JavaDoc for
         * <code>LinkedHashMap</code>.
         * </p>
         * @param eldest <code>Entry</code> in question; this implementation doesn't care what it is, since the implementation is only dependent on the size of the cache
         * @return <tt>true</tt> if the oldest
         * @see LinkedHashMap#removeEldestEntry(Entry)
         */
        @Override
        protected boolean removeEldestEntry(Entry<A, B> eldest) {
            return size() > _maxEntries;
        }
    }

}