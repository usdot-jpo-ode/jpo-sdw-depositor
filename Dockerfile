# Build container
FROM maven:3.5.4-jdk-8-alpine as builder
MAINTAINER 583114@bah.com

WORKDIR /home

COPY ./pom.xml .
RUN mvn dependency:resolve dependency:resolve-plugins -Dmaven.repo.local=/mvn/.m2nrepo/repository

COPY ./src ./src
RUN mvn package -DskipTests -Dmaven.repo.local=/mvn/.m2nrepo/repository

# Run container
FROM openjdk:8u171-jre-alpine

WORKDIR /home
COPY --from=builder /home/target/jpo-sdw-depositor-0.0.1-SNAPSHOT.jar /home

ENTRYPOINT ["java", \
	"-jar", \
	"/home/jpo-sdw-depositor-0.0.1-SNAPSHOT.jar"]
