/*
 * Copyright (C) 2011 Information Management Services, Inc.
 */
package com.imsweb.validation.entities;

/**
 * This class encapsulates the notion of "version" for a validator, providing comparison features.
 * <p/>
 * A version has a strict format composed of three parts separated by dashes:
 * <ul>
 * <li>A prefix that is not taken into account when comparing two versions</li>
 * <li>A three digits (left 0-padded) major release number</li>
 * <li>A two digits (left 0-padded) minor release number</li>
 * </ul>
 * An example of a version would then be <b>SE12-001-01</b>.
 * <p/>
 * Created on Feb 23, 2011 by depryf
 */
public class ValidatorVersion implements Comparable<ValidatorVersion> {

    /** The raw representation of the version */
    protected String _rawString;

    /** Prefix part */
    protected String _prefix;

    /** Major number part */
    protected Integer _major;

    /** Minor number part */
    protected Integer _minor;

    /** Minor suffix part */
    protected Integer _suffix;

    /**
     * Constructor.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     */
    public ValidatorVersion(String rawString) {
        if (!validateVersionFormat(rawString))
            throw new RuntimeException("provided raw version does not have a valid format: " + rawString);

        String[] parts = rawString.split("\\-");

        _rawString = rawString;
        _prefix = parts[0];
        _major = Integer.valueOf(parts[1]);
        _minor = Integer.valueOf(parts[2]);
        if (parts.length == 4)
            _suffix = Integer.valueOf(parts[3]);
        else
            _suffix = 0;
    }

    /**
     * Getter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @return raw representation of this version
     */
    public String getRawString() {
        return _rawString;
    }

    /**
     * Getter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @return prefix part
     */
    public String getPrefix() {
        return _prefix;
    }

    /**
     * Getter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @return major part
     */
    public Integer getMajor() {
        return _major;
    }

    /**
     * Getter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @return minor part
     */
    public Integer getMinor() {
        return _minor;
    }

    /**
     * Getter.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @return suffix part
     */
    public Integer getSuffix() {
        return _suffix;
    }

    /* (non-Javadoc)
    *
    * Created on Feb 23, 2011 by depryf
    * @see java.lang.Object#toString()
    */
    @Override
    public String toString() {
        return _rawString;
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
        ValidatorVersion other = (ValidatorVersion)obj;
        if (_major == null) {
            if (other._major != null)
                return false;
        }
        else if (!_major.equals(other._major))
            return false;
        if (_minor == null) {
            if (other._minor != null)
                return false;
        }
        else if (!_minor.equals(other._minor))
            return false;
        if (_suffix == null) {
            if (other._suffix != null)
                return false;
        }
        else if (!_suffix.equals(other._suffix))
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
        result = prime * result + ((_major == null) ? 0 : _major.hashCode());
        result = prime * result + ((_minor == null) ? 0 : _minor.hashCode());
        result = prime * result + ((_suffix == null) ? 0 : _suffix.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * 
     * Created on Feb 23, 2011 by depryf
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(ValidatorVersion o) {
        int comp = getMajor().compareTo(o.getMajor());
        if (comp != 0)
            return comp;
        comp = getMinor().compareTo(o.getMinor());
        if (comp != 0)
            return comp;
        return getSuffix().compareTo(o.getSuffix());
    }

    /**
     * Validates the provided raw version.
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @param rawVersion raw version
     * @return true if it has a valid format, false otherwise.
     */
    public static boolean validateVersionFormat(String rawVersion) {
        if (rawVersion == null)
            return false;

        String[] parts = rawVersion.split("\\-");
        if (parts.length == 3)
            return parts[1].matches("\\d\\d\\d") && parts[2].matches("\\d\\d");
        if (parts.length == 4)
            return parts[1].matches("\\d\\d\\d") && parts[2].matches("\\d\\d") && parts[3].matches("\\d");
        return false;
    }

    /**
     * Compares the two raw versions; if one of them is null or does not have a proper format, it will be considered as earlier (older).
     * <p/>
     * Created on Feb 23, 2011 by depryf
     * @param rawVersion1 left-side raw version
     * @param rawVersion2 right-side raw versiohn
     * @return comparison results (negative, 0 or positive)
     */
    public static int compareVersions(String rawVersion1, String rawVersion2) {
        if (!validateVersionFormat(rawVersion1))
            return -1;
        if (!validateVersionFormat(rawVersion2))
            return 1;
        return new ValidatorVersion(rawVersion1).compareTo(new ValidatorVersion(rawVersion2));
    }
}
