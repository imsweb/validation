/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package lab;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

import com.imsweb.datagenerator.naaccr.NaaccrDataGenerator;
import com.imsweb.layout.LayoutFactory;
import com.imsweb.layout.record.fixed.naaccr.NaaccrLayout;
import com.imsweb.validation.ValidationEngine;
import com.imsweb.validation.ValidationEngineInitializationStats;
import com.imsweb.validation.ValidatorContextFunctions;
import com.imsweb.validation.ValidatorServices;
import com.imsweb.validation.XmlValidatorFactory;
import com.imsweb.validation.entities.Rule;
import com.imsweb.validation.entities.RuleFailure;
import com.imsweb.validation.entities.SimpleNaaccrLinesValidatable;
import com.imsweb.validation.entities.Validator;
import com.imsweb.validation.functions.MetafileContextFunctions;

/**
 * TODO fix in translation: boolean in NAACCR-00789; had to fix in both framework; what broke it was changing the translation to use typed variables...
 * TODO in meta context functions, I fixed some int into Object; but I should be consistent and fix all of them
 * TODO half of the time of loading is parsing the properties, context and lookups. This can be highly optimized
 */
public class TranformMetafileLab {

    //public static final File MAIN_FOLDER = new File("C:\\dev\\");
    public static final File MAIN_FOLDER = new File("D:\\Users\\depryf\\dev\\");

    public static void main(String[] args) throws Exception {
        File metafile = new File(MAIN_FOLDER, "naaccr-translated-edits.xml");

        ValidatorServices.initialize(new ValidatorServices());
        ValidatorContextFunctions.initialize(new MetafileContextFunctions());

        System.out.println("Loading XML file...");
        long startLoad = System.currentTimeMillis();
        Validator v = XmlValidatorFactory.loadValidatorFromXml(metafile);
        System.out.println("   > done loading in " + (System.currentTimeMillis() - startLoad) + "ms");

        //runEdits1(v);
        //translate(v);
        //createFakeFile(MAIN_FOLDER, "naaccr-16-10-rec.txt.gz", 10);
        //runEdits3(v, new File(MAIN_FOLDER, "naaccr-16-large-file.txt.gz"), 4);
        runEdits3(v, new File(MAIN_FOLDER, "naaccr-16-large-file.txt.gz"), 4);
    }

    private static void createFakeFile(File parentFolder, String filename, int numLines) throws Exception {
        new NaaccrDataGenerator(LayoutFactory.LAYOUT_ID_NAACCR_16).generateFile(new File(parentFolder, filename), numLines);
    }

    private static void runEdits1(Validator v) throws Exception {
        System.out.println("Adding XML file...");
        printStats(ValidationEngine.initialize(v));

        System.out.println("Running XML file...");
        Map<String, String> entity = new HashMap<>();
        entity.put("nameLast", "123");
        SimpleNaaccrLinesValidatable validatable = new SimpleNaaccrLinesValidatable(Collections.singletonList(entity), null, true);
        System.out.println("  > num result before: " + ValidationEngine.validate(validatable).size());
        entity.put("nameLast", "ABC");
        System.out.println("  > num result after: " + ValidationEngine.validate(validatable).size());
        ValidationEngine.uninitialize();
    }

    private static void runEdits2(Validator v, File file) throws Exception {

        System.out.println("Adding XML file...");
        printStats(ValidationEngine.initialize(v));

        NaaccrLayout layout = (NaaccrLayout)LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_16);

        System.out.println("Running edits...");
        long start = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger(0);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))))) {
            String line = reader.readLine();
            while (line != null) {
                SimpleNaaccrLinesValidatable e = new SimpleNaaccrLinesValidatable(Collections.singletonList(layout.createRecordFromLine(line)), null, true);
                count.addAndGet(ValidationEngine.validate(e).size());
                line = reader.readLine();
            }
        }
        System.out.println(" > done in " + (System.currentTimeMillis() - start) + "ms; found " + count.get() + " failures...");
    }

    private static void runEdits3(Validator v, File file, int numThreads) throws Exception {

        System.out.println("Adding XML file...");
        printStats(ValidationEngine.initialize(v));

        NaaccrLayout layout = (NaaccrLayout)LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_16);

        System.out.println("Running edits...");
        long start = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))))) {
            String line = reader.readLine();
            while (line != null) {
                executor.submit(new MyRunnable(line, count, layout));
                line = reader.readLine();
            }
        }
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.MINUTES);
        System.out.println(" > done in " + (System.currentTimeMillis() - start) + "ms; found " + count.get() + " failures...");
    }

    private static void printStats(ValidationEngineInitializationStats stats) {
        System.out.println("   > done initialization in " + stats.getInitializationDuration() + " ms; num edits compiled: " + stats.getNumEditsDynamicallyCompiled() + "; num edits found on classpath: " + stats
                .getNumEditsStaticallyCompiled());
    }

    private static void translate(Validator v) throws Exception {
        System.out.println("Translating it...");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(MAIN_FOLDER, "NaaccrTranslatedEdits.groovy")))) {
            writer.write("import com.imsweb.validation.functions.MetafileContextFunctions\n");
            writer.write("import groovy.transform.CompileStatic\n");
            writer.write("\n");
            writer.write("@CompileStatic\n");
            writer.write("class " + createClassName(v.getId()) + " {\n\n");
            for (Rule r : v.getRules()) {
                if (!hasInnerMethod(r.getExpression())) {
                    writer.write("boolean " + createMethodName(r.getId()) + "(Binding binding, Map<String, Object> context, MetafileContextFunctions functions");
                    //for (String s : r.getJavaPath().split("\\."))
                    //    writer.write(", Map<String, Object> " + s);
                    writer.write(", List<Map<String, String>> untrimmedlines");
                    writer.write(", Map<String, String> untrimmedline");
                    writer.write(") throws Exception {\n");
                    writer.write(r.getExpression()
                            .replace("Functions.", "functions.")
                            .replace("Context.", "context."));
                    writer.write("\n}\n\n");
                }
            }
            writer.write("}");
        }
    }

    private static String createClassName(String id) {
        return "NaaccrTranslatedEdits"; // TODO
    }

    private static String createMethodName(String id) {
        return id.replace("-", "_"); // TODO
    }

    private static boolean hasInnerMethod(String expression) {
        for (String line : expression.split("\r?\n"))
            if (line.matches("^def [A-Za-z0-9_]+\\(\\) \\{$"))
                return true;
        return false;
    }

    private static class MyRunnable implements Runnable {

        private String _line;

        private AtomicInteger _count;

        private NaaccrLayout _layout;

        public MyRunnable(String line, AtomicInteger count, NaaccrLayout layout) {
            _line = line;
            _count = count;
            _layout = layout;
        }

        @Override
        public void run() {
            try {
                SimpleNaaccrLinesValidatable e = new SimpleNaaccrLinesValidatable(Collections.singletonList(_layout.createRecordFromLine(_line)), null, true);
                Collection<RuleFailure> failures = ValidationEngine.validate(e);
                _count.addAndGet(failures.size());
                for (RuleFailure failure : failures) {
                    //System.out.println(failure.getRule().getId() + ": " + failure.getMessage());
                    if (failure.getGroovyException() != null)
                        System.out.println("!!! " + failure.getGroovyException().getMessage());
                }

            }
            catch (Exception e1) {
                throw new RuntimeException(e1);
            }
        }
    }
}
