/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.validation.runtime;

import com.imsweb.validation.InitializationStats;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static com.imsweb.validation.InitializationStats.REASON_CLASS_ACCESS_ERROR;
import static com.imsweb.validation.InitializationStats.REASON_CLASS_CAST_ERROR;
import static com.imsweb.validation.InitializationStats.REASON_CLASS_INSTANCIATION_ERROR;
import static com.imsweb.validation.InitializationStats.REASON_CLASS_NOT_FOUND;
import static com.imsweb.validation.InitializationStats.REASON_CONSTRUCTOR_NOT_FOUND;
import static com.imsweb.validation.InitializationStats.REASON_DIFFERENT_VERSION;

/**
 * This class is used by the engine to support pre-parsed and pre-compiled edits.
 * <br/>
 * The engine will used pre-parsed and/or pre-compiled edits if it finds them on the classpath, otherwise it will default back to the regular edits.
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
 * The classes and methods must follow strict naming conventions to be found by the engine on the classpath; see the code for the conventions.
 * <br/>
 * Typically a caller will use the "create" methods in this class instead of trying to implement the conventions; that's by far the safest way.
 */
public class RuntimeUtils {

    public static String RUNTIME_PACKAGE_PREFIX = "com.imsweb.validation.runtime.";

    private static Pattern _P1 = Pattern.compile("\\s+|-+|/|\\.");
    private static Pattern _P2 = Pattern.compile("[()]");
    private static Pattern _P3 = Pattern.compile("[\\W&&[^\\s]]");
    private static Pattern _P4 = Pattern.compile("^_|_$");

    public static String createMethodName(String ruleId) {
        if (ruleId == null || ruleId.isEmpty())
            throw new RuntimeException("Rule ID cannot be blank!");

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
        return result.toString() + "CompiledRules";
    }

    public static CompiledRules findCompileRules(String validatorId, String version, InitializationStats stats) {
        CompiledRules compiledRules;

        String classPath = RUNTIME_PACKAGE_PREFIX + createCompiledRulesClassName(validatorId);
        try {
            compiledRules = (CompiledRules)(Class.forName(classPath).getDeclaredConstructor().newInstance());
        }
        catch (ClassNotFoundException e) {
            if (stats != null)
                stats.setReasonNotPreCompiled(validatorId, REASON_CLASS_NOT_FOUND.replace("{0}", classPath));
            compiledRules = null;
        }
        catch (InstantiationException e) {
            if (stats != null)
                stats.setReasonNotPreCompiled(validatorId, REASON_CLASS_INSTANCIATION_ERROR.replace("{0}", classPath));
            compiledRules = null;
        }
        catch (IllegalAccessException e) {
            if (stats != null)
                stats.setReasonNotPreCompiled(validatorId, REASON_CLASS_ACCESS_ERROR.replace("{0}", classPath));
            compiledRules = null;
        }
        catch (ClassCastException e) {
            if (stats != null)
                stats.setReasonNotPreCompiled(validatorId, REASON_CLASS_CAST_ERROR.replace("{0}", classPath));
            compiledRules = null;
        }
        catch (NoSuchMethodException | InvocationTargetException e) {
            if (stats != null)
                stats.setReasonNotPreCompiled(validatorId, REASON_CONSTRUCTOR_NOT_FOUND.replace("{0}", classPath));
            compiledRules = null;
        }

        if (compiledRules != null && !StringUtils.isBlank(version) && !version.equals(compiledRules.getValidatorVersion())) {
            if (stats != null)
                stats.setReasonNotPreCompiled(validatorId, REASON_DIFFERENT_VERSION.replace("{0}", compiledRules.getValidatorVersion()).replace("{1}", version));
            compiledRules = null;
        }

        return compiledRules;
    }

    public static Method findCompiledMethod(CompiledRules compiledRules, String ruleId, List<Class<?>> parameters) {
        if (ruleId == null)
            return null;
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
        return result.toString() + "ParsedProperties";

    }

    public static ParsedProperties findParsedProperties(String validatorId) {
        ParsedProperties parsedProperties;
        try {
            parsedProperties = (ParsedProperties)(Class.forName(RUNTIME_PACKAGE_PREFIX + createParsedPropertiesClassName(validatorId)).getDeclaredConstructor().newInstance());
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException | NoSuchMethodException | InvocationTargetException e) {
            parsedProperties = null;
        }
        return parsedProperties;
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
        return result.toString() + "ParsedContexts";
    }

    public static ParsedContexts findParsedContexts(String validatorId) {
        ParsedContexts parsedContexts;
        try {
            parsedContexts = (ParsedContexts)(Class.forName(RUNTIME_PACKAGE_PREFIX + createParsedContextsClassName(validatorId)).getDeclaredConstructor().newInstance());
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException | NoSuchMethodException | InvocationTargetException e) {
            parsedContexts = null;
        }
        return parsedContexts;
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
        return result.toString() + "ParsedLookups";
    }

    public static ParsedLookups findParsedLookups(String validatorId) {
        ParsedLookups parsedLookups;
        try {
            parsedLookups = (ParsedLookups)(Class.forName(RUNTIME_PACKAGE_PREFIX + createParsedLookupsClassName(validatorId)).getDeclaredConstructor().newInstance());
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException | NoSuchMethodException | InvocationTargetException e) {
            parsedLookups = null;
        }
        return parsedLookups;
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
