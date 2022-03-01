#!/bin/bash

# This script runs over a nearby ./configs-folder 
# and extracts the results in a way the evaluator needs
# Make a backup copy of the raw-results beforehand!! 

echo "Packing results found in ./configs" 

folders=`find ./configs -maxdepth 1 -mindepth 1 -type d`

for config_folder in $folders; do
    [ -e "$config_folder" ] || continue
    echo "running extractor in folder $config_folder"
    rm -rf "$config_folder/model"
    rm -rf "$config_folder/dataset"
    rm -rf "$config_folder/ur_dataset"
    mv "$config_folder/experiment_output/test_0.gold" "$config_folder"
    mv "$config_folder/experiment_output/test_0.output" "$config_folder"
    rm -rf "$config_folder/experiment_output"
done

echo "deleting nearby docker-compose files"

rm experiment-with-training*.yaml 
rm experiment-docker-compose*.yaml 
rm preprocessing-docker-compose*.yaml

echo "Finished extracting results from ./configs"

