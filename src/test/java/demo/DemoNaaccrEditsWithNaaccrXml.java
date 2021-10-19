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
import com.imsweb.validation.InitializationStats;
import com.imsweb.validation.TestingUtils;
import com.imsweb.validation.ValidationContextFunctions;
import com.imsweb.validation.ValidationEngine;
import com.imsweb.validation.edits.translated.naaccr.NaaccrTranslatedRuntimeEdits;
import com.imsweb.validation.entities.EmbeddedSet;
import com.imsweb.validation.entities.RuleFailure;
import com.imsweb.validation.entities.SimpleNaaccrLinesValidatable;
import com.imsweb.validation.entities.Validator;
import com.imsweb.validation.functions.MetafileContextFunctions;

public class DemoNaaccrEditsWithNaaccrXml {

    public static void main(String[] args) throws Exception {

        // it would be simpler to get the file on the classpath, but using an actual File object will allow people to easily point to their own file...
        File dataFile = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/data/synthetic-data_naaccr-xml-21-abstract_10-tumors.xml");

        // we have to initialize the CS staging framework since the NAACCR edits use it...
        Staging csStaging = Staging.getInstance(CsDataProvider.getInstance(CsVersion.LATEST));
        ValidationContextFunctions.initialize(new MetafileContextFunctions(csStaging, null, null));

        // load the NAACCR edits and initialize the validation engine
        Validator naaccrValidator = NaaccrTranslatedRuntimeEdits.loadValidator();
        InitializationStats stats = ValidationEngine.getInstance().initialize(naaccrValidator);
        System.out.println("Initialized " + stats.getNumEditsLoaded() + " NAACCR edits...");
        for (EmbeddedSet set : naaccrValidator.getSets())
            System.out.println("  > loaded \"" + set.getName() + "\" with " + set.getInclusions().size() + " edits...");

        // Note that it almost never makes sense to run all NAACCR edits; there are multiple ways to only run a subset:
        //  1. After loading the Validator (but before initializing the engine), go through its rules and remove the ones you don't want.
        //  2. After initializing the engine, you can use the "massUpdateIgnoreFlags" and pass a collection of edits to disable (by default they are all enabled);
        //     and so to run only a specific set of edits, gather the edit IDs that are not in the set, and provide them as the first parameter to that method.
        //  3. The validate method allows an optional parameter for rule IDs to include or exclude during the validation. This mechanism is more dynamic in that it
        //     allows to dynamically change the list of edits that need to be executed from one record to another

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

                // in NAACCR flat world, the edits were written in terms of a "line" representing a single tumor; in NAACCR XML word, this should really become
                // "tumor" instead of "line" and the validatable should work natively on tumors, but for now the edits still expect a "line" which is just a simple map...
                List<Map<String, String>> tumorList = new ArrayList<>();
                for (Tumor tumor : patient.getTumors()) {
                    Map<String, String> tumorMap = new HashMap<>();
                    for (Item item : patient.getItems())
                        tumorMap.put(item.getNaaccrId(), item.getValue());
                    for (Item item : tumor.getItems())
                        tumorMap.put(item.getNaaccrId(), item.getValue());
                    tumorList.add(tumorMap);
                }

                // note that unlike the SEER edits, NAACCR edits don't have "inter-tumor" edits, and so we could just validate each tumor independently...
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
