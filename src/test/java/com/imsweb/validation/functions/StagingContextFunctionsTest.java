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

import com.imsweb.validation.TestingUtils;

public class StagingContextFunctionsTest {

    private StagingContextFunctions _functions = new StagingContextFunctions();

    @Before
    public void setUp() throws Exception {
        TestingUtils.init();
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
        input.put("histologyIcdO3", "8750");
        input.put("csSiteSpecificFactor25", "30");
        Assert.assertEquals("MelanomaLipLower", _functions.getCsSchemaName(input));
        input.put("primarySite", "C003");
        Assert.assertEquals("MelanomaLipUpper", _functions.getCsSchemaName(input));
        input.put("csSiteSpecificFactor25", null);
        Assert.assertEquals("MelanomaLipUpper", _functions.getCsSchemaName(input));
        input.put("histologyIcdO3", "8720");
        Assert.assertEquals("MelanomaLipUpper", _functions.getCsSchemaName(input));
        input.put("histologyIcdO3", "8790");
        Assert.assertEquals("MelanomaLipUpper", _functions.getCsSchemaName(input));
        input.put("histologyIcdO3", "8719");
        Assert.assertNull(_functions.getCsSchemaName(input));
        input.put("histologyIcdO3", "8791");
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
        input.put("histologyIcdO3", "8750");
        input.put("csSiteSpecificFactor25", "30");
        Assert.assertEquals("melanoma_lip_lower", _functions.getCsSchemaId(input));
        input.put("primarySite", "C003");
        Assert.assertEquals("melanoma_lip_upper", _functions.getCsSchemaId(input));
        input.put("csSiteSpecificFactor25", null);
        Assert.assertEquals("melanoma_lip_upper", _functions.getCsSchemaId(input));
        input.put("histologyIcdO3", "8720");
        Assert.assertEquals("melanoma_lip_upper", _functions.getCsSchemaId(input));
        input.put("histologyIcdO3", "8790");
        Assert.assertEquals("melanoma_lip_upper", _functions.getCsSchemaId(input));
        input.put("histologyIcdO3", "8719");
        Assert.assertNull(_functions.getCsSchemaId(input));
        input.put("histologyIcdO3", "8791");
        Assert.assertNull(_functions.getCsSchemaId(input));
    }

    @Test
    public void testIsAcceptableCsCode() {

        // C447/8000 and csTumorSize, expecting [989, 994, 995, 001-988, 992, 993, 990, 991, 000, 999]
        Map<String, String> input = new HashMap<>();
        input.put("primarySite", "C447");
        input.put("histologyIcdO3", "8000");
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
        input.put("histologyIcdO3", "8000");
        input.put("csSiteSpecificFactor25", null);
        Assert.assertTrue(_functions.isAcceptableCsCode(input, "csSiteSpecFact25", "988"));
    }

    @Test
    public void testIsObsoleteCsCode() {

        // C180/8000 and csMetsAtDx with value of 40 (obsolete)
        Map<String, String> input = new HashMap<>();
        input.put("primarySite", "C180");
        input.put("histologyIcdO3", "8000");
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
        input.put("histologyIcdO3", "9699");
        input.put("csSiteSpecificFactor25", "988");
        Assert.assertFalse(_functions.isObsoleteCsCode(input, "csSiteSpecFact5", "988"));
    }

    @Test
    public void testIsRequiredCsCode() {

        // C619/8000 -> Prostate [1,3,8,10,2,7,9,11,12,13]
        Map<String, String> input = new HashMap<>();
        input.put("primarySite", "C619");
        input.put("histologyIcdO3", "8000");
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
        input.put("histologyIcdO3", "8000");
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
        input.put("histologyIcdO3", "8000");
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
        input.put("histologyIcdO3", "8000");
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
        input.put("histologyIcdO3", "8000");
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
        input.put("histologyIcdO3", "8000");
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
        input.put("histologyIcdO3", "8000");
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
        input.put("histologyIcdO3", "8000");
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
        input.put("histologyIcdO3", "8000");
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
        input.put("histologyIcdO3", "8000");
        input.put("csSiteSpecificFactor25", null);

        Assert.assertFalse(_functions.isCocRequiredPre2010CsCode(input, 1));
        Assert.assertTrue(_functions.isCocRequiredPre2010CsCode(input, 4));
        Assert.assertFalse(_functions.isCocRequiredPre2010CsCode(input, 23));
    }

    @Test
    public void testGetCsObsoleteReason() throws Exception {
        Assert.assertNull(_functions.getCsObsoleteReason(new HashMap<String, String>(), "", ""));

        // testing a single case (Bladder); the goal here is not to test the Obsolete logic, just that the call through is successful
        Map<String, String> input = new HashMap<>();
        input.put("primarySite", "C670");
        input.put("histologyIcdO3", "8000");
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
        input.put("histologyIcdO3", "8000");
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
        input.put("histologyIcdO3", "8750");
        input.put("csSiteSpecificFactor25", "30");
        Assert.assertEquals("Melanoma Lip Lower", _functions.getTnmSchemaName(input));
        input.put("primarySite", "C003");
        Assert.assertEquals("Melanoma Lip Upper", _functions.getTnmSchemaName(input));
        input.put("csSiteSpecificFactor25", null);
        Assert.assertEquals("Melanoma Lip Upper", _functions.getTnmSchemaName(input));
        input.put("histologyIcdO3", "8720");
        Assert.assertEquals("Melanoma Lip Upper", _functions.getTnmSchemaName(input));
        input.put("histologyIcdO3", "8790");
        Assert.assertEquals("Melanoma Lip Upper", _functions.getTnmSchemaName(input));
        input.put("histologyIcdO3", "8719");
        Assert.assertNull(_functions.getTnmSchemaName(input));
        input.put("histologyIcdO3", "8791");
        Assert.assertNull(_functions.getTnmSchemaName(input));
    }

    @Test
    public void testGetTnmSchemaId() {
        Map<String, String> input = new HashMap<>();
        Assert.assertNull(_functions.getTnmSchemaId(null));
        Assert.assertNull(_functions.getTnmSchemaId(input));

        input.put("primarySite", "C004");
        input.put("histologyIcdO3", "8750");
        input.put("csSiteSpecificFactor25", "30");
        Assert.assertEquals("melanoma_lip_lower", _functions.getTnmSchemaId(input));
        input.put("primarySite", "C003");
        Assert.assertEquals("melanoma_lip_upper", _functions.getTnmSchemaId(input));
        input.put("csSiteSpecificFactor25", null);
        Assert.assertEquals("melanoma_lip_upper", _functions.getTnmSchemaId(input));
        input.put("histologyIcdO3", "8720");
        Assert.assertEquals("melanoma_lip_upper", _functions.getTnmSchemaId(input));
        input.put("histologyIcdO3", "8790");
        Assert.assertEquals("melanoma_lip_upper", _functions.getTnmSchemaId(input));
        input.put("histologyIcdO3", "8719");
        Assert.assertNull(_functions.getTnmSchemaId(input));
        input.put("histologyIcdO3", "8791");
        Assert.assertNull(_functions.getTnmSchemaId(input));
        input.put("primarySite", "C481");
        input.put("histologyIcdO3", "8000");
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
        input.put("histologyIcdO3", "8000");
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
        input.put("histologyIcdO3", "8000");
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
        input.put("histologyIcdO3", "8000");
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
        input.put("histologyIcdO3", "8000");
        input.put("csSiteSpecificFactor25", null);

        Assert.assertFalse(_functions.isCocRequiredTnmCode(input, 3));
        Assert.assertTrue(_functions.isCocRequiredTnmCode(input, 2));
        Assert.assertTrue(_functions.isCocRequiredTnmCode(input, 12));
        Assert.assertFalse(_functions.isCocRequiredTnmCode(input, 23));
    }
    
    @Test
    public void testExpandKeys() {
        Assert.assertNotNull(_functions.expandKeys(Collections.singletonMap((Object)"1-9", (Object)"A")));
    }

    @Test
    public void testExpandList() {
        Assert.assertNotNull(_functions.expandList(Collections.singletonList((Object)"1-9")));
    }
}
