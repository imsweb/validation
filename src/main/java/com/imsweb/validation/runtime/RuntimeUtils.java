/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.validation.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.imsweb.validation.ValidationEngineInitializationStats;

public class RuntimeUtils {

    public static String RUNTIME_PACKAGE_PREFIX = "com.imsweb.validation.runtime.";

    public static String createMethodName(String ruleId) {
        if (ruleId == null || ruleId.isEmpty())
            throw new RuntimeException("Rule ID cannot be blank!");

        String[] parts = StringUtils.split(ruleId.replaceAll("\\s+|-+|/|_|\\.", " ").replaceAll("\\(.+\\)|[\\W&&[^\\s]]", ""), ' ');

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

    public static CompiledRules findCompileRules(String validatorId, ValidationEngineInitializationStats stats) {
        CompiledRules compiledRules;
        try {
            compiledRules = (CompiledRules)(Class.forName(RUNTIME_PACKAGE_PREFIX + createCompiledRulesClassName(validatorId)).newInstance());
        }
        catch (ClassNotFoundException e) {
            stats.setReasonNotPreCompiled(validatorId, ValidationEngineInitializationStats.REASON_CLASS_NOT_FOUND);
            compiledRules = null;
        }
        catch (InstantiationException e) {
            stats.setReasonNotPreCompiled(validatorId, ValidationEngineInitializationStats.REASON_CLASS_INSTANCIATION_ERROR);
            compiledRules = null;
        }
        catch (IllegalAccessException e) {
            stats.setReasonNotPreCompiled(validatorId, ValidationEngineInitializationStats.REASON_CLASS_ACCESS_ERROR);
            compiledRules = null;
        }
        catch (ClassCastException e) {
            stats.setReasonNotPreCompiled(validatorId, ValidationEngineInitializationStats.REASON_CLASS_CAST_ERROR);
            compiledRules = null;
        }
        return compiledRules;
    }

    public static Method findCompiledMethod(CompiledRules compiledRules, String ruleId, List<Class<?>> parameters) {
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
            parsedProperties = (ParsedProperties)(Class.forName(RUNTIME_PACKAGE_PREFIX + createParsedPropertiesClassName(validatorId)).newInstance());
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException e) {
            parsedProperties = null;
        }
        return parsedProperties;
    }

    @SuppressWarnings("unchecked")
    public static Set<String> getParsedProperties(ParsedProperties properties, String ruleId) {
        if (properties == null)
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
            parsedContexts = (ParsedContexts)(Class.forName(RUNTIME_PACKAGE_PREFIX + createParsedContextsClassName(validatorId)).newInstance());
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException e) {
            parsedContexts = null;
        }
        return parsedContexts;
    }

    @SuppressWarnings("unchecked")
    public static Set<String> getParsedContexts(ParsedContexts contexts, String ruleId) {
        if (contexts == null)
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
            parsedLookups = (ParsedLookups)(Class.forName(RUNTIME_PACKAGE_PREFIX + createParsedLookupsClassName(validatorId)).newInstance());
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException e) {
            parsedLookups = null;
        }
        return parsedLookups;
    }

    @SuppressWarnings("unchecked")
    public static Set<String> getParsedLookups(ParsedLookups lookups, String ruleId) {
        if (lookups == null)
            return null;
        try {
            return (Set<String>)lookups.getClass().getMethod(createMethodName(ruleId)).invoke(lookups);
        }
        catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            return null;
        }
    }
}
