# CodeBert Java Code-To-Text Preprocessing 

**Note** The Dockerimage was previously called 'ciselab/codebert-preprocessing:1.1', and is now split into 'ciselab/codebert-java-preprocessing:1.2' and 'ciselab/codebert-python-preprocessing:1.2'.

This part of the repository builds a container that alternates the training/test data of the CodeBERT-Experiment using the Java-Transformer. 

It first makes `.java-files` for all lines of the jsonl-dataset, 
then it runs the Transformer, 
then it stitches the altered `.java-files` back together into a new jsonl.

Place the dataset in the *compose_input*-folder, 
adjust the name (and maybe Transformer version) in the `docker-compose.yml`, 
adjust the config to your liking, 

and run it with 

```
docker-compose up --build
```

You can also adjust the Transformer-Version setting the `--build-arg TRANSFORMER_VERSION=1.2-SNAPSHOT` or other versions. Just make sure your mounted configuration matches. 

For the Grid experiment, including the replication package and multiple configs, there is a [separate repository](../GridExperiment). 

The above given compose is just a step to verify this building brick of the meta experiment.    

To provide it for the GridExperiment, do 

```
docker build --build-arg TRANSFORMER_VERSION=latest . -t lampion/codebert-java-preprocessing:1.2  -t lampion/codebert-java-preprocessing:latest -t ciselab/codebert-java-preprocessing:1.2  -t ciselab/codebert-preprocessing:latest -t ghcr.io/ciselab/lampion/codebert-java-preprocessing:1.2 -t ghcr.io/ciselab/lampion/codebert-java-preprocessing:latest
```

## Requirements

The JavaTransformer Image needs to be built / available.
See the other part of the repository for instructions how to build it.

Docker 19+

Last tested python version: 3.8.12

## Limitations

As by the definition of the experiment, there are some limitations in what the transformer can do: 

1. The Experiment does not allow comments
2. Due to preprocessing reasons, any transformations that add methods will add the additional methods to the code of the experiment. This is debatable
3. As the references are missing, the code cannot compile, blocking some transformations to be applied multiple times (such as the Lambda Transformation)
