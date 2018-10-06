package com.imsweb.validation;

public class InitializationOptions {

    private boolean _engineStatsEnabled;

    public InitializationOptions() {
        _engineStatsEnabled = false;
    }

    public void enableEngineStats() {
        _engineStatsEnabled = true;
    }

}
