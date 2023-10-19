/*
 * Copyright (C) 2011 Information Management Services, Inc.
 */
package com.imsweb.validation.internal.context;

public class JavaContextSymbol {

    public enum JavaContextSymbolType {
        /** LEFT_BRACKET */
        LEFT_BRACKET,
        /** RIGHT_BRACKET */
        RIGHT_BRACKET,
        /** STRING (renamed to STRING_VAL to avoid confusing with STRING JDK constant) */
        STRING_VAL,
        /** NUMBER */
        NUMBER,
        /** COMMA */
        COMMA,
        /** COLON */
        COLON,
        /** RANGE */
        RANGE,
        /** VARIABLE */
        VARIABLE
    }

    /** Symbol type */
    private final JavaContextSymbolType _type;

    /** Line number */
    private final int _line;

    /** Column number */
    private final int _column;

    /** Value (will be null for some symbol types) */
    private final Object _value;

    /** 
     * Constructor.
     * <p>
     * Created on Oct 7, 2011 by depryf
     * @param type symbol type
     * @param line line number
     * @param column column number
     */
    public JavaContextSymbol(JavaContextSymbolType type, int line, int column) {
        this(type, line, column, null);
    }

    /** 
     * Constructor.
     * <p>
     * Created on Oct 7, 2011 by depryf
     * @param type symbol type
     * @param line line number
     * @param column column number
     * @param value value of the symbol
     */
    public JavaContextSymbol(JavaContextSymbolType type, int line, int column, Object value) {
        _type = type;
        _line = line;
        _column = column;
        _value = value;
    }

    /**
     * @return Returns the _type.
     */
    public JavaContextSymbolType getType() {
        return _type;
    }

    /**
     * @return Returns the _line.
     */
    public int getLine() {
        return _line;
    }

    /**
     * @return Returns the _column.
     */
    @SuppressWarnings("unused")
    public int getColumn() {
        return _column;
    }

    /**
     * @return Returns the _value.
     */
    public Object getValue() {
        return _value;
    }

    /* (non-Javadoc)
     * 
     * Created on Oct 7, 2011 by depryf
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    /* (non-Javadoc)
     * 
     * Created on Oct 7, 2011 by depryf
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /* (non-Javadoc)
     * 
     * Created on Oct 7, 2011 by depryf
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        if (_value == null)
            return _type.toString();
        else
            return _type + ": " + _value;
    }
}