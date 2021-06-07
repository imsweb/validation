/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.validation.functions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.imsweb.staging.Staging;
import com.imsweb.staging.cs.CsDataProvider;
import com.imsweb.staging.entities.impl.StagingMetadata;
import com.imsweb.staging.entities.impl.StagingSchemaInput;
import com.imsweb.staging.eod.EodDataProvider;
import com.imsweb.staging.tnm.TnmDataProvider;
import com.imsweb.validation.TestingUtils;

public class StagingContextFunctionsTest {

    private StagingContextFunctions _functions;

    @Before
    public void setUp() {
        TestingUtils.init();

        Staging csStaging = Staging.getInstance(CsDataProvider.getInstance(CsDataProvider.CsVersion.LATEST));
        Staging tnmStaging = Staging.getInstance(TnmDataProvider.getInstance(TnmDataProvider.TnmVersion.LATEST));
        Staging eodStaging = Staging.getInstance(EodDataProvider.getInstance(EodDataProvider.EodVersion.LATEST));

        _functions = new StagingContextFunctions(csStaging, tnmStaging, eodStaging);
    }

    @Test
    public void testGetCsVersion() {
        Assert.assertNotNull(_functions.getCsVersion());
    }

    @Test
    public void testGetCsSchemaName() {

        // the input is what will test the function's behavior
        Map<String, String> input = new HashMap<>();

        Assert.assertNull(_functions.getCsSchemaName(null));
        Assert.assertNull(_functions.getCsSchemaName(null));
        Assert.assertNull(_functions.getCsSchemaName(input));
        input.put("primarySite", "C004");
        input.put("histologicTypeIcdO3", "8750");
        input.put("csSiteSpecificFactor25", "30");
        Assert.assertEquals("MelanomaLipLower", _functions.getCsSchemaName(input));
        input.put("primarySite", "C003");
        Assert.assertEquals("MelanomaLipUpper", _functions.getCsSchemaName(input));
        input.put("csSiteSpecificFactor25", null);
        Assert.assertEquals("MelanomaLipUpper", _functions.getCsSchemaName(input));
        input.put("histologicTypeIcdO3", "8720");
        Assert.assertEquals("MelanomaLipUpper", _functions.getCsSchemaName(input));
        input.put("histologicTypeIcdO3", "8790");
        Assert.assertEquals("MelanomaLipUpper", _functions.getCsSchemaName(input));
        input.put("histologicTypeIcdO3", "8719");
        Assert.assertNull(_functions.getCsSchemaName(input));
        input.put("histologicTypeIcdO3", "8791");
        Assert.assertNull(_functions.getCsSchemaName(input));
    }

    @Test
    public void testGetCsSchemaId() {

        // the input is what will test the function's behavior
        Map<String, String> input = new HashMap<>();

        Assert.assertNull(_functions.getCsSchemaId(null));
        Assert.assertNull(_functions.getCsSchemaId(null));
        Assert.assertNull(_functions.getCsSchemaId(input));
        input.put("primarySite", "C004");
        input.put("histologicTypeIcdO3", "8750");
        input.put("csSiteSpecificFactor25", "30");
        Assert.assertEquals("melanoma_lip_lower", _functions.getCsSchemaId(input));
        input.put("primarySite", "C003");
        Assert.assertEquals("melanoma_lip_upper", _functions.getCsSchemaId(input));
        input.put("csSiteSpecificFactor25", null);
        Assert.assertEquals("melanoma_lip_upper", _functions.getCsSchemaId(input));
        input.put("histologicTypeIcdO3", "8720");
        Assert.assertEquals("melanoma_lip_upper", _functions.getCsSchemaId(input));
        input.put("histologicTypeIcdO3", "8790");
        Assert.assertEquals("melanoma_lip_upper", _functions.getCsSchemaId(input));
        input.put("histologicTypeIcdO3", "8719");
        Assert.assertNull(_functions.getCsSchemaId(input));
        input.put("histologicTypeIcdO3", "8791");
        Assert.assertNull(_functions.getCsSchemaId(input));
    }

    @Test
    public void testIsAcceptableCsCode() {

        // C447/8000 and csTumorSize, expecting [989, 994, 995, 001-988, 992, 993, 990, 991, 000, 999]
        Map<String, String> input = new HashMap<>();
        input.put("primarySite", "C447");
        input.put("histologicTypeIcdO3", "8000");
        input.put("csSiteSpecificFactor25", null);

        Assert.assertTrue(_functions.isAcceptableCsCode(input, "csTumorSize", "000"));
        Assert.assertTrue(_functions.isAcceptableCsCode(input, "csTumorSize", "001"));
        Assert.assertTrue(_functions.isAcceptableCsCode(input, "csTumorSize", "050"));
        Assert.assertTrue(_functions.isAcceptableCsCode(input, "csTumorSize", "988"));
        Assert.assertTrue(_functions.isAcceptableCsCode(input, "csTumorSize", "991"));
        Assert.assertFalse(_functions.isAcceptableCsCode(input, "csTumorSize", "996"));
        Assert.assertFalse(_functions.isAcceptableCsCode(input, "csTumorSize", null));
        Assert.assertFalse(_functions.isAcceptableCsCode(input, "csTumorSize", ""));
        Assert.assertFalse(_functions.isAcceptableCsCode(input, "csTumorSize", "xyz"));
        Assert.assertFalse(_functions.isAcceptableCsCode(input, "csTumorSize", "-5"));
        Assert.assertFalse(_functions.isAcceptableCsCode(input, "csTumorSize", "99999"));
        Assert.assertFalse(_functions.isAcceptableCsCode(input, null, "000"));
        Assert.assertFalse(_functions.isAcceptableCsCode(input, "", "000"));
        Assert.assertFalse(_functions.isAcceptableCsCode(input, "zyz", "000"));
        Assert.assertFalse(_functions.isAcceptableCsCode(null, "csTumorSize", "000"));

        input = new HashMap<>();
        input.put("primarySite", "C619");
        input.put("histologicTypeIcdO3", "8000");
        input.put("csSiteSpecificFactor25", null);
        Assert.assertTrue(_functions.isAcceptableCsCode(input, "csSiteSpecificFactor25", "988"));
    }

    @Test
    public void testIsObsoleteCsCode() {

        // C180/8000 and csMetsAtDx with value of 40 (obsolete)
        Map<String, String> input = new HashMap<>();
        input.put("primarySite", "C180");
        input.put("histologicTypeIcdO3", "8000");
        input.put("csSiteSpecificFactor25", null);

        //assertTrue(_functions.isObsoleteCsCode(input, "csMetsAtDx", "40"));
        Assert.assertFalse(_functions.isObsoleteCsCode(input, "csMetsAtDx", "00"));
        Assert.assertFalse(_functions.isObsoleteCsCode(input, "csMetsAtDx", null));
        Assert.assertFalse(_functions.isObsoleteCsCode(input, null, "00"));
        Assert.assertFalse(_functions.isObsoleteCsCode(null, "csMetsAtDx", "00"));
        Assert.assertFalse(_functions.isObsoleteCsCode(input, "zyz", "00"));
        Assert.assertFalse(_functions.isObsoleteCsCode(input, "csMetsAtDx", "-5"));

        input.clear();
        input.put("primarySite", "C696");
        input.put("histologicTypeIcdO3", "9699");
        input.put("csSiteSpecificFactor25", "988");
        Assert.assertFalse(_functions.isObsoleteCsCode(input, "csSiteSpecificFactor5", "988"));
    }

    @Test
    public void testIsRequiredCsCode() {

        // C619/8000 -> Prostate [1,3,8,10,2,7,9,11,12,13]
        Map<String, String> input = new HashMap<>();
        input.put("primarySite", "C619");
        input.put("histologicTypeIcdO3", "8000");
        input.put("csSiteSpecificFactor25", null);

        Assert.assertTrue(_functions.isRequiredCsCode(input, 1));
        Assert.assertTrue(_functions.isRequiredCsCode(input, 2));
        Assert.assertFalse(_functions.isRequiredCsCode(input, 23));
    }

    @Test
    public void testIsAlreadyCollectedCsCode() {

        // C619/8000 -> Prostate [blank]
        Map<String, String> input = new HashMap<>();
        input.put("primarySite", "C619");
        input.put("histologicTypeIcdO3", "8000");
        input.put("csSiteSpecificFactor25", null);

        Assert.assertFalse(_functions.isAlreadyCollectedCsCode(input, 1));
        Assert.assertFalse(_functions.isAlreadyCollectedCsCode(input, 2));
        Assert.assertFalse(_functions.isAlreadyCollectedCsCode(input, 23));
    }

    @Test
    public void testIsNeededForStagingCsCode() {

        // C619/8000 -> Prostate [1,3,8,10]
        Map<String, String> input = new HashMap<>();
        input.put("primarySite", "C619");
        input.put("histologicTypeIcdO3", "8000");
        input.put("csSiteSpecificFactor25", null);

        Assert.assertTrue(_functions.isNeededForStagingCsCode(input, 1));
        Assert.assertFalse(_functions.isNeededForStagingCsCode(input, 2));
        Assert.assertFalse(_functions.isNeededForStagingCsCode(input, 23));
    }

    @Test
    public void testIsClinicallySignificantCsCode() {

        // C619/8000 -> Prostate [2,7,9,11,12,13]
        Map<String, String> input = new HashMap<>();
        input.put("primarySite", "C619");
        input.put("histologicTypeIcdO3", "8000");
        input.put("csSiteSpecificFactor25", null);

        Assert.assertFalse(_functions.isClinicallySignificantCsCode(input, 1));
        Assert.assertTrue(_functions.isClinicallySignificantCsCode(input, 2));
        Assert.assertFalse(_functions.isClinicallySignificantCsCode(input, 23));
    }

    @Test
    public void testIsRequiredPre2010CsCode() {

        // C619/8000 -> Prostate [4]
        Map<String, String> input = new HashMap<>();
        input.put("primarySite", "C619");
        input.put("histologicTypeIcdO3", "8000");
        input.put("csSiteSpecificFactor25", null);

        Assert.assertFalse(_functions.isRequiredPre2010CsCode(input, 1));
        Assert.assertTrue(_functions.isRequiredPre2010CsCode(input, 4));
        Assert.assertFalse(_functions.isRequiredPre2010CsCode(input, 23));
    }

    @Test
    public void testIsCocRequiredCsCode() {

        // C619/8000 -> Prostate [1,3,8,10,2,7,9,11,12,13]
        Map<String, String> input = new HashMap<>();
        input.put("primarySite", "C619");
        input.put("histologicTypeIcdO3", "8000");
        input.put("csSiteSpecificFactor25", null);

        Assert.assertTrue(_functions.isCocRequiredCsCode(input, 1));
        Assert.assertTrue(_functions.isCocRequiredCsCode(input, 2));
        Assert.assertFalse(_functions.isCocRequiredCsCode(input, 23));
    }

    @Test
    public void testIsCocAlreadyCollectedCsCode() {

        // C619/8000 -> Prostate [blank]
        Map<String, String> input = new HashMap<>();
        input.put("primarySite", "C619");
        input.put("histologicTypeIcdO3", "8000");
        input.put("csSiteSpecificFactor25", null);

        Assert.assertFalse(_functions.isCocAlreadyCollectedCsCode(input, 1));
        Assert.assertFalse(_functions.isCocAlreadyCollectedCsCode(input, 2));
        Assert.assertFalse(_functions.isCocAlreadyCollectedCsCode(input, 23));
    }

    @Test
    public void testIsCocNeededForStagingCsCode() {

        // C619/8000 -> Prostate [1,3,8,10]
        Map<String, String> input = new HashMap<>();
        input.put("primarySite", "C619");
        input.put("histologicTypeIcdO3", "8000");
        input.put("csSiteSpecificFactor25", null);

        Assert.assertTrue(_functions.isCocNeededForStagingCsCode(input, 1));
        Assert.assertFalse(_functions.isCocNeededForStagingCsCode(input, 2));
        Assert.assertFalse(_functions.isCocNeededForStagingCsCode(input, 23));
    }

    @Test
    public void testIsCocClinicallySignificantCsCode() {

        // C619/8000 -> Prostate [2,7,9,11,12,13]
        Map<String, String> input = new HashMap<>();
        input.put("primarySite", "C619");
        input.put("histologicTypeIcdO3", "8000");
        input.put("csSiteSpecificFactor25", null);

        Assert.assertFalse(_functions.isCocClinicallySignificantCsCode(input, 1));
        Assert.assertTrue(_functions.isCocClinicallySignificantCsCode(input, 2));
        Assert.assertFalse(_functions.isCocClinicallySignificantCsCode(input, 23));
    }

    @Test
    public void testIsCocRequiredPre2010CsCode() {

        // C619/8000 -> Prostate [4]
        Map<String, String> input = new HashMap<>();
        input.put("primarySite", "C619");
        input.put("histologicTypeIcdO3", "8000");
        input.put("csSiteSpecificFactor25", null);

        Assert.assertFalse(_functions.isCocRequiredPre2010CsCode(input, 1));
        Assert.assertTrue(_functions.isCocRequiredPre2010CsCode(input, 4));
        Assert.assertFalse(_functions.isCocRequiredPre2010CsCode(input, 23));
    }

    @Test
    public void testGetCsObsoleteReason() {
        Assert.assertNull(_functions.getCsObsoleteReason(new HashMap<>(), "", ""));

        // testing a single case (Bladder); the goal here is not to test the Obsolete logic, just that the call through is successful
        Map<String, String> input = new HashMap<>();
        input.put("primarySite", "C670");
        input.put("histologicTypeIcdO3", "8000");
        input.put("csSiteSpecificFactor25", "988");
        input.put("_csSchemaName", "Bladder");
        Assert.assertEquals("14", _functions.getCsObsoleteReason(input, "csExtension", "600"));
        Assert.assertEquals("13", _functions.getCsObsoleteReason(input, "csExtension", "730"));
        Assert.assertEquals("7", _functions.getCsObsoleteReason(input, "csMetsAtDx", "10"));
    }

    @Test
    public void testGetTnmVersion() {
        Assert.assertNotNull(_functions.getTnmVersion());
    }

    @Test
    public void testGetTnmSchema() {
        Map<String, String> input = new HashMap<>();
        Assert.assertNull(_functions.getTnmStagingSchema(null));
        Assert.assertNull(_functions.getTnmStagingSchema(input));

        input.put("primarySite", "C481");
        input.put("histologicTypeIcdO3", "8000");
        input.put("csSiteSpecificFactor25", null);
        Assert.assertNull(_functions.getTnmStagingSchema(input));

        input.put("sex", "1");
        Assert.assertNotNull(_functions.getTnmStagingSchema(input));

        input.put("csSiteSpecificFactor25", "010");
        Assert.assertNotNull(_functions.getTnmStagingSchema(input));

        input.put("primarySite", "C111");
        Assert.assertNotNull(_functions.getTnmStagingSchema(input));

        input.put("csSiteSpecificFactor25", null);
        Assert.assertNull(_functions.getTnmStagingSchema(input));
    }

    @Test
    public void testGetTnmSchemaName() {
        Map<String, String> input = new HashMap<>();
        Assert.assertNull(_functions.getTnmSchemaName(null));
        Assert.assertNull(_functions.getTnmSchemaName(input));

        input.put("primarySite", "C004");
        input.put("histologicTypeIcdO3", "8750");
        input.put("csSiteSpecificFactor25", "30");
        Assert.assertEquals("Melanoma Lip Lower", _functions.getTnmSchemaName(input));
        input.put("primarySite", "C003");
        Assert.assertEquals("Melanoma Lip Upper", _functions.getTnmSchemaName(input));
        input.put("csSiteSpecificFactor25", null);
        Assert.assertEquals("Melanoma Lip Upper", _functions.getTnmSchemaName(input));
        input.put("histologicTypeIcdO3", "8720");
        Assert.assertEquals("Melanoma Lip Upper", _functions.getTnmSchemaName(input));
        input.put("histologicTypeIcdO3", "8790");
        Assert.assertEquals("Melanoma Lip Upper", _functions.getTnmSchemaName(input));
        input.put("histologicTypeIcdO3", "8719");
        Assert.assertNull(_functions.getTnmSchemaName(input));
        input.put("histologicTypeIcdO3", "8791");
        Assert.assertNull(_functions.getTnmSchemaName(input));
    }

    @Test
    public void testGetTnmSchemaId() {
        Map<String, String> input = new HashMap<>();
        Assert.assertNull(_functions.getTnmSchemaId(null));
        Assert.assertNull(_functions.getTnmSchemaId(input));

        input.put("primarySite", "C004");
        input.put("histologicTypeIcdO3", "8750");
        input.put("csSiteSpecificFactor25", "30");
        Assert.assertEquals("melanoma_lip_lower", _functions.getTnmSchemaId(input));
        input.put("primarySite", "C003");
        Assert.assertEquals("melanoma_lip_upper", _functions.getTnmSchemaId(input));
        input.put("csSiteSpecificFactor25", null);
        Assert.assertEquals("melanoma_lip_upper", _functions.getTnmSchemaId(input));
        input.put("histologicTypeIcdO3", "8720");
        Assert.assertEquals("melanoma_lip_upper", _functions.getTnmSchemaId(input));
        input.put("histologicTypeIcdO3", "8790");
        Assert.assertEquals("melanoma_lip_upper", _functions.getTnmSchemaId(input));
        input.put("histologicTypeIcdO3", "8719");
        Assert.assertNull(_functions.getTnmSchemaId(input));
        input.put("histologicTypeIcdO3", "8791");
        Assert.assertNull(_functions.getTnmSchemaId(input));
        input.put("primarySite", "C481");
        input.put("histologicTypeIcdO3", "8000");
        input.put("csSiteSpecificFactor25", null);
        input.put("sex", "2");
        Assert.assertEquals("peritoneum_female_gen", _functions.getTnmSchemaId(input));
        input.put("sex", null);
        Assert.assertNull(_functions.getTnmSchemaId(input));
    }

    @Test
    public void testIsAcceptableTnmCode() {

        // C447/8000 and tnmClinT, expecting [88, c0, c1, c2, c3, c4, cX, pIS, blank]
        Map<String, String> input = new HashMap<>();
        input.put("primarySite", "C447");
        input.put("histologicTypeIcdO3", "8000");
        input.put("csSiteSpecificFactor25", null);

        Assert.assertFalse(_functions.isAcceptableTnmCode(input, "csTumorSize", "000"));
        Assert.assertTrue(_functions.isAcceptableTnmCode(input, "tnmClinT", "88"));
        Assert.assertTrue(_functions.isAcceptableTnmCode(input, "tnmClinT", "c0"));
        Assert.assertTrue(_functions.isAcceptableTnmCode(input, "tnmClinT", "c4"));
        Assert.assertTrue(_functions.isAcceptableTnmCode(input, "tnmClinT", "cX"));
        Assert.assertFalse(_functions.isAcceptableTnmCode(input, "tnmClinT", "c5"));
        Assert.assertTrue(_functions.isAcceptableTnmCode(input, "tnmClinT", null));
        Assert.assertTrue(_functions.isAcceptableTnmCode(input, "tnmClinT", ""));
        Assert.assertFalse(_functions.isAcceptableTnmCode(input, "tnmClinT", "xyz"));
        Assert.assertFalse(_functions.isAcceptableTnmCode(input, "tnmClinT", "-5"));
        Assert.assertFalse(_functions.isAcceptableTnmCode(input, "tnmClinT", "99999"));
        Assert.assertFalse(_functions.isAcceptableTnmCode(input, null, "000"));
        Assert.assertFalse(_functions.isAcceptableTnmCode(input, "", "000"));
        Assert.assertFalse(_functions.isAcceptableTnmCode(input, "zyz", "000"));
        Assert.assertFalse(_functions.isAcceptableTnmCode(null, "tnmClinT", "000"));

        // C447/8000 and clinStageGroup, expecting [0, 1, 2, 3, 4, 99, 88]
        Assert.assertTrue(_functions.isAcceptableTnmCode(input, "tnmClinStageGroup", "88"));
        Assert.assertTrue(_functions.isAcceptableTnmCode(input, "tnmClinStageGroup", "0"));
        Assert.assertTrue(_functions.isAcceptableTnmCode(input, "tnmClinStageGroup", "4"));
        Assert.assertTrue(_functions.isAcceptableTnmCode(input, "tnmClinStageGroup", "99"));
        Assert.assertFalse(_functions.isAcceptableTnmCode(input, "tnmClinStageGroup", "c5"));
        Assert.assertFalse(_functions.isAcceptableTnmCode(input, "tnmClinStageGroup", null));
        Assert.assertFalse(_functions.isAcceptableTnmCode(input, "tnmClinStageGroup", ""));
    }

    @Test
    public void testIsRequiredTnmCode() {

        // C619/8000 -> Prostate [1,8,10,2,7,9,12,13]
        Map<String, String> input = new HashMap<>();
        input.put("primarySite", "C619");
        input.put("histologicTypeIcdO3", "8000");
        input.put("csSiteSpecificFactor25", null);

        Assert.assertTrue(_functions.isRequiredTnmCode(input, 1));
        Assert.assertTrue(_functions.isRequiredTnmCode(input, 2));
        Assert.assertFalse(_functions.isRequiredTnmCode(input, 23));
    }

    @Test
    public void testIsNeededForStagingTnmCode() {

        // C619/8000 -> Prostate [1,8,10]
        Map<String, String> input = new HashMap<>();
        input.put("primarySite", "C619");
        input.put("histologicTypeIcdO3", "8000");
        input.put("csSiteSpecificFactor25", null);

        Assert.assertTrue(_functions.isNeededForStagingTnmCode(input, 1));
        Assert.assertFalse(_functions.isNeededForStagingTnmCode(input, 2));
        Assert.assertTrue(_functions.isNeededForStagingTnmCode(input, 8));
        Assert.assertFalse(_functions.isNeededForStagingTnmCode(input, 23));
    }

    @Test
    public void testIsCocRequiredTnmCode() {

        // C619/8000 -> Prostate [1,2,7,8,9,10,11,12,13]
        Map<String, String> input = new HashMap<>();
        input.put("primarySite", "C619");
        input.put("histologicTypeIcdO3", "8000");
        input.put("csSiteSpecificFactor25", null);

        Assert.assertFalse(_functions.isCocRequiredTnmCode(input, 3));
        Assert.assertTrue(_functions.isCocRequiredTnmCode(input, 2));
        Assert.assertTrue(_functions.isCocRequiredTnmCode(input, 12));
        Assert.assertFalse(_functions.isCocRequiredTnmCode(input, 23));
    }

    @Test
    public void testGetEodVersion() {
        Assert.assertNotNull(_functions.getEodVersion());
    }

    @Test
    public void testGetEodSchema() {
        Map<String, String> input = new HashMap<>();
        Assert.assertNull(_functions.getEodStagingSchema(null));
        Assert.assertNull(_functions.getEodStagingSchema(input));

        input.put("primarySite", "C481");
        input.put("histologicTypeIcdO3", "8000");
        Assert.assertNull(_functions.getEodStagingSchema(input));

        input.put("sex", "1");
        Assert.assertNotNull(_functions.getEodStagingSchema(input)); // retroperitoneum

        input.put("schemaDiscriminator1", "1");
        Assert.assertNotNull(_functions.getEodStagingSchema(input));

        input.put("primarySite", "C111");
        Assert.assertNotNull(_functions.getEodStagingSchema(input)); // nasopharynx

        input.put("schemaDiscriminator1", "2");
        Assert.assertNull(_functions.getEodStagingSchema(input));

        input.put("schemaDiscriminator2", "2");
        Assert.assertNotNull(_functions.getEodStagingSchema(input)); // oropharynx_hpv_mediated_p16_pos

        input.put("primarySite", "C700");
        input.put("histologicTypeIcdO3", "8710");
        input.put("behaviorCodeIcdO3", "0");
        input.put("schemaDiscriminator1", null);
        input.put("schemaDiscriminator2", null);
        Assert.assertNotNull(_functions.getEodStagingSchema(input)); // brain
    }

    @Test
    public void testGetEodSchemaName() {
        Map<String, String> input = new HashMap<>();
        Assert.assertNull(_functions.getEodSchemaName(null));
        Assert.assertNull(_functions.getEodSchemaName(input));

        input.put("primarySite", "C004");
        input.put("histologicTypeIcdO3", "8750");
        input.put("schemaDiscriminator1", "0");
        Assert.assertEquals("Melanoma Head and Neck", _functions.getEodSchemaName(input));
        input.put("primarySite", "C003");
        Assert.assertEquals("Melanoma Head and Neck", _functions.getEodSchemaName(input));
        input.put("schemaDiscriminator1", null);
        Assert.assertEquals("Melanoma Head and Neck", _functions.getEodSchemaName(input));
        input.put("histologicTypeIcdO3", "8720");
        Assert.assertEquals("Melanoma Head and Neck", _functions.getEodSchemaName(input));
        input.put("histologicTypeIcdO3", "8790");
        Assert.assertEquals("Melanoma Head and Neck", _functions.getEodSchemaName(input));
        input.put("histologicTypeIcdO3", "8719");
        Assert.assertNull(_functions.getEodSchemaName(input));
        input.put("histologicTypeIcdO3", "8791");
        Assert.assertNull(_functions.getEodSchemaName(input));
    }

    @Test
    public void testGetEodSchemaId() {
        Map<String, String> input = new HashMap<>();
        Assert.assertNull(_functions.getEodSchemaId(null));
        Assert.assertNull(_functions.getEodSchemaId(input));

        input.put("primarySite", "C004");
        input.put("histologicTypeIcdO3", "8750");
        input.put("schemaDiscriminator1", "0");
        Assert.assertEquals("melanoma_head_neck", _functions.getEodSchemaId(input));
        input.put("primarySite", "C003");
        Assert.assertEquals("melanoma_head_neck", _functions.getEodSchemaId(input));
        input.put("schemaDiscriminator1", null);
        Assert.assertEquals("melanoma_head_neck", _functions.getEodSchemaId(input));
        input.put("histologicTypeIcdO3", "8720");
        Assert.assertEquals("melanoma_head_neck", _functions.getEodSchemaId(input));
        input.put("histologicTypeIcdO3", "8790");
        Assert.assertEquals("melanoma_head_neck", _functions.getEodSchemaId(input));
        input.put("histologicTypeIcdO3", "8719");
        Assert.assertNull(_functions.getEodSchemaId(input));
        input.put("histologicTypeIcdO3", "8791");
        Assert.assertNull(_functions.getEodSchemaId(input));
        input.put("primarySite", "C481");
        input.put("histologicTypeIcdO3", "8000");
        input.put("schemaDiscriminator1", null);
        input.put("sex", "2");
        Assert.assertEquals("primary_peritoneal_carcinoma", _functions.getEodSchemaId(input));
        input.put("sex", null);
        Assert.assertNull(_functions.getEodSchemaId(input));
    }

    @Test
    public void testIsAcceptableEodCode() {

        // C447/8000 and eodPrimaryTumor, expecting [000, 100, 200, 700, 800, 999]
        Map<String, String> input = new HashMap<>();
        input.put("primarySite", "C447");
        input.put("histologicTypeIcdO3", "8000");
        input.put("csSiteSpecificFactor25", null);

        Assert.assertFalse(_functions.isAcceptableEodCode(input, "gradeClinical", "000"));
        Assert.assertTrue(_functions.isAcceptableEodCode(input, "eodPrimaryTumor", "000"));
        Assert.assertTrue(_functions.isAcceptableEodCode(input, "eodPrimaryTumor", "100"));
        Assert.assertTrue(_functions.isAcceptableEodCode(input, "eodPrimaryTumor", "700"));
        Assert.assertTrue(_functions.isAcceptableEodCode(input, "eodPrimaryTumor", "999"));
        Assert.assertFalse(_functions.isAcceptableEodCode(input, "eodPrimaryTumor", "888"));
        Assert.assertFalse(_functions.isAcceptableEodCode(input, "eodPrimaryTumor", null));
        Assert.assertFalse(_functions.isAcceptableEodCode(input, "eodPrimaryTumor", ""));
        Assert.assertFalse(_functions.isAcceptableEodCode(input, "eodPrimaryTumor", "xyz"));
        Assert.assertFalse(_functions.isAcceptableEodCode(input, "eodPrimaryTumor", "-5"));
        Assert.assertFalse(_functions.isAcceptableEodCode(input, "eodPrimaryTumor", "99999"));
        Assert.assertFalse(_functions.isAcceptableEodCode(input, null, "000"));
        Assert.assertFalse(_functions.isAcceptableEodCode(input, "", "000"));
        Assert.assertFalse(_functions.isAcceptableEodCode(input, "zyz", "000"));
        Assert.assertFalse(_functions.isAcceptableEodCode(null, "eodPrimaryTumor", "cX"));

        // C447/8000 and summaryStage2018, expecting [0, 1, 2, 3, 4, 7, 9]
        Assert.assertTrue(_functions.isAcceptableEodCode(input, "summaryStage2018", "1"));
        Assert.assertTrue(_functions.isAcceptableEodCode(input, "summaryStage2018", "0"));
        Assert.assertTrue(_functions.isAcceptableEodCode(input, "summaryStage2018", "4"));
        Assert.assertTrue(_functions.isAcceptableEodCode(input, "summaryStage2018", "9"));
        Assert.assertFalse(_functions.isAcceptableEodCode(input, "summaryStage2018", "c5"));
        Assert.assertFalse(_functions.isAcceptableEodCode(input, "summaryStage2018", null));
        Assert.assertFalse(_functions.isAcceptableEodCode(input, "summaryStage2018", ""));
    }

    @Test
    public void testIsRequiredEodField() {

        // C619/8000 -> Prostate [psa, prostatePathologicalExtension, numberOfCoresPositive, numberOfCoresExamined, gleasonTertiaryPattern, 
        //                        gleasonScoreClinical, gleasonPatternsClinical, gleasonScorePathological, gleasonPatternsPathological]
        Map<String, String> input = new HashMap<>();
        input.put("primarySite", "C619");
        input.put("histologicTypeIcdO3", "8000");
        input.put("schemaDiscriminator1", null);

        Assert.assertTrue(_functions.isRequiredEodField(input, "gleasonPatternsClinical"));
        Assert.assertTrue(_functions.isRequiredEodField(input, "psaLabValue"));
        Assert.assertFalse(_functions.isRequiredEodField(input, "regionalNodesPositive"));
    }

    @Test
    public void testIsNeededForStagingEodField() {

        // C619/8000 -> Prostate [psa, prostatePathologicalExtension]
        Map<String, String> input = new HashMap<>();
        input.put("primarySite", "C619");
        input.put("histologicTypeIcdO3", "8000");
        input.put("schemaDiscriminator1", null);

        Assert.assertTrue(_functions.isNeededForStagingEodField(input, "psaLabValue"));
        Assert.assertFalse(_functions.isNeededForStagingEodField(input, "gleasonPatternsClinical"));
        Assert.assertTrue(_functions.isNeededForStagingEodField(input, "prostatePathologicalExtension"));
        Assert.assertFalse(_functions.isNeededForStagingEodField(input, "regionalNodesPositive"));
    }

    @Test
    public void testIsCocRequiredEodCode() {

        // C619/8000 -> Prostate [psa, prostatePathologicalExtension, numberOfCoresPositive, numberOfCoresExamined, gleasonTertiaryPattern, 
        //                        gleasonScoreClinical, gleasonPatternsClinical, gleasonScorePathological, gleasonPatternsPathological]
        Map<String, String> input = new HashMap<>();
        input.put("primarySite", "C619");
        input.put("histologicTypeIcdO3", "8000");
        input.put("schemaDiscriminator1", null);

        Assert.assertTrue(_functions.isCocRequiredEodField(input, "psaLabValue"));
        Assert.assertTrue(_functions.isCocRequiredEodField(input, "gleasonPatternsClinical"));
        Assert.assertFalse(_functions.isCocRequiredEodField(input, "regionalNodesPositive"));
    }

    @Test
    public void testExpandKeys() {
        Assert.assertNotNull(_functions.expandKeys(Collections.singletonMap("1-9", "A")));
    }

    @Test
    public void testExpandList() {
        Assert.assertNotNull(_functions.expandList(Collections.singletonList("1-9")));
    }

    @Test
    public void testGetSsf25FromSex() {
        // test sex conditions
        Assert.assertEquals("001", _functions.getSsf25FromSex(null, "1", "8000", "2016", "peritoneum"));
        Assert.assertEquals("002", _functions.getSsf25FromSex(null, "2", "8000", "2016", "peritoneum"));
        Assert.assertEquals("003", _functions.getSsf25FromSex(null, "3", "8000", "2016", "peritoneum"));
        Assert.assertEquals("004", _functions.getSsf25FromSex(null, "4", "8000", "2016", "peritoneum"));
        Assert.assertEquals("001", _functions.getSsf25FromSex(null, "5", "8000", "2016", "peritoneum"));
        Assert.assertEquals("002", _functions.getSsf25FromSex(null, "6", "8000", "2016", "peritoneum"));
        Assert.assertEquals("009", _functions.getSsf25FromSex(null, "9", "8000", "2016", "peritoneum"));
        Assert.assertEquals("009", _functions.getSsf25FromSex(null, null, "8000", "2016", "peritoneum"));
        Assert.assertEquals("009", _functions.getSsf25FromSex(null, "", "8000", "2016", "peritoneum"));
        Assert.assertEquals("009", _functions.getSsf25FromSex(null, "7", "8000", "2016", "peritoneum"));

        // test hist, schema, and dx year conditions
        Assert.assertEquals("001", _functions.getSsf25FromSex(null, "1", "8000", "2016", "peritoneum_female_gen"));
        Assert.assertEquals("001", _functions.getSsf25FromSex(null, "1", "8000", "2017", "peritoneum"));
        Assert.assertNull(_functions.getSsf25FromSex(null, "1", "8000", "2016", "larynx_other"));
        Assert.assertNull(_functions.getSsf25FromSex(null, "1", "8000", "2015", "peritoneum"));
        Assert.assertEquals("981", _functions.getSsf25FromSex(null, "1", "8577", "2016", "peritoneum"));

        // test ssf25 condition
        Assert.assertEquals("001", _functions.getSsf25FromSex("001", null, "8000", "2016", "peritoneum"));
        Assert.assertEquals("001", _functions.getSsf25FromSex("001", "2", "8000", "2016", "peritoneum"));
        Assert.assertEquals("002", _functions.getSsf25FromSex("988", "2", "8000", "2016", "peritoneum"));
        Assert.assertEquals("002", _functions.getSsf25FromSex("   ", "2", "8000", "2016", "peritoneum"));
        Assert.assertEquals("002", _functions.getSsf25FromSex("   ", "2", "8000", "2016", "peritoneum"));

        Assert.assertNull(_functions.getSsf25FromSex(null, null, null, null, null));
        Assert.assertEquals("981", _functions.getSsf25FromSex(null, null, null, "2016", "peritoneum"));
    }

    @Test
    public void testExtractDxYear() {
        Assert.assertNull(_functions.extractDxYear(null));
        Assert.assertNull(_functions.extractDxYear(Collections.emptyMap()));
        Assert.assertEquals(1234, _functions.extractDxYear(Collections.singletonMap("dateOfDiagnosisYear", "1234")).intValue());
        Assert.assertEquals(1234, _functions.extractDxYear(Collections.singletonMap("dateOfDiagnosis", "12340101")).intValue());
        Assert.assertEquals(1234, _functions.extractDxYear(Collections.singletonMap("dateOfDiagnosis", "123401")).intValue());
        Assert.assertEquals(1234, _functions.extractDxYear(Collections.singletonMap("dateOfDiagnosis", "1234")).intValue());
        Map<String, String> map = new HashMap<>();
        map.put("dateOfDiagnosisYear", "1234");
        map.put("dateOfDiagnosis", "56780101");
        Assert.assertEquals(1234, _functions.extractDxYear(map).intValue());
    }

    @Test
    public void testCheckMetaData() {
        Map<String, String> input = Collections.singletonMap("dateOfDiagnosisYear", "2020");

        StagingSchemaInput schemaInput = new StagingSchemaInput();
        Assert.assertFalse(_functions.checkMetaData(input, schemaInput, "TAG"));
        Assert.assertFalse(_functions.checkMetaData(input, schemaInput, "OTHER"));

        StagingMetadata metadata = new StagingMetadata("TAG");
        schemaInput.setMetadata(Collections.singletonList(metadata));
        Assert.assertTrue(_functions.checkMetaData(input, schemaInput, "TAG"));
        Assert.assertFalse(_functions.checkMetaData(input, schemaInput, "OTHER"));

        metadata.setStart(2019);
        Assert.assertTrue(_functions.checkMetaData(input, schemaInput, "TAG"));
        metadata.setStart(2020);
        Assert.assertTrue(_functions.checkMetaData(input, schemaInput, "TAG"));
        metadata.setStart(2021);
        Assert.assertFalse(_functions.checkMetaData(input, schemaInput, "TAG"));
        metadata.setStart(null);

        metadata.setEnd(2019);
        Assert.assertFalse(_functions.checkMetaData(input, schemaInput, "TAG"));
        metadata.setEnd(2020);
        Assert.assertTrue(_functions.checkMetaData(input, schemaInput, "TAG"));
        metadata.setEnd(2021);
        Assert.assertTrue(_functions.checkMetaData(input, schemaInput, "TAG"));
        metadata.setEnd(null);

        metadata.setStart(2000);
        metadata.setStart(2025);
        input = Collections.singletonMap("dateOfDiagnosisYear", null);
        Assert.assertFalse(_functions.checkMetaData(input, schemaInput, "TAG"));
    }
}
