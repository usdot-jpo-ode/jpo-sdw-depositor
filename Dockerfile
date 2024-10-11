# Build container
FROM maven:3.8-eclipse-temurin-21-alpine AS builder
LABEL maintainer="583114@bah.com"

WORKDIR /home

# Copy only the files needed to avoid putting all sorts of junk from your local env on to the image
COPY ./pom.xml ./
COPY ./src ./src

RUN mvn clean package -DskipTests

# Run container
FROM eclipse-temurin:21-jre-alpine

WORKDIR /home
# Copy the jar file from the builder image to the new image 
# since we run mvn clean package, there should only be one jar file in the target directory, and we can safely copy it with a wildcard
COPY --from=builder /home/target/jpo-sdw-depositor-*-SNAPSHOT.jar /home/jpo-sdw-depositor.jar

ENTRYPOINT ["java", \
	"-jar", \
	"/home/jpo-sdw-depositor.jar"]
