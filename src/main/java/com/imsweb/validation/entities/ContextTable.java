/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.validation.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;

/**
 * This class represent a table in the Genedits framework.
 */
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

        // this might be a bit slow, but oh well; go over all values and compute longest length for each column
        List<Integer> maxLength = new ArrayList<>(_headers.size());
        for (String _header : _headers)
            maxLength.add(_header.length());
        for (List<String> row : _data)
            for (int col = 0; col < row.size(); col++)
                maxLength.set(col, Math.max(maxLength.get(col), row.get(col).length()));

        StringBuilder buf = new StringBuilder();
        buf.append(IntStream.range(0, _headers.size()).mapToObj(i -> StringUtils.rightPad("-", maxLength.get(i), "-")).collect(Collectors.joining("|"))).append("\n");
        buf.append(IntStream.range(0, _headers.size()).mapToObj(i -> StringUtils.rightPad(_headers.get(i), maxLength.get(i))).collect(Collectors.joining("|"))).append("\n");
        buf.append(IntStream.range(0, _headers.size()).mapToObj(i -> StringUtils.rightPad("-", maxLength.get(i), "-")).collect(Collectors.joining("|"))).append("\n");
        for (List<String> row : _data)
            buf.append(IntStream.range(0, row.size()).mapToObj(i -> StringUtils.rightPad(row.get(i), maxLength.get(i))).collect(Collectors.joining("|"))).append("\n");

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
