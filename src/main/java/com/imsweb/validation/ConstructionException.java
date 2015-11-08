/*
 * Copyright (C) 2004 Information Management Services, Inc.
 */
package com.imsweb.validation;

/**
 * Exception thrown to indicate an error constructing an object.
 * <p/>
 * Created on Jul 7, 2011 by depryf
 * @author depryf
 */
public class ConstructionException extends Exception {

    /** Class UID */
    private static final long serialVersionUID = 1L;

    /** First rule involved in the circular dependency (will be null for non-dependency exceptions) */
    private String _leftDependencyRule = null;

    /** Second rule involved in the circular dependency (will be null for non-dependency exceptions or self-dependency exception) */
    private String _rightDependencyRule = null;

    /**
     * Constructor.
     * <p/>
     * Created on Jul 7, 2011 by depryf
     * @param message exception message
     */
    public ConstructionException(String message) {
        super(message);
    }

    /**
     * Constructor.
     * <p/>
     * Created on Jul 7, 2011 by depryf
     * @param parent parent exception
     */
    public ConstructionException(Throwable parent) {
        super(parent);
    }

    /**
     * Constructor.
     * <p/>
     * Created on Jul 7, 2011 by depryf
     * @param message exception message
     * @param parent parent exception
     */
    public ConstructionException(String message, Throwable parent) {
        super(message, parent);
    }

    /**
     * Constructor for self-dependencies.
     * <p/>
     * Created on Jul 7, 2011 by depryf
     * @param message exception message
     * @param leftDependencyRule rule ID causing the dependency exception
     */
    public ConstructionException(String message, String leftDependencyRule) {
        super(message);

        _leftDependencyRule = leftDependencyRule;
    }

    /**
     * Constructor for dependency exceptions.
     * <p/>
     * Created on Jul 7, 2011 by depryf
     * @param message exception message
     * @param leftDependencyRule first rule ID causing the dependency exception
     * @param rightDependencyRule second rule ID causing the dependency exception
     */
    public ConstructionException(String message, String leftDependencyRule, String rightDependencyRule) {
        super(message);

        _leftDependencyRule = leftDependencyRule;
        _rightDependencyRule = rightDependencyRule;
    }

    /**
     * Returns the left rule ID that caused the dependency, null if the exception is not due to a dependeny issue.
     * <p/>
     * Created on Jul 7, 2011 by depryf
     * @return the left rule ID that caused the dependency, null if the exception is not due to a dependeny issue
     */
    public String getLeftDependencyRule() {
        return _leftDependencyRule;
    }

    /**
     * Returns the right rule ID that caused the dependency, null if the exception is not due to a dependeny issue or for self-dependencies.
     * <p/>
     * Created on Jul 7, 2011 by depryf
     * @return the right rule ID that caused the dependency, null if the exception is not due to a dependeny issue
     */
    public String getRightDependencyRule() {
        return _rightDependencyRule;
    }
}
