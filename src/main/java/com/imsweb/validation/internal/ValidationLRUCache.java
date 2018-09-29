/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.validation.internal;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class ValidationLRUCache<A, B> extends LinkedHashMap<A, B> {

    private static final long serialVersionUID = 1L;

    private final int _maxEntries;

    public ValidationLRUCache(int maxEntries) {
        super(maxEntries + 1, 1.0f, true);
        _maxEntries = maxEntries;
    }

    @Override
    protected boolean removeEldestEntry(Entry<A, B> eldest) {
        return size() > _maxEntries;
    }
}
