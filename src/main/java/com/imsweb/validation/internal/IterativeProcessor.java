/*
 * Copyright (C) 2004 Information Management Services, Inc.
 */
package com.imsweb.validation.internal;

import java.util.ArrayList;
import java.util.Collection;

import com.imsweb.validation.ValidationException;
import com.imsweb.validation.entities.RuleFailure;
import com.imsweb.validation.entities.Validatable;

/**
 * An <code>IterativeProcessor</code> is a <code>Processor</code> that <i>follows</i> a collection on a <code>Validatable</code>.
 * <p/>
 * Conceptually, an <code>IterativeProcessor</code> does not execute any edits; it is only a mechanism to iterate over a complex data structure.
 * <p/>
 * Created on Apr 26, 2011 by depryf
 * @author depryf
 */
public class IterativeProcessor implements Processor {

    /**
     * Name of the collection associated with this processor
     */
    private String _collectionName;

    /**
     * Child processor
     */
    private Processor _processor;

    /**
     * Constructor
     * <p/>
     * Created on Jan 9, 2006 by Mike Ringrose
     * @param processor that performs all of the actual work
     * @param collectionName collection to iterate over
     */
    public IterativeProcessor(Processor processor, String collectionName) {
        if (processor == null)
            throw new RuntimeException("Cannot create an iterative processor from a null processor!");
        if (collectionName == null)
            throw new RuntimeException("Cannot create an iterative processor from a null collection name!");

        _processor = processor;
        _collectionName = collectionName;
    }

    @Override
    public Collection<RuleFailure> process(Validatable validatable, ValidatingContext procCtx) throws ValidationException {
        Collection<RuleFailure> results = new ArrayList<>();

        try {
            // it is important to pass a new version of the failedRuleIds and failedConditionsIds!
            for (Validatable childValidatable : validatable.followCollection(_collectionName))
                results.addAll(_processor.process(childValidatable, procCtx));
        }
        catch (IllegalAccessException e) {
            throw new ValidationException(e);
        }

        return results;
    }
}
