#!/bin/bash

# This script simply runs all augment-docker-compose.yaml and preprocess-docker-compose.yaml in the same folder.
# It is intended to be copied to a grid experiment.

find . -name "augment*compose.yaml" -print0 | xargs -I {} -0 docker-compose -f {} up && docker-compose -f {} down

find . -name "preprocess*compose.yaml" -print0 | xargs -I {} -0 docker-compose -f {} up && docker-compose -f {} down