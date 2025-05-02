jpo-sdw-depositor Release Notes
----------------------------

Version 1.9.1, released May 2025
----------------------------------------
### **Summary**
This release updates the Kafka version in pom.xml and the action/download-artifact version in ci.yml to versions that do not have vulnerabilities.

Enhancements in this release:
[CDOT PR 32](https://github.com/CDOT-CV/jpo-sdw-depositor/pull/32): Set up CI with Azure Pipelines
[CDOT PR 33](https://github.com/CDOT-CV/jpo-sdw-depositor/pull/33): Update Dependency Versions


Version 1.9.0, released January 2025
----------------------------------------
### **Summary**
The jpo-sdw-depositor 1.9.0 release simplifies and improves the Docker build process by automating the handling of 
versioned jar files, eliminating the need for manual updates to the Dockerfile with each new version. The Dockerfile now
ensures versioned jars are copied to an unversioned name within the Docker image, allowing the application to reference 
a static file name while preserving versioned jars for distribution. Additionally, polish updates were made to modernize 
and streamline the build process, including replacing the deprecated MAINTAINER field with LABEL maintainer, resolving 
casing inconsistencies in Docker commands, and cleaning up unnecessary configurations in .devcontainer.json based on 
linter recommendations. These changes enhance reliability and maintainability while reducing the risk of errors.

Enhancements in this release:
- [CDOT PR 28](https://github.com/CDOT-CV/jpo-sdw-depositor/pull/28): Dockerfile jar rename
- [CDOT PR 30](https://github.com/CDOT-CV/jpo-sdw-depositor/pull/30): Fixed Dockerfile to correctly copy JAR after version update (SNAPSHOT removal)

Known issues:
- No known issues at this time.


Version 1.8.0, released September 2024
----------------------------------------
### **Summary**
The changes for the jpo-sdw-depositor 1.8.0 release include a fix for the SDW_SUBSCRIPTION_TOPIC environment variable not getting used instead of the default if provided, as well as the addition of a GitHub action to publish Java artifacts to GitHub's hosted Maven Central.

Enhancements in this release:
- CDOT PR 24: Fixed SDW_SUBSCRIPTION_TOPIC environment variable not getting used instead of default if provided
- CDOT PR 25: Added a GitHub action to publish java artifacts to GitHub's hosted Maven Central


Version 1.7.0, released June 2024
----------------------------------------
### **Summary**
The changes for the jpo-sdw-depositor 1.7.0 release include a Java update for the dev container, as well as revised documentation for accuracy and improved clarity/readability.

Enhancements in this release
- CDOT PR 19: Updated dev container to use Java 21
- CDOT PR 20: Revised documentation for accuracy & improved clarity/readability


Version 1.6.0, released February 2024
----------------------------------------

### **Summary**
The changes for the jpo-sdw-depositor 1.6.0 release include a java update & dockerhub image documentation.

Enhancements in this release:
- CDOT PR 12: Updated Java to v21
- CDOT PR 11: Added dockerhub image documentation

Known Issues:
- No known issues at this time.


Version 1.5.0, released November 2023
----------------------------------------

### **Summary**
The updates for the jpo-sdw-depositor 1.5.0 involve correcting the default SDX URL, fixing failing unit tests, and addressing a key reference issue in KafkaConsumerRestDepositor.java.
- The default SDX URL has been corrected.
- Resolved failing unit tests.
- Addressed a key reference issue in KafkaConsumerRestDepositor.java.
- Renamed mocked variables in DepositControllerTest.java

Known Issues:
- No known issues at this time.


Version 1.4.0, released July 5th 2023
----------------------------------------

### **Summary**
The updates for jpo-sdw-depositor 1.4.0 include CI/CD & dependency changes.

Enhancements in this release:
- Added CI/CD for Docker Build & Sonar Cloud

Fixes in this release:
- Bumped json from 20180130 to 20230227

Known Issues
- A non-critical error is occurring in the console occasionally. It is worth noting that the logs indicate successful message deposits to the SDX without encountering this error. Therefore, the problem seems to occur intermittently.

Version 1.3.0, released Mar 30th 2023
----------------------------------------

### **Summary**
The updates for jpo-sdw-depositor 1.0.0 include Confluent Cloud Integration, some fixes, multiple record deposit functionality and some documentation improvements.

Enhancements in this release:
-	Allowed the project to work with an instance of kafka hosted by Confluent Cloud.
-	Added CC integration info to README.
-	Introduced some new environment variables.
-	Added docker and dev container files.
-	Added updates to allow unit tests to run.
-	Transitioned to depositing multiple records into the SDX rather than a single record at a time.
-	Updated base image to eclipse-temurin:11-jre-alpine instead of the deprecated openjdk:8u171-jre-alpine image.

Fixes in this release:
-	Fixed some unit tests.
-	Swapped over to using the kafka_2.11 library instead of the kafka-clients library.
-	Added a fix for tests run via Maven.

