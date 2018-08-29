/*
 * Copyright (C) 2011 Information Management Services, Inc.
 */
package com.imsweb.validation.functions;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import groovy.lang.Binding;

import com.imsweb.staging.Staging;
import com.imsweb.staging.cs.CsDataProvider;
import com.imsweb.validation.ConstructionException;
import com.imsweb.validation.TestingUtils;
import com.imsweb.validation.entities.ContextTable;
import com.imsweb.validation.entities.ContextTableIndex;
import com.imsweb.validation.internal.context.JavaContextParser;

public class MetafileContextFunctionsTest {

    private MetafileContextFunctions _functions;

    @Before
    public void setUp() {
        TestingUtils.init();

        _functions = new MetafileContextFunctions(Staging.getInstance(CsDataProvider.getInstance(CsDataProvider.CsVersion.LATEST)));
    }

    @Test
    public void testGEN_VAL() {
        Assert.assertEquals(0, _functions.GEN_VAL(null));
        Assert.assertEquals(0, _functions.GEN_VAL(""));
        Assert.assertEquals(0, _functions.GEN_VAL("   "));
        Assert.assertEquals(0, _functions.GEN_VAL("abc"));
        Assert.assertEquals(0, _functions.GEN_VAL("abc1"));
        Assert.assertEquals(1, _functions.GEN_VAL("1"));
        Assert.assertEquals(-1, _functions.GEN_VAL("-1"));
        Assert.assertEquals(1, _functions.GEN_VAL(" 1 "));
        Assert.assertEquals(1000, _functions.GEN_VAL("1000"));
        Assert.assertEquals(1, _functions.GEN_VAL("1abc"));
        Assert.assertEquals(2, _functions.GEN_VAL("2abc1"));
        Assert.assertEquals(-2, _functions.GEN_VAL("-2abc1"));
        Assert.assertEquals(0, _functions.GEN_VAL("-0"));
        Assert.assertEquals(-1, _functions.GEN_VAL("-01"));
        Assert.assertEquals(-22, _functions.GEN_VAL("-22"));
        Assert.assertEquals(1, _functions.GEN_VAL("1-"));
    }

    @Test
    public void testGEN_INLIST() {
        Assert.assertFalse(_functions.GEN_INLIST(null, "1,10-13,101-111"));
        Assert.assertFalse(_functions.GEN_INLIST("", "1,10-13,101-111"));
        Assert.assertFalse(_functions.GEN_INLIST("     ", "1,10-13,101-111"));
        Assert.assertFalse(_functions.GEN_INLIST("100", "1,10-13,101-111"));
        Assert.assertTrue(_functions.GEN_INLIST("101", "1,10-13,101-111"));

        // value is trimmed...
        Assert.assertTrue(_functions.GEN_INLIST("101    ", "1,10-13,101-111"));
        Assert.assertTrue(_functions.GEN_INLIST("    101", "1,10-13,101-111"));
        Assert.assertTrue(_functions.GEN_INLIST("    101    ", "1,10-13,101-111"));

        Assert.assertFalse(_functions.GEN_INLIST("2x", "3-5"));

        // yet a single space should be found
        Assert.assertTrue(_functions.GEN_INLIST(" ", " "));
        Assert.assertTrue(_functions.GEN_INLIST(" ", "0-9", "\\d|\\s"));

        // value is only right-trim when a regex is provided
        Assert.assertTrue(_functions.GEN_INLIST("101", "100-150,225-229", "\\d\\d\\d"));
        Assert.assertTrue(_functions.GEN_INLIST("101   ", "100-150,225-229", "\\d\\d\\d"));
        Assert.assertFalse(_functions.GEN_INLIST("    101", "100-150,225-229", "\\d\\d\\d"));
        Assert.assertFalse(_functions.GEN_INLIST("    101    ", "100-150,225-229", "\\d\\d\\d"));

        // following block has been tested with Genedits
        Assert.assertFalse(_functions.GEN_INLIST("C101", "100-150", "C\\d\\d\\d", 2, 2));
        Assert.assertTrue(_functions.GEN_INLIST("C101", "100-150", "C\\d\\d\\d", 2, 3));
        Assert.assertTrue(_functions.GEN_INLIST("C101", "100-150", "C\\d\\d\\d", 2, 4));
        Assert.assertTrue(_functions.GEN_INLIST("C10", "10-15", "C\\d\\d", 2, 2));
        Assert.assertTrue(_functions.GEN_INLIST("C10", "10-15", "C\\d\\d", 2, 3));
        Assert.assertTrue(_functions.GEN_INLIST("C10", "10-15", "C\\d\\d", 2, 4));
        Assert.assertTrue(_functions.GEN_INLIST("101", "1-5", "\\d\\d\\d", 2, 2));
        Assert.assertTrue(_functions.GEN_INLIST("101", "1-5", "\\d\\d\\d", 2, 3));
        Assert.assertTrue(_functions.GEN_INLIST("101", "1-5", "\\d\\d\\d", 2, 4));
        Assert.assertTrue(_functions.GEN_INLIST("C10 ", "10-15", "C\\d\\d\\s", 2, 2));
        Assert.assertTrue(_functions.GEN_INLIST("C10 ", "10-15", "C\\d\\d\\s", 2, 3));
        Assert.assertTrue(_functions.GEN_INLIST("C10 ", "10-15", "C\\d\\d\\s", 2, 4));
        Assert.assertFalse(_functions.GEN_INLIST(" 101", "100-150", "\\s\\d\\d\\d", 2, 2));
        Assert.assertTrue(_functions.GEN_INLIST(" 101", "100-150", "\\s\\d\\d\\d", 2, 3));
        Assert.assertTrue(_functions.GEN_INLIST(" 101", "100-150", "\\s\\d\\d\\d", 2, 4));
        Assert.assertFalse(_functions.GEN_INLIST(" 01 ", "100-150", "\\s\\d\\d\\s", 2, 2));
        Assert.assertFalse(_functions.GEN_INLIST(" 01 ", "100-150", "\\s\\d\\d\\s", 2, 2));
        Assert.assertFalse(_functions.GEN_INLIST(" 01 ", "100-150", "\\s\\d\\d\\s", 2, 4));
        Assert.assertTrue(_functions.GEN_INLIST("cX  ", "X,0,1", "(c[A-Za-z0-9]\\s\\s)", 2, 4));
        Assert.assertFalse(_functions.GEN_INLIST("cXxx", "X,0,1", "(c[A-Za-z0-9]\\s\\s)", 2, 4));
        Assert.assertFalse(_functions.GEN_INLIST("c   ", "X,0,1", "(c[A-Za-z0-9]\\s\\s)", 2, 4));
        Assert.assertFalse(_functions.GEN_INLIST("    ", "X,0,1", "(c[A-Za-z0-9]\\s\\s)", 2, 4));
        Assert.assertTrue(_functions.GEN_INLIST("cX  ", "X,0,1", "(c[A-Za-z0-9]\\s\\s)", 2, 10));
        Assert.assertFalse(_functions.GEN_INLIST("cX  ", "X,0,1", "(c[A-Za-z0-9]\\s\\s)", 4, 4));
        Assert.assertFalse(_functions.GEN_INLIST("cX  ", "X,0,1", "(c[A-Za-z0-9]\\s\\s)", 10, 4));

        // I just don't understand how that function works!!!  All the following cases have been tested using genedits...
        Assert.assertFalse(_functions.GEN_INLIST("", "")); // this one is not a valid syntax in genedits
        Assert.assertTrue(_functions.GEN_INLIST(" ", " "));
        Assert.assertTrue(_functions.GEN_INLIST("", " "));
        Assert.assertFalse(_functions.GEN_INLIST(" ", "")); // this one is not a valid syntax in genedits
        Assert.assertFalse(_functions.GEN_INLIST("", "1"));
        Assert.assertFalse(_functions.GEN_INLIST(" ", "1"));
        Assert.assertTrue(_functions.GEN_INLIST("1", "1"));
        Assert.assertTrue(_functions.GEN_INLIST("1 ", "1"));
        Assert.assertTrue(_functions.GEN_INLIST(" 1", "1"));
        Assert.assertTrue(_functions.GEN_INLIST(" 1 ", "1"));
        Assert.assertTrue(_functions.GEN_INLIST("1", " 1"));
        Assert.assertTrue(_functions.GEN_INLIST("1", "1 "));
        Assert.assertTrue(_functions.GEN_INLIST("1", " 1 "));

        Assert.assertFalse(_functions.GEN_INLIST("", "", "\\s")); // this one is not a valid syntax in genedits
        Assert.assertTrue(_functions.GEN_INLIST(" ", " ", "\\s"));
        Assert.assertTrue(_functions.GEN_INLIST("", " ", "\\s"));
        Assert.assertFalse(_functions.GEN_INLIST(" ", "", "\\s")); // this one is not a valid syntax in genedits
        Assert.assertTrue(_functions.GEN_INLIST("", "1", "\\s"));
        Assert.assertTrue(_functions.GEN_INLIST(" ", "1", "\\s"));
        Assert.assertFalse(_functions.GEN_INLIST("1", "1", "\\s"));
        Assert.assertFalse(_functions.GEN_INLIST("1 ", "1", "\\s"));
        Assert.assertFalse(_functions.GEN_INLIST(" 1", "1", "\\s"));
        Assert.assertFalse(_functions.GEN_INLIST(" 1 ", "1", "\\s"));
        Assert.assertFalse(_functions.GEN_INLIST("1", " 1", "\\s"));
        Assert.assertFalse(_functions.GEN_INLIST("1", "1 ", "\\s"));
        Assert.assertFalse(_functions.GEN_INLIST("1", " 1 ", "\\s"));

        Assert.assertFalse(_functions.GEN_INLIST("", "", "\\d")); // this one is not a valid syntax in genedits
        Assert.assertFalse(_functions.GEN_INLIST(" ", " ", "\\d"));
        Assert.assertFalse(_functions.GEN_INLIST("", " ", "\\d"));
        Assert.assertFalse(_functions.GEN_INLIST(" ", "", "\\d")); // this one is not a valid syntax in genedits
        Assert.assertFalse(_functions.GEN_INLIST("", "1", "\\d"));
        Assert.assertFalse(_functions.GEN_INLIST(" ", "1", "\\d"));
        Assert.assertTrue(_functions.GEN_INLIST("1", "1", "\\d"));
        Assert.assertTrue(_functions.GEN_INLIST("1 ", "1", "\\d"));
        Assert.assertFalse(_functions.GEN_INLIST(" 1", "1", "\\d"));
        Assert.assertFalse(_functions.GEN_INLIST(" 1 ", "1", "\\d"));
        Assert.assertTrue(_functions.GEN_INLIST("1", " 1", "\\d"));
        Assert.assertTrue(_functions.GEN_INLIST("1", "1 ", "\\d"));
        Assert.assertTrue(_functions.GEN_INLIST("1", " 1 ", "\\d"));

        Assert.assertFalse(_functions.GEN_INLIST("", "", "(\\s|\\d)")); // this one is not a valid syntax in genedits
        Assert.assertTrue(_functions.GEN_INLIST(" ", " ", "(\\s|\\d)"));
        Assert.assertTrue(_functions.GEN_INLIST("", " ", "(\\s|\\d)"));
        Assert.assertFalse(_functions.GEN_INLIST(" ", "", "(\\s|\\d)")); // this one is not a valid syntax in genedits
        Assert.assertTrue(_functions.GEN_INLIST("", "1", "(\\s|\\d)"));
        Assert.assertTrue(_functions.GEN_INLIST(" ", "1", "(\\s|\\d)"));
        Assert.assertTrue(_functions.GEN_INLIST("1", "1", "(\\s|\\d)"));
        Assert.assertTrue(_functions.GEN_INLIST("1 ", "1", "(\\s|\\d)"));
        Assert.assertFalse(_functions.GEN_INLIST(" 1", "1", "(\\s|\\d)"));
        Assert.assertFalse(_functions.GEN_INLIST(" 1 ", "1", "(\\s|\\d)"));
        Assert.assertTrue(_functions.GEN_INLIST("1", " 1", "(\\s|\\d)"));
        Assert.assertTrue(_functions.GEN_INLIST("1", "1 ", "(\\s|\\d)"));
        Assert.assertTrue(_functions.GEN_INLIST("1", " 1 ", "(\\s|\\d)"));

        Assert.assertFalse(_functions.GEN_INLIST("", "", "(\\d|\\s)")); // this one is not a valid syntax in genedits
        Assert.assertTrue(_functions.GEN_INLIST(" ", " ", "(\\d|\\s)"));
        Assert.assertTrue(_functions.GEN_INLIST("", " ", "(\\d|\\s)"));
        Assert.assertFalse(_functions.GEN_INLIST(" ", "", "(\\d|\\s)")); // this one is not a valid syntax in genedits
        Assert.assertTrue(_functions.GEN_INLIST("", "1", "(\\d|\\s)"));
        Assert.assertTrue(_functions.GEN_INLIST(" ", "1", "(\\d|\\s)"));
        Assert.assertTrue(_functions.GEN_INLIST("1", "1", "(\\d|\\s)"));
        Assert.assertTrue(_functions.GEN_INLIST("1 ", "1", "(\\d|\\s)"));
        Assert.assertFalse(_functions.GEN_INLIST(" 1", "1", "(\\d|\\s)"));
        Assert.assertFalse(_functions.GEN_INLIST(" 1 ", "1", "(\\d|\\s)"));
        Assert.assertTrue(_functions.GEN_INLIST("1", " 1", "(\\d|\\s)"));
        Assert.assertTrue(_functions.GEN_INLIST("1", "1 ", "(\\d|\\s)"));
        Assert.assertTrue(_functions.GEN_INLIST("1", " 1 ", "(\\d|\\s)"));

        Assert.assertTrue(_functions.GEN_INLIST(" 1", "1"));
        Assert.assertTrue(_functions.GEN_INLIST(" 1 ", "1"));
        Assert.assertTrue(_functions.GEN_INLIST("1", " 1"));
        Assert.assertTrue(_functions.GEN_INLIST("1", "1 "));
        Assert.assertTrue(_functions.GEN_INLIST("1", " 1 "));

        // Testing non-digits in the search val
        Assert.assertTrue(_functions.GEN_INLIST("2A", "1-3"));
        Assert.assertFalse(_functions.GEN_INLIST("A2", "1-3"));
        Assert.assertFalse(_functions.GEN_INLIST("1A", "1"));
        Assert.assertTrue(_functions.GEN_INLIST("1A", "1-1"));
        Assert.assertTrue(_functions.GEN_INLIST("1 A", "1-5"));
        Assert.assertTrue(_functions.GEN_INLIST("1!!", "1-5"));
        Assert.assertTrue(_functions.GEN_INLIST("1()", "1-5"));
        Assert.assertTrue(_functions.GEN_INLIST("1()9()", "1-5"));
        Assert.assertFalse(_functions.GEN_INLIST("11()9()", "1-5"));
        Assert.assertFalse(_functions.GEN_INLIST("9()", "1-5"));
        Assert.assertFalse(_functions.GEN_INLIST(";;1()", "1-5"));
        Assert.assertTrue(_functions.GEN_INLIST("1 7", "1-5"));
        Assert.assertTrue(_functions.GEN_INLIST("17A", "1-5, 10-20"));
        Assert.assertTrue(_functions.GEN_INLIST("17ABCDEFGHI", "1-5, 10-20"));
        Assert.assertTrue(_functions.GEN_INLIST("017A", "1-5, 10-20"));
        Assert.assertFalse(_functions.GEN_INLIST("OOOO017A", "1-5, 10-20"));
        Assert.assertTrue(_functions.GEN_INLIST("00017A", "1-5, 10-20"));
        Assert.assertTrue(_functions.GEN_INLIST("17N00017A", "1-5, 10-20"));
        Assert.assertFalse(_functions.GEN_INLIST("ZZ00017A", "1-5, 10-20"));
        Assert.assertTrue(_functions.GEN_INLIST("  20A", "1-5, 10-20"));
        Assert.assertFalse(_functions.GEN_INLIST("& 20A", "1-5, 10-20"));

        Assert.assertFalse(_functions.GEN_INLIST("2A", "2"));
        Assert.assertFalse(_functions.GEN_INLIST("A2", "2"));
        Assert.assertTrue(_functions.GEN_INLIST("57A", "1-3,57B-59B"));
        Assert.assertTrue(_functions.GEN_INLIST("57A", "1-3,57C-59"));
        Assert.assertTrue(_functions.GEN_INLIST("57A", "1-3,57-59B"));
        Assert.assertTrue(_functions.GEN_INLIST("57asasa", "1-3,57U-59B"));
        Assert.assertFalse(_functions.GEN_INLIST("57A", "1-3,57,58-59"));
        Assert.assertTrue(_functions.GEN_INLIST("10F", "A-B, 10"));
        Assert.assertFalse(_functions.GEN_INLIST("10F", "10"));
        Assert.assertTrue(_functions.GEN_INLIST("10F", "10-11"));
        Assert.assertTrue(_functions.GEN_INLIST("10F", "A-<, 10"));
        Assert.assertFalse(_functions.GEN_INLIST("10F", "A-1<, 10"));
        Assert.assertFalse(_functions.GEN_INLIST("10F", "A-1, 10"));
        Assert.assertFalse(_functions.GEN_INLIST("10F", "A, 10"));

        Assert.assertTrue(_functions.GEN_INLIST("10", "A-A"));
        Assert.assertTrue(_functions.GEN_INLIST("10", "1-A"));
        Assert.assertFalse(_functions.GEN_INLIST("10", "A-1"));
        Assert.assertTrue(_functions.GEN_INLIST("1", "A-1"));
        Assert.assertTrue(_functions.GEN_INLIST("0", "A-1"));
        Assert.assertFalse(_functions.GEN_INLIST("-1", "A-1"));
        Assert.assertFalse(_functions.GEN_INLIST("2", "A-1"));
        Assert.assertFalse(_functions.GEN_INLIST("10", "A-1"));
        Assert.assertTrue(_functions.GEN_INLIST("1", "1-A"));
        Assert.assertTrue(_functions.GEN_INLIST("2", "1-A"));
        Assert.assertTrue(_functions.GEN_INLIST("10000", "1-A"));
        Assert.assertFalse(_functions.GEN_INLIST("0", "1-A"));

        Assert.assertTrue(_functions.GEN_INLIST("10", "A1-A1"));
        Assert.assertTrue(_functions.GEN_INLIST("10", "1A-A1"));
        Assert.assertFalse(_functions.GEN_INLIST("10", "A1-1A"));
        Assert.assertTrue(_functions.GEN_INLIST("1", "A1-1A"));
        Assert.assertTrue(_functions.GEN_INLIST("0", "A1-1A"));
        Assert.assertFalse(_functions.GEN_INLIST("-1", "A1-1A"));
        Assert.assertFalse(_functions.GEN_INLIST("2", "A1-1A"));
        Assert.assertFalse(_functions.GEN_INLIST("10", "A1-1A"));
        Assert.assertTrue(_functions.GEN_INLIST("1", "1A-A1"));
        Assert.assertTrue(_functions.GEN_INLIST("2", "1A-A1"));
        Assert.assertTrue(_functions.GEN_INLIST("10000", "1A-A1"));
        Assert.assertFalse(_functions.GEN_INLIST("0", "1A-A1"));

        Assert.assertTrue(_functions.GEN_INLIST("10FF", "A-A"));
        Assert.assertTrue(_functions.GEN_INLIST("10SDFA", "1f-A"));
        Assert.assertFalse(_functions.GEN_INLIST("10asdf1", "A-1dd"));
        Assert.assertTrue(_functions.GEN_INLIST("1ddd", "A-1df"));
        Assert.assertTrue(_functions.GEN_INLIST("0fad", "A-1f"));
        Assert.assertFalse(_functions.GEN_INLIST("-1df", "A1-1"));
        Assert.assertFalse(_functions.GEN_INLIST("2d", "A1-1"));
        Assert.assertFalse(_functions.GEN_INLIST("10f", "A1-1"));
        Assert.assertTrue(_functions.GEN_INLIST("1dsa", "1-A"));
        Assert.assertTrue(_functions.GEN_INLIST("2ddd", "1-A"));
        Assert.assertTrue(_functions.GEN_INLIST("10000ffdf", "1-A"));
        Assert.assertFalse(_functions.GEN_INLIST("0eee", "1-A"));

        // invalid range
        Assert.assertFalse(_functions.GEN_INLIST("10", "10-9"));
    }

    @Test
    public void testGEN_MATCH() {
        Assert.assertFalse(_functions.GEN_MATCH(null, "(\\d\\d\\d)"));
        Assert.assertFalse(_functions.GEN_MATCH("", "(\\d\\d\\d)"));
        Assert.assertFalse(_functions.GEN_MATCH("   ", "(\\d\\d\\d)"));
        Assert.assertFalse(_functions.GEN_MATCH("1234", "(\\d\\d\\d)"));
        Assert.assertTrue(_functions.GEN_MATCH("123", "(\\d\\d\\d)"));

        // it looks like they right-trim the incoming value
        Assert.assertTrue(_functions.GEN_MATCH("123 ", "(\\d\\d\\d)"));
        Assert.assertTrue(_functions.GEN_MATCH("123     ", "(\\d\\d\\d)"));

        // it also ignores any trailing spaces in the regex itself  -  FPD no it doesn't; it ignores single space regex, not trailing spaces!
        Assert.assertTrue(_functions.GEN_MATCH("123", "(\\d\\d\\d\\s)"));
        Assert.assertTrue(_functions.GEN_MATCH("123", "(\\d\\d\\d\\s\\s\\s)"));
        Assert.assertTrue(_functions.GEN_MATCH("123", "\\d\\d\\d\\s\\s\\s"));

        // It appears that an empty string (which means any blank string since they right-trim) matches to any number of spaces...
        Assert.assertTrue(_functions.GEN_MATCH("", "\\s"));
        Assert.assertTrue(_functions.GEN_MATCH("", "\\s\\s"));
        Assert.assertTrue(_functions.GEN_MATCH("", "\\s\\s\\s"));
        Assert.assertTrue(_functions.GEN_MATCH("", "(\\s\\s\\s)"));
        Assert.assertTrue(_functions.GEN_MATCH("", "(\\s)"));
        Assert.assertTrue(_functions.GEN_MATCH("", "(\\s|")); // following are not valid regex, but I am just testing the pre-condition...
        Assert.assertTrue(_functions.GEN_MATCH("", "|\\s)"));
        Assert.assertTrue(_functions.GEN_MATCH("", "|\\s|"));
        Assert.assertTrue(_functions.GEN_MATCH("", "|\\s"));
        Assert.assertTrue(_functions.GEN_MATCH("", "\\s|"));

        // apparently, they deal with an actual space the same way as a 'b' in the regex (which is totally un-documented)
        Assert.assertTrue(_functions.GEN_MATCH("", " "));
        Assert.assertTrue(_functions.GEN_MATCH("", "  "));
        Assert.assertTrue(_functions.GEN_MATCH("", "   "));
        Assert.assertTrue(_functions.GEN_MATCH("", "(   )"));
        Assert.assertTrue(_functions.GEN_MATCH("", "( )"));
        Assert.assertTrue(_functions.GEN_MATCH("", "( |")); // following are not valid regex, but I am just testing the pre-condition...
        Assert.assertTrue(_functions.GEN_MATCH("", "| )"));
        Assert.assertTrue(_functions.GEN_MATCH("", "| |"));
        Assert.assertTrue(_functions.GEN_MATCH("", "| "));
        Assert.assertTrue(_functions.GEN_MATCH("", " |"));

        Assert.assertTrue(_functions.GEN_MATCH("0000", "0000 "));
        Assert.assertTrue(_functions.GEN_MATCH("0000", "(0000 )"));

        Assert.assertTrue(_functions.GEN_MATCH("00  00", "(\\d\\d  \\d\\d)"));

        Assert.assertTrue(_functions.GEN_MATCH("1", "(1)|(\\s)"));
        Assert.assertTrue(_functions.GEN_MATCH(" ", "(1)|(\\s)"));

        // this is from a real edit for street name to make sure it's left justified
        //     original regex: @{?}*
        // following testing values have been tested within Genedits...
        String regex = "([^ \\t\\r\\n\\v\\f]((.))*)";
        Assert.assertTrue(_functions.GEN_MATCH("ABC", regex));
        Assert.assertTrue(_functions.GEN_MATCH("ABC ", regex));
        Assert.assertTrue(_functions.GEN_MATCH("ABC   ", regex));
        Assert.assertTrue(_functions.GEN_MATCH("A B C ", regex));
        Assert.assertFalse(_functions.GEN_MATCH(" ABC", regex));
        Assert.assertFalse(_functions.GEN_MATCH("  ABC", regex));
        Assert.assertFalse(_functions.GEN_MATCH(" ABC ", regex));
        Assert.assertFalse(_functions.GEN_MATCH(" A B C ", regex));
        Assert.assertFalse(_functions.GEN_MATCH("   ", regex));
        Assert.assertFalse(_functions.GEN_MATCH("", regex));

        // this is from a real edit for postal code (Can't be blank, must be alphanumeric, left-justified, and blank-filled. Mixed case is allowed. Special characters are not allowed).
        //     original regex: x{b,x}*
        // following testing values have been tested within Genedits...
        regex = "[A-Za-z0-9](\\s|[A-Za-z0-9])*";
        Assert.assertTrue(_functions.GEN_MATCH("12345", regex));
        Assert.assertTrue(_functions.GEN_MATCH("12345    ", regex));
        Assert.assertTrue(_functions.GEN_MATCH("A1234", regex));
        Assert.assertTrue(_functions.GEN_MATCH("1234Z", regex));
        Assert.assertTrue(_functions.GEN_MATCH("abcABC", regex));
        Assert.assertTrue(_functions.GEN_MATCH("123 456", regex));
        Assert.assertTrue(_functions.GEN_MATCH("1 23456   ", regex));
        Assert.assertFalse(_functions.GEN_MATCH("", regex));
        Assert.assertFalse(_functions.GEN_MATCH(" ", regex));
        Assert.assertFalse(_functions.GEN_MATCH("   ", regex));
        Assert.assertFalse(_functions.GEN_MATCH(" 123456", regex));
        Assert.assertFalse(_functions.GEN_MATCH("1234!", regex));
        Assert.assertFalse(_functions.GEN_MATCH("12;34;56", regex));
        Assert.assertFalse(_functions.GEN_MATCH("<12345>", regex));

        // this one was used for "Addr Current--Postal Code (COC)"
        //     original regex: x{x}*{b}*
        // following testing values have been tested within Genedits...
        regex = "([A-Za-z0-9](([A-Za-z0-9]))*((\\s))*)";
        Assert.assertTrue(_functions.GEN_MATCH("12345", regex));
        Assert.assertTrue(_functions.GEN_MATCH("12345    ", regex));
        Assert.assertTrue(_functions.GEN_MATCH("A1234", regex));
        Assert.assertTrue(_functions.GEN_MATCH("1234Z", regex));
        Assert.assertTrue(_functions.GEN_MATCH("abcABC", regex));
        Assert.assertFalse(_functions.GEN_MATCH("123 456", regex));
        Assert.assertFalse(_functions.GEN_MATCH("1 23456   ", regex));
        Assert.assertFalse(_functions.GEN_MATCH("", regex));
        Assert.assertFalse(_functions.GEN_MATCH(" ", regex));
        Assert.assertFalse(_functions.GEN_MATCH("   ", regex));
        Assert.assertFalse(_functions.GEN_MATCH(" 123456", regex));
        Assert.assertFalse(_functions.GEN_MATCH("1234!", regex));
        Assert.assertFalse(_functions.GEN_MATCH("12;34;56", regex));
        Assert.assertFalse(_functions.GEN_MATCH("<12345>", regex));

        // this one was used for "Name--First (NPCR)"
        //    original regex: a{a,b,-,'}*
        // following testing values have been tested within Genedits...
        regex = "([A-Za-z](([A-Za-z])|(\\s)|(\\-)|('))*)";
        Assert.assertTrue(_functions.GEN_MATCH("abc", regex));
        Assert.assertTrue(_functions.GEN_MATCH("ABC", regex));
        Assert.assertTrue(_functions.GEN_MATCH("A BC", regex));
        Assert.assertTrue(_functions.GEN_MATCH("A'BC", regex));
        Assert.assertTrue(_functions.GEN_MATCH("A-BC", regex));
        Assert.assertTrue(_functions.GEN_MATCH("ABC ", regex));
        Assert.assertFalse(_functions.GEN_MATCH("", regex));
        Assert.assertFalse(_functions.GEN_MATCH(" ", regex));
        Assert.assertFalse(_functions.GEN_MATCH("    ", regex));
        Assert.assertFalse(_functions.GEN_MATCH(" ABC", regex));
        Assert.assertFalse(_functions.GEN_MATCH("'ABC", regex));
        Assert.assertFalse(_functions.GEN_MATCH("AB<C>", regex));
        Assert.assertFalse(_functions.GEN_MATCH("AB_C", regex));

        // this one was taken from "Secondary Diagnosis 1 (COC)"
        //    original regex: uxx{x}*{b}*
        // following testing values have been tested within Genedits...
        regex = "([A-Z][A-Za-z0-9][A-Za-z0-9](([A-Za-z0-9]))*((\\s))*)";
        Assert.assertTrue(_functions.GEN_MATCH("ABC", regex));
        Assert.assertTrue(_functions.GEN_MATCH("Abc", regex));
        Assert.assertTrue(_functions.GEN_MATCH("A12", regex));
        Assert.assertTrue(_functions.GEN_MATCH("Abc ", regex));
        Assert.assertTrue(_functions.GEN_MATCH("Abc    ", regex));
        Assert.assertTrue(_functions.GEN_MATCH("Abc123", regex));
        Assert.assertTrue(_functions.GEN_MATCH("Abc123 ", regex));
        Assert.assertTrue(_functions.GEN_MATCH("X12", regex));
        Assert.assertTrue(_functions.GEN_MATCH("X12 ", regex));
        Assert.assertTrue(_functions.GEN_MATCH("Xab", regex));
        Assert.assertTrue(_functions.GEN_MATCH("Xab ", regex));
        Assert.assertFalse(_functions.GEN_MATCH("abc", regex));
        Assert.assertFalse(_functions.GEN_MATCH("aBC", regex));
        Assert.assertFalse(_functions.GEN_MATCH("123", regex));
        Assert.assertFalse(_functions.GEN_MATCH("a      ", regex));
        Assert.assertFalse(_functions.GEN_MATCH("x", regex));
        Assert.assertFalse(_functions.GEN_MATCH("x      ", regex));
        Assert.assertFalse(_functions.GEN_MATCH("1", regex));
        Assert.assertFalse(_functions.GEN_MATCH("1      ", regex));
        Assert.assertFalse(_functions.GEN_MATCH("A", regex));
        Assert.assertFalse(_functions.GEN_MATCH("A1", regex));
        Assert.assertFalse(_functions.GEN_MATCH("Aa", regex));
        Assert.assertFalse(_functions.GEN_MATCH("A ", regex));
        Assert.assertFalse(_functions.GEN_MATCH("A  ", regex));
        Assert.assertFalse(_functions.GEN_MATCH("ABC'", regex));
        Assert.assertFalse(_functions.GEN_MATCH("ABC-2", regex));
        Assert.assertFalse(_functions.GEN_MATCH("ABC abc", regex));

        // this one was also taken from "Secondary Diagnosis 1 (COC)"
        //    original regex: {A,B,E,G:P,R,S}xx{x}*{b}*
        // following testing values have been tested within Genedits...
        regex = "(((A)|(B)|(E)|([G-P])|(R)|(S))[A-Za-z0-9][A-Za-z0-9](([A-Za-z0-9]))*((\\s))*)";
        Assert.assertTrue(_functions.GEN_MATCH("A12", regex));
        Assert.assertTrue(_functions.GEN_MATCH("B12", regex));
        Assert.assertTrue(_functions.GEN_MATCH("G12", regex));
        Assert.assertTrue(_functions.GEN_MATCH("I12", regex));
        Assert.assertTrue(_functions.GEN_MATCH("R12", regex));
        Assert.assertTrue(_functions.GEN_MATCH("S12", regex));
        Assert.assertTrue(_functions.GEN_MATCH("Sab", regex));
        Assert.assertTrue(_functions.GEN_MATCH("SAB", regex));
        Assert.assertTrue(_functions.GEN_MATCH("SAB  ", regex));
        Assert.assertTrue(_functions.GEN_MATCH("SAB123", regex));
        Assert.assertTrue(_functions.GEN_MATCH("SAB123 ", regex));
        Assert.assertTrue(_functions.GEN_MATCH("SABabc", regex));
        Assert.assertTrue(_functions.GEN_MATCH("SABabc ", regex));
        Assert.assertTrue(_functions.GEN_MATCH("SAB", regex));
        Assert.assertFalse(_functions.GEN_MATCH("A", regex));
        Assert.assertFalse(_functions.GEN_MATCH("Aa", regex));
        Assert.assertFalse(_functions.GEN_MATCH("Aa ", regex));
        Assert.assertFalse(_functions.GEN_MATCH("A a", regex));
        Assert.assertFalse(_functions.GEN_MATCH("A1", regex));
        Assert.assertFalse(_functions.GEN_MATCH("A1 ", regex));
        Assert.assertFalse(_functions.GEN_MATCH("A 1", regex));
        Assert.assertFalse(_functions.GEN_MATCH("abc", regex));
        Assert.assertFalse(_functions.GEN_MATCH("a12", regex));
        Assert.assertFalse(_functions.GEN_MATCH("a12 ", regex));
        Assert.assertFalse(_functions.GEN_MATCH("C12", regex));
        Assert.assertFalse(_functions.GEN_MATCH("Cab", regex));
        Assert.assertFalse(_functions.GEN_MATCH("C1", regex));
        Assert.assertFalse(_functions.GEN_MATCH("C1  ", regex));
        Assert.assertFalse(_functions.GEN_MATCH("123", regex));
        Assert.assertFalse(_functions.GEN_MATCH("123 ", regex));

        // this one was taken from "Edit Over-rides (SEER REVIEWFL)" in NAACCR Call for Data metafile
        //    original regex: 1,b
        // following testing values have been tested within Genedits...
        regex = "(1)|(\\s)";
        Assert.assertTrue(_functions.GEN_MATCH("", regex));
        Assert.assertTrue(_functions.GEN_MATCH(" ", regex));
        Assert.assertTrue(_functions.GEN_MATCH("1", regex));
        Assert.assertFalse(_functions.GEN_MATCH("2", regex));

        // this one was also taken from "Edit Over-rides (SEER REVIEWFL)" in NAACCR Call for Data metafile
        //    original regex: 1:3,b
        // following testing values have been tested within Genedits...
        regex = "([1-3])|(\\s)";
        Assert.assertTrue(_functions.GEN_MATCH("", regex));
        Assert.assertTrue(_functions.GEN_MATCH(" ", regex));
        Assert.assertTrue(_functions.GEN_MATCH("1", regex));
        Assert.assertTrue(_functions.GEN_MATCH("2", regex));
        Assert.assertFalse(_functions.GEN_MATCH("4", regex));

        // this one was taken from "EOD--Old 4 digit (SEER IF264DIG_P1)"
        //    original regex: [bb,dd]{b,d}{b,d}
        // following testing values have been tested within Genedits...
        regex = "(((\\s\\s)|(\\d\\d))?((\\s)|(\\d))((\\s)|(\\d)))"; // note that the translation seems wrong; there shouldn't be a question mark!
        Assert.assertTrue(_functions.GEN_MATCH("0000", regex));
        Assert.assertTrue(_functions.GEN_MATCH("000 ", regex));
        Assert.assertTrue(_functions.GEN_MATCH("00  ", regex));
        Assert.assertFalse(_functions.GEN_MATCH("0   ", regex));
        Assert.assertFalse(_functions.GEN_MATCH(" 000", regex));
        Assert.assertTrue(_functions.GEN_MATCH("  00", regex));
        Assert.assertTrue(_functions.GEN_MATCH("   0", regex));
        Assert.assertTrue(_functions.GEN_MATCH("000", regex));
        Assert.assertTrue(_functions.GEN_MATCH("00", regex));
        Assert.assertFalse(_functions.GEN_MATCH("0", regex));
        Assert.assertTrue(_functions.GEN_MATCH("00 ", regex));
        Assert.assertFalse(_functions.GEN_MATCH(" 00", regex));
        Assert.assertFalse(_functions.GEN_MATCH("0 ", regex));
        Assert.assertFalse(_functions.GEN_MATCH("0  ", regex));
        Assert.assertFalse(_functions.GEN_MATCH(" 0", regex));
        Assert.assertTrue(_functions.GEN_MATCH("  0", regex));
    }

    @Test
    public void testGEN_EMPTY() {
        char[] empty = new char[10];
        _functions.GEN_STRCPY(empty, "  ");
        Assert.assertTrue(_functions.GEN_EMPTY(empty));

        empty = new char[10];
        _functions.GEN_STRCPY(empty, "  .");
        Assert.assertFalse(_functions.GEN_EMPTY(empty));

        empty = new char[10];
        _functions.GEN_STRCPY(empty, "");
        Assert.assertTrue(_functions.GEN_EMPTY(empty));
    }

    @Test
    public void testGEN_SUBSTR() {

        // special conditions
        Assert.assertEquals("", _functions.GEN_TO_STRING(_functions.GEN_SUBSTR(null, 1, 1)));
        Assert.assertEquals("", _functions.GEN_TO_STRING(_functions.GEN_SUBSTR("", 1, 1)));

        // examples from the Genedits documentation
        char[] str1 = new char[30];
        _functions.GEN_STRCPY(str1, "Hello World ");
        Assert.assertEquals(" World ", _functions.GEN_TO_STRING(_functions.GEN_SUBSTR(str1, 6, 12)));
        Assert.assertEquals("Hello", _functions.GEN_TO_STRING(_functions.GEN_SUBSTR(str1, 1, 5)));
        Assert.assertEquals("o W", _functions.GEN_TO_STRING(_functions.GEN_SUBSTR(str1, 5, 3)));

        // examples from a real edit
        Assert.assertEquals("8000", _functions.GEN_TO_STRING(_functions.GEN_SUBSTR("80003", 1, 4)));
        Assert.assertEquals("8", _functions.GEN_TO_STRING(_functions.GEN_SUBSTR("8", 1, 4)));
        Assert.assertEquals("80", _functions.GEN_TO_STRING(_functions.GEN_SUBSTR("80", 1, 4)));

        // if no length is provided, value should be right-trimmed
        Assert.assertEquals(" World", _functions.GEN_TO_STRING(_functions.GEN_SUBSTR(str1, 6)));
        Assert.assertEquals("Hello World", _functions.GEN_TO_STRING(_functions.GEN_SUBSTR(str1, 1)));
        Assert.assertEquals("o World", _functions.GEN_TO_STRING(_functions.GEN_SUBSTR(str1, 5)));
    }

    @Test
    public void testGEN_STRCPY() {

        // special conditions
        char[] array = new char[10];
        _functions.GEN_STRCPY(array, null);
        Assert.assertEquals("", _functions.GEN_TO_STRING(array));
        array = new char[10];
        _functions.GEN_STRCPY(array, " ");
        Assert.assertEquals(" ", _functions.GEN_TO_STRING(array));
        array = new char[10];
        _functions.GEN_STRCPY(array, "ABC");
        Assert.assertEquals("ABC", _functions.GEN_TO_STRING(array));
        array = new char[10];
        _functions.GEN_STRCPY(array, new char[] {'A', 'B', 'C'});
        Assert.assertEquals("ABC", _functions.GEN_TO_STRING(array));
        array = new char[10];
        _functions.GEN_STRCPY(array, "ABC", 1);
        Assert.assertEquals("A", _functions.GEN_TO_STRING(array));
        array = new char[10];
        _functions.GEN_STRCPY(array, "ABC", 4);
        Assert.assertEquals("ABC", _functions.GEN_TO_STRING(array));
        array = new char[10];
        _functions.GEN_STRCPY(array, "ABC", -4);
        Assert.assertEquals("ABC ", _functions.GEN_TO_STRING(array));

        // examples from the Genedits documentation
        char[] dest = new char[20], source = new char[20];
        _functions.GEN_STRCPY(source, "Hello, World");
        _functions.GEN_STRCPY(dest, source);
        Assert.assertEquals("Hello, World", _functions.GEN_TO_STRING(dest)); /* dest gets "Hello, World"    */
        _functions.GEN_STRCPY(dest, source, 10);
        Assert.assertEquals("Hello, Wor", _functions.GEN_TO_STRING(dest)); /* dest gets "Hello, Wor"      */
        _functions.GEN_STRCPY(dest, source, 15);
        Assert.assertEquals("Hello, World", _functions.GEN_TO_STRING(dest)); /* dest gets "Hello, World"    */
        _functions.GEN_STRCPY(dest, source, -15);
        Assert.assertEquals("Hello, World   ", _functions.GEN_TO_STRING(dest)); /* dest gets "Hello, World   " */
    }

    @Test
    public void testGEN_STRCAT() {

        char[] array = new char[20];
        _functions.GEN_STRCAT(array, null);
        Assert.assertEquals("", _functions.GEN_TO_STRING(array));
        _functions.GEN_STRCAT(array, " ");
        Assert.assertEquals(" ", _functions.GEN_TO_STRING(array));
        _functions.GEN_STRCAT(array, "ABC");
        Assert.assertEquals(" ABC", _functions.GEN_TO_STRING(array));
        _functions.GEN_STRCAT(array, new char[] {'A', 'B', 'C'});
        Assert.assertEquals(" ABCABC", _functions.GEN_TO_STRING(array));
        _functions.GEN_STRCAT(array, "ABC", 1);
        Assert.assertEquals(" ABCABCA", _functions.GEN_TO_STRING(array));
        _functions.GEN_STRCAT(array, "ABC", 4);
        Assert.assertEquals(" ABCABCAABC", _functions.GEN_TO_STRING(array));
        _functions.GEN_STRCAT(array, "ABC", -4);
        Assert.assertEquals(" ABCABCAABC", _functions.GEN_TO_STRING(array));

        array = new char[5];
        array[0] = 'A';
        array[1] = '\0';
        _functions.GEN_STRCAT(array, "ABCD");
        Assert.assertEquals("AABC", _functions.GEN_TO_STRING(array));
    }

    @Test
    public void testGEN_STRCMP() {

        // test no length param
        Assert.assertEquals(-1, _functions.GEN_STRCMP(null, null));
        Assert.assertEquals(-1, _functions.GEN_STRCMP(null, ""));
        Assert.assertEquals(-1, _functions.GEN_STRCMP("", null));
        Assert.assertEquals(0, _functions.GEN_STRCMP("", ""));
        Assert.assertEquals(-1, _functions.GEN_STRCMP("    ", "          "));
        Assert.assertEquals(-1, _functions.GEN_STRCMP("A", "AB"));
        Assert.assertEquals(1, _functions.GEN_STRCMP("AB", "A"));
        Assert.assertEquals(-1, _functions.GEN_STRCMP("AB", "a"));
        Assert.assertEquals(0, _functions.GEN_STRCMP("ABC", "ABC"));
        Assert.assertEquals(1, _functions.GEN_STRCMP("abc", "ABC"));
        Assert.assertEquals(1, _functions.GEN_STRCMP("ABC", "AB"));

        // test the length param
        Assert.assertEquals(-1, _functions.GEN_STRCMP(null, null, 1));
        Assert.assertEquals(-1, _functions.GEN_STRCMP(null, "", 1));
        Assert.assertEquals(-1, _functions.GEN_STRCMP("", null, 1));
        Assert.assertEquals(0, _functions.GEN_STRCMP("", "", 1));
        Assert.assertEquals(0, _functions.GEN_STRCMP("    ", "          ", 1));
        Assert.assertEquals(0, _functions.GEN_STRCMP("A", "AB", 1));
        Assert.assertEquals(0, _functions.GEN_STRCMP("AB", "A", 1));
        Assert.assertEquals(-1, _functions.GEN_STRCMP("AB", "a", 1));
        Assert.assertEquals(0, _functions.GEN_STRCMP("ABC", "ABC"), 1);
        Assert.assertEquals(0, _functions.GEN_STRCMP("ABC", "ABC"), 2);
        Assert.assertEquals(0, _functions.GEN_STRCMP("ABC", "ABC"), 3);
        Assert.assertEquals(0, _functions.GEN_STRCMP("ABC", "ABC"), 4);
        Assert.assertEquals(1, _functions.GEN_STRCMP("abc", "ABC", 1));
        Assert.assertEquals(0, _functions.GEN_STRCMP("ABC", "AB", 1));

        // this example is taken from the Genedits documentation...
        char[] t_state = new char[3], t_state_zip = new char[8];
        _functions.GEN_STRCPY(t_state, "GA");
        _functions.GEN_STRCPY(t_state_zip, "GA");
        _functions.GEN_STRCAT(t_state_zip, "30341");
        Assert.assertEquals(0, _functions.GEN_STRCMP(t_state, "GA"));
        Assert.assertEquals(0, _functions.GEN_STRCMP(t_state_zip, "GA", 2));

    }

    @Test
    public void testGEN_FMTSTR() {
        char[] val = new char[10];
        _functions.GEN_FMTSTR(val, "%-5ld", 10);
        Assert.assertEquals(_functions.GEN_TO_STRING(val), "10   ");
    }

    @Test
    public void testGEN_VALID_DATE_IOP() {
        //get today's date
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate nextMonth = today.plusMonths(1);
        LocalDate nextYear = today.plusYears(1);

        DateTimeFormatter yearMonthDayFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        Binding binding = new Binding();
        _functions.GEN_RESET_LOCAL_CONTEXT(binding);

        Assert.assertTrue(_functions.GEN_VALID_DATE_IOP(binding, "20110201"));
        Assert.assertTrue(_functions.GEN_VALID_DATE_IOP(binding, "18500101"));
        Assert.assertFalse(_functions.GEN_VALID_DATE_IOP(binding, "21110201"));
        Assert.assertFalse(_functions.GEN_VALID_DATE_IOP(binding, "18490201"));
        Assert.assertFalse(_functions.GEN_VALID_DATE_IOP(binding, "        "));
        Assert.assertTrue(_functions.GEN_VALID_DATE_IOP(binding, "185001  "));
        Assert.assertTrue(_functions.GEN_VALID_DATE_IOP(binding, "1850    "));
        Assert.assertTrue(_functions.GEN_VALID_DATE_IOP(binding, "2000    "));
        Assert.assertFalse(_functions.GEN_VALID_DATE_IOP(binding, "yyyyMMdd"));
        Assert.assertFalse(_functions.GEN_VALID_DATE_IOP(binding, "20100230"));
        Assert.assertFalse(_functions.GEN_VALID_DATE_IOP(binding, "20100229"));
        Assert.assertTrue(_functions.GEN_VALID_DATE_IOP(binding, "20120229"));

        //today
        Assert.assertTrue(_functions.GEN_VALID_DATE_IOP(binding, today.format(yearMonthDayFormatter)));
        //next month
        Assert.assertFalse(_functions.GEN_VALID_DATE_IOP(binding, nextMonth.format(yearMonthDayFormatter)));
        //tomorrow
        Assert.assertFalse(_functions.GEN_VALID_DATE_IOP(binding, tomorrow.format(yearMonthDayFormatter)));
        //next year
        Assert.assertFalse(_functions.GEN_VALID_DATE_IOP(binding, nextYear.format(yearMonthDayFormatter)));

        Assert.assertTrue(_functions.GEN_VALID_DATE_IOP(binding, "201310  "));
        Assert.assertFalse(_functions.GEN_VALID_DATE_IOP(binding, nextYear.format(yearMonthDayFormatter).substring(0, 4) + "11  "));
        Assert.assertFalse(_functions.GEN_VALID_DATE_IOP(binding, "2013  01"));
        Assert.assertTrue(_functions.GEN_VALID_DATE_IOP(binding, "2013    "));
        Assert.assertFalse(_functions.GEN_VALID_DATE_IOP(binding, nextYear.format(yearMonthDayFormatter).substring(0, 4) + "    "));
        Assert.assertFalse(_functions.GEN_VALID_DATE_IOP(binding, "201010AA"));
        Assert.assertFalse(_functions.GEN_VALID_DATE_IOP(binding, "201010-1"));

        _functions.GEN_ALLOW_FUTURE_DATE_IOP(binding, 0);
        Assert.assertFalse(_functions.GEN_VALID_DATE_IOP(binding, nextYear.format(yearMonthDayFormatter).substring(0, 4) + "    "));
        Assert.assertFalse(_functions.GEN_VALID_DATE_IOP(binding, tomorrow.format(yearMonthDayFormatter)));

        _functions.GEN_ALLOW_FUTURE_DATE_IOP(binding, 21);
        Assert.assertTrue(_functions.GEN_VALID_DATE_IOP(binding, "20120202"));
        Assert.assertTrue(_functions.GEN_VALID_DATE_IOP(binding, "20130202"));
        Assert.assertFalse(_functions.GEN_VALID_DATE_IOP(binding, "20400202"));
    }

    @Test
    public void testGEN_DATE_YEAR_IOP() {
        Binding binding = new Binding();
        _functions.GEN_RESET_LOCAL_CONTEXT(binding);

        Assert.assertEquals(MetafileContextFunctions.DT_ERROR, _functions.GEN_DATE_YEAR_IOP(binding, "yyyyMMdd"));
    }

    @Test
    public void testGEN_DATE_DAY_IOP() {
        Binding binding = new Binding();
        _functions.GEN_RESET_LOCAL_CONTEXT(binding);

        Assert.assertEquals(MetafileContextFunctions.DT_DAY_EMPTY, _functions.GEN_DATE_DAY_IOP(binding, "201108  "));
        Assert.assertEquals(MetafileContextFunctions.DT_DAY_EMPTY, _functions.GEN_DATE_DAY_IOP(binding, "201108"));
        Assert.assertEquals(MetafileContextFunctions.DT_DAY_EMPTY, _functions.GEN_DATE_DAY_IOP(binding, "201108 "));
        Assert.assertEquals(1, _functions.GEN_DATE_DAY_IOP(binding, "20110801"));
        Assert.assertEquals(11, _functions.GEN_DATE_DAY_IOP(binding, "20110811"));
    }

    @Test
    public void testGEN_DATE_MONTH_IOP() {
        Binding binding = new Binding();
        _functions.GEN_RESET_LOCAL_CONTEXT(binding);

        Assert.assertEquals(MetafileContextFunctions.DT_MONTH_EMPTY, _functions.GEN_DATE_MONTH_IOP(binding, "2011    "));
        Assert.assertEquals(MetafileContextFunctions.DT_MONTH_EMPTY, _functions.GEN_DATE_MONTH_IOP(binding, "2011  "));
        Assert.assertEquals(MetafileContextFunctions.DT_MONTH_EMPTY, _functions.GEN_DATE_MONTH_IOP(binding, "2011"));
        Assert.assertEquals(MetafileContextFunctions.DT_ERROR, _functions.GEN_DATE_MONTH_IOP(binding, "2011 "));
        Assert.assertEquals(MetafileContextFunctions.DT_MONTH_EMPTY, _functions.GEN_DATE_MONTH_IOP(binding, "2011  01"));
        Assert.assertEquals(8, _functions.GEN_DATE_MONTH_IOP(binding, "20110821"));
        Assert.assertEquals(11, _functions.GEN_DATE_MONTH_IOP(binding, "20101120"));
    }

    @Test
    public void testGEN_DATECMP_IOP() {
        Binding binding = new Binding();
        _functions.GEN_RESET_LOCAL_CONTEXT(binding);

        // full date comparisons (using first param)
        Assert.assertEquals(0, _functions.GEN_DATECMP_IOP(binding, "20000615", "20000615", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(1, _functions.GEN_DATECMP_IOP(binding, "20010615", "20000615", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(1, _functions.GEN_DATECMP_IOP(binding, "20000715", "20000615", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(1, _functions.GEN_DATECMP_IOP(binding, "20000616", "20000615", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(-1, _functions.GEN_DATECMP_IOP(binding, "19990615", "20000615", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(-1, _functions.GEN_DATECMP_IOP(binding, "20000515", "20000615", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(-1, _functions.GEN_DATECMP_IOP(binding, "20000614", "20000615", MetafileContextFunctions.DT_EXACT));
        // full date comparisons (using second param)
        Assert.assertEquals(0, _functions.GEN_DATECMP_IOP(binding, "20000615", "20000615", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(-1, _functions.GEN_DATECMP_IOP(binding, "20000615", "20010615", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(-1, _functions.GEN_DATECMP_IOP(binding, "20000615", "20000715", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(-1, _functions.GEN_DATECMP_IOP(binding, "20000615", "20000616", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(1, _functions.GEN_DATECMP_IOP(binding, "20000615", "19990615", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(1, _functions.GEN_DATECMP_IOP(binding, "20000615", "20000515", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(1, _functions.GEN_DATECMP_IOP(binding, "20000615", "20000614", MetafileContextFunctions.DT_EXACT));

        // invalid dates (using first param)
        Assert.assertEquals(MetafileContextFunctions.DT_ERROR, _functions.GEN_DATECMP_IOP(binding, "abc", "20000615", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(MetafileContextFunctions.DT_ERROR, _functions.GEN_DATECMP_IOP(binding, "00000000", "20000615", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(MetafileContextFunctions.DT_ERROR, _functions.GEN_DATECMP_IOP(binding, "99999999", "20000615", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(MetafileContextFunctions.DT_ERROR, _functions.GEN_DATECMP_IOP(binding, "2000  15", "20000615", MetafileContextFunctions.DT_EXACT));
        // invalid dates (using second param)
        Assert.assertEquals(MetafileContextFunctions.DT_ERROR, _functions.GEN_DATECMP_IOP(binding, "20000615", "abc", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(MetafileContextFunctions.DT_ERROR, _functions.GEN_DATECMP_IOP(binding, "20000615", "00000000", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(MetafileContextFunctions.DT_ERROR, _functions.GEN_DATECMP_IOP(binding, "20000615", "99999999", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(MetafileContextFunctions.DT_ERROR, _functions.GEN_DATECMP_IOP(binding, "20000615", "2000  15", MetafileContextFunctions.DT_EXACT));

        // blank dates (using first param)
        Assert.assertEquals(MetafileContextFunctions.DT_EMPTY, _functions.GEN_DATECMP_IOP(binding, "        ", "20000615", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(MetafileContextFunctions.DT_EMPTY, _functions.GEN_DATECMP_IOP(binding, "        ", "20000615", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(MetafileContextFunctions.DT_EMPTY, _functions.GEN_DATECMP_IOP(binding, "        ", "20000615", MetafileContextFunctions.DT_MAX));
        // blank dates (using second param)
        Assert.assertEquals(MetafileContextFunctions.DT_EMPTY, _functions.GEN_DATECMP_IOP(binding, "20000615", "        ", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(MetafileContextFunctions.DT_EMPTY, _functions.GEN_DATECMP_IOP(binding, "20000615", "        ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(MetafileContextFunctions.DT_EMPTY, _functions.GEN_DATECMP_IOP(binding, "20000615", "        ", MetafileContextFunctions.DT_MAX));

        // blank day (using first param)
        Assert.assertEquals(MetafileContextFunctions.DT_UNKNOWN, _functions.GEN_DATECMP_IOP(binding, "200006  ", "20000615", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(0, _functions.GEN_DATECMP_IOP(binding, "200006  ", "20000615", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(-1, _functions.GEN_DATECMP_IOP(binding, "200006  ", "20000615", MetafileContextFunctions.DT_MAX));

        // blank day (using second param)
        Assert.assertEquals(MetafileContextFunctions.DT_UNKNOWN, _functions.GEN_DATECMP_IOP(binding, "20000615", "200006  ", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(0, _functions.GEN_DATECMP_IOP(binding, "20000615", "200006  ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(-1, _functions.GEN_DATECMP_IOP(binding, "20000515", "200006  ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(1, _functions.GEN_DATECMP_IOP(binding, "20000715", "200006  ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(-1, _functions.GEN_DATECMP_IOP(binding, "20000615", "200006  ", MetafileContextFunctions.DT_MAX));

        // blank month (using first param)
        Assert.assertEquals(MetafileContextFunctions.DT_UNKNOWN, _functions.GEN_DATECMP_IOP(binding, "2000    ", "20000615", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(0, _functions.GEN_DATECMP_IOP(binding, "2000    ", "20000615", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(-1, _functions.GEN_DATECMP_IOP(binding, "2000    ", "20000615", MetafileContextFunctions.DT_MAX));

        // blank month (using second param)
        Assert.assertEquals(MetafileContextFunctions.DT_UNKNOWN, _functions.GEN_DATECMP_IOP(binding, "20000615", "2000    ", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(0, _functions.GEN_DATECMP_IOP(binding, "20000615", "2000    ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(-1, _functions.GEN_DATECMP_IOP(binding, "19990615", "2000    ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(1, _functions.GEN_DATECMP_IOP(binding, "20010615", "2000    ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(-1, _functions.GEN_DATECMP_IOP(binding, "20000615", "2000    ", MetafileContextFunctions.DT_MAX));

        //some more tests with blanks to test the new logic I added that fits better with the real genedits implementation and not their documentation
        String date1 = "20100606";
        String date2 = "2010    ";
        String date3 = "201006  ";

        Assert.assertEquals(0, _functions.GEN_DATECMP_IOP(binding, date2, date1, MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(-1, _functions.GEN_DATECMP_IOP(binding, date2, date1, MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(MetafileContextFunctions.DT_UNKNOWN, _functions.GEN_DATECMP_IOP(binding, date2, date1, MetafileContextFunctions.DT_EXACT));

        Assert.assertEquals(0, _functions.GEN_DATECMP_IOP(binding, date3, date1, MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(-1, _functions.GEN_DATECMP_IOP(binding, date3, date1, MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(MetafileContextFunctions.DT_UNKNOWN, _functions.GEN_DATECMP_IOP(binding, date3, date1, MetafileContextFunctions.DT_EXACT));

        Assert.assertEquals(0, _functions.GEN_DATECMP_IOP(binding, date1, date1, MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(0, _functions.GEN_DATECMP_IOP(binding, date1, date1, MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(0, _functions.GEN_DATECMP_IOP(binding, date1, date1, MetafileContextFunctions.DT_EXACT));

        Assert.assertEquals(0, _functions.GEN_DATECMP_IOP(binding, date2, date3, MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(-1, _functions.GEN_DATECMP_IOP(binding, date2, date3, MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(MetafileContextFunctions.DT_UNKNOWN, _functions.GEN_DATECMP_IOP(binding, date2, date3, MetafileContextFunctions.DT_EXACT));

        Assert.assertEquals(0, _functions.GEN_DATECMP_IOP(binding, date3, date2, MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(-1, _functions.GEN_DATECMP_IOP(binding, date3, date2, MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(MetafileContextFunctions.DT_UNKNOWN, _functions.GEN_DATECMP_IOP(binding, date3, date2, MetafileContextFunctions.DT_EXACT));

        Assert.assertEquals(-1, _functions.GEN_DATECMP_IOP(binding, "2009    ", date2, MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(1, _functions.GEN_DATECMP_IOP(binding, date3, "2009    ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(1, _functions.GEN_DATECMP_IOP(binding, "201007  ", "201006  ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(1, _functions.GEN_DATECMP_IOP(binding, "201007  ", "201006  ", MetafileContextFunctions.DT_MAX));
    }

    @Test
    public void testGEN_DAYDIFF_IOP() {
        Binding binding = new Binding();
        _functions.GEN_RESET_LOCAL_CONTEXT(binding);

        Assert.assertEquals(90, _functions.GEN_DAYDIFF_IOP(binding, "20090101", "20090401", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(MetafileContextFunctions.DT_EMPTY, _functions.GEN_DAYDIFF_IOP(binding, "20000101", "        ", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(MetafileContextFunctions.DT_EMPTY, _functions.GEN_DAYDIFF_IOP(binding, "        ", "20100101", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(3653, _functions.GEN_DAYDIFF_IOP(binding, "20000101", "20100101", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(3653, _functions.GEN_DAYDIFF_IOP(binding, "20000101", "20100101", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(3653, _functions.GEN_DAYDIFF_IOP(binding, "20000101", "20100101", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(3683, _functions.GEN_DAYDIFF_IOP(binding, "20000101", "201001  ", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(MetafileContextFunctions.DT_UNKNOWN, _functions.GEN_DAYDIFF_IOP(binding, "20000101", "2010    ", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(4017, _functions.GEN_DAYDIFF_IOP(binding, "20000101", "2010    ", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(574, _functions.GEN_DAYDIFF_IOP(binding, "20080606", "2010    ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(0, _functions.GEN_DAYDIFF_IOP(binding, "2008    ", "20080606", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(390, _functions.GEN_DAYDIFF_IOP(binding, "20080606", "20090701", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(1, _functions.GEN_DAYDIFF_IOP(binding, "20080131", "20080201", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(1, _functions.GEN_DAYDIFF_IOP(binding, "20080131", "20080201", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(395, _functions.GEN_DAYDIFF_IOP(binding, "200806  ", "20090701", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(389, _functions.GEN_DAYDIFF_IOP(binding, "20080606", "200906  ", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(MetafileContextFunctions.DT_UNKNOWN, _functions.GEN_DAYDIFF_IOP(binding, "20000101", "201001  ", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(730, _functions.GEN_DAYDIFF_IOP(binding, "2008    ", "2009    ", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(390, _functions.GEN_DAYDIFF_IOP(binding, "20080606", "20090701", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(-389, _functions.GEN_DAYDIFF_IOP(binding, "20090630", "20080606", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(-390, _functions.GEN_DAYDIFF_IOP(binding, "20090701", "20080606", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(-1, _functions.GEN_DAYDIFF_IOP(binding, "20080201", "20080131", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(-389, _functions.GEN_DAYDIFF_IOP(binding, "20090630", "20080606", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(-390, _functions.GEN_DAYDIFF_IOP(binding, "20090701", "20080606", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(-1, _functions.GEN_DAYDIFF_IOP(binding, "20080201", "20080131", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(-389, _functions.GEN_DAYDIFF_IOP(binding, "20090630", "20080606", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(-390, _functions.GEN_DAYDIFF_IOP(binding, "20090701", "20080606", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(-1, _functions.GEN_DAYDIFF_IOP(binding, "20080201", "20080131", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(-389, _functions.GEN_DAYDIFF_IOP(binding, "20090630", "20080606", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(-1, _functions.GEN_DAYDIFF_IOP(binding, "20080201", "20080131", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(335, _functions.GEN_DAYDIFF_IOP(binding, "20080131", "2008    ", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(365, _functions.GEN_DAYDIFF_IOP(binding, "2008    ", "2008    ", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(MetafileContextFunctions.DT_UNKNOWN, _functions.GEN_DAYDIFF_IOP(binding, "2008    ", "2008    ", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(MetafileContextFunctions.DT_UNKNOWN, _functions.GEN_DAYDIFF_IOP(binding, "2008    ", "2009    ", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(MetafileContextFunctions.DT_UNKNOWN, _functions.GEN_DAYDIFF_IOP(binding, "2009    ", "2008    ", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(389, _functions.GEN_DAYDIFF_IOP(binding, "20080606", "20090630", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(3653, _functions.GEN_DAYDIFF_IOP(binding, "20000101", "201001  ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(3653, _functions.GEN_DAYDIFF_IOP(binding, "20000101", "2010    ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(366, _functions.GEN_DAYDIFF_IOP(binding, "200806  ", "20090701", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(366, _functions.GEN_DAYDIFF_IOP(binding, "200806  ", "200907  ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(185, _functions.GEN_DAYDIFF_IOP(binding, "200806  ", "2009    ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(91, _functions.GEN_DAYDIFF_IOP(binding, "2008    ", "20090401", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(152, _functions.GEN_DAYDIFF_IOP(binding, "2008    ", "200906  ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(1, _functions.GEN_DAYDIFF_IOP(binding, "2008    ", "2009    ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(389, _functions.GEN_DAYDIFF_IOP(binding, "20080606", "20090630", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(3683, _functions.GEN_DAYDIFF_IOP(binding, "20000101", "201001  ", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(4017, _functions.GEN_DAYDIFF_IOP(binding, "20000101", "2010    ", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(395, _functions.GEN_DAYDIFF_IOP(binding, "200806  ", "20090701", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(425, _functions.GEN_DAYDIFF_IOP(binding, "200806  ", "200907  ", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(578, _functions.GEN_DAYDIFF_IOP(binding, "200806  ", "2009    ", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(456, _functions.GEN_DAYDIFF_IOP(binding, "2008    ", "20090401", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(546, _functions.GEN_DAYDIFF_IOP(binding, "2008    ", "200906  ", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(730, _functions.GEN_DAYDIFF_IOP(binding, "2008    ", "2009    ", MetafileContextFunctions.DT_MAX));

        Assert.assertEquals(1, _functions.GEN_DAYDIFF_IOP(binding, "20080131", "20080201", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(0, _functions.GEN_DAYDIFF_IOP(binding, "20080606", "200806  ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(0, _functions.GEN_DAYDIFF_IOP(binding, "20080131", "2008    ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(-122, _functions.GEN_DAYDIFF_IOP(binding, "200806  ", "20080131", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(1, _functions.GEN_DAYDIFF_IOP(binding, "200806  ", "200807  ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(0, _functions.GEN_DAYDIFF_IOP(binding, "200806  ", "2008    ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(0, _functions.GEN_DAYDIFF_IOP(binding, "2008    ", "20080131", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(0, _functions.GEN_DAYDIFF_IOP(binding, "2008    ", "200801  ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(0, _functions.GEN_DAYDIFF_IOP(binding, "2008    ", "2008    ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(1, _functions.GEN_DAYDIFF_IOP(binding, "20080131", "20080201", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(24, _functions.GEN_DAYDIFF_IOP(binding, "20080606", "200806  ", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(335, _functions.GEN_DAYDIFF_IOP(binding, "20080131", "2008    ", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(-122, _functions.GEN_DAYDIFF_IOP(binding, "200806  ", "20080131", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(60, _functions.GEN_DAYDIFF_IOP(binding, "200806  ", "200807  ", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(213, _functions.GEN_DAYDIFF_IOP(binding, "200806  ", "2008    ", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(30, _functions.GEN_DAYDIFF_IOP(binding, "2008    ", "20080131", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(30, _functions.GEN_DAYDIFF_IOP(binding, "2008    ", "200801  ", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(365, _functions.GEN_DAYDIFF_IOP(binding, "2008    ", "2008    ", MetafileContextFunctions.DT_MAX));

        Assert.assertEquals(-390, _functions.GEN_DAYDIFF_IOP(binding, "20090701", "20080606", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(-366, _functions.GEN_DAYDIFF_IOP(binding, "20090701", "200806  ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(-182, _functions.GEN_DAYDIFF_IOP(binding, "20090701", "2008    ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(-605, _functions.GEN_DAYDIFF_IOP(binding, "201002  ", "20080606", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(-731, _functions.GEN_DAYDIFF_IOP(binding, "201007  ", "200806  ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(-639, _functions.GEN_DAYDIFF_IOP(binding, "201010  ", "2008    ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(-574, _functions.GEN_DAYDIFF_IOP(binding, "2010    ", "20080606", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(-550, _functions.GEN_DAYDIFF_IOP(binding, "2010    ", "200806  ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(-1, _functions.GEN_DAYDIFF_IOP(binding, "2009    ", "2008    ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(-390, _functions.GEN_DAYDIFF_IOP(binding, "20090701", "20080606", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(-366, _functions.GEN_DAYDIFF_IOP(binding, "20090701", "200806  ", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(-182, _functions.GEN_DAYDIFF_IOP(binding, "20090701", "2008    ", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(-605, _functions.GEN_DAYDIFF_IOP(binding, "201002  ", "20080606", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(-731, _functions.GEN_DAYDIFF_IOP(binding, "201007  ", "200806  ", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(-639, _functions.GEN_DAYDIFF_IOP(binding, "201010  ", "2008    ", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(-574, _functions.GEN_DAYDIFF_IOP(binding, "2010    ", "20080606", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(-550, _functions.GEN_DAYDIFF_IOP(binding, "2010    ", "200806  ", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(-1, _functions.GEN_DAYDIFF_IOP(binding, "2009    ", "2008    ", MetafileContextFunctions.DT_MAX));
    }

    @Test
    public void testGEN_TRIM() {
        Assert.assertEquals("abc   ", _functions.GEN_TRIM("   abc   ", MetafileContextFunctions.TRIM_LEFT));
        Assert.assertEquals("   abc", _functions.GEN_TRIM("   abc   ", MetafileContextFunctions.TRIM_RIGHT));
        Assert.assertEquals("abc", _functions.GEN_TRIM("   abc   ", MetafileContextFunctions.TRIM_BOTH));
        Assert.assertEquals("", _functions.GEN_TRIM("", MetafileContextFunctions.TRIM_BOTH));
    }

    @Test
    public void testGEN_EXTERNALDLL() {
        //Test schema name
        char[] t_schema_name = new char[31];
        _functions.GEN_EXTERNALDLL("cstage.dll", "CStage_get_schema_name", 1, t_schema_name);
        Assert.assertEquals("LipUpper", new String(t_schema_name).trim());
        t_schema_name = new char[31];
        _functions.GEN_EXTERNALDLL("cstage.dll", "CStage_get_schema_name", 150, t_schema_name);
        Assert.assertEquals("Lymphoma", new String(t_schema_name).trim());
        t_schema_name = new char[31];
        _functions.GEN_EXTERNALDLL("cstage.dll", "CStage_get_schema_name", 100, t_schema_name);
        Assert.assertEquals("MycosisFungoides", new String(t_schema_name).trim());
        t_schema_name = new char[31];
        _functions.GEN_EXTERNALDLL("cstage.dll", "CStage_get_schema_name", 160, t_schema_name);
        Assert.assertEquals("", new String(t_schema_name).trim());
        t_schema_name = new char[31];
        _functions.GEN_EXTERNALDLL("cstage.dll", "CStage_get_schema_name", 0, t_schema_name);
        Assert.assertEquals("", new String(t_schema_name).trim());
        t_schema_name = new char[31];
        _functions.GEN_EXTERNALDLL("cstage.dll", "CStage_get_schema_name", -1, t_schema_name);
        Assert.assertEquals("", new String(t_schema_name).trim());
    }

    @Test
    public void testGEN_MONTHDIFF_IOP() {
        Binding binding = new Binding();
        _functions.GEN_RESET_LOCAL_CONTEXT(binding);

        Assert.assertEquals(MetafileContextFunctions.DT_EMPTY, _functions.GEN_MONTHDIFF_IOP(binding, "20000101", "        ", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(MetafileContextFunctions.DT_EMPTY, _functions.GEN_MONTHDIFF_IOP(binding, "        ", "20100101", MetafileContextFunctions.DT_EXACT));

        Assert.assertEquals(121, _functions.GEN_MONTHDIFF_IOP(binding, "20000101", "20100101", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(121, _functions.GEN_MONTHDIFF_IOP(binding, "20000101", "20100101", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(121, _functions.GEN_MONTHDIFF_IOP(binding, "20000101", "20100101", MetafileContextFunctions.DT_MAX));

        Assert.assertEquals(MetafileContextFunctions.DT_UNKNOWN, _functions.GEN_MONTHDIFF_IOP(binding, "20000101", "201001  ",
                MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(121, _functions.GEN_MONTHDIFF_IOP(binding, "20000101", "201001  ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(122, _functions.GEN_MONTHDIFF_IOP(binding, "20000101", "201001  ", MetafileContextFunctions.DT_MAX));

        Assert.assertEquals(MetafileContextFunctions.DT_UNKNOWN, _functions.GEN_MONTHDIFF_IOP(binding, "20000101", "2010    ",
                MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(121, _functions.GEN_MONTHDIFF_IOP(binding, "20000101", "2010    ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(133, _functions.GEN_MONTHDIFF_IOP(binding, "20000101", "2010    ", MetafileContextFunctions.DT_MAX));

        Assert.assertEquals(12, _functions.GEN_MONTHDIFF_IOP(binding, "20080606", "200906  ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(12, _functions.GEN_MONTHDIFF_IOP(binding, "200806  ", "20090701", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(0, _functions.GEN_MONTHDIFF_IOP(binding, "20080131", "2008    ", MetafileContextFunctions.DT_MIN));
        //assertEquals(0, _functions.GEN_MONTHDIFF_IOP(binding, "2008    ", "20080131", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(12, _functions.GEN_MONTHDIFF_IOP(binding, "20080606", "200906  ", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(13, _functions.GEN_MONTHDIFF_IOP(binding, "200806  ", "20090701", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(11, _functions.GEN_MONTHDIFF_IOP(binding, "20080131", "2008    ", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(1, _functions.GEN_MONTHDIFF_IOP(binding, "2008    ", "20080131", MetafileContextFunctions.DT_MAX));

        Assert.assertEquals(0, _functions.GEN_MONTHDIFF_IOP(binding, "2008    ", "2008    ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(12, _functions.GEN_MONTHDIFF_IOP(binding, "2008    ", "2008    ", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(0, _functions.GEN_MONTHDIFF_IOP(binding, "2008    ", "2009    ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(24, _functions.GEN_MONTHDIFF_IOP(binding, "2008    ", "2009    ", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(0, _functions.GEN_MONTHDIFF_IOP(binding, "2009    ", "2008    ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(0, _functions.GEN_MONTHDIFF_IOP(binding, "2009    ", "2008    ", MetafileContextFunctions.DT_MAX));

        Assert.assertEquals(0, _functions.GEN_MONTHDIFF_IOP(binding, "20090201", "20090301", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(0, _functions.GEN_MONTHDIFF_IOP(binding, "20090201", "20090302", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(1, _functions.GEN_MONTHDIFF_IOP(binding, "20090201", "20090303", MetafileContextFunctions.DT_MAX));

        Assert.assertEquals(MetafileContextFunctions.DT_UNKNOWN, _functions.GEN_MONTHDIFF_IOP(binding, "2008    ", "2008    ", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(MetafileContextFunctions.DT_UNKNOWN, _functions.GEN_MONTHDIFF_IOP(binding, "2008    ", "2009    ", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(MetafileContextFunctions.DT_UNKNOWN, _functions.GEN_MONTHDIFF_IOP(binding, "2009    ", "2008    ", MetafileContextFunctions.DT_EXACT));

        Assert.assertEquals(0, _functions.GEN_MONTHDIFF_IOP(binding, "20090101", "20081231", MetafileContextFunctions.DT_MAX));

        Assert.assertEquals(-19, _functions.GEN_MONTHDIFF_IOP(binding, "2010    ", "20080606", MetafileContextFunctions.DT_MIN));

        Assert.assertEquals(12, _functions.GEN_MONTHDIFF_IOP(binding, "20080606", "20090630", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(121, _functions.GEN_MONTHDIFF_IOP(binding, "20000101", "201001  ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(121, _functions.GEN_MONTHDIFF_IOP(binding, "20000101", "2010    ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(12, _functions.GEN_MONTHDIFF_IOP(binding, "200806  ", "20090701", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(12, _functions.GEN_MONTHDIFF_IOP(binding, "200806  ", "200907  ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(6, _functions.GEN_MONTHDIFF_IOP(binding, "200806  ", "2009    ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(3, _functions.GEN_MONTHDIFF_IOP(binding, "2008    ", "20090401", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(5, _functions.GEN_MONTHDIFF_IOP(binding, "2008    ", "200906  ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(0, _functions.GEN_MONTHDIFF_IOP(binding, "2008    ", "2009    ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(12, _functions.GEN_MONTHDIFF_IOP(binding, "20080606", "20090630", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(122, _functions.GEN_MONTHDIFF_IOP(binding, "20000101", "201001  ", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(133, _functions.GEN_MONTHDIFF_IOP(binding, "20000101", "2010    ", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(13, _functions.GEN_MONTHDIFF_IOP(binding, "200806  ", "20090701", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(14, _functions.GEN_MONTHDIFF_IOP(binding, "200806  ", "200907  ", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(19, _functions.GEN_MONTHDIFF_IOP(binding, "200806  ", "2009    ", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(15, _functions.GEN_MONTHDIFF_IOP(binding, "2008    ", "20090401", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(18, _functions.GEN_MONTHDIFF_IOP(binding, "2008    ", "200906  ", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(24, _functions.GEN_MONTHDIFF_IOP(binding, "2008    ", "2009    ", MetafileContextFunctions.DT_MAX));

        Assert.assertEquals(0, _functions.GEN_MONTHDIFF_IOP(binding, "20080131", "20080201", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(0, _functions.GEN_MONTHDIFF_IOP(binding, "20080606", "200806  ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(0, _functions.GEN_MONTHDIFF_IOP(binding, "20080131", "2008    ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(-4, _functions.GEN_MONTHDIFF_IOP(binding, "200806  ", "20080131", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(0, _functions.GEN_MONTHDIFF_IOP(binding, "200806  ", "200807  ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(0, _functions.GEN_MONTHDIFF_IOP(binding, "200806  ", "2008    ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(0, _functions.GEN_MONTHDIFF_IOP(binding, "2008    ", "20080131", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(0, _functions.GEN_MONTHDIFF_IOP(binding, "2008    ", "200801  ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(0, _functions.GEN_MONTHDIFF_IOP(binding, "2008    ", "2008    ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(0, _functions.GEN_MONTHDIFF_IOP(binding, "20080131", "20080201", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(0, _functions.GEN_MONTHDIFF_IOP(binding, "20080606", "200806  ", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(11, _functions.GEN_MONTHDIFF_IOP(binding, "20080131", "2008    ", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(-4, _functions.GEN_MONTHDIFF_IOP(binding, "200806  ", "20080131", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(2, _functions.GEN_MONTHDIFF_IOP(binding, "200806  ", "200807  ", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(7, _functions.GEN_MONTHDIFF_IOP(binding, "200806  ", "2008    ", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(1, _functions.GEN_MONTHDIFF_IOP(binding, "2008    ", "20080131", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(1, _functions.GEN_MONTHDIFF_IOP(binding, "2008    ", "200801  ", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(12, _functions.GEN_MONTHDIFF_IOP(binding, "2008    ", "2008    ", MetafileContextFunctions.DT_MAX));

        Assert.assertEquals(-13, _functions.GEN_MONTHDIFF_IOP(binding, "20090701", "20080606", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(-12, _functions.GEN_MONTHDIFF_IOP(binding, "20090701", "200806  ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(-6, _functions.GEN_MONTHDIFF_IOP(binding, "20090701", "2008    ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(-20, _functions.GEN_MONTHDIFF_IOP(binding, "201002  ", "20080606", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(-24, _functions.GEN_MONTHDIFF_IOP(binding, "201007  ", "200806  ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(-21, _functions.GEN_MONTHDIFF_IOP(binding, "201010  ", "2008    ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(-19, _functions.GEN_MONTHDIFF_IOP(binding, "2010    ", "20080606", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(-18, _functions.GEN_MONTHDIFF_IOP(binding, "2010    ", "200806  ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(0, _functions.GEN_MONTHDIFF_IOP(binding, "2009    ", "2008    ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(-13, _functions.GEN_MONTHDIFF_IOP(binding, "20090701", "20080606", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(-12, _functions.GEN_MONTHDIFF_IOP(binding, "20090701", "200806  ", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(-6, _functions.GEN_MONTHDIFF_IOP(binding, "20090701", "2008    ", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(-20, _functions.GEN_MONTHDIFF_IOP(binding, "201002  ", "20080606", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(-24, _functions.GEN_MONTHDIFF_IOP(binding, "201007  ", "200806  ", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(-21, _functions.GEN_MONTHDIFF_IOP(binding, "201010  ", "2008    ", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(-19, _functions.GEN_MONTHDIFF_IOP(binding, "2010    ", "20080606", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(-18, _functions.GEN_MONTHDIFF_IOP(binding, "2010    ", "200806  ", MetafileContextFunctions.DT_MAX));
        Assert.assertEquals(0, _functions.GEN_MONTHDIFF_IOP(binding, "2009    ", "2008    ", MetafileContextFunctions.DT_MAX));
    }

    @Test
    public void testGEN_YEARDIFF_IOP() {
        Binding binding = new Binding();
        _functions.GEN_RESET_LOCAL_CONTEXT(binding);

        Assert.assertEquals(MetafileContextFunctions.DT_EMPTY, _functions.GEN_YEARDIFF_IOP(binding, "20000101", "        ", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(MetafileContextFunctions.DT_EMPTY, _functions.GEN_YEARDIFF_IOP(binding, "        ", "20100101", MetafileContextFunctions.DT_EXACT));

        Assert.assertEquals(10, _functions.GEN_YEARDIFF_IOP(binding, "20000101", "20100101", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(10, _functions.GEN_YEARDIFF_IOP(binding, "20000101", "20100101", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(10, _functions.GEN_YEARDIFF_IOP(binding, "20000101", "20100101", MetafileContextFunctions.DT_MAX));

        Assert.assertEquals(MetafileContextFunctions.DT_UNKNOWN, _functions.GEN_YEARDIFF_IOP(binding, "20000101", "201001  ", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(10, _functions.GEN_YEARDIFF_IOP(binding, "20000101", "201001  ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(10, _functions.GEN_YEARDIFF_IOP(binding, "20000101", "201001  ", MetafileContextFunctions.DT_MAX));

        Assert.assertEquals(MetafileContextFunctions.DT_UNKNOWN, _functions.GEN_YEARDIFF_IOP(binding, "20000101", "2010    ", MetafileContextFunctions.DT_EXACT));
        Assert.assertEquals(10, _functions.GEN_YEARDIFF_IOP(binding, "20000101", "2010    ", MetafileContextFunctions.DT_MIN));
        Assert.assertEquals(11, _functions.GEN_YEARDIFF_IOP(binding, "20000101", "2010    ", MetafileContextFunctions.DT_MAX));
    }

    @Test
    public void testGEN_ILOOKUP() {

        // test a regular index (list of list)
        List<List<Object>> index = new ArrayList<>();
        List<Object> idx1 = new ArrayList<>();
        idx1.add("C400");
        idx1.add(3);
        index.add(idx1);
        List<Object> idx2 = new ArrayList<>();
        idx2.add("C420");
        idx2.add(1);
        index.add(idx2);
        List<Object> idx3 = new ArrayList<>();
        idx3.add("C440");
        idx3.add(2);
        index.add(idx3);
        Assert.assertTrue(_functions.GEN_ILOOKUP("C400", index));
        Assert.assertTrue(_functions.GEN_ILOOKUP("C420", index));
        Assert.assertTrue(_functions.GEN_ILOOKUP("C440", index));
        Assert.assertFalse(_functions.GEN_ILOOKUP("C399", index));
        Assert.assertFalse(_functions.GEN_ILOOKUP("C441", index));
        Assert.assertFalse(_functions.GEN_ILOOKUP("???", index));
        Assert.assertFalse(_functions.GEN_ILOOKUP(null, index));
        Assert.assertFalse(_functions.GEN_ILOOKUP("C400", null));

        // index has a space at the beginning (not common at all)
        index.clear();
        idx1 = new ArrayList<>();
        idx1.add(" ABC");
        idx1.add(1);
        index.add(idx1);
        Assert.assertFalse(_functions.GEN_ILOOKUP("ABC", index));
        Assert.assertTrue(_functions.GEN_ILOOKUP(" ABC", index));
        Assert.assertFalse(_functions.GEN_ILOOKUP("ABC ", index));
        Assert.assertFalse(_functions.GEN_ILOOKUP("A BC", index));
        Assert.assertFalse(_functions.GEN_ILOOKUP(" A BC", index));
        Assert.assertFalse(_functions.GEN_ILOOKUP("A BC ", index));
        Assert.assertFalse(_functions.GEN_ILOOKUP(" A BC  ", index));

        // index has a space in the middle
        index.clear();
        idx1 = new ArrayList<>();
        idx1.add("A BC");
        idx1.add(1);
        index.add(idx1);
        Assert.assertFalse(_functions.GEN_ILOOKUP("ABC", index));
        Assert.assertFalse(_functions.GEN_ILOOKUP(" ABC", index));
        Assert.assertFalse(_functions.GEN_ILOOKUP("ABC ", index));
        Assert.assertTrue(_functions.GEN_ILOOKUP("A BC", index));
        Assert.assertFalse(_functions.GEN_ILOOKUP(" A BC", index));
        Assert.assertTrue(_functions.GEN_ILOOKUP("A BC ", index));
        Assert.assertFalse(_functions.GEN_ILOOKUP(" A BC  ", index));

        // index has a space in the end (this is not possible because we trim the indexes, but whatever)
        index.clear();
        idx1 = new ArrayList<>();
        idx1.add("ABC ");
        idx1.add(1);
        index.add(idx1);
        Assert.assertFalse(_functions.GEN_ILOOKUP("ABC", index));
        Assert.assertFalse(_functions.GEN_ILOOKUP(" ABC", index));
        Assert.assertFalse(_functions.GEN_ILOOKUP("ABC ", index));
        Assert.assertFalse(_functions.GEN_ILOOKUP("A BC", index));
        Assert.assertFalse(_functions.GEN_ILOOKUP(" A BC", index));
        Assert.assertFalse(_functions.GEN_ILOOKUP("A BC ", index));
        Assert.assertFalse(_functions.GEN_ILOOKUP(" A BC  ", index));

        // test a list without the row numbers
        List<String> index2 = new ArrayList<>();
        index2.add("C400");
        index2.add("C420");
        index2.add("C440");
        Assert.assertTrue(_functions.GEN_ILOOKUP("C400", index2));
        Assert.assertTrue(_functions.GEN_ILOOKUP("C420", index2));
        Assert.assertTrue(_functions.GEN_ILOOKUP("C440", index2));
        Assert.assertFalse(_functions.GEN_ILOOKUP("C399", index2));
        Assert.assertFalse(_functions.GEN_ILOOKUP("C441", index2));
        Assert.assertFalse(_functions.GEN_ILOOKUP("???", index2));
        Assert.assertFalse(_functions.GEN_ILOOKUP(null, index2));

        // test a map (ILOOKUP doesn't actually use the row numbers)
        Map<String, Integer> index3 = new HashMap<>();
        index3.put("C400", 1);
        index3.put("C420", 2);
        index3.put("C440", 3);
        Assert.assertTrue(_functions.GEN_ILOOKUP("C400", index3));
        Assert.assertTrue(_functions.GEN_ILOOKUP("C420", index3));
        Assert.assertTrue(_functions.GEN_ILOOKUP("C440", index3));
        Assert.assertFalse(_functions.GEN_ILOOKUP("C399", index3));
        Assert.assertFalse(_functions.GEN_ILOOKUP("C441", index3));
        Assert.assertFalse(_functions.GEN_ILOOKUP("???", index3));
        Assert.assertFalse(_functions.GEN_ILOOKUP(null, index3));

        // test a set
        Set<String> index4 = new HashSet<>();
        index4.add("C400");
        index4.add("C420");
        index4.add("C440");
        Assert.assertTrue(_functions.GEN_ILOOKUP("C400", index4));
        Assert.assertTrue(_functions.GEN_ILOOKUP("C420", index4));
        Assert.assertTrue(_functions.GEN_ILOOKUP("C440", index4));
        Assert.assertFalse(_functions.GEN_ILOOKUP("C399", index4));
        Assert.assertFalse(_functions.GEN_ILOOKUP("C441", index4));
        Assert.assertFalse(_functions.GEN_ILOOKUP("???", index4));
        Assert.assertFalse(_functions.GEN_ILOOKUP(null, index4));
    }

    @Test
    public void testGEN_SQLLOOKUP() {

        // define the table we are going to use
        List<List<String>> tableData = new ArrayList<>();
        List<String> row0 = new ArrayList<>();
        row0.add("GPCODE"); // length 2
        row0.add("GPNAME"); // length 15
        row0.add("SITELOW"); // length 4
        row0.add("SITEHIGH"); // length 4
        tableData.add(row0);
        List<String> row1 = new ArrayList<>();
        row1.add("15");
        row1.add("Bones");
        row1.add("C420");
        row1.add("C429");
        tableData.add(row1);
        List<String> row2 = new ArrayList<>();
        row2.add("17");
        row2.add("Skin");
        row2.add("C440");
        row2.add("C449");
        tableData.add(row2);
        List<String> row3 = new ArrayList<>();
        row3.add("18");
        row3.add("Another Bones");
        row3.add("C400");
        row3.add("C409");
        tableData.add(row3);
        ContextTable table = new ContextTable("table", tableData);

        // a bad table wouldn't compile in Genedits, so the behavior here doesn't really matter
        Assert.assertFalse(_functions.GEN_SQLLOOKUP(null, null, "", null));
        Assert.assertFalse(_functions.GEN_SQLLOOKUP(null, null, "123", null));

        ContextTableIndex index = new ContextTableIndex("index", table, Collections.singletonList("GPNAME"));
        Assert.assertTrue(_functions.GEN_SQLLOOKUP(table, index, "Bones", null));
        Assert.assertTrue(_functions.GEN_SQLLOOKUP(table, index, "Skin", null));
        Assert.assertTrue(_functions.GEN_SQLLOOKUP(table, index, "Another Bones", null));
        Assert.assertFalse(_functions.GEN_SQLLOOKUP(table, index, "BadName", null));
        Assert.assertFalse(_functions.GEN_SQLLOOKUP(table, index, "", null));
        Assert.assertFalse(_functions.GEN_SQLLOOKUP(table, index, "BonesC420", null));

        index = new ContextTableIndex("index", table, Arrays.asList("GPNAME", "SITELOW"));
        Assert.assertTrue(_functions.GEN_SQLLOOKUP(table, index, "BonesC420", null));
        Assert.assertTrue(_functions.GEN_SQLLOOKUP(table, index, "SkinC440", null));
        Assert.assertTrue(_functions.GEN_SQLLOOKUP(table, index, "Another BonesC400", null));
        Assert.assertFalse(_functions.GEN_SQLLOOKUP(table, index, "BonesC450", null));
        Assert.assertFalse(_functions.GEN_SQLLOOKUP(table, index, "C420Bones", null));
        Assert.assertFalse(_functions.GEN_SQLLOOKUP(table, index, "Bones C420", null));
        Assert.assertFalse(_functions.GEN_SQLLOOKUP(table, index, " BonesC420", null));
        Assert.assertFalse(_functions.GEN_SQLLOOKUP(table, index, "BonesC420 ", null));
        Assert.assertFalse(_functions.GEN_SQLLOOKUP(table, index, "", null));
        Assert.assertFalse(_functions.GEN_SQLLOOKUP(table, index, "C440C449", null));

        index = new ContextTableIndex("index", table, Arrays.asList("GPNAME", "SITELOW", "GPCODE"));
        Assert.assertTrue(_functions.GEN_SQLLOOKUP(table, index, "BonesC42015", null));
        Assert.assertTrue(_functions.GEN_SQLLOOKUP(table, index, "SkinC44017", null));
        Assert.assertTrue(_functions.GEN_SQLLOOKUP(table, index, "Another BonesC40018", null));
        Assert.assertFalse(_functions.GEN_SQLLOOKUP(table, index, "BonesC420", null));

        char[] gpcode = new char[20];
        char[] gpname = new char[20];
        char[] sitelow = new char[20];
        char[] sitehigh = new char[20];
        Map<String, char[]> tablevars = new HashMap<>();
        tablevars.put("GPCODE", gpcode);
        tablevars.put("GPNAME", gpname);
        tablevars.put("SITELOW", sitelow);
        tablevars.put("SITEHIGH", sitehigh);

        _functions.GEN_SQLLOOKUP(table, index, "", tablevars);
        Assert.assertEquals("", _functions.GEN_TO_STRING(tablevars.get("GPCODE")));
        Assert.assertEquals("", _functions.GEN_TO_STRING(tablevars.get("GPNAME")));
        Assert.assertEquals("", _functions.GEN_TO_STRING(tablevars.get("SITELOW")));
        Assert.assertEquals("", _functions.GEN_TO_STRING(tablevars.get("SITEHIGH")));

        _functions.GEN_SQLLOOKUP(table, index, "BonesC42015", tablevars);
        Assert.assertEquals("15", _functions.GEN_TO_STRING(tablevars.get("GPCODE")));
        Assert.assertEquals("Bones", _functions.GEN_TO_STRING(tablevars.get("GPNAME")));
        Assert.assertEquals("C420", _functions.GEN_TO_STRING(tablevars.get("SITELOW")));
        Assert.assertEquals("C429", _functions.GEN_TO_STRING(tablevars.get("SITEHIGH")));

        _functions.GEN_SQLLOOKUP(table, index, "ABC", tablevars);
        Assert.assertEquals("", _functions.GEN_TO_STRING(tablevars.get("GPCODE")));
        Assert.assertEquals("", _functions.GEN_TO_STRING(tablevars.get("GPNAME")));
        Assert.assertEquals("", _functions.GEN_TO_STRING(tablevars.get("SITELOW")));
        Assert.assertEquals("", _functions.GEN_TO_STRING(tablevars.get("SITEHIGH")));

        // requesting a bad column wouldn't compile in the Genedits framework, so it's OK to throw an exception...
        try {
            new ContextTableIndex("index", table, Arrays.asList("GPNAME", "SITELOW", "GPCODE", "BadColumn"));
            Assert.fail("Should have been an exception!");
        }
        catch (RuntimeException e) {
            // expected
        }
    }

    @Test
    public void testGEN_SQLRANGELOOKUP() {

        // define the table we are going to use
        List<List<String>> tableData = new ArrayList<>();
        List<String> row0 = new ArrayList<>();
        row0.add("GPCODE"); // length 2
        row0.add("GPNAME"); // length 15
        row0.add("SITELOW"); // length 4
        row0.add("SITEHIGH"); // length 4
        tableData.add(row0);
        List<String> row1 = new ArrayList<>();
        row1.add("15");
        row1.add("Bones");
        row1.add("C420");
        row1.add("C429");
        tableData.add(row1);
        List<String> row2 = new ArrayList<>();
        row2.add("17");
        row2.add("Skin");
        row2.add("C440");
        row2.add("C449");
        tableData.add(row2);
        List<String> row3 = new ArrayList<>();
        row3.add("18");
        row3.add("Another Bones");
        row3.add("C400");
        row3.add("C409");
        tableData.add(row3);
        ContextTable table = new ContextTable("table", tableData);

        // a bad table wouldn't compile in Genedits, so the behavior here doesn't really matter
        Assert.assertFalse(_functions.GEN_SQLRANGELOOKUP(table, null, "", null));

        ContextTableIndex index = new ContextTableIndex("index", table, Collections.singletonList("SITELOW"));
        Assert.assertFalse(_functions.GEN_SQLRANGELOOKUP(table, index, "", null));
        Assert.assertFalse(_functions.GEN_SQLRANGELOOKUP(table, index, "C100", null));
        Assert.assertTrue(_functions.GEN_SQLRANGELOOKUP(table, index, "C400", null));
        Assert.assertTrue(_functions.GEN_SQLRANGELOOKUP(table, index, "C449", null));
        Assert.assertTrue(_functions.GEN_SQLRANGELOOKUP(table, index, "C999", null));

        index = new ContextTableIndex("index", table, Arrays.asList("SITELOW", "GPCODE"));
        Assert.assertFalse(_functions.GEN_SQLRANGELOOKUP(table, index, "", null));
        Assert.assertFalse(_functions.GEN_SQLRANGELOOKUP(table, index, "15", null));
        Assert.assertFalse(_functions.GEN_SQLRANGELOOKUP(table, index, "C10015", null));
        Assert.assertTrue(_functions.GEN_SQLRANGELOOKUP(table, index, "C44915", null));
        Assert.assertTrue(_functions.GEN_SQLRANGELOOKUP(table, index, "C44917", null));
        Assert.assertTrue(_functions.GEN_SQLRANGELOOKUP(table, index, "C40018", null));
        Assert.assertTrue(_functions.GEN_SQLRANGELOOKUP(table, index, "C41000", null));
        Assert.assertTrue(_functions.GEN_SQLRANGELOOKUP(table, index, "C99915", null));

        char[] gpcode = new char[20];
        char[] gpname = new char[20];
        char[] sitelow = new char[20];
        char[] sitehigh = new char[20];
        Map<String, char[]> tablevars = new HashMap<>();
        tablevars.put("GPCODE", gpcode);
        tablevars.put("GPNAME", gpname);
        tablevars.put("SITELOW", sitelow);
        tablevars.put("SITEHIGH", sitehigh);

        _functions.GEN_SQLRANGELOOKUP(table, index, "", tablevars);
        Assert.assertEquals("", _functions.GEN_TO_STRING(tablevars.get("GPCODE")));
        Assert.assertEquals("", _functions.GEN_TO_STRING(tablevars.get("GPNAME")));
        Assert.assertEquals("", _functions.GEN_TO_STRING(tablevars.get("SITELOW")));
        Assert.assertEquals("", _functions.GEN_TO_STRING(tablevars.get("SITEHIGH")));

        _functions.GEN_SQLRANGELOOKUP(table, index, "C42015", tablevars);
        Assert.assertEquals("15", _functions.GEN_TO_STRING(tablevars.get("GPCODE")));
        Assert.assertEquals("Bones", _functions.GEN_TO_STRING(tablevars.get("GPNAME")));
        Assert.assertEquals("C420", _functions.GEN_TO_STRING(tablevars.get("SITELOW")));
        Assert.assertEquals("C429", _functions.GEN_TO_STRING(tablevars.get("SITEHIGH")));

        _functions.GEN_SQLRANGELOOKUP(table, index, "C10", tablevars);
        Assert.assertEquals("", _functions.GEN_TO_STRING(tablevars.get("GPCODE")));
        Assert.assertEquals("", _functions.GEN_TO_STRING(tablevars.get("GPNAME")));
        Assert.assertEquals("", _functions.GEN_TO_STRING(tablevars.get("SITELOW")));
        Assert.assertEquals("", _functions.GEN_TO_STRING(tablevars.get("SITEHIGH")));
    }

    /**
     * The "concatenate-then-compare" strategy can backfire; here are two simple examples of how it may go wrong
     */
    @Test
    public void testGEN_SQLBadTable() {

        List<List<String>> tableData = new ArrayList<>();
        List<String> row0 = new ArrayList<>();
        row0.add("ALPHA1");
        row0.add("ALPHA2");
        row0.add("NUMBER1");
        row0.add("NUMBER2");
        tableData.add(row0);
        List<String> row1 = new ArrayList<>();
        row1.add("abc");
        row1.add("def");
        row1.add("2");
        row1.add("456");
        tableData.add(row1);
        List<String> row2 = new ArrayList<>();
        row2.add("ab");
        row2.add("cdef");
        row2.add("100");
        row2.add("56");
        tableData.add(row2);
        List<String> row3 = new ArrayList<>();
        row3.add("cdef");
        row3.add("ghij");
        row3.add("1");
        row3.add("01");
        tableData.add(row3);
        List<String> row4 = new ArrayList<>();
        row4.add("cdef");
        row4.add("klmn");
        row4.add("10");
        row4.add("1");
        tableData.add(row4);
        ContextTable table = new ContextTable("table", tableData);

        List<String> columns = new ArrayList<>();
        columns.add("ALPHA1");
        columns.add("ALPHA2");
        ContextTableIndex index = new ContextTableIndex("index", table, columns);

        char[] alpha1 = new char[20];
        char[] alpha2 = new char[20];
        char[] number1 = new char[20];
        char[] number2 = new char[20];
        Map<String, char[]> tablevars = new HashMap<>();
        tablevars.put("ALPHA1", alpha1);
        tablevars.put("ALPHA2", alpha2);
        tablevars.put("NUMBER1", number1);
        tablevars.put("NUMBER2", number2);

        // If we were looking for a row with ALPHA1 = abcd & ALPHA2 = ef, the function should return false, but we would hit row 1 and return true
        Assert.assertTrue(_functions.GEN_SQLLOOKUP(table, index, "abcdef", null));

        // This is probably not what the user wants; Row 3 -> NUMBER1 = 1 < 10 = Row 4 NUMBER1, and Row 3 -> NUMBER2 = 1 = 1 = Row 4 NUMBER2
        // So Row 3 < Row 4, but Row 3 is returned to tableVars
        columns.clear();
        columns.add("NUMBER1");
        columns.add("NUMBER2");
        index = new ContextTableIndex("index", table, columns);
        Assert.assertTrue(_functions.GEN_SQLRANGELOOKUP(table, index, "101", tablevars));
        Assert.assertEquals("cdef", _functions.GEN_TO_STRING(tablevars.get("ALPHA1")));
        Assert.assertEquals("ghij", _functions.GEN_TO_STRING(tablevars.get("ALPHA2")));
        Assert.assertEquals("1", _functions.GEN_TO_STRING(tablevars.get("NUMBER1")));
        Assert.assertEquals("01", _functions.GEN_TO_STRING(tablevars.get("NUMBER2")));
    }

    @Test
    public void testGEN_LOOKUP() {

        // define the table we are going to use (notice how the table is not sorted according to the GPCODE, but the index is sorted accoding to the SITELOW)
        List<List<Object>> table = new ArrayList<>();
        List<Object> row0 = new ArrayList<>();
        row0.add("GPCODE");
        row0.add("GPNAME");
        row0.add("SITELOW");
        row0.add("SITEHIGH");
        table.add(row0);
        List<Object> row1 = new ArrayList<>();
        row1.add("15");
        row1.add("Bones");
        row1.add("C420");
        row1.add("C429");
        table.add(row1);
        List<Object> row2 = new ArrayList<>();
        row2.add("17");
        row2.add("Skin");
        row2.add("C440");
        row2.add("C449");
        table.add(row2);
        List<Object> row3 = new ArrayList<>();
        row3.add("18");
        row3.add("Another Bones");
        row3.add("C400");
        row3.add("C409");
        table.add(row3);

        List<List<Object>> index = new ArrayList<>();
        List<Object> idx1 = new ArrayList<>();
        idx1.add("C400");
        idx1.add(3);
        index.add(idx1);
        List<Object> idx2 = new ArrayList<>();
        idx2.add("C420");
        idx2.add(1);
        index.add(idx2);
        List<Object> idx3 = new ArrayList<>();
        idx3.add("C440");
        idx3.add(2);
        index.add(idx3);

        char[] gpcode = new char[20];
        char[] gpname = new char[20];
        char[] sitelow = new char[20];
        char[] sitehigh = new char[20];
        Map<Integer, char[]> tablevars = new HashMap<>();
        tablevars.put(0, gpcode);
        tablevars.put(1, gpname);
        tablevars.put(2, sitelow);
        tablevars.put(3, sitehigh);

        // if both table and index are not there, should return false
        Assert.assertFalse(_functions.GEN_LOOKUP("C440", null, null, tablevars));

        // but if only the index is provided, it should return true
        Assert.assertTrue(_functions.GEN_LOOKUP("C440", null, index, tablevars));
        Assert.assertEquals("", _functions.GEN_TO_STRING(tablevars.get(0)));
        Assert.assertEquals("", _functions.GEN_TO_STRING(tablevars.get(1)));
        Assert.assertEquals("", _functions.GEN_TO_STRING(tablevars.get(2)));
        Assert.assertEquals("", _functions.GEN_TO_STRING(tablevars.get(3)));

        // lookup using the index
        Assert.assertTrue(_functions.GEN_LOOKUP("C440", table, index, tablevars));
        Assert.assertEquals("17", _functions.GEN_TO_STRING(tablevars.get(0)));
        Assert.assertEquals("Skin", _functions.GEN_TO_STRING(tablevars.get(1)));
        Assert.assertEquals("C440", _functions.GEN_TO_STRING(tablevars.get(2)));
        Assert.assertEquals("C449", _functions.GEN_TO_STRING(tablevars.get(3)));

        // lookup without using the index
        Assert.assertTrue(_functions.GEN_LOOKUP("18Another BonesC400C409", table, null, tablevars));
        Assert.assertEquals("18", _functions.GEN_TO_STRING(tablevars.get(0)));
        Assert.assertEquals("Another Bones", _functions.GEN_TO_STRING(tablevars.get(1)));
        Assert.assertEquals("C400", _functions.GEN_TO_STRING(tablevars.get(2)));
        Assert.assertEquals("C409", _functions.GEN_TO_STRING(tablevars.get(3)));

        // value not found
        Assert.assertFalse(_functions.GEN_LOOKUP("???", table, null, tablevars));

        // define the table we are going to use
        table = new ArrayList<>();
        row0 = new ArrayList<>();
        row0.add("COL1");
        row0.add("COL2");
        row0.add("COL1");
        table.add(row0);
        row1 = new ArrayList<>();
        row1.add("A ");
        row1.add("B ");
        row1.add("C ");
        table.add(row1);
        Assert.assertFalse(_functions.GEN_LOOKUP("ABC", table, null, null));
        Assert.assertTrue(_functions.GEN_LOOKUP("A B C ", table, null, null));
        Assert.assertTrue(_functions.GEN_LOOKUP("A B C", table, null, null));
        Assert.assertFalse(_functions.GEN_LOOKUP("A BC", table, null, null));
        Assert.assertFalse(_functions.GEN_LOOKUP("AB C", table, null, null));
        Assert.assertFalse(_functions.GEN_LOOKUP("ABC ", table, null, null));
        Assert.assertFalse(_functions.GEN_LOOKUP("ABC  ", table, null, null));
        Assert.assertFalse(_functions.GEN_LOOKUP(" ABC", table, null, null));
        Assert.assertFalse(_functions.GEN_LOOKUP(" A B C ", table, null, null));
        Assert.assertFalse(_functions.GEN_LOOKUP("???", table, null, null));

        row2 = new ArrayList<>();
        row2.add("A ");
        row2.add("BB");
        row2.add("C ");
        table.add(row2);
        Map<Integer, char[]> tableVars = new HashMap<>();
        tableVars.put(0, new char[3]);
        tableVars.put(1, new char[3]);
        tableVars.put(2, new char[3]);
        Assert.assertTrue(_functions.GEN_LOOKUP("A B C ", table, null, tableVars));
        Assert.assertEquals("B", _functions.GEN_TO_STRING(tableVars.get(1))); // columns are 0-based...
        Assert.assertTrue(_functions.GEN_LOOKUP("A BBC ", table, null, tableVars));
        Assert.assertEquals("BB", _functions.GEN_TO_STRING(tableVars.get(1)));

        // index has a space at the beginning (not common at all)
        index.clear();
        idx1 = new ArrayList<>();
        idx1.add(" ABC");
        idx1.add(1);
        index.add(idx1);
        Assert.assertFalse(_functions.GEN_LOOKUP("ABC", table, index, null));
        Assert.assertTrue(_functions.GEN_LOOKUP(" ABC", table, index, null));
        Assert.assertFalse(_functions.GEN_LOOKUP("ABC ", table, index, null));
        Assert.assertFalse(_functions.GEN_LOOKUP("A BC", table, index, null));
        Assert.assertFalse(_functions.GEN_LOOKUP(" A BC", table, index, null));
        Assert.assertFalse(_functions.GEN_LOOKUP("A BC ", table, index, null));
        Assert.assertFalse(_functions.GEN_LOOKUP(" A BC  ", table, index, null));

        // index has a space in the middle
        index.clear();
        idx1 = new ArrayList<>();
        idx1.add("A BC");
        idx1.add(1);
        index.add(idx1);
        Assert.assertFalse(_functions.GEN_LOOKUP("ABC", table, index, null));
        Assert.assertFalse(_functions.GEN_LOOKUP(" ABC", table, index, null));
        Assert.assertFalse(_functions.GEN_LOOKUP("ABC ", table, index, null));
        Assert.assertTrue(_functions.GEN_LOOKUP("A BC", table, index, null));
        Assert.assertFalse(_functions.GEN_LOOKUP(" A BC", table, index, null));
        Assert.assertTrue(_functions.GEN_LOOKUP("A BC ", table, index, null));
        Assert.assertFalse(_functions.GEN_LOOKUP(" A BC  ", table, index, null));

        // index has a space in the end (this is not possible because we trim the indexes, but whatever)
        index.clear();
        idx1 = new ArrayList<>();
        idx1.add("ABC ");
        idx1.add(1);
        index.add(idx1);
        Assert.assertFalse(_functions.GEN_LOOKUP("ABC", table, index, null));
        Assert.assertFalse(_functions.GEN_LOOKUP(" ABC", table, index, null));
        Assert.assertFalse(_functions.GEN_LOOKUP("ABC ", table, index, null));
        Assert.assertFalse(_functions.GEN_LOOKUP("A BC", table, index, null));
        Assert.assertFalse(_functions.GEN_LOOKUP(" A BC", table, index, null));
        Assert.assertFalse(_functions.GEN_LOOKUP("A BC ", table, index, null));
        Assert.assertFalse(_functions.GEN_LOOKUP(" A BC  ", table, index, null));

        // test a list without the row numbers
        List<String> index2 = new ArrayList<>();
        index2.add("C400");
        index2.add("C420");
        index2.add("C440");
        Assert.assertTrue(_functions.GEN_LOOKUP("C400", null, index2, null));
        Assert.assertTrue(_functions.GEN_LOOKUP("C420", null, index2, null));
        Assert.assertTrue(_functions.GEN_LOOKUP("C440", null, index2, null));
        Assert.assertFalse(_functions.GEN_LOOKUP("C399", null, index2, null));
        Assert.assertFalse(_functions.GEN_LOOKUP("C441", null, index2, null));
        Assert.assertFalse(_functions.GEN_LOOKUP("???", null, index2, null));
        Assert.assertFalse(_functions.GEN_LOOKUP(null, null, index2, null));

        // test a map without requesting tablevars
        Map<String, Integer> index3 = new HashMap<>();
        index3.put("C400", 1);
        index3.put("C420", 2);
        index3.put("C440", 3);
        Assert.assertTrue(_functions.GEN_LOOKUP("C400", null, index3, null));
        Assert.assertTrue(_functions.GEN_LOOKUP("C420", null, index3, null));
        Assert.assertTrue(_functions.GEN_LOOKUP("C440", null, index3, null));
        Assert.assertFalse(_functions.GEN_LOOKUP("C399", null, index3, null));
        Assert.assertFalse(_functions.GEN_LOOKUP("C441", null, index3, null));
        Assert.assertFalse(_functions.GEN_LOOKUP("???", null, index3, null));
        Assert.assertFalse(_functions.GEN_LOOKUP(null, null, index3, null));

        // test a map where we request tablevars
        List<List<Object>> table3 = new ArrayList<>();
        table3.add(Arrays.asList("INDEX", "SITE", "HIST"));
        table3.add(Arrays.asList("1", "C400", "8000"));
        table3.add(Arrays.asList("2", "C420", "8000"));
        table3.add(Arrays.asList("3", "C440", "8005"));
        Map<Integer, char[]> tableVars3 = new HashMap<>();
        tableVars3.put(1, new char[5]);
        tableVars3.put(2, new char[5]);
        Assert.assertTrue(_functions.GEN_LOOKUP("C440", table3, index3, tableVars3));
        Assert.assertEquals("C440", String.valueOf(tableVars3.get(1)).trim());
        Assert.assertEquals("8005", String.valueOf(tableVars3.get(2)).trim());

        // test a set
        Set<String> index4 = new HashSet<>();
        index4.add("C400");
        index4.add("C420");
        index4.add("C440");
        Assert.assertTrue(_functions.GEN_LOOKUP("C400", null, index4, null));
        Assert.assertTrue(_functions.GEN_LOOKUP("C420", null, index4, null));
        Assert.assertTrue(_functions.GEN_LOOKUP("C440", null, index4, null));
        Assert.assertFalse(_functions.GEN_LOOKUP("C399", null, index4, null));
        Assert.assertFalse(_functions.GEN_LOOKUP("C441", null, index4, null));
        Assert.assertFalse(_functions.GEN_LOOKUP("???", null, index4, null));
        Assert.assertFalse(_functions.GEN_LOOKUP(null, null, index4, null));

        // requested table vars with a set has no effect
        Map<Integer, char[]> tableVars4 = new HashMap<>();
        tableVars4.put(1, new char[5]);
        tableVars4.put(2, new char[5]);
        Assert.assertTrue(_functions.GEN_LOOKUP("C440", table3, index4, tableVars4));
        Assert.assertTrue(String.valueOf(tableVars4.get(1)).trim().isEmpty());
        Assert.assertTrue(String.valueOf(tableVars4.get(2)).trim().isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGEN_RLOOKUP() throws ConstructionException {

        // define the table we are going to use (notice how the table is not sorted according to the GPCODE, but the index is sorted accoding to the SITELOW)
        List<List<String>> table = new ArrayList<>();
        List<String> row0 = new ArrayList<>();
        row0.add("GPCODE");
        row0.add("GPNAME");
        row0.add("SITELOW");
        row0.add("SITEHIGH");
        table.add(row0);
        List<String> row1 = new ArrayList<>();
        row1.add("15");
        row1.add("Bones");
        row1.add("C420");
        row1.add("C429");
        table.add(row1);
        List<String> row2 = new ArrayList<>();
        row2.add("17");
        row2.add("Skin");
        row2.add("C440");
        row2.add("C449");
        table.add(row2);
        List<String> row3 = new ArrayList<>();
        row3.add("18");
        row3.add("Another Bones");
        row3.add("C400");
        row3.add("C409");
        table.add(row3);

        // corresponding list index
        List<List<Object>> indexList = new ArrayList<>();
        List<Object> idx1 = new ArrayList<>();
        idx1.add("C400");
        idx1.add(3);
        indexList.add(idx1);
        List<Object> idx2 = new ArrayList<>();
        idx2.add("C420");
        idx2.add(1);
        indexList.add(idx2);
        List<Object> idx3 = new ArrayList<>();
        idx3.add("C440");
        idx3.add(2);
        indexList.add(idx3);

        // corresponding tree index
        Map<String, Integer> indexTree = new TreeMap<>();
        indexTree.put("C400", 3);
        indexTree.put("C420", 1);
        indexTree.put("C440", 2);

        char[] gpcode = new char[20];
        char[] gpname = new char[20];
        char[] sitelow = new char[20];
        char[] sitehigh = new char[20];
        Map<Integer, char[]> tablevars = new HashMap<>();
        tablevars.put(0, gpcode);
        tablevars.put(1, gpname);
        tablevars.put(2, sitelow);
        tablevars.put(3, sitehigh);

        // try to find first value
        Assert.assertTrue(_functions.GEN_RLOOKUP("C400", table, indexList, tablevars));
        Assert.assertTrue(_functions.GEN_RLOOKUP("C405", table, indexList, tablevars));
        Assert.assertTrue(_functions.GEN_RLOOKUP("C409", table, indexList, tablevars));
        Assert.assertEquals("18", _functions.GEN_TO_STRING(tablevars.get(0)));
        Assert.assertEquals("Another Bones", _functions.GEN_TO_STRING(tablevars.get(1)));
        Assert.assertEquals("C400", _functions.GEN_TO_STRING(tablevars.get(2)));
        Assert.assertEquals("C409", _functions.GEN_TO_STRING(tablevars.get(3)));
        // same tests but with the tree index
        Assert.assertTrue(_functions.GEN_RLOOKUP("C400", table, indexTree, tablevars));
        Assert.assertTrue(_functions.GEN_RLOOKUP("C405", table, indexTree, tablevars));
        Assert.assertTrue(_functions.GEN_RLOOKUP("C409", table, indexTree, tablevars));
        Assert.assertEquals("18", _functions.GEN_TO_STRING(tablevars.get(0)));
        Assert.assertEquals("Another Bones", _functions.GEN_TO_STRING(tablevars.get(1)));
        Assert.assertEquals("C400", _functions.GEN_TO_STRING(tablevars.get(2)));
        Assert.assertEquals("C409", _functions.GEN_TO_STRING(tablevars.get(3)));

        // try to find middle value
        Assert.assertTrue(_functions.GEN_RLOOKUP("C420", table, indexList, tablevars));
        Assert.assertTrue(_functions.GEN_RLOOKUP("C425", table, indexList, tablevars));
        Assert.assertTrue(_functions.GEN_RLOOKUP("C429", table, indexList, tablevars));
        Assert.assertEquals("15", _functions.GEN_TO_STRING(tablevars.get(0)));
        Assert.assertEquals("Bones", _functions.GEN_TO_STRING(tablevars.get(1)));
        Assert.assertEquals("C420", _functions.GEN_TO_STRING(tablevars.get(2)));
        Assert.assertEquals("C429", _functions.GEN_TO_STRING(tablevars.get(3)));
        // same tests but with the tree index
        Assert.assertTrue(_functions.GEN_RLOOKUP("C420", table, indexTree, tablevars));
        Assert.assertTrue(_functions.GEN_RLOOKUP("C425", table, indexTree, tablevars));
        Assert.assertTrue(_functions.GEN_RLOOKUP("C429", table, indexTree, tablevars));
        Assert.assertEquals("15", _functions.GEN_TO_STRING(tablevars.get(0)));
        Assert.assertEquals("Bones", _functions.GEN_TO_STRING(tablevars.get(1)));
        Assert.assertEquals("C420", _functions.GEN_TO_STRING(tablevars.get(2)));
        Assert.assertEquals("C429", _functions.GEN_TO_STRING(tablevars.get(3)));

        // try to find last value
        Assert.assertTrue(_functions.GEN_RLOOKUP("C440", table, indexTree, tablevars));
        Assert.assertTrue(_functions.GEN_RLOOKUP("C445", table, indexTree, tablevars));
        Assert.assertTrue(_functions.GEN_RLOOKUP("C449", table, indexTree, tablevars));
        Assert.assertEquals("17", _functions.GEN_TO_STRING(tablevars.get(0)));
        Assert.assertEquals("Skin", _functions.GEN_TO_STRING(tablevars.get(1)));
        Assert.assertEquals("C440", _functions.GEN_TO_STRING(tablevars.get(2)));
        Assert.assertEquals("C449", _functions.GEN_TO_STRING(tablevars.get(3)));
        // same tests but with the tree index
        Assert.assertTrue(_functions.GEN_RLOOKUP("C440", table, indexTree, tablevars));
        Assert.assertTrue(_functions.GEN_RLOOKUP("C445", table, indexTree, tablevars));
        Assert.assertTrue(_functions.GEN_RLOOKUP("C449", table, indexTree, tablevars));
        Assert.assertEquals("17", _functions.GEN_TO_STRING(tablevars.get(0)));
        Assert.assertEquals("Skin", _functions.GEN_TO_STRING(tablevars.get(1)));
        Assert.assertEquals("C440", _functions.GEN_TO_STRING(tablevars.get(2)));
        Assert.assertEquals("C449", _functions.GEN_TO_STRING(tablevars.get(3)));

        // value is smaller than any index value -> value not found
        Assert.assertFalse(_functions.GEN_RLOOKUP("C390", table, indexTree, tablevars));
        Assert.assertEquals("", _functions.GEN_TO_STRING(tablevars.get(0)));
        Assert.assertEquals("", _functions.GEN_TO_STRING(tablevars.get(1)));
        Assert.assertEquals("", _functions.GEN_TO_STRING(tablevars.get(2)));
        Assert.assertEquals("", _functions.GEN_TO_STRING(tablevars.get(3)));
        // same tests but with the tree index
        Assert.assertFalse(_functions.GEN_RLOOKUP("C390", table, indexTree, tablevars));
        Assert.assertEquals("", _functions.GEN_TO_STRING(tablevars.get(0)));
        Assert.assertEquals("", _functions.GEN_TO_STRING(tablevars.get(1)));
        Assert.assertEquals("", _functions.GEN_TO_STRING(tablevars.get(2)));
        Assert.assertEquals("", _functions.GEN_TO_STRING(tablevars.get(3)));

        // value is bigger than any index value -> use last table value
        Assert.assertTrue(_functions.GEN_RLOOKUP("C500", table, indexTree, tablevars));
        Assert.assertEquals("Skin", _functions.GEN_TO_STRING(tablevars.get(1)));
        Assert.assertEquals("C440", _functions.GEN_TO_STRING(tablevars.get(2)));
        Assert.assertEquals("C449", _functions.GEN_TO_STRING(tablevars.get(3)));
        // same tests but with the tree index
        Assert.assertTrue(_functions.GEN_RLOOKUP("C500", table, indexTree, tablevars));
        Assert.assertEquals("Skin", _functions.GEN_TO_STRING(tablevars.get(1)));
        Assert.assertEquals("C440", _functions.GEN_TO_STRING(tablevars.get(2)));
        Assert.assertEquals("C449", _functions.GEN_TO_STRING(tablevars.get(3)));

        // value not found
        Assert.assertFalse(_functions.GEN_RLOOKUP("???", table, null, tablevars));
        // same tests but with the tree index
        Assert.assertFalse(_functions.GEN_RLOOKUP("???", table, null, tablevars));

        // define another table we are going to use
        table = new ArrayList<>();
        row0 = new ArrayList<>();
        row0.add("STATE");
        row0.add("ZIPLOW");
        row0.add("ZIPHIGH");
        table.add(row0);
        row1 = new ArrayList<>();
        row1.add("PR");
        row1.add("00600");
        row1.add("00799");
        table.add(row1);
        row2 = new ArrayList<>();
        row2.add("VI");
        row2.add("00800");
        row2.add("00899");
        table.add(row2);
        row3 = new ArrayList<>();
        row3.add("PR");
        row3.add("00900");
        row3.add("00999");
        table.add(row3);
        List<String> row4 = new ArrayList<>();
        row4.add("A");
        row4.add("1");
        row4.add("3");
        table.add(row4);
        List<String> row5 = new ArrayList<>();
        row5.add("NY");
        row5.add("10000");
        row5.add("14999");
        table.add(row5);

        // corresponding list index
        indexList = new ArrayList<>();
        idx1 = new ArrayList<>();
        idx1.add("0060000799PR");
        idx1.add(1);
        indexList.add(idx1);
        idx2 = new ArrayList<>();
        idx2.add("0080000899VI");
        idx2.add(2);
        indexList.add(idx2);
        idx3 = new ArrayList<>();
        idx3.add("0090000999PR");
        idx3.add(3);
        indexList.add(idx3);
        List<Object> idx4 = new ArrayList<>();
        idx4.add("1    3    A ");
        idx4.add(4);
        indexList.add(idx4);
        List<Object> idx5 = new ArrayList<>();
        idx5.add("1000014999NY");
        idx5.add(5);
        indexList.add(idx5);

        // corresponding tree index
        indexTree = new TreeMap<>();
        indexTree.put("0060000799PR", 1);
        indexTree.put("0080000899VI", 2);
        indexTree.put("0090000999PR", 3);
        indexTree.put("1    3    A ", 4);
        indexTree.put("1000014999NY", 5);

        char[] state = new char[20];
        char[] ziplow = new char[20];
        char[] ziphigh = new char[20];
        tablevars = new HashMap<>();
        tablevars.put(0, state);
        tablevars.put(1, ziplow);
        tablevars.put(2, ziphigh);

        Assert.assertTrue(_functions.GEN_RLOOKUP("0060006000PR", table, indexList, tablevars));
        Assert.assertEquals("PR", _functions.GEN_TO_STRING(tablevars.get(0)));
        Assert.assertEquals("00600", _functions.GEN_TO_STRING(tablevars.get(1)));
        Assert.assertEquals("00799", _functions.GEN_TO_STRING(tablevars.get(2)));
        // same tests but with the tree index
        Assert.assertTrue(_functions.GEN_RLOOKUP("0060006000PR", table, indexTree, tablevars));
        Assert.assertEquals("PR", _functions.GEN_TO_STRING(tablevars.get(0)));
        Assert.assertEquals("00600", _functions.GEN_TO_STRING(tablevars.get(1)));
        Assert.assertEquals("00799", _functions.GEN_TO_STRING(tablevars.get(2)));

        Assert.assertTrue(_functions.GEN_RLOOKUP("0085000899VI", table, indexList, tablevars));
        Assert.assertEquals("VI", _functions.GEN_TO_STRING(tablevars.get(0)));
        Assert.assertEquals("00800", _functions.GEN_TO_STRING(tablevars.get(1)));
        Assert.assertEquals("00899", _functions.GEN_TO_STRING(tablevars.get(2)));
        // same tests but with the tree index
        Assert.assertTrue(_functions.GEN_RLOOKUP("0085000899VI", table, indexTree, tablevars));
        Assert.assertEquals("VI", _functions.GEN_TO_STRING(tablevars.get(0)));
        Assert.assertEquals("00800", _functions.GEN_TO_STRING(tablevars.get(1)));
        Assert.assertEquals("00899", _functions.GEN_TO_STRING(tablevars.get(2)));

        Assert.assertTrue(_functions.GEN_RLOOKUP("1    3A", table, indexList, tablevars));
        Assert.assertEquals("A", _functions.GEN_TO_STRING(tablevars.get(0)));
        Assert.assertEquals("1", _functions.GEN_TO_STRING(tablevars.get(1)));
        Assert.assertEquals("3", _functions.GEN_TO_STRING(tablevars.get(2)));
        // same tests but with the tree index
        Assert.assertTrue(_functions.GEN_RLOOKUP("1    3A", table, indexTree, tablevars));
        Assert.assertEquals("A", _functions.GEN_TO_STRING(tablevars.get(0)));
        Assert.assertEquals("1", _functions.GEN_TO_STRING(tablevars.get(1)));
        Assert.assertEquals("3", _functions.GEN_TO_STRING(tablevars.get(2)));

        //return last entry in table since search value is greater than the last index
        Assert.assertTrue(_functions.GEN_RLOOKUP("15000160000NY", table, indexList, tablevars));
        Assert.assertEquals("NY", _functions.GEN_TO_STRING(tablevars.get(0)));
        Assert.assertEquals("10000", _functions.GEN_TO_STRING(tablevars.get(1)));
        Assert.assertEquals("14999", _functions.GEN_TO_STRING(tablevars.get(2)));
        // same tests but with the tree index
        Assert.assertTrue(_functions.GEN_RLOOKUP("15000160000NY", table, indexTree, tablevars));
        Assert.assertEquals("NY", _functions.GEN_TO_STRING(tablevars.get(0)));
        Assert.assertEquals("10000", _functions.GEN_TO_STRING(tablevars.get(1)));
        Assert.assertEquals("14999", _functions.GEN_TO_STRING(tablevars.get(2)));

        //return false because search value was smaller than anything in the table
        Assert.assertFalse(_functions.GEN_RLOOKUP("0050000599PR", table, indexList, tablevars));
        Assert.assertEquals("", _functions.GEN_TO_STRING(tablevars.get(0)));
        Assert.assertEquals("", _functions.GEN_TO_STRING(tablevars.get(1)));
        Assert.assertEquals("", _functions.GEN_TO_STRING(tablevars.get(2)));
        // same tests but with the tree index
        Assert.assertFalse(_functions.GEN_RLOOKUP("0050000599PR", table, indexTree, tablevars));
        Assert.assertEquals("", _functions.GEN_TO_STRING(tablevars.get(0)));
        Assert.assertEquals("", _functions.GEN_TO_STRING(tablevars.get(1)));
        Assert.assertEquals("", _functions.GEN_TO_STRING(tablevars.get(2)));

        // a real case that didn't use to work correctly
        ContextTable cTable = new ContextTable("NAACCR_CS_OBS", (List<List<String>>)JavaContextParser.parseContext(TestingUtils.readResource("tables/NAACCR_CS_OBS.txt"), null));
        ContextTableIndex cIndex = new ContextTableIndex("NAACCR_CS_OBS_INDEX1", cTable, Collections.singletonList("INDEX1"));
        char[] obsType = new char[3];
        Map<String, char[]> tableVars = Collections.singletonMap("OBS_TYPE", obsType);
        Assert.assertTrue(_functions.GEN_RLOOKUP("Prostate                        12410                                                               ", cTable, cIndex, tableVars));
        Assert.assertEquals("13", _functions.GEN_TO_STRING(obsType));
    }

    @Test
    public void testGEN_BINLOOKUP() {

        List<List<Integer>> table = new ArrayList<>();
        List<Integer> row0 = new ArrayList<>();
        row0.add(1);
        row0.add(0);
        row0.add(0);
        row0.add(0);
        row0.add(0);
        table.add(row0);
        List<Integer> row1 = new ArrayList<>();
        row1.add(0);
        row1.add(1);
        row1.add(0);
        row1.add(0);
        row1.add(0);
        table.add(row1);

        Assert.assertEquals(1, _functions.GEN_BINLOOKUP(table, 7).intValue());
        Assert.assertEquals(1, _functions.GEN_BINLOOKUP(table, 2, 2).intValue());

        Assert.assertEquals(0, _functions.GEN_BINLOOKUP(table, 100).intValue());
        Assert.assertEquals(0, _functions.GEN_BINLOOKUP(table, 100, 1).intValue());
        Assert.assertEquals(0, _functions.GEN_BINLOOKUP(table, 1, 100).intValue());

        Assert.assertEquals(0, _functions.GEN_BINLOOKUP(table, 0).intValue());
        Assert.assertEquals(0, _functions.GEN_BINLOOKUP(table, 0, 0).intValue());

        Assert.assertEquals(1, _functions.GEN_BINLOOKUP(table, 1).intValue());
        Assert.assertEquals(1, _functions.GEN_BINLOOKUP(table, 1, 1).intValue());
    }

    @Test
    public void testGEN_DT_TODAY() {
        Assert.assertTrue(_functions.GEN_DT_TODAY().matches("\\d{8}"));
    }
}
