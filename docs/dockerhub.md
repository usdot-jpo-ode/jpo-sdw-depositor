# jpo-sdw-depositor

## GitHub Repository Link
https://github.com/usdot-jpo-ode/jpo-sdw-depositor

## Purpose
The purpose of the jpo-sdw-depositor program is to deposit messages to the SDX.

## How to pull the latest image
The latest image can be pulled using the following command:
> docker pull usdotjpoode/jpo-sdw-depositor:develop

## Required environment variables
- DOCKER_HOST_IP
- SDW_EMAIL_LIST
- SDW_EMAIL_FROM
- SDW_API_KEY

## Direct Dependencies
The SDWD will fail to start up if the following containers are not already present:
- Kafka
- Zookeeper (relied on by Kafka)

## Indirect Dependencies
The SDWD will not receive messages to process if the ODE is not running.

## Example docker-compose.yml with direct dependencies:
```
version: '2'
services:
  zookeeper:
    image: wurstmeister/zookeeper
    ports:
      - "2181:2181"

  kafka:
    image: wurstmeister/kafka
    ports:
      - "9092:9092"
    environment:
      KAFKA_ADVERTISED_HOST_NAME: ${DOCKER_HOST_IP}
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_CREATE_TOPICS: "test:1:1"
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
      
  sdw_depositor:
    image: usdotjpoode/jpo-sdw-depositor:release_q3
    environment:
      # required
      DOCKER_HOST_IP: ${DOCKER_HOST_IP}
      SDW_EMAIL_LIST: ${SDW_EMAIL_LIST}
      SDW_EMAIL_FROM: ${SDW_EMAIL_FROM}
      SDW_API_KEY: ${SDW_API_KEY}
      # optional
      SDW_DESTINATION_URL: ${SDW_DESTINATION_URL}
      SPRING_MAIL_HOST: ${SPRING_MAIL_HOST}
      SPRING_MAIL_PORT: ${SPRING_MAIL_PORT}
      SDW_SUBSCRIPTION_TOPIC: ${SDW_SUBSCRIPTION_TOPIC}
    logging:
      options:
        max-size: "10m"
        max-file: "5"
```

## Expected startup output
The latest logs should look something like this:
```
2023-11-09 17:40:31.082  INFO 1 --- [           main] o.a.k.c.c.internals.AbstractCoordinator  : [Consumer clientId=consumer-usdot.jpo.sdw-1, groupId=usdot.jpo.sdw] Discovered group coordinator 192.168.0.243:9092 (id: 2147482646 rack: null)
2023-11-09 17:40:31.089  INFO 1 --- [           main] o.a.k.c.c.internals.AbstractCoordinator  : [Consumer clientId=consumer-usdot.jpo.sdw-1, groupId=usdot.jpo.sdw] (Re-)joining group
2023-11-09 17:40:31.174  INFO 1 --- [           main] o.a.k.c.c.internals.AbstractCoordinator  : [Consumer clientId=consumer-usdot.jpo.sdw-1, groupId=usdot.jpo.sdw] (Re-)joining group
2023-11-09 17:40:31.227  INFO 1 --- [           main] o.a.k.c.c.internals.AbstractCoordinator  : [Consumer clientId=consumer-usdot.jpo.sdw-1, groupId=usdot.jpo.sdw] Successfully joined group with generation Generation{generationId=1, memberId='consumer-usdot.jpo.sdw-1-cd6afbec-5bfe-46e0-beb6-539d47426902', protocol='range'}
2023-11-09 17:40:31.233  INFO 1 --- [           main] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-usdot.jpo.sdw-1, groupId=usdot.jpo.sdw] Finished assignment for group at generation 1: {consumer-usdot.jpo.sdw-1-cd6afbec-5bfe-46e0-beb6-539d47426902=Assignment(partitions=[topic.SDWDepositorInput-0])}
2023-11-09 17:40:31.369  INFO 1 --- [           main] o.a.k.c.c.internals.AbstractCoordinator  : [Consumer clientId=consumer-usdot.jpo.sdw-1, groupId=usdot.jpo.sdw] Successfully synced group in generation Generation{generationId=1, memberId='consumer-usdot.jpo.sdw-1-cd6afbec-5bfe-46e0-beb6-539d47426902', protocol='range'}
2023-11-09 17:40:31.370  INFO 1 --- [           main] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-usdot.jpo.sdw-1, groupId=usdot.jpo.sdw] Notifying assignor about the new Assignment(partitions=[topic.SDWDepositorInput-0])
2023-11-09 17:40:31.378  INFO 1 --- [           main] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-usdot.jpo.sdw-1, groupId=usdot.jpo.sdw] Adding newly assigned partitions: topic.SDWDepositorInput-0
2023-11-09 17:40:31.415  INFO 1 --- [           main] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-usdot.jpo.sdw-1, groupId=usdot.jpo.sdw] Found no committed offset for partition topic.SDWDepositorInput-0
2023-11-09 17:40:31.454  INFO 1 --- [           main] o.a.k.c.c.internals.SubscriptionState    : [Consumer clientId=consumer-usdot.jpo.sdw-1, groupId=usdot.jpo.sdw] Resetting offset for partition topic.SDWDepositorInput-0 to position FetchPosition{offset=0, offsetEpoch=Optional.empty, currentLeader=LeaderAndEpoch{leader=Optional[192.168.0.243:9092 (id: 1001 rack: null)], epoch=0}}.
```