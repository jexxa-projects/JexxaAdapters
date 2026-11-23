[![Maven Central](https://img.shields.io/maven-central/v/io.jexxa.adapters/common-adapters)](https://central.sonatype.com/artifact/io.jexxa.adapters/common-adapters)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Build](https://github.com/jexxa-projects/JexxaAdapters/actions/workflows/mavenBuild.yml/badge.svg)](https://github.com/jexxa-projects/JexxaAdapters/actions/workflows/mavenBuild.yml)
[![CodeQL](https://github.com/jexxa-projects/JexxaAdapters/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/jexxa-projects/JexxaAdapters/actions/workflows/codeql-analysis.yml)
[![OpenSSF Best Practices](https://www.bestpractices.dev/projects/12683/badge)](https://www.bestpractices.dev/projects/12683)

# JexxaAdapters — Reusable Infrastructure for Ports & Adapters Applications

**JexxaAdapters** provides reusable Java infrastructure components for [Jexxa](https://github.com/jexxa-projects/Jexxa), [JLegMed](https://github.com/jexxa-projects/JLegMed), and other applications: infrastructure adapters, established integration patterns, and common utilities.

> Application-specific logic stays in your application. Technical details such as JDBC, JMS, or S3 stay in adapters that you do not have to write yourself.

The components support different architectural styles, from modular monoliths to microservices.

**Quick links:** [Quickstart](#quickstart) · [Modules](#modules) · [Capabilities](#capabilities) · [Build from source](#build-from-source)

## When to Use JexxaAdapters

Use JexxaAdapters if you

- build applications with [Jexxa](https://github.com/jexxa-projects/Jexxa) or [JLegMed](https://github.com/jexxa-projects/JLegMed) and need production-ready infrastructure for them,
- want to switch between JDBC, in-memory, and S3 persistence without changing application code, or
- need proven messaging patterns such as the transactional outbox or an idempotent consumer.


## Quickstart
**Requirements:**: 
- Java **25 or higher**
- Maven or Gradle

### Why Java 25?

Java 25 is not technically essential for JexxaAdapters. Requiring a current LTS release is a deliberate decision: the Jexxa Projects ecosystem is designed to connect legacy and modern systems, and any component that integrates legacy systems should itself be built on a modern language level. This keeps integration code maintainable and encourages updating legacy landscapes instead of conserving them.


### Add dependencies

You can add Jexxa Adapters to your project in two ways:

- **BOM (recommended):** Manage consistent versions across all adapter modules and omit explicit version numbers when declaring dependencies.
- **Individual dependencies:** Add specific adapter modules directly by specifying their versions.

#### Option 1: Use the BOM (recommended)

The `jexxa-adapters-bom` ensures compatible versions across all adapter modules so you only need to declare the modules you actually use without repeating version numbers.

<summary><b>Maven</b></summary>

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.jexxa.adapters</groupId>
            <artifactId>jexxa-adapters-bom</artifactId>
            <version>10.0.0-RC2</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>io.jexxa.adapters</groupId>
        <artifactId>adapter-rest</artifactId>
    </dependency>
    
    <!-- Additional modules ... -->

    <dependency>
        <groupId>io.jexxa.adapters</groupId>
        <artifactId>adapter-jdbc</artifactId>
    </dependency>
</dependencies>
```

<summary><b>Gradle (Groovy DSL)</b></summary>

```groovy
dependencies {
    implementation platform('io.jexxa.adapters:jexxa-adapters-bom:10.0.0-RC2')
    implementation 'io.jexxa.adapters:adapter-rest'
    implementation 'io.jexxa.adapters:adapter-jdbc'
}
```


#### Option 2: Add individual dependencies directly

If you only need a single adapter, you can declare it directly with an explicit version.

<details>
<summary><b>Maven</b></summary>

```xml
<dependencies>
    <dependency>
        <groupId>io.jexxa.adapters</groupId>
        <artifactId>adapter-rest</artifactId>
        <version>10.0.0-RC2</version>
    </dependency>
</dependencies>
```

</details>

<details>
<summary><b>Gradle (Groovy DSL)</b></summary>

```groovy
dependencies {
    implementation 'io.jexxa.adapters:adapter-rest:10.0.0-RC2'
}
```

</details>





## Modules

| Module               | Purpose                                                                                              |
|----------------------|------------------------------------------------------------------------------------------------------|
| `adapter-core`       | Shared infrastructure utilities, reusable adapters, integration patterns, facades, and health checks |
| `adapter-jdbc`       | JDBC-based persistence adapters and repository implementations                                       |
| `adapter-jms`        | JMS-based messaging adapters, message senders, and transactional outbox support                      |
| `adapter-rest`       | REST-based adapters for HTTP communication                                                           |
| `adapter-s3`         | S3-compatible storage adapters                                                                       |
| `adapter-esp`        | Event-based messaging adapters for kafka                                                             |      
| `adapter-esp-test`   | Simplify testing for event-based applications                                                        |      
| `adapter-micrometer` | Application metrics collection and Micrometer monitoring adapters for Prometheus                     |      
| `jexxa-adapters-bom` | Centralized dependency version management for all Jexxa adapter modules                              |


## Examples

- [Driving adapter tests](adapter-rest/src/test/java/io/jexxa/adapters/rest/RESTfulRPCAdapterIT.java )
- [JexxaTutorials](https://github.com/jexxa-projects/JexxaTutorials): complete applications using these adapters
- [JLegMed](https://github.com/jexxa-projects/JLegMed): reusable processing components and integrations

### Where it fits

```mermaid
flowchart LR
    subgraph App["Your application"]
        CORE["Business core<br/>incl. driven ports (interfaces)"]
    end
    subgraph Adapters["JexxaAdapters"]
        REPO["Repositories<br/>JDBC / IMDB / S3"]
        PATTERNS["Integration patterns<br/>Transactional Outbox, Idempotent Consumer"]
    end
    subgraph Infra["Infrastructure"]
        DB[("Database / object storage")]
        MQ["Message broker"]
    end
    REPO -.->|implements| CORE
    PATTERNS -.->|implements| CORE
    REPO --> DB
    PATTERNS --> MQ
```

All arrows show source-code dependencies: the adapters depend on the ports of your business core, never the other way round.

## Capabilities

### Infrastructure Adapters

Reusable adapters connect applications to infrastructure technologies without coupling application logic to their technical details.

- **Repositories:** implementations for JDBC, in-memory databases (IMDB), and S3, following the [Repository pattern](https://martinfowler.com/eaaCatalog/repository.html)



### Integration Patterns

- **Transactional Outbox:** coordinates database updates with eventual message publication without distributed transactions. See the [Transactional Outbox pattern](https://microservices.io/patterns/data/transactional-outbox.html).
- **Idempotent Consumer:** helps applications handle duplicate message deliveries. See the [Idempotent Consumer pattern](https://microservices.io/patterns/communication-style/idempotent-consumer.html).


### Common Utilities

- **Technology facades:** wrappers around JDBC, JMS, S3, JSON, and logging APIs that simplify adapter implementations
- **Health checks:** components for reporting application and infrastructure health
- **In-process publish/subscribe (`InProcessPubSub`):** lightweight, synchronous, type-based messaging within a single JVM process. It provides no persistence, asynchronous delivery, or retries.




## Build from Source

Clone the repository and build it with Maven:

```shell
mvn clean install
```

Some integration tests require the services defined in the [developer stack](deploy/developerStack.yml), including PostgreSQL and an ActiveMQ broker. Start them before running those tests:

```shell
docker compose -f deploy/developerStack.yml up -d
```


## Contributing

Contributions and pull requests are welcome. For substantial changes, please open an issue first to discuss the proposed approach. Include or update tests when changing behavior.


## FLOSS Commitment

This project is built and maintained using **Free/Libre Open Source Software (FLOSS)**. The build process (Maven), software bill of materials generation (CycloneDX), and signing (GPG) use open-source tooling.

## Copyright and License

- Code: [Apache License 2.0](LICENSE) ([plain-language overview](https://www.tldrlegal.com/license/apache-license-2-0-apache-2-0))
- Documentation: [Creative Commons Attribution 4.0 International](https://creativecommons.org/licenses/by/4.0/)
- © 2023–2026 Michael Repplinger