/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.validation.entities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * This class represent a table index in the Genedits framework.
 * <br/><br/>
 * When a value is found, the row index in the corresponding table is returned; this is a 0-based value that doesn't take into account the table headers
 * (so if value 0 is returned, it means the first row of data in the table).
 */
public class ContextTableIndex {

    // index name
    private String _name;

    // data structure used for unique keys
    private NavigableMap<String, Integer> _uniqueKeysData;

    // data structure used for non-unique keys
    private List<Pair<String, Integer>> _nonUniqueKeysData;

    /**
     * Constructor
     * @param name index name
     * @param table parent table
     * @param indexedColumns columns (header) that make up this index
     */
    public ContextTableIndex(String name, ContextTable table, List<String> indexedColumns) {
        _name = name;

        List<Integer> colIdx = new ArrayList<>();
        for (String column : indexedColumns) {
            int idx = table.getHeaders().indexOf(column.trim());
            if (idx == -1)
                throw new RuntimeException("Unable to find column \"" + column + "\" to index on table \"" + table.getName() + "\"");
            colIdx.add(idx);
        }

        Set<String> keysAdded = new HashSet<>();
        boolean keysAreUnique = true;
        Pattern pattern = Pattern.compile("\\s+$");

        _nonUniqueKeysData = new ArrayList<>();
        for (int rowIdx = 0; rowIdx < table.getData().size(); rowIdx++) {
            List<String> row = table.getData().get(rowIdx);
            // I *think* the index keys are right-trimmed in Genedits (I can't really prove it though)
            String key = pattern.matcher(StringUtils.join(colIdx.stream().map(row::get).toArray(String[]::new))).replaceAll("");
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
        else
            _nonUniqueKeysData.sort(Comparator.comparing((Function<Pair<String, Integer>, String>)Pair::getKey).thenComparingInt(Pair::getValue));
    }

    /**
     * Returns the row number of the requested value in the parent table, -1 if not found.
     * @param value value to look for
     * @return corresponding row number in the parent table, -1 if not found
     */
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

    /**
     * Returns the number of the first row that is equals or greater than the requested value, -1 if not found.
     * @param value value to look for
     * @return corresponding row number in the parent table, -1 if not found
     */
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

    /**
     * Returns true if the index has unique values, false otherwise.
     * @return true if the index has unique values, false otherwise.
     */
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
