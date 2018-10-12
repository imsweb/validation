package com.imsweb.validation.runtime;

import java.io.IOException;

import com.imsweb.validation.ValidationXmlUtils;
import com.imsweb.validation.entities.Validator;

public class FakeValidatorRuntimeUtils {

    // TODO FD review this, this is really not final...

    public static Validator getValidator() {

        // this is still relying on the classpath to find the runtime pre-parsed components...

        try {
            Validator v =  ValidationXmlUtils.loadValidatorFromXml(Thread.currentThread().getContextClassLoader().getResource("fake-validator-runtime.xml"));
            v.setCompiledRules(new FakeValidatorRuntimeCompiledRules());
            return v;
        }
        catch (IOException e) {
            throw new RuntimeException("Unable to load validator", e);
        }
    }

}
