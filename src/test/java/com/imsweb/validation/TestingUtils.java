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
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;

import com.imsweb.staging.Staging;
import com.imsweb.validation.entities.EditableValidator;
import com.imsweb.validation.entities.RuleFailure;
import com.imsweb.validation.entities.Validator;

public final class TestingUtils {

    public static File TMP_DIR = new File(getWorkingDirectory() + "/build/test-tmp");

    private static Staging _CS_STAGING;
    private static Staging _TNM_STAGING;
    private static Staging _EOD_STAGING;

    public static void init() {

        if (!TMP_DIR.exists() && !TMP_DIR.mkdirs())
            throw new IllegalStateException("Unable to create tmp folder '" + TMP_DIR.getPath() + "'");

        // initialize engine
        if (!ValidationEngine.getInstance().isInitialized()) {
            ValidationServices.initialize(new TestingValidationServices());
            ValidationContextFunctions.initialize(new TestingValidationContextFunctions());

            InitializationOptions options = new InitializationOptions();
            options.enableEngineStats();
            ValidationEngine.getInstance().initialize(options);
        }

        if (_CS_STAGING == null)
            _CS_STAGING = loadStagingInstance("cs-02.05.50.zip");
        if (_TNM_STAGING == null)
            _TNM_STAGING = loadStagingInstance("tnm-2.1.zip");
        if (_EOD_STAGING == null)
            _EOD_STAGING = loadStagingInstance("eod_public-3.3.zip");
    }

    private static Staging loadStagingInstance(String data) {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("staging/" + data)) {
            return Staging.getInstance(is);
        }
        catch (IOException e) {
            throw new IllegalStateException("Unable to initialize staging from " + data, e);
        }
    }

    public static Staging getCsStaging() {
        return _CS_STAGING;
    }

    public static Staging getTnmStaging() {
        return _TNM_STAGING;
    }

    public static Staging getEodStaging() {
        return _EOD_STAGING;
    }

    public static String getWorkingDirectory() {
        return System.getProperty("user.dir").replace(".idea\\modules", "");
    }

    public static Validator loadValidator(String id) {
        Validator v = ValidationEngine.getInstance().getValidator(id);

        if (v == null) {
            try {
                v = ValidationEngine.getInstance().addValidator(
                        new EditableValidator(ValidationXmlUtils.loadValidatorFromXml(Thread.currentThread().getContextClassLoader().getResource(id + ".xml"))));
            }
            catch (Exception e) {
                throw new IllegalStateException("Unable to load '" + id + "'", e);
            }
        }

        return v;
    }

    public static void unloadValidator(String id) {
        try {
            ValidationEngine.getInstance().deleteValidator(id);
        }
        catch (Exception e) {
            throw new IllegalStateException("Unable to unload '" + id + "'", e);
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static String readResource(String path) {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path); ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            IOUtils.copy(is, os);
            return os.toString();
        }
        catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void assertEditFailure(Collection<RuleFailure> results, String... ruleIds) {
        if (ruleIds.length == 0)
            throw new IllegalStateException("This method requires at least one rule ID!");

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
                if (buf.isEmpty())
                    buf.append("none");
                Assert.fail("\nWas expecting a failure for '" + ruleId + "' but didn't get it; failures:\n" + buf);
            }
        }
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public static void assertNoEditFailure(Collection<RuleFailure> results, String... ruleIds) {
        if (ruleIds.length == 0)
            throw new IllegalStateException("This method requires at least one rule ID!");

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
                if (buf.isEmpty())
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
        Assert.assertTrue(((TestingValidationServices)ValidationServices.getInstance()).getLogMessages().contains(x));
    }

    public static class TestingValidationContextFunctions extends ValidationContextFunctions {

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

    public static class TestingValidationServices extends ValidationServices {

        /**
         * Map of java-path -> alias to use in the edits
         */
        private static final Map<String, String> _EXTRA_ALIASES = new HashMap<>();

        private final List<String> _logMessages = new ArrayList<>();

        static {
            _EXTRA_ALIASES.put("level1", "level1");
            _EXTRA_ALIASES.put("level1.level2", "level2");
            _EXTRA_ALIASES.put("level1.level2.level3", "level3");

            _EXTRA_ALIASES.put("level", "level");

            _EXTRA_ALIASES.put("runtime", "runtime");

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
        public void log(Object message) {
            _logMessages.add(Objects.toString(message, ""));
        }

        @Override
        public void logWarning(Object message) {
            _logMessages.add(Objects.toString(message, ""));
        }

        @Override
        public void logError(Object message) {
            _logMessages.add(Objects.toString(message, ""));
        }

        public List<String> getLogMessages() {
            return _logMessages;
        }
    }

}
