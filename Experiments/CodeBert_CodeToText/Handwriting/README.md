# CodeBERT Handwriting

This directory helps to manually alter and inspect CodeBERT results. 
Place any form-conformative javafile in ./javafiles and go ahead with the run section.

## Run 

First, place all Javafiles in ./javafiles. 
Sub-folders are supported. 

Packages and Class-names are ignored (a class is required).

Then, run the python parts using: 

```
conda run -n lampion-codebert-handwriting python3 javadir_to_jsonl.py grid_configuration.json
```

after that simply do 

```
docker-compose up 
[...]
docker-compose down
```

Be sure to compose down, as the containers are quite heavyweight.

## Setup 

Install conda stuff: 

```
conda env create -f environment.yml
```

It is the same as in [preprocessing](../Preprocessing).

Build the container as per [the repository](https://github.com/ciselab/CodeBert-CodeToText-Reproduction) or use the docker pull: 

```
docker pull docker.pkg.github.com/ciselab/codebert-codetotext-reproduction/codebert-code2text:1.2
```
(this should need a docker login)

## Requirements

- Conda 
- The CodeBert Reproduction Container 
- (a) pretrained model placed in `model`

## Known Issues 

The container needs quite a bit of memory, make sure to run it on nice hardware. 