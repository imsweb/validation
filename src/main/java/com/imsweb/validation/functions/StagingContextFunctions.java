/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.validation.functions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import groovy.lang.IntRange;

import com.imsweb.decisionengine.ColumnDefinition;
import com.imsweb.staging.SchemaLookup;
import com.imsweb.staging.Staging;
import com.imsweb.staging.entities.StagingSchema;
import com.imsweb.staging.entities.StagingSchemaInput;
import com.imsweb.staging.entities.StagingTable;
import com.imsweb.validation.ContextFunctionDocAnnotation;
import com.imsweb.validation.ValidationContextFunctions;

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

    // this maps the CS fields used by the edits (NAACCR property names) to the input keys used in the Staging framework
    public static final Map<String, String> CSTAGE_FIELDS = Collections.unmodifiableMap(new HashMap<String, String>() {{
        put("csTumorSize", "size");
        put("csExtension", "extension");
        put("csTumorSizeExtEval", "extension_eval");
        put("csLymphNodes", "nodes");
        put("csLymphNodesEval", "nodes_eval");
        put("regionalNodesPositive", "nodes_pos");
        put("regionalNodesExamined", "nodes_exam");
        put("csMetsAtDx", "mets");
        put("csMetsEval", "mets_eval");
        put("csSiteSpecificFactor1", "ssf1");
        put("csSiteSpecificFactor2", "ssf2");
        put("csSiteSpecificFactor3", "ssf3");
        put("csSiteSpecificFactor4", "ssf4");
        put("csSiteSpecificFactor5", "ssf5");
        put("csSiteSpecificFactor6", "ssf6");
        put("csSiteSpecificFactor7", "ssf7");
        put("csSiteSpecificFactor8", "ssf8");
        put("csSiteSpecificFactor9", "ssf9");
        put("csSiteSpecificFactor10", "ssf10");
        put("csSiteSpecificFactor11", "ssf11");
        put("csSiteSpecificFactor12", "ssf12");
        put("csSiteSpecificFactor13", "ssf13");
        put("csSiteSpecificFactor14", "ssf14");
        put("csSiteSpecificFactor15", "ssf15");
        put("csSiteSpecificFactor16", "ssf16");
        put("csSiteSpecificFactor17", "ssf17");
        put("csSiteSpecificFactor18", "ssf18");
        put("csSiteSpecificFactor19", "ssf19");
        put("csSiteSpecificFactor20", "ssf20");
        put("csSiteSpecificFactor21", "ssf21");
        put("csSiteSpecificFactor22", "ssf22");
        put("csSiteSpecificFactor23", "ssf23");
        put("csSiteSpecificFactor24", "ssf24");
        put("csSiteSpecificFactor25", "ssf25");
    }});

    // this maps the table number of the old DLL to the input keys used in the Staging framework
    public static final Map<Integer, String> CSTAGE_TABLE_NUMBERS = Collections.unmodifiableMap(new HashMap<Integer, String>() {{
        put(1, "size");
        put(2, "extension");
        put(3, "extension_eval");
        put(4, "nodes");
        put(5, "nodes_eval");
        put(6, "nodes_pos");
        put(7, "nodes_exam");
        put(8, "mets");
        put(9, "mets_eval");
        put(10, "ssf1");
        put(11, "ssf2");
        put(12, "ssf3");
        put(13, "ssf4");
        put(14, "ssf5");
        put(15, "ssf6");
        put(16, "ssf7");
        put(17, "ssf8");
        put(18, "ssf9");
        put(19, "ssf10");
        put(20, "ssf11");
        put(21, "ssf12");
        put(22, "ssf13");
        put(23, "ssf14");
        put(24, "ssf15");
        put(25, "ssf16");
        put(26, "ssf17");
        put(27, "ssf18");
        put(28, "ssf19");
        put(29, "ssf20");
        put(30, "ssf21");
        put(31, "ssf22");
        put(32, "ssf23");
        put(33, "ssf24");
        put(34, "ssf25");
        // there are a few more tables, but I don't think they are used in the translated edits: 35 HIST7; 36 HIST; 37 AJCC7; 38 AJCC; 39 SEERSUM
    }});

    // the obsolete reasons as defined in the CStage tables; edits use the key only...
    public static final Map<String, String> CSTAGE_OBSOLETE_REASONS = Collections.unmodifiableMap(new LinkedHashMap<String, String>() {{
        put("OBSOLETE DATA CONVERTED AND RETAINED V0200", "1");
        put("OBSOLETE DATA CONVERTED V0102", "2");
        put("OBSOLETE DATA CONVERTED V0104", "3");
        put("OBSOLETE DATA CONVERTED V0200", "4");
        put("OBSOLETE DATA RETAINED V0100", "5");
        put("OBSOLETE DATA RETAINED V0102", "6");
        put("OBSOLETE DATA RETAINED V0200", "7");
        put("OBSOLETE DATA REVIEWED AND CHANGED V0102", "8");
        put("OBSOLETE DATA REVIEWED AND CHANGED V0103", "9");
        put("OBSOLETE DATA REVIEWED AND CHANGED V0200", "10");
        put("OBSOLETE DATA CONVERTED V0203", "11");
        put("OBSOLETE DATA REVIEWED AND CHANGED V0203", "12");
        put("OBSOLETE DATA REVIEWED V0203", "13");
        put("OBSOLETE DATA RETAINED AND REVIEWED V0203", "14");
        put("OBSOLETE DATA RETAINED V0203", "15");
        put("OBSOLETE DATA RETAINED V0104", "16");
        put("OBSOLETE DATA RETAINED V0202", "17");
        put("OBSOLETE DATA RETAINED AND REVIEWED V0200", "18");
        put("OBSOLETE DATA CONVERTED V0204", "19");
        put("OBSOLETE DATA REVIEWED AND CHANGED V0204", "20");
        put("OBSOLETE DATA RETAINED AND REVIEWED V0204", "21");
        put("OBSOLETE DATA RETAINED V0204", "22");
    }});

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

    // this maps the TNM fields used by the edits (NAACCR property names) to the input keys used in the Staging framework
    public static final Map<String, String> TNM_FIELDS = Collections.unmodifiableMap(new HashMap<String, String>() {{
        put("eodPrimaryTumor", "seer_primary_tumor");
        put("eodRegionalNodes", "seer_nodes");
        put("eodMets", "seer_mets");
        put("tnmClinStageGroup", "clin_stage_group_direct");
        put("tnmPathStageGroup", "path_stage_group_direct");
        put("rxSummSystemicSurSeq", "systemic_surg_seq");
        put("rxSummSurgRadSeq", "radiation_surg_seq");
        put("tnmClinT", "clin_t");
        put("tnmClinN", "clin_n");
        put("tnmClinM", "clin_m");
        put("tnmPathT", "path_t");
        put("tnmPathN", "path_n");
        put("tnmPathM", "path_m");
        put("csSiteSpecificFactor1", "ssf1");
        put("csSiteSpecificFactor2", "ssf2");
        put("csSiteSpecificFactor3", "ssf3");
        put("csSiteSpecificFactor4", "ssf4");
        put("csSiteSpecificFactor5", "ssf5");
        put("csSiteSpecificFactor6", "ssf6");
        put("csSiteSpecificFactor7", "ssf7");
        put("csSiteSpecificFactor8", "ssf8");
        put("csSiteSpecificFactor9", "ssf9");
        put("csSiteSpecificFactor10", "ssf10");
        put("csSiteSpecificFactor11", "ssf11");
        put("csSiteSpecificFactor12", "ssf12");
        put("csSiteSpecificFactor13", "ssf13");
        put("csSiteSpecificFactor14", "ssf14");
        put("csSiteSpecificFactor15", "ssf15");
        put("csSiteSpecificFactor16", "ssf16");
        put("csSiteSpecificFactor17", "ssf17");
        put("csSiteSpecificFactor18", "ssf18");
        put("csSiteSpecificFactor19", "ssf19");
        put("csSiteSpecificFactor20", "ssf20");
        put("csSiteSpecificFactor21", "ssf21");
        put("csSiteSpecificFactor22", "ssf22");
        put("csSiteSpecificFactor23", "ssf23");
        put("csSiteSpecificFactor24", "ssf24");
        put("csSiteSpecificFactor25", "ssf25");
    }});

    // the standard NAACCR properties used when getting a EOD schema
    public static final String EOD_INPUT_PROP_SITE = "primarySite";
    public static final String EOD_INPUT_PROP_HIST = "histologicTypeIcdO3";
    public static final String EOD_INPUT_PROP_SEX = "sex";
    public static final String EOD_INPUT_PROP_DISC_1 = "schemaDiscriminator1";
    public static final String EOD_INPUT_PROP_DISC_2 = "schemaDiscriminator2";
    public static final String EOD_INPUT_PROP_BEHAV = "behaviorCodeIcdO3";

    // EOD metadata tags
    public static final String EOD_TAG_SEER_REQUIRED = "SEER_REQUIRED";
    public static final String EOD_TAG_COC_REQUIRED = "COC_REQUIRED";
    public static final String EOD_TAG_NPCR_REQUIRED = "NPCR_REQUIRED";
    public static final String EOD_TAG_CCCR_REQUIRED = "CCCR_REQUIRED";
    public static final String EOD_TAG_SSDI = "SSDI";

    // this maps the EOD fields used by the edits (NAACCR property names) to the input keys used in the Staging framework
    public static final Map<String, String> EOD_FIELDS = Collections.unmodifiableMap(new HashMap<String, String>() {{
        put("schemaDiscriminator1", "discriminator_1");
        put("schemaDiscriminator2", "discriminator_2");
        put("schemaDiscriminator3", "discriminator_3");
        put("ajccTnmClinM", "clin_m");
        put("ajccTnmClinN", "clin_n");
        put("ajccTnmClinNSuffix", "clin_n_suffix");
        put("ajccTnmClinStageGroup", "clin_stage_group_direct");
        put("ajccTnmClinT", "clin_t");
        put("ajccTnmClinTSuffix", "clin_t_suffix");
        put("ajccTnmPathM", "path_m");
        put("ajccTnmPathN", "path_n");
        put("ajccTnmPathNSuffix", "path_n_suffix");
        put("ajccTnmPathStageGroup", "path_stage_group_direct");
        put("ajccTnmPathT", "path_t");
        put("ajccTnmPathTSuffix", "path_t_suffix");
        put("ajccTnmPostTherapyM", "ypath_m");
        put("ajccTnmPostTherapyN", "ypath_n");
        put("ajccTnmPostTherapyNSuffix", "ypath_n_suffix");
        put("ajccTnmPostTherapyStageGroup", "ypath_stage_group_direct");
        put("ajccTnmPostTherapyT", "ypath_t");
        put("ajccTnmPostTherapyTSuffix", "ypath_t_suffix");
        put("eodPrimaryTumor", "eod_primary_tumor");
        put("eodRegionalNodes", "eod_regional_nodes");
        put("eodMets", "eod_mets");
        put("gradeClinical", "grade_clin");
        put("gradePathological", "grade_path");
        put("gradePostTherapy", "grade_post_therapy");
        put("tumorSizeClinical", "size_clin");
        put("tumorSizePathologic", "size_path");
        put("tumorSizeSummary", "size_summary");
        put("summaryStage2018", "ss2018");
        put("rxSummSystemicSurSeq", "systemic_surg_seq");
        put("rxSummSurgRadSeq", "radiation_surg_seq");
        put("regionalNodesPositive", "nodes_pos");
        put("regionalNodesExamined", "nodes_exam");
        put("lymphVascularInvasion", "lvi");
        put("adenoidCysticBasaloidPattern", "adenoid_cystic_basaloid_pattern");
        put("adenopathy", "adenopathy");
        put("afpPostOrchiectomyLabValue", "afp_post_orch_lab_value");
        put("afpPostOrchiectomyRange", "afp_post_orch_range");
        put("afpPreOrchiectomyLabValue", "afp_pre_orch_lab_value");
        put("afpPreOrchiectomyRange", "afp_pre_orch_range");
        put("afpPretreatmentInterpretation", "afp_pretx_interpretation");
        put("afpPretreatmentLabValue", "afp_pretx_lab_value");
        put("anemia", "anemia");
        put("bSymptoms", "b_symptoms");
        put("bilirubinPretxTotalLabValue", "bilirubin_pretx_lab_value");
        put("bilirubinPretxUnitOfMeasure", "bilirubin_pretx_unit");
        put("boneInvasion", "bone_invasion");
        put("brainMolecularMarkers", "brain_molecular_markers");
        put("breslowTumorThickness", "breslow_thickness");
        put("ca125PretreatmentInterpretation", "ca125_pretx_interpretation");
        put("ceaPretreatmentInterpretation", "cea_pretx_interpretation");
        put("ceaPretreatmentLabValue", "cea_pretx_lab_value");
        put("chromosome19qLossHeterozygosity", "chrom_19q_status");
        put("chromosome1pLossHeterozygosity", "chrom_1p_status");
        put("chromosome3Status", "chrom_3_status");
        put("chromosome8qStatus", "chrom_8q_status");
        put("circumferentialResectionMargin", "crm");
        put("creatininePretreatmentLabValue", "creatinine_pretx_lab_value");
        put("creatininePretxUnitOfMeasure", "creatinine_pretx_unit");
        put("esophagusAndEgjTumorEpicenter", "esoph_tumor_epicenter");
        put("estrogenReceptorPercntPosOrRange", "er_percent_positive");
        put("estrogenReceptorSummary", "er");
        put("estrogenReceptorTotalAllredScore", "er_allred_score");
        put("extranodalExtensionClin", "extranodal_ext_clin");
        put("extranodalExtensionHeadNeckClin", "extranodal_ext_hn_clin");
        put("extranodalExtensionHeadNeckPath", "extranodal_ext_hn_path");
        put("extranodalExtensionPath", "extranodal_ext_path");
        put("extravascularMatrixPatterns", "extravascular_matrix_patterns");
        put("fibrosisScore", "fibrosis_score");
        put("figoStage", "figo");
        put("gestationalTrophoblasticPxIndex", "gestational_prog_index");
        put("gleasonPatternsClinical", "gleason_patterns_clin");
        put("gleasonPatternsPathological", "gleason_patterns_path");
        put("gleasonScoreClinical", "gleason_score_clin");
        put("gleasonScorePathological", "gleason_score_path");
        put("gleasonTertiaryPattern", "gleason_tertiary_pattern");
        put("hcgPostOrchiectomyLabValue", "hcg_post_orch_lab_value");
        put("hcgPostOrchiectomyRange", "hcg_post_orch_range");
        put("hcgPreOrchiectomyLabValue", "hcg_pre_orch_lab_value");
        put("hcgPreOrchiectomyRange", "hcg_pre_orch_range");
        put("her2IhcSummary", "her2_ihc_summary");
        put("her2IshDualProbeCopyNumber", "her2_ish_dp_copy_no");
        put("her2IshDualProbeRatio", "her2_ish_dp_ratio");
        put("her2IshSingleProbeCopyNumber", "her2_ish_sp_copy_no");
        put("her2IshSummary", "her2_ish_summary");
        put("her2OverallSummary", "her2_summary");
        put("heritableTrait", "heritable_trait");
        put("highRiskCytogenetics", "high_risk_cytogenetics");
        put("highRiskHistologicFeatures", "high_risk_features");
        put("hivStatus", "hiv");
        put("iNRProthrombinTime", "inr_prothrombin_time");
        put("invasionBeyondCapsule", "invasion_beyond_capsule");
        put("ipsilateralAdrenalGlandInvolve", "ipsilateral_adrenal_gland_involv");
        put("jak2", "jak2");
        put("ki67", "ki67");
        put("kitGeneImmunohistochemistry", "kit");
        put("kras", "kras");
        put("ldhPostOrchiectomyRange", "ldh_post_orch_range");
        put("ldhPreOrchiectomyRange", "ldh_pre_orch_range");
        put("ldhPretreatmentLevel", "ldh_pretx_level");
        put("ldhUpperLimitsOfNormal", "ldh_upper_limit");
        put("lnAssessMethodFemoralInguinal", "ln_assessment_femoral");
        put("lnAssessMethodParaaortic", "ln_assessment_para_aortic");
        put("lnAssessMethodPelvic", "ln_assessment_pelvic");
        put("lnDistantAssessMethod", "ln_distant_assessment");
        put("lnDistantMediastinalScalene", "ln_distant_mediastinal_scalene");
        put("lnHeadAndNeckLevels1To3", "ln_hn_1_2_3");
        put("lnHeadAndNeckLevels4To5", "ln_hn_4_5");
        put("lnHeadAndNeckLevels6To7", "ln_hn_6_7");
        put("lnHeadAndNeckOther", "ln_hn_other");
        put("lnIsolatedTumorCells", "ln_itc");
        put("lnLaterality", "ln_laterality");
        put("lnPositiveAxillaryLevel1To2", "ln_pos_axillary_level_1_2");
        put("lnSize", "ln_size_of_mets");
        put("lnStatusFemorInguinParaaortPelv", "ln_status");
        put("lymphocytosis", "lymphocytosis");
        put("majorVeinInvolvement", "major_vein_involv");
        put("measuredBasalDiameter", "measured_basal_diameter");
        put("measuredThickness", "measured_thickness");
        put("methylationOfO6MGMT", "mgmt");
        put("microsatelliteInstability", "msi");
        put("microvascularDensity", "mvd");
        put("mitoticCountUvealMelanoma", "mitotic_count_uveal_mel");
        put("mitoticRateMelanoma", "mitotic_rate_melanoma");
        put("multigeneSignatureMethod", "multigene_signature_method");
        put("multigeneSignatureResults", "multigene_signature_result");
        put("nccnInternationalPrognosticIndex", "intern_prog_index");
        put("numberOfCoresExamined", "number_cores_exam");
        put("numberOfCoresPositive", "number_cores_pos");
        put("numberOfExaminedParaAorticNodes", "num_exam_para_aortic_nodes");
        put("numberOfExaminedPelvicNodes", "num_exam_pelvic_nodes");
        put("numberOfPositiveParaAorticNodes", "num_pos_para_aortic_nodes");
        put("numberOfPositivePelvicNodes", "num_pos_pelvic_nodes");
        put("oncotypeDxRecurrenceScoreDcis", "oncotype_dx_score_dcis");
        put("oncotypeDxRecurrenceScoreInvasiv", "oncotype_dx_score");
        put("oncotypeDxRiskLevelDcis", "oncotype_dx_risk_level_dcis");
        put("oncotypeDxRiskLevelInvasive", "oncotype_dx_risk_level");
        put("organomegaly", "organomegaly");
        put("percentNecrosisPostNeoadjuvant", "post_neoadj_chemo_percent_necrosis");
        put("perineuralInvasion", "perineural_invasion");
        put("peripheralBloodInvolvement", "peripheral_blood_involv");
        put("peritonealCytology", "peritoneal_cytology");
        put("pleuralEffusion", "pleural_effusion");
        put("primarySclerosingCholangitis", "prim_scleros_cholangitis");
        put("profoundImmuneSuppression", "profound_immune_suppression");
        put("progesteroneRecepPrcntPosOrRange", "pr_percent_positive");
        put("progesteroneRecepSummary", "pr");
        put("progesteroneRecepTotalAllredScor", "pr_allred_score");
        put("prostatePathologicalExtension", "prostate_path_extension");
        put("psaLabValue", "psa");
        put("residualTumVolPostCytoreduction", "resid_tumor_vol_post_cyto");
        put("responseToNeoadjuvantTherapy", "response_neoadjuv_therapy");
        put("sCategoryClinical", "s_category_clin");
        put("sCategoryPathological", "s_category_path");
        put("sarcomatoidFeatures", "sarcomatoid_features");
        put("seerSiteSpecificFact1", "seer_ssf1");
        put("separateTumorNodules", "separate_tumor_nodules");
        put("serumAlbuminPretreatmentLevel", "serum_alb_pretx_level");
        put("serumBeta2MicroglobulinPretxLvl", "b2_microglob_pretx_level");
        put("ldhPretreatmentLabValue", "ldh_pretx_lab_value");
        put("thrombocytopenia", "thrombocytopenia");
        put("tumorDeposits", "tumor_deposits");
        put("tumorGrowthPattern", "tumor_growth_pattern");
        put("ulceration", "ulceration");
        put("visceralParietalPleuralInvasion", "visceral_pleural_invasion");
    }});

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

        StagingSchema schema = getCsStagingSchema(input);
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

        StagingSchema schema = getCsStagingSchema(input);
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
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "field", param2 = "CS field name", paramName3 = "valueToCheck", param3 = "value to validate",
            desc = "Returns true if the provided value is valid for the CS schema corresponding to the inputs and the CS field, false otherwise. The inputs must contain the keys 'primarySite' and 'histologicTypeIcdO3'; they can optionaly contain the key 'csSiteSpecificFactor25'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologicTypeIcdO3' : record.histologicTypeIcdO3\n]\n\nreturn Functions.isAcceptableCsCode(inputs, 'csSiteSpecFact1', record.csSiteSpecFact1)")
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
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "field", param2 = "CS field name", paramName3 = "valueToCheck", param3 = "value to check",
            desc = "Returns true if the provided value is obsolete for the CS schema corresponding to the inputs and the CS field, false otherwise. The value is obsolete if its description in the CS table starts with OBSOLETE. The inputs must contain the keys 'primarySite' and 'histologicTypeIcdO3'; they can optionaly contain the key 'csSiteSpecificFactor25'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologicTypeIcdO3' : record.histologicTypeIcdO3\n]\n\nreturn Functions.isObsoleteCsCode(inputs, 'csSiteSpecFact1', record.csSiteSpecFact1)")
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
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "field", param2 = "CS field name", paramName3 = "valueToCheck", param3 = "value to check",
            desc = "Returns the reason why a particular code is obsolete.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologicTypeIcdO3' : record.histologicTypeIcdO3\n]\n\nreturn Functions.getCsObsoleteReason(inputs, 'csSiteSpecFact1', record.csSiteSpecFact1)")
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

        StagingSchema schema = getCsStagingSchema(input);
        if (schema == null)
            return false;

        StagingSchemaInput schemaInput = schema.getInputMap().get("ssf" + ssfIndex);
        return schemaInput != null && (schemaInput.getUsedForStaging() || (schemaInput.getMetadata() != null &&
                (schemaInput.getMetadata().contains(CSTAGE_TAG_ALREADY_COLLECTED_SEER) || schemaInput.getMetadata().contains(CSTAGE_TAG_CLINICALLY_SIGNIFICANT_SEER))));

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

        StagingSchema schema = getCsStagingSchema(input);
        if (schema == null)
            return false;

        StagingSchemaInput schemaInput = schema.getInputMap().get("ssf" + ssfIndex);
        return schemaInput != null && (schemaInput.getUsedForStaging() || (schemaInput.getMetadata() != null &&
                (schemaInput.getMetadata().contains(CSTAGE_TAG_ALREADY_COLLECTED_COC) || schemaInput.getMetadata().contains(CSTAGE_TAG_CLINICALLY_SIGNIFICANT_COC))));

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

        StagingSchema schema = getTnmStagingSchema(input);
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

        StagingSchema schema = getTnmStagingSchema(input);
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
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "field", param2 = "TNM field name", paramName3 = "valueToCheck", param3 = "value to validate",
            desc = "Returns true if the provided value is valid for the TNM schema corresponding to the inputs and the TNM field, false otherwise. The inputs must contain the keys 'primarySite' and 'histologicTypeIcdO3'; they can optionaly contain the keys 'csSiteSpecificFactor25' and 'sex'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologicTypeIcdO3' : record.histologicTypeIcdO3\n]\n\nreturn Functions.isAcceptableTnmCode(inputs, 'csSiteSpecFact1', record.csSiteSpecFact1)")
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

        StagingSchema schema = getTnmStagingSchema(input);
        if (schema == null)
            return false;

        StagingSchemaInput schemaInput = schema.getInputMap().get("ssf" + ssfIndex);
        return schemaInput != null && (schemaInput.getUsedForStaging() || (schemaInput.getMetadata() != null && schemaInput.getMetadata().contains(TNM_TAG_SEER_REQUIRED)));
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

        StagingSchema schema = getTnmStagingSchema(input);
        if (schema == null)
            return false;

        StagingSchemaInput schemaInput = schema.getInputMap().get("ssf" + ssfIndex);
        return schemaInput != null && (schemaInput.getUsedForStaging() || (schemaInput.getMetadata() != null && schemaInput.getMetadata().contains(TNM_TAG_COC_REQUIRED)));
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

        StagingSchema schema = getEodStagingSchema(input);
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

        StagingSchema schema = getEodStagingSchema(input);
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
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "field", param2 = "EOD field name", paramName3 = "valueToCheck", param3 = "value to validate",
            desc = "Returns true if the provided value is valid for the EOD schema corresponding to the inputs and the EOD field, false otherwise. The inputs must contain the keys 'primarySite' and 'histologicTypeIcdO3'; they can optionally contain the keys 'schemaDiscriminator1', 'schemaDiscriminator2', or 'sex'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologicTypeIcdO3' : record.histologicTypeIcdO3\n]\n\nreturn Functions.isAcceptableEodCode(inputs, 'eodPrimaryTumor', record.eodPrimaryTumor)")
    public boolean isAcceptableEodCode(Map<String, String> input, String field, String valueToCheck) {
        if (_eodStaging == null || input == null || field == null)
            return false;

        StagingSchema schema = getEodStagingSchema(input);
        return schema != null && _eodStaging.isCodeValid(schema.getId(), EOD_FIELDS.get(field), valueToCheck);
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
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "field", param2 = "EOD field name",
            desc = "Returns true if the passed EOD field is required for SEER for the schema corresponding to the passed input, "
                    + "false otherwise. Required means either 'seer-required' or 'needed-for-staging'. The inputs must contain the keys 'primarySite' and 'histologicTypeIcdO3'; they can optionaly contain the keys 'schemaDiscriminator1', 'schemaDiscriminator2', or 'sex'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologicTypeIcdO3' : record.histologicTypeIcdO3\n]\n\nreturn Functions.isRequiredEodField(inputs, ''eodPrimaryTumor')")
    public boolean isRequiredEodField(Map<String, String> input, String field) {
        if (_eodStaging == null || input == null || field == null)
            return false;

        StagingSchema schema = getEodStagingSchema(input);
        if (schema == null)
            return false;

        StagingSchemaInput schemaInput = schema.getInputMap().get(EOD_FIELDS.get(field));
        return schemaInput != null && (schemaInput.getUsedForStaging() || (schemaInput.getMetadata() != null && schemaInput.getMetadata().contains(EOD_TAG_SEER_REQUIRED)));
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
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "field", param2 = "EOD field name",
            desc = "Returns true if the passed EOD field is required (needed-for-staging) for SEER for the schema corresponding to the passed input, "
                    + "false otherwise. The inputs must contain the keys 'primarySite' and 'histologicTypeIcdO3'; they can optionally contain the keys 'schemaDiscriminator1', 'schemaDiscriminator2', or 'sex'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologicTypeIcdO3' : record.histologicTypeIcdO3\n]\n\nreturn Functions.isNeededForStagingEodField(inputs, 'eodPrimaryTumor')")
    public boolean isNeededForStagingEodField(Map<String, String> input, String field) {
        if (_eodStaging == null || input == null || field == null)
            return false;

        StagingSchema schema = getEodStagingSchema(input);
        if (schema == null)
            return false;

        StagingSchemaInput schemaInput = schema.getInputMap().get(EOD_FIELDS.get(field));
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
    @ContextFunctionDocAnnotation(paramName1 = "input", param1 = "map of inputs", paramName2 = "field", param2 = "EOD field name",
            desc = "Returns true if the passed EOD field is required for COC for the schema corresponding to the passed input, "
                    + "false otherwise. Required means either 'coc-required' or 'needed-for-staging'. The inputs must contain the keys 'primarySite' and 'histologicTypeIcdO3'; they can optionally contain the keys 'schemaDiscriminator1', 'schemaDiscriminator2', or 'sex'.",
            example = "def inputs = [\n 'primarySite' : record.primarySite,\n 'histologicTypeIcdO3' : record.histologicTypeIcdO3\n]\n\nreturn Functions.isCocRequiredEodField(inputs, 'eodPrimaryTumor')")
    public boolean isCocRequiredEodField(Map<String, String> input, String field) {
        if (_eodStaging == null || input == null || field == null)
            return false;

        StagingSchema schema = getEodStagingSchema(input);
        if (schema == null)
            return false;

        StagingSchemaInput schemaInput = schema.getInputMap().get(EOD_FIELDS.get(field));
        return schemaInput != null && (schemaInput.getUsedForStaging() || (schemaInput.getMetadata() != null && schemaInput.getMetadata().contains(EOD_TAG_COC_REQUIRED)));
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

        SchemaLookup lkup = new SchemaLookup(site, hist);
        lkup.setInput("ssf25", ssf25);

        List<StagingSchema> schemas = _csStaging.lookupSchema(lkup);
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

        SchemaLookup lkup = new SchemaLookup(site, hist);
        lkup.setInput("ssf25", ssf25);
        lkup.setInput("sex", sex);

        List<StagingSchema> schemas = _tnmStaging.lookupSchema(lkup);
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
    public StagingSchema getEodStagingSchema(Map<String, String> input) {
        if (_eodStaging == null || input == null)
            return null;

        String site = input.get(EOD_INPUT_PROP_SITE);
        String hist = input.get(EOD_INPUT_PROP_HIST);
        String disc1 = input.get(EOD_INPUT_PROP_DISC_1);
        String disc2 = input.get(EOD_INPUT_PROP_DISC_2);
        String sex = input.get(EOD_INPUT_PROP_SEX);
        String behav = input.get(EOD_INPUT_PROP_BEHAV);

        SchemaLookup lkup = new SchemaLookup(site, hist);
        lkup.setInput("discriminator_1", disc1);
        lkup.setInput("discriminator_2", disc2);
        lkup.setInput("sex", sex);
        lkup.setInput("behavior", behav);

        List<StagingSchema> schemas = _eodStaging.lookupSchema(lkup);
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
