import com.imsweb.validation.functions.MetafileContextFunctions
import groovy.transform.CompileStatic

@CompileStatic
class NaaccrTranslatedEdits {

boolean NAACCR_00861(Binding binding, Map<String, Object> context, MetafileContextFunctions functions, List<Map<String, String>> untrimmedlines, Map<String, String> untrimmedline) throws Exception {
functions.GEN_RESET_LOCAL_CONTEXT(binding);

functions.log('running NAACCR-00861')

    def ValidatePath = {
        return true
    }


if (functions.GEN_EMPTY(untrimmedline.nameLast))
    return false
if (!functions.GEN_MATCH(untrimmedline.nameLast, "([A-Za-z](([A-Za-z])|(\\s)|(\\-)|('))*)"))
    return false
return ValidatePath()

}

}