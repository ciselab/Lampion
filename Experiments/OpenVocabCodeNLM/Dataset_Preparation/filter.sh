#!/bin/bash

# This script filters the OpenVocabCodeNLM Small-dataset to 
# remove Python 2 Files.

# This is the second script to be run for dataset preparation.
# The python-corpus is not required anymore. 
# But the "open-vocab-small" directory needs to be filled already. 

python -m lampion ./reduce.properties ./open-vocab-small/Python_Test_Set ./Filtered/Python_Small_Test_Set | tee filter_test.log

python -m lampion ./reduce.properties ./open-vocab-small/Python_Validation_Set ./Filtered/Python_Small_Validation_Set | tee filter_valid.log

python -m lampion ./reduce.properties ./open-vocab-small/Python_Small_Training_Set ./Filtered/Python_Small_Training_Set | tee filter_training.log

