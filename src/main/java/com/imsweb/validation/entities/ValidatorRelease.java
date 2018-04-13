/*
 * Copyright (C) 2011 Information Management Services, Inc.
 */
package com.imsweb.validation.entities;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * An object encapsulating the information related to a particular release of an XML edits file (represented by a {@link Validator} Object).
 * <p/>
 * Created on Feb 23, 2011 by depryf
 */
public class ValidatorRelease implements Comparable<ValidatorRelease> {

    /**
     * Version for this release
     */
    protected ValidatorVersion _version;

    /**
     * Date of the release
     */
    protected Date _date;

    /**
     * Optional description for this release
     */
    protected String _description;

    /**
     * Constructor.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @param version release version
     * @param date release date
     * @param description release description
     */
    public ValidatorRelease(ValidatorVersion version, Date date, String description) {
        _version = version;
        _date = date;
        _description = description;
    }

    /**
     * Getter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @return the <code>ValidatorVersion</code> for this release.
     */
    public ValidatorVersion getVersion() {
        return _version;
    }

    /**
     * Getter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @return the <code>Date</code> for this release.
     */
    public Date getDate() {
        return _date;
    }

    /**
     * Getter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @return the description for this release.
     */
    public String getDescription() {
        return _description;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(_version.getRawString());
        if (_date != null)
            buf.append(" (").append(new SimpleDateFormat("dd/MM/yyyy").format(_date)).append(")");
        return buf.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidatorRelease that = (ValidatorRelease)o;
        return Objects.equals(_version, that._version);
    }

    @Override
    public int hashCode() {

        return Objects.hash(_version);
    }

    @Override
    public int compareTo(ValidatorRelease o) {
        return _version.compareTo(o.getVersion());
    }
}
