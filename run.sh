#!/bin/sh
docker build -t jpo-sdw-depositor .
docker run --rm -e DOCKER_HOST_IP=$DOCKER_HOST_IP jpo-sdw-depositor:latest
