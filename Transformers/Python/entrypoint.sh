#!/bin/bash
echo "Starting Lampion Python Transformer Container - Building the command"

# As the command gets maybe complex,
# it is stitched together with any flag set in docker.
commandCollector="python -m lampion ${configfile} ${target} ${output} --loglevel ${loglevel}"

if [ -z ${print_example+x} ];
then echo "not printing examples"
else commandCollector="$commandCollector --example"
fi

if [ -z ${only_store_touched+x} ];
then echo "printing only touched files"
else commandCollector="$commandCollector --output-only-changed"
fi

echo "final cmd is ${commandCollector}"

/bin/bash -c "$commandCollector"

# To keep the container open for inspection
# echo "Program finished - Keeping Container open for inspection"
# tail -f /dev/null
