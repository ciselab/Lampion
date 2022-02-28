#!/bin/bash

# This script runs the (required) composes in order 
# And logs the outputs

# This is intended to be run in trial-augment-run root!
# It will not work anywhere else without adjustment

docker-compose -f ./ntr-nte-docker-compose.yaml up | tee ntr-nte.log
docker-compose -f ./ntr-nte-docker-compose.yaml down

docker-compose -f ./ntr-ate-docker-compose.yaml up | tee ntr-ate.log
docker-compose -f ./ntr-ate-docker-compose.yaml down

docker-compose -f ./atr-nte-docker-compose.yaml up | tee atr-nte.log
docker-compose -f ./atr-nte-docker-compose.yaml down

docker-compose -f ./atr-ate-docker-compose.yaml up | tee atr-ate.log
docker-compose -f ./atr-ate-docker-compose.yaml down

