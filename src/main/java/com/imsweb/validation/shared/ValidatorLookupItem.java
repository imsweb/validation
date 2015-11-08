/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation.shared;

/**
 * Interface for a lookup item (lookups are collection of lookup items).
 * <p/>
 * Created on Apr 5, 2011 by depryf
 * @author depryf
 */
public interface ValidatorLookupItem {

    /**
     * Returns the key corresponding to this lookup item.
     * <p/>
     * Created on Mar 28, 2010 by depryf
     * @return the key corresponding to this lookup item.
     */
    String getKey();

    /**
     * Sets the key corresponding to this lookup item.
     * <p/>
     * Created on Mar 28, 2010 by depryf
     * @param key key
     */
    void setKey(String key);

    /**
     * Returns the value corresponding to this lookup item.
     * <p/>
     * Created on Mar 28, 2010 by depryf
     * @return the value corresponding to this lookup item.
     */
    String getValue();

    /**
     * Sets the value corresponding to this lookup item.
     * <p/>
     * Created on Mar 28, 2010 by depryf
     * @param value value
     */
    void setValue(String value);
}
