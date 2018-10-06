/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.validation.internal.callable;

import com.imsweb.validation.InitializationStats;
import com.imsweb.validation.entities.Rule;
import com.imsweb.validation.internal.ExecutableRule;
import com.imsweb.validation.runtime.CompiledRules;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * This class is used to multi-thread the compilation of the rules.
 */
public class RuleCompilingCallable implements Callable<Void> {

    /**
     * Rule to compile.
     */
    private Rule _rule;

    /**
     * Collection of compiled rules.
     */
    private Map<Long, ExecutableRule> _rules;

    /**
     * Pre-compile rules (can be null).
     */
    private CompiledRules _compiledRules;

    /**
     * Initialization stats.
     */
    private InitializationStats _stats;

    /**
     * Constructor.
     * @param rule rule to compile
     * @param rules collection of compiled rules
     */
    public RuleCompilingCallable(Rule rule, Map<Long, ExecutableRule> rules, CompiledRules compiledRules, InitializationStats stats) {
        _rule = rule;
        _rules = rules;
        _compiledRules = compiledRules;
        _stats = stats;
    }

    @Override
    public Void call() throws Exception {
        _rules.put(_rule.getRuleId(), new ExecutableRule(_rule, _compiledRules, _stats));
        return null;
    }
}
