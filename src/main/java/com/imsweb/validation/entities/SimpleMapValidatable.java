/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import com.imsweb.validation.ValidationServices;
import com.imsweb.validation.internal.ExtraPropertyEntityHandlerDto;

/**
 * This simple {@link Validatable} implementation can be used to validate objects represented as maps where the keys are strings
 * and the values are objects of three types:
 * <ol>
 * <li>Simple types: Integer, String, etc...</li>
 * <li>Complex type: the value must implement the <code>Map</code> interface</li>
 * <li>Collection: the value must implement the <code>List</code> interface</li>
 * </ol>
 * The entity that needs to be validated needs to be provided to the constructor as an argument. Another important argument of the constructor
 * is the root prefix, it defines the alias to be used in the edits: if the root is "record", the edit will have to access the properties as
 * "record.property"; if the root is "tumor", the edit will have to access the properties as "tumor.property".
 * <br/><br/>
 * In it's simplest form, this validatable can be used to wrap a data line represented as a map of properties where every value is the String read
 * from the data line. In that case, the default prefix "record" will be used, and the wrapped entity shouldn't have any complex types nor collections.
 * <br/><br/>
 * Created on Apr 17, 2010 by Fabian
 */
public class SimpleMapValidatable implements Validatable {

    /**
     * The default root prefix for this validatable.
     */
    public static final String ROOT_PREFIX = "record";

    /**
     * Display ID for this validatable.
     */
    private String _displayId;

    /**
     * Used to keep track of the property paths (prefix) when an error is reported; this is the full path with the indexes.
     */
    private String _prefix;

    /**
     * The current alias.
     */
    private String _alias;

    /**
     * Current map being validated.
     */
    private Map<String, Object> _current;

    /**
     * Link to the parent validatable.
     */
    private SimpleMapValidatable _parent;

    /**
     * Map of prefixes, contains the prefixes of this validatable plus any prefixes from the parents.
     */
    private Map<String, String> _prefixes;

    /**
     * Map of scopes, contains the scope of this validatable plus any scopes from the parents.
     */
    private Map<String, Object> _scopes;

    /**
     * Set of failing properties.
     */
    private Set<String> _propertiesWithError;

    /**
     * Constructor.
     * <p/>
     * Created on Apr 17, 2010 by Fabian
     * @param map map representing the object to be validated (map of lists of maps etc..)
     */
    public SimpleMapValidatable(Map<String, Object> map) {
        this("?", ROOT_PREFIX, map, null);
    }

    /**
     * Constructor.
     * <p/>
     * Created on Apr 17, 2010 by Fabian
     * @param rootPrefix the root prefix (first element of the java path provided in the edits)
     * @param map map representing the object to be validated (map of lists of maps etc..)
     */
    public SimpleMapValidatable(String rootPrefix, Map<String, Object> map) {
        this("?", rootPrefix, map, null);
    }

    /**
     * Constructor.
     * <p/>
     * Created on Apr 17, 2010 by Fabian
     * @param displayId display ID to return for this validatable (the display ID is used in some log messages)
     * @param rootPrefix the root prefix (first element of the java path provided in the edits)
     * @param map map representing the object to be validated (map of lists of maps etc..)
     */
    public SimpleMapValidatable(String displayId, String rootPrefix, Map<String, Object> map) {
        this(displayId, rootPrefix, map, null);
    }

    /**
     * Constructor.
     * <p/>
     * Created on Apr 17, 2010 by Fabian
     * @param displayId display ID to return for this validatable (the display ID is used in some log messages)
     * @param rootPrefix the root prefix (first element of the java path provided in the edits)
     * @param map map representing the object to be validated (map of lists of maps etc..)
     * @param context map of extra context to be provided to the edit
     */
    public SimpleMapValidatable(String displayId, String rootPrefix, Map<String, Object> map, Map<String, Object> context) {
        if (map == null)
            throw new RuntimeException("map cannot be null");

        _displayId = displayId;
        _prefix = rootPrefix;
        _alias = rootPrefix;
        _current = map;
        _parent = null;
        _prefixes = Collections.singletonMap(rootPrefix, rootPrefix);
        _scopes = new HashMap<>();
        _scopes.put(rootPrefix, map);
        if (context != null)
            _scopes.putAll(context);
        _propertiesWithError = new HashSet<>();
    }

    /**
     * Constructor used internally.
     * <p/>
     * Created on Apr 17, 2010 by Fabian
     * @param prefix current prefix (full path)
     * @param map current map being validated
     */
    private SimpleMapValidatable(SimpleMapValidatable parent, String prefix, Map<String, Object> map) {
        _prefix = prefix;
        _alias = ValidationServices.getInstance().getAliasForJavaPath(prefix.replaceAll("\\[\\d+]", ""));
        _current = map;
        _parent = parent;
        _prefixes = new HashMap<>(_parent.getPrefixes());
        _prefixes.put(_alias, prefix);
        _scopes = new HashMap<>(_parent.getScope());
        _scopes.put(_alias, map);
        _propertiesWithError = new HashSet<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Validatable> followCollection(String collection) throws IllegalAccessException {
        List<Validatable> result = new ArrayList<>();

        Object val = _current.get(collection);
        if (val != null) {
            if (!(val instanceof List))
                throw new IllegalAccessException("Unable to follow '" + collection + "', value is not a list");
            List<Map<String, Object>> values = (List<Map<String, Object>>)val;
            for (int i = 0; i < values.size(); i++)
                result.add(new SimpleMapValidatable(this, _prefix + "." + collection + "[" + i + "]", values.get(i)));
        }

        return result;
    }

    @Override
    public String getDisplayId() {
        String result = _displayId;
        SimpleMapValidatable validatable = getParent();
        while (validatable != null) {
            result = validatable._displayId;
            validatable = validatable.getParent();
        }
        return result;
    }

    @Override
    public Long getCurrentTumorId() {
        return null;
    }

    @Override
    public String getRootLevel() {
        String result = getCurrentLevel();
        SimpleMapValidatable validatable = getParent();
        while (validatable != null) {
            result = validatable.getCurrentLevel();
            validatable = validatable.getParent();
        }
        return result;
    }

    @Override
    public String getCurrentLevel() {
        return _prefix;
    }

    @Override
    public Map<String, Object> getScope() {
        return _scopes;
    }

    @Override
    public void reportFailureForProperty(String propertyName) {
        if (propertyName != null) {
            int pos = propertyName.indexOf('.');
            if (pos >= 0) {
                String alias = propertyName.substring(0, pos);
                String name = propertyName.substring(pos + 1);
                String prefix = _prefixes.get(alias);
                if (prefix != null)
                    _propertiesWithError.add(prefix + "." + name);
            }
        }
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void forceFailureForProperties(Set<ExtraPropertyEntityHandlerDto> toReport, Set<String> rawProperties) {
        for (ExtraPropertyEntityHandlerDto extra : toReport) {
            Set<String> props = extra.getProperties();
            if (props == null)
                props = rawProperties;
            for (String prop : props) {
                int pos = prop.indexOf('.');
                if (pos >= 0) {
                    String javaPath = ValidationServices.getInstance().getJavaPathForAlias(prop.substring(0, pos));
                    int colPos = javaPath == null ? -1 : javaPath.lastIndexOf(".");
                    if (javaPath != null && colPos >= 0) {
                        String collectionName = javaPath.substring(colPos + 1);
                        Object collection = _current.get(collectionName);
                        if (collection != null) {
                            int index = -1;
                            if (collection instanceof List)
                                index = ((List<Object>)collection).indexOf(extra.getEntity());
                            else if (collection instanceof SortedSet)
                                index = new ArrayList<>((SortedSet<Object>)collection).indexOf(extra.getEntity());
                            if (index != -1)
                                _propertiesWithError.add(_prefix + "." + collectionName + "[" + index + "]." + prop.substring(pos + 1));
                        }
                    }
                }
            }
        }
    }

    @Override
    public Set<String> getPropertiesWithError() {
        return _propertiesWithError;
    }

    @Override
    public void clearPropertiesWithError() {
        _propertiesWithError.clear();
    }

    // helper
    private Map<String, String> getPrefixes() {
        return _prefixes;
    }

    // helper 
    private SimpleMapValidatable getParent() {
        return _parent;
    }
}
