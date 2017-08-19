/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.validation;

import java.util.concurrent.atomic.AtomicInteger;

public class ValidationEngineInitializationStats {

    private long _initializationDuration;

    private AtomicInteger _numEditsLoaded;

    private AtomicInteger _numEditsCompiled;

    private AtomicInteger _numEditsFoundOnClassPath;

    public ValidationEngineInitializationStats() {
        _initializationDuration = 0L;
        _numEditsLoaded = new AtomicInteger();
        _numEditsCompiled = new AtomicInteger();
        _numEditsFoundOnClassPath = new AtomicInteger();
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

    public int getNumEditsFoundOnClassPath() {
        return _numEditsFoundOnClassPath.get();
    }

    public void incrementNumEditsFoundOnClassPath() {
        _numEditsFoundOnClassPath.getAndIncrement();
    }
}
