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
    public EngineStats() {
        _numRun = 1L;
        _totalTime = 0L;
        _longestTime = 0L;
        _shortestTime = 0L;
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
     * Setter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @param id ID
     */
    public void setId(String id) {
        this._id = id;
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
     * Setter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @param run longest run
     */
    public void setNumRun(long run) {
        _numRun = run;
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
     * Setter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @param time total time
     */
    public void setTotalTime(long time) {
        _totalTime = time;
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
     * Setter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @param time longest time
     */
    public void setLongestTime(long time) {
        _longestTime = time;
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

    /**
     * Setter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @param time shortest time
     */
    public void setShortestTime(long time) {
        _shortestTime = time;
    }
}
