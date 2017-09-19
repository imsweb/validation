/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.validation.entities;

import java.util.List;
import java.util.Objects;

public class ContextTable {

    private String _name;

    private List<String> _headers;

    private List<List<String>> _data;

    public ContextTable(String name, List<List<String>> data) {
        _name = name;
        _headers = data.get(0);
        _data = data.subList(1, data.size());
    }

    public String getName() {
        return _name;
    }

    public List<String> getHeaders() {
        return _headers;
    }

    public List<List<String>> getData() {
        return _data;
    }

    @Override
    public String toString() {
        return _data.toString(); // TODO format data
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
