# CodeBERT Code-To-Text Grid Experiment

This folder holds everything required to run the experiment in a grid fashion. 

The `main.py` uses the `grid_configuration.json` to build:

1. A set of configurations 
2. A docker compose to run the preprocessing for the configurations
3. A docker compose to run the experiment using the preprocessed jsonl files

The docker composes (and scripts) are orchestrated to grab into each other, so be careful manually changing them.

There are two scripts provided: 

[replicator.sh](replicator.sh) which copies all files and models to the configs, making unique non-blocking items. 
place the required model (with the correct name) under ./model and the training- and validation-file under ./ur_dataset.
When not running the training, the ./ur_dataset can be left empty.
The functionality of the replicator was initially in the main.py, but has been moved so that the replication can be done server side, reducing data in transit.

[extractor.sh](./extractor.sh) will be run after the experiments, and it reduces the folder ./configs to fit what is required for the evaluation. It removes the altered dataset and the model. 
**Be sure to have a backup before you run the extractor!** 


## Requirements

- Docker & Docker Compose 
- Python 3 and Conda
- The Docker Containers build / pulled
- A pretrained model from the CodeBert experiment
- The (cleaned) dataset as a .jsonl file

The cleaned dataset and the model can be found [here](https://surfdrive.surf.nl/files/index.php/f/8713322177). 
There is also a ready-made experiment set from the paper that only needs to run with docker-compose. 

## How to run 

Adjust the `grid_configuration.json` to your likings, but be careful to not overdo it in a single run. 
You might want to split it to multiple runs (which is fine to do, if you extract the results between the runs).

Place your model in *./models/* and either name it `model.bin` or adjust the name in [the experiment template file](./templates/experiment-docker-compose.yaml.j2).

Place your (unmodified dataset) in *./ur_dataset* and either name it `test_java.jsonl` or adjust the name in [the preprocessing template file](./templates/preprocessing-docker-compose.yaml.j2).

Activate the conda environment and run 

```shell
python3 main.py
```

(Or run)

```shell
conda run -n Lampion_CodeToText_GridExperiment python3 main.py grid_configuration.json
```

It will create the above mentioned files.
Run the replicator to place the copies in the right places: 

```shell
./replicator.sh
```

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

If you want to run the experiment with training, place the training and validation files under the configs `ur_dataset`. 
This is sadly necessary as otherwise they fight over file-locks. 

## Limitations 

Sometimes, sadly, the preprocessing fails for certain entries. 
A guide what to remove from the original test-data is in [a nearby file](./removal-info.txt) but the cleaned dataset will also be provided.

I am currently investigating this and will provide a dataset that is not failing in the preprocessing.


## Known Issues & Workarounds

For the Experiments containing the *If-True*-Transformer there is a little issue with the data as some characters are un-escaped. 
The bad candidate are (always) 4 entries containing an unescaped `\u00` or `\x00`. 
If this error appears, on linux systems just navigate next to the configs folder and run: 

```shell
find configs -type f -exec \
    sed -i 's/\u00/\\u00/g' {} \;
find configs -type f -exec \
    sed -i 's/\x00/\\x00/g' {} \;
```

This will replace all instances with the correct escaped ones.