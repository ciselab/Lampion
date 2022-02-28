# Lampion Experiment - OpenVocabCodeNLM

**Important:** The OpenVocabCodeNLM Experiment has not been conducted and is hence unfinished. 
The reason is, that due to model-architecture the computational cost rose exponentially with longer data-input. The Data Augmentation unfortunately increases the files, hence leading to un-doable-growth. 

In theory, all of this runs and is compatible. We just don't have the model / computational power yet. 

If you want to extend / mitigate this, 
you might want to consider:

- Only applying transformations that keep tokens
- Throw out big datapoints after augmentation
- Only Augment Small Datapoints
- Making a smaller model

The smaller model has been tried in "trial-augment-run" and seems to work, but it would be hard to draw conclusions except to ourselves in terms of improvements gained. 
As this is a research project for me, I could not continue "playing around with it" unless it gives me papers :man-shrugging:

## Structure (and Order)

1. [Dataset-Preparation](./Dataset_Preparation) to have examples of how to augment the data
2. [Grid-Dataset-Augmentation](tbd) the actual grid-experiment and values used to create all (augmented) datapoints
3. [Preprocessing](./Preprocessing) Sources to build the container that moves from .python files to the required encoded dataset files
4. [trial-run-augment](./Grid_Preprocessing) the actual grid-experiment to move from the Grid-Dataset-Augmentation datasets to usable files

## Requirements

1. Docker 
2. Docker + GPUs (in case you want to run the Grid-OpenVocab Considerably fast)
3. Images build from this repository (namely [lampion/openvocabcodenlm-preprocessing](./Preprocessing/Dockerfile) and [lampion/python-transformer](../../Transformer/Python/Dockerfile))
4. [OpenVocabCodeNLM GPU Container Reproduction Image](https://github.com/ciselab/OpenVocabCodeNLM) (Either build there, or pulled)

## Hardware & Time Estimates

Test-Data Augmentation: 50k Transformations for 16k Datapoints: 90 Minutes

Train Data Augmentation: 50k Trans for 13k Datapoints: 80 Minutes

(Below done with 3080ti)

Un-Augmented Training: 2h 

Un-Augmented Test: ~2 Min

Un-Augmented Completion Task: ~10h