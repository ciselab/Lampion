#!/bin/bash

echo "Starting the Lampion Java Transformer Container"

java -jar Lampion-Transformer.jar ${configfile} ${target} ${output}

# tail -f /dev/null