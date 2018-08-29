/*
 * Copyright (C) 2011 Information Management Services, Inc.
 */
package com.imsweb.validation.entities;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created on Feb 23, 2011 by depryf
 * @author depryf
 */
public class ValidatorVersionTest {

    @Test
    public void testFormatValidity() {
        Assert.assertFalse(ValidatorVersion.validateVersionFormat(null));
        Assert.assertFalse(ValidatorVersion.validateVersionFormat(""));
        Assert.assertFalse(ValidatorVersion.validateVersionFormat("  "));
        Assert.assertFalse(ValidatorVersion.validateVersionFormat("abc"));
        Assert.assertFalse(ValidatorVersion.validateVersionFormat("123"));
        Assert.assertFalse(ValidatorVersion.validateVersionFormat("123-456"));
        Assert.assertFalse(ValidatorVersion.validateVersionFormat("123-456-789"));

        Assert.assertTrue(ValidatorVersion.validateVersionFormat("123-456-78"));
        Assert.assertTrue(ValidatorVersion.validateVersionFormat("SE11-001-01"));
        Assert.assertTrue(ValidatorVersion.validateVersionFormat("SE12-001-01"));
        Assert.assertTrue(ValidatorVersion.validateVersionFormat("SE13-001-01"));
        Assert.assertTrue(ValidatorVersion.validateVersionFormat("SE13-001-01-1"));
        Assert.assertTrue(ValidatorVersion.validateVersionFormat("SE13-999-99"));
        Assert.assertTrue(ValidatorVersion.validateVersionFormat("SE13-999-99-9"));
    }

    @Test
    public void testComparison() {

        Assert.assertEquals(-1, ValidatorVersion.compareVersions("SE12-001-01", "SE12-001-02"));
        Assert.assertEquals(-1, ValidatorVersion.compareVersions("SE12-001-01", "SE12-001-99"));
        Assert.assertEquals(-1, ValidatorVersion.compareVersions("SE12-002-01", "SE12-002-02"));
        Assert.assertEquals(-1, ValidatorVersion.compareVersions("SE12-002-01", "SE12-003-02"));
        Assert.assertEquals(-1, ValidatorVersion.compareVersions("SE12-999-98", "SE12-999-99"));
        Assert.assertEquals(-1, ValidatorVersion.compareVersions("SE12-001-01", "SE12-001-01-1"));
        Assert.assertEquals(-1, ValidatorVersion.compareVersions("SE12-001-01-1", "SE12-001-01-2"));

        Assert.assertEquals(0, ValidatorVersion.compareVersions("SE12-001-01", "SE12-001-01"));
        Assert.assertEquals(0, ValidatorVersion.compareVersions("SE11-001-01", "SE13-001-01"));
        Assert.assertEquals(0, ValidatorVersion.compareVersions("abc-001-01", "def-001-01"));
        Assert.assertEquals(0, ValidatorVersion.compareVersions("SE12-001-01-1", "SE12-001-01-1"));
        Assert.assertEquals(0, ValidatorVersion.compareVersions("SE12-001-01-0", "SE12-001-01-0"));
        Assert.assertEquals(0, ValidatorVersion.compareVersions("SE12-001-01", "SE12-001-01-0"));

        Assert.assertEquals(1, ValidatorVersion.compareVersions("SE12-001-02", "SE12-001-01"));
        Assert.assertEquals(1, ValidatorVersion.compareVersions("SE12-001-99", "SE12-001-01"));
        Assert.assertEquals(1, ValidatorVersion.compareVersions("SE12-002-02", "SE12-002-01"));
        Assert.assertEquals(1, ValidatorVersion.compareVersions("SE12-003-02", "SE12-002-01"));
        Assert.assertEquals(1, ValidatorVersion.compareVersions("SE12-999-99", "SE12-999-98"));
        Assert.assertEquals(1, ValidatorVersion.compareVersions("SE12-001-01-1", "SE12-001-01"));
        Assert.assertEquals(1, ValidatorVersion.compareVersions("SE12-001-01-2", "SE12-001-01-1"));

        // invalid versions are always considered as older
        Assert.assertEquals(-1, ValidatorVersion.compareVersions("invalid", "SE12-001-01"));
        Assert.assertEquals(-1, ValidatorVersion.compareVersions("invalid", "invalid"));
        Assert.assertEquals(1, ValidatorVersion.compareVersions("SE12-001-01", "invalid"));

    }
}
