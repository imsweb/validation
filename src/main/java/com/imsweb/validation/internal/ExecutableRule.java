/*
 * Copyright (C) 2007 Information Management Services, Inc.
 */
package com.imsweb.validation.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.control.CompilationFailedException;

import groovy.lang.Binding;
import groovy.lang.Script;

import com.imsweb.validation.ConstructionException;
import com.imsweb.validation.InitializationStats;
import com.imsweb.validation.ValidationEngine;
import com.imsweb.validation.ValidationException;
import com.imsweb.validation.ValidationServices;
import com.imsweb.validation.entities.Rule;
import com.imsweb.validation.entities.Validatable;
import com.imsweb.validation.runtime.CompiledRules;
import com.imsweb.validation.runtime.RuntimeUtils;

/**
 * Created on Jun 28, 2011 by depryf
 * @author depryf
 */
public class ExecutableRule {

    // corresponding rule
    private Rule _rule;

    // internal ID
    private Long _internalId;

    // ID
    private String _id;

    // condition IDs
    private Set<String> _conditions;

    // operator (AND or OR) to use for the set of conditions.
    private Boolean _useAndForConditions;

    // java-path for this rule
    private String _javaPath;

    // message for this rule
    private String _message;

    // dependencies
    private Set<String> _dependencies;

    // ignored flag
    private Boolean _ignored;

    // set of properties contained in this rule; as their appear in the textual expression of the rule
    private Set<String> _usedProperties;

    // whether this rule needs to check for forced failures on entities/properties (this is an expensive mechanism); automatically populated when setExpression() is called.
    private Boolean _checkForcedEntities;

    // groovy script to execute
    private Script _script;

    // lock used for executing groovy scripts (since their interaction with the Binding objects is not thread-safe)
    private final Object _scriptLock = new Object();

    // pre-compiled rules; if those are available and a method corresponding to this rule is found in that class, then the Groovy script won't be compiled.
    private CompiledRules _compiledRules;

    // pre-compiled rule (as a Groovy method instead of a dynamically compiled Groovy script).
    private Method _compiledRule;

    // cached aliases for the java-path, only used with pre-compiled edits
    private List<String> _aliases;

    /**
     * Constructor.
     * <p/>
     * Created on Jun 28, 2011 by depryf
     * @param rule parent rule
     */
    public ExecutableRule(Rule rule) throws ConstructionException {
        this(rule, null, null);
    }

    /**
     * Constructor.
     * <p/>
     * Created on Jun 28, 2011 by depryf
     * @param rule parent rule
     * @param compiledRules pre-compiled rules (can be null in which case a Groovy Script will be compiled)
     * @param stats initialization stats (can be null)
     */
    public ExecutableRule(Rule rule, CompiledRules compiledRules, InitializationStats stats) throws ConstructionException {
        _rule = rule;
        _id = rule.getId();
        _internalId = rule.getRuleId();
        _javaPath = rule.getJavaPath();
        _conditions = rule.getConditions();
        _useAndForConditions = rule.getUseAndForConditions();
        _dependencies = rule.getDependencies();
        _message = rule.getMessage();
        _ignored = rule.getIgnored() == null ? Boolean.FALSE : rule.getIgnored();
        _usedProperties = rule.getRawProperties();
        _checkForcedEntities = computeCheckForcedEntities(rule.getExpression());

        _compiledRules = compiledRules;
        if (compiledRules != null) {
            _compiledRule = RuntimeUtils.findCompiledMethod(compiledRules, rule.getId(), compiledRules.getMethodParameters().get(rule.getJavaPath()));

            // optimization - pre-compute the different aliases for the rule's java path
            _aliases = new ArrayList<>();
            StringBuilder buf = new StringBuilder();
            for (String javaPathPart : StringUtils.split(_javaPath, '.')) {
                if (buf.length() > 0)
                    buf.append(".");
                buf.append(javaPathPart);
                _aliases.add(ValidationServices.getInstance().getAliasForJavaPath(buf.toString()));
            }
        }

        // only compile Groovy script if no re-compiled Groovy method was available...
        if (_compiledRule == null) {
            try {
                _script = ValidationServices.getInstance().compileExpression(rule.getExpression());
            }
            catch (CompilationFailedException e) {
                throw new ConstructionException("Unable to compile rule " + _rule.getId(), e);
            }
        }

        if (stats != null) {
            stats.incrementNumEditsLoaded(_rule.getValidator().getId());
            if (_compiledRule != null)
                stats.incrementNumEditsPreCompiled(_rule.getValidator().getId());
            else if (_script != null)
                stats.incrementNumEditsCompiled(_rule.getValidator().getId());
        }
    }

    /**
     * Constructor.
     * <p/>
     * Created on Oct 6, 2011 by depryf
     * @param execRule parent execution rule
     */
    public ExecutableRule(ExecutableRule execRule) {
        _rule = execRule._rule;
        _id = execRule._id;
        _internalId = execRule._internalId;
        _javaPath = execRule._javaPath;
        _conditions = execRule._conditions;
        _useAndForConditions = execRule._useAndForConditions;
        _dependencies = execRule._dependencies;
        _message = execRule._message;
        _ignored = execRule._ignored;
        _usedProperties = execRule._usedProperties;
        _script = execRule._script;
        _compiledRules = execRule._compiledRules;
        _compiledRule = execRule._compiledRule;
        _aliases = execRule._aliases;
        _checkForcedEntities = execRule._checkForcedEntities;
    }

    /**
     * @return Returns the internalId.
     */
    public Long getInternalId() {
        return _internalId;
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
        _id = id;
    }

    /**
     * Getter for the rule.
     * <p/>
     * Created on Jun 28, 2011 by depryf
     * @return rule
     */
    public Rule getRule() {
        return _rule;
    }

    /**
     * Getter for the dependencies.
     * <p/>
     * Created on Jun 29, 2011 by depryf
     * @return the dependencies
     */
    public Set<String> getDependencies() {
        return _dependencies;
    }

    /**
     * Getter for the java path.
     * <p/>
     * Created on Jun 29, 2011 by depryf
     * @return the java path
     */
    public String getJavaPath() {
        return _javaPath;
    }

    /**
     * Getter for the message.
     * <p/>
     * Created on Jun 29, 2011 by depryf
     * @return the message
     */
    public String getMessage() {
        return _message;
    }

    /**
     * Getter for the ignored flag.
     * <p/>
     * Created on Jun 29, 2011 by depryf
     * @return the ignored flag
     */
    public Boolean getIgnored() {
        return _ignored;
    }

    /**
     * @param rule The rule to set.
     */
    public void setRule(Rule rule) {
        _rule = rule;
    }

    /**
     * @param javaPath The javaPath to set.
     */
    public void setJavaPath(String javaPath) {
        _javaPath = javaPath;
    }

    /**
     * @param message The message to set.
     */
    public void setMessage(String message) {
        _message = message;
    }

    /**
     * @param dependencies The dependencies to set.
     */
    public void setDependencies(Set<String> dependencies) {
        _dependencies = dependencies;
    }

    /**
     * @param ignored The ignored to set.
     */
    public void setIgnored(Boolean ignored) {
        _ignored = ignored;
    }

    /**
     * Getter for the condition operator.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return true if the conditions should be AND'ed, false otherwise
     */
    public Boolean getUseAndForConditions() {
        return _useAndForConditions;
    }

    /**
     * Setter for the condition operator.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param useAndForConditions true if the conditions should be AND'ed, false otherwise
     */
    public void setUseAndForConditions(Boolean useAndForConditions) {
        _useAndForConditions = useAndForConditions == null ? Boolean.TRUE : useAndForConditions;
    }

    /**
     * @return Returns the condition IDs.
     */
    public Set<String> getConditions() {
        return _conditions;
    }

    /**
     * @param conditions The condition IDs to set.
     */
    public void setConditions(Set<String> conditions) {
        _conditions = conditions;
    }

    /**
     * Sets the rule expression.
     * <p/>
     * Created on Jun 29, 2011 by depryf
     * @param expression expression
     */
    public void setExpression(String expression) throws ConstructionException {

        try {
            Set<String> usedProperties = new HashSet<>(), usedContextEntries = new HashSet<>();
            ValidationServices.getInstance().parseExpression("rule", expression, usedProperties, usedContextEntries, null);
            _script = ValidationServices.getInstance().compileExpression(expression);
            _usedProperties = usedProperties;
            _checkForcedEntities = computeCheckForcedEntities(expression);

            // can't use pre-compiled methods when dynamically changing the expression! Let's make sure of that...
            _compiledRules = null;
            _compiledRule = null;
            _aliases = null;
        }
        catch (CompilationFailedException e) {
            throw new ConstructionException("Unable to compile rule " + _id, e);
        }
    }

    private boolean computeCheckForcedEntities(String expression) {
        return expression != null && (expression.contains("forceFailureOnEntity") || expression.contains("forceFailureOnProperty") || expression.contains("ignoreFailureOnProperty"));
    }

    @Override
    public String toString() {
        return _id;
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Runs the expression against the passed <code>Validatable</code> using the passed context.
     * <p/>
     * Created on Nov 9, 2007 by depryf
     * @param validatable <code>Validatable</code>
     * @param binding the Groovy binding to use
     * @return true if the expression passes, false otherwise
     */
    public boolean validate(Validatable validatable, Binding binding) throws ValidationException {
        ExtraPropertyHandlerDto extra = _checkForcedEntities ? new ExtraPropertyHandlerDto() : null;

        // this is a bit convoluted, but we still want to set the failing properties even if an exception happens...
        ValidationException exception = null;

        boolean success;
        try {
            success = internalValidate(validatable, binding, extra);
            // edits from Genedits use side-effect flags to fail, instead of returning false...
            if (success) {
                Boolean failingFlag = (Boolean)binding.getVariable(ValidationEngine.VALIDATOR_FAILING_FLAG);
                if (failingFlag != null && failingFlag)
                    success = false;
            }
        }
        catch (ValidationException e) {
            success = false;
            exception = e;
        }

        if (!success) {
            try {
                // go through each property to report
                for (String property : _usedProperties)
                    if (extra == null || extra.getIgnoredProperties() == null || !extra.getIgnoredProperties().contains(property))
                        validatable.reportFailureForProperty(property);
                // also add any property that needs to be forced
                if (extra != null && extra.getForcedProperties() != null)
                    for (String property : extra.getForcedProperties())
                        validatable.reportFailureForProperty(property);
                // and finally, report the extra entities
                if (extra != null && extra.getForcedEntities() != null)
                    validatable.forceFailureForProperties(extra.getForcedEntities(), _usedProperties);
            }
            catch (IllegalAccessException e) {
                throw new ValidationException(e.getMessage());
            }
        }

        if (exception != null)
            throw exception;

        return success;
    }

    /**
     * Runs the Groovy script defined by this rule. Returns true if the script returns true, false otherwise.
     * <p/>
     * Created on Nov 9, 2007 by depryf
     * @param validatable <code>Validatable</code>
     * @param binding the Groovy binding to use
     * @param extra wrapper for properties or entities to ignore or force
     * @return boolean
     */
    @SuppressWarnings("unchecked")
    private boolean internalValidate(Validatable validatable, Binding binding, ExtraPropertyHandlerDto extra) throws ValidationException {
        boolean success;

        // make sure there are no left-over stuff in the binding
        binding.setVariable(ValidationEngine.VALIDATOR_FORCE_FAILURE_ENTITY_KEY, null);
        binding.setVariable(ValidationEngine.VALIDATOR_FORCE_FAILURE_PROPERTY_KEY, null);
        binding.setVariable(ValidationEngine.VALIDATOR_IGNORE_FAILURE_PROPERTY_KEY, null);
        binding.setVariable(ValidationEngine.VALIDATOR_ERROR_MESSAGE, null);
        binding.setVariable(ValidationEngine.VALIDATOR_EXTRA_ERROR_MESSAGES, null);
        binding.setVariable(ValidationEngine.VALIDATOR_INFORMATION_MESSAGES, null);
        binding.setVariable(ValidationEngine.VALIDATOR_FAILING_FLAG, null);
        binding.setVariable(ValidationEngine.VALIDATOR_ORIGINAL_RESULT, null);

        // if a method is available, invoke it, otherwise execute the script
        if (_compiledRule != null) {
            List<Object> params = new ArrayList<>();
            params.add(binding);
            params.add(binding.getVariable(ValidationEngine.VALIDATOR_CONTEXT_KEY));
            params.add(binding.getVariable(ValidationEngine.VALIDATOR_FUNCTIONS_KEY));
            for (String alias : _aliases)
                params.add(binding.getVariable(alias));

            try {
                success = (Boolean)_compiledRule.invoke(_compiledRules, params.toArray(new Object[0]));
            }
            catch (IllegalAccessException | InvocationTargetException e) {
                throw new ValidationException("Exception invoking method for edit " + _id, e);
            }
        }
        else if (_script != null) {
            synchronized (_scriptLock) {
                _script.setBinding(binding);
                try {
                    Object result = _script.run();
                    if (result instanceof Boolean)
                        success = (Boolean)result;
                    else
                        throw new ValidationException("result is not a boolean");
                }
                catch (Exception e) {
                    StringBuilder buf = new StringBuilder();
                    if (_id != null) {
                        buf.append("Unable to execute edit '").append(_id).append("'");
                        String validated = validatable.getDisplayId();
                        if (validated != null)
                            buf.append(" on ").append(validatable.getDisplayId()).append(": ");
                        else
                            buf.append(": ");
                    }
                    else
                        buf.append("Unable to execute edit: ");
                    buf.append(e.getMessage() == null ? "null reference" : e.getMessage());
                    throw new ValidationException(buf.toString(), e);
                }
                finally {
                    _script.setBinding(null);
                }
            }
        }
        else
            success = true;

        // read back the forced/ignored entities/properties if there are any
        if (_checkForcedEntities) {
            Object forcedEntities = binding.getVariable(ValidationEngine.VALIDATOR_FORCE_FAILURE_ENTITY_KEY);
            if (forcedEntities != null)
                extra.setForcedEntities((Set<ExtraPropertyEntityHandlerDto>)forcedEntities);
            Object forcedProperties = binding.getVariable(ValidationEngine.VALIDATOR_FORCE_FAILURE_PROPERTY_KEY);
            if (forcedProperties != null)
                extra.setForcedProperties((Set<String>)forcedProperties);
            Object ignoredProperties = binding.getVariable(ValidationEngine.VALIDATOR_IGNORE_FAILURE_PROPERTY_KEY);
            if (ignoredProperties != null)
                extra.setIgnoredProperties((Set<String>)ignoredProperties);
        }

        // keep track of what the logic actually returns (translated edits can set flags in the binding meaning failure even if the logic passes
        binding.setVariable(ValidationEngine.VALIDATOR_ORIGINAL_RESULT, success);

        return success;
    }
}
