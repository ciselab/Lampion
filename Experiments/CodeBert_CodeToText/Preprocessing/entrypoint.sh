#!/usr/bin/env bash

echo "Starting Python Pre-Processing"
python3 /python-helpers/jsonl_to_java.py $RAW_DATA_FILE $POST_PRE_PROCESSING_DIRECTORY

# This part is taken from the original obfuscator 1.1 and may needs to be adjusted in further releases
echo "Starting the Lampion Obfuscator"
java -jar /usr/app/Lampion-JavaObfuscator.jar /config/config.properties

echo "Starting Python Post-Processing"
python3 /python-helpers/java_to_jsonl.py $POST_ALTERNATION_DIRECTORY $POST_ALTERNATION_JSONL

echo "Moving manifest to data directory"
mv $MANIFEST_PATH $POST_ALTERNATION_JSONL $OUTPUT_DIRECTORY

# Add this for debugging, container will not close so you can 'exec -it "container" bash' for inspection
# echo "Entering keep-alive state for debugging"
# tail -f /dev/null

echo "Run successful"
exit 0