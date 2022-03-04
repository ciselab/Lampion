#!/usr/bin/env bash

echo "Starting Python Pre-Processing"
python /python-helpers/jsonl_to_java.py $RAW_DATA_FILE $POST_PRE_PROCESSING_DIRECTORY

# This part is taken from the original transformer 1.1 and may needs to be adjusted in further releases
echo "Starting the Lampion Java Transformer"
java -jar /usr/app/Lampion-Transformer.jar /config/config.properties

echo "Starting Python Post-Processing"
python /python-helpers/java_to_jsonl.py $POST_ALTERNATION_DIRECTORY $POST_ALTERNATION_JSONL

# Add this for debugging, container will not close so you can 'exec -it "container" bash' for inspection
# echo "Entering keep-alive state for debugging"
# tail -f /dev/null

echo "Run successful"
exit 0