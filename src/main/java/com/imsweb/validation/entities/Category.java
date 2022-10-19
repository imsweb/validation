/*
 * Copyright (C) 2007 Information Management Services, Inc.
 */
package com.imsweb.validation.entities;

import java.util.Objects;

/**
 * A <code>Category</code> allows to logically group some of the rules from a validator.
 * <p/>
 * The validation engine doesn't use categories to execute the rules; this is used only to display the rules using categories.
 * <p/>
 * Created on Nov 9, 2007 by depryf
 */
public class Category {

    /**
     * DB ID for this category
     */
    protected Long _categoryId;

    /**
     * Application ID (same as a display ID) for this category
     */
    protected String _id;

    /**
     * Name for this category
     */
    protected String _name;

    /**
     * Description for this category
     */
    protected String _description;

    /**
     * Validator under which this category is registered
     */
    protected Validator _validator;

    /**
     * Getter for the category persistence ID. Must be unique within the validation engine.
     * <p/>
     * In a system persisting the categories in a database, that would represent a database primary key.
     * In a system using only memory objects loaded from XML, it should be assigned using the
     * getNextCategorySequence() method in <code>ValidationServices</code>.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return the category persistence ID
     */
    public Long getCategoryId() {
        return _categoryId;
    }

    /**
     * Setter for the category persistence ID. Must be unique within the validation engine.
     * <p/>
     * In a system persisting the categories in a database, that would represent a database primary key.
     * In a system using only memory objects loaded from XML, it should be assigned using the
     * getNextCategorySequence() method in <code>ValidationServices</code>.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param categoryId category persistence ID
     */
    public void setCategoryId(Long categoryId) {
        _categoryId = categoryId;
    }

    /**
     * Getter for the ID. The category ID must be unique within the validation engine.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return the category ID
     */
    public String getId() {
        return _id;
    }

    /**
     * Setter for the ID. The category ID must be unique within the validation engine.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param id the category ID
     */
    public void setId(String id) {
        _id = id;
    }

    /**
     * Getter for the name.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return the category name
     */
    public String getName() {
        return _name;
    }

    /**
     * Setter for the name.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param name the category name
     */
    public void setName(String name) {
        _name = name;
    }

    /**
     * Getter for the description.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return the category description
     */
    public String getDescription() {
        return _description;
    }

    /**
     * Setter for the description.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param description the category description
     */
    public void setDescription(String description) {
        _description = description;
    }

    /**
     * Getter for the parent <code>Validator</code>.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @return a <code>Validator</code>
     */
    public Validator getValidator() {
        return _validator;
    }

    /**
     * Setter for the parent <code>Valdidator</code>.
     * <p/>
     * Created on Mar 10, 2011 by depryf
     * @param validator the parent <code>Validator</code>.
     */
    public void setValidator(Validator validator) {
        _validator = validator;
    }

    @Override
    public String toString() {
        return getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category)) return false;
        Category category = (Category)o;
        if (_categoryId != null && category._categoryId != null)
            return Objects.equals(_categoryId, category._categoryId);
        return Objects.equals(_id, category._id);
    }

    @Override
    public int hashCode() {
        if (_categoryId != null)
            return Objects.hash(_categoryId);
        return Objects.hash(_id);
    }
}
