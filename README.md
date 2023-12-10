[![Maven Central](https://img.shields.io/maven-central/v/io.jexxa.common/common-adapters)](https://maven-badges.herokuapp.com/maven-central/io.jexxa.common/common-adapters)[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)


[![Maven Build](https://github.com/jexxa-projects/JexxaAdapters/actions/workflows/mavenBuild.yml/badge.svg)](https://github.com/jexxa-projects/JexxaAdapters/actions/workflows/mavenBuild.yml)
[![CodeQL](https://github.com/jexxa-projects/JexxaAdapters/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/jexxa-projects/JexxaAdapters/actions/workflows/codeql-analysis.yml)
# Common Adapters

This library contains common technology adapters 
used by the [Jexxa](https://www.jexxa.io) and [JLegMed](https://github.com/jexxa-projects/JLegMed) framework. 
These adapters are based on typical microservices and messaging patterns to simplify implementing microservices. Implemented patterns are: 
* [Repository](https://martinfowler.com/eaaCatalog/repository.html) that isolates the data layer from the rest of the app on object/entity level 
* [Transactional Outbox Pattern](https://microservices.io/patterns/data/transactional-outbox.html) to atomically update the database and send messages to a message broker when 2FC is not an option
* [Idempotent Consumer](https://microservices.io/patterns/communication-style/idempotent-consumer.html) to handle duplicate messages correctly
* Facade to Java JDBC and JMS API to simplify the implementation of additional adapters   


## Requirements

*   Java 17+ installed
*   IDE with maven support



## Use the library 
### Add dependencies
Maven:
```xml
<dependencies>
    <dependency>
        <groupId>io.jexxa.common</groupId>
        <artifactId>common-adapters</artifactId>
        <version>0.5.0</version>
    </dependency>
</dependencies>
```

Gradle:

```groovy
compile "io.jexxa.common:common-adapters:0.5.0"
``` 
### Examples
To see how to use this library, please refer to: 
* Tests in [adapter](common-adapters/src/test/java/io/jexxa/common/drivingadapter)
* Filter plugins [JLegMed](https://github.com/jexxa-projects/JLegMed)

## Build the library
This section describes how to build the library by yourself, if you want to contribute.
### Requirements 
*   A locally running [developer stack](deploy/developerStack.yml) providing a Postgres database, and ActiveMQ broker

### Build 
*   Checkout the new project in your favorite IDE

*   Without running integration tests:
    ```shell
    mvn clean install 
    ```

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.

## Copyright and license

Code and documentation copyright 2023 Michael Repplinger. Code released under the [Apache 2.0 License](LICENSE). Docs released under [Creative Commons](https://creativecommons.org/licenses/by/3.0/).
