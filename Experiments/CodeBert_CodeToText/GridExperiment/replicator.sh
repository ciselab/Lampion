#!/bin/bash

# This script copies the folder ./model and ./ur_dataset to all 
# config_X in ./configs

config_array=($(ls ./configs))


maxindex=${#config_array[*]}

echo there are $maxindex config folders

for c in ${config_array[*]}
do  
    cp -r ./model ./configs/$c/
    cp -r ./ur_dataset ./configs/$c/
done

echo "done copying - exiting successful"