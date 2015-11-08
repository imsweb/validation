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

    private static final boolean _VERBOSE = false;

    /**
     * Created on Oct 7, 2011 by depryf
     */
    @Test
    public void testLexer() throws Exception {
        if (_VERBOSE) {
            print("['abc']");
            print("['ab\\'c']");
            print("'ab\\'c'");
            print("'Hello \\'World\\''");
        }

        Assert.assertEquals("Hello 'World'", new JavaContextLexer(new StringReader("'Hello \\'World\\''")).next_token().getValue().toString());
    }

    private Symbol print(String input) throws Exception {
        System.out.println(">>>>>>>>>>>>>>>>   " + input);
        JavaContextLexer l = new JavaContextLexer(new StringReader(input));
        Symbol s = l.next_token();
        while (s != null) {
            System.out.println(s);
            s = l.next_token();
        }
        return s;
    }
}
