#!/bin/bash

echo "Starting (Python) jsonl->Java Pre-Processing"
python3 /python-helpers/jsonl_to_java.py $RAW_DATA_FILE $POST_PRE_PROCESSING_DIRECTORY

# This part is taken from the transformer 1.3 and may needs to be adjusted in further releases
echo "Starting the Lampion Java Transformer"
java -jar /usr/app/Lampion-Transformer.jar /config/config.properties $POST_PRE_PROCESSING_DIRECTORY $POST_ALTERNATION_DIRECTORY

echo "Starting (Python) Java->jsonl Post-Processing"
python3 /python-helpers/java_to_jsonl.py $POST_ALTERNATION_DIRECTORY $POST_ALTERNATION_JSONL

# Add this for debugging, container will not close so you can 'exec -it "container" bash' for inspection
# echo "Entering keep-alive state for debugging"
# tail -f /dev/null

echo "Run successful"
exit 0