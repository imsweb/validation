/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package lab;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.imsweb.validation.ValidationEngine;
import com.imsweb.validation.ValidatorContextFunctions;
import com.imsweb.validation.ValidatorServices;
import com.imsweb.validation.XmlValidatorFactory;
import com.imsweb.validation.entities.Rule;
import com.imsweb.validation.entities.SimpleNaaccrLinesValidatable;
import com.imsweb.validation.entities.Validator;
import com.imsweb.validation.functions.MetafileContextFunctions;

public class TranformMetafileLab {

    public static final File MAIN_FOLDER = new File("D:\\Users\\depryf\\dev\\");

    public static void main(String[] args) throws Exception {
        File metafile = new File(MAIN_FOLDER, "naaccr-translated-edits-min.xml");

        ValidatorServices.initialize(new ValidatorServices() {
            @Override
            public void log(String message) {
                System.out.println(message);
            }
        });
        ValidatorContextFunctions.initialize(new MetafileContextFunctions());

        System.out.println("Loading XML file...");
        Validator v = XmlValidatorFactory.loadValidatorFromXml(metafile);

        runEdits(v);
        translate(v);
    }

    private static void runEdits(Validator v) throws Exception {
        System.out.println("Adding XML file...");
        ValidationEngine.initialize(v);

        System.out.println("Running XML file...");
        Map<String, String> entity = new HashMap<>();
        entity.put("nameLast", "123");
        SimpleNaaccrLinesValidatable validatable = new SimpleNaaccrLinesValidatable(Collections.singletonList(entity), null, true);
        System.out.println("  > num result before: " + ValidationEngine.validate(validatable).size());
        entity.put("nameLast", "ABC");
        System.out.println("  > num result after: " + ValidationEngine.validate(validatable).size());
        ValidationEngine.uninitialize();
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
                //if (!hasInnerMethod(r.getExpression())) {
                    writer.write("boolean " + createMethodName(r.getId()) + "(Binding binding, Map<String, Object> context, MetafileContextFunctions functions");
                    //for (String s : r.getJavaPath().split("\\."))
                    //    writer.write(", Map<String, Object> " + s);
                    writer.write(", List<Map<String, String>> untrimmedlines");
                    writer.write(", Map<String, String> untrimmedline");
                    writer.write(") throws Exception {\n");
                    writer.write(r.getExpression().replace("Functions.", "functions.").replace("Context.", "context."));
                    writer.write("\n}\n\n");
                //}
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
}
