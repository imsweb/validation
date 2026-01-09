/*
 * Copyright (C) 2004 Information Management Services, Inc.
 */
package com.imsweb.validation;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import groovy.lang.Binding;

import com.imsweb.validation.internal.ExtraPropertyEntityHandlerDto;

/**
 * Helper methods made available to the edits.
 * <br/><br/>
 * These functions need to be initialized before executing any edits in the framework. This class only provides basic methods;
 * there are more advanced implementations that extends this class and can be provided in the initialize() method.
 * <br/><br/>
 * To initialize the framework with these basic method, call the following:<br/><br/>
 * <i>ValidationContextFunctions.initialize(new ValidatorContextFunction())</i>
 * As of version 1.5, the functions are lazily initialized with the default implementation; so if you don't need a special implementation,
 * there is no need to initialize this class anymore.
 * <br/><br/>
 * To add your own methods to the context of the edits, create a class that extends this one, add the methods, and call the following:<br/><br/>
 * <i>ValidationContextFunctions.initialize(new MyValidatorContextFunction())</i>
 * <br/><br/>
 * A special method in this class is the "getContext" method; it allows an edit from one validator to access a context from
 * another validator. In version 2.0, the engine was re-written to become non-static and allow multiple engines to run
 * concurrently (if you are not planning on using that feature, you can stop reading!). The method uses the static cached
 * engine (see ValidationEngine.getInstance()) and therefore it is not compatible with multiple engines. This is a known
 * issue that might be fixed in the future, but since referencing contexts from other validators is very uncommon, the
 * issue will be left unresolved for now.
 */
public class ValidationContextFunctions {

    // unique private instance
    private static ValidationContextFunctions _INSTANCE = new ValidationContextFunctions();

    /**
     * Initializes this class with the passed instance.
     * <br/><br/>
     * If it was already initialized with another instance, the previous one will be overridden with the new one. Use the
     * <i>isInitialized()</i> methods if you don't want this behavior.
     * <p/>
     * Created on Feb 11, 2008 by depryf
     * @param instance a <code>ValidationContextFunctions</code> instance
     */
    public static void initialize(ValidationContextFunctions instance) {
        _INSTANCE = instance;
    }

    /**
     * Gets current instance of the <code>ValidationContextFunctions</code>
     * <p/>
     * Created on Feb 11, 2008 by depryf
     * @return a <code>ValidationContextFunctions</code>
     */
    public static ValidationContextFunctions getInstance() {
        return _INSTANCE;
    }

    /**
     * Returns documentation about any methods available in the context of the edits execution.
     * <p/>
     * Created on Mar 3, 2010 by depryf
     * @return a list of <code>ValidatorContextFunctionDto</code>
     */
    public static List<ContextFunctionDocDto> getMethodsDocumentation() {
        if (_INSTANCE == null)
            throw new IllegalStateException("Validation Context Functions have not been initialized!");

        List<ContextFunctionDocDto> dtos = new ArrayList<>();

        for (Method m : _INSTANCE.getClass().getMethods()) {
            ContextFunctionDocAnnotation annotation = m.getAnnotation(ContextFunctionDocAnnotation.class);
            if (annotation != null) {
                ContextFunctionDocDto dto = new ContextFunctionDocDto();
                dto.setMethodName(m.getName());
                if (!StringUtils.isEmpty(annotation.param1()))
                    dto.getParams().add(annotation.param1());
                if (!StringUtils.isEmpty(annotation.param2()))
                    dto.getParams().add(annotation.param2());
                if (!StringUtils.isEmpty(annotation.param3()))
                    dto.getParams().add(annotation.param3());
                if (!StringUtils.isEmpty(annotation.param4()))
                    dto.getParams().add(annotation.param4());
                if (!StringUtils.isEmpty(annotation.param5()))
                    dto.getParams().add(annotation.param5());
                if (!StringUtils.isEmpty(annotation.param6()))
                    dto.getParams().add(annotation.param6());
                if (!StringUtils.isEmpty(annotation.param7()))
                    dto.getParams().add(annotation.param7());
                if (!StringUtils.isEmpty(annotation.param8()))
                    dto.getParams().add(annotation.param8());
                if (!StringUtils.isEmpty(annotation.param9()))
                    dto.getParams().add(annotation.param9());
                if (!StringUtils.isEmpty(annotation.param10()))
                    dto.getParams().add(annotation.param10());
                if (!StringUtils.isEmpty(annotation.paramName1()))
                    dto.getParamNames().add(annotation.paramName1());
                if (!StringUtils.isEmpty(annotation.paramName2()))
                    dto.getParamNames().add(annotation.paramName2());
                if (!StringUtils.isEmpty(annotation.paramName3()))
                    dto.getParamNames().add(annotation.paramName3());
                if (!StringUtils.isEmpty(annotation.paramName4()))
                    dto.getParamNames().add(annotation.paramName4());
                if (!StringUtils.isEmpty(annotation.paramName5()))
                    dto.getParamNames().add(annotation.paramName5());
                if (!StringUtils.isEmpty(annotation.paramName6()))
                    dto.getParamNames().add(annotation.paramName6());
                if (!StringUtils.isEmpty(annotation.paramName7()))
                    dto.getParamNames().add(annotation.paramName7());
                if (!StringUtils.isEmpty(annotation.paramName8()))
                    dto.getParamNames().add(annotation.paramName8());
                if (!StringUtils.isEmpty(annotation.paramName9()))
                    dto.getParamNames().add(annotation.paramName9());
                if (!StringUtils.isEmpty(annotation.paramName10()))
                    dto.getParamNames().add(annotation.paramName10());
                dto.setDescription(annotation.desc());
                dto.setExample(annotation.example());
                dtos.add(dto);
            }
        }

        return dtos;
    }

    // cached regular expressions
    private Map<String, Pattern> _regexCache;

    // maximum size of the regex cache (-1 for no limit)
    private AtomicInteger _regexCacheSize;

    // stats for the cached regular expressions
    private AtomicLong _numRegexCacheHit;
    private AtomicLong _numRegexCacheMiss;

    /**
     * Forces the given entity (corresponding to the given collection name) to report the given properties when the edit fails.
     * <p/>
     * There can be any number of properties. But if none are provided, then the properties gathered statically from the edit text will be used instead.
     * <p/>
     * Created on Apr 21, 2010 by depryf
     * @param binding groovy binding (cannot be null)
     * @param entity entity on which the properties need to be reported (cannot be null)
     * @param properties properties to report; if none are provided, the failures will be reported for all the static properties
     */
    @SuppressWarnings("unchecked")
    @ContextFunctionDocAnnotation(paramName1 = "binding", param1 = "Groovy binding (always called 'binding')", paramName2 = "entity",
            param2 = "entity to force the failure on; what entity means is application-dependent", paramName3 = "properties",
            param3 = "optional properties (with alias prefix) to report the failure on; if none are provided, the failure will be reported for all properties used in the edit",
            desc = "Forces a failure on the provided entity, this can be useful in situations where an edits iterates over sub-entities and a failure needs to be reported on some of those sub-entities.",
            example = "Functions.forceFailureOnEntity(binding, line)\nFunctions.forceFailureOnEntity(binding, line, 'line.nameLast')\nFunctions.forceFailureOnEntity(binding, line, 'line.nameLast', 'line.nameFirst')")
    public void forceFailureOnEntity(Binding binding, Object entity, String... properties) {
        if (binding == null || entity == null)
            return;

        Set<ExtraPropertyEntityHandlerDto> forcedEntities = (Set<ExtraPropertyEntityHandlerDto>)binding.getVariable(ValidationEngine.VALIDATOR_FORCE_FAILURE_ENTITY_KEY);
        if (forcedEntities == null) {
            forcedEntities = new HashSet<>();
            binding.setVariable(ValidationEngine.VALIDATOR_FORCE_FAILURE_ENTITY_KEY, forcedEntities);
        }
        forcedEntities.add(new ExtraPropertyEntityHandlerDto(entity, properties));
    }

    /**
     * Forces the given properties to be reported if the edit fails.
     * <p/>
     * Created on Apr 27, 2010 by depryf
     * @param binding groovy binding (cannot be null)
     * @param properties properties to report (with alias prefix); if null or empty then the function does nothing
     */
    @SuppressWarnings("unchecked")
    @ContextFunctionDocAnnotation(paramName1 = "binding", param1 = "Groovy binding (always called 'binding')", paramName2 = "properties",
            param2 = "properties (with alias prefix) to report the failure on; if none are provided, this function does nothing",
            desc = "Forces the provided properties to be reported when a failure happens; the properties will be reported on the current entity being validated (what entity means is application-dependent).",
            example = "Functions.forceFailureOnProperty(binding, 'line.nameLast')\nFunctions.forceFailureOnProperty(binding, 'line.nameLast', 'line.nameFirst')")
    public void forceFailureOnProperty(Binding binding, String... properties) {
        if (properties == null || properties.length == 0)
            return;

        Set<String> forcedProperties = (Set<String>)binding.getVariable(ValidationEngine.VALIDATOR_FORCE_FAILURE_PROPERTY_KEY);
        if (forcedProperties == null) {
            forcedProperties = new HashSet<>();
            binding.setVariable(ValidationEngine.VALIDATOR_FORCE_FAILURE_PROPERTY_KEY, forcedProperties);
        }
        forcedProperties.addAll(Arrays.asList(properties));
    }

    /**
     * Forces the given properties to be ignored if the edit fails.
     * <p/>
     * Created on Apr 27, 2010 by depryf
     * @param binding groovy binding (cannot be null)
     * @param properties properties to ignore (with alias prefix); if null or empty then the function does nothing
     */
    @SuppressWarnings("unchecked")
    @ContextFunctionDocAnnotation(paramName1 = "binding", param1 = "Groovy binding (always called 'binding')", paramName2 = "properties",
            param2 = "properties (with alias prefix) to ignore; if none are provided, this function does nothing", desc = "Ignores the provided properties when reporting a failure for the edit.",
            example = "Functions.ignoreFailureOnProperty(binding, 'line.nameLast')\nFunctions.ignoreFailureOnProperty(binding, 'line.nameLast', 'line.nameFirst')")
    public void ignoreFailureOnProperty(Binding binding, String... properties) {
        if (properties == null || properties.length == 0)
            return;

        Set<String> ignoredProperties = (Set<String>)binding.getVariable(ValidationEngine.VALIDATOR_IGNORE_FAILURE_PROPERTY_KEY);
        if (ignoredProperties == null) {
            ignoredProperties = new HashSet<>();
            binding.setVariable(ValidationEngine.VALIDATOR_IGNORE_FAILURE_PROPERTY_KEY, ignoredProperties);
        }
        ignoredProperties.addAll(Arrays.asList(properties));
    }

    /**
     * Gets the value defined in the context of the passed validator ID, under the passed key
     * <p/>
     * Created on Nov 12, 2007 by depryf
     * @param validatorId validator ID
     * @param contextKey context key
     * @return an object, possibly null
     * @throws ValidationException if provided validator ID or context key are null or invalid
     */
    @ContextFunctionDocAnnotation(paramName1 = "validatorId", param1 = "validator ID", paramName2 = "contextKey", param2 = "context key",
            desc = "Returns the value of the requested context key, throws an exception if the context is not found.", example = "Functions.getContext('seer', 'Birthplace_Table')")
    public Object getContext(String validatorId, String contextKey) throws ValidationException {
        if (validatorId == null)
            throw new ValidationException("Group is required when accessing a context entry.");
        if (contextKey == null)
            throw new ValidationException("Context key is required when accessing a context entry.");

        // this method uses the default (static) cached engine, this is a know limitation that hopefully won't cause trouble to anybody
        Object context = ValidationEngine.getInstance().getContext(contextKey, validatorId);
        if (context == null)
            throw new ValidationException("Unknown context key '" + contextKey + "' from group '" + validatorId + "'");

        return context;
    }

    /**
     * Returns the <code>ValidationLookup</code> corresponding to the passed ID, throws an exception if such a lookup doesn't exist.
     * <p/>
     * Created on Dec 20, 2007 by depryf
     * @param id lookup ID
     * @return a <code>ValidationLookup</code>, never null
     * @throws ValidationException if provided lookup ID is null or invalid
     */
    @ContextFunctionDocAnnotation(paramName1 = "id", param1 = "Lookup ID", desc = """
            Returns the lookup corresponding to the requested ID.
            
            The returned object is a ValidationLookup on which the following methods are available:
                String getId()
                String getByKey(String key)
                String getByKeyWithCase(String key)
                Set<String> getAllByKey(String key)
                Set<String> getAllByKeyWithCase(String key)
                Set<String> getAllKeys()
                String getByValue(String value)
                String getByValueWithCase(String value)
                Set<String> getAllByValue(String value)
                Set<String> getAllByValueWithCase(String value)
                Set<String> getAllValues()
                boolean containsKey(Object key)
                boolean containsKeyWithCase(Object key)
                boolean containsValue(Object value)
                boolean containsValueWithCase(Object value)
                boolean containsPair(String key, String value)
                boolean containsPairWithCase(String key, String value)
            """,
            example = "Functions.fetchLookup('lookup_id').containsKey(value)")
    public ValidationLookup fetchLookup(String id) throws ValidationException {
        if (id == null)
            throw new ValidationException("Unable to load lookup <null>");

        ValidationLookup lookup = ValidationServices.getInstance().getLookupById(id);
        if (lookup == null)
            throw new ValidationException("Unable to load lookup '" + id + "'");

        return lookup;
    }

    /**
     * Returns the value corresponding to the passed ID from our configuration files. Returns null if such an ID doesn't exist.
     * <p/>
     * Created on Dec 20, 2007 by depryf
     * @param id configuration variable ID
     * @return corresponding value, null if it doesn't exist
     * @throws ValidationException if provided ID is null
     */
    @ContextFunctionDocAnnotation(paramName1 = "id", param1 = "Configuration variable ID", desc = "Returns the value of the requested configuration variable.",
            example = "Functions.fetchConfVariable('id')")
    public Object fetchConfVariable(String id) throws ValidationException {
        if (id == null)
            throw new ValidationException("Unable to fetch configuration variable for null value");

        return ValidationServices.getInstance().getConfVariable(id);
    }

    /**
     * Logs the message
     * <p/>
     * Created on Dec 20, 2007 by depryf
     * @param message Message to log
     */
    @ContextFunctionDocAnnotation(paramName1 = "message", param1 = "Message", desc = "Logs the given message.",
            example = "Functions.log('message')")
    public void log(String message) {
        ValidationServices.getInstance().log(message);
    }

    /**
     * Logs the message as a warning
     * <p/>
     * Created on Dec 20, 2007 by depryf
     * @param message Message to log
     */
    @ContextFunctionDocAnnotation(paramName1 = "message", param1 = "Message", desc = "Logs the given message as a warning.",
            example = "Functions.logWarning('warning message')")
    public void logWarning(String message) {
        ValidationServices.getInstance().logWarning(message);
    }

    /**
     * Logs the message as an error
     * <p/>
     * Created on Dec 20, 2007 by depryf
     * @param message Message to log
     */
    @ContextFunctionDocAnnotation(paramName1 = "message", param1 = "Message", desc = "Logs the given message as a error.",
            example = "Functions.logError('error message')")
    public void logError(String message) {
        ValidationServices.getInstance().logError(message);
    }

    /**
     * Utility method that attempts to convert the supplied object to an Integer.
     * <p/>
     * Created on Dec 27, 2007 by depryf
     * @param value value
     * @return corresponding <code>Integer</code> value, or null if the conversion fails
     */
    @ContextFunctionDocAnnotation(paramName1 = "value", param1 = "value to convert to an Integer", desc = "Converts the passed value to an Integer, returns null if it can't be converted.",
            example = "Functions.asInt(record.ageAtDx)\nFunctions.asInt(25)\nFunctions.asInt('25')\nFunctions.asInt('whatever') would return null")
    public Integer asInt(Object value) {
        Integer result = null;

        if (value != null) {
            if (value instanceof Integer)
                result = (Integer)value;
            else if (value instanceof Number)
                result = ((Number)value).intValue();
            else {
                String str = value instanceof String ? (String)value : value.toString();
                if (NumberUtils.isDigits(str))
                    result = Integer.valueOf(str);
            }
        }

        return result;
    }

    /**
     * Checks that the passed value is between <code>low</code> and <code>high</code>; both inclusive.
     * <p/>
     * Created on Dec 27, 2007 by depryf
     * @param value value to check
     * @param low low limit to check against
     * @param high high limit to check against
     * @return true if value is between low and high, false otherwise
     */
    @ContextFunctionDocAnnotation(paramName1 = "value", param1 = "value to compare", paramName2 = "low", param2 = "low limit", paramName3 = "high", param3 = "high limit",
            desc = "Returns true if the value is between the low and hight limit (inclusive), false it is not or cannot be determined.",
            example = "Functions.between(2, 1, 3) -> true\nFunctions.between('B', 'A', 'C') -> true")
    public boolean between(Object value, Object low, Object high) {
        if (value == null || low == null || high == null)
            return false;

        if (value instanceof String val) {
            String l = low.toString();
            String h = high.toString();

            // special case, if the three params are numeric string, compare them as numeric, not strings
            if (NumberUtils.isDigits(val) && NumberUtils.isDigits(l) && NumberUtils.isDigits(h)) {
                long lVal = Long.parseLong(val);
                long lLow = Long.parseLong(l);
                long lHigh = Long.parseLong(h);
                return lVal >= lLow && lVal <= lHigh;
            }
            else
                return val.compareTo(l) >= 0 && val.compareTo(h) <= 0;
        }

        if (value instanceof Number && low instanceof Number && high instanceof Number) {
            double val = ((Number)value).doubleValue();
            double l = ((Number)low).doubleValue();
            double h = ((Number)high).doubleValue();

            return val >= l && val <= h;
        }

        return false;
    }

    /**
     * Returns the current day
     * <p/>
     * Created on Dec 27, 2007 by depryf
     * @return current day
     */
    @ContextFunctionDocAnnotation(desc = "Returns the current day as an integer.", example = "Functions.getCurrentDay()")
    public int getCurrentDay() {
        return LocalDate.now().getDayOfMonth();
    }

    /**
     * Returns the current month
     * <p/>
     * Created on Dec 27, 2007 by depryf
     * @return current month
     */
    @ContextFunctionDocAnnotation(desc = "Returns the current month as an integer.", example = "Functions.getCurrentMonth()")
    public int getCurrentMonth() {
        return LocalDate.now().getMonthValue();
    }

    /**
     * Returns the current year
     * <p/>
     * Created on Dec 27, 2007 by depryf
     * @return current year
     */
    @ContextFunctionDocAnnotation(desc = "Returns the current year as an integer.", example = "Functions.getCurrentYear()")
    public int getCurrentYear() {
        return LocalDate.now().getYear();
    }

    /**
     * No documentation on purpose, shouldn't be called from edits!
     * <br/><br/>
     * Enables the regex caching with unlimited cache size.
     */
    public void enableRegexCaching() {
        enableRegexCaching(Integer.MAX_VALUE);
    }

    /**
     * No documentation on purpose, shouldn't be called from edits!
     * <br/><br/>
     * Enables the regex caching with a maximum cache size. It is recommended to you this method only if using an unlimited cache size creates real memory issues.
     * @param cacheSize regex cache size, must be greater than 0.
     */
    public void enableRegexCaching(int cacheSize) {
        if (cacheSize < 0)
            throw new IllegalStateException("Cache size must be greater than 0!");
        _regexCache = new ConcurrentHashMap<>();
        _regexCacheSize = new AtomicInteger(cacheSize);
        _numRegexCacheHit = new AtomicLong();
        _numRegexCacheMiss = new AtomicLong();
    }

    /**
     * No documentation on purpose, shouldn't be called from edits!
     * <br/><br/>
     * Disables the regex caching
     */
    public void disableRegexCaching() {
        _regexCache = null;
        _regexCacheSize = new AtomicInteger(Integer.MAX_VALUE);
        _numRegexCacheHit = null;
        _numRegexCacheMiss = null;
    }

    /**
     * Returns true if the provided value matches according to the provided regular expression.
     * @param value value to match
     * @param regex regular expression (Java style) to match against
     * @return true if the value matches, false otherwise.
     */
    @ContextFunctionDocAnnotation(paramName1 = "value", param1 = "value to use with the regular expression", paramName2 = "regex", param2 = "regular expression",
            desc = "Returns true if the provided value matches the provided Java regular expression.",
            example = "Functions.matches('123', /\\d+/) -> true")
    public boolean matches(Object value, Object regex) {
        if (value == null || regex == null)
            return false;

        String val = value instanceof String ? (String)value : value.toString();
        String reg = regex instanceof String ? (String)regex : regex.toString();

        Pattern pattern;
        if (_regexCache != null) {
            pattern = _regexCache.get(reg);
            if (pattern == null) {
                _numRegexCacheMiss.incrementAndGet();
                pattern = Pattern.compile(reg);
                // in a multi-threaded environment, it's possible that the cache will add a few more values than the max cache size, and that's OK
                if (_regexCache.size() < _regexCacheSize.get())
                    _regexCache.put(reg, pattern);
            }
            else
                _numRegexCacheHit.incrementAndGet();
        }
        else
            pattern = Pattern.compile(reg);

        return pattern.matcher(val).matches();
    }

    /**
     * No documentation on purpose, shouldn't be called from edits!
     * <br/><br/>
     * Returns the number of hits in the regex cache.
     */
    public long getNumRegexCacheHit() {
        return _numRegexCacheHit == null ? 0L : _numRegexCacheHit.get();
    }

    /**
     * No documentation on purpose, shouldn't be called from edits!
     * <br/><br/>
     * Returns the number of misses in the regex cache.
     */
    public long getNumRegexCacheMiss() {
        return _numRegexCacheMiss == null ? 0L : _numRegexCacheMiss.get();
    }

        /**
     * Returns the difference in days between the two day/month/year. Returns -1 if it cannot be calculated.
     * <p/>
     * Created on Dec 27, 2007 by depryf
     * @param day1 first day
     * @param month1 first month
     * @param year1 first year
     * @param day2 second day
     * @param month2 second month
     * @param year2 second year
     * @return difference in days between the two dates
     */
    @SuppressWarnings("MagicConstant")
    @ContextFunctionDocAnnotation(paramName1 = "day1", param1 = "day of first date", paramName2 = "month1", param2 = "month of first date", paramName3 = "year1", param3 = "year of first date",
            paramName4 = "day2", param4 = "day of second date", paramName5 = "month2", param5 = "month of second day", paramName6 = "year2", param6 = "year of second date",
            desc = "Returns the difference in days between the first provided day/month/year and the second provided day/month/year, returns -1 if that cannot be determined.",
            example = "def days = differenceInDays(day1, month1, year1, day2, month2, year2)")
    public int differenceInDays(Object day1, Object month1, Object year1, Object day2, Object month2, Object year2) {

        Integer y1 = asInt(year1);
        if (y1 != null)
            y1 = y1 == 9999 ? null : y1;
        Integer m1 = asInt(month1);
        if (m1 != null)
            m1 = m1 == 99 ? null : m1;
        Integer d1 = asInt(day1);
        if (d1 != null)
            d1 = d1 == 99 ? null : d1;
        Integer y2 = asInt(year2);
        if (y2 != null)
            y2 = y2 == 9999 ? null : y2;
        Integer m2 = asInt(month2);
        if (m2 != null)
            m2 = m2 == 99 ? null : m2;
        Integer d2 = asInt(day2);
        if (d2 != null)
            d2 = d2 == 99 ? null : d2;

        if (y1 == null || m1 == null || y2 == null || m2 == null)
            return -1;

        Calendar cal1 = new GregorianCalendar(y1, m1 - 1, d1 == null ? 1 : d1);
        Calendar cal2 = new GregorianCalendar(y2, m2 - 1, d2 == null ? 1 : d2);

        // fix possible daylight saving time problem
        long fromL = cal1.getTimeInMillis() + cal1.getTimeZone().getOffset(cal1.getTimeInMillis());
        long toL = cal2.getTimeInMillis() + cal2.getTimeZone().getOffset(cal2.getTimeInMillis());
        long difference = toL - fromL;

        return (int)(difference / (1000 * 60 * 60 * 24));
    }
}

