# jpo-sdw-depositor
Subscribes to a Kafka topic and deposits messages to the SDC/SDW.

## Overview

This is a submodule of the [jpo-ode](https://github.com/usdot-jpo-ode/jpo-ode) repository. It subscribes to a Kafka topic and listens for incoming messages. Upon message arrival, this application deposits it over REST to the Situation Data Warehouse.

## Installation

*Note* This is a submodule of the [jpo-ode](https://github.com/usdot-jpo-ode/jpo-ode) repository, this will automatically be installed as part of the ODE installation process.

## Configuration

You may configure these values in `jpo-sdw-depositor/src/main/resources/application.properties` or by editing them in the `sample.env` file then renaming the file to `.env`

| Value in `application.properties` | Value as env var (in sample.env) | Description                                           | Example Value               |
|-----------------------------------|----------------------------------|-------------------------------------------------------|-----------------------------|
| sdw.groupId                       | SDW_GROUP_ID                     | The Kafka group id to be used for message consumption | usdot.jpo.sdw               |
| sdw.kafkaBrokers                  | SDW_KAFKA_BROKERS                | Base IP address of Kafka instance                     | $DOCKER_HOST_IP             |
| sdw.kafkaPort                     | SDW_KAFKA_PORT                   | Port of the Kafka instance                            | 9092                        |
| sdw.subscriptionTopic             | SDW_SUBSCRIPTION_TOPIC           | Kafka topic to listen to                              | topic.J2735TimBroadcastJson |
| sdw.destinationProtocol           | SDW_DESTINATION_PROTOCOL         | SDW server protocol: http/https                       | https                       |
| sdw.destinationUrl                | SDW_DESTINATION_URL              | Base path of the SDW server address                   | 127.0.0.1                   |
| sdw.destinationPort               | SDW_DESTINATION_PORT             | SDW server port                                       | 80                          |
| sdw.destinationEndpoint           | SDW_DESTINATION_ENDPOINT         | SDW server REST endpoint                              | /deposit                    |
