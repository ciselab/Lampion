#!/bin/bash

# This script moves the run.sh and data into the "grid_augmentation" folder
# And makes a tar file out of it for easier shipping

cp run.sh ./grid_augmentation/run.sh

cp -r ./data ./grid_augmentation/data

tar czvf grid_augmentation.tar.gz grid_augmentation/