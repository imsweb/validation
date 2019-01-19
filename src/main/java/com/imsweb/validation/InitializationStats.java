/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.validation;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class InitializationStats {

    public static final String REASON_NOT_PROVIDED = "pre-compiled edits not provided";
    public static final String REASON_DIFFERENT_VERSION = "pre-compiled validator has version {0} but application expected {1}";
    public static final String REASON_DISABLED = "pre-compiled edits are disabled";

    private long _initializationDuration;

    private AtomicInteger _numEditsLoaded;

    private AtomicInteger _numEditsCompiled;

    private AtomicInteger _numEditsPreCompiled;

    private Map<String, InitializationStatsPerValidator> _validatorStats;

    public InitializationStats() {
        _initializationDuration = 0L;
        _numEditsLoaded = new AtomicInteger();
        _numEditsCompiled = new AtomicInteger();
        _numEditsPreCompiled = new AtomicInteger();
        _validatorStats = new ConcurrentHashMap<>();
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

    public void incrementNumEditsLoaded(String validatorId) {
        _numEditsLoaded.getAndIncrement();
        _validatorStats.computeIfAbsent(validatorId, InitializationStatsPerValidator::new).incrementNumEditsLoaded();
    }

    public int getNumEditsCompiled() {
        return _numEditsCompiled.get();
    }

    public void incrementNumEditsCompiled(String validatorId) {
        _numEditsCompiled.getAndIncrement();
        _validatorStats.computeIfAbsent(validatorId, InitializationStatsPerValidator::new).incrementNumEditsCompiled();
    }

    public int getNumEditsPreCompiled() {
        return _numEditsPreCompiled.get();
    }

    public void incrementNumEditsPreCompiled(String validatorId) {
        _numEditsPreCompiled.getAndIncrement();
        _validatorStats.computeIfAbsent(validatorId, InitializationStatsPerValidator::new).incrementNumEditsPreCompiled();
    }

    public void setReasonNotPreCompiled(String validatorId, String reason) {
        _validatorStats.computeIfAbsent(validatorId, InitializationStatsPerValidator::new).setReasonNotPreCompiled(reason);
    }

    public List<InitializationStatsPerValidator> getValidatorStats() {
        return _validatorStats.values().stream().sorted(Comparator.comparing(InitializationStatsPerValidator::getValidatorId)).collect(Collectors.toList());
    }
}
