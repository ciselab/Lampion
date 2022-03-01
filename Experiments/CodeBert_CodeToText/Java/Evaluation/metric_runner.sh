#!/bin/bash

# This file runs over a directory full of config files
# and runs for each pair the bleu_evaluator.py
# creating a bleu.txt file containing the bleu score.
# The config folder must match the structure specified in the Readme!

# Run the metrics file with the folder to run on, e.g. ./data/PreliminaryResults

echo "Trying to run the metrics on $1"

folders=`find $1 -maxdepth 2 -mindepth 2 -type d`

for config_folder in $folders; do
    [ -e "$config_folder" ] || continue
    echo "running bleu in folder $config_folder"
    python3 ./bleu_evaluator.py \
      "$config_folder/test_0.gold" \
      < "$config_folder/test_0.output" \
      | tail -n 1 > "$config_folder/bleu.txt"
    cat $config_folder/bleu.txt
done

echo "Finished Bleu-Calculation - results are also in bleu.txt files in the corresponding folder"