/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.validation.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class ContextTableIndex {

    private String _name;

    private List<String> _indexedColumns;

    private NavigableMap<String, Integer> _uniqueKeysData;

    private List<Pair<String, Integer>> _nonUniqueKeysData;

    public ContextTableIndex(String name, ContextTable table, List<String> columnsToIndex) {

        List<Integer> colIdx = new ArrayList<>();
        for (String column : columnsToIndex) {
            int idx = table.getHeaders().indexOf(column);
            if (idx == -1)
                throw new RuntimeException("Unable to find column \"" + column + "\" to index on table \"" + table.getName() + "\"");
            colIdx.add(idx);
        }

        Set<String> keysAdded = new HashSet<>();
        boolean keysAreUnique = true;

        _nonUniqueKeysData = new ArrayList<>();
        for (int rowIdx = 0; rowIdx < table.getData().size(); rowIdx++) {
            List<String> row = table.getData().get(rowIdx);
            String key = StringUtils.join(colIdx.stream().map(row::get).collect(Collectors.toList()).toArray(new String[0]));
            if (keysAdded.contains(key))
                keysAreUnique = false;
            keysAdded.add(key);
            _nonUniqueKeysData.add(new ImmutablePair<>(key, rowIdx));
        }

        if (keysAreUnique) {
            _uniqueKeysData = new TreeMap<>();
            for (Pair<String, Integer> pair : _nonUniqueKeysData)
                _uniqueKeysData.put(pair.getKey(), pair.getValue());
            _nonUniqueKeysData = null;
        }
        else {
            _nonUniqueKeysData.sort((o1, o2) -> {
                int result = o1.getKey().compareTo(o2.getKey());
                return result == 0 ? o1.getValue().compareTo(o2.getValue()) : result;
            });
        }
    }

    public int find(String value) {
        int result = -1;

        if (_uniqueKeysData != null)
            result = _uniqueKeysData.getOrDefault(value, -1);
        else {
            for (Pair<String, Integer> pair : _nonUniqueKeysData) {
                int comp = value.compareTo(pair.getKey());
                if (comp == 0) {
                    result = pair.getValue();
                    break;
                }
                else if (comp < 0)
                    break; // values in the list are sorted, so we can stop the iteration sooner...
            }
        }

        return result;
    }

    @Override
    public String toString() {
        return _name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContextTableIndex that = (ContextTableIndex)o;
        return Objects.equals(_name, that._name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_name);
    }
}
