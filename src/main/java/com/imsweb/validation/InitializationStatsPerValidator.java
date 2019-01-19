/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.validation;

import java.util.concurrent.atomic.AtomicInteger;

public class InitializationStatsPerValidator {

    private String _validatorId;

    private AtomicInteger _numEditsLoaded;

    private AtomicInteger _numEditsCompiled;

    private AtomicInteger _numEditsPreCompiled;

    private String _reasonNotPreCompiled;

    public InitializationStatsPerValidator(String validatorId) {
        _validatorId = validatorId;
        _numEditsLoaded = new AtomicInteger();
        _numEditsCompiled = new AtomicInteger();
        _numEditsPreCompiled = new AtomicInteger();
    }

    public String getValidatorId() {
        return _validatorId;
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

    public void incrementNumEditsPreCompiled() {
        _numEditsPreCompiled.getAndIncrement();
    }

    public String getReasonNotPreCompiled() {
        return _reasonNotPreCompiled;
    }

    public void setReasonNotPreCompiled(String reason) {
        _reasonNotPreCompiled = reason;
    }
}
