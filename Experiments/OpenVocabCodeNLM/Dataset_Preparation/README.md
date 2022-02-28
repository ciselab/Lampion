# Lampion - OpenVocabCodeNLM Dataset Preparation 

This directory hold shell files to run and prepare the datasets for the OpenVocabCodeNLM Experiment. 

One issue arising was that the original dataset contained Python 2 Files, that were not parsable by our tool (inherited from the LibCST Library). We need to remove them.

Due to the File-Size // number of files, this can take a bit of time.

## Covered Steps 

1. Reduce total corpus to "Small Corpus" as per authors ([reduce.sh](./reduce.sh))
2. Filter small corpus for Python 3 files only ([filter.sh](./filter.sh))
3. Provide an example for applying docker-compose to filtered small corpus 

## Requirements 

- [Lampion Python Transformer](../../Transformers/Python) install as Pip Package 
- A Conda Environment for the LampionPythonTransformer called *LampionPython3*
- The [Preprocessing Container](https://github.com/ciselab/openvocabcodenlm-preprocessing)

Required Layout: 

'''
.
├── README.md
├── filter.sh
├── open-vocab-small
├── python-corpus
│   ├── ...
│   └── ...
├── python_dataset_stats
│   ├── small_training_set.txt
│   ├── test_set.txt
│   └── validation_set.txt
└── reduce.sh
'''


## Links 

- [OpenVocabCodeNLM  Meta-Repository](https://github.com/giganticode/icse-2020)
- [OpenVocabCodeNLM](https://github.com/mast-group/OpenVocabCodeNLM)
- [Original Dataset](https://zenodo.org/record/3628784)
- [BPE Encodings](https://zenodo.org/record/3628636)