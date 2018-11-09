# Java Validation Framework

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.imsweb/validation/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.imsweb/validation)

This framework allows edits to be defined in [Groovy](http://www.groovy-lang.org/) and to be executed on various data types.

## Features

* Edits are written in Groovy, a rich java-based scripting language.
* Large tables can be provided to the edits as contexts and shared among several edits.
* Edits can be loaded from an XML file, or defined programmatically.
* Any type of data can be validated; it just needs to implement the *Validatable* interface.
* The execution of edits is thread safe and the engine can be used in a heavily threaded application.
* Edits can be dynamically added, modified or removed in the engine.
* The engine supports an edit testing framework with unit tests written in Groovy as well.

## Download

The library is available on [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.imsweb%22%20AND%20a%3A%22validation%22).

To include it to your Maven or Gradle project, use the group ID `com.imsweb` and the artifact ID `validation`.

You can check out the [release page](https://github.com/imsweb/validation/releases) for a list of the releases and their changes.

## Core concepts

**ValidationEngine**

This is the class responsible for executing the edits. It needs to be initialized before that can happen.

**XmlValidatorFactory**

Provides utility methods for reading/writing edits XML files.

**Validator**

A logical grouping of edits (for example, the SEER edits, or the NAACCR edits). This is the entities used to initialize the validation engine.

**Rule**

Edits are called rules in this framework.

Rules represents a (usually small) piece of logic that returns a boolean value (true if the edit passes, false if it fails).

Rules must define a Java path which represents the logical entity the edits run on. 

or example, if a rule defines "record" as its Java path, the rule will run on record objects and will access the properties by calling "record.property". 
On the other hand, if the path is defined as "patient.tumor.treatment", the rule will run on every treatment of every tumor of a given patient object 
and it will access the properties by calling "treatment.property" or "tumor.property" or "patient.property'.

The concept of Java path is tied to the Validatable class which is addressed hereunder.

**Context**

Validators can also contain contexts; those are usually large data structures (list, maps, etc...) that are accessed by more than one edit. 
Edits can reference contexts using the prefix *"Context."*.

**Validatable**

An interface used to tell the engine how to execute the edits on specific data types. This allows very different types
(like a NAACCR line, a Java tumor object or a record from a data entry form) to be wrapped into a validatable and handled by the framework.

A Validatable is logically linked to the rules by its Java path. Every rule (and condition) define a Java path; a Validatable defines a root Java path. 
The engine uses that information to know which rules to execute for a given Validatable.

Example 1: a rule defines the Java path "record" and another one defines "patient", the SimpleMapValidatable is used (it defines its root Java path as "record"); 
the first rule will be executed when that SimpleMapValidatable is validated, the second rule won't.

Example 2: a rule defines the Java path "patient.tumor.treatment"; a customized Validatable defining a root Java path "patient" is used. That rule will be 
executed when that Validatable is validated. The Validatable will be responsible for building the data required by the rules at each level of the path. 
So it will build the "patient" data and the engine will run the "patient" rules; it will then build the "tumor" data ()for each tumor on the patient) and the 
engine will run the "patient.tumor" rules on that data; and finally it will build the "treatment" data (for each treatment of each tumor) and the 
engine will run the "patient.tumor.treatment" rules on that data.

**ValidatorServices**

Some services are made available to the edits (like accessing a lookup, or a configuration variable); different applications
provide those features differently, therefore the services need to be overridden if the default behavior is not the one needed.

**ValidatorContextFunctions**

The methods from this class are made available to the edits; they can be called using the prefix *"Function."*.
The default implementation provides very basic methods but it can be initialized with a more complex implementation if needed.

If the edits have been translated from a Genedits metafile, the MetafileContextFunctions class should be used instead.
The initialization of that class requires an instance of the following staging algorithm:
- CS (https://github.com/imsweb/staging-algorithm-cs)

If the edits need to access staging information (to execute SEER edits for example), the StagingContextFunctions class should be used for initialization.
The initialization of that class requires an instance of the following staging algorithms:
- CS (https://github.com/imsweb/staging-algorithm-cs)
- TNM (https://github.com/imsweb/staging-algorithm-tnm)
- EOD (https://github.com/imsweb/staging-algorithm-eod-public)

## Usage

### Reading a file of edits

Here is an example of a very simplified XML file:

```xml
<validator id="my-edits">
    <rules>
        <rule id="my-edit" java-path="record">
            <expression>return record.primarySite != 'C809'</expression>
            <message>Primary Site cannot be C809.</message>
        </rule>
    </rules>
</validator>
```

And here is the code that can be used to initialize the validation engine from that file:

```java
File file = new File("my-edits.xml")
Validator v = ValidationXmlUtils.loadValidatorFromXml(file);
ValidationEngine.initialize(v);
```

### Creating an edit programmatically

This example shows how to initialize the validation engine from edits created within the code.

```java
// create the rule
Rule r = new Rule();
r.setRuleId(ValidatorServices.getInstance().getNextRuleSequence());
r.setId("my-edit");
r.setJavaPath("record");
r.setMessage("Primary Site cannot be C809.");
r.setExpression("return record.primarySite != 'C809'");

// create the validator (a wrapper for all the rules that belong together)
Validator v = new Validator();
v.setValidatorId(ValidatorServices.getInstance().getNextValidatorSequence())
v.setId("my-edits");
v.getRules().add(r);
r.setValidator(v);

// initialize the engine
ValidationEngine.getInstance().initialize(v);
```

### Executing edits on a data file

This example shows how to validate a data file and print the edit failures; it uses the [layout](https://github.com/imsweb/layout)
framework to read a NAACCR file and translate it into a map of properties that the validation engine can handle. This example assumes
the engine has already been initialized with specific edits.

```java
File dataFile = new File("my-data.txd.gz");
NaaccrLayout layout = (NaaccrLayout)LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_16_ABSTRACT);
for (<Map<String, String> rec : layout.readAllRecords(dataFile)) {

    // this is how the engine knows how to validate the provided object
    Validatable validatable = new SimpleNaaccrLinesValidatable(rec)

    // go through the failures and display them
    Collection<RuleFailure> failures = ValidationEngine.getInstance().validate(validatable);
    for (RuleFailure failure : failures)
        System.out.println(failure.getMessage());
}
```

## Optimizing loading an executing edits

Several mechanisms are in place to speed up the initialization and the execution of the edits.

### Speed up the initialization by enabling multi-threaded compilation

Groovy edits need to be compiled before being executed by the engine. That step can be slow for big edits files,
but it can be optimized by using multi-threaded compilation:
```java
InitializationOptions options = new InitializationOptions();
options.setNumCompilationThreads(4);
ValidationEngine.getInstance().initialize(options, myValidator);
```
A value of 4 will usually work well for optimizing the compilation although it depends on the available resources. The default is to use 2 threads.

### Speed up the initialization and execution by using pre-compiled/pre-parsed edits

The engine supports registering pre-compiled edits; those edits will completely bypass the parsing and compilation steps. The edits will also need to be strongly typed in their 
syntax, allowing them to run much faster than regular Groovy edits.

Pre-compiled edits is an advanced feature; the engine supports it by default but creating the edits is much more work than maintaining them in an XML file.
See the "runtime" package for more information, in particular the RuntimeEdits and RuntimeUtils classes.

## About SEER

This library was developed through the [SEER](http://seer.cancer.gov/) program.

The Surveillance, Epidemiology and End Results program is a premier source for cancer statistics in the United States.
The SEER program collects information on incidence, prevalence and survival from specific geographic areas representing
a large portion of the US population and reports on all these data plus cancer mortality data for the entire country.