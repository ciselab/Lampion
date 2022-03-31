# Lampion CodeBERT Code2Text Reproduction 

Welcome to the reproduction package for the Lampion CodeBERT Code2Text Grid-Experiment. 
This is meant to give easy access for the final experiments shown in the accommodating paper. 

The creation process for these files, as well as a bigger overview, can be found [in the repository](https://github.com/ciselab/Lampion/tree/main/Experiments/CodeBert_CodeToText/).

## Requirements 

- Linux Operating System
- Docker v20.10.13
- Docker Compose v2.2.2
- [CodeBERT Experiment Dockerimage](https://github.com/ciselab/CodeBert-CodeToText-Reproduction) v.1.3
- [CodeBERT Python-Preprocessing Image](https://github.com/ciselab/Lampion/tree/main/Experiments/CodeBert_CodeToText/preprocessing-python) v1.2
- [CodeBERT Java-Preprocessing Image](https://github.com/ciselab/Lampion/tree/main/Experiments/CodeBert_CodeToText/preprocessing-java) v1.2

In case that no GPUs are available / configured, the experiment will default to using CPUs.

## How To 

1. Prepare the requirements (namely, download or build the images)
2. Ship the folders to your GPU-Server
3. Run the `replicator.sh` (this will need a lot of space!)
4. Run the `runner.sh` in background per: `nohup ./runner.sh >runner.log &`
5. Wait (estimate ~1h+ per experiment)
6. Optional: Run `extractor.sh` to only get output files (to not copy model-replicas and data-replicas on your local computer)

## Contents 

- Pretrained Models for Java and/or Python
- Cleaned Test-Datasets
- docker-composes to run experiments
- helper shell-files

The pretrained models are those that scored best in BLEU in training ("best bleu").
The models were trained as per default configuration in [CodeXGlue Readme](https://github.com/microsoft/CodeXGLUE/tree/main/Code-Text/code-to-text)

Not-Contents:

- Training & Validation Files
- File Cleaning Process
- Code-Files to run experiments
- Dockerfiles to create images

Most of the Non-Content elements are available FOSS [in the repository](https://github.com/ciselab/Lampion/).

## Licence(s)

The code and artifacts provided by the authors are under MIT Licence. 
The NVIDIA containers come with an implicit License. 

## Used Environment 

We are unfortunately aware that GPU Containers are very fragile. 
We hope that we figured most elements out, as it worked for us on multiple different machines. 
Never the less, here are the used specs to produce our results:

- Linux 5.4.0 Generic Ubuntu 
- NVIDIA A40 (Graphics Card)
- CUDA Version 11.6
- NVIDIA Driver Version 510.47.03
- NVIDIA Docker 2.9.1