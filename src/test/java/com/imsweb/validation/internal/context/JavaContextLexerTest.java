/*
 * Copyright (C) 2011 Information Management Services, Inc.
 */
package com.imsweb.validation.internal.context;

import java.io.StringReader;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created on Oct 7, 2011 by depryf
 * @author depryf
 */
public class JavaContextLexerTest {

    /**
     * Created on Oct 7, 2011 by depryf
     */
    @Test
    public void testLexer() throws Exception {
        Assert.assertEquals("Hello 'World'", new JavaContextLexer(new StringReader("'Hello \\'World\\''")).next_token().getValue().toString());
    }
}
