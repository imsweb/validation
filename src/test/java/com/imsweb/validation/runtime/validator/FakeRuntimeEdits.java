package com.imsweb.validation.runtime.validator;

import java.io.IOException;
import java.net.URL;

import com.imsweb.validation.ValidationXmlUtils;
import com.imsweb.validation.entities.Validator;
import com.imsweb.validation.runtime.CompiledRules;
import com.imsweb.validation.runtime.ParsedContexts;
import com.imsweb.validation.runtime.ParsedLookups;
import com.imsweb.validation.runtime.ParsedProperties;
import com.imsweb.validation.runtime.RuntimeEdits;

public class FakeRuntimeEdits implements RuntimeEdits {

    public static Validator getValidator() {
        try {
            return ValidationXmlUtils.loadValidatorFromXml(getXmlUrl(), new FakeRuntimeEdits());
        }
        catch (IOException e) {
            throw new RuntimeException("Unable to load validator", e);
        }
    }

    public static URL getXmlUrl() {
        return Thread.currentThread().getContextClassLoader().getResource("fake-validator-runtime.xml");
    }

    @Override
    public CompiledRules getCompiledRules() {
        return new FakeRuntimeEditsCompiledRules();
    }

    @Override
    public ParsedProperties getParsedProperties() {
        return new FakeRuntimeEditsParsedProperties();
    }

    @Override
    public ParsedContexts getParsedContexts() {
        return new FakeRuntimeEditsParsedContexts();
    }

    @Override
    public ParsedLookups getParsedLookups() {
        return new FakeRuntimeEditsParsedLookups();
    }
}
