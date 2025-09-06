# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).
## \[2.4.0] - 2025-09-06
### Added
* JexxaContext: Method to validate used technology stacks

## \[2.3.0] - 2025-07-24
### Added
* Method to get the class of a serializedLambda

### Changed
* Method `methodNameFromLambda` only returns the method name 

## \[2.2.0] - 2025-07-23
### Added
* Methods to enable/disable JDBC connection sharing
* Added utility method to validate a JDBC connection

### Fixed
* Updated dependencies
* Disabled JDBC connection sharing by default -> This feature can only be used in single threaded applications

## \[2.1.7] - 2025-06-25
### Fixed
* Updated dependencies

## \[2.1.6] - 2025-05-26
### Fixed
* Updated dependencies
* Moved deploy process from legacy OSS deploy server to new central sonatype

## \[2.1.5] - 2025-04-15
### Fixed
* Updated dependencies

## \[2.1.4] - 2025-03-18
### Fixed
* Updated dependencies

## \[2.1.3] - 2025-02-04
### Fixed
* Updated dependencies

## \[2.1.2] - 2024-12-17
### Fixed
* Updated dependencies

## \[2.1.1] - 2024-11-01
### Fixed
* Updated dependencies

## \[2.1.0] - 2024-10-12
### Added
Added:
* `SharedInvocationHandler` that does not init a transaction
* `TransactionalInvocationHandler` that replaces `DefaultInvocationHandler`
* Possibility to define a new default `InvocationHandler`
* Possibility to define a `InvocationHandler` for a specific object

### Changed
* `DefaultInvocationHandler` is declared deprecated and is replaced by `TransactionalInvocationHandler`

### Fixed
* `PreparedStatement` now uses '?' as placeholder instead of '?::<type> expect as for type JSON. This fixes a compatibility issue with databases such as oracle db that did not support type conversion as part of placeholder.

* Updated dependencies

## \[2.0.6] - 2024-10-05
### Fixed
* Updated dependencies

## \[2.0.5] - 2024-08-31
### Fixed
* Updated dependencies

## \[2.0.4] - 2024-08-01
### Fixed
* Updated dependencies
* Validate JMS properties before creating a JMSConnection. At least Driver and URL must be specified.

## \[2.0.3] - 2024-05-29
### Fixed
* Updated dependencies

## \[2.0.2] - 2024-05-13
### Fixed
* Updated dependencies

## \[2.0.1] - 2024-04-12
### Fixed
* Updated dependencies

## \[2.0.0] - 2024-03-16
### Removed
* Deprecated classes `RepositoryManager`, `ObjectStoreManager`, and `MessageSenderManager`. Instead, `RepositoryFactory`, `ObjectStoreFactory`, and `MessageSenderFactory` must be used.

### Fixed
* Updated dependencies


## \[1.3.0] - 2024-02-25
### Added
* `TransactionalOutboxSender`: Properties `outbox.table` to define the used table name. See [here](common-adapters/src/test/resources/application.properties) for an example.

* Added: `RepositoryFactory`, `ObjectStoreFactory`, and `MessageSenderFactory` as replacement for `RepositoryManager`, `ObjectStoreManager`, and `MessageSenderManager`

### Deprecated
* Declared `RepositoryManager`, `ObjectStoreManager`, and `MessageSenderManager` as deprecated because they are factories that is not obvious due to their names and APIs

### Fixed
* `TransactionalOutputSender`: The transactional outbox sender can now be used without a running transaction.  
* Updated dependencies

## \[1.2.0] - 2024-02-17
### Added
* Functional interfaces `SerializableBiConsumer` and `SerializableBiFunction`
* Method to query method name from lambda

## \[1.1.0] - 2024-02-14
### Added
* JDBC-Adapters:
  * `JDBCConnection`: added methods to create SQL builder without prefix `create`
  * `JDBCConnection`: declared methods with `create` prefix as deprecated
* JMS-Adapters:
  * `TextMessageListener`: Added listener class that converts content of byte and text messages into a string  

## \[1.0.1] - 2024-01-04
### Fixed
-  Updated dependencies
-  Code cleanup reported from static code analysis (documentation, corrected exception handling, ...)

## \[1.0.0] - 2023-12-15
- First public release

## \[0.5.0] - 2023-12-10 (RC-2 for release 1.0.0)
### Added 
### Changed
- adapter-api:
  - This release is now part of this multi-maven project JexxaAdapters. The reason for this is that the number of changes should be small because we belong to standard APIs   
  - The current version is based on jexxa-adapter-api which becomes now obsolete
  - The artifactID changed from jexxa-adapter-api to adapter-api 
  - To indicate this change, adapter-api starts with the same version number as Jexxa Adapters 

### Added
- Health checks   

## \[0.4.0] - 2023-12-09 (RC-1 for release 1.0.0)
### Added
- Added ApplicationBanner to show application-specific information at startup 

### Changed
- JDBC properties are now accessed via methods so that an application can add specific prefix 

## \[0.3.0] - 2023-12-05 (RC for release 1.0.0)
### Added
- Scheduler now supports a fixed number of iterations. 
- Listener for Scheduler that can be configured via their API 

## \[0.2.1] - 2023-11-29
### Fix
- Fixed missing parent pom   

## \[0.2.0] - 2023-11-28
### Changed 
- Renamed packages into driving and driven adapters 
- Moved repository

### Added 
- Scheduler

## \[0.1.1] - 2023-11-26

### Fixed
- POM.xml so that it includes all required information to be uploaded to maven central 

## \[0.1.0] - 2023-11-26

### Added
- Initial version that is tracked in ChangeLog