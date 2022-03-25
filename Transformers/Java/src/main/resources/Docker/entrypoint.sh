#!/bin/bash

echo "Starting the Lampion Transformer Container"

java -jar Lampion-Transformer.jar ${configfile} ${target} ${output}

tail -f /dev/null
