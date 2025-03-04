/*
 * Copyright (C) 2007 Information Management Services, Inc.
 */
package com.imsweb.validation.entities;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import org.codehaus.groovy.control.CompilationFailedException;

import com.imsweb.validation.ConstructionException;
import com.imsweb.validation.ValidationServices;
import com.imsweb.validation.runtime.ParsedContexts;
import com.imsweb.validation.runtime.ParsedLookups;
import com.imsweb.validation.runtime.ParsedProperties;
import com.imsweb.validation.runtime.RuntimeUtils;

/**
 * A <code>Rule</code> is the smallest entity in the validation engine. It defines
 * an expression that is evaluated and reported if it fails. Rules are wrapped into
 * {@link Validator}; they can be tied to a {@link Condition}.
 * <p/>
 * Created on Nov 9, 2007 by depryf
 */
@SuppressWarnings("unused")
public class Rule {

    private static final Pattern _MESSAGE_PATTERN = Pattern.compile("\r?\n");

    /**
     * DB ID for this rule
     */
    protected Long _ruleId;

    /**
     * Application ID (like a display ID) for this rule
     */
    protected String _id;

    /**
     * Name for this rule
     */
    protected String _name;

    /**
     * tag for this rule (tags are used in the Genedits framework)
     */
    protected String _tag;

    /**
     * Java path for this rule
     */
    protected String _javaPath;

    /**
     * Severity for this rule
     */
    protected Integer _severity;

    /**
     * Failure message for this rule
     */
    protected String _message;

    /**
     * Expression to evaluate for this rule
     */
    protected String _expression;

    /**
     * Description for this rule
     */
    protected String _description;

    /**
     * History for this rule
     */
    protected Set<RuleHistory> _histories;

    /**
     * Parent <code>Validator</code>
     */
    protected Validator _validator;

    /**
     * Category ID.
     */
    protected String _category;

    /**
     * Condition IDs.
     */
    protected Set<String> _conditions;

    /**
     * Operator (AND or OR) to use for the set of conditions.
     */
    protected Boolean _useAndForConditions;

    /**
     * Rule IDs on which this rule depends
     */
    protected Set<String> _dependencies;

    /**
     * Rule IDs that depends on this rule
     */
    protected Set<String> _invertedDependencies;

    /**
     * Set of properties contained in this rule; as their appear in the textual expression of the rule
     */
    protected Set<String> _usedProperties;

    /**
     * Set of lookup IDs used in this rule; as their appear in the textual expression of the rule
     */
    protected Set<String> _usedLookupIds;

    /**
     * Set of used context keys.
     */
    protected Set<String> _usedContextKeys;

    /**
     * If set to true, the rule won't be executed when validating a validatable, unless the rule is explicitly forced (default to false, should never be null)
     */
    protected Boolean _ignored;

    /**
     * Agency (used for translated edits only)
     */
    protected String _agency;

    /**
     * Whether the failures from this rule can be overridden or not; this field is not used in this library and was added for other projects using the library.
     */
    protected Byte _allowOverride;

    /**
     * Whether this rule needs to be reviewed or not; this field is not used in this library and was added for other projects using the library.
     */
    protected Boolean _needsReview;

    /**
     * Import edit flag; this field is not used in this library and was added for other projects using the library.
     */
    protected Byte _importEditFlag;

    /**
     * Data entry applicable record types; this field is not used in this library and was added for other projects using the library.
     */
    protected List<Long> _dataEntryTypes;

    /**
     * A (logical) data level for this edit (related to the Java Path, but not exactly the same thing); this field is not used in this library and was added for other projects using the library.
     */
    protected Byte _dataLevel;

    /**
     * Constructor.
     * <p/>
     * Created on Nov 9, 2007 by depryf
     */
    public Rule() {
        _conditions = new HashSet<>();
        _histories = new HashSet<>();
        _dependencies = new HashSet<>();
        _invertedDependencies = new HashSet<>();
        _usedProperties = new HashSet<>();
        _usedLookupIds = new HashSet<>();
        _usedContextKeys = new HashSet<>();
        _ignored = Boolean.FALSE;
        _useAndForConditions = Boolean.TRUE;
        _needsReview = Boolean.FALSE;
    }

    /**
     * Getter for the rule persistence ID. Must be unique within the validation engine.
     * <p/>
     * In a system persisting the rules in a database, that would represent a database primary key.
     * In a system using only memory objects loaded from XML, it should be assigned using the
     * getNextRuleSequence() method in <code>ValidationServices</code>.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return the rule persistence ID
     */
    public Long getRuleId() {
        return _ruleId;
    }

    /**
     * Setter for the rule persistence ID. Must be unique within the validation engine.
     * <p/>
     * In a system persisting the rules in a database, that would represent a database primary key.
     * In a system using only memory objects loaded from XML, it should be assigned using the
     * getNextRuleSequence() method in <code>ValidationServices</code>.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param ruleId rule persistence ID
     */
    public void setRuleId(Long ruleId) {
        _ruleId = ruleId;
    }

    /**
     * Getter for the ID. The rule ID must be unique within the validation engine.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return the rule ID
     */
    public String getId() {
        return _id;
    }

    /**
     * Setter for the ID. The rule ID must be unique within the validation engine.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param id the rule ID
     */
    public void setId(String id) {
        _id = id;
    }

    /**
     * Getter for the name.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return the rule name
     */
    public String getName() {
        return _name;
    }

    /**
     * Setter for the name.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param name the rule name
     */
    public void setName(String name) {
        _name = name;
    }

    /**
     * Getter for the tag.
     * <p/>
     * Created on May 19, 2017 by depryf
     * @return the rule tag
     */
    public String getTag() {
        return _tag;
    }

    /**
     * Setter for the tag.
     * <p/>
     * Created on May 19, 2017 by depryf
     * @param tag the rule tag
     */
    public void setTag(String tag) {
        _tag = tag;
    }

    /**
     * Getter for the java path.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return the rule java path
     */
    public String getJavaPath() {
        return _javaPath;
    }

    /**
     * Setter for the java path.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param path the java path, cannot be null or blank
     */
    public void setJavaPath(String path) {
        _javaPath = path;
    }

    /**
     * Getter for the severity (the meaning of the severity is application-dependent; the severity is not used by the validation engine).
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return the rule severity
     */
    public Integer getSeverity() {
        return _severity;
    }

    /**
     * Setter for the severity (the meaning of the severity is application-dependent; the severity is not used by the validation engine).
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param severity the rule severity
     */
    public void setSeverity(Integer severity) {
        _severity = severity;
    }

    /**
     * Getter for the failure message.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return the rule failure message
     */
    public String getMessage() {
        return _message;
    }

    /**
     * Setter for the failure message.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param message the rule failure message
     */
    public void setMessage(String message) {
        if (message != null)
            message = _MESSAGE_PATTERN.matcher(message).replaceAll(" ").trim();
        _message = message;
    }

    /**
     * Getter for the expression (Groovy script).
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return the rule expression
     */
    public String getExpression() {
        return _expression;
    }

    /**
     * Setter for the expression (Groovy script).
     * <p/>
     * This method will compile the script and gather the used properties, context entries and lookup IDs.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param expression the rule expression
     * @throws ConstructionException if the expression is not valid Groovy
     */
    public void setExpression(String expression) throws ConstructionException {
        if (!Objects.equals(expression, _expression)) {
            _expression = expression;
            synchronized (this) {
                try {
                    _usedProperties.clear();
                    _usedContextKeys.clear();
                    _usedLookupIds.clear();
                    ValidationServices.getInstance().parseExpression("rule", _expression, _usedProperties, _usedContextKeys, _usedLookupIds);
                }
                catch (CompilationFailedException e) {
                    throw new ConstructionException("Unable to parse rule " + getId(), e);
                }
            }
        }
    }

    /**
     * Setter for the expression taking pre-parsed objects for optimization.
     */
    public void setExpression(String expression, ParsedProperties parsedProperties, ParsedContexts parsedContexts, ParsedLookups parsedLookups) throws ConstructionException {
        // we are going to use the pre-parsed stuff only if they are all available, otherwise we don't use any of them...
        Set<String> properties = RuntimeUtils.getParsedProperties(parsedProperties, _id);
        Set<String> contexts = RuntimeUtils.getParsedContexts(parsedContexts, _id);
        Set<String> lookups = RuntimeUtils.getParsedLookups(parsedLookups, _id);
        if (properties != null && contexts != null && lookups != null) {
            _expression = expression;
            _usedProperties = properties;
            _usedContextKeys = contexts;
            _usedLookupIds = lookups;
        }
        else
            setExpression(expression);
    }

    /**
     * Getter for the description.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return the rule description
     */
    public String getDescription() {
        return _description;
    }

    /**
     * Setter for the description.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param description the rule description
     */
    public void setDescription(String description) {
        _description = description;
    }

    /**
     * Getter for the <code>RuleHistory</code>s.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return the set of <code>RuleHistory</code>, maybe empty but never null
     */
    public Set<RuleHistory> getHistories() {
        return _histories;
    }

    /**
     * Setter for the <code>RuleHistory</code>s.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param histories the set of <code>RuleHistory</code>
     */
    public void setHistories(Set<RuleHistory> histories) {
        _histories = histories == null ? new HashSet<>() : histories;
    }

    /**
     * Getter for the category.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return the category, maybe null
     */
    public String getCategory() {
        return _category;
    }

    /**
     * Setter for the category.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param category category
     */
    public void setCategory(String category) {
        _category = category;
    }

    /**
     * Getter for the conditions.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return the conditions, maybe null
     */
    public Set<String> getConditions() {
        return _conditions;
    }

    /**
     * Setter for the conditions.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param conditions conditions
     */
    public void setConditions(Set<String> conditions) {
        _conditions = conditions;
    }

    /**
     * Getter for the properties used in the expression.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return the set of properties, maybe empty but never null
     */
    public Set<String> getUsedProperties() {
        return _usedProperties;
    }

    /**
     * Setter for the properties used in the expression.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param usedProperties used properties
     */
    public void setUsedProperties(Set<String> usedProperties) {
        _usedProperties = usedProperties;
    }

    /**
     * Getter for the lookup IDs used in the expression.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return the set of used lookup IDs, maybe empty but never null
     */
    public Set<String> getUsedLookupIds() {
        return _usedLookupIds;
    }

    /**
     * Setter for the the lookup IDs used in the expression.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param usedLookupIds used lookup IDs
     */
    public void setUsedLookupIds(Set<String> usedLookupIds) {
        _usedLookupIds = usedLookupIds;
    }

    /**
     * Getter for the dependencies (the rule IDs on which this rule depends).
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return the set of dependencies, maybe empty but never null
     */
    public Set<String> getDependencies() {
        return _dependencies;
    }

    /**
     * Setter for the dependencies (the rule IDs on which this rule depends).
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param dependencies the set of dependencie
     */
    public void setDependencies(Set<String> dependencies) {
        _dependencies = dependencies == null ? new HashSet<>() : dependencies;
    }

    /**
     * Getter for the inverted dependencies (the rule IDs that depend on this rule).
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return the set of inverted dependencies, maybe empty but never null
     */
    public Set<String> getInvertedDependencies() {
        return _invertedDependencies;
    }

    /**
     * Setter for the inverted dependencies (the rule IDs that depend on this rule).
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param invertedDependencies the set of inverted dependencies
     */
    public void setInvertedDependencies(Set<String> invertedDependencies) {
        _invertedDependencies = invertedDependencies == null ? new HashSet<>() : invertedDependencies;
    }

    /**
     * Setter for the parent <code>Validator</code>.
     * @param validator validator
     */
    public void setValidator(Validator validator) {
        _validator = validator;
    }

    /**
     * Getter for the parent <code>Validator</code>.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return a <code>Validator</code>
     */
    public Validator getValidator() {
        return _validator;
    }

    /**
     * Getter for the ignored flag.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return true if the rule is ignored
     */
    public Boolean getIgnored() {
        return _ignored;
    }

    /**
     * Setter for the ignored flag.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param ignored the ignored flag to set
     */
    public void setIgnored(Boolean ignored) {
        _ignored = ignored == null ? Boolean.FALSE : ignored;
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
     * Getter for the context entries referenced in the expression.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return the set of potential context entries, maybe empty but never null
     */
    public Set<String> getUsedContextKeys() {
        return _usedContextKeys;
    }

    /**
     * Setter for the context entries referenced in the expression.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param usedContextKeys used lookup IDs
     */
    public void setUsedContextKeys(Set<String> usedContextKeys) {
        _usedContextKeys = usedContextKeys;
    }

    /**
     * Getter
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return the agency
     */
    public String getAgency() {
        return _agency;
    }

    /**
     * Setter
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param agency the agency
     */
    public void setAgency(String agency) {
        _agency = agency;
    }

    /**
     * Getter
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return whether override is allowed
     */
    public Byte getAllowOverride() {
        return _allowOverride;
    }

    /**
     * Setter
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param allowOverride whether override is allowed
     */
    public void setAllowOverride(Byte allowOverride) {
        _allowOverride = allowOverride;
    }

    /**
     * Getter
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return whether this rule needs to be reviewed
     */
    public Boolean getNeedsReview() {
        return _needsReview;
    }

    /**
     * Setter
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param needsReview whether this rule needs to be reviewed
     */
    public void setNeedsReview(Boolean needsReview) {
        _needsReview = needsReview;
    }

    /**
     * Getter
     * <p/>
     * Created on Jun 28, 2021 by depryf
     * @return whether this rule is an "import" edit
     */
    public Byte getImportEditFlag() {
        return _importEditFlag;
    }

    /**
     * Setter
     * <p/>
     * Created on Jun 28, 2021 by depryf
     * @param importEditFlag whether this rule is an "import" edit
     */
    public void setImportEditFlag(Byte importEditFlag) {
        _importEditFlag = importEditFlag;
    }


    /**
     * Getter
     * <p/>
     * Created on Feb 26, 2025 by depryf
     * @return list of applicable record types in data entry
     */
    public List<Long> getDataEntryTypes() {
        return _dataEntryTypes;
    }

    /**
     * Setter
     * <p/>
     * Created on Feb 26, 2025 by depryf
     * @param dataEntryTypes list of applicable record types in data entry
     */
    public void setDataEntryTypes(List<Long> dataEntryTypes) {
        _dataEntryTypes = dataEntryTypes;
    }

    /**
     * Getter
     * <p/>
     * Created on Jan 30, 2023 by depryf
     * @return the "logical" data level for this rule
     */
    public Byte getDataLevel() {
        return _dataLevel;
    }

    /**
     * Setter
     * <p/>
     * Created on Jan 30, 2023 by depryf
     * @param dataLevel the "logical" data level for this rule
     */
    public void setDataLevel(Byte dataLevel) {
        _dataLevel = dataLevel;
    }

    @Override
    public String toString() {
        return getId() + " {" + getMessage() + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rule)) return false;
        Rule rule = (Rule)o;
        if (_ruleId != null && rule._ruleId != null)
            return Objects.equals(_ruleId, rule._ruleId);
        return Objects.equals(_id, rule._id);
    }

    @Override
    public int hashCode() {
        if (_ruleId != null)
            return Objects.hash(_ruleId);
        return Objects.hash(_id);
    }
}
