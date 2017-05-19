/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.validation.internal.callable;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;

import com.imsweb.validation.ConstructionException;
import com.imsweb.validation.XmlValidatorFactory;
import com.imsweb.validation.entities.Rule;
import com.imsweb.validation.entities.RuleHistory;
import com.imsweb.validation.entities.Validator;
import com.imsweb.validation.entities.ValidatorVersion;
import com.imsweb.validation.entities.xml.HistoryEventXmlDto;
import com.imsweb.validation.entities.xml.RuleXmlDto;

/**
 * This class is used to multi-thread the parsing of the rules.
 */
public class RuleParsingCallable implements Callable<Void> {

    /**
     * XML rule object.
     */
    private RuleXmlDto _xmlRule;

    /**
     * The rule ID to use for the new rule.
     */
    private Long _ruleId;

    /**
     * Parent validator object.
     */
    private Validator _validator;

    /**
     * Available versions in the validator.
     */
    private Map<String, ValidatorVersion> _versions;

    /**
     * Rules processed so far, keyed by rule ID.
     */
    private Map<String, Rule> _rules;

    /**
     * Constructor.
     * @param xmlRule XML rule object
     * @param ruleId rule ID to use
     * @param validator parent validator object
     * @param versions available versions in the validator
     * @param rules rules processed so far, keyed by rule ID
     */
    public RuleParsingCallable(RuleXmlDto xmlRule, Long ruleId, Validator validator, Map<String, ValidatorVersion> versions, Map<String, Rule> rules) {
        _xmlRule = xmlRule;
        _ruleId = ruleId;
        _validator = validator;
        _versions = versions;
        _rules = rules;
    }

    @Override
    public Void call() throws Exception {

        Rule rule = new Rule();
        rule.setRuleId(_ruleId);
        rule.setValidator(_validator);
        if (_xmlRule.getId() == null)
            throw new IOException("Rule ID is required");
        rule.setId(_xmlRule.getId().trim());
        if (_xmlRule.getName() != null)
            rule.setName(_xmlRule.getName().trim());
        rule.setTag(_xmlRule.getTag());
        if (_xmlRule.getJavaPath() == null)
            throw new IOException("Unable to load " + _xmlRule.getId() + " in " + _validator.getId() + "; java-path is missing");
        rule.setJavaPath(_xmlRule.getJavaPath());
        rule.setCategory(_xmlRule.getCategory());
        if (rule.getCategory() != null && _validator.getCategory(rule.getCategory()) == null)
            throw new IOException("Unknown category '" + _xmlRule.getCategory() + "' defined for " + _xmlRule.getId() + " in " + _validator.getId());
        if (_xmlRule.getCondition() != null) {
            boolean useAnds = _xmlRule.getCondition().indexOf('&') != -1;
            Set<String> conditions = new HashSet<>();
            for (String s : Arrays.asList(StringUtils.split(_xmlRule.getCondition(), useAnds ? '&' : '|')))
                conditions.add(s.trim());
            for (String condition : conditions)
                if (_validator.getCondition(condition) == null)
                    throw new IOException("Unknown condition '" + condition + "' defined for " + _xmlRule.getId() + " in " + _validator.getId());
            rule.setConditions(conditions);
            rule.setUseAndForConditions(useAnds);

        }
        rule.setSeverity(_xmlRule.getSeverity());
        rule.setAgency(_xmlRule.getAgency());

        if (_xmlRule.getExpression() == null)
            throw new IOException("Unable to load '" + rule.getId() + "' in " + _validator.getId() + "; no expression provided");
        try {
            rule.setExpression(XmlValidatorFactory.reAlign(_xmlRule.getExpression()));
        }
        catch (ConstructionException e) {
            throw new IOException("Unable to load '" + rule.getId() + "' in " + _validator.getId() + "; it contain an invalid expression", e);
        }

        if (_xmlRule.getMessage() == null)
            throw new IOException("Unable to load '" + rule.getId() + "' in " + _validator.getId() + "; no message provided");
        rule.setMessage(XmlValidatorFactory.trimEmptyLines(_xmlRule.getMessage(), true));

        if (_xmlRule.getDepends() != null && !_xmlRule.getDepends().isEmpty()) {
            Set<String> dependencies = new HashSet<>();
            for (String s : _xmlRule.getDepends().split(","))
                if (s != null)
                    dependencies.add(s.trim());
            rule.setDependencies(dependencies);
        }
        if (_xmlRule.getDescription() != null)
            rule.setDescription(XmlValidatorFactory.reAlign(_xmlRule.getDescription()));
        if (_xmlRule.getHistoryEvents() != null && !_xmlRule.getHistoryEvents().isEmpty()) {
            Set<RuleHistory> history = new HashSet<>();
            for (HistoryEventXmlDto event : _xmlRule.getHistoryEvents()) {
                if (event.getValue() != null) {
                    RuleHistory rh = new RuleHistory();
                    rh.setRule(rule);
                    if (event.getVersion() == null)
                        throw new IOException("Unable to load '" + rule.getId() + "' in " + _validator.getId() + "; no version provided in history entry");
                    ValidatorVersion version = _versions.get(event.getVersion());
                    if (version == null)
                        throw new IOException("Unable to load '" + rule.getId() + "' in " + _validator.getId() + "; history entry references unknown version: " + event.getVersion());
                    rh.setVersion(version);
                    if (event.getUser() == null)
                        throw new IOException("Unable to load '" + rule.getId() + "' in " + _validator.getId() + "; no user provided in history entry");
                    rh.setUsername(event.getUser().trim());
                    if (event.getDate() == null)
                        throw new IOException("Unable to load '" + rule.getId() + "' in " + _validator.getId() + "; no date provided in history entry");
                    rh.setDate(event.getDate());
                    rh.setReference(event.getRef());
                    if (event.getValue() == null)
                        throw new IOException("Unable to load '" + rule.getId() + "' in " + _validator.getId() + "; no content provided in history entry");
                    rh.setMessage(XmlValidatorFactory.trimEmptyLines(event.getValue(), true));
                    history.add(rh);
                }
            }
            rule.setHistories(history);
        }
        _rules.put(rule.getId(), rule);

        return null;
    }
}
