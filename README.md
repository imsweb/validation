# Java Validation Framework

[![Build Status](https://travis-ci.org/imsweb/validation.svg?branch=master)](https://travis-ci.org/imsweb/validation)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.imsweb/validation/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.imsweb/validation)

This framework allows edits to be defined using the Groovy scripting language, and to execute them on different types data.

## Features

* Edits are written in Groovy, a rich java-based scripting language.
* Large tables can be provided to the edits as contexts and shared among several edits.
* Edits can be loaded from an XML file, or defined programmatically.
* Any type of data can be validated; it just needs to implement the *Validatable* interface.
* The validation engine executing edits is thread safe.
* Edits can be dynamically added, modified or removed in the engine.
* The engine supports an edits testing framework with unit tests written in Groovy as well.

## Download

This library will be available in Maven Central soon.

## Usage

### Reading a file of edits

```java
File file = new File("my-edits.xml")
Validator v = XmlValidatorFactory.loadValidatorFromXml(file);
ValidationEngine.initialize(v);
```

### Creating an edit programmatically

```java
// create the rule
Rule r = new Rule();
r.setRuleId(ValidatorServices.getInstance().getNextRuleSequence());
r.setId("my-rule");
r.setJavaPath("record");
r.setMessage("Primary Site cannot be C809.");
r.setExpression("return record.primarySite != 'C809'");

// create the validator (a wrapper for all the rules that belong together)
Validator v = new Validator();
v.setValidatorId(ValidatorServices.getInstance().getNextValidatorSequence())
v.setId("my-rules");
v.getRules().add(r);
r.setValidator(v);

// initialize the engine
ValidationEngine.initialize(v);
```

### Executing edits on a data file

This example uses the layout framework to read NAACCR files and translate them into maps of properties.

```java
File dataFile = new File("my-data.txd.gz");
Layout layout = LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_16_ABSTRACT);
for (<Map<String, String> rec : (RecordLayout)layout.readAllRecords(new File("my_file.txt"))) {
    Collection<RuleFailure> failures = ValidationEngine.validate(new SimpleNaaccrLinesValidatable(rec));
    for (RuleFailure failure : failures)
        System.out.println(failure.getMessage());
}
```

### Using the edits testing framework

This example shows how to write a multi-threaded unit test for some XML edits:

```java
    public void testSeerEdits() throws Exception {

        // load and run the tests
        List<String> errors = Collections.synchronizedList(new ArrayList<String>());
        ExecutorService service = Executors.newFixedThreadPool(4);
        for (RuleTest test : XmlValidatorFactory.loadTestsFromXml(currentThread().getContextClassLoader().getResource("seer-edits-tests.xml")).getTests().values())
            service.submit(new EditTestExecutor(test, errors));
        service.shutdown();

        // wait until all the threads are done
        service.awaitTermination(1, TimeUnit.MINUTES);
        if (!errors.isEmpty()) {
            StringBuilder failures = new StringBuilder();
            for (String error : errors)
                failures.append(error);
            fail("\n\nDone running SEER edits tests; found following problems:\n\n" + failures);
        }
    }

    // used to run a single edit test
    private static class EditTestExecutor implements Runnable {

        private RuleTest _test;
        private List<String> _errors;

        public EditTestExecutor(RuleTest test, List<String> errors) {
            _test = test;
            _errors = errors;
        }

        @Override
        public void run() {
            try {
                for (List<RuleTestResult> list : _test.executeTest().values())
                    for (RuleTestResult r : list)
                        if (!r.isSuccess())
                            _errors.add("  " + _test.getTestedRuleId() + ": " + r + "\n");
            }
            catch (Exception e) {
                _errors.add("  " + _test.getTestedRuleId() + ": [test threw an exception] - " + e.getMessage() + "\n");
            }
        }
    }
```