# Lampion Experiment - OpenVocabCodeNLM

## Structure (and Order)

1. [Dataset-Preparation](./Dataset_Preparation) to have examples of how to augment the data
2. [Grid-Dataset-Augmentation](tbd) the actual grid-experiment and values used to create all (augmented) datapoints
3. [Preprocessing](./Preprocessing) Sources to build the container that moves from .python files to the required encoded dataset files
4. [Grid-Preprocessing](./Grid_Preprocessing) the actual grid-experiment to move from the Grid-Dataset-Augmentation datasets to usable files
5. [Grid-OpenVocab](./Grid_OpenVocab) running the OpenVocabCodeNLM Experiment with the augmented data
6. [Evaluation](./Evaluation) contains post-processing values and statistical tests for elements in the paper. 

## Requirements

1. Docker 
2. Docker + GPUs (in case you want to run the Grid-OpenVocab Considerably fast)
3. Images build from this repository (namely [lampion/openvocabcodenlm-preprocessing](./Preprocessing/Dockerfile) and [lampion/python-transformer](../../Transformer/Python/Dockerfile))
4. [OpenVocabCodeNLM GPU Container Reproduction Image](https://github.com/ciselab/OpenVocabCodeNLM) (Either build there, or pulled)

## Hardware & Time Estimates

Test-Data Augmentation: 50k Transformations for 16k Datapoints: 90 Minutes

Train Data Augmentation: 50k Trans for 13k Datapoints: 80 Minutes
