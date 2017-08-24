/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.validation;

import java.util.concurrent.atomic.AtomicInteger;

public class ValidationEngineInitializationStats {

    private long _initializationDuration;

    private AtomicInteger _numEditsLoaded;

    private AtomicInteger _numEditsDynamicallyCompiled;

    private AtomicInteger _numEditsStaticallyCompiled;

    public ValidationEngineInitializationStats() {
        _initializationDuration = 0L;
        _numEditsLoaded = new AtomicInteger();
        _numEditsDynamicallyCompiled = new AtomicInteger();
        _numEditsStaticallyCompiled = new AtomicInteger();
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

    public int getNumEditsDynamicallyCompiled() {
        return _numEditsDynamicallyCompiled.get();
    }

    public void incrementNumEditsCompiled() {
        _numEditsDynamicallyCompiled.getAndIncrement();
    }

    public int getNumEditsStaticallyCompiled() {
        return _numEditsStaticallyCompiled.get();
    }

    public void incrementNumEditsFoundOnClassPath() {
        _numEditsStaticallyCompiled.getAndIncrement();
    }
}
