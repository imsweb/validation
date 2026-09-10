/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.validation.internal.callable;

import groovy.lang.GroovyShell;

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
     * Groovy shell shared by all the rules being compiled together (can be null).
     */
    private GroovyShell _shell;

    /**
     * Constructor.
     * @param rule rule to compile
     * @param rules collection of compiled rules
     */
    public RuleCompilingCallable(Rule rule, Map<Long, ExecutableRule> rules, CompiledRules compiledRules, InitializationStats stats) {
        this(rule, rules, compiledRules, stats, null);
    }

    /**
     * Constructor.
     * @param rule rule to compile
     * @param rules collection of compiled rules
     * @param compiledRules pre-compiled rules (can be null)
     * @param stats initialization stats (can be null)
     * @param shell Groovy shell to compile the expression with (can be null)
     */
    public RuleCompilingCallable(Rule rule, Map<Long, ExecutableRule> rules, CompiledRules compiledRules, InitializationStats stats, GroovyShell shell) {
        _rule = rule;
        _rules = rules;
        _compiledRules = compiledRules;
        _stats = stats;
        _shell = shell;
    }

    @Override
    public Void call() throws Exception {
        _rules.put(_rule.getRuleId(), new ExecutableRule(_rule, _compiledRules, _stats, _shell));
        return null;
    }
}
