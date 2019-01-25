#!/bin/sh
docker build -t jpo-sdw-depositor .
docker run --rm jpo-sdw-depositor:latest
