package com.imsweb.validation.runtime.validator;

import java.io.IOException;

import com.imsweb.validation.ValidationXmlUtils;
import com.imsweb.validation.entities.Validator;
import com.imsweb.validation.runtime.CompiledRules;
import com.imsweb.validation.runtime.ParsedContexts;
import com.imsweb.validation.runtime.ParsedLookups;
import com.imsweb.validation.runtime.ParsedProperties;
import com.imsweb.validation.runtime.RuntimeEdits;

public class FakeValidatorRuntime implements RuntimeEdits {

    public static Validator validator() {
        try {
            return ValidationXmlUtils.loadValidatorFromXml(Thread.currentThread().getContextClassLoader().getResource("fake-validator-runtime.xml"), new FakeValidatorRuntime());
        }
        catch (IOException e) {
            throw new RuntimeException("Unable to load validator", e);
        }
    }

    @Override
    public CompiledRules getCompiledRules() {
        return new FakeValidatorRuntimeCompiledRules();
    }

    @Override
    public ParsedProperties getParsedProperties() {
        return new FakeValidatorRuntimeParsedProperties();
    }

    @Override
    public ParsedContexts getParsedContexts() {
        return new FakeValidatorRuntimeParsedContexts();
    }

    @Override
    public ParsedLookups getParsedLookups() {
        return new FakeValidatorRuntimeParsedLookups();
    }
}
