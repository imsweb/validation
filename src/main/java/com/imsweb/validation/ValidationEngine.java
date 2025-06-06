/*
 * Copyright (C) 2004 Information Management Services, Inc.
 */
package com.imsweb.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.imsweb.validation.entities.Category;
import com.imsweb.validation.entities.Condition;
import com.imsweb.validation.entities.ContextEntry;
import com.imsweb.validation.entities.EditableCondition;
import com.imsweb.validation.entities.EditableRule;
import com.imsweb.validation.entities.EditableValidator;
import com.imsweb.validation.entities.EmbeddedSet;
import com.imsweb.validation.entities.Rule;
import com.imsweb.validation.entities.RuleFailure;
import com.imsweb.validation.entities.RuleHistory;
import com.imsweb.validation.entities.Validatable;
import com.imsweb.validation.entities.Validator;
import com.imsweb.validation.internal.ExecutableCondition;
import com.imsweb.validation.internal.ExecutableRule;
import com.imsweb.validation.internal.IterativeProcessor;
import com.imsweb.validation.internal.Processor;
import com.imsweb.validation.internal.ValidatingProcessor;
import com.imsweb.validation.internal.callable.RuleCompilingCallable;
import com.imsweb.validation.runtime.CompiledRules;
import com.imsweb.validation.runtime.RuntimeUtils;

/**
 * This class is responsible for running loaded rules (edits) on {@link Validatable} objects and returning a collection of {@link RuleFailure} objects.
 * <br/><br/>
 * The first thing that needs to happen before using the engine for validation is to initialize the services and the context methods. This is accomplished
 * by calling the initialize method of the {@link ValidationServices} and {@link ValidationContextFunctions} classes. That method takes as an argument
 * an instance of those classes; to use the default implementation, you can instanciate those classes themselves:
 * <pre>
 *     ValidationServices.initialize(new ValidationServices());
 *     ValidationContextFunctions.initialize(new ValidationContextFunctions());
 * </pre>
 * But that is not required since those classes will be automatically initialized with those default classes the first time the getInstance() method is called
 * (if they have not been explicitly initialized yet).
 * <br/><br/>
 * More complex applications might need more customized services and extra context functions available to the Groovy edits; in that case those classes
 * should be extended and initialized with the customized versions (before any code tries to get their instance):
 * <pre>
 *     ValidationServices.initialize(new MyCustomValidatorServices());
 *     ValidationContextFunctions.initialize(new MyCustomValidatorContextFunctions());
 * </pre>
 * The second thing to do is to initialize the engine using one of its <b>initialize()</b> methods. Those methods take as argument an optional options object and one
 * or several {@link Validator} objects, which represent a logical group of {@link Rule} (edits); usually in a file. The {@link Validator} object can be built
 * programmatically or parsed from XML using the utility methods from the {@link ValidationXmlUtils} class.
 * <br/><br/>
 * Prior to version 2.0 of the library, the engine was a singleton class with static methods. As of 2.0, the engine is not static anymore and can be created via its
 * public constructor. A big advantages of that approach is to allow multiple engines to run concurrently in a given application. To be compatible with prior versions
 * and because most applications only need one instance of an engine, this class has a cached static engine available. It can always be called via the getInstance()
 * method and that instance is never null (but it does need to be initialized like any other instance).
 * <br/><br/>
 * After initializing the engine, the <b>validate()</b> methods can be called. They take as argument a {@link Validatable} object. Those objects are
 * application-dependent; the validation module only defines an interface (and a few simple implementations, like {@link com.imsweb.validation.entities.SimpleMapValidatable}
 * and {@link com.imsweb.validation.entities.SimpleNaaccrLinesValidatable}), it is the caller's responsibility to define the {@link Validatable} corresponding to their need
 * (for example one application might validate NAACCR records, but another one might validate tumor objects).
 * <br/><br/>
 * This class also contains several methods to allow updating the state of the engine dynamically (for example adding/removing rules, etc...). This is an
 * advanced feature; most applications only need to execute edits, not update them...
 * <p/>
 * Created on Apr 26, 2008 by Fabian Depry
 */
public class ValidationEngine {

    /**
     * Engine version (used to check compatibility with the edits)
     */
    private static final String _ENGINE_VERSION = "6.8";

    /**
     * The different context types supported by the engine
     */
    public static final String CONTEXT_TYPE_GROOVY = "groovy";
    public static final String CONTEXT_TYPE_JAVA = "java";
    public static final String CONTEXT_TYPE_TABLE = "table";
    public static final String CONTEXT_TYPE_TABLE_INDEX_DEF = "table-index-def";

    /**
     * Context key for the helper functions - Functions.
     */
    public static final String VALIDATOR_FUNCTIONS_KEY = "Functions";

    /**
     * Context key for the context objects (new context notation, introduced in version 4.3) -  Context.
     */
    public static final String VALIDATOR_CONTEXT_KEY = "Context";

    /**
     * Context key for the testing helper functions - Testing.
     */
    public static final String VALIDATOR_TESTING_FUNCTIONS_KEY = "Testing";

    /**
     * Context key for the force-failure-on-entity mechanism (used internally for the Genedits translated edits only).
     */
    public static final String VALIDATOR_FORCE_FAILURE_ENTITY_KEY = "__force_failure_on_entity_key";

    /**
     * Context key for the force-failure-on-entity mechanism (used internally for the Genedits translated edits only).
     */
    public static final String VALIDATOR_FORCE_FAILURE_PROPERTY_KEY = "__force_failure_on_property_key";

    /**
     * Context key for the force-failure-on-entity mechanism (used internally for the Genedits translated edits only).
     */
    public static final String VALIDATOR_IGNORE_FAILURE_PROPERTY_KEY = "__ignore_failure_on_property_key";

    /**
     * Context key to provide extra error messages (used internally for the Genedits translated edits only).
     */
    public static final String VALIDATOR_ERROR_MESSAGE = "__error_message";

    /**
     * Context key to provide extra error messages (used internally for the Genedits translated edits only).
     */
    public static final String VALIDATOR_EXTRA_ERROR_MESSAGES = "__extra_error_messages";

    /**
     * Context key to provide warning messages (used internally for the Genedits translated edits only).
     */
    public static final String VALIDATOR_INFORMATION_MESSAGES = "__information_messages";

    /**
     * Context key to set a flag indicating an edit failed (used internally for the Genedits translated edits only).
     */
    public static final String VALIDATOR_FAILING_FLAG = "__failing_flag";

    /**
     * The true result returned by the translated edits (it's possible for an edit to fail because of a set flag but still return true...)
     */
    public static final String VALIDATOR_ORIGINAL_RESULT = "__original_result";

    /**
     * Message used when an exception happened while executing a rule.
     */
    public static final String EXCEPTION_MSG = "Edit failed with exception.";

    /**
     * Message used when a rule doesn't define an error message.
     */
    @SuppressWarnings("unused")
    public static final String NO_MESSAGE_DEFINED_MSG = "No default error message defined.";

    /**
     * Cached instance of an engine; most applications should use this instance but some advance use of this framework might require multiple engine to run concurrently...
     */
    private static final ValidationEngine _INSTANCE = new ValidationEngine();

    /**
     * Returns the cached engine.
     */
    public static ValidationEngine getInstance() {
        return _INSTANCE;
    }

    /**
     * Map of <code>Validator</code>s, keyed by validator ID
     */
    protected Map<String, Validator> _validators = new HashMap<>();

    /**
     * Map of <code>Processor</code>s, keyed by java-path root
     */
    protected Map<String, ValidatingProcessor> _processors = new HashMap<>();

    /**
     * Currently used processor roots (for SEER, that would be "lines", for DMS it would be "patient", etc...); values are number of edits under that root
     */
    protected Map<String, AtomicInteger> _processorRoots = new HashMap<>();

    /**
     * Map of <code>ExecutableRule</code>s, keyed by rule internal ID
     */
    protected Map<Long, ExecutableRule> _executableRules = new HashMap<>();

    /**
     * Map of <code>ExecutableCondition</code>s, keyed by condition internal ID
     */
    protected Map<Long, ExecutableCondition> _executableConditions = new HashMap<>();

    /**
     * Compiled contexts, keyed by validator internal ID and context ID
     */
    protected Map<Long, Map<String, Object>> _contexts = new HashMap<>();

    /**
     * Possible statuses for the engine
     */
    private enum ValidationEngineStatus {
        /**
         * Validation engine status
         */
        NOT_INITIALIZED,
        /**
         * Validation engine status
         */
        INITIALIZING,
        /**
         * Validation engine status
         */
        INITIALIZED
    }

    /**
     * Current engine status
     */
    private ValidationEngineStatus _status = ValidationEngineStatus.NOT_INITIALIZED;

    /**
     * Initialization options
     */
    protected InitializationOptions _options;

    /**
     * Private lock controlling access to the state of the engine; all methods using the state of the engine (including the validate methods) need to acquire a read lock;
     * all methods changing the state of the engine need to acquire a write lock.
     */
    private final ReentrantReadWriteLock _lock = new ReentrantReadWriteLock();

    /**
     * The edits statistics gathered so far by the engine (the collection will be empty if the statistics are disabled in the initialization options)
     */
    protected Map<String, EngineStats> _editsStats = new HashMap<>();

    /**
     * Whether or not the edits statistics should be computed (initial value is based on the initialization options, but can be changed later)
     */
    protected boolean _computeEditsStats = false;

    /**
     * Private lock controlling access to the stats; those are "written" every time new stats are reported (which is constantly), and so using a global engine lock is not good enough.
     */
    private final ReentrantReadWriteLock _statsLock = new ReentrantReadWriteLock();

    // ********************************************************************************
    //                INITIALIZATION METHOD (require the write lock)
    // ********************************************************************************

    /**
     * Returns true if the engine has been initialized, false otherwise.
     * <p/>
     * Created on Aug 12, 2009 by depryf
     * @return true if the engine has been initialized, false otherwise.
     */
    public boolean isInitialized() {
        return _status == ValidationEngineStatus.INITIALIZED;
    }

    /**
     * Initializes this validation engine.
     * <p/>
     * Created on Mar 6, 2008 by depryf
     */
    public void initialize() {
        try {
            initialize(new InitializationOptions(), Collections.emptyList());
        }
        catch (ConstructionException e) {
            throw new IllegalStateException(e); // should never happen since we are not really initializing anything...
        }
    }

    /**
     * Initializes this validation engine.
     * <p/>
     * Created on Mar 6, 2008 by depryf
     * @param options initialization options
     * @return initialization statistics
     */
    public InitializationStats initialize(InitializationOptions options) {
        try {
            return initialize(options, Collections.emptyList());
        }
        catch (ConstructionException e) {
            throw new IllegalStateException(e); // should never happen since we are not really initializing anything...
        }
    }

    /**
     * Initializes this validation engine using the provided <code>Validator</code>.
     * <p/>
     * Created on Mar 6, 2008 by depryf
     * @param validator <code>Validator</code> to load
     * @return initialization statistics
     * @throws ConstructionException if a ... construction exception happens...
     */
    public InitializationStats initialize(Validator validator) throws ConstructionException {
        return initialize(new InitializationOptions(), Collections.singletonList(validator));
    }

    /**
     * Initializes this validation engine using the provided <code>Validator</code>.
     * <p/>
     * Created on Mar 6, 2008 by depryf
     * @param options initialization options
     * @param validator <code>Validator</code> to load
     * @return initialization statistics
     * @throws ConstructionException if a ... construction exception happens...
     */
    public InitializationStats initialize(InitializationOptions options, Validator validator) throws ConstructionException {
        return initialize(options, Collections.singletonList(validator));
    }

    /**
     * Initializes this validation engine using the provided list of <code>Validator</code>.
     * <p/>
     * Created on Mar 6, 2008 by depryf
     * @param validators list of <code>Validator</code> to load
     * @return initialization statistics
     * @throws ConstructionException if a ... construction exception happens...
     */
    public InitializationStats initialize(List<Validator> validators) throws ConstructionException {
        return initialize(new InitializationOptions(), validators);
    }

    /**
     * Initializes this validation engine using the provided list of <code>Validator</code>.
     * <p/>
     * Created on Mar 6, 2008 by depryf
     * @param options initialization options
     * @param validators list of <code>Validator</code> to load
     * @return initialization statistics
     * @throws ConstructionException if a ... construction exception happens...
     */
    public InitializationStats initialize(InitializationOptions options, List<Validator> validators) throws ConstructionException {
        _status = ValidationEngineStatus.INITIALIZING;

        InitializationStats stats = new InitializationStats();

        long start = System.currentTimeMillis();

        _lock.writeLock().lock();
        try {
            uninitialize();

            _options = options == null ? new InitializationOptions() : options;

            _computeEditsStats = _options.isEngineStatsEnabled();

            if (validators != null) {
                checkValidatorConstraints(validators);

                Map<Long, ExecutableRule> rules = new ConcurrentHashMap<>();
                Map<Long, ExecutableCondition> conditions = new ConcurrentHashMap<>();
                Map<Long, Map<String, Object>> allContexts = new ConcurrentHashMap<>();

                // internalize the validators (that will compile any Groovy, which could through a construction exception)
                for (Validator v : validators) {
                    Map<String, Object> contexts = new HashMap<>();
                    internalizeValidator(v, conditions, rules, contexts, stats);
                    allContexts.put(v.getValidatorId(), contexts);
                }

                // sort the rules by dependencies (this could throw a dependency exception)
                List<ExecutableRule> sortedRules = getRulesSortedByDependencies(rules, conditions);

                // at this point we checked everything, so let's update the internal state of the engine
                _executableConditions.putAll(conditions);
                _executableRules.putAll(rules);
                _contexts.putAll(allContexts);
                populateProcessors(sortedRules);

                // update the raw structure only if the state was successfully updated...
                for (Validator v : validators)
                    _validators.put(v.getId(), v);
            }
            else
                populateProcessors(null);
        }
        finally {
            _lock.writeLock().unlock();
        }

        _status = ValidationEngineStatus.INITIALIZED;

        stats.setInitializationDuration(System.currentTimeMillis() - start);

        return stats;
    }

    /**
     * Un-initializes the engine; the engine can't be used after this call, unless it is re-initialized.
     * <p/>
     * Created on Apr 15, 2010 by depryf
     */
    public void uninitialize() {
        _status = ValidationEngineStatus.NOT_INITIALIZED;

        _lock.writeLock().lock();
        try {
            _validators.clear();
            _processors.clear();
            _processorRoots.clear();
            _executableRules.clear();
            _executableConditions.clear();
            _contexts.clear();
        }
        finally {
            _lock.writeLock().unlock();
        }
    }

    // ********************************************************************************
    //                     GET METHODS (require the read lock)
    // ********************************************************************************

    /**
     * Returns all the <code>Validator</code>s contained in the engine, keyed by their ID.
     * <p/>
     * <b>ATTENTION</b>: any entities returned by this method should not be modified outside of the engine!
     * <p/>
     * Created on Jun 29, 2011 by depryf
     * @return all the <code>Validator</code>s contained in the engine, keyed by their ID; maybe empty but never null
     */
    public Map<String, Validator> getValidators() {
        _lock.readLock().lock();
        try {
            return Collections.unmodifiableMap(_validators);
        }
        finally {
            _lock.readLock().unlock();
        }
    }

    /**
     * Returns the <code>Validator</code> for the requested  ID.
     * <p/>
     * <b>ATTENTION</b>: any entities returned by this method should not be modified outside of the engine!
     * <p/>
     * Created on Jun 29, 2011 by depryf
     * @param validatorId validator ID
     * @return the <code>Validator</code> for the requested ID, null if not found
     */

    public Validator getValidator(String validatorId) {
        if (validatorId == null)
            return null;

        _lock.readLock().lock();
        try {
            return _validators.get(validatorId);
        }
        finally {
            _lock.readLock().unlock();
        }
    }

    /**
     * Returns the <code>Condition</code> for the requested ID.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param conditionId condition ID
     * @return requested <code>Condition</code>, null if not found
     */
    public Condition getCondition(String conditionId) {
        return getCondition(conditionId, null);
    }

    /**
     * Returns the <code>Condition</code> for the requested ID, in the requested validator.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param conditionId condition ID (if null, then null will be returned)
     * @param validatorId validator ID (if null then the condition will be searched among all the available validators)
     * @return requested <code>Condition</code>, null if not found
     */
    public Condition getCondition(String conditionId, String validatorId) {
        if (conditionId == null)
            return null;

        _lock.readLock().lock();
        try {
            if (validatorId != null) {
                Validator v = _validators.get(validatorId);
                if (v == null)
                    return null;
                return v.getCondition(conditionId);
            }

            for (Validator v : _validators.values()) {
                Condition c = v.getCondition(conditionId);
                if (c != null)
                    return c;
            }

            return null;
        }
        finally {
            _lock.readLock().unlock();
        }
    }

    /**
     * Returns the <code>Category</code> for the requested ID.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param categoryId category ID
     * @return requested <code>Category</code>, null if not found
     */
    public Category getCategory(String categoryId) {
        return getCategory(categoryId, null);
    }

    /**
     * Returns the <code>Category</code> for the requested ID, in the requested validator.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param categoryId category ID (if null, then null will be returned)
     * @param validatorId validator ID (if null then the category will be searched among all the available validators)
     * @return requested <code>Category</code>, null if not found
     */
    public Category getCategory(String categoryId, String validatorId) {
        if (categoryId == null)
            return null;

        _lock.readLock().lock();
        try {
            if (validatorId != null) {
                Validator v = _validators.get(validatorId);
                if (v == null)
                    return null;
                return v.getCategory(categoryId);
            }

            for (Validator v : _validators.values()) {
                Category c = v.getCategory(categoryId);
                if (c != null)
                    return c;
            }

            return null;
        }
        finally {
            _lock.readLock().unlock();
        }
    }

    /**
     * Returns the <code>Rule</code> for the requested ID.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param ruleId rule ID
     * @return requested <code>Rule</code>, null if not found
     */
    public Rule getRule(String ruleId) {
        return getRule(ruleId, null);
    }

    /**
     * Returns the <code>Rule</code> for the requested ID, in the requested validator.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param ruleId rule ID (if null, then null will be returned)
     * @param validatorId validator ID (if null then the rule will be searched among all the available validators)
     * @return requested <code>Rule</code>, null if not found
     */
    public Rule getRule(String ruleId, String validatorId) {
        if (ruleId == null)
            return null;

        _lock.readLock().lock();
        try {
            if (validatorId != null) {
                Validator v = _validators.get(validatorId);
                if (v == null)
                    return null;
                return v.getRule(ruleId);
            }

            for (Validator v : _validators.values()) {
                Rule r = v.getRule(ruleId);
                if (r != null)
                    return r;
            }

            return null;
        }
        finally {
            _lock.readLock().unlock();
        }
    }

    /**
     * Returns the compiled context for the requested key.
     * <p/>
     * Created on Jul 7, 2011 by depryf
     * @param contextKey context key (if null, then null will be returned)
     * @return requested compiled context, null if not found
     */
    public Object getContext(String contextKey) {
        return getContext(contextKey, null);
    }

    /**
     * Returns the compiled context for the requested key, in the requested validator.
     * <p/>
     * Created on Jul 7, 2011 by depryf
     * @param contextKey context key (if null, then null will be returned)
     * @param validatorId validator ID (if null then the context will be searched among all the available validators)
     * @return requested compiled context, null if not found
     */
    public Object getContext(String contextKey, String validatorId) {
        if (contextKey == null)
            return null;

        _lock.readLock().lock();
        try {
            if (validatorId != null) {
                Validator v = _validators.get(validatorId);
                if (v == null)
                    return null;
                return _contexts.get(v.getValidatorId()).get(contextKey);
            }

            for (Map<String, Object> context : _contexts.values()) {
                Object c = context.get(contextKey);
                if (c != null)
                    return c;
            }

            return null;
        }
        finally {
            _lock.readLock().unlock();
        }
    }

    // ********************************************************************************
    //                    VALIDATE METHODS (require the read lock)
    // ********************************************************************************

    /**
     * Validates the provided <code>Validatable</code> object using all the rules loaded in the engine.
     * <p/>
     * Note that a rule object itself can be flagged as ignored, in which case it will not run when this method is invoked.
     * <p/>
     * Created on Mar 6, 2008 by depryf
     * @param validatable a <code>Validatable</code>, cannot be null
     * @return a collection of <code>RuleFailure</code>, maybe empty but not null
     * @throws ValidationException if anything goes wrong during the validation
     */
    public Collection<RuleFailure> validate(Validatable validatable) throws ValidationException {
        _lock.readLock().lock();
        try {
            ValidatingContext vContext = new ValidatingContext();
            vContext.setComputeEditsStats(_computeEditsStats);
            return internalValidate(validatable, vContext);
        }
        finally {
            _lock.readLock().unlock();
        }
    }

    /**
     * Validates the provided <code>Validatable</code> object, ignoring the provided rule IDs.
     * <p/>
     * If a rule depends on an ignored rule, it will also be ignored.
     * <p/>
     * Note that a rule object itself can be flagged as ignored, in which case it will not run when this method is invoked.
     * <p/>
     * This method has been kept for compatibility, the preferred way is to use the one taking both a collection of rule IDs to
     * ignore and a collection of rule IDs to execute (optionally using null for one of them).
     * <p/>
     * Created on Mar 6, 2008 by depryf
     * @param validatable a <code>Validatable</code>, cannot be null
     * @param ruleIdsToIgnore rule IDs that need to be ignored
     * @return a collection of <code>RuleFailure</code>, maybe empty but not null
     * @throws ValidationException if anything goes wrong during the validation
     */
    public Collection<RuleFailure> validate(Validatable validatable, Collection<String> ruleIdsToIgnore) throws ValidationException {
        _lock.readLock().lock();
        try {
            ValidatingContext vContext = new ValidatingContext();
            vContext.setToIgnore(ruleIdsToIgnore);
            vContext.setComputeEditsStats(_computeEditsStats);
            return internalValidate(validatable, vContext);
        }
        finally {
            _lock.readLock().unlock();
        }
    }

    /**
     * Validates the provided <code>Validatable</code> object, executing the provided rule IDs or ignoring them.
     * <p/>
     * If a rule depends on an ignored rule, it will be ignored (in other words, the list of execute/ignored IDs doesn't influence the dependency mechanism).
     * <p/>
     * Note that a rule object itself can be flagged as ignored, in which case it will not run when this method is invoked (in other words, the list of execute/ignored IDs
     * doesn't influence the ignored flag of the individual rules).
     * <p/>
     * Either collection of IDs can be null (they can also be both null). If they are both non-null, only the execute one will be used.
     * <p/>
     * Created on Mar 6, 2008 by depryf
     * @param validatable a <code>Validatable</code>, cannot be null
     * @param ruleIdsToIgnore rule IDs that need to be ignored
     * @param ruleIdsToExecute rule IDs that need to be executed
     * @return a collection of <code>RuleFailure</code>, maybe empty but not null
     * @throws ValidationException if anything goes wrong during the validation
     */
    public Collection<RuleFailure> validate(Validatable validatable, Collection<String> ruleIdsToIgnore, Collection<String> ruleIdsToExecute) throws ValidationException {
        _lock.readLock().lock();
        try {
            ValidatingContext vContext = new ValidatingContext();
            vContext.setToIgnore(ruleIdsToIgnore);
            vContext.setToExecute(ruleIdsToExecute);
            vContext.setComputeEditsStats(_computeEditsStats);
            return internalValidate(validatable, vContext);
        }
        finally {
            _lock.readLock().unlock();
        }
    }

    /**
     * Validates the provided <code>Validatable</code> object, running only the single provided rule ID, which must be an exising rule within the engine.
     * <p/>
     * Note that a rule object itself can be flagged as ignored, but if a rule is forced to run through this method, its ignore flag won't be used. Similarly,
     * the condition referenced by the rule (if one is defined) won't be executed. A typical situation for using this method would be for testing a particular rule,
     * independently of the ignore flags, dependencies or condition.
     * <p/>
     * Because this method uses an existing rule, the engine won't have to create a runtime version of the rule (it will use the one already exisitng);
     * therefore this method is the one to use when doing batch testing (running many tests on many rules).
     * <p/>
     * Created on Mar 6, 2008 by depryf
     * @param validatable a <code>Validatable</code>, cannot be null
     * @param ruleId ID of the rule that needs to be executed, cannot be null, must be a rule existing in the engine
     * @return a collection of <code>RuleFailure</code>, maybe empty but not null
     * @throws ValidationException if anything goes wrong during the validation
     */
    public Collection<RuleFailure> validate(Validatable validatable, String ruleId) throws ValidationException {
        _lock.readLock().lock();
        try {
            Rule rule = getRule(ruleId);
            if (rule == null)
                throw new IllegalStateException("Unknown rule ID: " + ruleId);
            ValidatingContext vContext = new ValidatingContext();
            vContext.setToForce(rule);
            vContext.setComputeEditsStats(_computeEditsStats);
            return internalValidate(validatable, vContext);
        }
        finally {
            _lock.readLock().unlock();
        }
    }

    /**
     * Validates the provided <code>Validatable</code> object, running only the single provided rule. The rule must have a valid java path set.
     * <p/>
     * Note that a rule object itself can be flagged as ignored, but if a rule is forced to run through this method, its ignore flag won't be used. Similarly,
     * the condition referenced by the rule (if one is defined) won't be executed. A typical situation for using this method would be for testing a particular rule,
     * independently of the ignore flags, dependencies or condition.
     * <p/>
     * Whether the provided rule already exists in the engine or not, this method will force the engine to create a runtime version of the rule. This is useful
     * to run a test on a modified version of an existing rule, or on a rule that doesn't exist in the engine yet. The downside is that this is an expensive
     * operation that will be very inefficient for doing batch testing (running many tests on many rules).
     * <p/>
     * Created on Mar 6, 2008 by depryf
     * @param validatable a <code>Validatable</code>, cannot be null
     * @param rule a <code>Rule</code> object, cannot be null, it doesn't have to exist in the validation engine
     * @return a collection of <code>RuleFailure</code>, maybe empty but not null
     * @throws ValidationException if anything goes wrong during the validation
     */
    public Collection<RuleFailure> validate(Validatable validatable, Rule rule) throws ValidationException {
        _lock.readLock().lock();
        try {
            if (rule == null)
                throw new IllegalStateException("This method requires a non-null rule!");
            if (rule.getJavaPath() == null)
                throw new IllegalStateException("The provided rule must have a java-path!");

            ValidatingContext vContext = new ValidatingContext();
            vContext.setToForce(rule);
            vContext.setComputeEditsStats(_computeEditsStats);
            return internalValidate(validatable, vContext);
        }
        finally {
            _lock.readLock().unlock();
        }
    }

    /**
     * Validates the provided <code>Validatable</code> object using all the rules loaded in the engine.
     * <p/>
     * Note that a rule object itself can be flagged as ignored, in which case it will not run when this method is invoked.
     * <p/>
     * Created on Mar 6, 2008 by depryf
     * @param validatable a <code>Validatable</code>, cannot be null
     * @param vContext a <code>ValidatingContext</code>, cannot be null. All the other validate methods are convenience methods, this one is the main one.
     * Using a validating context allows the caller to have access to some information that is gathered during the validation; for example,
     * all the failed rules and failed conditions can be accessed through the context once the method returns...
     * @return a collection of <code>RuleFailure</code>, maybe empty but not null
     * @throws ValidationException if anything goes wrong during the validation
     */
    public Collection<RuleFailure> validate(Validatable validatable, ValidatingContext vContext) throws ValidationException {
        _lock.readLock().lock();
        try {
            vContext.setComputeEditsStats(_computeEditsStats);
            return internalValidate(validatable, vContext);
        }
        finally {
            _lock.readLock().unlock();
        }
    }

    // ********************************************************************************
    //              ADD/DELETE/UPDATE METHODS (require the write lock
    // ********************************************************************************

    /**
     * Updates an existing rule in the engine.
     * <p/>
     * The following steps should be performed:
     * <ol>
     * <li>Create an editable rule by using the EditableRule's default constructor. </li>
     * <li>Modify the editable rule (this would correspond to changes done in a GUI by a user)</li>
     * <li>Call this method using the editable rule</li>
     * </ol>
     * <p/>
     * Once this method returns (and if no exception were thrown), the new rule will be accessible by using the getRule() method.
     * <p/>
     * Created on Jun 29, 2011 by depryf
     * @param editableRule <code>EditableRule</code>, cannot be null
     * @return the created <code>Rule</code>
     * @throws ConstructionException if the rule contains an error
     */
    public Rule addRule(EditableRule editableRule) throws ConstructionException {
        _lock.writeLock().lock();
        try {

            if (editableRule == null)
                throw new ConstructionException("An editable rule is required for adding a new edit");
            if (editableRule.getId() == null)
                throw new ConstructionException("An edit ID is required when adding a new edit");
            if (editableRule.getJavaPath() == null)
                throw new ConstructionException("A java-path is required when adding a new edit");
            if (editableRule.getValidatorId() == null)
                throw new ConstructionException("A group is required when adding a new edit");
            if (editableRule.getMessage() == null)
                throw new ConstructionException("A message is required when adding a new edit");
            if (getRule(editableRule.getId()) != null)
                throw new ConstructionException("Edit IDs must be unique within the edits engine, cannot add '" + editableRule.getId() + "'");
            if (!_validators.containsKey(editableRule.getValidatorId()))
                throw new ConstructionException("Unknown group: " + editableRule.getValidatorId());
            if (!ValidationServices.getInstance().getAllJavaPaths().containsKey(editableRule.getJavaPath()))
                throw new ConstructionException("Unknown java-path: " + editableRule.getJavaPath());

            // verify the condition exists if provided
            if (editableRule.getConditions() != null) {
                for (String conditionId : editableRule.getConditions()) {
                    Condition condition = getCondition(conditionId, null); // passing null for the validator ID to allow cross-validator conditions (used in SEER*DMS)
                    if (condition == null)
                        throw new ConstructionException("Unknown condition: " + conditionId);
                }
            }

            // verify the category exists if provided
            if (editableRule.getCategory() != null) {
                Category category = getCategory(editableRule.getCategory(), null); // passing null for the validator ID to allow cross-validator conditions (used in SEER*DMS)
                if (category == null)
                    throw new ConstructionException("Unknown category: " + editableRule.getCategory());
            }

            // create the rule to add
            Rule rule = new Rule();
            rule.setId(editableRule.getId());
            rule.setRuleId(editableRule.getRuleId());
            if (rule.getRuleId() == null)
                rule.setRuleId(ValidationServices.getInstance().getNextRuleSequence());
            rule.setName(editableRule.getName());
            rule.setJavaPath(editableRule.getJavaPath());
            rule.setExpression(editableRule.getExpression());
            rule.setMessage(editableRule.getMessage());
            if (editableRule.getIgnored() != null)
                rule.setIgnored(editableRule.getIgnored());
            if (editableRule.getSeverity() != null)
                rule.setSeverity(editableRule.getSeverity());
            rule.setConditions(editableRule.getConditions());
            rule.setUseAndForConditions(editableRule.getUseAndForConditions());
            rule.setCategory(editableRule.getCategory());
            rule.setTag(editableRule.getTag());
            rule.setAgency(editableRule.getAgency());
            rule.setAllowOverride(editableRule.getAllowOverride());
            rule.setNeedsReview(editableRule.getNeedsReview());
            rule.setImportEditFlag(editableRule.getImportEditFlag());
            rule.setDataEntryTypes(editableRule.getDataEntryTypes());
            rule.setDataLevel(editableRule.getDataLevel());
            rule.setDescription(editableRule.getDescription());
            rule.setDependencies(editableRule.getDependencies());
            rule.setHistories(editableRule.getHistories());
            rule.setValidator(_validators.get(editableRule.getValidatorId()));

            // create an executable rule from it
            ExecutableRule execRule = new ExecutableRule(rule);

            // update the dependencies; make sure we don't leave the internal structures in a bad state if something goes wrong...
            Map<Long, ExecutableRule> rules = new HashMap<>(_executableRules);
            rules.put(execRule.getInternalId(), execRule);
            List<ExecutableRule> sortedRules = getRulesSortedByDependencies(rules, _executableConditions); // this will validate the rule dependencies...
            _executableRules.put(execRule.getInternalId(), execRule);

            // update the processors after re-evaluating the rules order (if the new java path doesn't exist, re-populate all the processors)
            if (!_processors.containsKey(editableRule.getJavaPath()))
                populateProcessors(sortedRules);
            else
                updateProcessorsRules(sortedRules); // this is way less expensive than re-populating the processors...

            // update raw data
            _validators.get(editableRule.getValidatorId()).getRules().add(rule);

            // update the inverted dependencies in the raw data
            if (editableRule.getDependencies() != null && !editableRule.getDependencies().isEmpty())
                for (Rule r : rule.getValidator().getRules())
                    if (rule.getDependencies().contains(r.getId()))
                        r.getInvertedDependencies().add(rule.getId());

            return rule;
        }
        finally {
            _lock.writeLock().unlock();
        }
    }

    /**
     * Updates an existing rule in the engine.
     * <p/>
     * The following steps should be performed:
     * <ol>
     * <li>Get the rule to update using the getRule() method</li>
     * <li>Wrap the rule into an editable rule by passing it to the EditableRule's constructor. </li>
     * <li>Modify the editable rule (this would correspond to changes done in a GUI by a user)</li>
     * <li>Call this method using the editable rule</li>
     * </ol>
     * <b>No modification should be done on the Rule object itself, only the editable rule should be modified!</b>
     * <p/>
     * Once this method returns (and if no exception were thrown), the rule will be accessible by using the getRule() method and
     * all the requested modifications will have been applied to it.
     * <p/>
     * Note that the ruleId property should not be modified under any circumstances since it is how the engine knows
     * which edit needs to be updated. It is an internal identifier.
     * <p/>
     * Created on Jun 29, 2011 by depryf
     * @param editableRule <code>EditableRule</code>, cannot be null
     * @throws ConstructionException if the rule contains an error
     */
    public void updateRule(EditableRule editableRule) throws ConstructionException {
        _lock.writeLock().lock();
        try {
            if (editableRule == null)
                throw new ConstructionException("An editable rule is required for modifying an edit");
            if (editableRule.getRuleId() == null)
                throw new ConstructionException("An internal ID is required when modifying an edit");
            if (editableRule.getId() == null)
                throw new ConstructionException("An edit ID is required when modifying an edit");
            if (editableRule.getValidatorId() == null)
                throw new ConstructionException("A group is required when modifying an edit");
            if (editableRule.getMessage() == null)
                throw new ConstructionException("A message is required when modifying an edit");
            if (!_validators.containsKey(editableRule.getValidatorId()))
                throw new ConstructionException("Unknown group: " + editableRule.getValidatorId());
            if (!ValidationServices.getInstance().getAllJavaPaths().containsKey(editableRule.getJavaPath()))
                throw new ConstructionException("Unknown java-path: " + editableRule.getJavaPath());

            // get original executable rule
            ExecutableRule originalExecRule = _executableRules.get(editableRule.getRuleId());
            if (originalExecRule == null)
                throw new ConstructionException("Validation Engine does not contain requested edit");

            // get the rule to update
            Rule rule = getRule(originalExecRule.getId());
            if (rule == null)
                throw new ConstructionException("Validation Engine does not contain requested edit");

            // verify the condition exists if provided
            if (editableRule.getConditions() != null) {
                for (String conditionId : editableRule.getConditions()) {
                    Condition condition = getCondition(conditionId, null); // passing null for the validator ID to allow cross-validator conditions (used in SEER*DMS)
                    if (condition == null)
                        throw new ConstructionException("Unknown condition: " + conditionId);
                }
            }

            // verify the category exists if provided
            if (editableRule.getCategory() != null) {
                Category category = getCategory(editableRule.getCategory(), null); // passing null for the validator ID to allow cross-validator conditions (used in SEER*DMS)
                if (category == null)
                    throw new ConstructionException("Unknown category: " + editableRule.getCategory());
            }

            // check ID unicity
            if (!editableRule.getId().equals(rule.getId()))
                if (getRule(editableRule.getId()) != null)
                    throw new ConstructionException("Edit IDs must be unique within the edits engine, cannot add '" + editableRule.getId() + "'");

            boolean idUpdated = !editableRule.getId().equals(rule.getId());
            boolean expressionUpdated = editableRule.getExpression() == null || !editableRule.getExpression().equals(rule.getExpression());
            boolean dependenciesUpdated = editableRule.getDependencies() == null || !editableRule.getDependencies().equals(rule.getDependencies());
            boolean historiesUpdated = editableRule.getHistories() == null || !editableRule.getHistories().equals(rule.getHistories());

            // create an executable rule and update the requested properties (the cheap one are always updated, other ones have a pre-condition)
            ExecutableRule execRule = new ExecutableRule(originalExecRule);
            if (idUpdated)
                execRule.setId(editableRule.getId());
            if (expressionUpdated)
                execRule.setExpression(editableRule.getExpression());
            execRule.setMessage(editableRule.getMessage());
            execRule.setIgnored(editableRule.getIgnored() == null ? Boolean.FALSE : editableRule.getIgnored());
            if (dependenciesUpdated)
                execRule.setDependencies(editableRule.getDependencies() == null ? Collections.emptySet() : editableRule.getDependencies());
            execRule.setConditions(editableRule.getConditions());
            execRule.setUseAndForConditions(editableRule.getUseAndForConditions());
            execRule.setJavaPath(editableRule.getJavaPath());

            // update the dependencies; make sure we don't leave the internal structures in a bad state if something goes wrong...
            Map<Long, ExecutableRule> rules = new HashMap<>(_executableRules);
            rules.put(execRule.getInternalId(), execRule);
            List<ExecutableRule> sortedRules = getRulesSortedByDependencies(rules, _executableConditions); // this will validate the rule dependencies...
            _executableRules.put(execRule.getInternalId(), execRule);

            // update the processors after re-evaluating the rules order (if the new java path doesn't exist, re-populate all the processors)
            if (!_processors.containsKey(editableRule.getJavaPath()))
                populateProcessors(sortedRules);
            else
                updateProcessorsRules(sortedRules); // this is way less expensive than re-populating the processors...

            // update the raw data
            rule.setId(editableRule.getId());
            rule.setName(editableRule.getName());
            rule.setExpression(editableRule.getExpression());
            rule.setMessage(editableRule.getMessage());
            rule.setIgnored(editableRule.getIgnored() == null ? Boolean.FALSE : editableRule.getIgnored());
            rule.setDescription(editableRule.getDescription());
            rule.setJavaPath(editableRule.getJavaPath());
            rule.setConditions(editableRule.getConditions());
            rule.setUseAndForConditions(editableRule.getUseAndForConditions());
            rule.setCategory(editableRule.getCategory());
            rule.setTag(editableRule.getTag());
            rule.setAgency(editableRule.getAgency());
            rule.setAllowOverride(editableRule.getAllowOverride());
            rule.setNeedsReview(editableRule.getNeedsReview());
            rule.setImportEditFlag(editableRule.getImportEditFlag());
            rule.setDataEntryTypes(editableRule.getDataEntryTypes());
            rule.setDataLevel(editableRule.getDataLevel());
            if (editableRule.getSeverity() != null)
                rule.setSeverity(editableRule.getSeverity());
            if (dependenciesUpdated) {
                Set<String> dependencies = new HashSet<>();
                if (editableRule.getDependencies() != null)
                    dependencies.addAll(editableRule.getDependencies());
                rule.setDependencies(new HashSet<>(dependencies));
            }
            if (historiesUpdated) {
                Set<RuleHistory> histories = new HashSet<>();
                if (editableRule.getHistories() != null) {
                    for (RuleHistory hist : editableRule.getHistories()) {
                        hist.setRule(rule);
                        histories.add(hist);
                    }
                }
                rule.setHistories(histories);
            }

            // update the inverted dependencies in the raw data
            if (dependenciesUpdated) {
                for (Rule r : rule.getValidator().getRules()) {
                    if (rule.getDependencies().contains(r.getId()))
                        r.getInvertedDependencies().add(rule.getId());
                    else
                        r.getInvertedDependencies().remove(rule.getId());
                }
            }
        }
        finally {
            _lock.writeLock().unlock();
        }
    }

    /**
     * Deletes an existing rule from the engine.
     * <p/>
     * This is a convenient method that only takes the rule ID as a parameter.
     * <p/>
     * Created on Jul 7, 2011 by depryf
     * @param ruleId rule ID to delete
     * @throws ConstructionException if the rule cannot be found
     */
    public void deleteRule(String ruleId) throws ConstructionException {
        _lock.writeLock().lock();
        try {
            Rule r = getRule(ruleId);
            if (r == null)
                throw new ConstructionException("Unknown edit: " + ruleId);
            deleteRule(new EditableRule(r));
        }
        finally {
            _lock.writeLock().unlock();
        }
    }

    /**
     * Deletes an existing rule from the engine.
     * <p/>
     * The following steps should be performed:
     * <ol>
     * <li>Get the rule to delete using the getRule() method</li>
     * <li>Wrap the rule into an editable rule by passing it to the EditableRule's constructor. </li>
     * <li>Call this method using the editable rule</li>
     * </ol>
     * <p/>
     * Created on Jun 29, 2011 by depryf
     * @param editableRule <code>EditableRule</code>, cannot be null
     */
    public void deleteRule(EditableRule editableRule) throws ConstructionException {
        _lock.writeLock().lock();
        try {
            if (editableRule == null)
                throw new ConstructionException("An editable rule is required for deleting an edit");
            if (editableRule.getRuleId() == null)
                throw new ConstructionException("An internal edit ID is required when deleting an edit");
            if (editableRule.getId() == null)
                throw new ConstructionException("An edit ID is required when deleting an edit");
            if (editableRule.getValidatorId() == null)
                throw new ConstructionException("A group is required when deleting an edit");
            if (!_validators.containsKey(editableRule.getValidatorId()))
                throw new ConstructionException("Unknown group: " + editableRule.getValidatorId());

            for (Rule r : _validators.get(editableRule.getValidatorId()).getRules())
                if (r.getDependencies().contains(editableRule.getId()))
                    throw new ConstructionException(editableRule.getId() + " cannot be deleted, " + r.getId() + " depends on it");

            // get the rule
            Rule rule = getRule(editableRule.getId(), editableRule.getValidatorId());
            if (rule == null)
                throw new ConstructionException("Validation Engine does not contain requested edit");

            // update the executable rule
            _executableRules.remove(rule.getRuleId());

            // update the processors after re-evaluating the rules order
            updateProcessorsRules(getRulesSortedByDependencies(_executableRules, _executableConditions));

            // update raw data
            _validators.get(editableRule.getValidatorId()).getRules().remove(rule);

            // update the inverted dependencies in the raw data
            for (Rule r : rule.getValidator().getRules())
                if (rule.getDependencies().contains(r.getId()))
                    r.getInvertedDependencies().remove(rule.getId());
        }
        finally {
            _lock.writeLock().unlock();
        }
    }

    /**
     * Adds a new condition in the engine.
     * <p/>
     * The following steps should be performed:
     * <ol>
     * <li>Create an editable condition by using the EditableCondition's default constructor. </li>
     * <li>Modify the editable condition (this would correspond to changes done in a GUI by a user)</li>
     * <li>Call this method using the editable condition</li>
     * </ol>
     * <p/>
     * Once this method returns (and if no exception were thrown), the new condition will be accessible by using the getCondition() method.
     * <p/>
     * Created on Jun 29, 2011 by depryf
     * @param editableCondition <code>EditableCondition</code>, cannot be null
     * @return the created <code>Condition</code>
     * @throws ConstructionException if the condition contains an error
     */
    public Condition addCondition(EditableCondition editableCondition) throws ConstructionException {
        _lock.writeLock().lock();
        try {
            if (editableCondition == null)
                throw new ConstructionException("An editable condition is required for adding a new condition");
            if (editableCondition.getId() == null)
                throw new ConstructionException("A condition ID is required when adding a new condition");
            if (editableCondition.getValidatorId() == null)
                throw new ConstructionException("A group is required when adding a new condition");
            if (editableCondition.getJavaPath() == null)
                throw new ConstructionException("A java-path is required when adding a new condition");
            if (getCondition(editableCondition.getId()) != null)
                throw new ConstructionException("Condition IDs must be unique within the edits engine, cannot add '" + editableCondition.getId() + "'");
            if (!_validators.containsKey(editableCondition.getValidatorId()))
                throw new ConstructionException("Unknown group: " + editableCondition.getValidatorId());
            if (!ValidationServices.getInstance().getAllJavaPaths().containsKey(editableCondition.getJavaPath()))
                throw new ConstructionException("Unknown java-path: " + editableCondition.getJavaPath());

            // create the condition to add
            Condition condition = new Condition();
            condition.setId(editableCondition.getId());
            condition.setConditionId(editableCondition.getConditionId());
            if (condition.getConditionId() == null)
                condition.setConditionId(ValidationServices.getInstance().getNextConditionSequence());
            condition.setId(editableCondition.getId());
            condition.setName(editableCondition.getName());
            condition.setDescription(editableCondition.getDescription());
            condition.setJavaPath(editableCondition.getJavaPath());
            condition.setExpression(editableCondition.getExpression());
            condition.setValidator(_validators.get(editableCondition.getValidatorId()));

            // create the executable condition
            ExecutableCondition execCondition = new ExecutableCondition(condition);

            // update internal state
            _executableConditions.put(execCondition.getInternalId(), execCondition);

            // update the processors (if the new java path doesn't exist, re-populate all the processors)
            if (!_processors.containsKey(editableCondition.getJavaPath()))
                populateProcessors(getRulesSortedByDependencies(_executableRules, _executableConditions));
            else
                updateProcessorsConditions(_executableConditions.values()); // this is way less expensive than re-populating the processors...

            // update the raw structure only if the state was successfully updated...
            _validators.get(editableCondition.getValidatorId()).getConditions().add(condition);

            return condition;
        }
        finally {
            _lock.writeLock().unlock();
        }
    }

    /**
     * Updates an existing condition in the engine.
     * <p/>
     * The following steps should be performed:
     * <ol>
     * <li>Get the condition to update using the getCondition() method</li>
     * <li>Wrap the condition into an editable condition by passing it to the EditableCondition's constructor. </li>
     * <li>Modify the editable condition (this would correspond to changes done in a GUI by a user)</li>
     * <li>Call this method using the editable condition</li>
     * </ol>
     * <b>No modification should be done on the condition object itself, only the editable condition should be modified!</b>
     * <p/>
     * Once this method returns (and if no exception were thrown), the condition will be accessible by using the getCondition() method and
     * all the requested modifications will have been applied to it.
     * <p/>
     * Created on Jun 29, 2011 by depryf
     * @param editableCondition <code>EditableCondition</code>, cannot be null
     * @throws ConstructionException if the condition contains an error
     */
    public void updateCondition(EditableCondition editableCondition) throws ConstructionException {
        _lock.writeLock().lock();
        try {
            if (editableCondition == null)
                throw new ConstructionException("An editable condition is required for modifying an condition");
            if (editableCondition.getConditionId() == null)
                throw new ConstructionException("An internal ID is required when modifying a condition");
            if (editableCondition.getId() == null)
                throw new ConstructionException("A category ID is required when modifying a condition");
            if (editableCondition.getValidatorId() == null)
                throw new ConstructionException("A group is required when modifying a condition");
            if (editableCondition.getJavaPath() == null)
                throw new ConstructionException("A java-path is required when adding a condition");
            if (!_processorRoots.containsKey(StringUtils.split(editableCondition.getJavaPath(), '.')[0]))
                throw new ConstructionException("Invalid java-path");
            if (!_validators.containsKey(editableCondition.getValidatorId()))
                throw new ConstructionException("Unknown group: " + editableCondition.getValidatorId());
            if (!ValidationServices.getInstance().getAllJavaPaths().containsKey(editableCondition.getJavaPath()))
                throw new ConstructionException("Unknown java-path: " + editableCondition.getJavaPath());

            // get original executable condition
            ExecutableCondition originalExecCondition = _executableConditions.get(editableCondition.getConditionId());
            if (originalExecCondition == null)
                throw new ConstructionException("Unknown condition: " + editableCondition.getId());

            // get the condition
            Condition condition = getCondition(originalExecCondition.getId());
            if (condition == null)
                throw new ConstructionException("Unknown condition: " + editableCondition.getId());

            // check condition unicity
            if (!condition.getId().equals(editableCondition.getId()))
                if (getCondition(editableCondition.getId()) != null)
                    throw new ConstructionException("Condition IDs must be unique within the edits engine, cannot update ID to '" + editableCondition.getId() + "'");

            // create the executable condition
            ExecutableCondition execCondition = new ExecutableCondition(originalExecCondition);
            execCondition.setId(editableCondition.getId());
            execCondition.setJavaPath(editableCondition.getJavaPath());
            if ((condition.getExpression() == null && editableCondition.getExpression() != null) || (condition.getExpression() != null && !condition.getExpression().equals(
                    editableCondition.getExpression())))
                execCondition.setExpression(editableCondition.getExpression());

            // update internal state
            _executableConditions.put(execCondition.getInternalId(), execCondition);

            // update the processors (if the new java path doesn't exist, re-populate all the processors)
            if (!_processors.containsKey(editableCondition.getJavaPath()))
                populateProcessors(getRulesSortedByDependencies(_executableRules, _executableConditions));
            else
                updateProcessorsConditions(_executableConditions.values()); // this is way less expensive than re-populating the processors...

            // update the raw structure only if the state was successfully updated...
            condition.setId(editableCondition.getId());
            condition.setValidator(_validators.get(editableCondition.getValidatorId()));
            condition.setName(editableCondition.getName());
            condition.setDescription(editableCondition.getDescription());
            condition.setJavaPath(editableCondition.getJavaPath());
            condition.setExpression(editableCondition.getExpression());
        }
        finally {
            _lock.writeLock().unlock();
        }
    }

    /**
     * Deletes an existing condition from the engine.
     * <p/>
     * This is a convenient method that only takes the rule ID as a parameter.
     * <p/>
     * Created on Jul 7, 2011 by depryf
     * @param conditionId condition ID to delete
     * @throws ConstructionException if the condition cannot be found
     */
    public void deleteCondition(String conditionId) throws ConstructionException {
        _lock.writeLock().lock();
        try {
            Condition condition = getCondition(conditionId);
            if (condition == null)
                throw new ConstructionException("Unknown condition: " + conditionId);
            deleteCondition(new EditableCondition(condition));
        }
        finally {
            _lock.writeLock().unlock();
        }
    }

    /**
     * Deletes an existing condition from the engine.
     * <p/>
     * The following steps should be performed:
     * <ol>
     * <li>Get the condition to delete using the getCondition() method</li>
     * <li>Wrap the condition into an editable condition by passing it to the EditableCondition's constructor. </li>
     * <li>Call this method using the editable condition</li>
     * </ol>
     * <p/>
     * Created on Jun 29, 2011 by depryf
     * @param editableCondition <code>EditableCondition</code>, cannot be null
     */
    public void deleteCondition(EditableCondition editableCondition) throws ConstructionException {
        _lock.writeLock().lock();
        try {
            // get the condition
            Condition condition = getCondition(editableCondition.getId());
            if (condition == null)
                throw new ConstructionException("Unknown condition: " + editableCondition.getId());

            // update internal state
            _executableConditions.remove(editableCondition.getConditionId());
            updateProcessorsConditions(_executableConditions.values());

            // update the raw structure only if the state was successfully updated...
            _validators.get(editableCondition.getValidatorId()).getConditions().remove(condition);
        }
        finally {
            _lock.writeLock().unlock();
        }
    }

    /**
     * Adds a new validator in the engine.
     * <p/>
     * The following steps should be performed:
     * <ol>
     * <li>Create a new Validator object by loading it from XML using the ValidationXmlUtils, or create it programmatically</li>
     * <li>Wrap the validator into an editable validator by passing it to the EditableValidator's constructor. </li>
     * <li>Call this method using the editable validator</li>
     * </ol>
     * <p/>
     * Once this method returns (and if no exception were thrown), the new validator will be accessible by using the getValidator() method.
     * <p/>
     * Created on Jun 29, 2011 by depryf
     * @param editableValidator <code>EditableValidator</code>, cannot be null
     * @return the created <code>Validator</code>
     * @throws ConstructionException if the validator contains an error
     */
    public Validator addValidator(EditableValidator editableValidator) throws ConstructionException {
        _lock.writeLock().lock();
        try {
            if (getValidator(editableValidator.getId()) != null)
                throw new ConstructionException("Group IDs must be unique within the edits engine, cannot add '" + editableValidator.getId() + "'");

            // create the validator to add
            Validator v = new Validator();
            v.setValidatorId(editableValidator.getValidatorId());
            if (v.getValidatorId() == null)
                v.setValidatorId(ValidationServices.getInstance().getNextValidatorSequence());
            v.setId(editableValidator.getId());
            v.setName(editableValidator.getName());
            v.setReleases(editableValidator.getReleases());
            v.setVersion(v.getReleases() == null || v.getReleases().isEmpty() ? null : v.getReleases().last().getVersion().getRawString());
            v.setHash(v.getHash());
            v.setRawContext(editableValidator.getRawContext());
            v.setCategories(editableValidator.getCategories());
            v.setConditions(editableValidator.getConditions());
            v.setRules(editableValidator.getRules());

            // internalize the validators (that will compile any Groovy, which could through a construction exception)
            Map<Long, ExecutableCondition> conditions = new ConcurrentHashMap<>();
            Map<Long, ExecutableRule> rules = new ConcurrentHashMap<>();
            Map<String, Object> contexts = new ConcurrentHashMap<>();
            internalizeValidator(v, conditions, rules, contexts, null);

            // add the existing rules and conditions
            conditions.putAll(_executableConditions);
            rules.putAll(_executableRules);

            // sort the rules by dependencies (this could though a dependency exception)
            List<ExecutableRule> sortedRules = getRulesSortedByDependencies(rules, conditions);

            // at this point we checked everything, so let's update the internal state of the engine
            _executableConditions.putAll(conditions);
            _executableRules.putAll(rules);
            _contexts.put(v.getValidatorId(), contexts);
            populateProcessors(sortedRules);

            // update the raw structure only if the state was successfully updated...
            _validators.put(v.getId(), v);

            return v;
        }
        finally {
            _lock.writeLock().unlock();
        }
    }

    /**
     * Updates an existing validator in the engine.
     * <p/>
     * The following steps should be performed:
     * <ol>
     * <li>Get the validator to update using the getValidator() method</li>
     * <li>Wrap the validator into an editable validator by passing it to the EditableValidator constructor.</li>
     * <li>Modify the editable validator (this would correspond to changes done in a GUI by a user)</li>
     * <li>Call this method using the editable validator</li>
     * </ol>
     * <b>No modification should be done on the Validator object itself, only the editable validator should be modified!</b>
     * <p/>
     * Once this method returns (and if no exception were thrown), the validator will be accessible by using the getValidator() method and
     * all the requested modifications will have been applied to it.
     * <p/>
     * Created on Jun 29, 2011 by depryf
     * @param editableValidator <code>EditableValidator</code>, cannot be null
     * @throws ConstructionException if the validator contains an error
     */
    public void updateValidator(EditableValidator editableValidator) throws ConstructionException {
        _lock.writeLock().lock();
        try {
            // get the validator
            Validator v = null;
            for (Validator val : _validators.values())
                if (val.getValidatorId().equals(editableValidator.getValidatorId()))
                    v = val;
            if (v == null)
                throw new ConstructionException("Unknown group: " + editableValidator.getId());

            // this is a very lazy way of doing it; if it becomes an issue, we can be smarter and do an actual update...
            deleteValidator(v.getId());
            addValidator(editableValidator);
        }
        finally {
            _lock.writeLock().unlock();
        }
    }

    /**
     * Deletes an existing validator from the engine.
     * <p/>
     * This is a convenient method that only takes the validator ID as a parameter.
     * <p/>
     * Created on Jul 7, 2011 by depryf
     * @param validatorId validator ID to delete
     * @throws ConstructionException if the validator cannot be found
     */
    public void deleteValidator(String validatorId) throws ConstructionException {
        _lock.writeLock().lock();
        try {
            Validator v = getValidator(validatorId);
            if (v == null)
                throw new ConstructionException("Unknown group: " + validatorId);
            deleteValidator(new EditableValidator(v));
        }
        finally {
            _lock.writeLock().unlock();
        }
    }

    /**
     * Deletes an existing validator from the engine.
     * <p/>
     * The following steps should be performed:
     * <ol>
     * <li>Get the validator to delete using the getValidator() method</li>
     * <li>Wrap the validator into an editable validator by passing it to the editableValidator's constructor. </li>
     * <li>Call this method using the editable validator</li>
     * </ol>
     * <p/>
     * Created on Jun 29, 2011 by depryf
     * @param editableValidator <code>EditableValidator</code>, cannot be null
     * @throws ConstructionException if the validator contains an error
     */
    public void deleteValidator(EditableValidator editableValidator) throws ConstructionException {
        _lock.writeLock().lock();
        try {
            // get the validator
            Validator v = getValidator(editableValidator.getId());
            if (v == null)
                throw new ConstructionException("Unknown group: " + editableValidator.getId());

            for (Condition condition : v.getConditions())
                _executableConditions.remove(condition.getConditionId());
            for (Rule r : v.getRules())
                _executableRules.remove(r.getRuleId());
            _contexts.remove(editableValidator.getValidatorId());

            // sort the rules by dependencies (this could though a dependency exception)
            List<ExecutableRule> sortedRules = getRulesSortedByDependencies(_executableRules, _executableConditions);

            // update the internal state of the engine
            populateProcessors(sortedRules);

            // update the raw structure only if the state was successfully updated...
            _validators.remove(editableValidator.getId());
        }
        finally {
            _lock.writeLock().unlock();
        }
    }

    /**
     * Adds a new context entry for the provided validator ID.
     * <p/>
     * Created on Jul 7, 2011 by depryf
     * @param contextEntryId internal ID for the new context, if null a new ID will be generated using the ValidationServices.getNextContextEntrySequence()
     * @param contextKey new context key
     * @param validatorId validator ID
     * @param expression raw expression
     * @param type type ("java", "groovy", "table", etc...)
     * @return the created <code>ContextEntry</code>
     * @throws ConstructionException if the context contains an error
     */
    public ContextEntry addContext(Long contextEntryId, String contextKey, String validatorId, String expression, String type) throws ConstructionException {
        _lock.writeLock().lock();
        try {
            Validator v = getValidator(validatorId);
            if (v == null)
                throw new ConstructionException("Invalid group: " + validatorId);

            // check unicity
            if (_contexts.get(v.getValidatorId()).containsKey(contextKey))
                throw new ConstructionException("Context key '" + contextKey + "' already exists; context keys must be unique within a group");

            Map<String, Object> contexts = _contexts.get(v.getValidatorId());
            if (contexts == null)
                throw new ConstructionException("Invalid group: " + validatorId);

            ValidationServices.getInstance().addContextExpression(expression, contexts, contextKey, type);

            updateProcessorsContexts(_contexts);

            ContextEntry entry = new ContextEntry();
            entry.setContextEntryId(contextEntryId);
            entry.setKey(contextKey);
            entry.setExpression(expression);
            entry.setType(type);
            v.getRawContext().add(entry);

            return entry;
        }
        finally {
            _lock.writeLock().unlock();
        }
    }

    /**
     * Updates an existing context entry for the provided validator ID.
     * <p/>
     * Created on Jul 7, 2011 by depryf
     * @param contextKey new context key
     * @param validatorId validator ID
     * @param expression raw expression
     * @param type type ("java", "groovy", "table", etc...)
     * @throws ConstructionException if the context contains an error or is not found
     */
    public void updateContext(String contextKey, String validatorId, String expression, String type) throws ConstructionException {
        _lock.writeLock().lock();
        try {
            Validator v = getValidator(validatorId);
            if (v == null)
                throw new ConstructionException("Invalid group: " + validatorId);
            ContextEntry entry = v.getRawContext(contextKey);
            if (entry == null)
                throw new ConstructionException("Invalid key: " + contextKey);

            Map<String, Object> contexts = _contexts.get(v.getValidatorId());
            if (contexts == null)
                throw new ConstructionException("Invalid group: " + validatorId);
            if (!contexts.containsKey(contextKey))
                throw new ConstructionException("Group " + validatorId + " does not contain a context for key " + contextKey);

            ValidationServices.getInstance().addContextExpression(expression, contexts, contextKey, type);

            updateProcessorsContexts(_contexts);

            entry.setExpression(expression);
            entry.setType(type);
        }
        finally {
            _lock.writeLock().unlock();
        }
    }

    /**
     * Deletes an existing context entry for the provided validator ID.
     * <p/>
     * Created on Jul 7, 2011 by depryf
     * @param contextKey new context key
     * @param validatorId validator ID
     * @throws ConstructionException if the context is not found
     */
    public void deleteContext(String contextKey, String validatorId) throws ConstructionException {
        _lock.writeLock().lock();
        try {
            Validator v = getValidator(validatorId);
            if (v == null)
                throw new ConstructionException("Invalid group: " + validatorId);
            ContextEntry entry = v.getRawContext(contextKey);
            if (entry == null)
                throw new ConstructionException("Invalid key: " + contextKey);

            Map<String, Object> contexts = _contexts.get(v.getValidatorId());
            if (contexts == null)
                throw new ConstructionException("Invalid group: " + validatorId);
            if (!contexts.containsKey(contextKey))
                throw new ConstructionException("Group " + validatorId + " does not contain a context for key " + contextKey);

            contexts.remove(contextKey);

            updateProcessorsContexts(_contexts);

            v.getRawContext().remove(entry);
        }
        finally {
            _lock.writeLock().unlock();
        }
    }

    /**
     * Utility method that allows to update the ignore flags on many edits very efficiently; the alternative is to call the
     * <code>updateRule</code> method once per edit, but that will be MUCH slower.
     * <p/>
     * If a rule ID is not present in any of the provided collections, then its ignore flag won't be modified.
     * <p/>
     * Created on Oct 6, 2011 by depryf
     * @param idsToIgnore a collection of rule IDs that must be ignored, no rule will be set to ignore if the collection is null (or empty)
     * @param idsToStopIgnoring a collection of rule IDs that must not be ignored anymore, no rule will be set to not-ignore if the collection is null (or empty)
     */
    public void massUpdateIgnoreFlags(Collection<String> idsToIgnore, Collection<String> idsToStopIgnoring) {
        _lock.writeLock().lock();
        try {
            // update the executable rules
            for (ExecutableRule execRule : _executableRules.values()) {
                String id = execRule.getId();

                if (idsToIgnore != null && idsToIgnore.contains(id))
                    execRule.setIgnored(Boolean.TRUE);
                else if (idsToStopIgnoring != null && idsToStopIgnoring.contains(id))
                    execRule.setIgnored(Boolean.FALSE);
            }

            // update the processors after re-evaluating the rules order
            try {
                updateProcessorsRules(getRulesSortedByDependencies(_executableRules, _executableConditions));
            }
            catch (ConstructionException e) {
                throw new IllegalStateException("Internal state has not changed, this exception should not happen!", e);
            }

            // update the raw data
            for (Validator v : _validators.values()) {
                for (Rule r : v.getRules()) {
                    String id = r.getId();

                    if (idsToIgnore != null && idsToIgnore.contains(id))
                        r.setIgnored(Boolean.TRUE);
                    else if (idsToStopIgnoring != null && idsToStopIgnoring.contains(id))
                        r.setIgnored(Boolean.FALSE);
                }
            }
        }
        finally {
            _lock.writeLock().unlock();
        }
    }

    /**
     * Enables the requested embedded set.
     * @param validatorId validator ID
     * @param setId set ID
     * @throws ConstructionException if the set is not found
     */
    @SuppressWarnings("unused")
    public void enableEmbeddedSet(String validatorId, String setId) throws ConstructionException {
        _lock.writeLock().lock();
        try {
            Validator v = getValidator(validatorId);
            if (v == null)
                throw new ConstructionException("Invalid group: " + validatorId);
            EmbeddedSet s = v.getSet(setId);
            if (s == null)
                throw new ConstructionException("Invalid set: " + setId);

            // the sets are not used in the internal state of the engine; so all we have to do is to update the raw data...
            s.setIgnored(false);
        }
        finally {
            _lock.writeLock().unlock();
        }
    }

    /**
     * Disables the requested embedded set.
     * @param validatorId validator ID
     * @param setId set ID
     * @throws ConstructionException if the set is not found
     */
    @SuppressWarnings("unused")
    public void disableEmbeddedSet(String validatorId, String setId) throws ConstructionException {
        _lock.writeLock().lock();
        try {
            Validator v = getValidator(validatorId);
            if (v == null)
                throw new ConstructionException("Invalid group: " + validatorId);
            EmbeddedSet s = v.getSet(setId);
            if (s == null)
                throw new ConstructionException("Invalid set: " + setId);

            // the sets are not used in the internal state of the engine; so all we have to do is to update the raw data...
            s.setIgnored(true);
        }
        finally {
            _lock.writeLock().unlock();
        }
    }

    // ********************************************************************************
    //       OTHER PUBLIC METHODS (some lock required, depends on the method)
    // ********************************************************************************

    public String getEngineVersion() {
        return _ENGINE_VERSION;
    }

    /**
     * Returns the root (first element) of the supported java-path.
     * <p/>
     * Created on Sep 30, 2010 by depryf
     * @return the root (first element) of the supported java-path
     */
    public Set<String> getSupportedJavaPathRoots() {
        return getSupportedJavaPathRoots(false);
    }

    /**
     * Returns the root (first element) of the supported java-path.
     * <p/>
     * Created on Sep 30, 2010 by depryf
     * @param filterEmptyPaths if set to true then a root java path that doesn't have any edit under it will be excluded
     * @return the root (first element) of the supported java-path
     */
    public Set<String> getSupportedJavaPathRoots(boolean filterEmptyPaths) {
        _lock.readLock().lock();
        try {
            if (filterEmptyPaths)
                return _processorRoots.entrySet().stream().filter(e -> e.getValue().get() > 0).map(Entry::getKey).collect(Collectors.toSet());
            else
                return Collections.unmodifiableSet(_processorRoots.keySet());
        }
        finally {
            _lock.readLock().unlock();
        }
    }

    /**
     * Returns the statistics gathered so far...
     * <p/>
     * Created on Nov 30, 2007 by depryf
     * @return a collection of <code>StatsDTO</code> object, possibly empty
     */
    public Map<String, EngineStats> getStats() {
        _statsLock.readLock().lock();
        try {
            return _editsStats;
        }
        finally {
            _statsLock.readLock().unlock();
        }
    }

    /**
     * Resets the statistics gathered so far...
     * <p/>
     * Created on Jun 29, 2009 by depryf
     */
    public void resetStats() {
        _statsLock.writeLock().lock();
        try {
            _editsStats.clear();
        }
        finally {
            _statsLock.writeLock().unlock();
        }
    }

    /**
     * Dynamically enables/disabled computing the edits statistics on this engine.
     */
    public void setEditsStatsEnabled(boolean enabled) {
        _computeEditsStats = enabled;
    }

    /**
     * Returns true if the edits statistics are on (that can be done via the initialization or dynamically via the engine itself).
     */
    public boolean isEditsStatsEnabled() {
        return _computeEditsStats;
    }

    /**
     * Returns a string representation of the engine's internal state.
     * <p/>
     * Note that this string can't be used as a mechanism to persist a state and re-initialize the engine from it, the returned
     * String does not contain enough information for that.
     * <p/>
     * Created on Jan 14, 2008 by depryf
     * @return a string representation of the engine's internal state
     */
    public String dumpInternalState() {
        _lock.readLock().lock();
        try {
            StringBuilder result = new StringBuilder();
            for (String key : new TreeSet<>(_processors.keySet())) // let's display the processors from smallest java path to biggest one...
                _processors.get(key).dumpCache(result, key);

            return result.toString();
        }
        finally {
            _lock.readLock().unlock();
        }
    }

    // ********************************************************************************
    //                  INTERNAL METHODS (no lock required)
    // ********************************************************************************

    private void internalizeValidator(Validator validator, Map<Long, ExecutableCondition> conditions, Map<Long, ExecutableRule> rules, Map<String, Object> contexts, InitializationStats stats) throws ConstructionException {

        if (validator.getValidatorId() == null)
            validator.setValidatorId(ValidationServices.getInstance().getNextValidatorSequence());
        if (validator.getValidatorId() == null)
            throw new ConstructionException("Validator must have a non-null internal ID to be registered in the engine");

        // get pre-compiled rules if we have to
        CompiledRules compiledRules = null;
        if (_options.isPreCompiledEditsEnabled())
            compiledRules = RuntimeUtils.findCompileRules(validator, stats);
        else if (stats != null)
            stats.setReasonNotPreCompiled(validator.getId(), InitializationStats.REASON_DISABLED);

        // internalize the rules
        try (ExecutorService service = Executors.newFixedThreadPool(_options.getNumCompilationThreads())) {
            List<Future<Void>> results = new ArrayList<>(validator.getRules().size());
            if (validator.getRules() != null) {
                for (Rule r : validator.getRules()) {
                    if (r.getRuleId() == null)
                        r.setRuleId(ValidationServices.getInstance().getNextRuleSequence());
                    if (r.getRuleId() == null)
                        throw new ConstructionException("Edits must have a non-null internal ID to be registered in the engine");
                    results.add(service.submit(new RuleCompilingCallable(r, rules, compiledRules, stats)));
                }
                validator.setRules(new HashSet<>(validator.getRules())); // since internal IDs might have changed
            }

            // internalize the conditions
            if (validator.getConditions() != null) {
                for (Condition c : validator.getConditions()) {
                    if (c.getConditionId() == null)
                        c.setConditionId(ValidationServices.getInstance().getNextConditionSequence());
                    if (c.getConditionId() == null)
                        throw new ConstructionException("Conditions must have a non-null internal ID to be registered in the engine");
                    conditions.put(c.getConditionId(), new ExecutableCondition(c));
                }
                validator.setConditions(new HashSet<>(validator.getConditions())); // since internal IDs might have changed
            }

            // we don't internalize the categories because they are not used at runtime, but let's still assign a unique ID to them...
            if (validator.getCategories() != null) {
                for (Category c : validator.getCategories())
                    if (c.getCategoryId() == null)
                        c.setCategoryId(ValidationServices.getInstance().getNextCategorySequence());
                validator.setCategories(new HashSet<>(validator.getCategories())); // since internal IDs might have changed
            }

            // for any context entry that threw an exception, re-try it a second time, hopefully the dependency exception will be resolved...
            if (validator.getRawContext() != null) {
                Set<ContextEntry> reRun = new HashSet<>();
                for (ContextEntry entry : validator.getRawContext()) {
                    if (entry.getContextEntryId() == null)
                        entry.setContextEntryId(ValidationServices.getInstance().getNextContextEntrySequence());
                    try {
                        // this is really not great, a better way would be to fully parse the context expressions, but that will do for now...
                        if (entry.getExpression().contains(VALIDATOR_CONTEXT_KEY + "."))
                            reRun.add(entry);
                        else
                            ValidationServices.getInstance().addContextExpression(entry.getExpression(), contexts, entry.getKey(), entry.getType());
                    }
                    catch (ConstructionException e) {
                        reRun.add(entry);
                    }
                }
                for (ContextEntry entry : reRun)
                    ValidationServices.getInstance().addContextExpression(entry.getExpression(), contexts, entry.getKey(), entry.getType());
                validator.setRawContext(new HashSet<>(validator.getRawContext())); // since internal IDs might have changed
            }

            // we don't internalize the sets because they are not used at runtime, but let's still assign a unique ID to them...
            if (validator.getSets() != null) {
                for (EmbeddedSet s : validator.getSets())
                    if (s.getSetId() == null)
                        s.setSetId(ValidationServices.getInstance().getNextSetSequence());
                validator.setSets(new HashSet<>(validator.getSets())); // since internal IDs might have changed
            }

            // we won't be submitting new work anymore
            service.shutdown();

            // this is important to detect any exception in the background threads
            for (Future<Void> result : results) {
                try {
                    result.get();
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                catch (ExecutionException e) {
                    if (e.getCause() instanceof ConstructionException)
                        throw (ConstructionException)e.getCause();
                    throw new IllegalStateException(e);
                }
            }

            // the work should be done by now because we call get(), which is a blocking call; but better safe than sorry...
            try {
                if (!service.awaitTermination(30, TimeUnit.SECONDS))
                    throw new IllegalStateException("Background compilation took too long to complete");
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void checkValidatorConstraints(List<Validator> validators) throws ConstructionException {
        Set<String> validatorIds = new HashSet<>();
        Set<String> conditionIds = new HashSet<>();
        Set<String> categoryIds = new HashSet<>();
        Set<String> ruleIds = new HashSet<>();
        for (Validator v : validators) {
            if (validatorIds.contains(v.getId()))
                throw new ConstructionException("Group ID '" + v.getId() + "' is not unique");
            if (v.getMinEngineVersion() != null && ValidationServices.getInstance().compareEngineVersions(v.getMinEngineVersion(), _ENGINE_VERSION) > 0)
                throw new ConstructionException("Group ID '" + v.getId() + "' requires version " + v.getMinEngineVersion() + "; current version is " + _ENGINE_VERSION);
            validatorIds.add(v.getId());

            if (v.getConditions() != null) {
                for (Condition c : v.getConditions()) {
                    if (conditionIds.contains(c.getId()))
                        throw new ConstructionException("Condition ID '" + c.getId() + "' (from group '" + v.getId() + "') is not unique across all groups");
                    conditionIds.add(c.getId());
                }
            }

            if (v.getCategories() != null) {
                for (Category c : v.getCategories()) {
                    if (categoryIds.contains(c.getId()))
                        throw new ConstructionException("Category ID '" + c.getId() + "' (from group '" + v.getId() + "') is not unique across all groups");
                    categoryIds.add(c.getId());
                }
            }

            for (Rule r : v.getRules()) {
                if (ruleIds.contains(r.getId()))
                    throw new ConstructionException("Edit ID '" + r.getId() + "' (from group '" + v.getId() + "') is not unique across all groups");
                ruleIds.add(r.getId());
            }
        }
    }

    private Collection<RuleFailure> internalValidate(Validatable validatable, ValidatingContext vContext) throws ValidationException {

        // pre-condition: engine must be initialized
        if (_status == ValidationEngineStatus.NOT_INITIALIZED)
            return new HashSet<>();

        // pre-condition: there must be a root processor for this validatable
        Processor processor = _processors.get(validatable.getRootLevel());
        if (processor == null)
            return new HashSet<>();

        // pre-condition: if a forced rule is provided, it must have a known java path
        if (vContext.getToForce() != null && !ValidationServices.getInstance().getAllJavaPaths().containsKey(vContext.getToForce().getJavaPath()))
            throw new ValidationException("Unknown java path for forced edit: " + vContext.getToForce().getJavaPath());

        // process the validatable
        Collection<RuleFailure> failures = processor.process(validatable, vContext);

        // report the stats if we have to
        if (_computeEditsStats) {
            _statsLock.writeLock().lock();
            try {
                for (Entry<String, Long> entry : vContext.getEditDurations().entrySet())
                    _editsStats.computeIfAbsent(entry.getKey(), EngineStats::new).reportStat(entry.getValue());
            }
            finally {
                _statsLock.writeLock().unlock();
            }
        }

        return failures;
    }

    private void populateProcessors(List<ExecutableRule> sortedRules) {

        _processors.clear();
        _processorRoots.clear();

        // go through each java path and create/get the corresponding processors        
        for (String javaPath : ValidationServices.getInstance().getAllJavaPaths().keySet()) {
            String[] parts = StringUtils.split(javaPath, '.');

            // keep track of the roots (I couldn't find a concurrent implementation of a set, so I am using a map with dummy objects)
            _processorRoots.put(parts[0], new AtomicInteger());

            // keep track of the current partial path
            StringBuilder partialPath = new StringBuilder(parts[0]);

            // first part correspond to a validating processor, the rest of the parts correspond to iterative processors...
            ValidatingProcessor current = _processors.computeIfAbsent(partialPath.toString(), k -> new ValidatingProcessor(partialPath.toString()));
            for (int i = 1; i < parts.length; i++) {
                partialPath.append(".").append(parts[i]);

                ValidatingProcessor vProcessor = _processors.get(partialPath.toString());
                if (vProcessor == null) {
                    vProcessor = new ValidatingProcessor(partialPath.toString());
                    IterativeProcessor iProcessor = new IterativeProcessor(vProcessor, parts[i]);
                    _processors.put(partialPath.toString(), vProcessor);
                    current.addNested(iProcessor);
                }

                current = vProcessor;
            }
        }

        // update the processors
        if (sortedRules != null)
            updateProcessorsRules(sortedRules);
        updateProcessorsConditions(_executableConditions.values());
        updateProcessorsContexts(_contexts);
    }

    private void updateProcessorsRules(List<ExecutableRule> sortedRules) {

        // get the sorted rules by java-path
        Map<String, List<ExecutableRule>> rules = new HashMap<>();
        for (ExecutableRule rule : sortedRules)
            rules.computeIfAbsent(rule.getJavaPath(), k -> new ArrayList<>()).add(rule);

        // since we are about to reset all the rules in every processor, let's reset the rule counts as well
        _processorRoots.values().forEach(i -> i.set(0));

        // update all the processors
        for (ValidatingProcessor p : _processors.values()) {
            List<ExecutableRule> rulesForCurrentProcessor = rules.getOrDefault(p.getJavaPath(), Collections.emptyList());
            p.setRules(rulesForCurrentProcessor);
            _processorRoots.get(StringUtils.split(p.getJavaPath(), '.')[0]).addAndGet(rulesForCurrentProcessor.size());
        }
    }

    private void updateProcessorsConditions(Collection<ExecutableCondition> allConditions) {

        // get the conditions by java-path (there is no order needed for conditions)
        Map<String, List<ExecutableCondition>> conditions = new HashMap<>();
        for (ExecutableCondition condition : allConditions)
            conditions.computeIfAbsent(condition.getJavaPath(), k -> new ArrayList<>()).add(condition);

        // update all the processors
        for (ValidatingProcessor p : _processors.values())
            p.setConditions(conditions.getOrDefault(p.getJavaPath(), Collections.emptyList()));
    }

    private void updateProcessorsContexts(Map<Long, Map<String, Object>> allContexts) {

        // this code used to be smart about which validator was used at which java-path, and provide only the contexts for that particular
        // java-path to the processor; but that doesn't work in SEER*DMS where some edits are persisted but not registered to the engine!
        for (ValidatingProcessor p : _processors.values())
            p.setContexts(allContexts);
    }

    private List<ExecutableRule> getRulesSortedByDependencies(Map<Long, ExecutableRule> rules, Map<Long, ExecutableCondition> conditions) throws ConstructionException {
        List<ExecutableRule> rulesQueue = new ArrayList<>();

        // cache all of our rules
        Map<String, ExecutableRule> ruleCache = new HashMap<>(rules.size() + 1); // rule-id -> rule object, modified as the process goes on
        Map<String, String> pathCache = new HashMap<>(rules.size() + 1); // rule-id -> rule-set-java-path, unmodified
        Map<String, String> validatorCache = new HashMap<>(rules.size() + 1); // rule-id -> validator-id, unmodified

        // build a map of condition ID -> java path
        Map<String, String> conditionPaths = new HashMap<>();
        for (ExecutableCondition condition : conditions.values())
            conditionPaths.put(condition.getId(), condition.getJavaPath());

        // gather and validate all the rules
        for (ExecutableRule rule : rules.values()) {
            addToRuleCache(pathCache, validatorCache, rule, ruleCache);

            // validate the referenced condition(s): rule must be at the same level, or lower
            if (rule.getConditions() != null) {
                for (String conditionId : rule.getConditions()) {
                    String conditionPath = conditionPaths.get(conditionId);
                    if (conditionPath == null)
                        throw new ConstructionException("Edit '" + rule.getId() + "' references unknown condition: " + conditionId, rule.getId());
                    if (conditionPath.startsWith(rule.getJavaPath()) && !conditionPath.equals(rule.getJavaPath()))
                        throw new ConstructionException("Edit '" + rule.getId() + "' references condition '" + conditionId + "' which is defined lower in the data structure tree.", rule.getId());
                }
            }
        }

        // cache of IDs, used to identify circular dependencies
        Set<String> currents = new HashSet<>();

        // populate the queue
        while (!ruleCache.isEmpty())
            addToRuleQueue(ruleCache.remove(ruleCache.keySet().iterator().next()), ruleCache, currents, rulesQueue, pathCache, validatorCache);

        return rulesQueue;
    }

    private void addToRuleCache(Map<String, String> pathCache, Map<String, String> validatorCache, ExecutableRule rule, Map<String, ExecutableRule> ruleCache) throws ConstructionException {

        String ruleId = rule.getId();

        pathCache.put(ruleId, rule.getJavaPath());
        validatorCache.put(ruleId, rule.getRule().getValidator().getId());

        // check self dependency
        if (rule.getDependencies().contains(ruleId))
            throw new ConstructionException("Edit '" + ruleId + "' cannot depend on itself", ruleId);

        ruleCache.put(ruleId, rule);
    }

    private void addToRuleQueue(ExecutableRule rule, Map<String, ExecutableRule> cache, Set<String> currents, List<ExecutableRule> queue, Map<String, String> pathCache, Map<String, String> validatorCache) throws ConstructionException {

        if (rule.getDependencies() != null && !rule.getDependencies().isEmpty()) {
            String rId = rule.getId();
            String rPath = rule.getJavaPath();
            String vId = rule.getRule().getValidator().getId();
            for (String depId : rule.getDependencies()) {
                String validatorCachedId = validatorCache.get(depId);
                if (validatorCachedId == null)
                    throw new ConstructionException("Unable to resolve dependency '" + depId + "' for edit '" + rId + "' (" + vId + ")");

                // check that dependencies go bottom-up in the patient set structure
                String depPath = pathCache.get(depId);
                if (rPath == null)
                    throw new ConstructionException("Got a null java-path for edit '" + rId + "' (" + vId + ")");
                if (depPath == null)
                    throw new ConstructionException("Got a null java-path for edit '" + depId + "' (on which '" + rId + "' depends)");
                if (!rPath.startsWith(depPath) || rPath.length() < depPath.length())
                    throw new ConstructionException("Edit '" + rId + "' cannot depend on '" + depId + "' which is lower in the data structure tree.");

                // check that dependencies do not cross validators                    
                if (!vId.equals(validatorCachedId))
                    throw new ConstructionException("No cross-group dependency is allowed, edit '" + rId + "' (" + vId + ") cannot depend on '" + depId + "' (" + validatorCachedId + ")", rId);

                // check for circular dependencies
                if (currents.contains(depId))
                    throw new ConstructionException("Circular dependency detected between '" + depId + "' and '" + rId + "'", depId, rId);

                if (cache.containsKey(depId)) {
                    currents.add(rule.getId());
                    addToRuleQueue(cache.remove(depId), cache, currents, queue, pathCache, validatorCache);
                }
            }
        }

        queue.add(rule);
        currents.clear();
    }

}