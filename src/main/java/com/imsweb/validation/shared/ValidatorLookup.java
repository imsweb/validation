/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation.shared;

import java.util.Set;

/**
 * This interface defines the methods used by the validation engine when dealing with lookups.
 * <p/>
 * Not all projects use lookups, and when they do, they do not use the same implemetation. But to
 * be used in edits, all those implementations must implement this interface.
 * <p/>
 * Created on Apr 5, 2011 by depryf
 * @author depryf
 */
public interface ValidatorLookup {

    /**
     * Returns the ID for this lookup
     * <p/>
     * Created on Mar 10, 2008 by depryf
     * @return ID for this lookup, never null
     */
    String getId();

    /**
     * Returns the value corresponding to the given key. Comparison is NOT case-sensitive.
     * If several values exist for the passed key, the first one found will be returned.
     * <p/>
     * Created on Jul 28, 2004 by depryf
     * @param key requested key
     * @return the corresponding value, null if there isn't one
     */
    String getByKey(String key);

    /**
     * Returns the value corresponding to the given key. Comparison is case-sensitive.
     * If several values exist for the passed key, the first one found will be returned.
     * <p/>
     * Created on Jul 28, 2004 by depryf
     * @param key requested key
     * @return the corresponding value, null if there isn't one
     */
    String getByKeyWithCase(String key);

    /**
     * Returns all the values corresponding to the given key. Comparison is NOT case-sensitive.
     * <p/>
     * Created on Feb 19, 2008 by depryf
     * @param key requested value
     * @return a collection of <code>String</code>, maybe empty but never null
     */
    Set<String> getAllByKey(String key);

    /**
     * Returns all the values corresponding to the given key. Comparison is case-sensitive.
     * <p/>
     * Created on Feb 19, 2008 by depryf
     * @param key requested value
     * @return a collection of <code>String</code>, maybe empty but never null
     */
    Set<String> getAllByKeyWithCase(String key);

    /**
     * Returns all the keys defined within this lookup.
     * <p/>
     * Created on Jul 1, 2008 by depryf
     * @return a set of keys, maybe empty but never null
     */
    Set<String> getAllKeys();

    /**
     * Returns the key corresponding to the given value. Comparison is NOT case-sensitive.
     * If several keys exist for the passed value, the first one found will be returned.
     * <p/>
     * Created on Jul 28, 2004 by depryf
     * @param value requested value
     * @return the corresponding key, null if there isn't one
     */
    String getByValue(String value);

    /**
     * Returns the key corresponding to the given value. Comparison is case-sensitive.
     * If several keys exist for the passed value, the first one found will be returned.
     * <p/>
     * Created on Jul 28, 2004 by depryf
     * @param value requested value
     * @return the corresponding key, null if there isn't one
     */
    String getByValueWithCase(String value);

    /**
     * Returns all the <code>String</code> corresponding to the given value. Comparison is NOT case-sensitive.
     * <p/>
     * Created on Feb 19, 2008 by depryf
     * @param value requested value
     * @return a collection of <code>String</code>, maybe empty but never null
     */
    Set<String> getAllByValue(String value);

    /**
     * Returns all the <code>String</code> corresponding to the given label. Comparison is case-sensitive.
     * <p/>
     * Created on Feb 19, 2008 by depryf
     * @param value requested label
     * @return a collection of <code>String</code>, maybe empty but never null
     */
    Set<String> getAllByValueWithCase(String value);

    /**
     * Returns all the values defined within this lookup.
     * <p/>
     * Created on Jul 1, 2008 by depryf
     * @return a set of values, maybe empty but never null
     */
    Set<String> getAllValues();

    /**
     * Returns true if the given key is contains in this lookup. Comparison is NOT case-sensitive.
     * <p/>
     * Created on Jul 28, 2004 by depryf
     * @param key requested key
     * @return true if the given value is contains in this lookup
     */
    boolean containsKey(Object key);

    /**
     * Returns true if the given key is contains in this lookup. Comparison is NOT case-sensitive.
     * <p/>
     * Created on Jul 28, 2004 by depryf
     * @param key requested key
     * @return true if the given value is contains in this lookup
     */
    boolean containsKeyWithCase(Object key);

    /**
     * Returns true if the given value is contains in this lookup. Comparison is NOT case-sensitive.
     * <p/>
     * Created on Jul 28, 2004 by depryf
     * @param value requested value
     * @return true if the given label is contains in this lookup
     */
    boolean containsValue(Object value);

    /**
     * Returns true if the given value is contains in this lookup. Comparison is case-sensitive.
     * <p/>
     * Created on Jul 28, 2004 by depryf
     * @param value requested value
     * @return true if the given label is contains in this lookup
     */
    boolean containsValueWithCase(Object value);

    /**
     * Returns true if this lookup contains the key-value pair. Comparison is NOT case-sensitive.
     * <p/>
     * Created on Nov 1, 2007 by rukaja
     * @param value value
     * @param key key
     * @return true if this lookup contains the key-value pair
     */
    boolean containsPair(String key, String value);

    /**
     * Returns true if this lookup contains the key-value pair. Comparison is case-sensitive.
     * <p/>
     * Created on Nov 1, 2007 by rukaja
     * @param value value
     * @param key key
     * @return true if this lookup contains the key-value pair
     */
    boolean containsPairWithCase(String key, String value);
}
