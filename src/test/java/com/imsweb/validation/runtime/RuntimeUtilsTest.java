/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.validation.runtime;

import org.junit.Assert;
import org.junit.Test;

public class RuntimeUtilsTest {

    @Test
    public void testCreateMethodName() {
        Assert.assertEquals("id", RuntimeUtils.createMethodName("id"));
        Assert.assertEquals("id", RuntimeUtils.createMethodName("ID"));
        Assert.assertEquals("id", RuntimeUtils.createMethodName("iD"));
        Assert.assertEquals("di", RuntimeUtils.createMethodName("Di"));
        Assert.assertEquals("naaccr0001", RuntimeUtils.createMethodName("NAACCR0001"));
        Assert.assertEquals("naaccr0001", RuntimeUtils.createMethodName("NAACCR-0001"));
        Assert.assertEquals("naaccr0001", RuntimeUtils.createMethodName("NAACCR_0001"));
        Assert.assertEquals("naaccr0001", RuntimeUtils.createMethodName("NAACCR 0001"));
        Assert.assertEquals("if100", RuntimeUtils.createMethodName("IF100"));
        Assert.assertEquals("if100", RuntimeUtils.createMethodName("IF100(2000+)"));
    }

    @Test
    public void testCreateCompiledRulesClassName() {
        Assert.assertEquals("NaaccrTranslatedCompiledRules", RuntimeUtils.createCompiledRulesClassName("naaccr-translated"));
        Assert.assertEquals("SeerCompiledRules", RuntimeUtils.createCompiledRulesClassName("seer"));
        Assert.assertEquals("SeerExtendedCompiledRules", RuntimeUtils.createCompiledRulesClassName("seer-extended"));
    }

    @Test
    public void testCreateParsedPropertiesClassName() {
        Assert.assertEquals("NaaccrTranslatedParsedProperties", RuntimeUtils.createParsedPropertiesClassName("naaccr-translated"));
        Assert.assertEquals("SeerParsedProperties", RuntimeUtils.createParsedPropertiesClassName("seer"));
        Assert.assertEquals("SeerExtendedParsedProperties", RuntimeUtils.createParsedPropertiesClassName("seer-extended"));
    }

    @Test
    public void testCreateParsedLookupsClassName() {
        Assert.assertEquals("NaaccrTranslatedParsedLookups", RuntimeUtils.createParsedLookupsClassName("naaccr-translated"));
        Assert.assertEquals("SeerParsedLookups", RuntimeUtils.createParsedLookupsClassName("seer"));
        Assert.assertEquals("SeerExtendedParsedLookups", RuntimeUtils.createParsedLookupsClassName("seer-extended"));
    }

    @Test
    public void testCreateParsedContextsClassName() {
        Assert.assertEquals("NaaccrTranslatedParsedContexts", RuntimeUtils.createParsedContextsClassName("naaccr-translated"));
        Assert.assertEquals("SeerParsedContexts", RuntimeUtils.createParsedContextsClassName("seer"));
        Assert.assertEquals("SeerExtendedParsedContexts", RuntimeUtils.createParsedContextsClassName("seer-extended"));
    }
}
