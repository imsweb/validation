/*
 * Copyright (C) 2004 Information Management Services, Inc.
 */
package com.imsweb.validation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.apache.commons.lang3.StringUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;
import com.thoughtworks.xstream.converters.basic.BooleanConverter;
import com.thoughtworks.xstream.converters.basic.ByteConverter;
import com.thoughtworks.xstream.converters.basic.DateConverter;
import com.thoughtworks.xstream.converters.basic.DoubleConverter;
import com.thoughtworks.xstream.converters.basic.FloatConverter;
import com.thoughtworks.xstream.converters.basic.IntConverter;
import com.thoughtworks.xstream.converters.basic.LongConverter;
import com.thoughtworks.xstream.converters.basic.NullConverter;
import com.thoughtworks.xstream.converters.basic.ShortConverter;
import com.thoughtworks.xstream.converters.basic.StringConverter;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.WildcardTypePermission;

import com.imsweb.validation.entities.Category;
import com.imsweb.validation.entities.Condition;
import com.imsweb.validation.entities.ContextEntry;
import com.imsweb.validation.entities.DeletedRuleHistory;
import com.imsweb.validation.entities.EmbeddedSet;
import com.imsweb.validation.entities.Rule;
import com.imsweb.validation.entities.RuleHistory;
import com.imsweb.validation.entities.RuleTest;
import com.imsweb.validation.entities.StandaloneSet;
import com.imsweb.validation.entities.Validator;
import com.imsweb.validation.entities.ValidatorRelease;
import com.imsweb.validation.entities.ValidatorTests;
import com.imsweb.validation.entities.ValidatorVersion;
import com.imsweb.validation.entities.xml.CategoryXmlDto;
import com.imsweb.validation.entities.xml.ConditionXmlDto;
import com.imsweb.validation.entities.xml.ContextEntryXmlDto;
import com.imsweb.validation.entities.xml.DeletedRuleXmlDto;
import com.imsweb.validation.entities.xml.HistoryEventXmlDto;
import com.imsweb.validation.entities.xml.ReleaseXmlDto;
import com.imsweb.validation.entities.xml.RuleXmlDto;
import com.imsweb.validation.entities.xml.SetXmlDto;
import com.imsweb.validation.entities.xml.StandaloneSetValidatorXmlDto;
import com.imsweb.validation.entities.xml.StandaloneSetXmlDto;
import com.imsweb.validation.entities.xml.TestXmlDto;
import com.imsweb.validation.entities.xml.TestedValidatorXmlDto;
import com.imsweb.validation.entities.xml.ValidatorXmlDto;
import com.imsweb.validation.internal.callable.RuleParsingCallable;
import com.imsweb.validation.internal.xml.StandaloneSetXmlDriver;
import com.imsweb.validation.internal.xml.TestsXmlDriver;
import com.imsweb.validation.internal.xml.ValidatorXmlDriver;
import com.imsweb.validation.runtime.ParsedContexts;
import com.imsweb.validation.runtime.ParsedLookups;
import com.imsweb.validation.runtime.ParsedProperties;
import com.imsweb.validation.runtime.RuntimeEdits;

import static com.imsweb.validation.ValidationEngine.CONTEXT_TYPE_GROOVY;
import static com.imsweb.validation.ValidationEngine.CONTEXT_TYPE_JAVA;
import static com.imsweb.validation.ValidationEngine.CONTEXT_TYPE_TABLE;
import static com.imsweb.validation.ValidationEngine.CONTEXT_TYPE_TABLE_INDEX_DEF;

/**
 * This class is responsible for reading and writing XML files containing edits definitions.
 * <p/>
 * Created on Apr 26, 2011 by depryf
 */
public final class ValidationXmlUtils {

    /**
     * The attributes of the root tag
     */
    public static final String ROOT_ATTR_ID = "id";
    public static final String ROOT_ATTR_NAME = "name";
    public static final String ROOT_ATTR_VERSION = "version";
    public static final String ROOT_ATTR_MIN_ENGINE_VERSION = "min-engine-version";
    public static final String ROOT_ATTR_TRANSLATED_FROM = "translated-from";

    /**
     * Compiled <code>Pattern</code> for tab characters
     */
    private static final Pattern _PATTERN_TAB = Pattern.compile("\\t");

    /**
     * Compiled <code>Pattern</code> for non-printable characters
     */
    private static final Pattern _CONTROL_CHARACTERS_PATTERN = Pattern.compile("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]");

    /**
     * Compiled <code>Pattern</code> for sorting the rule by ID
     */
    private static final Pattern _PATTERN_RULE_ID = Pattern.compile("^(\\D*+)(\\d++)(.*+)$");

    /**
     * Whether or not the expressions, descriptions, messages, etc... should be re-aligned (disabled by default).
     */
    private static boolean _REALIGNMENT_ENABLED = false;

    /**
     * Private constructor, no instanciation.
     * <p/>
     * Created on Apr 19, 2010 by depryf
     */
    private ValidationXmlUtils() {
    }

    // ****************************************************************************************************
    //                                     VALIDATOR METHODS
    // ****************************************************************************************************

    /**
     * Creates an instance of XStream for reading and writing validator objects.
     * @return an instance of XStream, never null
     */
    private static XStream createValidatorXStream() {
        XStream xstream = new XStream(new ValidatorXmlDriver()) {
            // only register the converters we need; other converters generate a private access warning in the console on Java9+...
            @Override
            protected void setupConverters() {
                registerConverter(new NullConverter(), PRIORITY_VERY_HIGH);
                registerConverter(new IntConverter(), PRIORITY_NORMAL);
                registerConverter(new FloatConverter(), PRIORITY_NORMAL);
                registerConverter(new DoubleConverter(), PRIORITY_NORMAL);
                registerConverter(new LongConverter(), PRIORITY_NORMAL);
                registerConverter(new ShortConverter(), PRIORITY_NORMAL);
                registerConverter(new BooleanConverter(), PRIORITY_NORMAL);
                registerConverter(new ByteConverter(), PRIORITY_NORMAL);
                registerConverter(new StringConverter(), PRIORITY_NORMAL);
                registerConverter(new DateConverter(), PRIORITY_NORMAL);
                registerConverter(new CollectionConverter(getMapper()), PRIORITY_NORMAL);
                registerConverter(new ReflectionConverter(getMapper(), getReflectionProvider()), PRIORITY_VERY_LOW);
            }
        };
        xstream.autodetectAnnotations(true);
        xstream.alias("validator", ValidatorXmlDto.class);
        xstream.registerConverter(new AbstractSingleValueConverter() {
            @Override
            public boolean canConvert(Class type) {
                return type.equals(Date.class);
            }

            @Override
            public Object fromString(String str) {
                try {
                    return new SimpleDateFormat("yyyy-MM-dd").parse(str);
                }
                catch (ParseException e) {
                    throw new ConversionException("Cannot parse date " + str);
                }
            }

            @Override
            public String toString(Object obj) {
                return new SimpleDateFormat("yyyy-MM-dd").format((Date)obj);
            }
        });

        // setup proper security by limiting what classes can be loaded by XStream
        xstream.addPermission(NoTypePermission.NONE);
        xstream.addPermission(new WildcardTypePermission(new String[] {"com.imsweb.validation.**"}));

        return xstream;
    }

    /**
     * Creates a new <code>Validator</code> object by reading the passed XML file.
     * <br/><br/>
     * If the filename ends with 'gz', a compressed file will be assumed; zipped files
     * are not supported (that doesn't mean they cannot be handled, it just means the caller has
     * to provide a stream to the zip entry).
     * <p/>
     * Created on Feb 5, 2008 by depryf
     * @param file <code>File</code> to XML file to load (cannot be null, must exist)
     * @return a new <code>Validator</code>
     * @throws IOException if unable to properly read/write the entity
     */
    public static Validator loadValidatorFromXml(File file) throws IOException {
        return loadValidatorFromXml(file, null);
    }

    /**
     * Creates a new <code>Validator</code> object by reading the passed XML file.
     * <br/><br/>
     * If the filename ends with 'gz', a compressed file will be assumed; zipped files
     * are not supported (that doesn't mean they cannot be handled, it just means the caller has
     * to provide a stream to the zip entry).
     * <p/>
     * Created on Feb 5, 2008 by depryf
     * @param file <code>File</code> to XML file to load (cannot be null, must exist)
     * @param runtime <code>RuntimeEdits</code> optional pre-parsed classes
     * @return a new <code>Validator</code>
     * @throws IOException if unable to properly read/write the entity
     */
    public static Validator loadValidatorFromXml(File file, RuntimeEdits runtime) throws IOException {
        if (file == null)
            throw new IOException("Unable to load validator, target file is null");
        if (!file.exists())
            throw new IOException("Unable to load validator, target file doesn't exist");

        try (InputStream is = file.getName().toLowerCase().endsWith(".gz") ? new GZIPInputStream(Files.newInputStream(file.toPath())) : Files.newInputStream(file.toPath())) {
            return loadValidatorFromXml(is, runtime);
        }
    }

    /**
     * Creates a new <code>Validator</code> object by reading the passed <code>URL</code> to an XML file.
     * <br/><br/>
     * This method supports a gzipped compressed resource (if the URL path ends with gz or gzip); otherwise
     * it assumes the resource is not compressed.
     * <p/>
     * Created on Feb 5, 2008 by depryf
     * @param url <code>URL</code> to XML file to load (if null or if a stream cannot be opened from it, an exception will be raised)
     * @return a new <code>Validator</code>
     * @throws IOException if unable to properly read/write the entity
     */
    public static Validator loadValidatorFromXml(URL url) throws IOException {
        return loadValidatorFromXml(url, null);
    }

    /**
     * Creates a new <code>Validator</code> object by reading the passed <code>URL</code> to an XML file.
     * <br/><br/>
     * This method supports a gzipped compressed resource (if the URL path ends with gz or gzip); otherwise
     * it assumes the resource is not compressed.
     * <p/>
     * Created on Feb 5, 2008 by depryf
     * @param url <code>URL</code> to XML file to load (if null or if a stream cannot be opened from it, an exception will be raised)
     * @param runtime <code>RuntimeEdits</code> optional pre-parsed classes
     * @return a new <code>Validator</code>
     * @throws IOException if unable to properly read/write the entity
     */
    public static Validator loadValidatorFromXml(URL url, RuntimeEdits runtime) throws IOException {
        if (url == null)
            throw new IOException("Unable to load validator, target URL is null");

        try (InputStream is = url.getPath().toLowerCase().endsWith(".gz") ? new GZIPInputStream(url.openStream()) : url.openStream()) {
            return loadValidatorFromXml(is, runtime);
        }
    }

    /**
     * Creates a new <code>Validator</code> object by reading the provided input stream.
     * <br/><br/>
     * The passed stream will NOT be closed when this method returns.
     * <br/><br/>
     * This methods makes no assumptions on the compression of the stream.
     * <p/>
     * Created on Feb 5, 2008 by depryf
     * @param is <code>InputStream</code> to validator file to load (if null an exception will be raised)
     * @return a new <code>Validator</code>
     * @throws IOException if unable to properly read/write the entity
     */
    public static Validator loadValidatorFromXml(InputStream is) throws IOException {
        return loadValidatorFromXml(is, null);
    }

    /**
     * Creates a new <code>Validator</code> object by reading the provided input stream.
     * <br/><br/>
     * The passed stream will NOT be closed when this method returns.
     * <br/><br/>
     * This methods makes no assumptions on the compression of the stream.
     * <p/>
     * Created on Feb 5, 2008 by depryf
     * @param is <code>InputStream</code> to validator file to load (if null an exception will be raised)
     * @param runtime <code>RuntimeEdits</code> optional pre-parsed classes
     * @return a new <code>Validator</code>
     * @throws IOException if unable to properly read/write the entity
     */
    public static Validator loadValidatorFromXml(InputStream is, RuntimeEdits runtime) throws IOException {
        if (is == null)
            throw new IOException("Unable to load validator, target input stream is null");

        try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            return loadValidatorFromXml(reader, runtime);
        }
    }

    /**
     * Creates a new <code>Validator</code> object by reading the passed XML.
     * <p/>
     * The passed reader will NOT be closed when this method returns.
     * <p/>
     * Created on Feb 5, 2008 by depryf
     * @param reader <code>Reader</code> to XML (if null an exception will be raised)
     * @return a new <code>Validator</code>
     * @throws IOException if unable to properly read/write the entity
     */
    public static Validator loadValidatorFromXml(Reader reader) throws IOException {
        return loadValidatorFromXml(reader, null);
    }

    /**
     * Creates a new <code>Validator</code> object by reading the passed XML.
     * <p/>
     * The passed reader will NOT be closed when this method returns.
     * <p/>
     * Created on Feb 5, 2008 by depryf
     * @param reader <code>Reader</code> to XML (if null an exception will be raised)
     * @param runtime <code>RuntimeEdits</code> optional pre-parsed classes
     * @return a new <code>Validator</code>
     * @throws IOException if unable to properly read/write the entity
     */
    public static Validator loadValidatorFromXml(Reader reader, RuntimeEdits runtime) throws IOException {
        if (reader == null)
            throw new IOException("Unable to load validator, target reader is null");

        try {
            ValidatorXmlDto validatorType = (ValidatorXmlDto)createValidatorXStream().fromXML(reader);

            Validator validator = new Validator();
            validator.setValidatorId(ValidationServices.getInstance().getNextValidatorSequence());
            if (validatorType.getId() == null)
                throw new IOException("Validator ID is required");
            validator.setId(validatorType.getId());
            validator.setName(validatorType.getName());
            validator.setVersion(validatorType.getVersion());
            validator.setMinEngineVersion(validatorType.getMinEngineVersion());
            validator.setTranslatedFrom(validatorType.getTranslatedFrom());
            readValidatorReleases(validator, validatorType.getReleases());
            readValidatorDeletedRuleHistories(validator, validatorType.getDeletedRules());
            readValidatorContext(validator, validatorType.getContextEntries());
            readValidatorCategories(validator, validatorType.getCategories());
            readValidatorConditions(validator, validatorType.getConditions());
            readValidatorRules(validator, validatorType.getRules(), runtime);
            readValidatorSets(validator, validatorType.getSets());

            // and finally calculate the inverted dependencies - this requires two passes over the edits; maybe somebody smarter will make it faster ;-)
            Map<String, Set<String>> invertedDependencies = new HashMap<>();
            for (Rule r : validator.getRules()) {
                for (String id : r.getDependencies())
                    invertedDependencies.computeIfAbsent(id, k -> new HashSet<>()).add(r.getId());
            }
            for (Rule r : validator.getRules())
                r.setInvertedDependencies(invertedDependencies.get(r.getId()));

            if (runtime != null)
                validator.setCompiledRules(runtime.getCompiledRules());

            return validator;
        }
        catch (RuntimeException e) {
            throw new IOException("Unable to construct new validator instance", e);
        }
    }

    /**
     * Writes the passed <code>Validator</code> object to the passed file.
     * <p/>
     * Created on Nov 29, 2009 by depryf
     * @param validator validator to write
     * @param file <code>File</code> where the validator will be written (parent folder must exists)
     * @throws IOException if unable to properly read/write the entity
     */
    public static void writeValidatorToXml(Validator validator, File file) throws IOException {
        if (file == null)
            throw new IOException("Unable to write validator, target file is null");

        try (OutputStream os = file.getName().toLowerCase().endsWith(".gz") ? new GZIPOutputStream(Files.newOutputStream(file.toPath())) : Files.newOutputStream(file.toPath())) {
            writeValidatorToXml(validator, os);
        }
    }

    /**
     * Writes the passed <code>Validator</code> object to the passed XML stream.
     * <p/>
     * The passed stream will NOT be closed when this method returns.
     * <p/>
     * Created on Nov 29, 2009 by depryf
     * @param validator validator to write
     * @param os <code>OutputStream</code> to XML (if null an exception will be raised)
     * @throws IOException if unable to properly read/write the entity
     */
    public static void writeValidatorToXml(Validator validator, OutputStream os) throws IOException {
        if (os == null)
            throw new IOException("Unable to write validator '" + validator.getId() + "', target output stream is null");

        try (OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
            writeValidatorToXml(validator, writer);
        }
    }

    /**
     * Writes the passed <code>Validator</code> object to the passed XML writer.
     * <p/>
     * The passed writer will NOT be closed when this method returns.
     * <p/>
     * Created on Nov 29, 2009 by depryf
     * @param validator validator to write
     * @param writer <code>Writer</code> to XML (if null an exception will be raised)
     * @throws IOException if unable to properly read/write the entity
     */
    public static void writeValidatorToXml(Validator validator, Writer writer) throws IOException {
        if (validator == null)
            throw new IOException("Unable to write NULL validator");
        if (writer == null)
            throw new IOException("Unable to write validator '" + validator.getId() + "', target writer is null");

        try {
            ValidatorXmlDto validatorType = new ValidatorXmlDto();
            validatorType.setId(validator.getId());
            validatorType.setName(validator.getName());
            validatorType.setVersion(validator.getVersion());
            validatorType.setMinEngineVersion(validator.getMinEngineVersion());
            validatorType.setTranslatedFrom(validator.getTranslatedFrom());
            validatorType.setReleases(writeValidatorReleases(validator));
            validatorType.setDeletedRules(writeValidatorDeletedRuleHistories(validator));
            validatorType.setContextEntries(writeValidatorContext(validator));
            validatorType.setCategories(writeValidatorCategories(validator));
            validatorType.setConditions(writeValidatorConditions(validator));
            validatorType.setRules(writeValidatorRules(validator));
            validatorType.setSets(writeValidatorSets(validator));

            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.write(System.lineSeparator());
            createValidatorXStream().toXML(validatorType, writer);
        }
        catch (RuntimeException e) {
            throw new IOException("Unable to write validator", e);
        }
    }

    private static void readValidatorReleases(Validator validator, List<ReleaseXmlDto> releasesType) throws IOException {
        if (releasesType != null && !releasesType.isEmpty()) {
            for (ReleaseXmlDto release : releasesType) {
                if (release.getVersion() == null)
                    throw new IOException("Release version is required");
                if (release.getDate() == null)
                    throw new IOException("Release date is required");
                validator.getReleases().add(new ValidatorRelease(new ValidatorVersion(release.getVersion()), release.getDate(), release.getDesc()));
            }
        }
    }

    private static void readValidatorDeletedRuleHistories(Validator validator, List<DeletedRuleXmlDto> deletedRulesType) throws IOException {
        Set<DeletedRuleHistory> histories = new HashSet<>();

        if (deletedRulesType != null && !deletedRulesType.isEmpty()) {

            // create a map of raw version -> version object
            Map<String, ValidatorVersion> versions = new HashMap<>();
            if (validator.getReleases() != null)
                for (ValidatorRelease release : validator.getReleases())
                    versions.put(release.getVersion().getRawString(), release.getVersion());

            for (DeletedRuleXmlDto event : deletedRulesType) {
                DeletedRuleHistory rh = new DeletedRuleHistory();
                rh.setRuleHistoryId(ValidationServices.getInstance().getNextRuleHistorySequence());
                if (event.getId() == null)
                    throw new IOException("Deleted rule ID is required");
                rh.setDeletedRuleId(event.getId());
                rh.setDeletedRuleName(event.getName());
                rh.setValidator(validator);
                if (event.getVersion() != null) {
                    ValidatorVersion version = versions.get(event.getVersion());
                    if (version == null)
                        throw new IOException("Deleted Rule '" + event.getId() + "' in validator '" + validator.getId() + "' defines a bad version: " + event.getVersion());
                    rh.setVersion(version);
                }
                if (event.getUser() == null)
                    throw new IOException("Deleted rule user is required");
                rh.setUsername(event.getUser().trim());
                if (event.getDate() == null)
                    throw new IOException("Deleted rule date is required");
                rh.setDate(event.getDate());
                rh.setReference(event.getRef());
                rh.setReplacedBy(event.getReplacedBy());
                rh.setMessage(ValidationXmlUtils.trimEmptyLines(event.getValue(), true));
                histories.add(rh);
            }
        }

        validator.setDeletedRuleHistories(histories);
    }

    private static void readValidatorContext(Validator validator, List<ContextEntryXmlDto> contextEntries) throws IOException {
        Set<ContextEntry> rawContext = new HashSet<>();

        if (contextEntries != null && !contextEntries.isEmpty()) {
            for (ContextEntryXmlDto entryType : contextEntries) {
                ContextEntry entry = new ContextEntry();
                entry.setContextEntryId(ValidationServices.getInstance().getNextContextEntrySequence());
                entry.setValidator(validator);
                if (entryType.getId() == null)
                    throw new IOException("Context entry ID is required");
                entry.setKey(entryType.getId());
                String contextType = entryType.getType() == null ? CONTEXT_TYPE_GROOVY : entryType.getType();
                List<String> allowed = Arrays.asList(CONTEXT_TYPE_GROOVY, CONTEXT_TYPE_JAVA, CONTEXT_TYPE_TABLE, CONTEXT_TYPE_TABLE_INDEX_DEF);
                if (!allowed.contains(contextType))
                    throw new IOException("Unable to load context '" + entryType.getId() + "' in " + validator.getId() + "; type must be in " + allowed);
                entry.setType(contextType);
                entry.setExpression(reAlign(entryType.getValue()));
                rawContext.add(entry);
            }
        }

        validator.setRawContext(rawContext);
    }

    private static void readValidatorCategories(Validator validator, List<CategoryXmlDto> categoriesType) throws IOException {
        Set<Category> categories = new HashSet<>();

        if (categoriesType != null && !categoriesType.isEmpty()) {

            // go over the categories
            Set<String> usedIds = new HashSet<>();
            for (CategoryXmlDto type : categoriesType) {

                if (type.getId() == null)
                    throw new IOException("Category ID is required");

                // make sure category ID has not been used yet
                if (!usedIds.contains(type.getId()))
                    usedIds.add(type.getId());
                else
                    throw new IOException("Category '" + type.getId() + "' is defined more than once");

                // create new object
                Category category = new Category();
                category.setCategoryId(ValidationServices.getInstance().getNextCategorySequence());

                // copy properties
                category.setId(type.getId().trim());
                category.setValidator(validator);
                if (type.getName() != null)
                    category.setName(type.getName().trim());
                if (type.getDescription() != null)
                    category.setDescription(reAlign(type.getDescription()));

                categories.add(category);
            }
        }

        validator.setCategories(categories);
    }

    private static void readValidatorConditions(Validator validator, List<ConditionXmlDto> conditionsType) throws IOException {
        Set<Condition> conditions = new HashSet<>();

        if (conditionsType != null && !conditionsType.isEmpty()) {

            // go over the conditions
            Set<String> usedIds = new HashSet<>();
            for (ConditionXmlDto type : conditionsType) {

                if (type.getId() == null)
                    throw new IOException("Condition ID is required");

                // make sure condition ID has not been used yet
                if (!usedIds.contains(type.getId()))
                    usedIds.add(type.getId());
                else
                    throw new IOException("Condition '" + type.getId() + "' is defined more than once");

                // create the object
                Condition condition = new Condition();
                condition.setConditionId(ValidationServices.getInstance().getNextConditionSequence());

                // copy the properties
                condition.setId(type.getId().trim());
                condition.setValidator(validator);
                if (type.getName() != null)
                    condition.setName(type.getName().trim());
                condition.setJavaPath(type.getJavaPath().trim());
                try {
                    condition.setExpression(reAlign(type.getExpression()));
                }
                catch (ConstructionException e) {
                    throw new IOException("Unable to load condition '" + condition.getId() + "'; it contain an invalid expression", e);
                }
                if (type.getDescription() != null)
                    condition.setDescription(reAlign(type.getDescription()));

                conditions.add(condition);
            }
        }

        validator.setConditions(conditions);
    }

    private static void readValidatorRules(Validator validator, List<RuleXmlDto> rulesType, RuntimeEdits runtime) throws IOException {
        Map<String, Rule> rules = new ConcurrentHashMap<>();

        if (rulesType != null && !rulesType.isEmpty()) {

            // create a map of raw version -> version object
            Map<String, ValidatorVersion> versions = new HashMap<>();
            if (validator.getReleases() != null)
                for (ValidatorRelease release : validator.getReleases())
                    versions.put(release.getVersion().getRawString(), release.getVersion());

            // get pre-parsed information
            ParsedProperties props = null;
            ParsedContexts contexts = null;
            ParsedLookups lookups = null;
            if (runtime != null) {
                props = runtime.getParsedProperties();
                contexts = runtime.getParsedContexts();
                lookups = runtime.getParsedLookups();
            }

            // go through each rule (we multi-thread this part since it can be a bit slow
            try (ExecutorService service = Executors.newFixedThreadPool(2)) {
                List<Future<Void>> results = new ArrayList<>(rulesType.size());
                for (RuleXmlDto type : rulesType) {

                    // make sure rule ID has not been used yet
                    if (rules.containsKey(type.getId()))
                        throw new IOException("Edit '" + type.getId() + "' defined more than once in group " + validator.getId());

                    results.add(service.submit(new RuleParsingCallable(type, ValidationServices.getInstance().getNextRuleSequence(), validator, versions, rules, props, contexts, lookups)));
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
                        if (e.getCause() instanceof IOException)
                            throw (IOException)e.getCause();
                        throw new IllegalStateException(e);
                    }
                }

                // the work should be done by now because we call get(), which is a blocking call; but better safe than sorry...
                try {
                    if (!service.awaitTermination(5, TimeUnit.MINUTES))
                        throw new IllegalStateException("Edits compilation took too long to complete");
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        validator.setRules(new HashSet<>(rules.values()));
    }

    private static void readValidatorSets(Validator validator, List<SetXmlDto> setsType) throws IOException {
        Set<EmbeddedSet> sets = new HashSet<>();

        if (setsType != null && !setsType.isEmpty()) {

            // go over the sets
            Set<String> usedIds = new HashSet<>();
            for (SetXmlDto type : setsType) {

                if (type.getId() == null)
                    throw new IOException("Set ID is required");

                // make sure set ID has not been used yet
                if (!usedIds.contains(type.getId()))
                    usedIds.add(type.getId());
                else
                    throw new IOException("Set '" + type.getId() + "' defined more than once in " + validator.getId());

                // create new object
                EmbeddedSet set = new EmbeddedSet();
                set.setSetId(ValidationServices.getInstance().getNextSetSequence());

                // copy properties
                set.setId(type.getId().trim());
                set.setValidator(validator);
                if (type.getName() != null)
                    set.setName(type.getName().trim());
                set.setTag(type.getTag());
                if (type.getDescription() != null)
                    set.setDescription(reAlign(type.getDescription()));

                String include = type.getInclude();
                Set<String> inclusions = new HashSet<>();
                if (include != null && !include.trim().isEmpty())
                    for (String s : StringUtils.split(include, ','))
                        inclusions.add(s.trim());
                set.setInclusions(inclusions);

                String exclude = type.getExclude();
                Set<String> exclusions = new HashSet<>();
                if (exclude != null && !exclude.trim().isEmpty())
                    for (String s : StringUtils.split(exclude, ','))
                        exclusions.add(s.trim());
                set.setExclusions(exclusions);

                sets.add(set);
            }
        }

        validator.setSets(sets);
    }

    private static List<ReleaseXmlDto> writeValidatorReleases(Validator validator) {
        if (validator.getReleases() == null || validator.getReleases().isEmpty())
            return null;

        List<ReleaseXmlDto> releases = new ArrayList<>();
        for (ValidatorRelease r : validator.getReleases()) {
            ReleaseXmlDto release = new ReleaseXmlDto();
            release.setVersion(r.getVersion().getRawString());
            release.setDate(r.getDate());
            release.setDesc(r.getDescription());
            releases.add(release);
        }

        // sort the releases so the most recent is always first in the file...
        releases.sort((o1, o2) -> {
            if (o1.getDate() == null)
                return -1;
            else if (o2.getDate() == null)
                return 1;
            else
                return -1 * o1.getDate().compareTo(o2.getDate());
        });

        return releases;
    }

    private static List<DeletedRuleXmlDto> writeValidatorDeletedRuleHistories(Validator validator) {
        if (validator.getDeletedRuleHistories() == null || validator.getDeletedRuleHistories().isEmpty())
            return null;

        List<DeletedRuleXmlDto> deletedRules = new ArrayList<>();

        for (DeletedRuleHistory history : validator.getDeletedRuleHistories()) {
            DeletedRuleXmlDto eventType = new DeletedRuleXmlDto();
            eventType.setId(history.getDeletedRuleId());
            eventType.setName(history.getDeletedRuleName());
            if (history.getVersion() != null)
                eventType.setVersion(history.getVersion().getRawString());
            eventType.setUser(history.getUsername());
            eventType.setValue(history.getMessage());
            eventType.setDate(history.getDate());
            eventType.setRef(history.getReference());
            eventType.setReplacedBy(history.getReplacedBy());
            deletedRules.add(eventType);
        }

        // sort the event by date/ref/id
        deletedRules.sort((o1, o2) -> {
            Calendar c1 = Calendar.getInstance();
            c1.setTime(o1.getDate());
            Calendar c2 = Calendar.getInstance();
            c2.setTime(o2.getDate());
            if (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH) && c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH)) {
                if (o1.getRef() != null && o2.getRef() != null && !o1.getRef().equals(o2.getRef()))
                    return o1.getRef().compareTo(o2.getRef());
                return o1.getId().compareTo(o2.getId());
            }
            return o1.getDate().compareTo(o2.getDate());
        });

        return deletedRules;
    }

    private static List<ContextEntryXmlDto> writeValidatorContext(Validator validator) {
        if (validator.getRawContext() == null || validator.getRawContext().isEmpty())
            return null;

        // sort the context entries by their key
        List<ContextEntry> list = new ArrayList<>(validator.getRawContext());
        list.sort((o1, o2) -> o1.getKey().compareToIgnoreCase(o2.getKey()));

        List<ContextEntryXmlDto> contextEntries = new ArrayList<>();
        for (ContextEntry contextEntry : list) {
            ContextEntryXmlDto contextEntryType = new ContextEntryXmlDto();
            contextEntryType.setId(contextEntry.getKey());
            contextEntryType.setValue(contextEntry.getExpression());
            contextEntryType.setType(contextEntry.getType());
            contextEntries.add(contextEntryType);
        }

        return contextEntries;
    }

    private static List<CategoryXmlDto> writeValidatorCategories(Validator validator) {
        if (validator.getCategories() == null || validator.getCategories().isEmpty())
            return null;

        // sort the categories by they ID
        List<Category> list = new ArrayList<>();
        if (validator.getCategories() != null)
            list.addAll(validator.getCategories());
        list.sort((o1, o2) -> o1.getId().compareToIgnoreCase(o2.getId()));

        List<CategoryXmlDto> categoriesType = new ArrayList<>();
        for (Category category : list) {
            CategoryXmlDto categoryType = new CategoryXmlDto();
            categoryType.setId(category.getId());
            categoryType.setName(category.getName());
            categoryType.setDescription(category.getDescription());
            categoriesType.add(categoryType);
        }

        return categoriesType;
    }

    private static List<ConditionXmlDto> writeValidatorConditions(Validator validator) {
        if (validator.getConditions() == null || validator.getConditions().isEmpty())
            return null;

        // sort the condition by they ID
        List<Condition> list = new ArrayList<>();
        if (validator.getConditions() != null)
            list.addAll(validator.getConditions());
        list.sort((o1, o2) -> o1.getId().compareToIgnoreCase(o2.getId()));

        List<ConditionXmlDto> conditionsType = new ArrayList<>();
        for (Condition condition : list) {
            ConditionXmlDto conditionType = new ConditionXmlDto();
            conditionType.setId(condition.getId());
            conditionType.setName(condition.getName());
            conditionType.setJavaPath(condition.getJavaPath());
            conditionType.setExpression(reAlign(condition.getExpression()));
            conditionType.setDescription(condition.getDescription());
            conditionsType.add(conditionType);
        }

        return conditionsType;
    }

    private static List<RuleXmlDto> writeValidatorRules(Validator validator) {
        if (validator.getRules() == null || validator.getRules().isEmpty())
            return null;

        // sort the rules by they ID (try to be smart about the IF edits)
        List<Rule> list = new ArrayList<>(validator.getRules());
        list.sort((o1, o2) -> {
            String id1 = o1.getId();
            String id2 = o2.getId();

            Matcher m1 = _PATTERN_RULE_ID.matcher(id1);
            Matcher m2 = _PATTERN_RULE_ID.matcher(id2);

            if (m1.matches() && m2.matches()) {
                String prefix1 = m1.group(1);
                String prefix2 = m2.group(1);
                String integerPart1 = m1.group(2);
                String integerPart2 = m2.group(2);
                String suffix1 = m1.group(3);
                String suffix2 = m2.group(3);

                int result = prefix1.compareToIgnoreCase(prefix2);
                if (result == 0) {
                    Integer i1 = Integer.valueOf(integerPart1);
                    Integer i2 = Integer.valueOf(integerPart2);
                    result = i1.compareTo(i2);
                    if (result == 0) {
                        if (suffix1 == null)
                            return -1;
                        else if (suffix2 == null)
                            return 1;
                        else
                            return suffix1.compareToIgnoreCase(suffix2);
                    }
                    else
                        return result;
                }
                else
                    return result;
            }
            else
                return id1.toUpperCase().compareTo(id2.toUpperCase());
        });

        List<RuleXmlDto> rulesType = new ArrayList<>();
        for (Rule rule : list) {
            RuleXmlDto ruleType = new RuleXmlDto();
            if (rule.getDependencies() != null && !rule.getDependencies().isEmpty()) {
                List<String> sortedDep = new ArrayList<>(rule.getDependencies());
                Collections.sort(sortedDep);
                StringBuilder buf = new StringBuilder();
                for (String dep : sortedDep)
                    buf.append(dep).append(",");
                buf.setLength(buf.length() - 1);
                ruleType.setDepends(buf.toString());
            }
            ruleType.setDescription(rule.getDescription());
            ruleType.setExpression(reAlign(rule.getExpression()));
            ruleType.setId(rule.getId());
            ruleType.setMessage(rule.getMessage());
            ruleType.setName(rule.getName());
            ruleType.setTag(rule.getTag());
            ruleType.setJavaPath(rule.getJavaPath());
            ruleType.setCategory(rule.getCategory());
            if (rule.getConditions() != null && !rule.getConditions().isEmpty()) {
                StringBuilder condBuf = new StringBuilder();
                for (String c : rule.getConditions()) {
                    if (!c.isEmpty())
                        condBuf.append(Boolean.TRUE.equals(rule.getUseAndForConditions()) ? "&" : "|");
                    condBuf.append(c);
                }
                ruleType.setCondition(condBuf.toString());
            }
            if (rule.getSeverity() != null)
                ruleType.setSeverity(rule.getSeverity());
            ruleType.setAgency(rule.getAgency());

            if (rule.getHistories() != null && !rule.getHistories().isEmpty()) {
                List<HistoryEventXmlDto> sortedEvents = new ArrayList<>();
                for (RuleHistory history : rule.getHistories()) {
                    HistoryEventXmlDto eventType = new HistoryEventXmlDto();
                    if (history.getVersion() != null)
                        eventType.setVersion(history.getVersion().getRawString());
                    eventType.setUser(history.getUsername());
                    eventType.setValue(history.getMessage());
                    eventType.setDate(history.getDate());
                    eventType.setRef(history.getReference());
                    sortedEvents.add(eventType);
                }
                // sort the event by date, from older to newer
                sortedEvents.sort((o1, o2) -> {
                    Calendar c1 = Calendar.getInstance();
                    c1.setTime(o1.getDate());
                    Calendar c2 = Calendar.getInstance();
                    c2.setTime(o2.getDate());
                    if (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH) && c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH)) {
                        if (o1.getRef() != null && o2.getRef() != null)
                            return o1.getRef().compareTo(o2.getRef());
                        return o1.getUser().compareTo(o2.getUser());
                    }
                    return o1.getDate().compareTo(o2.getDate());
                });
                ruleType.setHistoryEvents(sortedEvents);
            }

            rulesType.add(ruleType);
        }

        return rulesType;
    }

    private static List<SetXmlDto> writeValidatorSets(Validator validator) {
        if (validator.getSets() == null || validator.getSets().isEmpty())
            return null;

        // sort the sets by they ID
        List<EmbeddedSet> list = new ArrayList<>();
        if (validator.getSets() != null)
            list.addAll(validator.getSets());
        list.sort((o1, o2) -> o1.getId().compareToIgnoreCase(o2.getId()));

        List<SetXmlDto> setsType = new ArrayList<>();
        for (EmbeddedSet set : list) {
            SetXmlDto setType = new SetXmlDto();
            setType.setId(set.getId());
            setType.setName(set.getName());
            setType.setTag(set.getTag());
            setType.setDescription(set.getDescription());
            List<String> inclusions = new ArrayList<>(set.getInclusions() == null ? Collections.emptySet() : set.getInclusions());
            Collections.sort(inclusions);
            if (!inclusions.isEmpty()) {
                StringBuilder buf = new StringBuilder();
                for (String s : inclusions)
                    buf.append(s).append(",");
                buf.setLength(buf.length() - 1);
                setType.setInclude(buf.toString());
            }

            List<String> exclusions = new ArrayList<>(set.getExclusions() == null ? Collections.emptySet() : set.getExclusions());
            Collections.sort(exclusions);
            if (!exclusions.isEmpty()) {
                StringBuilder buf = new StringBuilder();
                for (String s : exclusions)
                    buf.append(s).append(",");
                buf.setLength(buf.length() - 1);
                setType.setExclude(buf.toString());
            }

            setsType.add(setType);
        }

        return setsType;
    }

    // ****************************************************************************************************
    //                                 STANDALONE SETS METHODS
    // ****************************************************************************************************

    /**
     * Creates an instance of XStream for reading and writing validator objects.
     * @return an instance of XStream, never null
     */
    private static XStream createStandaloneSetXStream() {
        XStream xstream = new XStream(new StandaloneSetXmlDriver()) {
            // only register the converters we need; other converters generate a private access warning in the console on Java9+...
            @Override
            protected void setupConverters() {
                registerConverter(new NullConverter(), PRIORITY_VERY_HIGH);
                registerConverter(new IntConverter(), PRIORITY_NORMAL);
                registerConverter(new FloatConverter(), PRIORITY_NORMAL);
                registerConverter(new DoubleConverter(), PRIORITY_NORMAL);
                registerConverter(new LongConverter(), PRIORITY_NORMAL);
                registerConverter(new ShortConverter(), PRIORITY_NORMAL);
                registerConverter(new BooleanConverter(), PRIORITY_NORMAL);
                registerConverter(new ByteConverter(), PRIORITY_NORMAL);
                registerConverter(new StringConverter(), PRIORITY_NORMAL);
                registerConverter(new DateConverter(), PRIORITY_NORMAL);
                registerConverter(new CollectionConverter(getMapper()), PRIORITY_NORMAL);
                registerConverter(new ReflectionConverter(getMapper(), getReflectionProvider()), PRIORITY_VERY_LOW);
            }
        };
        xstream.autodetectAnnotations(true);
        xstream.alias("set", StandaloneSetXmlDto.class);

        // setup proper security by limiting what classes can be loaded by XStream
        xstream.addPermission(NoTypePermission.NONE);
        xstream.addPermission(new WildcardTypePermission(new String[] {"com.imsweb.validation.**"}));

        return xstream;
    }

    /**
     * Loads a standalone set of edits from the corresponding file.
     * <br/><br/>
     * If the filename ends with 'gz', a compressed file will be assumed; zipped files
     * are not supported (that doesn't mean they cannot be handled, it just means the caller has
     * to provide a stream to the zip entry).
     * <p/>
     * Created on Nov 24, 2010 by Fabian
     * @param file <code>File</code>, cannot be null, must exist.
     * @return a <code>StandaloneSet</code>, never null
     * @throws IOException if unable to properly read/write the entity
     */
    public static StandaloneSet loadStandaloneSetFromXml(File file) throws IOException {
        if (file == null)
            throw new IOException("Unable to load standalone set, target file is null");
        if (!file.exists())
            throw new IOException("Unable to load standalone set, target file doesn't exist");

        try (InputStream is = file.getName().toLowerCase().endsWith(".gz") ? new GZIPInputStream(Files.newInputStream(file.toPath())) : Files.newInputStream(file.toPath())) {
            return loadStandaloneSetFromXml(is);
        }
    }

    /**
     * Loads a standalone set of edits from the corresponding <code>URL</code>.
     * <br/><br/>
     * This method supports a gzipped compressed resource (if the URL path ends with gz or gzip); otherwise
     * it assumes the resource is not compressed.
     * <p/>
     * Created on Nov 24, 2010 by Fabian
     * @param url <code>URL</code>, cannot be null.
     * @return a <code>StandaloneSet</code>, never null
     * @throws IOException if unable to properly read/write the entity
     */
    public static StandaloneSet loadStandaloneSetFromXml(URL url) throws IOException {
        if (url == null)
            throw new IOException("Unable to load standalone set, target URL is null");

        try (InputStream is = url.getPath().toLowerCase().endsWith(".gz") ? new GZIPInputStream(url.openStream()) : url.openStream()) {
            return loadStandaloneSetFromXml(is);
        }
    }

    /**
     * Loads a standalone set of edits from the corresponding input stream.
     * <br/><br/>
     * The passed stream will NOT be closed when this method returns.
     * <br/><br/>
     * This methods makes no assumptions on the compression of the stream.
     * <p/>
     * Created on Nov 24, 2010 by Fabian
     * @param is <code>InputStream</code>, cannot be null
     * @return a <code>StandaloneSet</code>, never null
     * @throws IOException if unable to properly read/write the entity
     */
    public static StandaloneSet loadStandaloneSetFromXml(InputStream is) throws IOException {
        if (is == null)
            throw new IOException("Unable to load standalone set, target input stream is null");

        try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            return loadStandaloneSetFromXml(reader);
        }
    }

    /**
     * Loads a standalone set of edits from the corresponding reader.
     * <br/><br/>
     * The passed reader will NOT be closed when this method returns.
     * <p/>
     * Created on Nov 24, 2010 by Fabian
     * @param reader <code>Reader</code>, cannot be null
     * @return a <code>StandaloneSet</code>, never null
     * @throws IOException if unable to properly read/write the entity
     */
    public static StandaloneSet loadStandaloneSetFromXml(Reader reader) throws IOException {
        if (reader == null)
            throw new IOException("Unable to load standalone set, target reader is null");

        try {
            StandaloneSetXmlDto setType = (StandaloneSetXmlDto)createStandaloneSetXStream().fromXML(reader);

            StandaloneSet set = new StandaloneSet();
            if (setType.getId() == null)
                throw new IOException("Set ID is required");
            set.setId(setType.getId());
            set.setName(setType.getName());
            set.setDescription(reAlign(setType.getDesc()));

            for (StandaloneSetValidatorXmlDto validatorType : setType.getValidators()) {

                Map<String, List<String>> inclusions = new HashMap<>();
                if (validatorType.getId() == null)
                    throw new IOException("Validator ID is required");
                set.addReferencedValidatorId(validatorType.getId());
                String include = validatorType.getInclude();
                if (include != null && !include.trim().isEmpty()) {
                    List<String> inc = new ArrayList<>();
                    for (String s : StringUtils.split(include, ','))
                        inc.add(s.trim());
                    inclusions.put(validatorType.getId(), inc);
                }
                set.setInclusions(inclusions);

                Map<String, List<String>> exclusions = new HashMap<>();
                String exclude = validatorType.getExclude();
                if (exclude != null && !exclude.trim().isEmpty()) {
                    List<String> exc = new ArrayList<>();
                    for (String s : StringUtils.split(exclude, ','))
                        exc.add(s.trim());
                    exclusions.put(validatorType.getId(), exc);
                }
                set.setExclusions(exclusions);
            }

            return set;
        }
        catch (RuntimeException e) {
            throw new IOException("Unable to construct new standalone set instance", e);
        }
    }

    /**
     * Writes the provided standalone set to the provided file.
     * <p/>
     * Created on Nov 24, 2010 by Fabian
     * @param set <code>StandaloneSet</code> to write, cannot be null
     * @param file <code>targetFile</code>, cannot be null, parent directory must exist
     * @throws IOException if unable to properly read/write the entity
     */
    public static void writeStandaloneSetToXml(StandaloneSet set, File file) throws IOException {
        if (file == null)
            throw new IOException("Unable to write set '" + set.getId() + "', target file is null");

        try (OutputStream os = file.getName().toLowerCase().endsWith(".gz") ? new GZIPOutputStream(Files.newOutputStream(file.toPath())) : Files.newOutputStream(file.toPath())) {
            writeStandaloneSetToXml(set, os);
        }
    }

    /**
     * Writes the provided standalone set to the provided input stream.
     * <p/>
     * The passed stream will NOT be closed when this method returns.
     * <p/>
     * Created on Nov 24, 2010 by Fabian
     * @param set <code>StandaloneSet</code> to write, cannot be null
     * @param os <code>OutputStream</code>, cannot be null
     * @throws IOException if unable to properly read/write the entity
     */
    public static void writeStandaloneSetToXml(StandaloneSet set, OutputStream os) throws IOException {
        if (os == null)
            throw new IOException("Unable to write set '" + set.getId() + "', target output stream is null");

        try (OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
            writeStandaloneSetToXml(set, writer);
        }
    }

    /**
     * Writes the provided standalone set to the provided reader.
     * <p/>
     * The passed reader will NOT be closed when this method returns.
     * <p/>
     * Created on Nov 24, 2010 by Fabian
     * @param set <code>StandaloneSet</code> to write, cannot be null
     * @param writer <code>Writer</code>, cannot be null
     * @throws IOException if unable to properly read/write the entity
     */
    public static void writeStandaloneSetToXml(StandaloneSet set, Writer writer) throws IOException {
        if (set == null)
            throw new IOException("Unable to write NULL set");
        if (writer == null)
            throw new IOException("Unable to write set '" + set.getId() + "', target writer is null");

        try {
            StandaloneSetXmlDto setType = new StandaloneSetXmlDto();
            setType.setId(set.getId());
            setType.setName(set.getName());
            setType.setDesc(set.getDescription());

            Set<String> validatorIds = new TreeSet<>();
            if (set.getInclusions() != null)
                validatorIds.addAll(set.getInclusions().keySet());
            if (set.getExclusions() != null && !set.getExclusions().isEmpty())
                validatorIds.addAll(set.getExclusions().keySet());

            List<StandaloneSetValidatorXmlDto> validators = new ArrayList<>();
            for (String validatorId : validatorIds) {
                StandaloneSetValidatorXmlDto validatorType = new StandaloneSetValidatorXmlDto();
                validatorType.setId(validatorId);

                List<String> inclusions = set.getInclusions() == null ? null : set.getInclusions().get(validatorId);
                List<String> exclusions = set.getExclusions() == null ? null : set.getExclusions().get(validatorId);

                if (inclusions != null && !inclusions.isEmpty()) {
                    StringBuilder buf = new StringBuilder();
                    for (String s : inclusions)
                        buf.append(s).append(",");
                    buf.setLength(buf.length() - 1);
                    validatorType.setInclude(buf.toString());
                }

                if (exclusions != null && !exclusions.isEmpty()) {
                    StringBuilder buf = new StringBuilder();
                    for (String s : exclusions)
                        buf.append(s).append(",");
                    buf.setLength(buf.length() - 1);
                    validatorType.setExclude(buf.toString());
                }

                validators.add(validatorType);
            }
            setType.setValidators(validators);

            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.write(System.lineSeparator());
            createStandaloneSetXStream().toXML(setType, writer);
        }
        catch (RuntimeException e) {
            throw new IOException(e);
        }
    }

    // ****************************************************************************************************
    //                                       TESTS METHODS
    // ****************************************************************************************************

    /**
     * Creates an instance of XStream for reading and writing validator objects.
     * @return an instance of XStream, never null
     */
    private static XStream createTestsXStream() {
        XStream xstream = new XStream(new TestsXmlDriver()) {
            // only register the converters we need; other converters generate a private access warning in the console on Java9+...
            @Override
            protected void setupConverters() {
                registerConverter(new NullConverter(), PRIORITY_VERY_HIGH);
                registerConverter(new IntConverter(), PRIORITY_NORMAL);
                registerConverter(new FloatConverter(), PRIORITY_NORMAL);
                registerConverter(new DoubleConverter(), PRIORITY_NORMAL);
                registerConverter(new LongConverter(), PRIORITY_NORMAL);
                registerConverter(new ShortConverter(), PRIORITY_NORMAL);
                registerConverter(new BooleanConverter(), PRIORITY_NORMAL);
                registerConverter(new ByteConverter(), PRIORITY_NORMAL);
                registerConverter(new StringConverter(), PRIORITY_NORMAL);
                registerConverter(new DateConverter(), PRIORITY_NORMAL);
                registerConverter(new CollectionConverter(getMapper()), PRIORITY_NORMAL);
                registerConverter(new ReflectionConverter(getMapper(), getReflectionProvider()), PRIORITY_VERY_LOW);
            }
        };
        xstream.autodetectAnnotations(true);
        xstream.alias("tested-validator", TestedValidatorXmlDto.class);

        // setup proper security by limiting what classes can be loaded by XStream
        xstream.addPermission(NoTypePermission.NONE);
        xstream.addPermission(new WildcardTypePermission(new String[] {"com.imsweb.validation.**"}));

        return xstream;
    }

    /**
     * Loads a suite of tests of edits from the corresponding file.
     * <br/><br/>
     * If the filename ends with 'gz', a compressed file will be assumed; zipped files
     * are not supported (that doesn't mean they cannot be handled, it just means the caller has
     * to provide a stream to the zip entry).
     * <p/>
     * Created on Nov 24, 2010 by Fabian
     * @param file <code>File</code>, cannot be null, must exist.
     * @return a <code>ValidatorTests</code>, never null
     * @throws IOException if unable to properly read/write the entity
     */
    public static ValidatorTests loadTestsFromXml(File file) throws IOException {
        if (file == null)
            throw new IOException("Unable to load tests suite, target file is null");
        if (!file.exists())
            throw new IOException("Unable to load tests suite, target file doesn't exist");

        try (InputStream is = file.getName().toLowerCase().endsWith(".gz") ? new GZIPInputStream(Files.newInputStream(file.toPath())) : Files.newInputStream(file.toPath())) {
            return loadTestsFromXml(is);
        }
    }

    /**
     * Loads a suite of tests from the corresponding URL.
     * <br/><br/>
     * This method supports a gzipped compressed resource (if the URL path ends with gz or gzip); otherwise
     * it assumes the resource is not compressed.
     * <p/>
     * Created on Nov 24, 2010 by Fabian
     * @param url <code>URL</code>, cannot be null.
     * @return a <code>ValidatorTests</code>, never null
     * @throws IOException if unable to properly read/write the entity
     */
    public static ValidatorTests loadTestsFromXml(URL url) throws IOException {
        if (url == null)
            throw new IOException("Unable to load tests suite, target URL is null");

        try (InputStream is = url.getPath().toLowerCase().endsWith(".gz") ? new GZIPInputStream(url.openStream()) : url.openStream()) {
            return loadTestsFromXml(is);
        }
    }

    /**
     * Loads a suite of tests from the corresponding input stream.
     * <br/><br/>
     * The passed stream will NOT be closed when this method returns.
     * <br/><br/>
     * This methods makes no assumptions on the compression of the stream.
     * <p/>
     * Created on Nov 24, 2010 by Fabian
     * @param is <code>InputStream</code>, cannot be null
     * @return a <code>ValidatorTests</code>, never null
     * @throws IOException if unable to properly read/write the entity
     */
    public static ValidatorTests loadTestsFromXml(InputStream is) throws IOException {
        if (is == null)
            throw new IOException("Unable to load tests suite, target input stream is null");

        try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            return loadTestsFromXml(reader);
        }
    }

    /**
     * Loads a suite of tests from the corresponding reader.
     * <br/><br/>
     * The passed reader will NOT be closed when this method returns.
     * <p/>
     * Created on Nov 24, 2010 by Fabian
     * @param reader <code>Reader</code>, cannot be null
     * @return a <code>ValidatorTests</code>, never null
     * @throws IOException if unable to properly read/write the entity
     */
    public static ValidatorTests loadTestsFromXml(Reader reader) throws IOException {
        if (reader == null)
            throw new IOException("Unable to load test suites, target reader is null");

        try {
            TestedValidatorXmlDto validatorTestsType = (TestedValidatorXmlDto)createTestsXStream().fromXML(reader);

            ValidatorTests validatorTests = new ValidatorTests();
            validatorTests.setTestedValidatorId(validatorTestsType.getId());

            Map<String, RuleTest> tests = new HashMap<>();
            for (TestXmlDto testType : validatorTestsType.getTest()) {
                RuleTest test = new RuleTest();
                test.setTestedRuleId(testType.getTestId());
                test.setScriptText(reAlign(testType.getScript()));
                tests.put(test.getTestedRuleId(), test);
            }
            validatorTests.setTests(tests);

            return validatorTests;
        }
        catch (RuntimeException e) {
            throw new IOException("Unable to construct new tests suite instance", e);
        }
    }

    /**
     * Writes the provided tests suite to the provided file.
     * <p/>
     * Created on Nov 24, 2010 by Fabian
     * @param tests <code>ValidatorTests</code> to write, cannot be null
     * @param file <code>targetFile</code>, cannot be null, parent directory must exist
     * @throws IOException if unable to properly read/write the entity
     */
    public static void writeTestsToXml(ValidatorTests tests, File file) throws IOException {
        if (file == null)
            throw new IOException("Unable to tests suite for '" + tests.getTestedValidatorId() + "', target file is null");

        try (OutputStream os = file.getName().toLowerCase().endsWith(".gz") ? new GZIPOutputStream(Files.newOutputStream(file.toPath())) : Files.newOutputStream(file.toPath())) {
            writeTestsToXml(tests, os);
        }
    }

    /**
     * Writes the provided tests to the provided input stream.
     * <p/>
     * The passed stream will NOT be closed when this method returns.
     * <p/>
     * Created on Nov 24, 2010 by Fabian
     * @param tests <code>ValidatorTests</code> to write, cannot be null
     * @param os <code>OutputStream</code>, cannot be null
     * @throws IOException if unable to properly read/write the entity
     */
    public static void writeTestsToXml(ValidatorTests tests, OutputStream os) throws IOException {
        if (os == null)
            throw new IOException("Unable to write tests suite for '" + tests.getTestedValidatorId() + "', target output stream is null");

        try (OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
            writeTestsToXml(tests, writer);
        }
    }

    /**
     * Writes the provided tests to the provided reader.
     * <p/>
     * The passed reader will NOT be closed when this method returns.
     * <p/>
     * Created on Nov 24, 2010 by Fabian
     * @param tests <code>ValidatorTests</code> to write, cannot be null
     * @param writer <code>Writer</code>, cannot be null
     * @throws IOException if unable to properly read/write the entity
     */
    public static void writeTestsToXml(ValidatorTests tests, Writer writer) throws IOException {
        if (tests == null)
            throw new IOException("Unable to write NULL tests suite");
        if (writer == null)
            throw new IOException("Unable to write tests suite for '" + tests.getTestedValidatorId() + "', target writer is null");

        try {
            TestedValidatorXmlDto validatorType = new TestedValidatorXmlDto();
            validatorType.setId(tests.getTestedValidatorId());

            if (tests.getTests() != null) {
                List<RuleTest> sortedTests = new ArrayList<>(tests.getTests().values());

                // sort the tests by they ID (try to be smart about the IF edits)
                sortedTests.sort((o1, o2) -> {
                    String id1 = o1.getTestedRuleId();
                    String id2 = o2.getTestedRuleId();

                    Matcher m1 = _PATTERN_RULE_ID.matcher(id1);
                    Matcher m2 = _PATTERN_RULE_ID.matcher(id2);

                    if (m1.matches() && m2.matches()) {
                        String prefix1 = m1.group(1);
                        String prefix2 = m2.group(1);
                        String integerPart1 = m1.group(2);
                        String integerPart2 = m2.group(2);
                        String suffix1 = m1.group(3);
                        String suffix2 = m2.group(3);

                        int result = prefix1.compareToIgnoreCase(prefix2);
                        if (result == 0) {
                            Integer i1 = Integer.valueOf(integerPart1);
                            Integer i2 = Integer.valueOf(integerPart2);
                            result = i1.compareTo(i2);
                            if (result == 0) {
                                if (suffix1 == null)
                                    return -1;
                                else if (suffix2 == null)
                                    return 1;
                                else
                                    return suffix1.compareToIgnoreCase(suffix2);
                            }
                            else
                                return result;
                        }
                        else
                            return result;
                    }
                    else
                        return id1.toUpperCase().compareTo(id2.toUpperCase());
                });

                List<TestXmlDto> testList = new ArrayList<>();
                for (RuleTest test : sortedTests) {
                    TestXmlDto testType = new TestXmlDto();
                    testType.setTestId(test.getTestedRuleId());
                    testType.setScript(test.getScriptText());
                    testList.add(testType);
                }
                validatorType.setTest(testList);
            }

            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.write(System.lineSeparator());
            createTestsXStream().toXML(validatorType, writer);
        }
        catch (RuntimeException e) {
            throw new IOException(e);
        }
    }

    // ****************************************************************************************************
    //                                      MISCELLANEOUS METHODS
    // ****************************************************************************************************

    /**
     * Enables the re-alignment mechanism when reading expressions, descriptions, messages, etc... This is the default behavior of the engine.
     */
    public static void enableRealignment() {
        _REALIGNMENT_ENABLED = true;
    }

    /**
     * Disables the re-alignment mechanism when reading expressions, descriptions, messages, etc...
     */
    public static void disableRealignment() {
        _REALIGNMENT_ENABLED = false;
    }

    /**
     * Returns true if the passed <code>URL</code> exists, false otherwise
     * <p/>
     * Created on Mar 6, 2008 by depryf
     * @param url <code>URL</code>, possibly null
     * @return boolean
     */
    public static boolean targetValidatorXmlExists(URL url) {
        if (url == null)
            return false;

        try (InputStream is = url.openStream()) {
            // I don't like to depend on an exception to check the existence but URL doesn't have any method to do that :-(
            if (is == null)
                return false;
        }
        catch (IOException | RuntimeException e) {
            return false;
        }

        return true;
    }

    /**
     * Returns the main attributes of the validator corresponding to the passed <code>URL</code>, null if none are found.
     * <br/><br/>
     * This method supports a gzipped compressed resource (if the URL path ends with gz or gzip); otherwise
     * it assumes the resource is not compressed.
     * <p/>
     * Created on Nov 13, 2008 by depryf
     * @param url target <code>URL</code>
     * @return corresponding attributes, maybe empty but never null
     */
    public static Map<String, String> getXmlValidatorRootAttributes(URL url) {
        Map<String, String> result = new HashMap<>();
        if (url == null)
            return result;

        Pattern regex1 = Pattern.compile("<validator\\s++([^>]++)>", Pattern.MULTILINE | Pattern.DOTALL);
        Pattern regex2 = Pattern.compile("(id|name|version|min-engine-version|translated-from)\\s*+=\\s*+['\"]([^'\"]+?)['\"]", Pattern.MULTILINE | Pattern.DOTALL);

        boolean gzipped = url.getPath().toLowerCase().endsWith(".gz") || url.getPath().toLowerCase().endsWith(".gzip");
        try (InputStream is = gzipped ? new GZIPInputStream(url.openStream()) : url.openStream()) {
            byte[] bytes = new byte[1024];
            int n = is.read(bytes);
            while (n > 0) {
                String line = new String(bytes, 0, n, StandardCharsets.UTF_8);
                if (StringUtils.contains(line, "<validator")) { // hopefully this check is cheaper that using the regex

                    Matcher m1 = regex1.matcher(line);
                    if (m1.find()) {
                        Matcher m2 = regex2.matcher(m1.group(1).trim().replaceAll("\r?\n", ""));
                        while (m2.find())
                            result.put(m2.group(1), m2.group(2));
                    }
                    else {
                        // we have the tag but not the regex, it probably indicates that we are in between; let's get the next line...
                        n = is.read(bytes);
                        m1 = regex1.matcher(line + new String(bytes, 0, n, StandardCharsets.UTF_8));
                        if (m1.find()) {
                            Matcher m2 = regex2.matcher(m1.group(1).trim().replaceAll("\r?\n", ""));
                            while (m2.find())
                                result.put(m2.group(1), m2.group(2));
                        }
                    }

                    // regardless of whether we found the attributes or not, we did find the (supposedly unique) validator tag, so we can stop looking
                    break;
                }
                n = is.read(bytes);
            }
        }
        catch (IOException | RuntimeException e) {
            /* do nothing */
        }

        return result;
    }

    /**
     * Returns the hash code corresponding to the passed <code>URL</code>, null if it cannot be computed.
     * <br/><br/>
     * This method uses the SHA-1 algorithm to compute the code.
     * <br/><br/>
     * This method supports a gzipped compressed resource (if the URL path ends with gz or gzip); otherwise
     * it assumes the resource is not compressed.
     * <p/>
     * Created on Mar 6, 2008 by depryf
     * @param url <code>URL</code>, possibly null
     * @return corresponding hash code
     */
    @SuppressWarnings("java:S4790") // week hash algorithm, but hashing is just used to know if an validator changed, it's OK in this case
    public static String getXmlValidatorHash(URL url) {
        if (url == null)
            return null;

        String result;
        boolean gzipped = url.getPath().toLowerCase().endsWith(".gz") || url.getPath().toLowerCase().endsWith(".gzip");
        try (InputStream is = gzipped ? new GZIPInputStream(url.openStream()) : url.openStream()) {
            result = Hex.encodeHexString(DigestUtils.updateDigest(DigestUtils.getDigest(MessageDigestAlgorithms.SHA_1), is).digest());
        }
        catch (IOException | RuntimeException e) {
            return null;
        }

        return result;
    }

    /**
     * Gets the validator ID from the passed <code>URL</code>, null if none is found.
     * <br/><br/>
     * This method supports a gzipped compressed resource (if the URL path ends with gz or gzip); otherwise
     * it assumes the resource is not compressed.
     * <p/>
     * Created on Nov 13, 2008 by depryf
     * @param url target <code>URL</code>
     * @return corresponding validator ID, maybe null
     */
    public static String getXmlValidatorId(URL url) {
        return getXmlValidatorRootAttributes(url).get(ROOT_ATTR_ID);
    }

    /**
     * Returns the validator name from the provided <code>URL</code>, null if it can't be found.
     * <br/><br/>
     * This method supports a gzipped compressed resource (if the URL path ends with gz or gzip); otherwise
     * it assumes the resource is not compressed.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @param url <code>URL</code>, can't be null
     * @return validator name, null if it can't be found
     */
    public static String getXmlValidatorName(URL url) {
        return getXmlValidatorRootAttributes(url).get(ROOT_ATTR_NAME);
    }

    /**
     * Returns the validator version from the provided <code>URL</code>, null if it can't be found.
     * <br/><br/>
     * This method supports a gzipped compressed resource (if the URL path ends with gz or gzip); otherwise
     * it assumes the resource is not compressed.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @param url <code>URL</code>, can't be null
     * @return validator version, null if it can't be found
     */
    public static String getXmlValidatorVersion(URL url) {
        return getXmlValidatorRootAttributes(url).get(ROOT_ATTR_VERSION);
    }

    /**
     * Removes any leading or trailing empty lines
     * <p/>
     * Created on Jan 29, 2008 by depryf
     * @param s string to parse
     * @return parsed string
     */
    public static String trimEmptyLines(String s, boolean trim) {
        if (StringUtils.isBlank(s))
            return null;

        if (_REALIGNMENT_ENABLED) {

            // replace tabs by 4 spaces
            s = _PATTERN_TAB.matcher(s).replaceAll("    ");

            // remove any control characters
            s = _CONTROL_CHARACTERS_PATTERN.matcher(s).replaceAll("");

            // keep track of leading spaces so we can re-apply them after trimming (if we have to trim, none of this is relevant)
            if (!trim) {
                StringBuilder spacesBuffer = new StringBuilder();
                for (int i = 0; i < s.length(); i++) {
                    if (s.charAt(i) == ' ')
                        spacesBuffer.append(" ");
                    else
                        break;
                }

                s = s.trim();
                if (spacesBuffer.length() > 0)
                    s = spacesBuffer + s;
            }

        }

        // remove any leading/trailing spaces
        if (trim)
            s = s.trim();

        return s;
    }

    /**
     * Removes any extra white space at the beginning of each line depending on the white spaces at the beginning of the first line.
     * <p/>
     * Created on Jan 29, 2008 by depryf
     * @param toAlign string to parse
     * @return parsed string
     */
    public static String reAlign(String toAlign) {
        if (StringUtils.isBlank(toAlign))
            return null;

        if (_REALIGNMENT_ENABLED) {

            // let's remove the leading/trailing new lines, but not the leading/trailing spaces of the first/last line (so we can properly re-align)
            String s = trimEmptyLines(toAlign, false);

            // determine number of extra white spaces
            int extraSpaces = Integer.MAX_VALUE;
            try {
                @SuppressWarnings("ConstantConditions")
                LineNumberReader reader = new LineNumberReader(new StringReader(s));
                String line = reader.readLine();
                while (line != null) {
                    int numSpaces = 0;
                    if (!line.trim().isEmpty()) {
                        for (int i = 0; i < line.length(); i++)
                            if (line.charAt(i) == ' ')
                                numSpaces++;
                            else
                                break;

                        extraSpaces = Math.min(extraSpaces, numSpaces);
                    }

                    line = reader.readLine();
                }
            }
            catch (IOException | RuntimeException e) {
                return s;
            }

            // don't bother going on if there is nothing to trim
            if (extraSpaces == 0 || extraSpaces == Integer.MAX_VALUE)
                return StringUtils.trim(s);

            // apply the trimming
            StringBuilder result = new StringBuilder();
            try {
                LineNumberReader reader = new LineNumberReader(new StringReader(s));
                String line = reader.readLine();
                while (line != null) {
                    if (!line.trim().isEmpty())
                        result.append(line.substring(extraSpaces)).append("\n");
                    else
                        result.append("\n");

                    line = reader.readLine();
                }

                result.setLength(result.length() - 1);
            }
            catch (Exception e) {
                return s;
            }

            return result.toString().trim();
        }

        return toAlign;
    }
}