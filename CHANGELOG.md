# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## \[1.3.0] - 2024-02-25
### Added
* `TransactionalOutboxSender`: Properties `outbox.table` to define the used table name. See [here](common-adapters/src/test/resources/application.properties) for an example.

* Added: `RepositoryFactory`, `ObjectStoreFactory`, and `MessageSenderFactory` as replacement for `RepositoryManager`, `ObjectStoreManager`, and `MessageSenderManager`

### Deprecated
* Declared `RepositoryManager`, `ObjectStoreManager`, and `MessageSenderManager` as deprecated because they are factories that is not obvious due to their names and APIs
s
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