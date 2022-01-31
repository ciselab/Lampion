ARG PYTHON_VERSION=3.9
FROM python:${PYTHON_VERSION}-alpine
ARG SUBWORD_NMT_VERSION=0.3.8

LABEL maintainer="L.H.Applis@tu-delft.nl"
LABEL name="ciselab/openvocabcodenlm-preprocessing"
LABEL description="Preprocessing for the OpenVocabCodeNLM Training & Evaluation"

LABEL org.opencontainers.image.source="https://github.com/ciselab/openvocabcodenlm-preprocessing"
LABEL vcs="https://github.com/ciselab/openvocabcodenlm-preprocessing/"
LABEL url="https://github.com/ciselab/openvocabcodenlm-preprocessing/"

# Where the lampion should be applied/run, default to a sample file
ENV input_folder="/data"
ENV output_folder="/output"
ENV encoding_path="/encodings/python_encoding.enc_bpe_10000"
ENV loglevel="info"
ENV merged_filename="selfmade_pre_enc_10000"

WORKDIR /OpenVocab_DataPreparation

COPY requirements.txt requirements.txt
COPY entrypoint.sh entrypoint.sh
COPY prep.py prep.py

RUN pip install -r requirements.txt

RUN pip install subword-nmt==${SUBWORD_NMT_VERSION}

ENTRYPOINT ["sh","entrypoint.sh"]