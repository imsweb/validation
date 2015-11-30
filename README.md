# Java Validation Framework

[![Build Status](https://travis-ci.org/imsweb/validation.svg?branch=master)](https://travis-ci.org/imsweb/validation)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.imsweb/validation/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.imsweb/validation)

This framework allows edits to be defined in [Groovy](http://www.groovy-lang.org/) and to be executed on various data types.

## Features

* Edits are written in Groovy, a rich java-based scripting language.
* Large tables can be provided to the edits as contexts and shared among several edits.
* Edits can be loaded from an XML file, or defined programmatically.
* Any type of data can be validated; it just needs to implement the *Validatable* interface.
* The validation engine executing edits is thread safe.
* Edits can be dynamically added, modified or removed in the engine.
* The engine supports an edit testing framework with unit tests written in Groovy as well.

## Download

The library is available on [Maven Central](http://search.maven.org/); type `g:"com.imsweb" AND a:"validation"` in the search to find it.

You can check out the [release page](https://github.com/imsweb/validation/releases) for a list of the releases and their changes.

## Core concepts

- **ValidationEngine**: this is the class responsible for executing the edits. It needs to be initialized before that can happen.

- **XmlValidatorFactory**: provides utility methods for reading/writing edits XML files.

- **Validator**: a logic grouping of edits (for example, the SEER edits, or the NAACCR edits). This is the entities used to
initialize the validation engine.

- **Rule**: edits are called rules in this framework.

- **Context**: validators can also contain contexts; those are usually large data structures (list, maps, etc...) that are accessed by more than one edit.
Edits can reference contexts using the prefix *"Context."*.

- **Validatable**: an interface used to tell the engine how to execute the edits on specific data types. This allows very different types
(like a NAACCR line, a Java tumor object or a record from a data entry form) to be wrapped into a validatable and handled by the framework.

- **ValidatorServices**: some services are made available to the edits (like accessing a lookup, or a configuration variable); different applications
provide those features differently, therefore the services need to be overridden if the default behavior is not the one needed.

- **ValidatorContextFunctions**: the methods from this class are made available to the edits; they can be called using the prefix *"Function."*.
The default implementation provides very basic methods but it can be initialized with a more complex implementation if needed.
If the edits need to access staging information, the StagingContextFunctions class should be used for initialization.
If the edits have been translated from a Genedits metafile, the MetafileContextFunctions class should be used instead.

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
Validator v = XmlValidatorFactory.loadValidatorFromXml(file);
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
ValidationEngine.initialize(v);
```

### Executing edits on a data file

This example shows how to validate a data file and print the edit failures; it uses the [layout](https://github.com/imsweb/layout)
framework to read a NAACCR file and translate it into a map of properties that the validation engine can handle.

```java
File dataFile = new File("my-data.txd.gz");
Layout layout = LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_16_ABSTRACT);
for (<Map<String, String> rec : (RecordLayout)layout.readAllRecords(dataFile)) {

    // this is how the engine knows how to validate the provided object
    Validatable validatable = new SimpleNaaccrLinesValidatable(rec)

    // go through the failures and display them
    Collection<RuleFailure> failures = ValidationEngine.validate(validatable);
    for (RuleFailure failure : failures)
        System.out.println(failure.getMessage());
}
```
