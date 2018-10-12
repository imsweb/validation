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
