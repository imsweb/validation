/*
 * Copyright (C) 2021 Information Management Services, Inc.
 */
package demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.imsweb.layout.LayoutFactory;
import com.imsweb.layout.record.fixed.naaccr.NaaccrLayout;
import com.imsweb.validation.InitializationStats;
import com.imsweb.validation.TestingUtils;
import com.imsweb.validation.ValidationContextFunctions;
import com.imsweb.validation.ValidationEngine;
import com.imsweb.validation.edits.seer.SeerRuntimeEdits;
import com.imsweb.validation.entities.RuleFailure;
import com.imsweb.validation.entities.SimpleNaaccrLinesValidatable;
import com.imsweb.validation.entities.Validatable;
import com.imsweb.validation.functions.StagingContextFunctions;

/**
 * This example demonstrates running the pre-compiled SEER edits on a NAACCR fixed-columns (flat) file.
 * <br/><br/>
 * It requires two dependencies available on Maven Central (see the build file from the project):
 *   1. The "layout" framework to read the data file: com.imsweb:layout:X.X'
 *   2. The pre-compiled SEER edits: com.imsweb:validation-edits-seer:XXX-XX
 * <br/><br/>
 * The example uses a fake NAACCR 18 flat files that is also contained in this project.
 */
public class DemoSeerEditsWithNaaccrFlat {

    public static void main(String[] args) throws Exception {

        // it would be simpler to get the file on the classpath, but using an actual File object will allow people to easily point to their own file...
        File dataFile = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/data/synthetic-data_naaccr-18-incidence_5-records.txt");

        // we have to initialize the staging framework since the SEER edits use it...
        ValidationContextFunctions.initialize(new StagingContextFunctions(TestingUtils.getCsStaging(), TestingUtils.getTnmStaging(), TestingUtils.getEodStaging()));

        // load the SEER edits and initialize the validation engine
        InitializationStats stats = ValidationEngine.getInstance().initialize(SeerRuntimeEdits.loadValidator());
        System.out.println("Initialized " + stats.getNumEditsLoaded() + " SEER edits...");

        // we will use this layout object to read the data file
        NaaccrLayout layout = LayoutFactory.getNaaccrFixedColumnsLayout(LayoutFactory.LAYOUT_ID_NAACCR_18_INCIDENCE);

        // and finally, run the edits and display some counts
        long start = System.currentTimeMillis();
        System.out.println("Running edits...");
        AtomicInteger recCount = new AtomicInteger(), failuresCount = new AtomicInteger();
        try (LineNumberReader reader = new LineNumberReader(new InputStreamReader(new FileInputStream(dataFile), StandardCharsets.UTF_8))) {
            Map<String, String> rec = layout.readNextRecord(reader);
            while (rec != null) {
                recCount.getAndIncrement();

                Validatable validatable = new SimpleNaaccrLinesValidatable(Collections.singletonList(rec));
                Collection<RuleFailure> failures = ValidationEngine.getInstance().validate(validatable);
                failuresCount.addAndGet(failures.size());

                // The SEER edits contain inter-record edits but for those to work, the list of all lines for a given patient needs to be
                // provided to the validatable; this simple example reads each line of the data file individually and doesn't attempt to group
                // them by Patient ID Number, and so the SEER inter-record edits won't work. See the NAACCR XML demo for an example that
                // properly runs those inter-record edits...
                System.out.println("Validated Patient " + rec.get("patientIdNumber") + " (line #" + reader.getLineNumber() + "):");
                for (RuleFailure failure : failures)
                    System.out.println("  > " + failure.getRule().getId() + ": " + failure.getMessage());

                rec = layout.readNextRecord(reader);
            }
        }
        System.out.println("Done running edits in " + (System.currentTimeMillis() - start) + "ms");
        System.out.println("  > num records: " + recCount.get());
        System.out.println("  > num failures: " + failuresCount.get());
    }
}
