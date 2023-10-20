/*
 * Copyright (C) 2020 Information Management Services, Inc.
 */
package com.imsweb.validation.runtime;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Special implementation of compiled-rules that handles a list of split compiled-rules objects.
 */
public class CompiledRulesBundle implements CompiledRules {

    // the split compiled rules, not null nor empty
    protected List<CompiledRules> _splitCompiledRules;

    /**
     * Constructor.
     * @param splitCompiledRules split compiled-rules
     */
    public CompiledRulesBundle(CompiledRules... splitCompiledRules) {
        _splitCompiledRules = Arrays.asList(splitCompiledRules);
        if (_splitCompiledRules.isEmpty())
            throw new IllegalStateException("At least one compiled-rules object must be provided!");
    }

    @Override
    public String getValidatorId() {
        return _splitCompiledRules.get(0).getValidatorId();
    }

    @Override
    public String getValidatorVersion() {
        return _splitCompiledRules.get(0).getValidatorVersion();
    }

    @Override
    public Map<String, List<Class<?>>> getMethodParameters() {
        return _splitCompiledRules.get(0).getMethodParameters();
    }

    /**
     * Returns the split compiled-rules object that can handle the provided rule ID.
     * @param ruleId the requested rule ID
     * @return the corresponding compiled-rules object
     */
    public CompiledRules getCompiledRulesForRuleId(String ruleId) {
        for (CompiledRules rules : _splitCompiledRules)
            if (rules.containsRuleId(ruleId))
                return rules;
        return null;
    }
}
