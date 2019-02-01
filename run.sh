#!/bin/sh
docker build -t jpo-sdw-depositor . && docker run --rm  --env-file .env jpo-sdw-depositor:latest
