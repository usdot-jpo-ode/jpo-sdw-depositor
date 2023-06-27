Jpo-sdw-depositor Release Notes
----------------------------

Version 1.0.0, released Mar 30th 2023
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

