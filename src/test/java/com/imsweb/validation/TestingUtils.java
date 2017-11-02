/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.validation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;

import com.imsweb.validation.entities.EditableValidator;
import com.imsweb.validation.entities.RuleFailure;
import com.imsweb.validation.entities.Validator;
import com.imsweb.validation.shared.ContextFunctionAliasAnnotation;

public final class TestingUtils {

    public static File TMP_DIR = new File(System.getProperty("user.dir") + "/build/test-tmp");

    public static void init() {

        if (!TMP_DIR.exists() && !TMP_DIR.mkdirs())
            throw new RuntimeException("Unable to create tmp folder '" + TMP_DIR.getPath() + "'");

        // initialize services
        if (!ValidatorServices.isInitialized())
            ValidatorServices.initialize(new TestingValidatorServices());

        // initialize context functions
        if (!ValidatorContextFunctions.isInitialized())
            ValidatorContextFunctions.initialize(new TestingValidatorContextFunctions());

        // initialize engine
        if (!ValidationEngine.isInitialized())
            ValidationEngine.initialize();

        // no edits should take more than one second (except the one tha tests the timeout)
        ValidationEngine.enableEditExecutionTimeout(1);
    }

    public static Validator loadValidator(String id) {
        Validator v = ValidationEngine.getValidator(id);

        if (v == null) {
            try {
                v = ValidationEngine.addValidator(new EditableValidator(XmlValidatorFactory.loadValidatorFromXml(Thread.currentThread().getContextClassLoader().getResource(id + ".xml"))));
            }
            catch (Exception e) {
                throw new RuntimeException("Unable to load '" + id + "'", e);
            }
        }

        return v;
    }

    public static void unloadValidator(String id) {
        try {
            ValidationEngine.deleteValidator(id);
        }
        catch (Exception e) {
            throw new RuntimeException("Unable to unload '" + id + "'", e);
        }
    }

    public static String readResource(String path) {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path); ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            IOUtils.copy(is, os);
            return os.toString();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void assertEditFailure(Collection<RuleFailure> results, String... ruleIds) {
        if (ruleIds.length == 0)
            throw new RuntimeException("This method requires at least one rule ID!");

        for (String ruleId : ruleIds) {
            boolean found = false;
            for (RuleFailure rf : results) {
                if (ruleId.equals(rf.getRule().getId())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                StringBuilder buf = new StringBuilder();
                for (RuleFailure rf : results)
                    buf.append("  ").append(rf.getRule().getId()).append(": ").append(rf.getMessage()).append("\n");
                if (buf.length() == 0)
                    buf.append("none");
                Assert.fail("\nWas expecting a failure for '" + ruleId + "' but didn't get it; failures:\n" + buf);
            }
        }
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public static void assertNoEditFailure(Collection<RuleFailure> results, String... ruleIds) {
        if (ruleIds.length == 0)
            throw new RuntimeException("This method requires at least one rule ID!");

        for (String ruleId : ruleIds) {
            boolean found = false;
            for (RuleFailure rf : results) {
                if (ruleId.equals(rf.getRule().getId())) {
                    found = true;
                    break;
                }
            }

            if (found) {
                StringBuilder buf = new StringBuilder();
                for (RuleFailure rf : results) {
                    if (rf.getGroovyException() != null)
                        buf.append("  ").append(rf.getRule().getId()).append(": [EXCEPTION] ").append(rf.getGroovyException().getMessage()).append("\n");
                    else
                        buf.append("  ").append(rf.getRule().getId()).append(": ").append(rf.getMessage()).append("\n");
                }
                if (buf.length() == 0)
                    buf.append("none");
                Assert.fail("\nWas expecting no failure for '" + ruleId + "' but got it; failures:\n" + buf);
            }
        }
    }

    public static void assertEditFailures(Collection<RuleFailure> results) {
        if (results.isEmpty())
            Assert.fail("\nWas expecting at least one failure but got none");
    }

    public static void assertLogMessage(String x) {
        Assert.assertTrue(((TestingValidatorServices)ValidatorServices.getInstance()).getLogMessages().contains(x));
    }

    private static class TestingValidatorContextFunctions extends ValidatorContextFunctions {

        @SuppressWarnings("unused")
        @ContextFunctionAliasAnnotation(value = "ctc")
        public Map<String, String> getCtcFromAllCtcs(Object obj) {
            return null;
        }

        @SuppressWarnings("unused")
        @ContextFunctionAliasAnnotation(value = "line")
        public Map<String, String> getNaaccrLineFromAllLines(Object obj) {
            return null;
        }
    }

    private static class TestingValidatorServices extends ValidatorServices {

        /**
         * Map of java-path -> alias to use in the edits
         */
        private static final Map<String, String> _EXTRA_ALIASES = new HashMap<>();

        private List<String> _logMessages = new ArrayList<>();

        static {
            _EXTRA_ALIASES.put("level1", "level1");
            _EXTRA_ALIASES.put("level1.level2", "level2");
            _EXTRA_ALIASES.put("level1.level2.level3", "level3");

            _EXTRA_ALIASES.put("level", "level");
            _EXTRA_ALIASES.put("level-runtime", "level-runtime");

            _EXTRA_ALIASES.put("root", "root");
            _EXTRA_ALIASES.put("root.repeatedObjects", "repeatedObject");

            _EXTRA_ALIASES.put("patient", "patient");
            _EXTRA_ALIASES.put("patient.ctcs", "ctc");
            _EXTRA_ALIASES.put("patient.ctcs.facilityAdmissions", "facilityAdmission");
            _EXTRA_ALIASES.put("patient.ctcs.courses", "course");
            _EXTRA_ALIASES.put("patient.ctcs.courses.treatmentProcedures", "treatmentProcedure");
        }

        @Override
        public String getAliasForJavaPath(String javaPath) {
            if (_EXTRA_ALIASES.containsKey(javaPath))
                return _EXTRA_ALIASES.get(javaPath);
            return super.getAliasForJavaPath(javaPath);
        }

        @Override
        public String getJavaPathForAlias(String alias) {
            for (Map.Entry<String, String> entry : _EXTRA_ALIASES.entrySet())
                if (entry.getValue().equals(alias))
                    return entry.getKey();
            return super.getJavaPathForAlias(alias);
        }

        @Override
        public Map<String, String> getAllJavaPaths() {
            Map<String, String> result = new HashMap<>();

            result.putAll(super.getAllJavaPaths());
            result.putAll(_EXTRA_ALIASES);

            return result;
        }

        @Override
        public void log(String message) {
            _logMessages.add(message);
        }

        @Override
        public void logWarning(String message) {
            _logMessages.add(message);
        }

        @Override
        public void logError(String message) {
            _logMessages.add(message);
        }

        public List<String> getLogMessages() {
            return _logMessages;
        }
    }

}
