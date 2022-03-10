#!/bin/bash

echo "Starting Python Pre-Processing"
python3 /python-helpers/jsonl_to_python.py $RAW_DATA_FILE $POST_PRE_PROCESSING_DIRECTORY

# This part is taken from the original obfuscator 1.1 and may needs to be adjusted in further releases
echo "Starting the Lampion Python Transformer"
cd /lampion/
python -m lampion ${LAMPION_CONFIG_FILE} ${POST_PRE_PROCESSING_DIRECTORY} ${POST_ALTERNATION_DIRECTORY} --loglevel ${LAMPION_LOGLEVEL}

echo "Starting Python Post-Processing"
cd /python-helpers
python3 /python-helpers/python_to_jsonl.py $POST_ALTERNATION_DIRECTORY $POST_ALTERNATION_JSONL

# Add this for debugging, container will not close so you can 'exec -it "container" bash' for inspection
#echo "Entering keep-alive state for debugging"
#tail -f /dev/null

echo "Run successful"
exit 0