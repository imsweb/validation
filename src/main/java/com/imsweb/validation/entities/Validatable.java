/*
 * Copyright (C) 2011 Information Management Services, Inc.
 */
package com.imsweb.validation.entities;

import com.imsweb.validation.ValidationContextFunctions;
import com.imsweb.validation.internal.ExtraPropertyEntityHandlerDto;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Validatable interface, this represents a wrapper for the different entities that can be validated.
 * <p/>
 * Created on Apr 26, 2011 by depryf
 */
public interface Validatable {

    /**
     * Key for the line number corresponding to the entity being validated; some validatable implementations might put this on their "line" data.
     */
    String KEY_LINE_NUMBER = "_lineNumber";

    /**
     * Key for the calculated CS staging schema ID that some validatable implementations put in the context of the executed edits.
     */
    String KEY_CS_SCHEMA_ID = "_csSchemaId";

    /**
     * Key for the calculated TNM staging schema ID that some validatable implementations put in the context of the executed edits.
     */
    String KEY_TNM_SCHEMA_ID = "_tnmSchemaId";

    /**
     * Key for the calculated EOD staging schema ID that some validatable implementations put in the context of the executed edits.
     */
    String KEY_EOD_SCHEMA_ID = "_eodSchemaId";

    /**
     * Returns the root level of this validatable. This corresponds to the first property in the full java-path of any rulesets.
     * <p/>
     * Created on Mar 6, 2008 by depryf
     * @return root level, never null
     */
    String getRootLevel();

    /**
     * Returns the current level of this validatable. This correspond to the full prefix of the property path.
     * <p/>
     * For a simple validatable wrapping a NAACCR line, the root prefix might be "line" and that might also be
     * the full prefix since there is only a single level of properties. For a more complex data structure like a patient set
     * in SEER*DMS, the current level could be something like "patient.ctcs".
     * <p/>
     * Created on Jul 29, 2010 by depryf
     * @return current level, never null
     */
    String getCurrentLevel();

    /**
     * Returns the scope for this validatable object.
     * <p/>
     * The scope is all the properties and their values for the current level being validated, plus any scope of parent levels. So for
     * example, if the current level is "patient.ctcs", the current scope is all the properties and values of the current tumor being validated
     * plus all the properties and values of the patient. That means an edit can always access the fields of the level it is supposed to run
     * against but also of any parent level in the data tree.
     * <p/>
     * Created on Nov 8, 2007 by depryf
     * @return the scope for this validatable object
     */
    Map<String, Object> getScope();

    /**
     * Returns the current Tumor ID, used to create the rule failure object. Could be null if the failure
     * is not for a particular tumor.
     * <p/>
     * Created on Nov 8, 2007 by depryf
     * @return current tumor ID, maybe null
     */
    Long getCurrentTumorId();

    /**
     * Method used to generate an array of {@link Validatable} objects from a given property representing a collection.
     * <br/><br/>
     * For example, if the collection is "ctcs" and the current level is "patient.ctcs", this method needs to return
     * an array containing one {@link Validatable} object for each tumor on the patient.
     * <br/><br/>
     * Ultimately, this method is the one that allows the validation engine to "follow" a complex data structure and
     * run the edits that need to be run on each level of the data.
     * <p/>
     * Created on Nov 8, 2007 by depryf
     * @param collection collection name
     * @return arary of validatable objects
     * @throws IllegalAccessException if anything goes wrong
     */
    List<Validatable> followCollection(String collection) throws IllegalAccessException;

    /**
     * This method is used by the validation engine to report a failure on a given property when running a particular edit.
     * <p/>
     * Created on Nov 8, 2007 by depryf
     * @param propertyName property name
     * @throws IllegalAccessException if anything goes wrong
     */
    void reportFailureForProperty(String propertyName) throws IllegalAccessException;

    /**
     * Force a failure on the passed properties.
     * <p/>
     * This is an advanced feature that allows an edit to report a failure on a property that is not actually used. See the
     * <code>forceFailureOnProperty()</code> method on the {@link ValidationContextFunctions} class.
     * <p/>
     * Created on Apr 27, 2010 by depryf
     * @param toReport properties to report, wrapped into a set of <code>ExtraPropertyEntityHandlerDto</code>
     * @param rawProperties the raw statically parsed properties
     * @throws IllegalAccessException if anything goes wrong
     */
    void forceFailureForProperties(Set<ExtraPropertyEntityHandlerDto> toReport, Set<String> rawProperties) throws IllegalAccessException;

    /**
     * Returns the set of properties corresponding to the failures for this validatable.
     * <p/>
     * Created on Nov 8, 2007 by depryf
     * @return set of properties, maybe null or empty
     */
    Set<String> getPropertiesWithError();

    /**
     * Clears the failing properties that have been reported so far.
     * <br/></br>
     * The validation engine calls this method after executing each rule.
     * <p/>
     * Created on Nov 8, 2007 by depryf
     */
    void clearPropertiesWithError();

    /**
     * Returns the display ID for this <code>Validatable</code>; will be used in case an exception
     * happens, so we know what entity was being validated.
     * <p/>
     * Created on Nov 13, 2008 by depryf
     */
    String getDisplayId();
}
