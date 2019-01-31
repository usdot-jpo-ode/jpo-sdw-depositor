#!/bin/sh
docker build -t jpo-sdw-depositor . && docker run --rm  --env-file sample.env jpo-sdw-depositor:latest
