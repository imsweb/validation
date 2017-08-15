/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */


import com.imsweb.validation.ValidatorContextFunctions
import groovy.transform.CompileStatic

@CompileStatic
class Validator_test_groovy {

    boolean fvCondition(Binding binding, Map<String, Object> Context, ValidatorContextFunctions Functions, Map<String, String> level1, Map<String, Object> level2) throws Exception {
        println 'Executing fvCondition'
        return level2.prop2 != 'IGNORED'
    }

    boolean fvRule1(Binding binding, Map<String, Object> Context, ValidatorContextFunctions Functions, Map<String, Object> level1) throws Exception {
        println 'Executing fvRule1'
        return level1.prop != Context.FV_CONTEXT1
    }

    boolean fvRule2(Binding binding, Map<String, Object> Context, ValidatorContextFunctions Functions, Map<String, Object> level1, Map<String, Object> level2) throws Exception {
        Functions.forceFailureOnProperty(binding, 'level2.otherProp')
        Functions.ignoreFailureOnProperty(binding, 'level2.prop')
        return level2.prop != Context.FV_CONTEXT1
    }

    boolean fvRule3(Binding binding, Map<String, Object> Context, ValidatorContextFunctions Functions, Map<String, Object> level1, Map<String, Object> level2, Map<String, Object> level3) throws Exception {
        println 'Executing fvRule3'
        return level3.prop != Context.FV_CONTEXT1
    }
}
