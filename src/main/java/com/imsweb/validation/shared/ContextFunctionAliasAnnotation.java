/*
 * Copyright (C) 2009 Information Management Services, Inc.
 */
package com.imsweb.validation.shared;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used by the context methods to provide the alias of the returned object.
 * <p/>
 * For example, if a method takes a list of NAACCR lines (so a list of maps), and runs some logic
 * to select and return one of them, it would use this annotation to let the validation engine know
 * that the returned object is a NAACCR line (using the alias "line").
 * <p/>
 * Consider an edit running on the NAACCR lines:
 * <fixed>
 * def earliestDxLine = Functions.getEarliestDxLine(lines)
 * def site = earliestDxLine.primariSite
 * ...
 * </fixed>
 * <p/>
 * Without this notation, the engine would not be able to correctly parse the edit so that primary site
 * is recognized as a line property.
 * <p/>
 * Created on Feb 23, 2009 by depryf
 * @author depryf
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ContextFunctionAliasAnnotation {

    /** Unique value for this annotation */
    String value();
}
