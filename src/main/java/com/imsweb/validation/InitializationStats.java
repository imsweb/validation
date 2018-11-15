/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.validation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class InitializationStats {

    public static final String REASON_NOT_PROVIDED = "pre-compiled edits not provided";
    public static final String REASON_DIFFERENT_VERSION = "pre-compiled validator has version {1} but application expected {2}";

    // following reasons are deprecated and will be removed in a future version
    public static final String REASON_CLASS_NOT_FOUND = "pre-compiled class '{0}' not found";
    public static final String REASON_CLASS_INSTANCIATION_ERROR = "unable to create instance of pre-compiled class '{0}'";
    public static final String REASON_CLASS_ACCESS_ERROR = "pre-compiled class '{0}' can't be accessed";
    public static final String REASON_CLASS_CAST_ERROR = "pre-compiled class '{0}' was not of type 'CompiledRules'";
    public static final String REASON_CONSTRUCTOR_NOT_FOUND = "pre-compiled class '{0}' doesn't define a default constructor";

    private long _initializationDuration;

    private AtomicInteger _numEditsLoaded;

    private AtomicInteger _numEditsCompiled;

    private AtomicInteger _numEditsPreCompiled;

    private Map<String, String> _reasonNotPreCompiled;

    public InitializationStats() {
        _initializationDuration = 0L;
        _numEditsLoaded = new AtomicInteger();
        _numEditsCompiled = new AtomicInteger();
        _numEditsPreCompiled = new AtomicInteger();
        _reasonNotPreCompiled = new HashMap<>();
    }

    public long getInitializationDuration() {
        return _initializationDuration;
    }

    public void setInitializationDuration(long initializationDuration) {
        _initializationDuration = initializationDuration;
    }

    public int getNumEditsLoaded() {
        return _numEditsLoaded.get();
    }

    public void incrementNumEditsLoaded() {
        _numEditsLoaded.getAndIncrement();
    }

    public int getNumEditsCompiled() {
        return _numEditsCompiled.get();
    }

    public void incrementNumEditsCompiled() {
        _numEditsCompiled.getAndIncrement();
    }

    public int getNumEditsPreCompiled() {
        return _numEditsPreCompiled.get();
    }

    public void incrementNumEditsFoundOnClassPath() {
        _numEditsPreCompiled.getAndIncrement();
    }

    public Map<String, String> getReasonNotPreCompiled() {
        return _reasonNotPreCompiled;
    }

    public void setReasonNotPreCompiled(String validatorId, String reason) {
        _reasonNotPreCompiled.put(validatorId, reason);
    }
}