# Validation Framework

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
[Version](VERSION) | 
[Changelog](changelog.txt) | 
[Snapshots](http://cilantro.imsweb.com:8080/nexus/content/repositories/snapshots/com/imsweb/validation/) | 
[Releases](http://cilantro.imsweb.com:8080/nexus/content/repositories/releases/com/imsweb/validation/)
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Group ID: ```com.imsweb```
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Artifact ID: ```validation```

This framework allows edits to be defined using the Groovy scripting language, and to execute them on all type of different data.

The **XmlValidatorFactory** class can be used to read/write edits from/to XML data files.
 
The **ValidationEngine** is the main class of the framework and is used to execute edits. Here is an example of how the engine can be used:

```java
    public static void main(String[] args) throws Exception {

        long start = System.currentTimeMillis();
        System.out.println("Strarting to run Validation Engine Demo...");

        // before using any of the methods from the validation module, we need to initialize the services and context methods;
        // this simple example will use the default implementations, but those classes are designed to be extended to customize the services
        // and add more application-specific context functions...
        ValidatorServices.initialize(new ValidatorServices());
        ValidatorContextFunctions.initialize(new ValidatorContextFunctions());
        System.out.println("Initialized services...");

        // create a rule (rules must be contained in a validator, so we have to create that too)
        Rule r = new Rule();
        r.setRuleId(ValidatorServices.getInstance().getNextRuleSequence()); // this is an internal ID
        r.setId("testing-rule");
        r.setJavaPath("record");
        r.setMessage("Just for testing...");
        r.setExpression("return record.primarySite != 'C809'"); // this is Groovy code
        Validator v = new Validator();
        v.setValidatorId(ValidatorServices.getInstance().getNextValidatorSequence()); // this is an internal ID
        v.setId("testing-validator");
        v.setName("Test");
        v.getRules().add(r);
        r.setValidator(v);
        ValidationEngine.initialize(v);
        System.out.println("Initialized validation engine with " + v.getRules().size() + " edit...");

        // let's create a fake record to be validated
        System.out.println("Calling validate() method...");
        Map<String, Object> record = new HashMap<String, Object>();
        record.put("primarySite", "C809");
        if (!ValidationEngine.validate(new SimpleMapValidatable("ID", "record", record)).isEmpty())
            System.out.println("  > was expecting a failure for unknown site and got one  :-)");
        else
            System.err.println("  > was expecting a failure for unknown site but didn't get one  :-(");

        // let's change the site and make sure there is no more failure
        record.put("primarySite", "C123");
        if (ValidationEngine.validate(new SimpleMapValidatable("ID", "record", record)).isEmpty())
            System.out.println("  > was expecting no failure for known site and didn't get one  :-)");
        else
            System.err.println("  > was expecting no failure for known site but got one  :-(");

        System.out.println("Demo ran in " + (SeerUtils.formatTime(System.currentTimeMillis() - start)) + "; good bye...");
    }
```

Here is the result of running that code:
```asciidoc
Strarting to run Validation Engine Demo #1...
Initialized services...
Initialized validation engine with 1 edit...
Calling validate() method...
  > was expecting a failure for unknown site and got one  :-)
  > was expecting no failure for known site and didn't get one  :-)
Demo ran in 2 seconds; good bye...
```

The framework makes no assumption on the format of the data; it just expects a **Validatable** object. It is the caller's responsibility 
to wrap their own entities into a **Validatable**. For most purposes, the **SimpleMapValidatable** should be enough; there is also a 
**SimpleNaaccrLinesValidatable** specifically designed to work with NAACCR data files...

The framework also contains functionalities to load and execute edit tests. Here is an example of a multi-threaded unit test using the SEER edits:
```java
    public void testSeerEdits() throws Exception {
        
        // load SEER edits
        Validator validator = XmlValidatorFactory.loadValidatorFromXml(currentThread().getContextClassLoader().getResource("seer-edits.xml"));
        ValidationEngine.addValidator(new EditableValidator(validator));

        // laod and run the tests
        List<String> errors = Collections.synchronizedList(new ArrayList<String>());
        ExecutorService service = Executors.newFixedThreadPool(4);
        for (RuleTest test : XmlValidatorFactory.loadTestsFromXml(currentThread().getContextClassLoader().getResource("seer-edits-tests.xml")).getTests().values())
            service.submit(new EditTestExecutor(test, errors));
        service.shutdown();
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