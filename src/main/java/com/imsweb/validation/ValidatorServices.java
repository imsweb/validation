/*
 * Copyright (C) 2008 Information Management Services, Inc.
 */
package com.imsweb.validation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.SourceUnit;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import com.imsweb.validation.entities.SimpleMapValidatable;
import com.imsweb.validation.entities.SimpleNaaccrLinesValidatable;
import com.imsweb.validation.entities.Validatable;
import com.imsweb.validation.internal.EditCodeVisitorSupport;
import com.imsweb.validation.internal.context.JavaContextParser;
import com.imsweb.validation.shared.ValidatorLookup;

/**
 * This class provides basic utility services to the validation engine.
 * <p/>
 * These services need to be initialized before using the engine or the factory
 * <br/><br/>
 * To initialize the framework with the default services, call the following:<br/><br/>
 * <i>ValidatorServices.initialize(new ValidatorServices())</i>
 * As of version 1.5, the services are lazily initialized with the default implementation; so if you don't need a special behavior,
 * there is no need to initialize this class anymore.
 * <br/><br/>
 * To implement your own services, create a class that extends this one, override the methods, and call the following:<br/><br/>
 * <i>ValidatorServices.initialize(new MyValidatorServices())</i>
 * <p/>
 * Created on Feb 8, 2008 by depryf
 */
public class ValidatorServices {

    /**
     * Internal sequence to use for rule IDs
     */
    private static AtomicInteger _RULE_SEQ = new AtomicInteger(0);

    /**
     * Internal sequence to use for category IDs
     */
    private static AtomicInteger _CATEGORY_SEQ = new AtomicInteger(0);

    /**
     * Internal sequence to use for condition IDs
     */
    private static AtomicInteger _CONDITION_SEQ = new AtomicInteger(0);

    /**
     * Internal sequence to use for validator IDs
     */
    private static AtomicInteger _VALIDATOR_SEQ = new AtomicInteger(0);

    /**
     * Internal sequence to use for context entry IDs
     */
    private static AtomicInteger _CONTEXT_ENTRY_SEQ = new AtomicInteger(0);

    /**
     * Internal sequence to use for set IDs
     */
    private static AtomicInteger _SET_SEQ = new AtomicInteger(0);

    /**
     * Private instance of a <code>ValidatorServices</code>
     */
    private static ValidatorServices _INSTANCE;

    /**
     * Pattern for property values replacement
     */
    private static Pattern _PROP_REPLACEMENT_PATTERN = Pattern.compile("(\\$\\{(.+?)\\})");

    /**
     * Map of java-path -> alias to use in the edits
     */
    private static final Map<String, String> _ALIASES = new HashMap<>();

    static {
        // this alias is used to run edits using the SimpleMapValidatable wrapper
        _ALIASES.put(SimpleMapValidatable.ROOT_PREFIX, "record");

        // these aliases are used to run edits against NAACCR lines
        _ALIASES.put(SimpleNaaccrLinesValidatable.ROOT_PREFIX, "lines");
        _ALIASES.put(SimpleNaaccrLinesValidatable.ROOT_PREFIX + ".line", "line");

        // these aliases are used to run edits against untrimmed NAACCR lines
        _ALIASES.put(SimpleNaaccrLinesValidatable.ROOT_PREFIX_UNTRIMMED, "untrimmedlines");
        _ALIASES.put(SimpleNaaccrLinesValidatable.ROOT_PREFIX_UNTRIMMED + ".untrimmedline", "untrimmedline");
    }

    /**
     * Cached pattern for the versions
     */
    private static final Pattern _VERSIONS_PATTERN = Pattern.compile("\\d(\\.\\d)*");

    /**
     * Initializes this class with the passed instance.
     * <br/><br/>
     * If it was already initialized with another instance, the previous one will be overridden with the new one. Use the
     * <i>isInitialized()</i> methods if you don't want this behavior.
     * <p/>
     * Created on Feb 11, 2008 by depryf
     * @param instance a <code>ValidatorServices</code>
     */
    public static void initialize(ValidatorServices instance) {
        _INSTANCE = instance;
    }

    /**
     * Returns true if this class has already been initialized, false otherwise.
     * <p/>
     * Created on Mar 6, 2008 by depryf
     * @return true if this class has already been initialized, false otherwise
     */
    public static boolean isInitialized() {
        return _INSTANCE != null;
    }

    /**
     * Gets current instance of the <code>ValidatorServices</code>
     * <p/>
     * Created on Feb 11, 2008 by depryf
     * @return a <code>ValidatorServices</code>
     */
    public static synchronized ValidatorServices getInstance() {
        if (_INSTANCE == null)
            _INSTANCE = new ValidatorServices();

        return _INSTANCE;
    }

    /**
     * Returns the alias corresponding to the passed java-path, null if none is found.
     * <p/>
     * Created on Mar 25, 2008 by depryf
     * @param javaPath full java-path (like it is defined on the rules)
     * @return corresponding alias, maybe null
     */
    public String getAliasForJavaPath(String javaPath) {
        return _ALIASES.get(javaPath);
    }

    /**
     * Returns the java-path corresponding to the passed alias. Returns null if no java path is found.
     * <p/>
     * Created on Mar 25, 2008 by depryf
     * @param alias requested alias
     * @return the java-path corresponding to the passed alias
     */
    public String getJavaPathForAlias(String alias) {
        for (Map.Entry<String, String> entry : _ALIASES.entrySet())
            if (entry.getValue().equals(alias))
                return entry.getKey();
        return null;
    }

    /**
     * Returns all the java paths and their corresponding aliases.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @return a non-modifiable map of java paths -> alias
     */
    public Map<String, String> getAllJavaPaths() {
        return Collections.unmodifiableMap(_ALIASES);
    }

    /**
     * Returns the <code>ValidatorLookup</code> corresponding to the passed ID.
     * <p/>
     * Created on Feb 8, 2008 by depryf
     * @param id ID of the lookup to fetch
     * @return corresponding <code>Lookup</code> or null if we didn't find it
     */
    @SuppressWarnings("UnusedParameters")
    public ValidatorLookup getLookupById(String id) {
        return null;
    }

    /**
     * Returns the configuration variable value corresponding to the passed ID.
     * <p/>
     * Created on Feb 8, 2008 by depryf
     * @param id ID of the configuration variable to fetch
     * @return corresponding value, possibly null
     */
    @SuppressWarnings("UnusedParameters")
    public Object getConfVariable(String id) {
        return null;
    }

    /**
     * Returns the next rule ID to use.
     * <p/>
     * Created on Feb 25, 2008 by depryf
     * @return next sequence to use
     */
    public Long getNextRuleSequence() {
        return (long)_RULE_SEQ.incrementAndGet();
    }

    /**
     * Returns the next category ID to use.
     * <p/>
     * Created on Feb 25, 2008 by depryf
     * @return next sequence to use
     */
    public Long getNextCategorySequence() {
        return (long)_CATEGORY_SEQ.incrementAndGet();
    }

    /**
     * Returns the next condition ID to use.
     * <p/>
     * Created on Feb 25, 2008 by depryf
     * @return next sequence to use
     */
    public Long getNextConditionSequence() {
        return (long)_CONDITION_SEQ.incrementAndGet();
    }

    /**
     * Returns the next validator ID to use.
     * <p/>
     * Created on Feb 25, 2008 by depryf
     * @return next sequence to use
     */
    public Long getNextValidatorSequence() {
        return (long)_VALIDATOR_SEQ.incrementAndGet();
    }

    /**
     * Returns the next context entry ID to use.
     * <p/>
     * Created on Feb 25, 2008 by depryf
     * @return next sequence to use
     */
    public Long getNextContextEntrySequence() {
        return (long)_CONTEXT_ENTRY_SEQ.incrementAndGet();
    }

    /**
     * Returns the next set ID to use.
     * <p/>
     * Created on Feb 25, 2008 by depryf
     * @return next sequence to use
     */
    public Long getNextSetSequence() {
        return (long)_SET_SEQ.incrementAndGet();
    }

    /**
     * Adds the result of the groovy execution of the passed expression, using the passed context. Puts it back into the context
     * under the passed ID
     * <p/>
     * Created on Nov 16, 2007 by depryf
     * @param expression expression to parse
     * @param context context
     * @param entryId context entry ID
     * @param type the type of the entry
     * @return the "compiled" context entry
     * @throws ConstructionException
     */
    public Object addContextExpression(String expression, Map<String, Object> context, String entryId, String type) throws ConstructionException {
        Object result;

        if ("groovy".equals(type))
            result = addGroovyContextExpression(expression, context, entryId);
        else if ("java".equals(type))
            result = addJavaContextExpression(expression, context, entryId);
        else
            throw new ConstructionException("Unsupported context type: " + type);

        return result;
    }

    /**
     * Adds the passed groovy context and add it to the provided current context.
     * <p/>
     * Created on Aug 24, 2010 by depryf
     * @param expression expression to parse
     * @param context context
     * @param entryId context entry ID
     * @return the "compiled" context entry
     * @throws ConstructionException
     */
    Object addGroovyContextExpression(String expression, Map<String, Object> context, String entryId) throws ConstructionException {
        Object result;

        try {
            Script script = new GroovyShell().parse(expression);

            Binding binding = new Binding();
            binding.setVariable(ValidationEngine.VALIDATOR_FUNCTIONS_KEY, ValidatorContextFunctions.getInstance());
            binding.setVariable(ValidationEngine.VALIDATOR_CONTEXT_KEY, context); // new way of referencing contexts (using a prefix)
            for (Entry<String, Object> entry : context.entrySet()) // old way of using the contexts (without a prefix); for now we still support it...
                binding.setVariable(entry.getKey(), entry.getValue());
            script.setBinding(binding);

            result = script.run();
            if (result == null)
                throw new ConstructionException("Context expression '" + entryId + "' evaluated to 'null'");

            // make sure that closures use 'def' keyword for any new variable (#66487)
            if (result instanceof Closure) {
                try {
                    parseExpression(entryId, expression, null, null, null, true);
                }
                catch (Exception e) {
                    throw new ConstructionException("Error in context '" + entryId + "': " + e.getMessage());
                }
            }
            context.put(entryId, result);
        }
        catch (Exception e) {
            throw new ConstructionException("Unable to evaluate context for key '" + entryId + "'", e);
        }

        return result;
    }

    /**
     * Adds the passed java context and add it to the provided current context.
     * <p/>
     * Created on Aug 24, 2010 by depryf
     * @param expression expression to parse
     * @param context context
     * @param entryId context entry ID
     * @return the "compiled" context entry
     * @throws ConstructionException
     */
    Object addJavaContextExpression(String expression, Map<String, Object> context, String entryId) throws ConstructionException {
        Object result;

        try {
            result = JavaContextParser.parseContext(expression, context);
            context.put(entryId, result);
        }
        catch (Exception e) {
            throw new ConstructionException("Unable to evaluate context for key '" + entryId + "'", e);
        }

        return result;
    }

    /**
     * Parses the given groovy expression and creates the corresponding <code>Script</code> that
     * will be used when executing the rule. The passed set of properties and context entries will
     * be filled in during this process.
     * <p/>
     * Created on Jan 17, 2008 by depryf
     * @param id identifier of the script being parsed
     * @param expression expression to parse.
     * @param properties properties used in the expression (if null, they will not be gathered)
     * @param contextEntries context entries used in the expression (if null, they will not be gathered)
     * @param lookups lookup IDs used in the expression (if null, they will not be gathered)
     * @throws CompilationFailedException
     */
    public void parseExpression(String id, String expression, Set<String> properties, Set<String> contextEntries, Set<String> lookups) throws CompilationFailedException {
        parseExpression(id, expression, properties, contextEntries, lookups, false);
    }

    /**
     * Parses the given groovy expression and creates the corresponding <code>Script</code> that
     * will be used when executing the rule. The passed set of properties and context entries will
     * be filled in during this process.
     * <p/>
     * Created on Jan 17, 2008 by depryf
     * @param id identifier of the script being parsed
     * @param expression expression to parse.
     * @param properties properties used in the expression (if null, they will not be gathered)
     * @param contextEntries context entries used in the expression (if null, they will not be gathered)
     * @param lookups lookup IDs used in the expression (if null, they will not be gathered)
     * @param forceDefKeyword if true and a variable is defined without the def keyword, then an exception will be raised
     * @throws CompilationFailedException
     */
    public void parseExpression(String id, String expression, Set<String> properties, Set<String> contextEntries, Set<String> lookups, boolean forceDefKeyword) throws CompilationFailedException {
        if (expression == null || expression.trim().isEmpty())
            expression = "return true";

        SourceUnit su = SourceUnit.create(id, expression);
        su.parse();
        su.completePhase();
        su.convert();
        ModuleNode tree = su.getAST();
        EditCodeVisitorSupport visitor = new EditCodeVisitorSupport(properties, contextEntries, lookups, forceDefKeyword);
        tree.getStatementBlock().visit(visitor);
        for (MethodNode method : tree.getMethods())
            method.getCode().visit(visitor);
    }

    /**
     * Compile the provided expression into a Groovy script.
     * <p/>
     * Created on Jun 28, 2011 by depryf
     * @param expression expression to compile
     * @return Groovy Script
     * @throws CompilationFailedException
     */
    public Script compileExpression(String expression) throws CompilationFailedException {
        if (expression == null || expression.trim().isEmpty())
            expression = "return true";

        return new GroovyShell().parse(expression);
    }

    /**
     * Replaces the property tags by their value (for example {line.vitalStatus})
     * <p/>
     * Created on Aug 27, 2010 by depryf
     * @param msg message to full
     * @param validatable current validatable
     * @return replaced message
     */
    public String fillInMessage(String msg, Validatable validatable) {
        if (msg == null)
            return "";

        List<Integer> starts = new ArrayList<>();
        List<Integer> ends = new ArrayList<>();
        List<String> values = new ArrayList<>();

        Matcher matcher = _PROP_REPLACEMENT_PATTERN.matcher(msg);
        while (matcher.find()) {
            String[] parts = StringUtils.split(matcher.group(2), '.');

            if (parts.length >= 2) {
                String prefix = parts[0], propertyName = parts[1], suffix = parts.length == 3 ? parts[2] : null;
                boolean error = false;
                Object replacement = null;

                Object obj = validatable.getScope().get(prefix);
                if (obj instanceof Map<?, ?>)
                    replacement = ((Map<?, ?>)obj).get(propertyName);
                else {
                    try {
                        Field field = obj.getClass().getDeclaredField(propertyName);
                        field.setAccessible(true);
                        replacement = field.get(obj);
                    }
                    catch (IllegalAccessException | NoSuchFieldException e) {
                        error = true;
                    }
                }

                starts.add(matcher.start());
                ends.add(matcher.end());

                String value;
                if (replacement != null && "formatDate()".equals(suffix)) {
                    value = replacement.toString().trim();

                    // check if we need to format a date...
                    if ("formatDate()".equals(suffix)) {
                        if (value.length() == 8)
                            value = "Y:" + value.substring(0, 4) + " M:" + value.substring(4, 6) + " D:" + value.substring(6);
                        else if (value.length() == 6)
                            value = "Y:" + value.substring(0, 4) + " M:" + value.substring(4, 6) + " D:";
                        else if (value.length() == 4)
                            value = "Y:" + value + " M:   D:";
                        else
                            value = "Y:     M:   D:";
                    }
                }
                else if (error)
                    value = "<ERROR>";
                else if (replacement == null || replacement.toString().trim().isEmpty())
                    value = "<BLANK>";
                else
                    value = replacement.toString().trim();

                values.add(value);
            }
        }

        StringBuilder buf = new StringBuilder(msg);
        for (int i = starts.size() - 1; i >= 0; i--)
            buf.replace(starts.get(i), ends.get(i), values.get(i));

        return buf.toString();
    }

    public int compareEngineVersions(String version1, String version2) {
        if (version1 == null)
            return -1;
        if (version2 == null)
            return 1;

        if (!_VERSIONS_PATTERN.matcher(version1).matches() || !_VERSIONS_PATTERN.matcher(version2).matches())
            return version1.compareTo(version2);

        String[] parts1 = StringUtils.split(version1, '.');
        String[] parts2 = StringUtils.split(version2, '.');

        List<Integer> list1 = new ArrayList<>(), list2 = new ArrayList<>();
        for (int i = 0; i < Math.max(parts1.length, parts2.length); i++) {
            if (i < parts1.length)
                list1.add(Integer.valueOf(parts1[i]));
            else
                list1.add(0);
        }
        for (int i = 0; i < Math.max(parts1.length, parts2.length); i++) {
            if (i < parts2.length)
                list2.add(Integer.valueOf(parts2[i]));
            else
                list2.add(0);
        }

        for (int i = 0; i < list1.size(); i++) {
            int result = list2.get(i).compareTo(list1.get(i)) * -1;
            if (result != 0)
                return result;
        }

        return 0;
    }
}
