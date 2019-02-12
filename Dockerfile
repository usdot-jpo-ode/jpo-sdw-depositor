# Build container
FROM maven:3.5.4-jdk-8-alpine as builder
MAINTAINER 583114@bah.com

WORKDIR /home

# Copy only the files needed to avoid putting all sorts of junk from your local env on to the image
COPY ./pom.xml ./
COPY ./src ./src

RUN mvn clean package -DskipTests

# Run container
FROM openjdk:8u171-jre-alpine

WORKDIR /home
COPY --from=builder /home/target/jpo-sdw-depositor-0.0.1-SNAPSHOT.jar /home

ENTRYPOINT ["java", \
	"-jar", \
	"/home/jpo-sdw-depositor-0.0.1-SNAPSHOT.jar"]
