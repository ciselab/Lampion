#!/bin/bash

# This script runs all (known) docker-composes of the Lampion CodeBert-Code2Text-Grid-Experiment
# It is intended to be next to the docker-composes, with a ./config folder nearby too.
# At first, all preprocessing scripts are run, then all test-scripts

echo "Running all preprocessing files"

find . -name "preprocessing-docker-compose*.yaml" -print0 | xargs -I {} -0 docker-compose -f {} up && docker-compose -f {} down

echo "Finished all preprocessing files"
echo "Running all experiment files"

find . -name "experiment-docker-compose*.yaml" -print0 | xargs -I {} -0 docker-compose -f {} up && docker-compose -f {} down

echo "Finished all experiment files"