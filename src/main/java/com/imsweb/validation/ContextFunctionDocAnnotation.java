/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annoatation used to provide a documenation for a particular context method.
 * <p>
 * Created on Mar 2, 2010 by depryf
 * @author depryf
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ContextFunctionDocAnnotation {

    /** param */
    String param1() default "";

    /** param */
    String param2() default "";

    /** param */
    String param3() default "";

    /** param */
    String param4() default "";

    /** param */
    String param5() default "";

    /** param */
    String param6() default "";

    /** param */
    String param7() default "";

    /** param */
    String param8() default "";

    /** param */
    String param9() default "";

    /** param */
    String param10() default "";

    /** param name*/
    String paramName1() default "";

    /** param name */
    String paramName2() default "";

    /** param name */
    String paramName3() default "";

    /** param name */
    String paramName4() default "";

    /** param name */
    String paramName5() default "";

    /** param name */
    String paramName6() default "";

    /** param name */
    String paramName7() default "";

    /** param name */
    String paramName8() default "";

    /** param name */
    String paramName9() default "";

    /** param name */
    String paramName10() default "";

    /** method description */
    String desc();

    /** example(s) */
    String example() default "";
}
