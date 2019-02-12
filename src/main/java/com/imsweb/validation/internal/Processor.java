/*
 * Copyright (C) 2004 Information Management Services, Inc.
 */
package com.imsweb.validation.internal;

import java.util.Collection;

import com.imsweb.validation.ValidatingContext;
import com.imsweb.validation.ValidationException;
import com.imsweb.validation.entities.RuleFailure;
import com.imsweb.validation.entities.Validatable;

/**
 * A <code>Processor</code> is responsible for running the edits on a given <code>Validatable</code>.
 * <p/>
 * It is very similar to a <code>Validator</code>, but unlike it, it is structured to be used internally by the validation engine.
 * <p/>
 * Created on Apr 26, 2011 by depryf
 * @author depryf
 */
public interface Processor {

    /**
     * Calculates the edits on the passed validatable object
     * <p/>
     * Created on Nov 15, 2007 by depryf
     * @param validatable the <code>Validatable</code> to process
     * @param procCtx a processing context
     * @return a collection of <code>RuleFailure</code>, maybe empty but never null
     * @throws ValidationException
     */
    Collection<RuleFailure> process(Validatable validatable, ValidatingContext procCtx) throws ValidationException;
}
