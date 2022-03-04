# CodeBert Python Code-To-Text Preprocessing 

This part of the repository builds a container that alternates the training/test data of the CodeBERT-Experiment using the **Python-Transformer**. 

It first makes `.python-files` for all lines of the jsonl-dataset, 
then it runs the Transformer, 
then it stitches the altered `.python-files` back together into a new jsonl.

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
docker build --build-arg TRANSFORMER_VERSION=latest . -t lampion/codebert-python-preprocessing:1.2  -t lampion/codebert-python-preprocessing:latest -t ciselab/codebert-python-preprocessing:1.2  -t ciselab/codebert-python-preprocessing:latest -t ghcr.io/ciselab/lampion/codebert-python-preprocessing:1.2 -t ghcr.io/ciselab/lampion/codebert-python-preprocessing:latest
```

## Requirements

The PythonTransformer Image needs to be built / available.
See the other part of the repository for instructions how to build it.

Docker 19+

**On Script Requirements within Docker:**

To run the scripts in this folder, the only "new" python dependency is jsonlines 2.0.0
It needs however some basics from python, like wheel and regex. 
To not end up overwriting dependencies, we only manually install jsonlines==2.0.0 and otherwise take regex and functions 
from the base image, to not end up breaking lampion with the helpers here.

## Limitations

As by the definition of the experiment, there are some limitations in what the transformer can do: 

1. The Experiment does not allow comments
2. Due to preprocessing reasons, any transformations that add methods will add the additional methods to the code of the experiment. This is debatable
3. As the references are missing, the code cannot compile, blocking some transformations to be applied multiple times (such as the Lambda Transformation)
