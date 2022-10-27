/*
 * Copyright (C) 2022 Information Management Services, Inc.
 */
package com.imsweb.validation;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.imsweb.staging.Staging;
import com.imsweb.staging.cs.CsDataProvider;
import com.imsweb.staging.eod.EodDataProvider;
import com.imsweb.staging.tnm.TnmDataProvider;

import static com.imsweb.validation.ValidationStagingUtils.SCHEMA_ID_TNM_PERITONEUM;
import static com.imsweb.validation.ValidationStagingUtils.getSsf25FromSex;

public class ValidationStagingUtilsTest {

    private Staging _csStaging;
    private Staging _tnmStaging;
    private Staging _eodStaging;

    @Before
    public void setUp() {
        TestingUtils.init();

        _csStaging = Staging.getInstance(CsDataProvider.getInstance(CsDataProvider.CsVersion.LATEST));
        _tnmStaging = Staging.getInstance(TnmDataProvider.getInstance(TnmDataProvider.TnmVersion.LATEST));
        _eodStaging = Staging.getInstance(EodDataProvider.getInstance(EodDataProvider.EodVersion.LATEST));
    }

    @Test
    public void testComputeCsShemaId() {
        Assert.assertNull(ValidationStagingUtils.computeCsSchemaId(_csStaging, null));

        Map<String, String> input = new HashMap<>();
        Assert.assertNull(ValidationStagingUtils.computeCsSchemaId(_csStaging, input));

        input.put("primarySite", "C481");
        input.put("histologicTypeIcdO3", "8000");
        Assert.assertNull(ValidationStagingUtils.computeCsSchemaId(_csStaging, input));

        input.put("sex", "1");
        Assert.assertNull(ValidationStagingUtils.computeCsSchemaId(_csStaging, input)); // peritoneum

        input.put("dateOfDiagnosisYear", "2016");
        input.put("csSiteSpecificFactor25", getSsf25FromSex(null, "1", "8000", "2016", SCHEMA_ID_TNM_PERITONEUM));
        Assert.assertNotNull(ValidationStagingUtils.computeCsSchemaId(_csStaging, input)); // peritoneum

        input.put("csSiteSpecificFactor25", "010");
        Assert.assertNull(ValidationStagingUtils.computeCsSchemaId(_csStaging, input));

        input.put("primarySite", "C111");
        Assert.assertNotNull(ValidationStagingUtils.computeCsSchemaId(_csStaging, input)); // nasopharynx

        input.put("csSiteSpecificFactor25", "000");
        Assert.assertNull(ValidationStagingUtils.computeCsSchemaId(_csStaging, input));

        input.put("csSiteSpecificFactor25", "020");
        Assert.assertNotNull(ValidationStagingUtils.computeCsSchemaId(_csStaging, input)); // pharyngeal_tonsil
    }

    @Test
    public void testComputeTnmShemaId() {
        Assert.assertNull(ValidationStagingUtils.computeTnmSchemaId(_tnmStaging, null));

        Map<String, String> input = new HashMap<>();
        Assert.assertNull(ValidationStagingUtils.computeTnmSchemaId(_tnmStaging, input));

        input.put("primarySite", "C481");
        input.put("histologicTypeIcdO3", "8000");
        Assert.assertNull(ValidationStagingUtils.computeTnmSchemaId(_tnmStaging, input));

        input.put("sex", "1");
        Assert.assertNotNull(ValidationStagingUtils.computeTnmSchemaId(_tnmStaging, input)); // peritoneum

        input.put("csSiteSpecificFactor25", "010");
        Assert.assertNotNull(ValidationStagingUtils.computeTnmSchemaId(_tnmStaging, input));

        input.put("primarySite", "C111");
        Assert.assertNotNull(ValidationStagingUtils.computeTnmSchemaId(_tnmStaging, input)); // nasopharynx

        input.put("csSiteSpecificFactor25", "000");
        Assert.assertNull(ValidationStagingUtils.computeTnmSchemaId(_tnmStaging, input));

        input.put("csSiteSpecificFactor25", "020");
        Assert.assertNotNull(ValidationStagingUtils.computeTnmSchemaId(_tnmStaging, input)); // pharyngeal_tonsil
    }
    
    @Test
    public void testComputeEodShemaId() {
        Assert.assertNull(ValidationStagingUtils.computeEodSchemaId(_eodStaging, null));

        Map<String, String> input = new HashMap<>();
        Assert.assertNull(ValidationStagingUtils.computeEodSchemaId(_eodStaging, input));

        input.put("primarySite", "C481");
        input.put("histologicTypeIcdO3", "8000");
        Assert.assertNull(ValidationStagingUtils.computeEodSchemaId(_eodStaging, input));

        input.put("sex", "1");
        Assert.assertNotNull(ValidationStagingUtils.computeEodSchemaId(_eodStaging, input)); // retroperitoneum

        input.put("schemaDiscriminator1", "1");
        Assert.assertNotNull(ValidationStagingUtils.computeEodSchemaId(_eodStaging, input));

        input.put("primarySite", "C111");
        Assert.assertNotNull(ValidationStagingUtils.computeEodSchemaId(_eodStaging, input)); // nasopharynx

        input.put("schemaDiscriminator1", "2");
        Assert.assertNull(ValidationStagingUtils.computeEodSchemaId(_eodStaging, input));

        input.put("schemaDiscriminator2", "2");
        Assert.assertNotNull(ValidationStagingUtils.computeEodSchemaId(_eodStaging, input)); // oropharynx_hpv_mediated_p16_pos

        input.put("primarySite", "C700");
        input.put("histologicTypeIcdO3", "8710");
        input.put("behaviorCodeIcdO3", "0");
        input.put("schemaDiscriminator1", null);
        input.put("schemaDiscriminator2", null);
        Assert.assertNotNull(ValidationStagingUtils.computeEodSchemaId(_eodStaging, input)); // brain
    }

    @Test
    public void testGetSsf25FromSex() {
        // test sex conditions
        Assert.assertEquals("001", ValidationStagingUtils.getSsf25FromSex(null, "1", "8000", "2016", "peritoneum"));
        Assert.assertEquals("002", ValidationStagingUtils.getSsf25FromSex(null, "2", "8000", "2016", "peritoneum"));
        Assert.assertEquals("003", ValidationStagingUtils.getSsf25FromSex(null, "3", "8000", "2016", "peritoneum"));
        Assert.assertEquals("004", ValidationStagingUtils.getSsf25FromSex(null, "4", "8000", "2016", "peritoneum"));
        Assert.assertEquals("001", ValidationStagingUtils.getSsf25FromSex(null, "5", "8000", "2016", "peritoneum"));
        Assert.assertEquals("002", ValidationStagingUtils.getSsf25FromSex(null, "6", "8000", "2016", "peritoneum"));
        Assert.assertEquals("009", ValidationStagingUtils.getSsf25FromSex(null, "9", "8000", "2016", "peritoneum"));
        Assert.assertEquals("009", ValidationStagingUtils.getSsf25FromSex(null, null, "8000", "2016", "peritoneum"));
        Assert.assertEquals("009", ValidationStagingUtils.getSsf25FromSex(null, "", "8000", "2016", "peritoneum"));
        Assert.assertEquals("009", ValidationStagingUtils.getSsf25FromSex(null, "7", "8000", "2016", "peritoneum"));

        // test hist, schema, and dx year conditions
        Assert.assertEquals("001", ValidationStagingUtils.getSsf25FromSex(null, "1", "8000", "2016", "peritoneum_female_gen"));
        Assert.assertEquals("001", ValidationStagingUtils.getSsf25FromSex(null, "1", "8000", "2017", "peritoneum"));
        Assert.assertNull(ValidationStagingUtils.getSsf25FromSex(null, "1", "8000", "2016", "larynx_other"));
        Assert.assertNull(ValidationStagingUtils.getSsf25FromSex(null, "1", "8000", "2015", "peritoneum"));
        Assert.assertEquals("981", ValidationStagingUtils.getSsf25FromSex(null, "1", "8577", "2016", "peritoneum"));

        // test ssf25 condition
        Assert.assertEquals("001", ValidationStagingUtils.getSsf25FromSex("001", null, "8000", "2016", "peritoneum"));
        Assert.assertEquals("001", ValidationStagingUtils.getSsf25FromSex("001", "2", "8000", "2016", "peritoneum"));
        Assert.assertEquals("002", ValidationStagingUtils.getSsf25FromSex("988", "2", "8000", "2016", "peritoneum"));
        Assert.assertEquals("002", ValidationStagingUtils.getSsf25FromSex("   ", "2", "8000", "2016", "peritoneum"));
        Assert.assertEquals("002", ValidationStagingUtils.getSsf25FromSex("   ", "2", "8000", "2016", "peritoneum"));

        Assert.assertNull(ValidationStagingUtils.getSsf25FromSex(null, null, null, null, null));
        Assert.assertEquals("981", ValidationStagingUtils.getSsf25FromSex(null, null, null, "2016", "peritoneum"));
    }
}
