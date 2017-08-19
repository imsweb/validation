/*
 * Copyright (C) 2007 Information Management Services, Inc.
 */
package com.imsweb.validation.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.control.CompilationFailedException;

import groovy.lang.Binding;
import groovy.lang.Script;

import com.imsweb.validation.ConstructionException;
import com.imsweb.validation.ValidationEngine;
import com.imsweb.validation.ValidationEngineInitializationStats;
import com.imsweb.validation.ValidationException;
import com.imsweb.validation.ValidatorServices;
import com.imsweb.validation.entities.Rule;
import com.imsweb.validation.entities.Validatable;
import com.imsweb.validation.functions.MetafileContextFunctions;

/**
 * Created on Jun 28, 2011 by depryf
 * @author depryf
 */
public class ExecutableRule {

    /**
     * Corresponding rule
     */
    private Rule _rule;

    /**
     * Internal ID
     */
    private Long _internalId;

    /**
     * Validator internal ID
     */
    private Long _internalValidatorId;

    /**
     * ID
     */
    private String _id;

    /**
     * Condition IDs.
     */
    private Set<String> _conditions;

    /**
     * Operator (AND or OR) to use for the set of conditions.
     */
    private Boolean _useAndForConditions;

    /**
     * Java-path for this rule
     */
    private String _javaPath;

    /**
     * Message for this rule
     */
    private String _message;

    /**
     * Dependencies
     */
    private Set<String> _dependencies;

    /**
     * Ignored flag
     */
    private Boolean _ignored;

    /**
     * Set of properties contained in this rule; as their appear in the textual expression of the rule
     */
    private Set<String> _rawProperties;

    /**
     * Set of potential context entries (they are potential because they might not all be context entries;
     * but if a context entry is used, it will be in this list...
     */
    private Set<String> _potentialContextEntries;

    /**
     * Whether this rule needs to check for forced failures on entities/properties (this is an expensive mechanism);
     * automatically populated when setExpression() is called.
     */
    private Boolean _checkForcedEntities;

    /**
     * Groovy script to execute
     */
    private Script _script;

    private Object _clazz;

    private Method _method;

    /**
     * Constructor.
     * <p/>
     * Created on Jun 28, 2011 by depryf
     * @param rule parent rule
     * @throws ConstructionException
     */
    public ExecutableRule(Rule rule) throws ConstructionException {
        this(rule, null);
    }

    /**
     * Constructor.
     * <p/>
     * Created on Jun 28, 2011 by depryf
     * @param rule parent rule
     * @throws ConstructionException
     */
    public ExecutableRule(Rule rule, ValidationEngineInitializationStats stats) throws ConstructionException {
        _rule = rule;
        _internalId = rule.getRuleId();
        _internalValidatorId = rule.getValidator() != null ? rule.getValidator().getValidatorId() : null;
        _javaPath = rule.getJavaPath();
        _conditions = rule.getConditions();
        _useAndForConditions = rule.getUseAndForConditions();
        _dependencies = rule.getDependencies();
        _message = rule.getMessage();
        _ignored = rule.getIgnored() == null ? Boolean.FALSE : rule.getIgnored();
        _rawProperties = rule.getRawProperties();
        _potentialContextEntries = rule.getPotentialContextEntries();

        try {
            //Class<?> clazz = Class.forName("com.imsweb.validation.validators.Validator_test");
            //Class<?> clazz = Class.forName("Validator_test_groovy");
            Class<?> clazz = Class.forName("NaaccrTranslatedEdits");
            String[] parts = StringUtils.split(rule.getId(), "-");
            //            StringBuilder buf = new StringBuilder(parts[0]);
            //            for (int i = 1; i < parts.length; i++)
            //                buf.append(StringUtils.capitalize(parts[i]));
            StringBuilder buf = new StringBuilder(rule.getId().replace("-", "_"));
            parts = StringUtils.split(rule.getJavaPath(), ".");
            List<Class<?>> params = new ArrayList<>();
            params.add(Binding.class);
            params.add(Map.class);
            params.add(MetafileContextFunctions.class);
            //for (int i = 0; i < parts.length; i++)
            //    params.add(Map.class);
            params.add(List.class);
            params.add(Map.class);
            _method = clazz.getMethod(buf.toString(), params.toArray(new Class[0]));
            //System.out.println("Found method '" + _method.getName() + " for edit " + rule.getId());

            _clazz = clazz.newInstance();
        }
        catch (ClassNotFoundException | NoSuchMethodException e) {
            //e.printStackTrace();
        }
        catch (IllegalAccessException e) {
            //e.printStackTrace();
        }
        catch (InstantiationException e) {
            //e.printStackTrace();
        }

        synchronized (this) {
            _id = rule.getId();

            if (_method == null) {
                try {
                    _script = ValidatorServices.getInstance().compileExpression(rule.getExpression());
                }
                catch (CompilationFailedException e) {
                    _script = null;
                    throw new ConstructionException("Unable to compile rule " + _rule.getId(), e);
                }
            }

            if (rule.getExpression() != null)
                _checkForcedEntities = rule.getExpression().contains("forceFailureOnEntity") || rule.getExpression().contains("forceFailureOnProperty") || rule.getExpression().contains(
                        "ignoreFailureOnProperty");
        }

        if (stats != null) {
            stats.incrementNumEditsLoaded();
            if (_method != null)
                stats.incrementNumEditsFoundOnClassPath();
            else if (_script != null)
                stats.incrementNumEditsCompiled();
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
        _internalId = execRule._internalId;
        _internalValidatorId = execRule._internalValidatorId;
        _javaPath = execRule._javaPath;
        _conditions = execRule._conditions;
        _useAndForConditions = execRule._useAndForConditions;
        _dependencies = execRule._dependencies;
        _message = execRule._message;
        _ignored = execRule._ignored;
        _rawProperties = execRule._rawProperties;
        _potentialContextEntries = execRule._rawProperties;
        synchronized (this) {
            _id = execRule._id;
            _script = execRule._script;
            _checkForcedEntities = execRule._checkForcedEntities;
        }
    }

    /**
     * @return Returns the internalId.
     */
    public Long getInternalId() {
        return _internalId;
    }

    /**
     * @param internalId The internalId to set.
     */
    public void setInternalId(Long internalId) {
        this._internalId = internalId;
    }

    /**
     * @return Returns the internalValidatorId.
     */
    public Long getInternalValidatorId() {
        return _internalValidatorId;
    }

    /**
     * @param internalValidatorId The internalValidatorId to set.
     */
    public void setInternalValidatorId(Long internalValidatorId) {
        this._internalValidatorId = internalValidatorId;
    }

    /**
     * @return Returns the id.
     */
    public synchronized String getId() {
        return _id;
    }

    /**
     * @param id The id to set.
     */
    public synchronized void setId(String id) {
        this._id = id;
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
        this._conditions = conditions;
    }

    /**
     * Sets the rule expression.
     * <p/>
     * Created on Jun 29, 2011 by depryf
     * @param expression expression
     * @throws ConstructionException
     */
    public void setExpression(String expression) throws ConstructionException {
        synchronized (this) {
            try {
                _rawProperties.clear();
                _potentialContextEntries.clear();
                ValidatorServices.getInstance().parseExpression("rule", expression, _rawProperties, _potentialContextEntries, null);
                _script = ValidatorServices.getInstance().compileExpression(expression);
            }
            catch (CompilationFailedException e) {
                _script = null;
                throw new ConstructionException("Unable to compile rule " + _rule.getId(), e);
            }

            if (expression != null)
                _checkForcedEntities = expression.contains("forceFailureOnEntity") || expression.contains("forceFailureOnProperty") || expression.contains("ignoreFailureOnProperty");
        }
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
     * @throws ValidationException
     */
    public boolean validate(Validatable validatable, Binding binding) throws ValidationException {
        ExtraPropertyHandlerDto extra = null;
        synchronized (this) {
            if (_checkForcedEntities)
                extra = new ExtraPropertyHandlerDto();
        }

        // this is a bit convoluted, but we still want to set the failing properties even if an exception happens...
        ValidationException exception = null;

        boolean success;
        try {
            success = validateForGroovy(validatable, binding, extra);
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
                for (String property : _rawProperties)
                    if (extra == null || extra.getIgnoredProperties() == null || !extra.getIgnoredProperties().contains(property))
                        validatable.reportFailureForProperty(property);
                // also add any property that needs to be forced
                if (extra != null && extra.getForcedProperties() != null)
                    for (String property : extra.getForcedProperties())
                        validatable.reportFailureForProperty(property);
                // and finally, report the extra entities
                if (extra != null && extra.getForcedEntities() != null)
                    validatable.forceFailureForProperties(extra.getForcedEntities(), _rawProperties);
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
     * @throws ValidationException
     */
    @SuppressWarnings("unchecked")
    private synchronized boolean validateForGroovy(Validatable validatable, Binding binding, ExtraPropertyHandlerDto extra) throws ValidationException {
        if (_method != null) {
            boolean result;

            // http://www.tothenew.com/blog/compiling-groovy-code-statically/

            //System.out.println("Using method for " + _id);

            // clean up any leftover binding properties
            binding.setVariable(ValidationEngine.VALIDATOR_FORCE_FAILURE_ENTITY_KEY, null);
            binding.setVariable(ValidationEngine.VALIDATOR_FORCE_FAILURE_PROPERTY_KEY, null);
            binding.setVariable(ValidationEngine.VALIDATOR_IGNORE_FAILURE_PROPERTY_KEY, null);
            binding.setVariable(ValidationEngine.VALIDATOR_ERROR_MESSAGE, null);
            binding.setVariable(ValidationEngine.VALIDATOR_EXTRA_ERROR_MESSAGES, null);
            binding.setVariable(ValidationEngine.VALIDATOR_INFORMATION_MESSAGES, null);
            binding.setVariable(ValidationEngine.VALIDATOR_FAILING_FLAG, null);
            binding.setVariable(ValidationEngine.VALIDATOR_ORIGINAL_RESULT, null);

            List<Object> params = new ArrayList<>();
            params.add(binding);
            params.add(binding.getVariable("Context"));
            params.add(binding.getVariable("Functions"));
            String[] parts = StringUtils.split(_javaPath, ".");
            //System.out.println(binding.getVariables().keySet());
            for (int i = 0; i < parts.length; i++)
                params.add(binding.getVariable(parts[i]));

            try {
                //System.out.println("Trying to invoke " + _method.getName() + " with params " + params);
                result = (Boolean)_method.invoke(_clazz, params.toArray(new Object[0]));

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
            }
            catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                return false;
            }

            return result;
        }

        if (_script == null)
            return true;

        boolean success;

        try {
            // clean up any leftover binding properties
            binding.setVariable(ValidationEngine.VALIDATOR_FORCE_FAILURE_ENTITY_KEY, null);
            binding.setVariable(ValidationEngine.VALIDATOR_FORCE_FAILURE_PROPERTY_KEY, null);
            binding.setVariable(ValidationEngine.VALIDATOR_IGNORE_FAILURE_PROPERTY_KEY, null);
            binding.setVariable(ValidationEngine.VALIDATOR_ERROR_MESSAGE, null);
            binding.setVariable(ValidationEngine.VALIDATOR_EXTRA_ERROR_MESSAGES, null);
            binding.setVariable(ValidationEngine.VALIDATOR_INFORMATION_MESSAGES, null);
            binding.setVariable(ValidationEngine.VALIDATOR_FAILING_FLAG, null);
            binding.setVariable(ValidationEngine.VALIDATOR_ORIGINAL_RESULT, null);

            _script.setBinding(binding);

            //System.out.println(_rule.getId());
            Object result = _script.run();
            if (result instanceof Boolean)
                success = (Boolean)result;
            else
                throw new ValidationException("result is not a boolean");

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

            // keep track of what the logic actually returns
            binding.setVariable(ValidationEngine.VALIDATOR_ORIGINAL_RESULT, result);
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

        return success;
    }
}
