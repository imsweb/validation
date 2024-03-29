/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.validation.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.imsweb.validation.InitializationStats;
import com.imsweb.validation.entities.Validator;

import static com.imsweb.validation.InitializationStats.REASON_DIFFERENT_VERSION;
import static com.imsweb.validation.InitializationStats.REASON_NOT_PROVIDED;

/**
 * This class is used by the engine to support pre-parsed and pre-compiled edits.
 * <br/>
 * The engine will used pre-parsed and/or pre-compiled edits if they are provided during initialization, otherwise it will default back to the regular edits.
 * <br/><br/>
 * Pre-parsed and pre-compiled edits need to be provided via a class implementing the <code>RuntimeEdits</code> interface.
 * <br/>
 * Pre-parsed edits need to be included in three files implementing a specific interface:
 * <ul>
 * <li>ParsedProperties: used to find the pre-parsed used properties</li>
 * <li>ParsedLookups: used to find the pre-parsed used lookups</li>
 * <li>ParsedContexts: used to find the pre-parsed used context keys</li>
 * </ul>
 * Pre-compiled edits need to be included in a single file implementing a specific interface:
 * <ul>
 * <li>CompiledRules: used to find pre-compiled edit expressions</li>
 * </ul>
 * The methods in those classes must follow strict naming conventions to be found by the engine; see the code for the conventions.
 * <br/>
 * Typically a caller will use the "create" methods in this class instead of trying to implement the conventions; that's by far the safest way.
 */
public final class RuntimeUtils {

    private static final Pattern _P1 = Pattern.compile("\\s+|-+|/|\\.");
    private static final Pattern _P2 = Pattern.compile("[()]");
    private static final Pattern _P3 = Pattern.compile("[\\W&&[^\\s]]");
    private static final Pattern _P4 = Pattern.compile("(^_)|(_$)");

    private RuntimeUtils() {
        // utility class
    }

    public static String createMethodName(String ruleId) {
        if (ruleId == null || ruleId.isEmpty())
            throw new IllegalStateException("Rule ID cannot be blank!");

        ruleId = _P1.matcher(ruleId).replaceAll(" ");
        ruleId = _P2.matcher(ruleId).replaceAll("_");
        ruleId = _P3.matcher(ruleId).replaceAll("");
        ruleId = _P4.matcher(ruleId).replaceAll("");

        String[] parts = StringUtils.split(ruleId, ' ');

        StringBuilder buf = new StringBuilder();
        buf.append(StringUtils.uncapitalize(parts[0].toLowerCase()));
        for (int i = 1; i < parts.length; i++)
            buf.append(StringUtils.capitalize(parts[i].toLowerCase()));

        return buf.toString();
    }

    public static String createCompiledRulesClassName(String validatorId) {
        StringBuilder result = new StringBuilder();
        for (String s : StringUtils.split(validatorId, "-"))
            result.append(StringUtils.capitalize(s));
        return result + "CompiledRules";
    }

    public static CompiledRules findCompileRules(Validator validator, InitializationStats stats) {
        if (validator == null)
            return null;
        CompiledRules compiledRules = validator.getCompiledRules();
        if (compiledRules != null) {
            if (!StringUtils.isBlank(validator.getVersion()) && !validator.getVersion().equals(compiledRules.getValidatorVersion())) {
                if (stats != null)
                    stats.setReasonNotPreCompiled(validator.getId(), REASON_DIFFERENT_VERSION.replace("{0}", compiledRules.getValidatorVersion()).replace("{1}", validator.getVersion()));
                compiledRules = null;
            }
        }
        else if (stats != null)
            stats.setReasonNotPreCompiled(validator.getId(), REASON_NOT_PROVIDED);
        return compiledRules;
    }

    public static Method findCompiledMethod(CompiledRules compiledRules, String ruleId, List<Class<?>> parameters) {
        if (ruleId == null || compiledRules == null)
            return null;

        if (parameters == null)
            throw new IllegalStateException("Got null parameters for rule ID '" + ruleId + "'");

        try {
            return compiledRules.getClass().getMethod(RuntimeUtils.createMethodName(ruleId), parameters.toArray(new Class[0]));
        }
        catch (NoSuchMethodException e) {
            return null;
        }
    }

    public static String createParsedPropertiesClassName(String validatorId) {
        StringBuilder result = new StringBuilder();
        for (String s : StringUtils.split(validatorId, "-"))
            result.append(StringUtils.capitalize(s));
        return result + "ParsedProperties";

    }

    @SuppressWarnings("unchecked")
    public static Set<String> getParsedProperties(ParsedProperties properties, String ruleId) {
        if (properties == null || ruleId == null)
            return null;
        try {
            return (Set<String>)properties.getClass().getMethod(createMethodName(ruleId)).invoke(properties);
        }
        catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            return null;
        }
    }

    public static String createParsedContextsClassName(String validatorId) {
        StringBuilder result = new StringBuilder();
        for (String s : StringUtils.split(validatorId, "-"))
            result.append(StringUtils.capitalize(s));
        return result + "ParsedContexts";
    }

    @SuppressWarnings("unchecked")
    public static Set<String> getParsedContexts(ParsedContexts contexts, String ruleId) {
        if (contexts == null || ruleId == null)
            return null;
        try {
            return (Set<String>)contexts.getClass().getMethod(createMethodName(ruleId)).invoke(contexts);
        }
        catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            return null;
        }
    }

    public static String createParsedLookupsClassName(String validatorId) {
        StringBuilder result = new StringBuilder();
        for (String s : StringUtils.split(validatorId, "-"))
            result.append(StringUtils.capitalize(s));
        return result + "ParsedLookups";
    }

    @SuppressWarnings("unchecked")
    public static Set<String> getParsedLookups(ParsedLookups lookups, String ruleId) {
        if (lookups == null || ruleId == null)
            return null;
        try {
            return (Set<String>)lookups.getClass().getMethod(createMethodName(ruleId)).invoke(lookups);
        }
        catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            return null;
        }
    }
}
