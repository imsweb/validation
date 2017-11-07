/*
 * Copyright (C) 2011 Information Management Services, Inc.
 */
package com.imsweb.validation.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.imsweb.staging.entities.StagingSchema;
import com.imsweb.validation.ValidatorContextFunctions;
import com.imsweb.validation.ValidatorServices;
import com.imsweb.validation.functions.StagingContextFunctions;
import com.imsweb.validation.internal.ExtraPropertyEntityHandlerDto;

/**
 * This simple {@link Validatable} implementation can be used to validate NAACCR records represented as maps of strings.
 * <br/><br/>
 * The simplest constructor takes a map of a strings representing a single NAACCR record as an argument; other flavors take a list of
 * records representing a full patient set (useful to execute inter-record edits).
 * <br/><br/>
 * This class also supports the "untrimmed" notation used by the Genedits translated edits (by default edits are designed to run on trimmed values).
 * <br/><br/>
 * As a convenience, a property "_csSchemaName" will be automatically added to each data line if the context functions have been initialized with
 * the CStage implementation. The variable and set to the CStage schema name based on the following properties:
 * <ul>
 * <li>primarySite</li>
 * <li>histologyIcdO3</li>
 * <li>csSiteSpecificFactor25</li>
 * </ul>
 * The property will be added if it's not there yet, or if either one of the site/hist properties exists. In other words, if you want to
 * to populate the "_csSchemaName" yourself on the line (which can be useful for unit tests), make sure to not also provide a site/hist.
 * or the schema will be overridden.
 * <br/><br/>
 * Created on Apr 17, 2010 by Fabian
 */
public class SimpleNaaccrLinesValidatable implements Validatable {

    /**
     * The root prefix for NAACCR lines.
     */
    public static final String ROOT_PREFIX = "lines";

    /**
     * The root prefix for untrimmed NAACCR lines.
     */
    public static final String ROOT_PREFIX_UNTRIMMED = "untrimmedlines";

    /**
     * Used to keep track of the property paths (prefix) when an error is reported; this is the full path with the indexes
     */
    private String _prefix;

    /**
     * The current alias
     */
    private String _alias;

    /**
     * Current collections of lines (applied only to the root validatable)
     */
    private List<Map<String, String>> _lines;

    /**
     * Context entries
     */
    private Map<String, Object> _context;

    /**
     * Current map being validated
     */
    private Map<String, String> _currentLine;

    /**
     * Link to the parent validatable
     */
    private SimpleNaaccrLinesValidatable _parent;

    /**
     * Map of prefixes, contains the prefixes of this validatable plus any prefixes from the parents
     */
    private Map<String, String> _prefixes;

    /**
     * Map of scopes, contains the scope of this validatable plus any scopes from the parents
     */
    private Map<String, Object> _scopes;

    /**
     * Set of failing properties
     */
    private Set<String> _propertiesWithError;

    /**
     * If true, the untrimmed notation will be used ('untrimmedline' instead of 'line')
     */
    private boolean _useUntrimmedNotation;

    /**
     * Constructor.
     * <p/>
     * Created on Aug 7, 2011 by depryf
     * @param map entity (a map representing one  NAACCR record)
     */
    public SimpleNaaccrLinesValidatable(Map<String, String> map) {
        this(Collections.singletonList(map), null);
    }

    /**
     * Constructor.
     * <p/>
     * Created on Aug 7, 2011 by depryf
     * @param list wrapped entity (a list of map representing one or several NAACCR records for one patient set)
     */
    public SimpleNaaccrLinesValidatable(List<Map<String, String>> list) {
        this(list, null);
    }

    /**
     * Constructor.
     * <p/>
     * Created on Aug 7, 2011 by depryf
     * @param list wrapped entity (a list of map representing one or several NAACCR records for one patient set)
     * @param context optional context to be provided to the executed rule
     */
    public SimpleNaaccrLinesValidatable(List<Map<String, String>> list, Map<String, Object> context) {
        this(list, context, false);
    }

    /**
     * Constructor.
     * <p/>
     * Created on Aug 7, 2011 by depryf
     * @param list wrapped entity (a list of map representing one or several NAACCR records for one patient set)
     * @param context optional context to be provided to the executed rule
     * @param useUntrimmedNotation if true, the untrimmed notation will be used ('untrimmedline' instead of 'line'); defaults to false
     */
    public SimpleNaaccrLinesValidatable(List<Map<String, String>> list, Map<String, Object> context, boolean useUntrimmedNotation) {
        if (list == null)
            throw new RuntimeException("wrapped entity cannot be null");

        String rootPrefix = useUntrimmedNotation ? ROOT_PREFIX_UNTRIMMED : ROOT_PREFIX;

        _prefix = rootPrefix;
        _alias = rootPrefix;
        _lines = list;
        _currentLine = null;
        _parent = null;
        _prefixes = Collections.singletonMap(rootPrefix, rootPrefix);
        _scopes = new HashMap<>(Collections.singletonMap(rootPrefix, (Object)list));
        _propertiesWithError = new HashSet<>();
        _context = context;
        _useUntrimmedNotation = useUntrimmedNotation;
    }

    /**
     * Constructor used internally.
     * <p/>
     * @param parent parent validatable
     * @param prefix current path prefix
     * @param map current line
     * @param useUntrimmedNotation whether the values should be untrimmed
     */
    private SimpleNaaccrLinesValidatable(SimpleNaaccrLinesValidatable parent, String prefix, Map<String, String> map, Map<String, Object> context, boolean useUntrimmedNotation) {
        _prefix = prefix;
        _alias = ValidatorServices.getInstance().getAliasForJavaPath(prefix.replaceAll("\\[\\d+\\]", ""));
        _lines = null;
        _currentLine = map;
        _parent = parent;
        _prefixes = new HashMap<>(_parent.getPrefixes());
        _prefixes.put(_alias, prefix);
        _scopes = new HashMap<>(_parent.getScope());
        _scopes.put(_alias, map);
        _propertiesWithError = new HashSet<>();
        _context = context;
        _useUntrimmedNotation = useUntrimmedNotation;

        // this used to be in a protected method, but we are in a private constructor; pointless really, might as well put the code here!
        if (ValidatorContextFunctions.isInitialized() && ValidatorContextFunctions.getInstance() instanceof StagingContextFunctions) {
            boolean hasCsSchemaId = _currentLine.containsKey(Validatable.KEY_CS_SCHEMA_ID);
            boolean hasTnmSchemaId = _currentLine.containsKey(Validatable.KEY_TNM_SCHEMA_ID);
            boolean hasSite = _currentLine.containsKey(StagingContextFunctions.CSTAGE_INPUT_PROP_SITE);
            boolean hasHist = _currentLine.containsKey(StagingContextFunctions.CSTAGE_INPUT_PROP_HIST);

            // set TNM schema
            if (!hasTnmSchemaId || hasSite || hasHist) {
                StagingSchema schema = ((StagingContextFunctions)ValidatorContextFunctions.getInstance()).getTnmStagingSchema(_currentLine);
                _currentLine.put(Validatable.KEY_TNM_SCHEMA_ID, schema != null ? schema.getId() : null);
            }

            // set CS schema
            if (!hasCsSchemaId || hasSite || hasHist) {
                StagingSchema schema = ((StagingContextFunctions)ValidatorContextFunctions.getInstance()).getCsStagingSchema(_currentLine);
                _currentLine.put(Validatable.KEY_CS_SCHEMA_ID, schema != null ? schema.getId() : null);
            }
        }
    }

    @Override
    public List<Validatable> followCollection(String collection) throws IllegalAccessException {
        List<Validatable> result = new ArrayList<>();

        if ((!"line".equals(collection) && !"untrimmedline".equals(collection)) || _lines == null)
            throw new IllegalAccessException("This validatable can only work with a single collection called 'line' or 'untrimmedline'");

        for (int i = 0; i < _lines.size(); i++)
            result.add(new SimpleNaaccrLinesValidatable(this, _prefix + "." + collection + "[" + i + "]", _lines.get(i), _context, _useUntrimmedNotation));

        return result;
    }

    @Override
    public String getDisplayId() {
        String result = "?";

        // use the patient ID number and the tumor record #
        if (_currentLine != null) {
            result = _currentLine.get("patientIdNumber");
            if (result != null) {
                String tumRecNum = _currentLine.get("tumorRecordNumber");
                if (tumRecNum != null)
                    result = result + "/" + tumRecNum;
            }
        }

        return result == null ? "?" : result;
    }

    @Override
    public Long getCurrentTumorId() {
        return null;
    }

    @Override
    public String getRootLevel() {
        String result = getCurrentLevel();
        SimpleNaaccrLinesValidatable validatable = getParent();
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
        if (_context != null)
            for (Entry<String, Object> entry : _context.entrySet())
                _scopes.put(entry.getKey(), entry.getValue());

        return _scopes;
    }

    @Override
    public void reportFailureForProperty(String propertyName) throws IllegalAccessException {
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
    public void forceFailureForProperties(Set<ExtraPropertyEntityHandlerDto> toReport, Set<String> rawProperties) throws IllegalAccessException {
        if (_lines == null)
            return;

        for (ExtraPropertyEntityHandlerDto extra : toReport) {
            Set<String> props = extra.getProperties();
            if (props == null)
                props = rawProperties;
            int idx = !(extra.getEntity() instanceof Map) ? -1 : _lines.indexOf(extra.getEntity());
            if (idx == -1)
                continue;

            for (String prop : props) {
                if (_useUntrimmedNotation)
                    _propertiesWithError.add("untrimmedlines.untrimmedline[" + idx + "]." + prop.replace("untrimmedline.", ""));
                else
                    _propertiesWithError.add("lines.line[" + idx + "]." + prop.replace("line.", ""));
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
    private SimpleNaaccrLinesValidatable getParent() {
        return _parent;
    }
}
