/*
 * Copyright (C) 2011 Information Management Services, Inc.
 */
package com.imsweb.validation.entities;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * An object encapsulating the information related to a particular release of an XML edits file (represented by a {@link Validator} Object).
 * <p/>
 * Created on Feb 23, 2011 by depryf
 */
public class ValidatorRelease implements Comparable<ValidatorRelease> {

    /** Version for this release */
    protected ValidatorVersion _version;

    /** Date of the release */
    protected Date _date;

    /** Optional description for this release */
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

    /* (non-Javadoc)
     * 
     * Created on Feb 23, 2011 by depryf
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(_version.getRawString());
        if (_date != null)
            buf.append(" (").append(new SimpleDateFormat("dd/MM/yyyy").format(_date)).append(")");
        return buf.toString();
    }

    /* (non-Javadoc)
     * 
     * Created on Feb 23, 2011 by depryf
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ValidatorRelease other = (ValidatorRelease)obj;
        if (_version == null) {
            if (other._version != null)
                return false;
        }
        else if (!_version.equals(other._version))
            return false;
        return true;
    }

    /* (non-Javadoc)
     * 
     * Created on Feb 23, 2011 by depryf
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + ((_version == null) ? 0 : _version.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * 
     * Created on Feb 23, 2011 by depryf
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(ValidatorRelease o) {
        return getVersion().compareTo(o.getVersion());
    }
}
