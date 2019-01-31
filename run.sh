#!/bin/sh
docker build -t jpo-sdw-depositor . && docker run --rm  -e DOCKER_HOST_IP=$DOCKER_HOST_IP --env-file sample.env jpo-sdw-depositor:latest
