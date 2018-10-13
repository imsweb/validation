/*
 * Copyright (C) 2018 Information Management Services, Inc.
 */
package com.imsweb.validation.runtime;

public interface RuntimeEdits {

    CompiledRules getCompiledRules();

    ParsedProperties getParsedProperties();

    ParsedContexts getParsedContexts();

    ParsedLookups getParsedLookups();
}
