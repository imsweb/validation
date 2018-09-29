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
    private Long _numRun = 1L;

    /**
     * Total time
     */
    private Long _totalTime = 0L;

    /**
     * Longest run time
     */
    private Long _longestTime = null;

    /**
     * Shortest run time
     */
    private Long _shortestTime = null;

    /**
     * Constructor.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @param id ID for this stat
     * @param time time for this stat
     */
    public EngineStats(String id, Long time) {
        _id = id;
        _numRun = 1L;
        _totalTime = time;
        _longestTime = time;
        _shortestTime = time;
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
    public void setNumRun(Long run) {
        _numRun = run;
    }

    /**
     * Getter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @return total time
     */
    public Long getTotalTime() {
        return _totalTime;
    }

    /**
     * Setter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @param time total time
     */
    public void setTotalTime(Long time) {
        _totalTime = time;
    }

    /**
     * Getter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @return longest time
     */
    public Long getLongestTime() {
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
    public Long getShortestTime() {
        return _shortestTime;
    }

    /**
     * Setter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @param time shortest time
     */
    public void setShortestTime(Long time) {
        _shortestTime = time;
    }

    /**
     * Reports that passed time to the passed <code>StatsDTO</code>
     * <p/>
     * Created on Dec 10, 2007 by depryf
     * @param dto <code>ValidatorStatsDto</code>, can't be null
     * @param time time
     */
    public static synchronized void reportRun(EngineStats dto, long time) {
        if (time >= 0) {
            dto.setNumRun(dto.getNumRun() + 1);
            dto.setTotalTime(dto.getTotalTime() + time);
            if (dto.getShortestTime() == null || dto.getShortestTime() > time)
                dto.setShortestTime(time);
            if (dto.getLongestTime() == null || dto.getLongestTime() < time)
                dto.setLongestTime(time);
        }
    }
}
