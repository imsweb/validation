package com.imsweb.validation;

/**
 * Use an instance of this class to provide initialization options to the engine.
 */
public class InitializationOptions {

    // whether or not the engine needs to keep track of edits statistics (defaults to false)
    private boolean _engineStatsEnabled;

    // the number of threads the engine can use to compile the edits (defaults to 2)
    private int _numCompilationThreads;

    // whether or not the engine should use pre-compiled edits if available (defaults to true)
    private boolean _preCompiledEditsEnabled;

    /**
     * Constructor.
     */
    public InitializationOptions() {
        _engineStatsEnabled = false;
        _numCompilationThreads = 2;
        _preCompiledEditsEnabled = true;
    }

    public void enableEngineStats() {
        _engineStatsEnabled = true;
    }

    public boolean isEngineStatsEnabled() {
        return _engineStatsEnabled;
    }

    public void setNumCompilationThreads(int n) {
        if (n < 1 || n > 32)
            throw new IllegalStateException("Number of threads must be between 1 and 32");
        _numCompilationThreads = n;
    }

    public int getNumCompilationThreads() {
        return _numCompilationThreads;
    }

    public void disablePreCompiledEdits() {
        _preCompiledEditsEnabled = false;
    }

    public boolean isPreCompiledEditsEnabled() {
        return _preCompiledEditsEnabled;
    }
}
