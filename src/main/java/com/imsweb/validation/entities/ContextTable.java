/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.validation.entities;

import java.util.List;
import java.util.Objects;

/**
 * This class represent a table in the Genedits framework.
 */
public class ContextTable {

    // table name
    private String _name;

    // table headers
    private List<String> _headers;

    // table content
    private List<List<String>> _data;

    /**
     * Constructor
     * @param name table name
     * @param data table content (first row (index 0) are the headers)
     */
    public ContextTable(String name, List<List<String>> data) {
        _name = name;
        _headers = data.get(0);
        _data = data.subList(1, data.size());
    }

    /**
     * Retursn the table name.
     * @return table name
     */
    public String getName() {
        return _name;
    }

    /**
     * Returns the table headers.
     * @return table headers
     */
    public List<String> getHeaders() {
        return _headers;
    }

    /**
     * Returns the table content.
     * @return table content
     */
    public List<List<String>> getData() {
        return _data;
    }

    @Override
    public String toString() {
        return _name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContextTable that = (ContextTable)o;
        return Objects.equals(_name, that._name);
    }

    @Override
    public int hashCode() {

        return Objects.hash(_name);
    }
}
