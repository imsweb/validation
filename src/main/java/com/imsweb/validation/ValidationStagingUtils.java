/*
 * Copyright (C) 2022 Information Management Services, Inc.
 */
package com.imsweb.validation;

import java.util.List;
import java.util.Map;

import com.imsweb.staging.Staging;
import com.imsweb.staging.entities.Schema;
import com.imsweb.staging.entities.SchemaLookup;

public class ValidationStagingUtils {

    public static final String SCHEMA_ID_TNM_PERITONEUM = "peritoneum";
    public static final String SCHEMA_ID_TNM_PERITONEUM_FEMALE_GEN = "peritoneum_female_gen";

    // the standard NAACCR properties used when getting a schema
    public static final String INPUT_PROP_SITE = "primarySite";
    public static final String INPUT_PROP_HIST = "histologicTypeIcdO3";
    public static final String INPUT_PROP_SSF25 = "csSiteSpecificFactor25";
    public static final String INPUT_PROP_SEX = "sex";
    public static final String INPUT_PROP_DISC_1 = "schemaDiscriminator1";
    public static final String INPUT_PROP_DISC_2 = "schemaDiscriminator2";
    public static final String INPUT_PROP_BEHAV = "behaviorCodeIcdO3";
    public static final String INPUT_PROP_DX_YEAR = "dateOfDiagnosisYear";
    
    /**
     * Private constructor, no instantiation.
     */
    private ValidationStagingUtils() {
    }

    public static String computeCsSchemaId(Staging stagingInstance, Map<String, String> input) {
        if (stagingInstance == null || input == null)
            return null;
        
        String site = input.get(INPUT_PROP_SITE);
        String hist = input.get(INPUT_PROP_HIST);
        String ssf25 = input.get(INPUT_PROP_SSF25);

        SchemaLookup lkup = new SchemaLookup(site, hist);
        lkup.setInput("ssf25", ssf25);
        List<Schema> info = stagingInstance.lookupSchema(lkup);
        if (info.size() == 1) {
            Schema schema = stagingInstance.getSchema(info.get(0).getId());
            return schema.getId();
        }
        return null;
    }
    
    public static String computeTnmSchemaId(Staging stagingInstance, Map<String, String> input) {
        if (stagingInstance == null || input == null)
            return null;
        
        String site = input.get(INPUT_PROP_SITE);
        String hist = input.get(INPUT_PROP_HIST);
        String ssf25 = input.get(INPUT_PROP_SSF25);
        String sex = input.get(INPUT_PROP_SEX);
        
        // get the TNM schema ID
        SchemaLookup lkup = new SchemaLookup(site, hist);
        lkup.setInput("ssf25", ssf25);
        lkup.setInput("sex", sex);
        List<Schema> info = stagingInstance.lookupSchema(lkup);
        if (info.size() == 1) {
            Schema schema = stagingInstance.getSchema(info.get(0).getId());
            return schema.getId();
        }
        return null;
    }

    public static String computeEodSchemaId(Staging stagingInstance, Map<String, String> input) {
        if (stagingInstance == null || input == null)
            return null;
            
        String site = input.get(INPUT_PROP_SITE);
        String hist = input.get(INPUT_PROP_HIST);
        String disc1 = input.get(INPUT_PROP_DISC_1);
        String disc2 = input.get(INPUT_PROP_DISC_2);
        String sex = input.get(INPUT_PROP_SEX);
        String behav = input.get(INPUT_PROP_BEHAV);
        String dxYear = input.get(INPUT_PROP_DX_YEAR);

        SchemaLookup lkup = new SchemaLookup(site, hist);
        lkup.setInput("discriminator_1", disc1);
        lkup.setInput("discriminator_2", disc2);
        lkup.setInput("sex", sex);
        lkup.setInput("behavior", behav);
        lkup.setInput("year_dx", dxYear);
        List<Schema> info = stagingInstance.lookupSchema(lkup);
        if (info.size() == 1) {
            Schema schema = stagingInstance.getSchema(info.get(0).getId());
            return schema.getId();
        }
        return null;
    }
    
    /**
     * Returns the SSF25 value based on the sex value and some conditions
     */
    public static String getSsf25FromSex(String ssf25, String sex, String hist, String dxYear, String schemaId) {
        boolean isPeritoneum = SCHEMA_ID_TNM_PERITONEUM.equals(schemaId) || SCHEMA_ID_TNM_PERITONEUM_FEMALE_GEN.equals(schemaId);
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