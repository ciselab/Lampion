ARG TRANSFORMER_VERSION=latest
FROM lampion/python-transformer:${TRANSFORMER_VERSION}

LABEL org.opencontainers.image.source https://github.com/ciselab/lampion

RUN pip install jsonlines==2.0.0

# Important info from Obfuscator Image: 
# The jar is placed under /usr/app
# The config must be under /config

RUN mkdir /python-helpers

WORKDIR /python-helpers

COPY python_to_jsonl.py .
RUN chmod +x python_to_jsonl.py
COPY jsonl_to_python.py .
RUN chmod +x jsonl_to_python.py

# Be careful: There will be two "entrypoint.sh" - one from the obfuscator, one from this
COPY entrypoint.sh .
RUN chmod +x ./entrypoint.sh

# Where is the data before the python preprocessing, that is the initial .jsonl files
# Default value is for the example file
ENV RAW_DATA_FILE /raw_data/python.jsonl
# Where should the python files be printed too (must match the configuration of the obfuscator)
ENV POST_PRE_PROCESSING_DIRECTORY /usr/app/transformer_input
# Where are the files to re-merge to an .jsonl file (must match the configuration of the obfuscator)
ENV POST_ALTERNATION_DIRECTORY /usr/app/transformer_output
# Where should the final data be stored 
ENV OUTPUT_DIRECTORY /data
# Where should the final jsonl be stored and how is it called
ENV POST_ALTERNATION_JSONL $OUTPUT_DIRECTORY/altered_python.jsonl

ENV LAMPION_LOGLEVEL info

ENV LAMPION_CONFIG_FILE /config/config.properties

RUN mkdir /data

ENTRYPOINT ["./entrypoint.sh"]