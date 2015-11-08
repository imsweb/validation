/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.imsweb.validation.ConstructionException;
import com.imsweb.validation.TestingUtils;
import com.imsweb.validation.ValidatorServices;

/**
 * Created on Mar 1, 2011 by depryf
 * @author depryf
 */
public class EditCodeVisitorSupportTest {

    @Before
    public void setUp() throws Exception {
        TestingUtils.init();
    }

    /**
     * Created on Dec 10, 2009 by depryf
     */
    @Test
    public void testParsing() throws ConstructionException, IOException {
        SortedSet<String> properties = new TreeSet<>();

        // we will use the service to parse (wraps some of the complexity)
        ValidatorServices parser = ValidatorServices.getInstance();

        // special conditions
        parser.parseExpression("id", null, properties, null, null);
        assertProperties(properties);
        parser.parseExpression("id", "", properties, null, null);
        assertProperties(properties);
        parser.parseExpression("id", "    ", properties, null, null);
        assertProperties(properties);

        // regular property references
        parser.parseExpression("id", "return ctc.primarySite != null", properties, null, null);
        assertProperties(properties, "ctc.primarySite");
        properties.clear();
        parser.parseExpression("id", "return patient.person.name != null", properties, null, null);
        assertProperties(properties, "patient.person.name");
        properties.clear();
        parser.parseExpression("id", "return ctc.primarySite.substring(0, 5) != '?'", properties, null, null);
        assertProperties(properties, "ctc.primarySite");
        properties.clear();
        parser.parseExpression("id", "return patient.ctcs.size()> 2", properties, null, null);
        assertProperties(properties);
        properties.clear();
        parser.parseExpression("id", "return ctc.primarySite > 'C123' && ctc.ageAtDiagnosis < 0", properties, null, null);
        assertProperties(properties, "ctc.primarySite", "ctc.ageAtDiagnosis");
        properties.clear();
        parser.parseExpression("id", "return line.primarySite > 'C123' || line.ageAtDx < 0", properties, null, null);
        assertProperties(properties, "line.primarySite", "line.ageAtDx");
        properties.clear();
        parser.parseExpression("id", "return line.primarySite > 'C123' || line.ageAtDx < 0 || Calendar.getInstance().get(Calendar.DAY_OF_MONTH) > 0", properties, null, null);
        assertProperties(properties, "line.primarySite", "line.ageAtDx");
        properties.clear();
        parser.parseExpression("id", "if (record.majorSubtype.shortName == 'hrec_naaccr') return false", properties, null, null);
        assertProperties(properties, "record.majorSubtype.shortName");
        properties.clear();

        // declarations
        parser.parseExpression("id", "def alias = ctc; return alias.primarySite != null", properties, null, null);
        assertProperties(properties, "ctc.primarySite");
        properties.clear();
        parser.parseExpression("id", "def alias = ctc.registryData; if (alias.primarySite != null) return false", properties, null, null);
        assertProperties(properties, "ctc.registryData", "ctc.registryData.primarySite");
        properties.clear();
        parser.parseExpression("id", "def alias = Functions.getCtcFromAllCtcs(ctcs); if (alias.primarySite != null) return false", properties, null, null);
        assertProperties(properties, "ctc.primarySite");
        properties.clear();
        parser.parseExpression("id", "def alias = Functions.getNaaccrLineFromAllLines(lines); if (alias.primarySite != null) return false", properties, null, null);
        assertProperties(properties, "line.primarySite");
        properties.clear();
        parser.parseExpression("id", "def ageDx = Functions.asInt(line.ageAtDx)", properties, null, null);
        assertProperties(properties, "line.ageAtDx");
        properties.clear();
        parser.parseExpression("id", "def primarySiteNum = Functions.asInt(line.primarySite.substring(1))", properties, null, null);
        assertProperties(properties, "line.primarySite");
        properties.clear();
        parser.parseExpression("id", "def line1 = lines.get(0); return line1.primarySite != null", properties, null, null);
        assertProperties(properties, "line.primarySite");
        properties.clear();
        parser.parseExpression("id", "def line1 = lines[0]; return line1.primarySite != null", properties, null, null);
        assertProperties(properties, "line.primarySite");
        properties.clear();
        parser.parseExpression("id", "def ctc1 = patient.ctcs[index]; return ctc1.primarySite != null", properties, null, null);
        assertProperties(properties, "ctc.primarySite");
        properties.clear();

        // method calls
        parser.parseExpression("id", "return Functions.asInt(ctc.primarySite)", properties, null, null);
        assertProperties(properties, "ctc.primarySite");
        properties.clear();
        parser.parseExpression("id", "return Functions.between(ctc.primarySite, 'C123', 'C456')", properties, null, null);
        assertProperties(properties, "ctc.primarySite");
        properties.clear();
        parser.parseExpression("id", "['C123', 'C456'].contains(ctc.primarySite)", properties, null, null);
        assertProperties(properties, "ctc.primarySite");
        properties.clear();
        parser.parseExpression("id", "if (!Functions.isValidDate(ctc.dateOfDiagnosisDd, ctc.dateOfDiagnosisMm, ctc.dateOfDiagnosisYyyy)) { return false }", properties, null, null);
        assertProperties(properties, "ctc.dateOfDiagnosisDd", "ctc.dateOfDiagnosisMm", "ctc.dateOfDiagnosisYyyy");
        properties.clear();
        parser.parseExpression("id", "return Functions.asInt(line.primarySite.substring(1))", properties, null, null);
        assertProperties(properties, "line.primarySite");
        properties.clear();

        // closures
        parser.parseExpression("id", "patient.ctcs.each { if (it.primarySite != null) return false }", properties, null, null);
        assertProperties(properties, "ctc.primarySite");
        properties.clear();
        parser.parseExpression("id", "patient.ctcs.each { ctc -> if (ctc.primarySite != null) return false }", properties, null, null);
        assertProperties(properties, "ctc.primarySite");
        properties.clear();
        parser.parseExpression("id", "patient.ctcs.each { alias -> if (alias.primarySite != null) return false }", properties, null, null);
        assertProperties(properties, "ctc.primarySite");
        properties.clear();
        parser.parseExpression("id", "lines.each { if (it.primarySite != null) return false }", properties, null, null);
        assertProperties(properties, "line.primarySite");
        properties.clear();
        parser.parseExpression("id", "lines.each { line -> if (line.primarySite != null) return false }", properties, null, null);
        assertProperties(properties, "line.primarySite");
        properties.clear();
        parser.parseExpression("id", "lines.each { alias -> if (alias.primarySite != null) return false }", properties, null, null);
        assertProperties(properties, "line.primarySite");
        properties.clear();

        // for loops
        parser.parseExpression("id", "for (ctc in patient.ctcs) {  if (ctc.primarySite != null) return false }", properties, null, null);
        assertProperties(properties, "ctc.primarySite");
        properties.clear();
        parser.parseExpression("id", "for (alias in patient.ctcs) {  if (alias.primarySite != null) return false }", properties, null, null);
        assertProperties(properties, "ctc.primarySite");
        properties.clear();
        parser.parseExpression("id", "for (line in lines) {  if (line.primarySite != null) return false }", properties, null, null);
        assertProperties(properties, "line.primarySite");
        properties.clear();
        parser.parseExpression("id", "for (alias in lines) {  if (alias.primarySite != null) return false }", properties, null, null);
        assertProperties(properties, "line.primarySite");
        properties.clear();
        parser.parseExpression("id", "for (ctc in patient.ctcs) {  if (Functions.between(ctc.primarySite, 'C123', 'C456')) return false }", properties, null, null);
        assertProperties(properties, "ctc.primarySite");
        properties.clear();

        // while loops
        parser.parseExpression("id", "while (var > 0) {  if (ctc.primarySite != null) return false }", properties, null, null);
        assertProperties(properties, "ctc.primarySite");
        properties.clear();
        parser.parseExpression("id", "while (var > 0 && ctc.primarySite != null) { return false }", properties, null, null);
        assertProperties(properties, "ctc.primarySite");
        properties.clear();

        // lookup stuff
        SortedSet<String> lkups = new TreeSet<>();
        parser.parseExpression("id", "for (ctc in patient.ctcs) {  if (Functions.fetchLookup('lkup_facilityDisplayId').contains(ctc.primarySite)) return false }", null, null, lkups);
        assertProperties(lkups, "lkup_facilityDisplayId");
        lkups.clear();

        // context stuff (old notation, no suffix)
        SortedSet<String> context = new TreeSet<>();
        parser.parseExpression("id", "for (ctc in patient.ctcs) {  if (MY_ARRAY.contains(ctc.primarySite)) return false }", null, context, null);
        assertProperties(context, "MY_ARRAY");
        context.clear();
        parser.parseExpression("id", "return registryId == '0000001521' || registryId == '0000001522'", null, context, null);
        assertProperties(context, "registryId");
        context.clear();
        parser.parseExpression("id", "def x = FVC_SET_1.flatten()", null, context, null);
        assertProperties(context, "FVC_SET_1");
        context.clear();
        parser.parseExpression("id", "if (site in FVC_TABLE_2.keySet()) { return false }", null, context, null);
        assertProperties(context, "FVC_TABLE_2", "site");
        context.clear();
        parser.parseExpression("id", "def site = 'C000'; if (site in FVC_TABLE_2.keySet()) { return false }", null, context, null);
        assertProperties(context, "FVC_TABLE_2");
        context.clear();
        parser.parseExpression("id", "return fvc_asInt(ctc.primarySite.substring(1)) < 200", null, context, null);
        assertProperties(context, "fvc_asInt");
        context.clear();
        parser.parseExpression("id", "return FVCM_SOME_VALUES.contains('3')", null, context, null);
        assertProperties(context, "FVCM_SOME_VALUES");
        context.clear();
        parser.parseExpression("id", "val = MY_ARRAY[line.registryId]", null, context, null);
        assertProperties(context, "MY_ARRAY", "val");
        context.clear();
        parser.parseExpression("id", "val = Functions.asInt(MY_ARRAY[line.registryId]) - 120", null, context, null);
        assertProperties(context, "MY_ARRAY", "val");
        context.clear();
        parser.parseExpression("id", "def val = MY_ARRAY[line.registryId]", null, context, null);
        assertProperties(context, "MY_ARRAY");
        context.clear();
        parser.parseExpression("id", "def val = Functions.asInt(MY_ARRAY[line.registryId]) - 120", null, context, null);
        assertProperties(context, "MY_ARRAY");
        context.clear();

        // context stuff (new notation, using the Context. suffix)
        parser.parseExpression("id", "for (ctc in patient.ctcs) {  if (Context.MY_ARRAY.contains(ctc.primarySite)) return false }", null, context, null);
        assertProperties(context, "MY_ARRAY");
        context.clear();
        parser.parseExpression("id", "return Context.registryId == '0000001521' || Context.registryId == '0000001522'", null, context, null);
        assertProperties(context, "registryId");
        context.clear();
        parser.parseExpression("id", "def x = Context.FVC_SET_1.flatten()", null, context, null);
        assertProperties(context, "FVC_SET_1");
        context.clear();
        parser.parseExpression("id", "if (site in Context.FVC_TABLE_2.keySet()) { return false }", null, context, null);
        assertProperties(context, "FVC_TABLE_2", "site");
        context.clear();
        parser.parseExpression("id", "def site = 'C000'; if (site in Context.FVC_TABLE_2.keySet()) { return false }", null, context, null);
        assertProperties(context, "FVC_TABLE_2");
        context.clear();
        parser.parseExpression("id", "return Context.fvc_asInt(ctc.primarySite.substring(1)) < 200", null, context, null);
        assertProperties(context, "fvc_asInt");
        context.clear();
        parser.parseExpression("id", "return Context.FVCM_SOME_VALUES.contains('3')", null, context, null);
        assertProperties(context, "FVCM_SOME_VALUES");
        context.clear();
        parser.parseExpression("id", "val = Context.MY_ARRAY[line.registryId]", null, context, null);
        assertProperties(context, "MY_ARRAY", "val");
        context.clear();
        parser.parseExpression("id", "val = Functions.asInt(Context.MY_ARRAY[line.registryId]) - 120", null, context, null);
        assertProperties(context, "MY_ARRAY", "val");
        context.clear();
        parser.parseExpression("id", "def val = Context.MY_ARRAY[line.registryId]", null, context, null);
        assertProperties(context, "MY_ARRAY");
        context.clear();
        parser.parseExpression("id", "def val = Functions.asInt(Context.MY_ARRAY[line.registryId]) - 120", null, context, null);
        assertProperties(context, "MY_ARRAY");
        context.clear();
        parser.parseExpression("id", "def array = Context.MY_ARRAY; return true", null, context, null);
        assertProperties(context, "MY_ARRAY");
        context.clear();

        // and finally run the old parsing test, the more tests the better
        SortedSet<String> expected = new TreeSet<>();
        expected.add("ctc.seerSummaryStage2000");
        expected.add("ctc.primarySite");
        expected.add("ctc.histologyICDO3");
        expected.add("ctc.typeOfReportingSource");
        expected.add("facilityAdmission.registryData");
        expected.add("facilityAdmission.registryData.prop2");
        expected.add("facilityAdmission.prop1");
        expected.add("ctc.histology");
        expected.add("ctc.dateOfDiagnosisYyyy");
        expected.add("course.sequenceNumber");
        expected.add("course.calculationMethod");
        SortedSet<String> expectedLkup = new TreeSet<>();
        expectedLkup.add("lkup_1");
        expectedLkup.add("lkup_2");
        SortedSet<String> rawProperties = new TreeSet<>();
        SortedSet<String> lookups = new TreeSet<>();
        String exp = getContent(Thread.currentThread().getContextClassLoader().getResource("property-parsing-test.txt"));
        ValidatorServices.getInstance().parseExpression("id", exp, rawProperties, null, lookups);
        assertProperties(rawProperties, expected.toArray(new String[expected.size()]));
        assertProperties(lookups, expectedLkup.toArray(new String[expectedLkup.size()]));

        // another full test
        expected.clear();
        expected.add("line.dateOfLastContactYear");
        expected.add("line.recordType");
        expected.add("line.censusTract708090");
        expected.add("line.censusTract2000");
        rawProperties.clear();
        exp = getContent(Thread.currentThread().getContextClassLoader().getResource("property-parsing-seer-test.txt"));
        ValidatorServices.getInstance().parseExpression("id", exp, rawProperties, null, null);
        Assert.assertEquals(expected, rawProperties);

        // another full test
        expected.clear();
        expected.add("line.behaviorIcdO3");
        expected.add("line.histologyIcdO3");
        expected.add("line.laterality");
        expected.add("line.overRideSiteLatSeqNo");
        expected.add("line.primarySite");
        rawProperties.clear();
        context.clear();
        exp = getContent(Thread.currentThread().getContextClassLoader().getResource("property-parsing-single-test.txt"));
        ValidatorServices.getInstance().parseExpression("id", exp, rawProperties, null, null);
        Assert.assertEquals(expected, rawProperties);

        // another full test
        expected.clear();
        expected.add("untrimmedline.tnmClinStageGroup");
        expected.add("untrimmedline.tnmPathStageGroup");
        expected.add("untrimmedline.dateOfDiagnosis");
        expected.add("untrimmedline.behaviorIcdO3");
        expected.add("untrimmedline.histologyIcdO3");
        expected.add("untrimmedline.tnmEditionNumber");
        expected.add("untrimmedline.ageAtDx");
        expected.add("untrimmedline.overRideSiteTnmStgGrp");
        expected.add("untrimmedline.primarySite");
        rawProperties.clear();
        context.clear();
        exp = getContent(Thread.currentThread().getContextClassLoader().getResource("property-parsing-function-test.txt"));
        ValidatorServices.getInstance().parseExpression("id", exp, rawProperties, null, null);
        Assert.assertEquals(expected, rawProperties);

        // another test based on a bug
        expected.clear();
        expected.add("line.dateOfDiagnosisYear");
        expected.add("line.histologyIcdO3");
        expected.add("line.behaviorIcdO3");
        rawProperties.clear();
        exp = getContent(Thread.currentThread().getContextClassLoader().getResource("property-parsing-another-test.txt"));
        ValidatorServices.getInstance().parseExpression("id", exp, rawProperties, null, null);
        Assert.assertEquals(expected, rawProperties);
        rawProperties.clear();
        exp = getContent(Thread.currentThread().getContextClassLoader().getResource("property-parsing-another-test2.txt"));
        ValidatorServices.getInstance().parseExpression("id", exp, rawProperties, null, null);
        Assert.assertEquals(expected, rawProperties);
    }

    /**
     * Created on Mar 1, 2010 by depryf
     */
    @Test
    @SuppressWarnings("ConstantConditions")
    public void testProperUseOfDefKeyword() throws ConstructionException {
        boolean exception = false;

        // we will use the service to parse (wraps some of the complexity)
        ValidatorServices parser = ValidatorServices.getInstance();

        // all variable are defined with the 'def' keyword -> no exception
        parser.parseExpression("id", "def var1 = null; def var2 = 0; if (var1 == null) {def var5 = var2; var5++}; for (i in 1..5) {def var3 = var2; var3++}; def var4 = var2; return var1;", null,
                null, null, true);

        // var1 missing def
        try {
            parser.parseExpression("id", "var1 = null; def var2 = 0; if (var1 == null) {def var5 = var2; var5++}; for (i in 1..5) {def var3 = var2; var3++}; def var4 = var2; return var1;", null,
                    null, null, true);
        }
        catch (Exception e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("was expecting an exception, didn't happened!");

        // var2 missing def
        try {
            parser.parseExpression("id", "def var1 = null; var2 = 0; if (var1 == null) {def var5 = var2; var5++}; for (i in 1..5) {def var3 = var2; var3++}; def var4 = var2; return var1;", null,
                    null, null, true);
        }
        catch (Exception e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("was expecting an exception, didn't happened!");

        // var5 missing def
        try {
            parser.parseExpression("id", "def var1 = null; def var2 = 0; if (var1 == null) {var5 = var2; var5++}; for (i in 1..5) {def var3 = var2; var3++}; def var4 = var2; return var1;", null,
                    null, null, true);
        }
        catch (Exception e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("was expecting an exception, didn't happened!");

        // var3 missing def
        try {
            parser.parseExpression("id", "def var1 = null; def var2 = 0; if (var1 == null) {def var5 = var2; var5++}; for (i in 1..5) {var3 = var2; var3++}; def var4 = var2; return var1;", null,
                    null, null, true);
        }
        catch (Exception e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("was expecting an exception, didn't happened!");

        // var4 missing def
        try {
            parser.parseExpression("id", "def var1 = null; def var2 = 0; if (var1 == null) {def var5 = var2; var5++}; for (i in 1..5) {def var3 = var2; var3++}; def var4 = var2; return var1;", null,
                    null, null, true);
        }
        catch (Exception e) {
            exception = true;
        }
        if (!exception)
            Assert.fail("was expecting an exception, didn't happened!");
    }

    // helper
    private void assertProperties(SortedSet<String> properties, String... expected) {
        SortedSet<String> exp = new TreeSet<>();
        Collections.addAll(exp, expected);
        if (!properties.equals(exp)) {
            StringBuilder buf = new StringBuilder("\nWas expecting\n");
            for (String s : exp)
                buf.append("      ").append(s).append("\n");
            buf.append("but got\n");
            for (String s : properties)
                buf.append("      ").append(s).append("\n");
            Assert.fail(buf.toString());
        }
    }

    // helper
    private String getContent(URL url) {
        StringBuilder buffer = new StringBuilder();
        BufferedReader br = null;

        try {
            br = new BufferedReader(new InputStreamReader(url.openStream()));

            String line = br.readLine();
            while (line != null) {
                buffer.append(line);
                buffer.append("\n");

                line = br.readLine();
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            try {
                if (br != null)
                    br.close();
            }
            catch (IOException e) {
                // ignored
            }
        }

        return buffer.toString();
    }
}
