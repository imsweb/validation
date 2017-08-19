/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.validation;

import org.apache.commons.lang3.StringUtils;

public class ValidatorUtils {


    public static String createClassNameFromValidatorId(String id) {
        StringBuilder result = new StringBuilder();
        for (String s : StringUtils.split(id, "-"))
            result.append(StringUtils.capitalize(s));
        return result.toString();

    }

    public static String createMethodNameFromRuleid(String id) {
        return id.replace("-", "_");
    }
}
