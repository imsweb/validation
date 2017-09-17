## Validation Framework Version History

**Version 1.8**

- Increased the engine version to 5.8
- Added support for pre-parsed and pre-compiled edits (optimizations).
- Added support for new context types 'table' and 'table-index-def' that will be used for Genedits framework.

**Version 1.7**

- Increased the engine version to 5.7
- Added support for new SQLLOOKUP and SQLRANGELOOKUP Genedits functions.
- Added new log methods in context functions, a framework can provide a log implementation in the services.
- Added support for edit/set tags (used in the Genedits framework).
- Added support for timing out execution of edits (disabled by default).

**Version 1.6.3**

- Updated staging client from version 2.4 to version 2.5; this contains the new TNM 1.4 algorithm. 
- SimpleNaaccrLinesValidatable will convert the sex value to SSF25 when the SSF25 discriminator is missing for Peritoneum and Peritoneum Female Gen so that the CS schema will be found.

**Version 1.6.2**

- Updated staging client from version 2.3 to version 2.4; this contains the new TNM 1.3 algorithm.

**Version 1.6.1**

- Fixed a bug that prevented the root attributes to be found if the file contained large text before the root tag.

**Version 1.6**

- Added new method in XmlValidatorFactory to extract all attributes from main Validator tag.
- Removed Joda library dependency, replaced by new Java 8 date framework.
- Removed BeanUtils library dependency.
- Updated staging client from version 2.1.1 to version 2.3.
- Updated Groovy library from version 2.4.6 (indy) to version 2.4.7 (indy).
- This library now requires Java 8 at minimum.

**Version 1.5.9**

- The used properties won't be recalculated if the assigned expression is the same as the previous value.
- The returned sequences from the validator services are now Long instead of native long.

**Version 1.5.8**

- Increased the engine version to 5.6 (version 5.6 is needed for ensuring correct results when running translated metafile edits).
- Fixed another bug in the GenEDITS INLIST function implementation resulting in some edits failing when they should pass.

**Version 1.5.7**

- Added staging context methods getCsSchemaId() and getTnmSchemaId() that return a schema ID given a map of inputs.

**Version 1.5.6**

- Increased the engine version to 5.5 (version 5.5 is needed for ensuring correct results when running translated metafile edits).
- Fixed a bug in the GenEDITS INLIST function implementation resulting in some edits failing when they should pass.

**Version 1.5.5**

- Fixed a bug in the GenEDITS INLIST function implementation resulting in a out-of-bound exception.
- Removed the deprecated staging schema name constants from the Validatable class.

**Version 1.5.4**

- Increased the engine version to 5.4 (version 5.4 is needed for edits using staging schema ID instead of name).
- Deprecated the constants related to staging schema name in Validatable; those have been replaced by staging ID constants.

**Version 1.5.3**

- Increased the engine version to 5.3 (version 5.3 is needed for edits using new TNM context methods).
- Added support for treating a WARNING in a translated edits as a failing state instead of just ignoring it.
- Updated staging client from version 2.1 to version 2.1.1.
- Updated Groovy library from version 2.4.4 (indy) to version 2.4.6 (indy).
- Updated Joda library (for date utilities) from version 2.8.1 to version 2.9.3.
- Updated XStreams library (for XML utilities) from version 1.4.7 to version 1.4.9.
- Updated Apache commons-lang library from version 3.3.2 to version 3.4.

**Version 1.5.2**

- Changed staging context method isAcceptableTnmCode() to not always return false for a null value.

**Version 1.5.1**

- Added support for TNM staging in the context methods and the simple NAACCR line validatable; using TNM 1.1 algorithm.

**Version 1.5**

- Increased the engine version to 5.2.
- The context functions and validator services are now lazily initialized with default implementation.
- Added support for multi-threading rule parsing/compilation; see XmlValidatorFactory.enableMultiThreadedParsing() and ValidationEngine.enableMultiThreadedCompilation().
- Improved multi-threading support; the validate methods will now block if the state of the engine is being changed, instead of returning no failures.
- Removed deprecated CStage context methods from the ValidatorContextFunctions class.
- Split context functions, this change requires doing the initialization a bit differently: to run translated and/or SEER edits, use the MetafileContextFunctions; to use only SEER edits, use the StagingContextFunctions.
- Replaced JAXB by XStream for all XML operations.
- Updated Groovy library from version 2.4.3 to version 2.4.4; now using "indy" version of the library.
- Updated Staging client library from version 1.4.2 to version 1.4.6.
- This library now requires Java 7 at minimum.

**Version 1.4.1**

- Fixed a bug with assigning CStage schema in SimpleNaaccrValidatable.
- Increased the engine version to 5.1 (this should have been done it in the previous release).

**Version 1.4**

- Updated Groovy library from 2.2.2 to 2.4.3.
- Updated Staging client library from 1.2 to 1.4.2.
- Updated Commons Lang library from 3.3.2 to 3.4.
- Updated Joda Time library from 2.6 to 2.8.1.

**Version 1.3**

- Updated Staging client to version 1.2, which included minor bug fixes.

**Version 1.2**

- Now using a pure java-based CStage implementation.

**Version 1.1**
    
- Now using normal distribution of Groovy instead of the "groovy-all" one.
- Updated commons-lang library from 2.x to 3.x.

**Version 1.0**

- Validation framework split from SEER*Utils into its own project.

**Legacy**

 - [SEER*Utils v4.9  ]  Moved all validation classes from "com.imsweb.seerutils.validator" to "com.imsweb.validation".
 - [SEER*Utils v4.8.3]  Fixed the demo programs that can be run on the command line.
 - [SEER*Utils v4.8.2]  Fixed a bug in the testing framework making the validation of a single edit very slow.
 - [SEER*Utils v4.8.1]  Fixed an issue where "forced rules" (see ValidationEngine.validate(validatable, rule)) were sometimes not correctly executed and returned no results.
 - [SEER*Utils v4.8.1]  Added support of an undocumented Genedits method "SET_ERROR"; seems like NCDB-00326 was the only edit to use that method.
 - [SEER*Utils v4.8.1]  Fixed a bug in the parsing of the edits code that would result in some properties not being correctly reported when an edit fails.
 - [SEER*Utils v4.8  ]  Replaced rulesets by conditions and categories; applied other minor changes to the framework.
 - [SEER*Utils v4.6  ]  The ValidatorContextFunctions constructor now takes an optional CStage version; if not provided, the latest available CStage version will be used.
 - [SEER*Utils v4.6  ]  Fixed a bug in INLIST related to ranges containing non-numeric characters.
 - [SEER*Utils v4.6  ]  Added support for reporting deleted edits in the edits XML file; this change doesn't break any API.
 - [SEER*Utils v4.5.6]  Fixed a bug in VAL context method for the translated edits where a result of -1 would be returned instead of 0 for a bad incoming value.
 - [SEER*Utils v4.5.6]  Added a new field "originalResult" on RuleFailure to handle case when translated edits fail because of a flag.
 - [SEER*Utils v4.5.6]  Fixed date values not properly formatted in error messages.
 - [SEER*Utils v4.5.6]  Fixed a problem in GEN_MATCH related to blank values.
 - [SEER*Utils v4.5.6]  Now using <BLANK> instead of <blank> when replacing a blank value in a message.
 - [SEER*Utils v4.5.6]  Optimized calls to LOOKUP and ILOOKUP.
 - [SEER*Utils v4.5.5]  Fixed a bug in the GEN_VALID_DATE_IOP method related to valid days.
 - [SEER*Utils 4.5.5 ]  Tweaked GEN_LOOKUP and GEN_RLOOKUP to accept a null table, allowing more optimization in the translated edits XML files.
 - [SEER*Utils v4.5.5]  Added support for extra error messages and information messages; mainly used for translated edits.
 - [SEER*Utils v4.5.4]  Fixed an exception in GEN_LOOKUP for table using integers instead of strings.
 - [SEER*Utils v4.5.3]  Added an optional list of edit IDs to execute, instead of allowing only a list of IDs to ignore for main validate method.
 - [SEER*Utils v4.5.3]  Now displaying a warning instead of throwing an exception if an edit history date can't be read successfully from an XML file.
 - [SEER*Utils v4.5.3]  Added support for defining a minimum validation engine version in the XML files.
 - [SEER*Utils v4.5.3]  Properties used inside an inner-method of an edit were not properly parsed.
 - [SEER*Utils v4.5.3]  Fixed a trimming issue in GEN_LOOKUP and GEN_ILOOKUP; this affected only translated edits.
 - [SEER*Utils v4.5.2]  Changed the XML validator factory to sort the rule histories when creating an XML file.
 - [SEER*Utils v4.4.2]  Added support in Genedits context methods for DT_TODAY constant (implemented as a method call).
 - [SEER*Utils v4.4.1]  Fixed SAVE_TEXT and SAVE_ERROR_TEXT Genedits context methods that were not properly setting the failing flag.
 - [SEER*Utils v4.4  ]  Optimized the Genedits context functions.
 - [SEER*Utils v4.3.2]  Fixed the XML validator factory to use the OS line separator.
 - [SEER*Utils v4.3.1]  Added support for loading gzipped URL/Files.
 - [SEER*Utils v4.3.1]  Removed default severity on the rules.
 - [SEER*Utils v4.3  ]  Context entries should now be referenced using the "Context." prefix; for now the old way (no prefix) is still supported.
 - [SEER*Utils v4.2.8]  Renamed some fields in the LayoutInfo object.
 - [SEER*Utils v4.2.8]  Improved documentation of the fetchLookup method in the validation engine.
 - [SEER*Utils v4.2.7]  Changed method addContext() so it requires the context key to be unique within the validator, not within the entire engine.
 - [SEER*Utils v4.2.6]  Added new method on EditsSet class to get all the referenced validator IDs.
 - [SEER*Utils v4.2.6]  Failing properties are now returned even if the edit failed because of an exception.
 - [SEER*Utils v4.2.6]  Fixed a bug in GEN_INLIST context function related to blank values.
 - [SEER*Utils v4.1  ]  Fixed and improved Validator Context methods documentation.
 - [SEER*Utils v4.0.1]  Fixed a bug with CStage variables in SimpleNaaccrValidatable.
 - [SEER*Utils v4.0  ]  Allowed extra suffix for validator versions.
 - [SEER*Utils v4.0  ]  Added support for closures in edits tests.
 - [SEER*Utils v3.1  ]  Map values for failing edits wasn't not correctly cloned for SEER*DMS edits.
 - [SEER*Utils v3.0  ]  Now sorting edits test before creating XML file.
 - [SEER*Utils v2.1  ]  Added ValidatorContextFunctions to testing framework.
 - [SEER*Utils v2.1  ]  Added new OBSOLETE codes to ValidatorContextFunctions.
 - [SEER*Utils v2.1  ]  Improved testing framework for SEER*DMS.
 - [SEER*Utils v2.1  ]  Redirected output from running tests on an edit and made it available in a variable.
 - [SEER*Utils v2.1  ]  Edits translation - various issues.
 - [SEER*Utils v2.1  ]  Implemented our own parsing of the java contexts.
 - [SEER*Utils v2.0  ]  Edits translation - bugs in regex translations.
 - [SEER*Utils v1.3  ]  History and Dependencies are now being sroted before being written to the XML file.
 - [SEER*Utils v1.0  ]  Improved validation engine memory usage.
 - [SEER*Utils v1.0  ]  Added a method to return all aliases.
 - [SEER*Utils v1.0  ]  Added a method in the Validation Engine to return a specific rule.
 - [SEER*Utils v1.0  ]  Moved validation engine out of SEER*DMS, into a new shared library.
