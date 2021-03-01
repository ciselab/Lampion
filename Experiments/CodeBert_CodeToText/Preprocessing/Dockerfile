ARG OBFUSCATOR_VERSION=latest
FROM lampion/java-obfuscator:${OBFUSCATOR_VERSION}

RUN apk add python3>=3.8.5-r0 py3-pip>=20.1.1-r0 python3-dev>=3.8.5-r0 build-base bash

RUN pip install wheel==0.34.2 regex==2020.6.8 jsonlines

# Important info from Obfuscator Image: 
# The jar is placed under /usr/app
# The config must be under /config

RUN mkdir /python-helpers

WORKDIR /python-helpers

COPY java_to_jsonl.py .
RUN chmod +x java_to_jsonl.py
COPY jsonl_to_java.py .
RUN chmod +x jsonl_to_java.py

# Be careful: There will be two "entrypoint.sh" - one from the obfuscator, one from this
COPY entrypoint.sh .
RUN chmod +x ./entrypoint.sh

# Where is the data before the python preprocessing, that is the initial .jsonl files
ENV RAW_DATA_FILE /raw_data/java.jsonl
# Where should the java files be printed too (must match the configuration of the obfuscator)
ENV POST_PRE_PROCESSING_DIRECTORY /usr/app/obfuscator_input
# Where are the files to re-merge to an .jsonl file (must match the configuration of the obfuscator)
ENV POST_ALTERNATION_DIRECTORY /usr/app/obfuscator_output
# Where should the final data be stored 
ENV OUTPUT_DIRECTORY /data
# Where should the final jsonl be stored and how is it called
ENV POST_ALTERNATION_JSONL $OUTPUT_DIRECTORY/altered_java.jsonl

# Where does the Obfuscator write the manifest to (must match the configuration of the obfuscator)
ENV MANIFEST_PATH /usr/app/obfuscator_output/manifest/TransformationManifest.db

RUN mkdir /data

ENTRYPOINT ["./entrypoint.sh"]