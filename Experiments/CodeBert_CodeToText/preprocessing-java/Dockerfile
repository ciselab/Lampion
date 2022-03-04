ARG TRANSFORMER_VERSION=latest
FROM ciselab/lampion/java-transformer:${TRANSFORMER_VERSION}

LABEL org.opencontainers.image.source="https://github.com/ciselab/lampion"
LABEL url="https://github.com/ciselab/lampion"
LABEL vcs="https://github.com/ciselab/lampion"
LABEL maintainer="L.H.Applis@tu-delft.nl"
LABEL name="ciselab/lampion/codebert-java-preprocessing"

RUN apk add python3>=3.8.5-r0 py3-pip>=20.1.1-r0 python3-dev>=3.8.5-r0 build-base bash

# Important info from Transformer Image:
# The jar is placed under /usr/app
# The config (of the transformer) must be under /config

RUN mkdir /python-helpers

WORKDIR /python-helpers

COPY requirements.txt .
RUN pip install -r requirements.txt

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