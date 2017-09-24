/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.validation.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
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
            int idx = table.getHeaders().indexOf(column.trim());
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

    public int findFloor(String value) {
        int result = -1;

        // if the value is smaller than the smaller index key, return not found (-1)
        // if the value is greater than the greatest index key, return the last index value
        // if the value is equals to an index key, return that index value
        // if a value is not equals to any index keys (but within the range of the keys), return the value of the first key that is greater than the value

        if (_uniqueKeysData != null) {
            if (value.compareTo(_uniqueKeysData.firstKey()) >= 0) {
                Entry<String, Integer> entry = _uniqueKeysData.floorEntry(value);
                if (entry != null)
                    result = entry.getValue();
                else if (value.compareTo(_uniqueKeysData.lastKey()) > 0)
                    result = _uniqueKeysData.lastEntry().getValue();
            }
        }
        else {
            if (value.compareTo(_nonUniqueKeysData.get(0).getKey()) >= 0) {
                for (int indexIdx = 0; indexIdx < _nonUniqueKeysData.size(); indexIdx++) {
                    Pair<String, Integer> pair = _nonUniqueKeysData.get(indexIdx);
                    int comp = value.compareTo(pair.getKey());
                    if (comp == 0) {
                        result = pair.getValue();
                        break;
                    }
                    else if (comp < 0) {
                        result = _nonUniqueKeysData.get(indexIdx - 1).getValue();
                        break;
                    }
                }
                if (result == -1)
                    if (value.compareTo(_nonUniqueKeysData.get(_nonUniqueKeysData.size() - 1).getKey()) > 0)
                        result = _nonUniqueKeysData.get(_nonUniqueKeysData.size() - 1).getValue();
            }
        }

        return result;
    }

    public boolean hasUniqueKeys() {
        return _uniqueKeysData != null;
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
