/*
 * Copyright (C) 2021 Information Management Services, Inc.
 */
package demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.imsweb.layout.LayoutFactory;
import com.imsweb.layout.naaccrxml.NaaccrXmlLayout;
import com.imsweb.naaccrxml.PatientXmlReader;
import com.imsweb.naaccrxml.entity.Item;
import com.imsweb.naaccrxml.entity.Patient;
import com.imsweb.naaccrxml.entity.Tumor;
import com.imsweb.staging.Staging;
import com.imsweb.staging.cs.CsDataProvider;
import com.imsweb.staging.cs.CsDataProvider.CsVersion;
import com.imsweb.staging.eod.EodDataProvider;
import com.imsweb.staging.eod.EodDataProvider.EodVersion;
import com.imsweb.staging.tnm.TnmDataProvider;
import com.imsweb.staging.tnm.TnmDataProvider.TnmVersion;
import com.imsweb.validation.InitializationStats;
import com.imsweb.validation.TestingUtils;
import com.imsweb.validation.ValidationContextFunctions;
import com.imsweb.validation.ValidationEngine;
import com.imsweb.validation.edits.seer.SeerRuntimeEdits;
import com.imsweb.validation.entities.RuleFailure;
import com.imsweb.validation.entities.SimpleNaaccrLinesValidatable;
import com.imsweb.validation.functions.StagingContextFunctions;

public class DemoSeerEditsWithNaaccrXml {

    public static void main(String[] args) throws Exception {

        // it would be simpler to get the file on the classpath, but using an actual File object will allow people to easily point to their own file...
        File dataFile = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/data/synthetic-data_naaccr-xml-21-abstract_10-tumors.xml");

        // we have to initialize the staging framework since the SEER edits use it...
        Staging csStaging = Staging.getInstance(CsDataProvider.getInstance(CsVersion.LATEST));
        Staging tnmStaging = Staging.getInstance(TnmDataProvider.getInstance(TnmVersion.LATEST));
        Staging eodStaging = Staging.getInstance(EodDataProvider.getInstance(EodVersion.LATEST));
        ValidationContextFunctions.initialize(new StagingContextFunctions(csStaging, tnmStaging, eodStaging));

        // load the SEER edits and initialize the validation engine
        InitializationStats stats = ValidationEngine.getInstance().initialize(SeerRuntimeEdits.loadValidator());
        System.out.println("Initialized " + stats.getNumEditsLoaded() + " SEER edits...");

        // we will use this layout object to read the data file
        NaaccrXmlLayout layout = LayoutFactory.getNaaccrXmlLayout(LayoutFactory.LAYOUT_ID_NAACCR_XML_21_ABSTRACT);

        // and finally, run the edits and display some counts
        long start = System.currentTimeMillis();
        System.out.println("Running edits...");
        AtomicInteger tumorCount = new AtomicInteger(), failuresCount = new AtomicInteger();
        try (PatientXmlReader reader = new PatientXmlReader(new LineNumberReader(new InputStreamReader(new FileInputStream(dataFile), StandardCharsets.UTF_8)))) {
            Patient patient = layout.readNextPatient(reader);
            while (patient != null) {
                tumorCount.addAndGet(patient.getTumors().size());

                // even in NAACCR flat world, the SEER edits already supported the concept of a "patient" which was a grouping of lines with the same patient ID number;
                // the syntax of the SEER edits still references the "lines" and "line" context variables and expect those to be simple maps of string. It's possible one
                // day the SEER edits will be re-written to natively run on NAACCR XML Patients and Tumors, but for now those need to be translated to maps...
                List<Map<String, String>> tumorList = new ArrayList<>();
                for (Tumor tumor : patient.getTumors()) {
                    Map<String, String> tumorMap = new HashMap<>();
                    for (Item item : patient.getItems())
                        tumorMap.put(item.getNaaccrId(), item.getValue());
                    for (Item item : tumor.getItems())
                        tumorMap.put(item.getNaaccrId(), item.getValue());
                    tumorList.add(tumorMap);
                }

                Collection<RuleFailure> failures = ValidationEngine.getInstance().validate(new SimpleNaaccrLinesValidatable(tumorList));
                failuresCount.addAndGet(failures.size());
                patient = layout.readNextPatient(reader);
            }
        }
        System.out.println("  > done in " + (System.currentTimeMillis() - start) + "ms");
        System.out.println("  > num tumors: " + tumorCount.get());
        System.out.println("  > num failures: " + failuresCount.get());
    }
}
