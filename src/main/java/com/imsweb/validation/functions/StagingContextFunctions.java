/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.validation.functions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import groovy.lang.IntRange;

import com.imsweb.staging.Staging;
import com.imsweb.staging.entities.ColumnDefinition;
import com.imsweb.staging.entities.Input;
import com.imsweb.staging.entities.Metadata;
import com.imsweb.staging.entities.Schema;
import com.imsweb.staging.entities.SchemaLookup;
import com.imsweb.staging.entities.Table;
import com.imsweb.validation.ContextFunctionDocAnnotation;
import com.imsweb.validation.ValidationContextFunctions;
import com.imsweb.validation.ValidationStagingUtils;

/**
 * Staging-related helper methods made available to the edits. If you want to execute edits that call some staging utility methods, you need to initialize
 * the context functions with an instance of this class. Otherwise you should just use the ValidationContextFunctions one.
 */
@SuppressWarnings("unused")
public class StagingContextFunctions extends ValidationContextFunctions {

    // the standard NAACCR properties used when getting a CStage schema
    public static final String CSTAGE_INPUT_PROP_SITE = "primarySite";
    public static final String CSTAGE_INPUT_PROP_HIST = "histologicTypeIcdO3";
    public static final String CSTAGE_INPUT_PROP_DISC = "csSiteSpecificFactor25";

    // meta data required the DX year; the full date is the standard item, but some framework use the year itself and so this class will check the year before the fulll date
    public static final String CSTAGE_INPUT_PROP_DX_DATE = "dateOfDiagnosis";
    public static final String CSTAGE_INPUT_PROP_DX_DATE_YEAR = "dateOfDiagnosisYear";

    // this maps the table number of the old DLL to the input keys used in the Staging framework
    public static final Map<Integer, String> CSTAGE_TABLE_NUMBERS;

    static {
        Map<Integer, String> tmpMap = new HashMap<>();
        tmpMap.put(1, "size");
        tmpMap.put(2, "extension");
        tmpMap.put(3, "extension_eval");
        tmpMap.put(4, "nodes");
        tmpMap.put(5, "nodes_eval");
        tmpMap.put(6, "nodes_pos");
        tmpMap.put(7, "nodes_exam");
        tmpMap.put(8, "mets");
        tmpMap.put(9, "mets_eval");
        tmpMap.put(10, "ssf1");
        tmpMap.put(11, "ssf2");
        tmpMap.put(12, "ssf3");
        tmpMap.put(13, "ssf4");
        tmpMap.put(14, "ssf5");
        tmpMap.put(15, "ssf6");
        tmpMap.put(16, "ssf7");
        tmpMap.put(17, "ssf8");
        tmpMap.put(18, "ssf9");
        tmpMap.put(19, "ssf10");
        tmpMap.put(20, "ssf11");
        tmpMap.put(21, "ssf12");
        tmpMap.put(22, "ssf13");
        tmpMap.put(23, "ssf14");
        tmpMap.put(24, "ssf15");
        tmpMap.put(25, "ssf16");
        tmpMap.put(26, "ssf17");
        tmpMap.put(27, "ssf18");
        tmpMap.put(28, "ssf19");
        tmpMap.put(29, "ssf20");
        tmpMap.put(30, "ssf21");
        tmpMap.put(31, "ssf22");
        tmpMap.put(32, "ssf23");
        tmpMap.put(33, "ssf24");
        tmpMap.put(34, "ssf25");
        // there are a few more tables, but I don't think they are used in the translated edits: 35 HIST7; 36 HIST; 37 AJCC7; 38 AJCC; 39 SEERSUM

        CSTAGE_TABLE_NUMBERS = Collections.unmodifiableMap(tmpMap);
    }

    // the obsolete reasons as defined in the CStage tables; edits use the key only...
    public static final Map<String, String> CSTAGE_OBSOLETE_REASONS;

    static {
        Map<String, String> tmpMap = new HashMap<>();
        tmpMap.put("OBSOLETE DATA CONVERTED AND RETAINED V0200", "1");
        tmpMap.put("OBSOLETE DATA CONVERTED V0102", "2");
        tmpMap.put("OBSOLETE DATA CONVERTED V0104", "3");
        tmpMap.put("OBSOLETE DATA CONVERTED V0200", "4");
        tmpMap.put("OBSOLETE DATA RETAINED V0100", "5");
        tmpMap.put("OBSOLETE DATA RETAINED V0102", "6");
        tmpMap.put("OBSOLETE DATA RETAINED V0200", "7");
        tmpMap.put("OBSOLETE DATA REVIEWED AND CHANGED V0102", "8");
        tmpMap.put("OBSOLETE DATA REVIEWED AND CHANGED V0103", "9");
        tmpMap.put("OBSOLETE DATA REVIEWED AND CHANGED V0200", "10");
        tmpMap.put("OBSOLETE DATA CONVERTED V0203", "11");
        tmpMap.put("OBSOLETE DATA REVIEWED AND CHANGED V0203", "12");
        tmpMap.put("OBSOLETE DATA REVIEWED V0203", "13");
        tmpMap.put("OBSOLETE DATA RETAINED AND REVIEWED V0203", "14");
        tmpMap.put("OBSOLETE DATA RETAINED V0203", "15");
        tmpMap.put("OBSOLETE DATA RETAINED V0104", "16");
        tmpMap.put("OBSOLETE DATA RETAINED V0202", "17");
        tmpMap.put("OBSOLETE DATA RETAINED AND REVIEWED V0200", "18");
        tmpMap.put("OBSOLETE DATA CONVERTED V0204", "19");
        tmpMap.put("OBSOLETE DATA REVIEWED AND CHANGED V0204", "20");
        tmpMap.put("OBSOLETE DATA RETAINED AND REVIEWED V0204", "21");
        tmpMap.put("OBSOLETE DATA RETAINED V0204", "22");

        CSTAGE_OBSOLETE_REASONS = Collections.unmodifiableMap(tmpMap);
    }

    // some specific cstage required tags used in the edits (the "required for staging" is not a tag, it's a calculated field, so it's not here)
    public static final String CSTAGE_TAG_ALREADY_COLLECTED_SEER = "SEER_ALREADY_COLLECTED";
    public static final String CSTAGE_TAG_CLINICALLY_SIGNIFICANT_SEER = "SEER_CLINICALLY_SIGNIFICANT";
    public static final String CSTAGE_TAG_REQUIRED_PRE_2010_SEER = "SEER_REQUIRED_PRE_2010";
    public static final String CSTAGE_TAG_ALREADY_COLLECTED_COC = "COC_ALREADY_COLLECTED";
    public static final String CSTAGE_TAG_CLINICALLY_SIGNIFICANT_COC = "COC_CLINICALLY_SIGNIFICANT";
    public static final String CSTAGE_TAG_REQUIRED_PRE_2010_COC = "COC_REQUIRED_PRE_2010";
    public static final String CSTAGE_TAG_UNDEFINED_SSF = "UNDEFINED_SSF";

    // the standard NAACCR properties used when getting a TNM schema
    public static final String TNM_INPUT_PROP_SITE = "primarySite";
    public static final String TNM_INPUT_PROP_HIST = "histologicTypeIcdO3";
    public static final String TNM_INPUT_PROP_SSF25 = "csSiteSpecificFactor25";
    public static final String TNM_INPUT_PROP_SEX = "sex";

    // TNM metadata tags
    public static final String TNM_TAG_SEER_REQUIRED = "SEER_REQUIRED";
    public static final String TNM_TAG_COC_REQUIRED = "COC_REQUIRED";
    public static final String TNM_TAG_NPCR_REQUIRED = "NPCR_REQUIRED";
    public static final String TNM_TAG_CCCR_REQUIRED = "CCCR_REQUIRED";

    // the standard NAACCR properties used when getting a EOD schema
    public static final String EOD_INPUT_PROP_SITE = "primarySite";
    public static final String EOD_INPUT_PROP_HIST = "histologicTypeIcdO3";
    public static final String EOD_INPUT_PROP_SEX = "sex";
    public static final String EOD_INPUT_PROP_DISC_1 = "schemaDiscriminator1";
    public static final String EOD_INPUT_PROP_DISC_2 = "schemaDiscriminator2";
    public static final String EOD_INPUT_PROP_BEHAV = "behaviorCodeIcdO3";
    public static final String EOD_INPUT_PROP_DX_YEAR = "dateOfDiagnosisYear";

    // EOD metadata tags
    public static final String EOD_TAG_SEER_REQUIRED = "SEER_REQUIRED";
    public static final String EOD_TAG_COC_REQUIRED = "COC_REQUIRED";
    public static final String EOD_TAG_NPCR_REQUIRED = "NPCR_REQUIRED";
    public static final String EOD_TAG_CCCR_REQUIRED = "CCCR_REQUIRED";
    public static final String EOD_TAG_SSDI = "SSDI";

    // the staging instances to use for cstage- and tnm-related logic
    protected Staging _csStaging;
    protected Staging _tnmStaging;
    protected Staging _eodStaging;

    // Cached schema ID per schema number for CS
    protected Map<Integer, String> _csSchemaIdByNumber = new HashMap<>();

    /**
     * Constructor.
     * @param csStaging a Staging instance responsible for all CStage-related logic
     * @param tnmStaging a Staging instance responsible for all TNM-related logic
     * @param eodStaging a Staging instance responsible for all EOD-related logic
     */
    public StagingContextFunctions(Staging csStaging, Staging tnmStaging, Staging eodStaging) {
        _csStaging = csStaging;
        _tnmStaging = tnmStaging;
        _eodStaging = eodStaging;

        if (_csStaging != null) {
            for (String schemaId : _csStaging.getSchemaIds()) {
                Schema schema = _csStaging.getSchema(schemaId);
                if (schema.getSchemaNum() != null)
                    _csSchemaIdByNumber.put(schema.getSchemaNum(), schemaId);
            }
        }
    }

    /**
     * Returns the Collaborative State library version.
     * <p/>
     * Created on Feb 4, 2011 by depryf
     * @return the Collaborative State library version
     */
    @ContextFunctionDocAnnotation(desc = "Returns the CS version as provided by the CS library.", example = "def csVersion = Functions.getCsVersion()")
    public String getCsVersion() {
        if (_csStaging == null)
            return null;

        return _csStaging.getVersion().replace(".", "");
    }

    /**
     * Returns the CS schema for the passed input field.
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologicTypeIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * </ul>
     * <p/>
     * The site and the histology cannot be null (or the resulting schema will be null); the SSF25 can be null or missing.
     * <p/>
     * Created on Jan 19, 2010 by depryf
     * @param input input map containing the required "primarySite" and "histologicTypeIcdO3" keys and the optional "csSiteSpecificFactor25" key
     * @return the corresponding CS schema name; null if not found
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs",
            desc = "Returns the CS schema name corresponding to the inputs; those inputs must contain the keys 'primarySite' and 'histologicTypeIcdO3'; they can optionally contain the key 'csSiteSpecificFactor25'. Returns null if the schema can't be determined.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologicTypeIcdO3' : record.histologicTypeIcdO3\n]\n\ndef schemaName = Functions.getCsSchemaName(inputs)")
    public String getCsSchemaName(Map<String, String> input) {
        if (_csStaging == null)
            return null;

        Schema schema = getCsStagingSchema(input);
        return schema == null ? null : schema.getName();
    }

    /**
     * Returns the CS schema ID for the passed input fields.
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologicTypeIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * </ul>
     * <p/>
     * The site and the histology cannot be null (or the resulting schema will be null); the SSF25 can be null or missing.
     * <p/>
     * Created on Jan 19, 2010 by depryf
     * @param input input map containing the required "primarySite" and "histologicTypeIcdO3" keys and the optional "csSiteSpecificFactor25" key
     * @return the corresponding CS schema ID; null if not found
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs",
            desc = "Returns the CS schema ID corresponding to the inputs; those inputs must contain the keys 'primarySite' and 'histologicTypeIcdO3'; they can optionally contain the key 'csSiteSpecificFactor25'. Returns null if the schema can't be determined.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologicTypeIcdO3' : record.histologicTypeIcdO3\n]\n\ndef schemaId = Functions.getCsSchemaId(inputs)")
    public String getCsSchemaId(Map<String, String> input) {
        if (_csStaging == null)
            return null;

        Schema schema = getCsStagingSchema(input);
        return schema == null ? null : schema.getId();
    }

    /**
     * Returns whether the passed value is an acceptable code for the input fields and the requested CS field.
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologicTypeIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * </ul>
     * <p/>
     * Created on Jan 22, 2010 by depryf
     * @param input input map containing the required "primarySite" and "histologicTypeIcdO3" keys and the optional "csSiteSpecificFactor25" key
     * @param field requested CS field
     * @param valueToCheck the value to check
     * @return true if the value is acceptable, false otherwise
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "field", param2 = "field NAACCR XML ID", paramName3 = "valueToCheck", param3 = "value to validate",
            desc = "Returns true if the provided value is valid for the CS schema corresponding to the inputs and the CS field, false otherwise. The inputs must contain the keys 'primarySite' and 'histologicTypeIcdO3'; they can optionaly contain the key 'csSiteSpecificFactor25'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologicTypeIcdO3' : record.histologicTypeIcdO3\n]\n\nreturn Functions.isAcceptableCsCode(inputs, 'csSiteSpecificFactor1', record.csSiteSpecificFactor1)")
    public boolean isAcceptableCsCode(Map<String, String> input, String field, String valueToCheck) {
        if (_csStaging == null || input == null || field == null || valueToCheck == null)
            return false;

        Schema schema = getCsStagingSchema(input);
        if (schema == null)
            return false;

        Input inputField = schema.getInputs().stream().filter(i -> field.equals(i.getNaaccrXmlId())).findFirst().orElse(null);
        if (inputField == null)
            return false;

        return _csStaging.isCodeValid(schema.getId(), inputField.getKey(), valueToCheck);

    }

    /**
     * Returns whether the passed value is an obsolete code for the input fields and the requested CS field.
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologicTypeIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * </ul>
     * <p/>
     * Created on Jan 22, 2010 by depryf
     * @param input input map containing the required "primarySite" and "histologicTypeIcdO3" keys and the optional "csSiteSpecificFactor25" key
     * @param field requested CS field
     * @param valueToCheck the value to check
     * @return true if the value is obsolete, false otherwise
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "field", param2 = "field NAACCR XML ID", paramName3 = "valueToCheck", param3 = "value to check",
            desc = "Returns true if the provided value is obsolete for the CS schema corresponding to the inputs and the CS field, false otherwise. The value is obsolete if its description in the CS table starts with OBSOLETE. The inputs must contain the keys 'primarySite' and 'histologicTypeIcdO3'; they can optionaly contain the key 'csSiteSpecificFactor25'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologicTypeIcdO3' : record.histologicTypeIcdO3\n]\n\nreturn Functions.isObsoleteCsCode(inputs, 'csSiteSpecificFactor1', record.csSiteSpecificFactor1)")
    public boolean isObsoleteCsCode(Map<String, String> input, String field, String valueToCheck) {
        if (_csStaging == null || input == null || field == null || valueToCheck == null)
            return false;

        String description = getDescriptionForCode(input, field, valueToCheck);
        return description != null && description.trim().toUpperCase().startsWith("OBSOLETE");

    }

    /**
     * Returns whether the passed value is an obsolete code for the input fields and the requested CS field.
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologicTypeIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * </ul>
     * <p/>
     * Created on Jan 22, 2010 by depryf
     * @param input input map containing the required "primarySite" and "histologicTypeIcdO3" keys and the optional "csSiteSpecificFactor25" key
     * @param field requested CS field
     * @param valueToCheck the value to check
     * @return true if the value is obsolete, false otherwise
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "field", param2 = "field NAACCR XML ID", paramName3 = "valueToCheck", param3 = "value to check",
            desc = "Returns the reason why a particular code is obsolete.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologicTypeIcdO3' : record.histologicTypeIcdO3\n]\n\nreturn Functions.getCsObsoleteReason(inputs, 'csSiteSpecificFactor1', record.csSiteSpecificFactor1)")
    public String getCsObsoleteReason(Map<String, String> input, String field, String valueToCheck) {
        if (_csStaging == null || input == null || field == null || valueToCheck == null)
            return null;

        String description = getDescriptionForCode(input, field, valueToCheck);
        if (description == null)
            return null;

        // this is very inefficient, we should consider caching it...
        String desc = description.trim().toUpperCase();
        for (Map.Entry<String, String> entry : CSTAGE_OBSOLETE_REASONS.entrySet())
            if (desc.startsWith(entry.getKey()))
                return entry.getValue();

        return null;
    }

    /**
     * Returns true if the passed SSF index is required for SEER for the schema corresponding to the passed input.
     * <p/>
     * Required means either already-collected, needed-for-staging or clinically-significant.
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologicTypeIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * </ul>
     * <p/>
     * Created on Feb 23, 2010 by depryf
     * @param input input map containing the required "primarySite" and "histologicTypeIcdO3" keys and the optional "csSiteSpecificFactor25" key
     * @param ssfIndex requested SSF index
     * @return true if the index is required, false otherwise
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "ssfIndex", param2 = "site specific factor index (Integer)",
            desc = "Returns true if the passed Site Specific Factor index is required for SEER for the schema corresponding to the passed input, "
                    + "false otherwise. Required means either 'already-collected', 'needed-for-staging' or 'clinically significant'. The inputs must contain the keys 'primarySite' and 'histologicTypeIcdO3'; they can optionaly contain the key 'csSiteSpecificFactor25'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologicTypeIcdO3' : record.histologicTypeIcdO3\n]\n\nreturn Functions.isRequiredCsCode(inputs, 1)")
    public boolean isRequiredCsCode(Map<String, String> input, Integer ssfIndex) {
        if (_csStaging == null || input == null || ssfIndex == null)
            return false;

        Schema schema = getCsStagingSchema(input);
        if (schema == null)
            return false;

        Input schemaInput = schema.getInputMap().get("ssf" + ssfIndex);
        if (schemaInput == null)
            return false;

        return schemaInput.getUsedForStaging() || checkMetaData(input, schemaInput, CSTAGE_TAG_ALREADY_COLLECTED_SEER, CSTAGE_TAG_CLINICALLY_SIGNIFICANT_SEER);
    }

    /**
     * Returns true if the passed SSF index is required (needed-for-staging) for SEER for the schema corresponding to the passed input.
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologicTypeIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * </ul>
     * <p/>
     * Created on Feb 23, 2010 by depryf
     * @param input input map containing the required "primarySite" and "histologicTypeIcdO3" keys and the optional "csSiteSpecificFactor25" key
     * @param ssfIndex requested SSF index
     * @return true if the index is required (needed for staging), false otherwise
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "ssfIndex", param2 = "site specific factor index (Integer)",
            desc = "Returns true if the passed Site Specific Factor index is required (needed-for-staging) for SEER for the schema corresponding to the passed input, "
                    + "false otherwise. The inputs must contain the keys 'primarySite' and 'histologicTypeIcdO3'; they can optionaly contain the key 'csSiteSpecificFactor25'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologicTypeIcdO3' : record.histologicTypeIcdO3\n]\n\nreturn Functions.isNeededForStagingCsCode(inputs, 1)")
    public boolean isNeededForStagingCsCode(Map<String, String> input, Integer ssfIndex) {
        if (_csStaging == null || input == null || ssfIndex == null)
            return false;

        Schema schema = getCsStagingSchema(input);
        if (schema == null)
            return false;

        Input schemaInput = schema.getInputMap().get("ssf" + ssfIndex);
        if (schemaInput == null)
            return false;

        return schemaInput.getUsedForStaging();
    }

    /**
     * Returns true if the passed SSF index is required (already-collected) for SEER for the schema corresponding to the passed input.
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologicTypeIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * </ul>
     * <p/>
     * Created on Feb 23, 2010 by depryf
     * @param input input map containing the required "primarySite" and "histologicTypeIcdO3" keys and the optional "csSiteSpecificFactor25" key
     * @param ssfIndex requested SSF index
     * @return true if the index is required (already collected), false otherwise
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "ssfIndex", param2 = "site specific factor index (Integer)",
            desc = "Returns true if the passed Site Specific Factor index is required (already-collected) for SEER for the schema corresponding to the passed input, "
                    + "false otherwise. The inputs must contain the keys 'primarySite' and 'histologicTypeIcdO3'; they can optionaly contain the key 'csSiteSpecificFactor25'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologicTypeIcdO3' : record.histologicTypeIcdO3\n]\n\nreturn Functions.isAlreadyCollectedCsCode(inputs, 1)")
    public boolean isAlreadyCollectedCsCode(Map<String, String> input, Integer ssfIndex) {
        if (_csStaging == null || input == null || ssfIndex == null)
            return false;

        Schema schema = getCsStagingSchema(input);
        if (schema == null)
            return false;

        Input schemaInput = schema.getInputMap().get("ssf" + ssfIndex);
        if (schemaInput == null)
            return false;

        return checkMetaData(input, schemaInput, CSTAGE_TAG_ALREADY_COLLECTED_SEER);
    }

    /**
     * Returns true if the passed SSF index is required (clinically-significant) for SEER for the schema corresponding to the passed input.
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologicTypeIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * </ul>
     * <p/>
     * Created on Feb 23, 2010 by depryf
     * @param input input map containing the required "primarySite" and "histologicTypeIcdO3" keys and the optional "csSiteSpecificFactor25" key
     * @param ssfIndex requested SSF index
     * @return true if the index is required (clinically significant), false otherwise
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "ssfIndex", param2 = "site specific factor index (Integer)",
            desc = "Returns true if the passed Site Specific Factor index is required (clinically-significant) for SEER for the schema corresponding to the passed input, "
                    + "false otherwise. The inputs must contain the keys 'primarySite' and 'histologicTypeIcdO3'; they can optionaly contain the key 'csSiteSpecificFactor25'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologicTypeIcdO3' : record.histologicTypeIcdO3\n]\n\nreturn Functions.isClinicallySignificantCsCode(inputs, 1)")
    public boolean isClinicallySignificantCsCode(Map<String, String> input, Integer ssfIndex) {
        if (_csStaging == null || input == null || ssfIndex == null)
            return false;

        Schema schema = getCsStagingSchema(input);
        if (schema == null)
            return false;

        Input schemaInput = schema.getInputMap().get("ssf" + ssfIndex);
        if (schemaInput == null)
            return false;

        return checkMetaData(input, schemaInput, CSTAGE_TAG_CLINICALLY_SIGNIFICANT_SEER);
    }

    /**
     * Returns true if the passed SSF index is required pre-2010 for SEER for the schema corresponding to the passed input.
     * <p/>
     * Required means either already-collected, needed-for-staging or clinically-significant.
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologicTypeIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * </ul>
     * <p/>
     * Created on Feb 23, 2010 by depryf
     * @param input input map containing the required "primarySite" and "histologicTypeIcdO3" keys and the optional "csSiteSpecificFactor25" key
     * @param ssfIndex requested SSF index
     * @return true if the index is required pre-2010, false otherwise
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "ssfIndex", param2 = "site specific factor index (Integer)",
            desc = "Returns true if the passed Site Specific Factor index is required (pre-2010) for SEER for the schema corresponding to the passed input, "
                    + "false otherwise. The inputs must contain the keys 'primarySite' and 'histologicTypeIcdO3'; they can optionaly contain the key 'csSiteSpecificFactor25'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologicTypeIcdO3' : record.histologicTypeIcdO3\n]\n\nreturn Functions.isRequiredPre2010CsCode(inputs, 1)")
    public boolean isRequiredPre2010CsCode(Map<String, String> input, Integer ssfIndex) {
        if (_csStaging == null || input == null || ssfIndex == null)
            return false;

        Schema schema = getCsStagingSchema(input);
        if (schema == null)
            return false;

        Input schemaInput = schema.getInputMap().get("ssf" + ssfIndex);
        if (schemaInput == null)
            return false;

        return checkMetaData(input, schemaInput, CSTAGE_TAG_REQUIRED_PRE_2010_SEER);
    }

    /**
     * Returns true if the passed SSF index is required for CoC for the schema corresponding to the passed input.
     * <p/>
     * Required means either already-collected, needed-for-staging or clinically-significant.
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologicTypeIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * </ul>
     * <p/>
     * Created on Feb 23, 2010 by depryf
     * @param input input map containing the required "primarySite" and "histologicTypeIcdO3" keys and the optional "csSiteSpecificFactor25" key
     * @param ssfIndex requested SSF index
     * @return true if the index is required, false otherwise
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "ssfIndex", param2 = "site specific factor index (Integer)",
            desc = "Returns true if the passed Site Specific Factor index is required for CoC for the schema corresponding to the passed input, "
                    + "false otherwise. Required means either 'already-collected', 'needed-for-staging' or 'clinically significant'. The inputs must contain the keys 'primarySite' and 'histologicTypeIcdO3'; they can optionaly contain the key 'csSiteSpecificFactor25'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologicTypeIcdO3' : record.histologicTypeIcdO3\n]\n\nreturn Functions.isRequiredCsCode(inputs, 1)")
    public boolean isCocRequiredCsCode(Map<String, String> input, Integer ssfIndex) {
        if (_csStaging == null || input == null || ssfIndex == null)
            return false;

        Schema schema = getCsStagingSchema(input);
        if (schema == null)
            return false;

        Input schemaInput = schema.getInputMap().get("ssf" + ssfIndex);
        if (schemaInput == null)
            return false;

        return schemaInput.getUsedForStaging() || checkMetaData(input, schemaInput, CSTAGE_TAG_ALREADY_COLLECTED_COC, CSTAGE_TAG_CLINICALLY_SIGNIFICANT_COC);
    }

    /**
     * Returns true if the passed SSF index is required (already-collected) for CoC for the schema corresponding to the passed input.
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologicTypeIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * </ul>
     * <p/>
     * Created on Feb 23, 2010 by depryf
     * @param input input map containing the required "primarySite" and "histologicTypeIcdO3" keys and the optional "csSiteSpecificFactor25" key
     * @param ssfIndex requested SSF index
     * @return true if the index is required (already collected), false otherwise
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "ssfIndex", param2 = "site specific factor index (Integer)",
            desc = "Returns true if the passed Site Specific Factor index is required (already-collected) for CoC for the schema corresponding to the passed input, false otherwise. The inputs must contain the keys 'primarySite' and 'histologicTypeIcdO3'; they can optionaly contain the key 'csSiteSpecificFactor25'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologicTypeIcdO3' : record.histologicTypeIcdO3\n]\n\nreturn Functions.isAlreadyCollectedCsCode(inputs, 1)")
    public boolean isCocAlreadyCollectedCsCode(Map<String, String> input, Integer ssfIndex) {
        if (_csStaging == null || input == null || ssfIndex == null)
            return false;

        Schema schema = getCsStagingSchema(input);
        if (schema == null)
            return false;

        Input schemaInput = schema.getInputMap().get("ssf" + ssfIndex);
        if (schemaInput == null)
            return false;

        return checkMetaData(input, schemaInput, CSTAGE_TAG_ALREADY_COLLECTED_COC);
    }

    /**
     * Returns true if the passed SSF index is required (needed-for-staging) for CoC for the schema corresponding to the passed input.
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologicTypeIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * </ul>
     * <p/>
     * Created on Feb 23, 2010 by depryf
     * @param input input map containing the required "primarySite" and "histologicTypeIcdO3" keys and the optional "csSiteSpecificFactor25" key
     * @param ssfIndex requested SSF index
     * @return true if the index is required (needed for staging), false otherwise
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "ssfIndex", param2 = "site specific factor index (Integer)",
            desc = "Returns true if the passed Site Specific Factor index is required (needed-for-staging) for CoC for the schema corresponding to the passed input, false otherwise. The inputs must contain the keys 'primarySite' and 'histologicTypeIcdO3'; they can optionaly contain the key 'csSiteSpecificFactor25'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologicTypeIcdO3' : record.histologicTypeIcdO3\n]\n\nreturn Functions.isNeededForStagingCsCode(inputs, 1)")
    public boolean isCocNeededForStagingCsCode(Map<String, String> input, Integer ssfIndex) {
        return isNeededForStagingCsCode(input, ssfIndex); // needed for staging is not agency-specific...
    }

    /**
     * Returns true if the passed SSF index is required (clinically-significant) for CoC for the schema corresponding to the passed input.
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologicTypeIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * </ul>
     * <p/>
     * Created on Feb 23, 2010 by depryf
     * @param input input map containing the required "primarySite" and "histologicTypeIcdO3" keys and the optional "csSiteSpecificFactor25" key
     * @param ssfIndex requested SSF index
     * @return true if the index is required (clinically significant), false otherwise
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "ssfIndex", param2 = "site specific factor index (Integer)",
            desc = "Returns true if the passed Site Specific Factor index is required (clinically-significant) for CoC for the schema corresponding to the passed input, false otherwise. The inputs must contain the keys 'primarySite' and 'histologicTypeIcdO3'; they can optionaly contain the key 'csSiteSpecificFactor25'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologicTypeIcdO3' : record.histologicTypeIcdO3\n]\n\nreturn Functions.isClinicallySignificantCsCode(inputs, 1)")
    public boolean isCocClinicallySignificantCsCode(Map<String, String> input, Integer ssfIndex) {
        if (_csStaging == null || input == null || ssfIndex == null)
            return false;

        Schema schema = getCsStagingSchema(input);
        if (schema == null)
            return false;

        Input schemaInput = schema.getInputMap().get("ssf" + ssfIndex);
        if (schemaInput == null)
            return false;

        return checkMetaData(input, schemaInput, CSTAGE_TAG_CLINICALLY_SIGNIFICANT_COC);
    }

    /**
     * Returns true if the passed SSF index is required pre-2010 for CoC for the schema corresponding to the passed input.
     * <p/>
     * Required means either already-collected, needed-for-staging or clinically-significant.
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologicTypeIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * </ul>
     * <p/>
     * Created on Feb 23, 2010 by depryf
     * @param input input map containing the required "primarySite" and "histologicTypeIcdO3" keys and the optional "csSiteSpecificFactor25" key
     * @param ssfIndex requested SSF index
     * @return true if the index is required pre-2010, false otherwise
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "ssfIndex", param2 = "site specific factor index (Integer)",
            desc = "Returns true if the passed Site Specific Factor index is required (pre-2010) for CoC for the schema corresponding to the passed input, false otherwise. The inputs must contain the keys 'primarySite' and 'histologicTypeIcdO3'; they can optionaly contain the key 'csSiteSpecificFactor25'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologicTypeIcdO3' : record.histologicTypeIcdO3\n]\n\nreturn Functions.isRequiredPre2010CsCode(inputs, 1)")
    public boolean isCocRequiredPre2010CsCode(Map<String, String> input, Integer ssfIndex) {
        if (_csStaging == null || input == null || ssfIndex == null)
            return false;

        Schema schema = getCsStagingSchema(input);
        if (schema == null)
            return false;

        Input schemaInput = schema.getInputMap().get("ssf" + ssfIndex);
        if (schemaInput == null)
            return false;

        return checkMetaData(input, schemaInput, CSTAGE_TAG_REQUIRED_PRE_2010_COC);
    }

    /**
     * Returns the TNM library version.
     * <p/>
     * Created on Feb 4, 2011 by depryf
     * @return the TNM library version
     */
    @ContextFunctionDocAnnotation(desc = "Returns the TNM version as provided by the TNM library.", example = "def tnmVersion = Functions.getTnmVersion()")
    public String getTnmVersion() {
        if (_tnmStaging == null)
            return null;

        return _tnmStaging.getVersion();
    }

    /**
     * Returns the TNM schema name for the given input, null if there is no schema
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologicTypeIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * <li>sex</li>
     * </ul>
     * <p/>
     * @param input input map containing the required "primarySite" and "histologicTypeIcdO3" keys and the optional "csSiteSpecificFactor25" or "sex" keys
     * @return the corresponding TNM schema name; null if not found
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs",
            desc = "Returns the TNM schema name corresponding to the inputs; those inputs must contain the keys 'primarySite' and 'histologicTypeIcdO3'; they can optionally contain the key 'csSiteSpecificFactor25' or 'sex'. Returns null if the schema can't be determined.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologicTypeIcdO3' : record.histologicTypeIcdO3\n]\n\ndef schemaName = Functions.getTnmSchemaName(inputs)")
    public String getTnmSchemaName(Map<String, String> input) {
        if (_tnmStaging == null)
            return null;

        Schema schema = getTnmStagingSchema(input);
        return schema == null ? null : schema.getName();
    }

    /**
     * Returns the TNM schema ID for the given input, null if there is no schema
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologicTypeIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * <li>sex</li>
     * </ul>
     * <p/>
     * @param input input map containing the required "primarySite" and "histologicTypeIcdO3" keys and the optional "csSiteSpecificFactor25" or "sex" keys
     * @return the corresponding TNM schema ID; null if not found
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs",
            desc = "Returns the TNM schema ID corresponding to the inputs; those inputs must contain the keys 'primarySite' and 'histologicTypeIcdO3'; they can optionally contain the key 'csSiteSpecificFactor25' or 'sex'. Returns null if the schema can't be determined.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologicTypeIcdO3' : record.histologicTypeIcdO3\n]\n\ndef schemaId = Functions.getTnmSchemaId(inputs)")
    public String getTnmSchemaId(Map<String, String> input) {
        if (_tnmStaging == null)
            return null;

        Schema schema = getTnmStagingSchema(input);
        return schema == null ? null : schema.getId();
    }

    /**
     * Returns true if the given value is valid for the given field with the schema corresponding to the passed input
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologicTypeIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * <li>sex</li>
     * </ul>
     * <p/>
     * @param input input map containing the required "primarySite" and "histologicTypeIcdO3" keys and the optional "csSiteSpecificFactor25" or "sex" keys
     * @param field requested TNM field
     * @param valueToCheck value to check
     * @return true if the value is acceptable, false otherwise
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "field", param2 = "field NAACCR XML ID", paramName3 = "valueToCheck", param3 = "value to validate",
            desc = "Returns true if the provided value is valid for the TNM schema corresponding to the inputs and the TNM field, false otherwise. The inputs must contain the keys 'primarySite' and 'histologicTypeIcdO3'; they can optionally contain the keys 'csSiteSpecificFactor25' and 'sex'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologicTypeIcdO3' : record.histologicTypeIcdO3\n]\n\nreturn Functions.isAcceptableTnmCode(inputs, 'csSiteSpecificFactor1', record.csSiteSpecificFactor1)")
    public boolean isAcceptableTnmCode(Map<String, String> input, String field, String valueToCheck) {
        if (_tnmStaging == null || input == null || field == null)
            return false;

        Schema schema = getTnmStagingSchema(input);
        if (schema == null)
            return false;

        Input inputField = schema.getInputs().stream().filter(i -> field.equals(i.getNaaccrXmlId())).findFirst().orElse(null);
        if (inputField == null)
            return false;

        return _tnmStaging.isCodeValid(schema.getId(), inputField.getKey(), valueToCheck);
    }

    /**
     * Returns true if the passed SSF index is required for SEER for the schema corresponding to the passed input.
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologicTypeIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * <li>sex</li>
     * </ul>
     * <p/>
     * @param input input map containing the required "primarySite" and "histologicTypeIcdO3" keys and the optional "csSiteSpecificFactor25" or "sex" keys
     * @param ssfIndex requested SSF index
     * @return true if the index is required for SEER, false otherwise
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "ssfIndex", param2 = "site specific factor index (Integer)",
            desc = "Returns true if the passed Site Specific Factor index is required for SEER for the schema corresponding to the passed input, "
                    + "false otherwise. Required means either 'seer-required' or 'needed-for-staging'. The inputs must contain the keys 'primarySite' and 'histologicTypeIcdO3'; they can optionaly contain the keys 'csSiteSpecificFactor25' or 'sex'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologicTypeIcdO3' : record.histologicTypeIcdO3\n]\n\nreturn Functions.isRequiredTnmCode(inputs, 1)")
    public boolean isRequiredTnmCode(Map<String, String> input, Integer ssfIndex) {
        if (_tnmStaging == null || input == null || ssfIndex == null)
            return false;

        Schema schema = getTnmStagingSchema(input);
        if (schema == null)
            return false;

        Input schemaInput = schema.getInputMap().get("ssf" + ssfIndex);
        if (schemaInput == null)
            return false;

        return schemaInput.getUsedForStaging() || checkMetaData(input, schemaInput, TNM_TAG_SEER_REQUIRED);
    }

    /**
     * Returns true if the passed SSF index is required (needed-for-staging) for SEER for the schema corresponding to the passed input.
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologicTypeIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * <li>sex</li>
     * </ul>
     * <p/>
     * @param input input map containing the required "primarySite" and "histologicTypeIcdO3" keys and the optional "csSiteSpecificFactor25" or "sex" keys
     * @param ssfIndex requested SSF index
     * @return true if the index is needed for staging, false otherwise
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "ssfIndex", param2 = "site specific factor index (Integer)",
            desc = "Returns true if the passed Site Specific Factor index is required (needed-for-staging) for SEER for the schema corresponding to the passed input, "
                    + "false otherwise. The inputs must contain the keys 'primarySite' and 'histologicTypeIcdO3'; they can optionaly contain the keys 'csSiteSpecificFactor25' or 'sex'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologicTypeIcdO3' : record.histologicTypeIcdO3\n]\n\nreturn Functions.isNeededForStagingTnmCode(inputs, 1)")
    public boolean isNeededForStagingTnmCode(Map<String, String> input, Integer ssfIndex) {
        if (_tnmStaging == null || input == null || ssfIndex == null)
            return false;

        Schema schema = getTnmStagingSchema(input);
        if (schema == null)
            return false;

        Input schemaInput = schema.getInputMap().get("ssf" + ssfIndex);
        if (schemaInput == null)
            return false;

        return schemaInput.getUsedForStaging();
    }

    /**
     * Returns true if the passed SSF index is required for CoC for the schema corresponding to the passed input.
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologicTypeIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * <li>sex</li>
     * </ul>
     * <p/>
     * @param input input map containing the required "primarySite" and "histologicTypeIcdO3" keys and the optional "csSiteSpecificFactor25" or "sex" keys
     * @param ssfIndex requested SSF index
     * @return true if the index is required for COC, false otherwise
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "ssfIndex", param2 = "site specific factor index (Integer)",
            desc = "Returns true if the passed Site Specific Factor index is required for COC for the schema corresponding to the passed input, "
                    + "false otherwise. Required means either 'coc-required' or 'needed-for-staging'. The inputs must contain the keys 'primarySite' and 'histologicTypeIcdO3'; they can optionaly contain the keys 'csSiteSpecificFactor25' or 'sex'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologicTypeIcdO3' : record.histologicTypeIcdO3\n]\n\nreturn Functions.isCocRequiredTnmCode(inputs, 1)")
    public boolean isCocRequiredTnmCode(Map<String, String> input, Integer ssfIndex) {
        if (_tnmStaging == null || input == null || ssfIndex == null)
            return false;

        Schema schema = getTnmStagingSchema(input);
        if (schema == null)
            return false;

        Input schemaInput = schema.getInputMap().get("ssf" + ssfIndex);
        if (schemaInput == null)
            return false;

        return schemaInput.getUsedForStaging() || checkMetaData(input, schemaInput, TNM_TAG_COC_REQUIRED);
    }

    /**
     * @return the EOD library version
     */
    @ContextFunctionDocAnnotation(desc = "Returns the EOD version as provided by the EOD library.", example = "def eodVersion = Functions.getEodVersion()")
    public String getEodVersion() {
        if (_eodStaging == null)
            return null;

        return _eodStaging.getVersion();
    }

    /**
     * Returns the EOD schema name for the given input, null if there is no schema
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologicTypeIcdO3</li>
     * <li>sex</li>
     * <li>schemaDiscriminator1</li>
     * <li>schemaDiscriminator2</li>
     * </ul>
     * <p/>
     * @param input input map containing the required "primarySite" and "histologicTypeIcdO3" keys and the optional "schemaDiscriminator1", "schemaDiscriminator2", or "sex" keys
     * @return the corresponding EOD schema name; null if not found
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs",
            desc = "Returns the EOD schema name corresponding to the inputs; those inputs must contain the keys 'primarySite' and 'histologicTypeIcdO3'; they can optionally contain the key 'schemaDiscriminator1', 'schemaDiscriminator2 ', or 'sex'. Returns null if the schema can't be determined.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologicTypeIcdO3' : record.histologicTypeIcdO3\n]\n\ndef schemaName = Functions.getEodSchemaName(inputs)")
    public String getEodSchemaName(Map<String, String> input) {
        if (_eodStaging == null)
            return null;

        Schema schema = getEodStagingSchema(input);
        return schema == null ? null : schema.getName();
    }

    /**
     * Returns the EOD schema ID for the given input, null if there is no schema
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologicTypeIcdO3</li>
     * <li>sex</li>
     * <li>schemaDiscriminator1</li>
     * <li>schemaDiscriminator2</li>
     * </ul>
     * <p/>
     * @param input input map containing the required "primarySite" and "histologicTypeIcdO3" keys and the optional "schemaDiscriminator1", "schemaDiscriminator2", or "sex" keys
     * @return the corresponding EOD schema ID; null if not found
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs",
            desc = "Returns the TNM schema ID corresponding to the inputs; those inputs must contain the keys 'primarySite' and 'histologicTypeIcdO3'; they can optionally contain the keys 'schemaDiscriminator1', 'schemaDiscriminator2', or 'sex'. Returns null if the schema can't be determined.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologicTypeIcdO3' : record.histologicTypeIcdO3\n]\n\ndef schemaId = Functions.getEodSchemaId(inputs)")
    public String getEodSchemaId(Map<String, String> input) {
        if (_eodStaging == null)
            return null;

        Schema schema = getEodStagingSchema(input);
        return schema == null ? null : schema.getId();
    }

    /**
     * Returns true if the given value is valid for the given field with the schema corresponding to the passed input
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologicTypeIcdO3</li>
     * <li>sex</li>
     * <li>schemaDiscriminator1</li>
     * <li>schemaDiscriminator2</li>
     * </ul>
     * <p/>
     * @param input input map containing the required "primarySite" and "histologicTypeIcdO3" keys and the optional "schemaDiscriminator1", "schemaDiscriminator2", or "sex" keys
     * @param field requested EOD field
     * @param valueToCheck value to check
     * @return true if the value is acceptable, false otherwise
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "field", param2 = "field NAACCR XML ID", paramName3 = "valueToCheck", param3 = "value to validate",
            desc = "Returns true if the provided value is valid for the EOD schema corresponding to the inputs and the EOD field, false otherwise. The inputs must contain the keys 'primarySite' and 'histologicTypeIcdO3'; they can optionally contain the keys 'schemaDiscriminator1', 'schemaDiscriminator2', or 'sex'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologicTypeIcdO3' : record.histologicTypeIcdO3\n]\n\nreturn Functions.isAcceptableEodCode(inputs, 'eodPrimaryTumor', record.eodPrimaryTumor)")
    public boolean isAcceptableEodCode(Map<String, String> input, String field, String valueToCheck) {
        if (_eodStaging == null || input == null || field == null)
            return false;

        Schema schema = getEodStagingSchema(input);
        if (schema == null)
            return false;

        Input inputField = schema.getInputs().stream().filter(i -> field.equals(i.getNaaccrXmlId())).findFirst().orElse(null);
        if (inputField == null)
            return false;

        return _eodStaging.isCodeValid(schema.getId(), inputField.getKey(), valueToCheck);
    }

    /**
     * Returns true if the passed EOD field is required for SEER for the schema corresponding to the passed input.
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologicTypeIcdO3</li>
     * <li>sex</li>
     * <li>schemaDiscriminator1</li>
     * <li>schemaDiscriminator2</li>
     * </ul>
     * <p/>
     * @param input input map containing the required "primarySite" and "histologicTypeIcdO3" keys and the optional "schemaDiscriminator1", "schemaDiscriminator2", or "sex" keys
     * @param field requested EOD field
     * @return true if the field is required for SEER, false otherwise
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "field", param2 = "field NAACCR XML ID",
            desc = "Returns true if the passed EOD field is required for SEER for the schema corresponding to the passed input, "
                    + "false otherwise. Required means either 'seer-required' or 'needed-for-staging'. The inputs must contain the keys 'primarySite' and 'histologicTypeIcdO3'; they can optionaly contain the keys 'schemaDiscriminator1', 'schemaDiscriminator2', or 'sex'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologicTypeIcdO3' : record.histologicTypeIcdO3\n]\n\nreturn Functions.isRequiredEodField(inputs, ''eodPrimaryTumor')")
    public boolean isRequiredEodField(Map<String, String> input, String field) {
        if (_eodStaging == null || input == null || field == null)
            return false;

        Schema schema = getEodStagingSchema(input);
        if (schema == null)
            return false;

        Input schemaInput = schema.getInputs().stream().filter(i -> field.equals(i.getNaaccrXmlId())).findFirst().orElse(null);
        if (schemaInput == null)
            return false;

        return schemaInput.getUsedForStaging() || checkMetaData(input, schemaInput, EOD_TAG_SEER_REQUIRED);
    }

    /**
     * Returns true if the passed EODo field is required (needed-for-staging) for SEER for the schema corresponding to the passed input.
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologicTypeIcdO3</li>
     * <li>sex</li>
     * <li>schemaDiscriminator1</li>
     * <li>schemaDiscriminator2</li>
     * </ul>
     * <p/>
     * @param input input map containing the required "primarySite" and "histologicTypeIcdO3" keys and the optional "schemaDiscriminator1", "schemaDiscriminator2", or "sex" keys
     * @param field requested EOD field
     * @return true if the field is needed for staging, false otherwise
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "field", param2 = "field NAACCR XML ID",
            desc = "Returns true if the passed EOD field is required (needed-for-staging) for SEER for the schema corresponding to the passed input, "
                    + "false otherwise. The inputs must contain the keys 'primarySite' and 'histologicTypeIcdO3'; they can optionally contain the keys 'schemaDiscriminator1', 'schemaDiscriminator2', or 'sex'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologicTypeIcdO3' : record.histologicTypeIcdO3\n]\n\nreturn Functions.isNeededForStagingEodField(inputs, 'eodPrimaryTumor')")
    public boolean isNeededForStagingEodField(Map<String, String> input, String field) {
        if (_eodStaging == null || input == null || field == null)
            return false;

        Schema schema = getEodStagingSchema(input);
        if (schema == null)
            return false;

        Input schemaInput = schema.getInputs().stream().filter(i -> field.equals(i.getNaaccrXmlId())).findFirst().orElse(null);
        if (schemaInput == null)
            return false;

        return schemaInput.getUsedForStaging();
    }

    /**
     * Returns true if the passed EOD field is required for COC for the schema corresponding to the passed input.
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologicTypeIcdO3</li>
     * <li>sex</li>
     * <li>schemaDiscriminator1</li>
     * <li>schemaDiscriminator2</li>
     * </ul>
     * <p/>
     * @param input input map containing the required "primarySite" and "histologicTypeIcdO3" keys and the optional "schemaDiscriminator1", "schemaDiscriminator2", or "sex" keys
     * @param field requested EOD field
     * @return true if the field is required for COC, false otherwise
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "field", param2 = "field NAACCR XML ID",
            desc = "Returns true if the passed EOD field is required for COC for the schema corresponding to the passed input, "
                    + "false otherwise. Required means either 'coc-required' or 'needed-for-staging'. The inputs must contain the keys 'primarySite' and 'histologicTypeIcdO3'; they can optionally contain the keys 'schemaDiscriminator1', 'schemaDiscriminator2', or 'sex'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologicTypeIcdO3' : record.histologicTypeIcdO3\n]\n\nreturn Functions.isCocRequiredEodField(inputs, 'eodPrimaryTumor')")
    public boolean isCocRequiredEodField(Map<String, String> input, String field) {
        if (_eodStaging == null || input == null || field == null)
            return false;

        Schema schema = getEodStagingSchema(input);
        if (schema == null)
            return false;

        Input schemaInput = schema.getInputs().stream().filter(i -> field.equals(i.getNaaccrXmlId())).findFirst().orElse(null);
        if (schemaInput == null)
            return false;

        return schemaInput.getUsedForStaging() || checkMetaData(input, schemaInput, EOD_TAG_COC_REQUIRED);
    }

    /**
     * Helper method to get the schema from an input line (using standard NAACCR properties).
     * <br/>
     * I am making this method public because it's useful in many other places in the code...
     * @param input input fields (standard NAACCR properties)
     * @return corresponding schema, maybe null
     */
    public Schema getCsStagingSchema(Map<String, String> input) {
        if (_csStaging == null || input == null)
            return null;

        String site = input.get(CSTAGE_INPUT_PROP_SITE);
        String hist = input.get(CSTAGE_INPUT_PROP_HIST);
        String ssf25 = input.get(CSTAGE_INPUT_PROP_DISC);

        SchemaLookup lkup = new SchemaLookup(site, hist);
        lkup.setInput("ssf25", ssf25);

        List<Schema> schemas = _csStaging.lookupSchema(lkup);
        if (schemas.size() == 1)
            return _csStaging.getSchema(schemas.get(0).getId());

        return null;
    }

    /**
     * Helper method to get the TNM schema from an input line (using standard NAACCR properties).
     * <br/>
     * I am making this method public because it's useful in many other places in the code...
     * @param input input fields (standard NAACCR properties)
     * @return corresponding schema, maybe null
     */
    public Schema getTnmStagingSchema(Map<String, String> input) {
        if (_tnmStaging == null || input == null)
            return null;

        String site = input.get(TNM_INPUT_PROP_SITE);
        String hist = input.get(TNM_INPUT_PROP_HIST);
        String ssf25 = input.get(TNM_INPUT_PROP_SSF25);
        String sex = input.get(TNM_INPUT_PROP_SEX);

        SchemaLookup lkup = new SchemaLookup(site, hist);
        lkup.setInput("ssf25", ssf25);
        lkup.setInput("sex", sex);

        List<Schema> schemas = _tnmStaging.lookupSchema(lkup);
        if (schemas.size() == 1)
            return _tnmStaging.getSchema(schemas.get(0).getId());

        return null;
    }

    /**
     * Helper method to get the EOD schema from an input line (using standard NAACCR properties).
     * <br/>
     * I am making this method public because it's useful in many other places in the code...
     * @param input input fields (standard NAACCR properties)
     * @return corresponding schema, maybe null
     */
    public Schema getEodStagingSchema(Map<String, String> input) {
        if (_eodStaging == null || input == null)
            return null;

        String site = input.get(EOD_INPUT_PROP_SITE);
        String hist = input.get(EOD_INPUT_PROP_HIST);
        String disc1 = input.get(EOD_INPUT_PROP_DISC_1);
        String disc2 = input.get(EOD_INPUT_PROP_DISC_2);
        String sex = input.get(EOD_INPUT_PROP_SEX);
        String behav = input.get(EOD_INPUT_PROP_BEHAV);
        String dxYear = input.get(EOD_INPUT_PROP_DX_YEAR);

        SchemaLookup lkup = new SchemaLookup(site, hist);
        lkup.setInput("discriminator_1", disc1);
        lkup.setInput("discriminator_2", disc2);
        lkup.setInput("sex", sex);
        lkup.setInput("behavior", behav);
        lkup.setInput("year_dx", dxYear);

        List<Schema> schemas = _eodStaging.lookupSchema(lkup);
        if (schemas.size() == 1)
            return _eodStaging.getSchema(schemas.get(0).getId());

        return null;
    }

    /**
     * Helper method to get the schema from a schema number (as used in the old cstage DLL).
     * <br/>
     * @param schemaNumber schema number
     * @return corresponding schema, maybe null
     */
    private Schema getCsStagingSchema(int schemaNumber) {
        if (_csStaging == null || schemaNumber == -1)
            return null;

        return _csStaging.getSchema(_csSchemaIdByNumber.get(schemaNumber));
    }

    /**
     * Helper method to get the description of the row corresponding to the requested code.
     * <br/>
     * The method assumes there is only one description column (if there is several, it will only consider the first one).
     * @param input input input fields (standard NAACCR properties)
     * @param naaccrXmlId field to check (to know which table to use)
     * @return corresponding description, maybe null
     */
    private String getDescriptionForCode(Map<String, String> input, String naaccrXmlId, String code) {
        if (_csStaging == null || input == null || code == null)
            return null;

        Schema schema = getCsStagingSchema(input);
        if (schema == null)
            return null;

        Input schemaInput = schema.getInputs().stream().filter(i -> naaccrXmlId.equals(i.getNaaccrXmlId())).findFirst().orElse(null);
        if (schemaInput == null)
            return null;

        Table table = _csStaging.getTable(schemaInput.getTable());
        if (table == null)
            return null;

        int colIndex = -1;
        for (int i = 0; i < table.getColumnDefinitions().size(); i++) {
            if (table.getColumnDefinitions().get(i).getType() == ColumnDefinition.ColumnType.DESCRIPTION) {
                colIndex = i;
                break;
            }
        }
        if (colIndex == -1)
            return null;

        Integer rowIndex = _csStaging.findMatchingTableRow(table.getId(), schemaInput.getKey(), code);
        if (rowIndex == null)
            return null;

        return table.getRawRows().get(rowIndex).get(colIndex);
    }

    protected int getCsNumSchemas() {
        if (_csStaging == null)
            return -1;

        return _csStaging.getSchemaIds().size();
    }

    protected int getCsSchemaNumber(Map<String, String> input) {
        if (_csStaging == null)
            return -1;

        Schema schema = getCsStagingSchema(input);
        return schema == null || schema.getSchemaNum() == null ? -1 : schema.getSchemaNum();
    }

    protected String getCsSchemaName(int schemaNum) {
        if (_csStaging == null)
            return null;

        Schema schema = getCsStagingSchema(schemaNum);
        return schema == null ? null : schema.getName();
    }

    protected boolean isAcceptableCsCode(int schemaNumber, int tableNumber, String valueToCheck) {
        if (_csStaging == null)
            return false;

        Schema schema = getCsStagingSchema(schemaNumber);
        return schema != null && _csStaging.isCodeValid(schema.getId(), CSTAGE_TABLE_NUMBERS.get(tableNumber), valueToCheck);
    }

    protected boolean checkMetaData(Map<String, String> input, Input schemaInput, String... tags) {
        if (schemaInput == null || schemaInput.getMetadata() == null || schemaInput.getMetadata().isEmpty())
            return false;

        for (Metadata data : schemaInput.getMetadata()) {
            if (ArrayUtils.contains(tags, data.getName())) {
                if (data.getStart() == null && data.getEnd() == null)
                    return true;

                Integer inputDxYear = extractDxYear(input);

                return inputDxYear != null && (data.getStart() == null || inputDxYear >= data.getStart()) && (data.getEnd() == null || inputDxYear <= data.getEnd());
            }
        }

        return false;
    }

    protected Integer extractDxYear(Map<String, String> input) {
        if (input == null)
            return null;

        String val = input.get(CSTAGE_INPUT_PROP_DX_DATE_YEAR);
        if (StringUtils.isBlank(val)) {
            String fullVal = input.get(CSTAGE_INPUT_PROP_DX_DATE);
            if (fullVal != null && fullVal.length() >= 4)
                val = fullVal.substring(0, 4);
        }

        return NumberUtils.isDigits(val) ? Integer.valueOf(val) : null;
    }

    /**
     * Expands the keys of the passed map
     * <br/><br/>
     * I know this method are not staging-specific but I would like to limit its visibility and eventually remove it...
     * <p/>
     * Created on Nov 16, 2007 by depryf
     * @param map map to expand
     * @return new expanded map
     */
    @SuppressWarnings("unchecked")
    @ContextFunctionDocAnnotation(paramName1 = "map", param1 = "Map to expand", desc = "Expands the keys of the provided map, replacing all the ranges by their actual values",
            example = "Functions.expandKeys ( [ 1..3 : '1' ] ) returns [ 1 : '1', 2 : '1', 3 : '1' ]")
    public Map<Object, Object> expandKeys(Map<Object, Object> map) {
        Map<Object, Object> result = new HashMap<>();

        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            Object key = entry.getKey();
            Object val = entry.getValue();

            if (key instanceof List) {
                List<Object> list = (List<Object>)key;
                for (Object obj : list) {
                    if (obj instanceof IntRange)
                        for (int i = ((IntRange)obj).getFromInt(); i <= ((IntRange)obj).getToInt(); i++)
                            result.put(i, val);
                    else if (obj instanceof String && ((String)obj).contains("..")) {
                        String[] parts = StringUtils.split((String)obj, "..");
                        if (parts.length != 2)
                            throw new IllegalStateException("Bad range: " + obj);
                        Integer low = asInt(parts[0]);
                        Integer high = asInt(parts[1]);
                        if (low == null || high == null || low >= high)
                            throw new IllegalStateException("Bad range: " + obj);
                        for (int i = low; i <= high; i++)
                            result.put(String.valueOf(i), val);
                    }
                    else
                        result.put(obj, val);
                }
            }
            else if (key instanceof String && ((String)key).contains("..")) {
                String[] parts = StringUtils.split((String)key, "..");
                if (parts.length != 2)
                    throw new IllegalStateException("Bad range: " + key);
                Integer low = asInt(parts[0]);
                Integer high = asInt(parts[1]);
                if (low == null || high == null || low >= high)
                    throw new IllegalStateException("Bad range: " + key);
                for (int i = low; i <= high; i++)
                    result.put(String.valueOf(i), val);
            }
            else {
                result.put(key, val);
            }
        }

        return result;
    }

    /**
     * Expands the values of the passed list
     * <br/><br/>
     * I know this method are not staging-specific but I would like to limit its visibility and eventually remove it...
     * <p/>
     * Created on Nov 16, 2007 by depryf
     * @param list list to expand
     * @return new expanded list
     */
    @ContextFunctionDocAnnotation(paramName1 = "list", param1 = "List to expand", desc = "Expands the values of the provided list, replacing all the ranges by their actual values",
            example = "Functions.expandList ( [ 1..3, 4, 5..6 ] ) returns [ 1, 2, 3, 4, 5, 6]")
    public List<Object> expandList(List<Object> list) {
        List<Object> result = new ArrayList<>();

        for (Object obj : list) {
            if (obj instanceof IntRange)
                for (int i = ((IntRange)obj).getFromInt(); i <= ((IntRange)obj).getToInt(); i++)
                    result.add(i);
            else if (obj instanceof String && ((String)obj).contains("..")) {
                String[] parts = StringUtils.split((String)obj, "..");
                if (parts.length != 2)
                    throw new IllegalStateException("Bad range: " + obj);
                Integer low = asInt(parts[0]);
                Integer high = asInt(parts[1]);
                if (low == null || high == null || low >= high)
                    throw new IllegalStateException("Bad range: " + obj);
                for (int i = low; i <= high; i++)
                    result.add(String.valueOf(i));
            }
            else
                result.add(obj);
        }

        return result;
    }

    /**
     * Returns the corresponding SSF25 value for the given sex value
     */
    public String getSsf25FromSex(String ssf25, String sex, String hist, String dxYear, String tnmSchemaId) {
        return ValidationStagingUtils.getSsf25FromSex(ssf25, sex, hist, dxYear, tnmSchemaId);
    }
}
