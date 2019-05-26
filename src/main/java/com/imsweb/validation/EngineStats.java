/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation;

/**
 * This class encapsulates the notion of a single statistics (for example the statistics for a single edit).
 * <p/>
 * Created on Feb 23, 2011 by depryf
 */
public class EngineStats {

    /**
     * ID for this DTO (can represent anything - polisher ID, registry coding task ID, auto-cons rule ID, etc...)
     */
    private String _id;

    /**
     * Number of run
     */
    private long _numRun;

    /**
     * Total time
     */
    private long _totalTime;

    /**
     * Longest run time
     */
    private long _longestTime;

    /**
     * Shortest run time
     */
    private long _shortestTime;

    /**
     * Constructor.
     */
    public EngineStats(String id) {
        _id = id;
        _numRun = 0;
        _totalTime = 0;
        _longestTime = 0;
        _shortestTime = 0;
    }

    public void reportStat(long duration) {
        _numRun++;
        _totalTime += duration;
        if (duration > _longestTime)
            _longestTime = duration;
        if ((duration < _shortestTime || _shortestTime == 0) && duration > 0)
            _shortestTime = duration;
    }

    /**
     * Getter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @return ID
     */
    public String getId() {
        return _id;
    }

    /**
     * Getter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @return number of run
     */
    public long getNumRun() {
        return _numRun;
    }

    /**
     * Getter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @return total time
     */
    public long getTotalTime() {
        return _totalTime;
    }

    /**
     * Getter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @return longest time
     */
    public long getLongestTime() {
        return _longestTime;
    }

    /**
     * Getter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @return shortest time
     */
    public long getShortestTime() {
        return _shortestTime;
    }
}
