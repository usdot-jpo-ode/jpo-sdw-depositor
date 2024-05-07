# jpo-sdw-depositor

Subscribes to a Kafka topic and deposits messages to the [Situational Data Exchange (SDX)](https://sdx.trihydro.com/).

## Overview

This is a submodule of the [jpo-ode](https://github.com/usdot-jpo-ode/jpo-ode) repository. It subscribes to a Kafka topic and listens for incoming messages. Upon message arrival, this application deposits the message to the SDX via [REST API](https://sdx-service.trihydro.com/index.html).

## Release Notes
The current version and release history of the jpo-sdw-depositor project: [jpo-sdw-depositor Release Notes](<docs/Release_notes.md>)

## Installation and Operation

### Requirements

- [Kafka](https://kafka.apache.org/)
- [Docker](https://www.docker.com/)

### Option 1: As ODE submodule
The jpo-sdw-depositor is intended to be run as a submodule of the [jpo-ode](https://github.com/usdot-jpo-ode/jpo-ode) project. The ODE project repository includes a docker-compose file that will run the depositor in conjunction with the ODE by default. The same environment variables mentioned in the [Configuration Reference](#configuration-reference) below will need to be set in the `.env` file in the root of the ODE project.

### Option 2: Standalone (Depositor Only) with Remote Kafka

Use this option when you want to run the depositor by itself and you already have a Kafka cluster running remotely. This option will run the depositor in a Docker container and connect to a remote Kafka cluster to listen for messages. The depositor will then deposit these messages to the SDX.

1. Rename your `sample.env` file to `.env`. This file contains the environment variables that the application will use to connect to Kafka and the SDX.
1. Configure your environment variables in the `.env` file. See the [Configuration Reference](#configuration-reference) below.
1. Execute the `run.sh` script OR execute these commands:

```
docker build -t jpo-sdw-depositor .
docker run --rm  --env-file .env jpo-sdw-depositor:latest
```

### Option 3: With Local Kafka
Use this option when you want to run the depositor and you want to run a local Kafka cluster alongside it. This option will run the depositor and a Kafka cluster in Docker containers. The depositor will listen for messages on the local Kafka cluster and deposit them to the SDX.

1. Rename your `sample.env` file to `.env`. This file contains the environment variables that the application will use to connect to Kafka and the SDX.
1. Configure your environment variables in the `.env` file. See the [Configuration Reference](#configuration-reference) below.
1. Run the following command:

```
docker compose -f docker-compose-confluent-cloud.yml up --build
```

### Option 4: With Confluent Cloud Kafka
Use this option when you want to run the depositor and you want to connect to a Kafka cluster hosted by Confluent Cloud. This option will run the depositor in a Docker container and connect to a Kafka cluster hosted by Confluent Cloud to listen for messages. The depositor will then deposit these messages to the SDX.

1. Rename your `sample.env` file to `.env`. This file contains the environment variables that the application will use to connect to Kafka and the SDX.
1. Configure your environment variables in the `.env` file. See the [Configuration Reference](#configuration-reference) below.
1. Run the following command:

```
docker compose -f docker-compose-confluent-cloud.yml up --build
```

See the [Confluent Cloud Integration](#confluent-cloud-integration) section for more information.

## Configuration Reference

**SOME OF THESE PROPERTIES ARE SENSITIVE. DO NOT PUBLISH THEM TO VERSION CONTROL**

You may configure these values in `jpo-sdw-depositor/src/main/resources/application.properties` or by editing them in the `sample.env` file.

**IMPORTANT** When using the env file method, you must You must rename or duplicate the `sample.env` file to `.env`


| Value in `application.properties` | Value as env var (in sample.env) | Description                                           | Example Value               |
|-----------------------------------|----------------------------------|-------------------------------------------------------|-----------------------------|
| sdw.kafkaBrokers                | DOCKER_HOST_IP              | Host IP ([instructions](https://github.com/usdot-jpo-ode/jpo-ode/wiki/Docker-management#obtaining-docker_host_ip))                   | 10.1.2.3                   || sdw.groupId                       | SDW_GROUP_ID                     | The Kafka group id to be used for message consumption | usdot.jpo.sdw               |            |
| sdw.kafkaPort                     | SDW_KAFKA_PORT                   | Port of the Kafka instance                            | 9092                        |
| sdw.subscriptionTopic             | SDW_SUBSCRIPTION_TOPIC           | Kafka topic to listen to                              | topic.J2735TimBroadcastJson |
| sdw.destinationUrl                | SDW_DESTINATION_URL              | Full path of the SDX server address                   | 127.0.0.1                   |
| sdw.apikey                | SDW_API_KEY       | SDX API Key (generated by [SDX](https://sdx.trihydro.com))        | (n/a)
| sdw.emailList             | SDW_EMAIL_LIST    | Comma-delimited email list to send error emails to                                    | error@email.com,test@test.com  
| sdw.emailFrom             | SDW_EMAIL_FROM    | Support email to send from                                                            | error@email.com

## Confluent Cloud Integration
Rather than using a local kafka instance, this project can utilize an instance of kafka hosted by Confluent Cloud via SASL.

### Environment variables
#### Purpose & Usage
- The DOCKER_HOST_IP environment variable is used to communicate with the bootstrap server that the instance of Kafka is running on.
- The KAFKA_TYPE environment variable specifies what type of kafka connection will be attempted and is used to check if Confluent should be utilized.
- The CONFLUENT_KEY and CONFLUENT_SECRET environment variables are used to authenticate with the bootstrap server.

#### Values
- DOCKER_HOST_IP must be set to the bootstrap server address (excluding the port)
- KAFKA_TYPE must be set to "CONFLUENT"
- CONFLUENT_KEY must be set to the API key being utilized for CC
- CONFLUENT_SECRET must be set to the API secret being utilized for CC

### CC Docker Compose File
There is a provided docker-compose file (docker-compose-confluent-cloud.yml) that passes the above environment variables into the container that gets created. Further, this file doesn't spin up a local kafka instance since it is not required.

### Note
This has only been tested with Confluent Cloud but technically all SASL authenticated Kafka brokers can be reached using this method.

## Unit Testing
The unit tests can be run by executing the following command from the root directory of the project:
```
mvn test
```

It should be noted that Maven & Java are required to run the unit tests. If you do not have Maven or Java installed, you can reopen the project in the provided dev container and run the tests from there.

## Object data consumption
The KafkaConsumerRestDepositor will accept any string as input to be passed into the SDW. If provided a JSON object, the tokens of "encodedMsg" and "estimatedRemovalDate" will be passed through directly to the SDW in the form of the following:
{depositRequests:[{"encodeType": STRING ,"encodedMsg": STRING, "estimatedRemovalDate": STRING}]}

If provided a string of non-json form, the value of "encodedMsg" will inherit the passed value and information will be passed to the SDW in the form of the following:
{depositRequests:[{"encodeType": STRING ,"encodedMsg": STRING}]}
