/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO used to provide documenatation about a particular context method.
 * <p/>
 * Created on Apr 5, 2011 by depryf
 * @author depryf
 */
@SuppressWarnings("unused")
public class ContextFunctionDocDto {

    /** Method name */
    private String _methodName;

    /** Method description */
    private String _description;

    /** Example(s) */
    private String _example;

    /** Method parameters */
    private List<String> _params;
    private List<String> _paramNames;

    /**
     * Default constructor.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     */
    public ContextFunctionDocDto() {
        _params = new ArrayList<>();
        _paramNames = new ArrayList<>();
    }

    /**
     * Getter for the method name.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @return method name
     */
    public String getMethodName() {
        return _methodName;
    }

    /**
     * Setter for the method name.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param methodName method name
     */
    public void setMethodName(String methodName) {
        _methodName = methodName;
    }

    /**
     * Getter for the description.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @return description
     */
    public String getDescription() {
        return _description;
    }

    /**
     * Setter for the description.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param description description
     */
    public void setDescription(String description) {
        _description = description;
    }

    /**
     * Getter for the example(s).
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @return example(s)
     */
    public String getExample() {
        return _example;
    }

    /**
     * Setter for the example(s).
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param example example(s)
     */
    public void setExample(String example) {
        _example = example;
    }

    /**
     * Getter for the parameters.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @return parameters
     */
    public List<String> getParams() {
        return _params;
    }

    /**
     * Setter for the parameters.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param params parameters
     */
    public void setParams(List<String> params) {
        _params = params;
    }

    /**
     * Getter for the parameters.
     * <p/>
     * Created on Oct 12, 2012 by murphyr
     * @return parameter names
     */
    public List<String> getParamNames() {
        return _paramNames;
    }

    /**
     * Setter for the parameters.
     * <p/>
     * Created on Oct 12, 2012 by murphyr
     * @param paramNames parameter names
     */
    public void setParamNames(List<String> paramNames) {
        _paramNames = paramNames;
    }
}
