# CodeBERT Code-To-Text Grid Experiment

This folder holds everything required to run the experiment in a grid fashion. 

The `main.py` uses the `grid_configuration.json` to build:

1. A set of configurations 
2. A docker compose to run the preprocessing for the configurations
3. A docker compose to run the experiment using the preprocessed jsonl files

The docker composes are orchestrated to grab into each other, so be careful manually changing them.

## Requirements

- Docker & Docker Compose 
- Python 3 and Conda
- The Docker Containers build / pulled
- A pretrained model from the CodeBert experiment
- The (cleaned) dataset as a .jsonl file

## How to run 

Adjust the `grid_configuration.json` to your likings, but be careful to not overdo it in a single run. 
You might want to split it to multiple runs (which is fine to do, if you extract the results between the runs).

Place your model in *./models/* and either name it `model.bin` or adjust the name in [the experiment template file](./templates/experiment-docker-compose.yaml.j2).

Place your (unmodified dataset) in *./ur_dataset* and either name it `test_java.jsonl` or adjust the name in [the preprocessing template file](./templates/preprocessing-docker-compose.yaml.j2).

Activate the conda environment and run 

```
python3 main.py
```

It will create the above mentioned files.

Run the docker-composes using

```
docker-compose -f preprocessing-docker-compose.yaml up 
[...]
docker-compose -f preprocessing-docker-compose.yaml down
```

and 

```
docker-compose -f experiment-docker-compose.yaml up 
[...]
docker-compose -f experiment-docker-compose.yaml down
```

**Make sure to docker compose down, otherwise you infest your system with a lot of containers.**

The results of the runs are gathered under the configs.

If you want to run the experiment with training, place the training and validation files under the configs `ur_dataset`. This is sadly necessary as otherwise they fight over file-locks. 

## Limitations 

Sometimes, sadly, the preprocessing fails for certain entries. 

I am currently investigating this and will provide a dataset that is not failing in the preprocessing.
