/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.validation.functions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import groovy.lang.IntRange;

import com.imsweb.decisionengine.ColumnDefinition;
import com.imsweb.staging.Staging;
import com.imsweb.staging.cs.CsDataProvider;
import com.imsweb.staging.cs.CsSchemaLookup;
import com.imsweb.staging.entities.StagingSchema;
import com.imsweb.staging.entities.StagingSchemaInput;
import com.imsweb.staging.entities.StagingTable;
import com.imsweb.staging.tnm.TnmDataProvider;
import com.imsweb.staging.tnm.TnmSchemaLookup;
import com.imsweb.staging.tnm.TnmStagingData;
import com.imsweb.validation.ValidatorContextFunctions;
import com.imsweb.validation.shared.ContextFunctionDocAnnotation;

/**
 * Staging-related helper methods made available to the edits. If you want to execute edits that call some staging utility methods, you need to initialize
 * the context functions with an instance of this class. Otherwise you should just use the ValidatorContextFunctions one.
 */
public class StagingContextFunctions extends ValidatorContextFunctions {

    // the standard NAACCR properties used when getting a CStage schema
    public static final String CSTAGE_INPUT_PROP_SITE = "primarySite";
    public static final String CSTAGE_INPUT_PROP_HIST = "histologyIcdO3";
    public static final String CSTAGE_INPUT_PROP_DISC = "csSiteSpecificFactor25";
    public static final String CSTAGE_PROP_DX_YEAR = "dateOfDiagnosisYear";

    // this maps the CS fields used by the edits (NAACCR property names) to the input keys used in the Staging framework
    public static final Map<String, String> CSTAGE_FIELDS = new HashMap<>();

    static {
        CSTAGE_FIELDS.put("csTumorSize", "size");
        CSTAGE_FIELDS.put("csExtension", "extension");
        CSTAGE_FIELDS.put("csTumorSizeExtEval", "extension_eval");
        CSTAGE_FIELDS.put("csLymphNodes", "nodes");
        CSTAGE_FIELDS.put("csLymphNodesEval", "nodes_eval");
        CSTAGE_FIELDS.put("regionalNodesPositive", "nodes_pos");
        CSTAGE_FIELDS.put("regionalNodesExamined", "nodes_exam");
        CSTAGE_FIELDS.put("csMetsAtDx", "mets");
        CSTAGE_FIELDS.put("csMetsEval", "mets_eval");
        CSTAGE_FIELDS.put("csSiteSpecFact1", "ssf1");
        CSTAGE_FIELDS.put("csSiteSpecFact2", "ssf2");
        CSTAGE_FIELDS.put("csSiteSpecFact3", "ssf3");
        CSTAGE_FIELDS.put("csSiteSpecFact4", "ssf4");
        CSTAGE_FIELDS.put("csSiteSpecFact5", "ssf5");
        CSTAGE_FIELDS.put("csSiteSpecFact6", "ssf6");
        CSTAGE_FIELDS.put("csSiteSpecFact7", "ssf7");
        CSTAGE_FIELDS.put("csSiteSpecFact8", "ssf8");
        CSTAGE_FIELDS.put("csSiteSpecFact9", "ssf9");
        CSTAGE_FIELDS.put("csSiteSpecFact10", "ssf10");
        CSTAGE_FIELDS.put("csSiteSpecFact11", "ssf11");
        CSTAGE_FIELDS.put("csSiteSpecFact12", "ssf12");
        CSTAGE_FIELDS.put("csSiteSpecFact13", "ssf13");
        CSTAGE_FIELDS.put("csSiteSpecFact14", "ssf14");
        CSTAGE_FIELDS.put("csSiteSpecFact15", "ssf15");
        CSTAGE_FIELDS.put("csSiteSpecFact16", "ssf16");
        CSTAGE_FIELDS.put("csSiteSpecFact17", "ssf17");
        CSTAGE_FIELDS.put("csSiteSpecFact18", "ssf18");
        CSTAGE_FIELDS.put("csSiteSpecFact19", "ssf19");
        CSTAGE_FIELDS.put("csSiteSpecFact20", "ssf20");
        CSTAGE_FIELDS.put("csSiteSpecFact21", "ssf21");
        CSTAGE_FIELDS.put("csSiteSpecFact22", "ssf22");
        CSTAGE_FIELDS.put("csSiteSpecFact23", "ssf23");
        CSTAGE_FIELDS.put("csSiteSpecFact24", "ssf24");
        CSTAGE_FIELDS.put("csSiteSpecFact25", "ssf25");
    }

    // this maps the table number of the old DLL to the input keys used in the Staging framework
    public static final Map<Integer, String> CSTAGE_TABLE_NUMBERS = new HashMap<>();

    static {
        CSTAGE_TABLE_NUMBERS.put(1, "size");
        CSTAGE_TABLE_NUMBERS.put(2, "extension");
        CSTAGE_TABLE_NUMBERS.put(3, "extension_eval");
        CSTAGE_TABLE_NUMBERS.put(4, "nodes");
        CSTAGE_TABLE_NUMBERS.put(5, "nodes_eval");
        CSTAGE_TABLE_NUMBERS.put(6, "nodes_pos");
        CSTAGE_TABLE_NUMBERS.put(7, "nodes_exam");
        CSTAGE_TABLE_NUMBERS.put(8, "mets");
        CSTAGE_TABLE_NUMBERS.put(9, "mets_eval");
        CSTAGE_TABLE_NUMBERS.put(10, "ssf1");
        CSTAGE_TABLE_NUMBERS.put(11, "ssf2");
        CSTAGE_TABLE_NUMBERS.put(12, "ssf3");
        CSTAGE_TABLE_NUMBERS.put(13, "ssf4");
        CSTAGE_TABLE_NUMBERS.put(14, "ssf5");
        CSTAGE_TABLE_NUMBERS.put(15, "ssf6");
        CSTAGE_TABLE_NUMBERS.put(16, "ssf7");
        CSTAGE_TABLE_NUMBERS.put(17, "ssf8");
        CSTAGE_TABLE_NUMBERS.put(18, "ssf9");
        CSTAGE_TABLE_NUMBERS.put(19, "ssf10");
        CSTAGE_TABLE_NUMBERS.put(20, "ssf11");
        CSTAGE_TABLE_NUMBERS.put(21, "ssf12");
        CSTAGE_TABLE_NUMBERS.put(22, "ssf13");
        CSTAGE_TABLE_NUMBERS.put(23, "ssf14");
        CSTAGE_TABLE_NUMBERS.put(24, "ssf15");
        CSTAGE_TABLE_NUMBERS.put(25, "ssf16");
        CSTAGE_TABLE_NUMBERS.put(26, "ssf17");
        CSTAGE_TABLE_NUMBERS.put(27, "ssf18");
        CSTAGE_TABLE_NUMBERS.put(28, "ssf19");
        CSTAGE_TABLE_NUMBERS.put(29, "ssf20");
        CSTAGE_TABLE_NUMBERS.put(30, "ssf21");
        CSTAGE_TABLE_NUMBERS.put(31, "ssf22");
        CSTAGE_TABLE_NUMBERS.put(32, "ssf23");
        CSTAGE_TABLE_NUMBERS.put(33, "ssf24");
        CSTAGE_TABLE_NUMBERS.put(34, "ssf25");
        // there are a few more tables, but I don't think they are used in the translated edits: 35 HIST7; 36 HIST; 37 AJCC7; 38 AJCC; 39 SEERSUM
    }

    // the obsolete reasons as defined in the CStage tables; edits use the key only...
    public static final Map<String, String> CSTAGE_OBSOLETE_REASONS = new LinkedHashMap<>();

    static {
        CSTAGE_OBSOLETE_REASONS.put("OBSOLETE DATA CONVERTED AND RETAINED V0200", "1");
        CSTAGE_OBSOLETE_REASONS.put("OBSOLETE DATA CONVERTED V0102", "2");
        CSTAGE_OBSOLETE_REASONS.put("OBSOLETE DATA CONVERTED V0104", "3");
        CSTAGE_OBSOLETE_REASONS.put("OBSOLETE DATA CONVERTED V0200", "4");
        CSTAGE_OBSOLETE_REASONS.put("OBSOLETE DATA RETAINED V0100", "5");
        CSTAGE_OBSOLETE_REASONS.put("OBSOLETE DATA RETAINED V0102", "6");
        CSTAGE_OBSOLETE_REASONS.put("OBSOLETE DATA RETAINED V0200", "7");
        CSTAGE_OBSOLETE_REASONS.put("OBSOLETE DATA REVIEWED AND CHANGED V0102", "8");
        CSTAGE_OBSOLETE_REASONS.put("OBSOLETE DATA REVIEWED AND CHANGED V0103", "9");
        CSTAGE_OBSOLETE_REASONS.put("OBSOLETE DATA REVIEWED AND CHANGED V0200", "10");
        CSTAGE_OBSOLETE_REASONS.put("OBSOLETE DATA CONVERTED V0203", "11");
        CSTAGE_OBSOLETE_REASONS.put("OBSOLETE DATA REVIEWED AND CHANGED V0203", "12");
        CSTAGE_OBSOLETE_REASONS.put("OBSOLETE DATA REVIEWED V0203", "13");
        CSTAGE_OBSOLETE_REASONS.put("OBSOLETE DATA RETAINED AND REVIEWED V0203", "14");
        CSTAGE_OBSOLETE_REASONS.put("OBSOLETE DATA RETAINED V0203", "15");
        CSTAGE_OBSOLETE_REASONS.put("OBSOLETE DATA RETAINED V0104", "16");
        CSTAGE_OBSOLETE_REASONS.put("OBSOLETE DATA RETAINED V0202", "17");
        CSTAGE_OBSOLETE_REASONS.put("OBSOLETE DATA RETAINED AND REVIEWED V0200", "18");
        CSTAGE_OBSOLETE_REASONS.put("OBSOLETE DATA CONVERTED V0204", "19");
        CSTAGE_OBSOLETE_REASONS.put("OBSOLETE DATA REVIEWED AND CHANGED V0204", "20");
        CSTAGE_OBSOLETE_REASONS.put("OBSOLETE DATA RETAINED AND REVIEWED V0204", "21");
        CSTAGE_OBSOLETE_REASONS.put("OBSOLETE DATA RETAINED V0204", "22");
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
    public static final String TNM_INPUT_PROP_HIST = "histologyIcdO3";
    public static final String TNM_INPUT_PROP_SSF25 = "csSiteSpecificFactor25";
    public static final String TNM_INPUT_PROP_SEX = "sex";

    // TNM metadata tags
    public static final String TNM_TAG_SEER_REQUIRED = "SEER_REQUIRED";
    public static final String TNM_TAG_COC_REQUIRED = "COC_REQUIRED";
    public static final String TNM_TAG_NPCR_REQUIRED = "NPCR_REQUIRED";
    public static final String TNM_TAG_CCCR_REQUIRED = "CCCR_REQUIRED";

    // this maps the TNM fields used by the edits (NAACCR property names) to the input keys used in the Staging framework
    public static final Map<String, String> TNM_FIELDS = new HashMap<>();

    static {
        TNM_FIELDS.put("seerPrimaryTumor", "seer_primary_tumor");
        TNM_FIELDS.put("seerRegionalNodes", "seer_nodes");
        TNM_FIELDS.put("seerMets", "seer_mets");
        TNM_FIELDS.put("tnmClinStageGroup", "clin_stage_group_direct");
        TNM_FIELDS.put("tnmPathStageGroup", "path_stage_group_direct");
        TNM_FIELDS.put("rxSummSystemicSurgSeq", "systemic_surg_seq");
        TNM_FIELDS.put("rxSummSurgRadSeq", "radiation_surg_seq");
        TNM_FIELDS.put("tnmClinT", "clin_t");
        TNM_FIELDS.put("tnmClinN", "clin_n");
        TNM_FIELDS.put("tnmClinM", "clin_m");
        TNM_FIELDS.put("tnmPathT", "path_t");
        TNM_FIELDS.put("tnmPathN", "path_n");
        TNM_FIELDS.put("tnmPathM", "path_m");
        TNM_FIELDS.put("csSiteSpecFact1", "ssf1");
        TNM_FIELDS.put("csSiteSpecFact2", "ssf2");
        TNM_FIELDS.put("csSiteSpecFact3", "ssf3");
        TNM_FIELDS.put("csSiteSpecFact4", "ssf4");
        TNM_FIELDS.put("csSiteSpecFact5", "ssf5");
        TNM_FIELDS.put("csSiteSpecFact6", "ssf6");
        TNM_FIELDS.put("csSiteSpecFact7", "ssf7");
        TNM_FIELDS.put("csSiteSpecFact8", "ssf8");
        TNM_FIELDS.put("csSiteSpecFact9", "ssf9");
        TNM_FIELDS.put("csSiteSpecFact10", "ssf10");
        TNM_FIELDS.put("csSiteSpecFact11", "ssf11");
        TNM_FIELDS.put("csSiteSpecFact12", "ssf12");
        TNM_FIELDS.put("csSiteSpecFact13", "ssf13");
        TNM_FIELDS.put("csSiteSpecFact14", "ssf14");
        TNM_FIELDS.put("csSiteSpecFact15", "ssf15");
        TNM_FIELDS.put("csSiteSpecFact16", "ssf16");
        TNM_FIELDS.put("csSiteSpecFact17", "ssf17");
        TNM_FIELDS.put("csSiteSpecFact18", "ssf18");
        TNM_FIELDS.put("csSiteSpecFact19", "ssf19");
        TNM_FIELDS.put("csSiteSpecFact20", "ssf20");
        TNM_FIELDS.put("csSiteSpecFact21", "ssf21");
        TNM_FIELDS.put("csSiteSpecFact22", "ssf22");
        TNM_FIELDS.put("csSiteSpecFact23", "ssf23");
        TNM_FIELDS.put("csSiteSpecFact24", "ssf24");
        TNM_FIELDS.put("csSiteSpecFact25", "ssf25");
    }

    // the staging instances to use for cstage- and tnm-related logic
    protected Staging _csStaging;
    protected Staging _tnmStaging;

    // Cached schema ID per schema number for CS
    protected Map<Integer, String> _csSchemaIdByNumber = new HashMap<>();

    /**
     * Default constructor
     */
    public StagingContextFunctions() {
        this(Staging.getInstance(CsDataProvider.getInstance(CsDataProvider.CsVersion.LATEST)), Staging.getInstance(TnmDataProvider.getInstance(TnmDataProvider.TnmVersion.LATEST)));
    }

    /**
     * Constructor.
     * @param csStaging a Staging instance responsible for all CStage-related logic
     * @param tnmStaging a Staging instance responsible for all TNM-related logic
     */
    public StagingContextFunctions(Staging csStaging, Staging tnmStaging) {
        _csStaging = csStaging;
        _tnmStaging = tnmStaging;

        if (_csStaging != null) {
            for (String schemaId : _csStaging.getSchemaIds()) {
                StagingSchema schema = _csStaging.getSchema(schemaId);
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
     * <li>histologyIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * </ul>
     * <p/>
     * The site and the histology cannot be null (or the resulting schema will be null); the SSF25 can be null or missing.
     * <p/>
     * Created on Jan 19, 2010 by depryf
     * @param input input map containing the required "primarySite" and "histologyIcdO3" keys and the optional "csSiteSpecificFactor25" key
     * @return the corresponding CS schema name; null if not found
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs",
            desc = "Returns the CS schema name corresponding to the inputs; those inputs must contain the keys 'primarySite' and 'histologyIcdO3'; they can optionally contain the key 'csSiteSpecificFactor25'. Returns null if the schema can't be determined.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologyIcdO3' : record.histologyIcdO3\n]\n\ndef schemaName = Functions.getCsSchemaName(inputs)")
    public String getCsSchemaName(Map<String, String> input) {
        if (_csStaging == null)
            return null;

        StagingSchema schema = getCsStagingSchema(input);
        return schema == null ? null : schema.getName();
    }

    /**
     * Returns the CS schema ID for the passed input fields.
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologyIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * </ul>
     * <p/>
     * The site and the histology cannot be null (or the resulting schema will be null); the SSF25 can be null or missing.
     * <p/>
     * Created on Jan 19, 2010 by depryf
     * @param input input map containing the required "primarySite" and "histologyIcdO3" keys and the optional "csSiteSpecificFactor25" key
     * @return the corresponding CS schema ID; null if not found
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs",
            desc = "Returns the CS schema ID corresponding to the inputs; those inputs must contain the keys 'primarySite' and 'histologyIcdO3'; they can optionally contain the key 'csSiteSpecificFactor25'. Returns null if the schema can't be determined.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologyIcdO3' : record.histologyIcdO3\n]\n\ndef schemaId = Functions.getCsSchemaId(inputs)")
    public String getCsSchemaId(Map<String, String> input) {
        if (_csStaging == null)
            return null;

        StagingSchema schema = getCsStagingSchema(input);
        return schema == null ? null : schema.getId();
    }

    /**
     * Returns whether the passed value is an acceptable code for the input fields and the requested CS field.
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologyIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * </ul>
     * <p/>
     * Created on Jan 22, 2010 by depryf
     * @param input input map containing the required "primarySite" and "histologyIcdO3" keys and the optional "csSiteSpecificFactor25" key
     * @param field requested CS field
     * @param valueToCheck the value to check
     * @return true if the value is acceptable, false otherwise
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "field", param2 = "CS field name", paramName3 = "valueToCheck", param3 = "value to validate",
            desc = "Returns true if the provided value is valid for the CS schema corresponding to the inputs and the CS field, false otherwise. The inputs must contain the keys 'primarySite' and 'histologyIcdO3'; they can optionaly contain the key 'csSiteSpecificFactor25'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologyIcdO3' : record.histologyIcdO3\n]\n\nreturn Functions.isAcceptableCsCode(inputs, 'csSiteSpecFact1', record.csSiteSpecFact1)")
    public boolean isAcceptableCsCode(Map<String, String> input, String field, String valueToCheck) {
        if (_csStaging == null || input == null || field == null || valueToCheck == null)
            return false;

        StagingSchema schema = getCsStagingSchema(input);
        return schema != null && _csStaging.isCodeValid(schema.getId(), CSTAGE_FIELDS.get(field), valueToCheck);

    }

    /**
     * Returns whether the passed value is an obsolete code for the input fields and the requested CS field.
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologyIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * </ul>
     * <p/>
     * Created on Jan 22, 2010 by depryf
     * @param input input map containing the required "primarySite" and "histologyIcdO3" keys and the optional "csSiteSpecificFactor25" key
     * @param field requested CS field
     * @param valueToCheck the value to check
     * @return true if the value is obsolete, false otherwise
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "field", param2 = "CS field name", paramName3 = "valueToCheck", param3 = "value to check",
            desc = "Returns true if the provided value is obsolete for the CS schema corresponding to the inputs and the CS field, false otherwise. The value is obsolete if its description in the CS table starts with OBSOLETE. The inputs must contain the keys 'primarySite' and 'histologyIcdO3'; they can optionaly contain the key 'csSiteSpecificFactor25'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologyIcdO3' : record.histologyIcdO3\n]\n\nreturn Functions.isObsoleteCsCode(inputs, 'csSiteSpecFact1', record.csSiteSpecFact1)")
    public boolean isObsoleteCsCode(Map<String, String> input, String field, String valueToCheck) {
        if (_csStaging == null || input == null || field == null || valueToCheck == null)
            return false;

        String description = getDescriptionForCode(input, CSTAGE_FIELDS.get(field), valueToCheck);
        return description != null && description.trim().toUpperCase().startsWith("OBSOLETE");

    }

    /**
     * Returns whether the passed value is an obsolete code for the input fields and the requested CS field.
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologyIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * </ul>
     * <p/>
     * Created on Jan 22, 2010 by depryf
     * @param input input map containing the required "primarySite" and "histologyIcdO3" keys and the optional "csSiteSpecificFactor25" key
     * @param field requested CS field
     * @param valueToCheck the value to check
     * @return true if the value is obsolete, false otherwise
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "field", param2 = "CS field name", paramName3 = "valueToCheck", param3 = "value to check",
            desc = "Returns the reason why a particular code is obsolete.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologyIcdO3' : record.histologyIcdO3\n]\n\nreturn Functions.getCsObsoleteReason(inputs, 'csSiteSpecFact1', record.csSiteSpecFact1)")
    public String getCsObsoleteReason(Map<String, String> input, String field, String valueToCheck) {
        if (_csStaging == null || input == null || field == null || valueToCheck == null)
            return null;

        String description = getDescriptionForCode(input, CSTAGE_FIELDS.get(field), valueToCheck);
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
     * <li>histologyIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * </ul>
     * <p/>
     * Created on Feb 23, 2010 by depryf
     * @param input input map containing the required "primarySite" and "histologyIcdO3" keys and the optional "csSiteSpecificFactor25" key
     * @param ssfIndex requested SSF index
     * @return true if the index is required, false otherwise
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "ssfIndex", param2 = "site specific factor index (Integer)",
            desc = "Returns true if the passed Site Specific Factor index is required for SEER for the schema corresponding to the passed input, "
                    + "false otherwise. Required means either 'already-collected', 'needed-for-staging' or 'clinically significant'. The inputs must contain the keys 'primarySite' and 'histologyIcdO3'; they can optionaly contain the key 'csSiteSpecificFactor25'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologyIcdO3' : record.histologyIcdO3\n]\n\nreturn Functions.isRequiredCsCode(inputs, 1)")
    public boolean isRequiredCsCode(Map<String, String> input, Integer ssfIndex) {
        if (_csStaging == null || input == null || ssfIndex == null)
            return false;

        StagingSchema schema = getCsStagingSchema(input);
        if (schema == null)
            return false;

        StagingSchemaInput schemaInput = schema.getInputMap().get("ssf" + ssfIndex);
        return schemaInput != null && (schemaInput.getUsedForStaging() || schemaInput.getMetadata() != null && (schemaInput.getMetadata().contains(CSTAGE_TAG_ALREADY_COLLECTED_SEER) || schemaInput
                .getMetadata().contains(CSTAGE_TAG_CLINICALLY_SIGNIFICANT_SEER)));

    }

    /**
     * Returns true if the passed SSF index is required (needed-for-staging) for SEER for the schema corresponding to the passed input.
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologyIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * </ul>
     * <p/>
     * Created on Feb 23, 2010 by depryf
     * @param input input map containing the required "primarySite" and "histologyIcdO3" keys and the optional "csSiteSpecificFactor25" key
     * @param ssfIndex requested SSF index
     * @return true if the index is required (needed for staging), false otherwise
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "ssfIndex", param2 = "site specific factor index (Integer)",
            desc = "Returns true if the passed Site Specific Factor index is required (needed-for-staging) for SEER for the schema corresponding to the passed input, "
                    + "false otherwise. The inputs must contain the keys 'primarySite' and 'histologyIcdO3'; they can optionaly contain the key 'csSiteSpecificFactor25'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologyIcdO3' : record.histologyIcdO3\n]\n\nreturn Functions.isNeededForStagingCsCode(inputs, 1)")
    public boolean isNeededForStagingCsCode(Map<String, String> input, Integer ssfIndex) {
        if (_csStaging == null || input == null || ssfIndex == null)
            return false;

        StagingSchema schema = getCsStagingSchema(input);
        if (schema == null)
            return false;

        StagingSchemaInput schemaInput = schema.getInputMap().get("ssf" + ssfIndex);
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
     * <li>histologyIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * </ul>
     * <p/>
     * Created on Feb 23, 2010 by depryf
     * @param input input map containing the required "primarySite" and "histologyIcdO3" keys and the optional "csSiteSpecificFactor25" key
     * @param ssfIndex requested SSF index
     * @return true if the index is required (already collected), false otherwise
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "ssfIndex", param2 = "site specific factor index (Integer)",
            desc = "Returns true if the passed Site Specific Factor index is required (already-collected) for SEER for the schema corresponding to the passed input, "
                    + "false otherwise. The inputs must contain the keys 'primarySite' and 'histologyIcdO3'; they can optionaly contain the key 'csSiteSpecificFactor25'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologyIcdO3' : record.histologyIcdO3\n]\n\nreturn Functions.isAlreadyCollectedCsCode(inputs, 1)")
    public boolean isAlreadyCollectedCsCode(Map<String, String> input, Integer ssfIndex) {
        if (_csStaging == null || input == null || ssfIndex == null)
            return false;

        StagingSchema schema = getCsStagingSchema(input);
        if (schema == null)
            return false;

        StagingSchemaInput schemaInput = schema.getInputMap().get("ssf" + ssfIndex);
        return schemaInput != null && schemaInput.getMetadata() != null && schemaInput.getMetadata().contains(CSTAGE_TAG_ALREADY_COLLECTED_SEER);

    }

    /**
     * Returns true if the passed SSF index is required (clinically-significant) for SEER for the schema corresponding to the passed input.
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologyIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * </ul>
     * <p/>
     * Created on Feb 23, 2010 by depryf
     * @param input input map containing the required "primarySite" and "histologyIcdO3" keys and the optional "csSiteSpecificFactor25" key
     * @param ssfIndex requested SSF index
     * @return true if the index is required (clinically significant), false otherwise
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "ssfIndex", param2 = "site specific factor index (Integer)",
            desc = "Returns true if the passed Site Specific Factor index is required (clinically-significant) for SEER for the schema corresponding to the passed input, "
                    + "false otherwise. The inputs must contain the keys 'primarySite' and 'histologyIcdO3'; they can optionaly contain the key 'csSiteSpecificFactor25'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologyIcdO3' : record.histologyIcdO3\n]\n\nreturn Functions.isClinicallySignificantCsCode(inputs, 1)")
    public boolean isClinicallySignificantCsCode(Map<String, String> input, Integer ssfIndex) {
        if (_csStaging == null || input == null || ssfIndex == null)
            return false;

        StagingSchema schema = getCsStagingSchema(input);
        if (schema == null)
            return false;

        StagingSchemaInput schemaInput = schema.getInputMap().get("ssf" + ssfIndex);
        return schemaInput != null && schemaInput.getMetadata() != null && schemaInput.getMetadata().contains(CSTAGE_TAG_CLINICALLY_SIGNIFICANT_SEER);

    }

    /**
     * Returns true if the passed SSF index is required pre-2010 for SEER for the schema corresponding to the passed input.
     * <p/>
     * Required means either already-collected, needed-for-staging or clinically-significant.
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologyIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * </ul>
     * <p/>
     * Created on Feb 23, 2010 by depryf
     * @param input input map containing the required "primarySite" and "histologyIcdO3" keys and the optional "csSiteSpecificFactor25" key
     * @param ssfIndex requested SSF index
     * @return true if the index is required pre-2010, false otherwise
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "ssfIndex", param2 = "site specific factor index (Integer)",
            desc = "Returns true if the passed Site Specific Factor index is required (pre-2010) for SEER for the schema corresponding to the passed input, "
                    + "false otherwise. The inputs must contain the keys 'primarySite' and 'histologyIcdO3'; they can optionaly contain the key 'csSiteSpecificFactor25'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologyIcdO3' : record.histologyIcdO3\n]\n\nreturn Functions.isRequiredPre2010CsCode(inputs, 1)")
    public boolean isRequiredPre2010CsCode(Map<String, String> input, Integer ssfIndex) {
        if (_csStaging == null || input == null || ssfIndex == null)
            return false;

        StagingSchema schema = getCsStagingSchema(input);
        if (schema == null)
            return false;

        StagingSchemaInput schemaInput = schema.getInputMap().get("ssf" + ssfIndex);
        return schemaInput != null && schemaInput.getMetadata() != null && schemaInput.getMetadata().contains(CSTAGE_TAG_REQUIRED_PRE_2010_SEER);

    }

    /**
     * Returns true if the passed SSF index is required for CoC for the schema corresponding to the passed input.
     * <p/>
     * Required means either already-collected, needed-for-staging or clinically-significant.
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologyIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * </ul>
     * <p/>
     * Created on Feb 23, 2010 by depryf
     * @param input input map containing the required "primarySite" and "histologyIcdO3" keys and the optional "csSiteSpecificFactor25" key
     * @param ssfIndex requested SSF index
     * @return true if the index is required, false otherwise
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "ssfIndex", param2 = "site specific factor index (Integer)",
            desc = "Returns true if the passed Site Specific Factor index is required for CoC for the schema corresponding to the passed input, "
                    + "false otherwise. Required means either 'already-collected', 'needed-for-staging' or 'clinically significant'. The inputs must contain the keys 'primarySite' and 'histologyIcdO3'; they can optionaly contain the key 'csSiteSpecificFactor25'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologyIcdO3' : record.histologyIcdO3\n]\n\nreturn Functions.isRequiredCsCode(inputs, 1)")
    public boolean isCocRequiredCsCode(Map<String, String> input, Integer ssfIndex) {
        if (_csStaging == null || input == null || ssfIndex == null)
            return false;

        StagingSchema schema = getCsStagingSchema(input);
        if (schema == null)
            return false;

        StagingSchemaInput schemaInput = schema.getInputMap().get("ssf" + ssfIndex);
        return schemaInput != null && (schemaInput.getUsedForStaging() || schemaInput.getMetadata() != null && (schemaInput.getMetadata().contains(CSTAGE_TAG_ALREADY_COLLECTED_COC) || schemaInput
                .getMetadata().contains(CSTAGE_TAG_CLINICALLY_SIGNIFICANT_COC)));

    }

    /**
     * Returns true if the passed SSF index is required (already-collected) for CoC for the schema corresponding to the passed input.
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologyIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * </ul>
     * <p/>
     * Created on Feb 23, 2010 by depryf
     * @param input input map containing the required "primarySite" and "histologyIcdO3" keys and the optional "csSiteSpecificFactor25" key
     * @param ssfIndex requested SSF index
     * @return true if the index is required (already collected), false otherwise
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "ssfIndex", param2 = "site specific factor index (Integer)",
            desc = "Returns true if the passed Site Specific Factor index is required (already-collected) for CoC for the schema corresponding to the passed input, false otherwise. The inputs must contain the keys 'primarySite' and 'histologyIcdO3'; they can optionaly contain the key 'csSiteSpecificFactor25'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologyIcdO3' : record.histologyIcdO3\n]\n\nreturn Functions.isAlreadyCollectedCsCode(inputs, 1)")
    public boolean isCocAlreadyCollectedCsCode(Map<String, String> input, Integer ssfIndex) {
        if (_csStaging == null || input == null || ssfIndex == null)
            return false;

        StagingSchema schema = getCsStagingSchema(input);
        if (schema == null)
            return false;

        StagingSchemaInput schemaInput = schema.getInputMap().get("ssf" + ssfIndex);
        return schemaInput != null && schemaInput.getMetadata() != null && schemaInput.getMetadata().contains(CSTAGE_TAG_ALREADY_COLLECTED_COC);

    }

    /**
     * Returns true if the passed SSF index is required (needed-for-staging) for CoC for the schema corresponding to the passed input.
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologyIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * </ul>
     * <p/>
     * Created on Feb 23, 2010 by depryf
     * @param input input map containing the required "primarySite" and "histologyIcdO3" keys and the optional "csSiteSpecificFactor25" key
     * @param ssfIndex requested SSF index
     * @return true if the index is required (needed for staging), false otherwise
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "ssfIndex", param2 = "site specific factor index (Integer)",
            desc = "Returns true if the passed Site Specific Factor index is required (needed-for-staging) for CoC for the schema corresponding to the passed input, false otherwise. The inputs must contain the keys 'primarySite' and 'histologyIcdO3'; they can optionaly contain the key 'csSiteSpecificFactor25'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologyIcdO3' : record.histologyIcdO3\n]\n\nreturn Functions.isNeededForStagingCsCode(inputs, 1)")
    public boolean isCocNeededForStagingCsCode(Map<String, String> input, Integer ssfIndex) {
        return isNeededForStagingCsCode(input, ssfIndex); // needed for staging is not agency-specific...
    }

    /**
     * Returns true if the passed SSF index is required (clinically-significant) for CoC for the schema corresponding to the passed input.
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologyIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * </ul>
     * <p/>
     * Created on Feb 23, 2010 by depryf
     * @param input input map containing the required "primarySite" and "histologyIcdO3" keys and the optional "csSiteSpecificFactor25" key
     * @param ssfIndex requested SSF index
     * @return true if the index is required (clinically significant), false otherwise
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "ssfIndex", param2 = "site specific factor index (Integer)",
            desc = "Returns true if the passed Site Specific Factor index is required (clinically-significant) for CoC for the schema corresponding to the passed input, false otherwise. The inputs must contain the keys 'primarySite' and 'histologyIcdO3'; they can optionaly contain the key 'csSiteSpecificFactor25'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologyIcdO3' : record.histologyIcdO3\n]\n\nreturn Functions.isClinicallySignificantCsCode(inputs, 1)")
    public boolean isCocClinicallySignificantCsCode(Map<String, String> input, Integer ssfIndex) {
        if (_csStaging == null || input == null || ssfIndex == null)
            return false;

        StagingSchema schema = getCsStagingSchema(input);
        if (schema == null)
            return false;

        StagingSchemaInput schemaInput = schema.getInputMap().get("ssf" + ssfIndex);
        return schemaInput != null && schemaInput.getMetadata() != null && schemaInput.getMetadata().contains(CSTAGE_TAG_CLINICALLY_SIGNIFICANT_COC);

    }

    /**
     * Returns true if the passed SSF index is required pre-2010 for CoC for the schema corresponding to the passed input.
     * <p/>
     * Required means either already-collected, needed-for-staging or clinically-significant.
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologyIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * </ul>
     * <p/>
     * Created on Feb 23, 2010 by depryf
     * @param input input map containing the required "primarySite" and "histologyIcdO3" keys and the optional "csSiteSpecificFactor25" key
     * @param ssfIndex requested SSF index
     * @return true if the index is required pre-2010, false otherwise
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "ssfIndex", param2 = "site specific factor index (Integer)",
            desc = "Returns true if the passed Site Specific Factor index is required (pre-2010) for CoC for the schema corresponding to the passed input, false otherwise. The inputs must contain the keys 'primarySite' and 'histologyIcdO3'; they can optionaly contain the key 'csSiteSpecificFactor25'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologyIcdO3' : record.histologyIcdO3\n]\n\nreturn Functions.isRequiredPre2010CsCode(inputs, 1)")
    public boolean isCocRequiredPre2010CsCode(Map<String, String> input, Integer ssfIndex) {
        if (_csStaging == null || input == null || ssfIndex == null)
            return false;

        StagingSchema schema = getCsStagingSchema(input);
        if (schema == null)
            return false;

        StagingSchemaInput schemaInput = schema.getInputMap().get("ssf" + ssfIndex);
        return schemaInput != null && schemaInput.getMetadata() != null && schemaInput.getMetadata().contains(CSTAGE_TAG_REQUIRED_PRE_2010_COC);

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
     * <li>histologyIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * <li>sex</li>
     * </ul>
     * <p/>
     * @param input input map containing the required "primarySite" and "histologyIcdO3" keys and the optional "csSiteSpecificFactor25" or "sex" keys
     * @return the corresponding TNM schema name; null if not found
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs",
            desc = "Returns the TNM schema name corresponding to the inputs; those inputs must contain the keys 'primarySite' and 'histologyIcdO3'; they can optionally contain the key 'csSiteSpecificFactor25' or 'sex'. Returns null if the schema can't be determined.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologyIcdO3' : record.histologyIcdO3\n]\n\ndef schemaName = Functions.getTnmSchemaName(inputs)")
    public String getTnmSchemaName(Map<String, String> input) {
        if (_tnmStaging == null)
            return null;

        StagingSchema schema = getTnmStagingSchema(input);
        return schema == null ? null : schema.getName();
    }

    /**
     * Returns the TNM schema ID for the given input, null if there is no schema
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologyIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * <li>sex</li>
     * </ul>
     * <p/>
     * @param input input map containing the required "primarySite" and "histologyIcdO3" keys and the optional "csSiteSpecificFactor25" or "sex" keys
     * @return the corresponding TNM schema ID; null if not found
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs",
            desc = "Returns the TNM schema ID corresponding to the inputs; those inputs must contain the keys 'primarySite' and 'histologyIcdO3'; they can optionally contain the key 'csSiteSpecificFactor25' or 'sex'. Returns null if the schema can't be determined.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologyIcdO3' : record.histologyIcdO3\n]\n\ndef schemaId = Functions.getTnmSchemaId(inputs)")
    public String getTnmSchemaId(Map<String, String> input) {
        if (_tnmStaging == null)
            return null;

        StagingSchema schema = getTnmStagingSchema(input);
        return schema == null ? null : schema.getId();
    }
    
    /**
     * Returns true if the given value is valid for the given field with the schema corresponding to the passed input
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologyIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * <li>sex</li>
     * </ul>
     * <p/>
     * @param input input map containing the required "primarySite" and "histologyIcdO3" keys and the optional "csSiteSpecificFactor25" or "sex" keys
     * @param field requested TNM field
     * @param valueToCheck value to check
     * @return true if the value is acceptable, false otherwise
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "field", param2 = "TNM field name", paramName3 = "valueToCheck", param3 = "value to validate",
            desc = "Returns true if the provided value is valid for the TNM schema corresponding to the inputs and the TNM field, false otherwise. The inputs must contain the keys 'primarySite' and 'histologyIcdO3'; they can optionaly contain the keys 'csSiteSpecificFactor25' and 'sex'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologyIcdO3' : record.histologyIcdO3\n]\n\nreturn Functions.isAcceptableTnmCode(inputs, 'csSiteSpecFact1', record.csSiteSpecFact1)")
    public boolean isAcceptableTnmCode(Map<String, String> input, String field, String valueToCheck) {
        if (_tnmStaging == null || input == null || field == null)
            return false;

        StagingSchema schema = getTnmStagingSchema(input);
        return schema != null && _tnmStaging.isCodeValid(schema.getId(), TNM_FIELDS.get(field), valueToCheck);
    }

    /**
     * Returns true if the passed SSF index is required for SEER for the schema corresponding to the passed input.
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologyIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * <li>sex</li>
     * </ul>
     * <p/>
     * @param input input map containing the required "primarySite" and "histologyIcdO3" keys and the optional "csSiteSpecificFactor25" or "sex" keys
     * @param ssfIndex requested SSF index
     * @return true if the index is required for SEER, false otherwise
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "ssfIndex", param2 = "site specific factor index (Integer)",
            desc = "Returns true if the passed Site Specific Factor index is required for SEER for the schema corresponding to the passed input, "
                    + "false otherwise. Required means either 'seer-required' or 'needed-for-staging'. The inputs must contain the keys 'primarySite' and 'histologyIcdO3'; they can optionaly contain the keys 'csSiteSpecificFactor25' or 'sex'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologyIcdO3' : record.histologyIcdO3\n]\n\nreturn Functions.isRequiredTnmCode(inputs, 1)")
    public boolean isRequiredTnmCode(Map<String, String> input, Integer ssfIndex) {
        if (_tnmStaging == null || input == null || ssfIndex == null)
            return false;

        StagingSchema schema = getTnmStagingSchema(input);
        if (schema == null)
            return false;

        StagingSchemaInput schemaInput = schema.getInputMap().get("ssf" + ssfIndex);
        return schemaInput != null && (schemaInput.getUsedForStaging() || schemaInput.getMetadata() != null && (schemaInput.getMetadata().contains(TNM_TAG_SEER_REQUIRED)));
    }

    /**
     * Returns true if the passed SSF index is required (needed-for-staging) for SEER for the schema corresponding to the passed input.
     * <p/>
     * The data structure should contains the following keys:
     * <ul>
     * <li>primarySite</li>
     * <li>histologyIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * <li>sex</li>
     * </ul>
     * <p/>
     * @param input input map containing the required "primarySite" and "histologyIcdO3" keys and the optional "csSiteSpecificFactor25" or "sex" keys
     * @param ssfIndex requested SSF index
     * @return true if the index is needed for staging, false otherwise
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "ssfIndex", param2 = "site specific factor index (Integer)",
            desc = "Returns true if the passed Site Specific Factor index is required (needed-for-staging) for SEER for the schema corresponding to the passed input, "
                    + "false otherwise. The inputs must contain the keys 'primarySite' and 'histologyIcdO3'; they can optionaly contain the keys 'csSiteSpecificFactor25' or 'sex'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologyIcdO3' : record.histologyIcdO3\n]\n\nreturn Functions.isNeededForStagingTnmCode(inputs, 1)")
    public boolean isNeededForStagingTnmCode(Map<String, String> input, Integer ssfIndex) {
        if (_tnmStaging == null || input == null || ssfIndex == null)
            return false;

        StagingSchema schema = getTnmStagingSchema(input);
        if (schema == null)
            return false;

        StagingSchemaInput schemaInput = schema.getInputMap().get("ssf" + ssfIndex);
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
     * <li>histologyIcdO3</li>
     * <li>csSiteSpecificFactor25</li>
     * <li>sex</li>
     * </ul>
     * <p/>
     * @param input input map containing the required "primarySite" and "histologyIcdO3" keys and the optional "csSiteSpecificFactor25" or "sex" keys
     * @param ssfIndex requested SSF index
     * @return true if the index is required for COC, false otherwise
     */
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "ssfIndex", param2 = "site specific factor index (Integer)",
            desc = "Returns true if the passed Site Specific Factor index is required for COC for the schema corresponding to the passed input, "
                    + "false otherwise. Required means either 'coc-required' or 'needed-for-staging'. The inputs must contain the keys 'primarySite' and 'histologyIcdO3'; they can optionaly contain the keys 'csSiteSpecificFactor25' or 'sex'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologyIcdO3' : record.histologyIcdO3\n]\n\nreturn Functions.isCocRequiredTnmCode(inputs, 1)")
    public boolean isCocRequiredTnmCode(Map<String, String> input, Integer ssfIndex) {
        if (_tnmStaging == null || input == null || ssfIndex == null)
            return false;

        StagingSchema schema = getTnmStagingSchema(input);
        if (schema == null)
            return false;

        StagingSchemaInput schemaInput = schema.getInputMap().get("ssf" + ssfIndex);
        return schemaInput != null && (schemaInput.getUsedForStaging() || schemaInput.getMetadata() != null && (schemaInput.getMetadata().contains(TNM_TAG_COC_REQUIRED)));
    }

    /**
     * Helper method to get the schema from an input line (using standard NAACCR properties).
     * <br/>
     * I am making this method public because it's useful in many other places in the code...
     * @param input input fields (standard NAACCR properties)
     * @return corresponding schema, maybe null
     */
    public StagingSchema getCsStagingSchema(Map<String, String> input) {
        if (_csStaging == null || input == null)
            return null;

        String site = input.get(CSTAGE_INPUT_PROP_SITE);
        String hist = input.get(CSTAGE_INPUT_PROP_HIST);
        String ssf25 = input.get(CSTAGE_INPUT_PROP_DISC);

        List<StagingSchema> schemas = _csStaging.lookupSchema(new CsSchemaLookup(site, hist, ssf25));
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
    public StagingSchema getTnmStagingSchema(Map<String, String> input) {
        if (_tnmStaging == null || input == null)
            return null;

        String site = input.get(TNM_INPUT_PROP_SITE);
        String hist = input.get(TNM_INPUT_PROP_HIST);
        String ssf25 = input.get(TNM_INPUT_PROP_SSF25);
        String sex = input.get(TNM_INPUT_PROP_SEX);

        TnmSchemaLookup lkup = new TnmSchemaLookup(site, hist);
        lkup.setInput(TnmStagingData.SSF25_KEY, ssf25);
        lkup.setInput(TnmStagingData.SEX_KEY, sex);

        List<StagingSchema> schemas = _tnmStaging.lookupSchema(lkup);
        if (schemas.size() == 1)
            return _tnmStaging.getSchema(schemas.get(0).getId());

        return null;
    }

    /**
     * Helper method to get the schema from a schema number (as used in the old cstage DLL).
     * <br/>
     * @param schemaNumber schema number
     * @return corresponding schema, maybe null
     */
    private StagingSchema getCsStagingSchema(int schemaNumber) {
        if (_csStaging == null || schemaNumber == -1)
            return null;

        return _csStaging.getSchema(_csSchemaIdByNumber.get(schemaNumber));
    }

    /**
     * Helper method to get the description of the row corresponding to the requested code.
     * <br/>
     * The method assumes there is only one description column (if there is several, it will only consider the first one).
     * @param input input input fields (standard NAACCR properties)
     * @param cstageField field/key to check (to know which table to use)
     * @return corresponding description, maybe null
     */
    private String getDescriptionForCode(Map<String, String> input, String cstageField, String code) {
        if (_csStaging == null || input == null || code == null)
            return null;

        StagingSchema schema = getCsStagingSchema(input);
        if (schema == null)
            return null;

        StagingSchemaInput schemaInput = schema.getInputMap().get(cstageField);
        if (schemaInput == null)
            return null;

        StagingTable table = _csStaging.getTable(schemaInput.getTable());
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

        Integer rowIndex = _csStaging.findMatchingTableRow(table.getId(), cstageField, code);
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

        StagingSchema schema = getCsStagingSchema(input);
        return schema == null || schema.getSchemaNum() == null ? -1 : schema.getSchemaNum();
    }

    protected String getCsSchemaName(int schemaNum) {
        if (_csStaging == null)
            return null;

        StagingSchema schema = getCsStagingSchema(schemaNum);
        return schema == null ? null : schema.getName();
    }

    protected boolean isAcceptableCsCode(int schemaNumber, int tableNumber, String valueToCheck) {
        if (_csStaging == null)
            return false;

        StagingSchema schema = getCsStagingSchema(schemaNumber);
        return schema != null && _csStaging.isCodeValid(schema.getId(), CSTAGE_TABLE_NUMBERS.get(tableNumber), valueToCheck);
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
                        String[] parts = ((String)obj).split("[.]{2}");
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
                String[] parts = ((String)key).split("..");
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
                String[] parts = ((String)obj).split("[.]{2}");
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
    public String getSsf25FromSex(String ssf25, String sex, String hist, String dxYear, String schemaId) {
        boolean isPeritoneum = "peritoneum".equals(schemaId) || "peritoneum_female_gen".equals(schemaId);
        boolean isMissingSsf25 = !("001".equals(ssf25) || "002".equals(ssf25) || "003".equals(ssf25) || "004".equals(ssf25) || "009".equals(ssf25) || "981".equals(ssf25));
        if (isPeritoneum && isMissingSsf25 && ("2016".equals(dxYear) || "2017".equals(dxYear))) {
            Integer histInt = hist != null ? Integer.valueOf(hist) : null;
            if (hist == null || !((8000 <= histInt && histInt <= 8576) || (8590 <= histInt && histInt <= 8671) || (8930 <= histInt && histInt <= 8934) || (8940 <= histInt && histInt <= 9110)))
                return "981";

            if (sex == null)
                return "009";

            switch (sex) {
                case "2":
                case "6":
                    return "002";
                case "1":
                case "5":
                    return "001";
                case "3":
                    return "003";
                case "4":
                    return "004";
                default:
                    return "009";
            }
        }

        return ssf25;
    }
}
