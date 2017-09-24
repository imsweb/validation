/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.validation.entities;

import java.util.ArrayList;
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

        // this might be a bit slow, but oh well; go over all values and compute longest values
        List<Integer> maxLength = new ArrayList<>(_headers.size());
        for (int i = 0; i < _headers.size(); i++)
            maxLength.set(i, _headers.get(i).length());
        for (List<String> row : _data)
            for (int col = 0; col < row.size(); col++)
                maxLength.set(col, Math.max(maxLength.get(col), row.get(col).length()));

        StringBuilder buf = new StringBuilder();
        buf.append("[");
        //for (String s : _headers)
        //    buf.append(StringUtils.join(_headers.stream().map(s -> StringUtils.rightPad(s, )), '|'));

        return buf.toString();
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
