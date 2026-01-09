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

/**
 * This example demonstrates running the pre-compiled NAACCR edits on a NAACCR XML file.
 * <br/><br/>
 * It requires two dependencies available on Maven Central (see the build file from the project):
 *   1. The "layout" framework to read the data file: com.imsweb:layout:X.X'
 *   2. The pre-compiled SEER edits: com.imsweb:validation-edits-naaccr-translated:XXX-XX
 * <br/><br/>
 * The example uses a fake NAACCR 22 XML files that is also contained in this project.
 */
public class DemoNaaccrEditsWithNaaccrXml {

    public static void main(String[] args) throws Exception {

        // it would be simpler to get the file on the classpath, but using an actual File object will allow people to easily point to their own file...
        File dataFile = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/data/synthetic-data_naaccr-xml-22-abstract_5-tumors.xml");

        // we have to initialize the CS staging framework since the NAACCR edits use it...
        ValidationContextFunctions.initialize(new MetafileContextFunctions(TestingUtils.getCsStaging(), null, null));

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

                // In NAACCR flat world, the edits were written in terms of a "untrimmedline" representing a single tumor; in NAACCR XML word, this should really become
                // "tumor" instead of "untrimmedline" and the validatable should work natively on tumors, but for now the edits still expect an "untrimmedline"
                // which is just a simple map.
                List<Map<String, String>> tumorList = new ArrayList<>();
                for (Tumor tumor : patient.getTumors()) {
                    Map<String, String> tumorMap = new HashMap<>();
                    // add the NaaccrData items (they appear once per file)
                    for (Item item : reader.getRootData().getItems())
                        tumorMap.put(item.getNaaccrId(), item.getValue());
                    // add the patient items (they appear once per Patient)
                    for (Item item : patient.getItems())
                        tumorMap.put(item.getNaaccrId(), item.getValue());
                    // add the tumor items (they appear once per Tumor)
                    for (Item item : tumor.getItems())
                        tumorMap.put(item.getNaaccrId(), item.getValue());
                    tumorList.add(tumorMap);

                    // Unlike the SEER edits, NAACCR edits don't have "inter-tumor" edits, and so we can just validate each tumor independently...
                    // Also, the translated edits require leading/trailing spaces to be provided with the values (in other words, the values are always
                    // exactly their expected length); that means the validatable has to behave differently and use a "untrimmedline" notation.
                    // The context param can be used to provide meta-data information to the edits (like the current version of the software running them);
                    // that feature is not used for SEER or NAACCR edits.
                    Collection<RuleFailure> failures = ValidationEngine.getInstance().validate(new SimpleNaaccrLinesValidatable(tumorList, null, true));
                    System.out.println("Validated Tumor " + tumor.getItemValue("tumorRecordNumber") + " on Patient " + patient.getItemValue("patientIdNumber") + ":");
                    for (RuleFailure failure : failures)
                        System.out.println("  > " + failure.getRule().getId() + ": " + failure.getMessage());
                    failuresCount.addAndGet(failures.size());
                }
                patient = layout.readNextPatient(reader);
            }
        }
        System.out.println("Done running edits in " + (System.currentTimeMillis() - start) + "ms");
        System.out.println("  > num tumors: " + tumorCount.get());
        System.out.println("  > num failures: " + failuresCount.get());
    }
}
