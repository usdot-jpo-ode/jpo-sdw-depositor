Jpo-sdw-depositor Release Notes
----------------------------

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

