# CodeBert - Code-To-Text

This experiment (and folder) is all about implementing the meta experiment around the CodeBert Code-To-Text 

## Summary

The CodeBERT Code-To-Text experiment uses a neural network based on RoBERTa which trains in a bi-modal fashion on code and documentation tokens. 
The model is supposed to learn corresponding pairs of tokens, this approach is similar to what image captioning does (Image captioning AIs learn on pairs of images and their captions).
After learning, the model should be able to generate Documentation for an previously unseen method. 
The documentation is evaluated against the actual documentation of the method using the BLEU-Score (a common translation metric). 

While the experiment from Microsoft/Nvidia has multiple languages, this experiment first focusses on java and python. 

## Structure

- *preprocessing* contains all items necessary to containerise the preprocessing done including the obfuscation --- 2 folders, one for java one for python. They use the same interface in docker-compose.
- *GridExperiment* will contain all elements necessary to run the experiment in a parameterized, grid fashion (to be done)
- *evaluation* contains the evaluation of the meta experiment, using the results of the grid experiment.

## Dataset - Preparation 

The original dataset had to be minimally adjusted. You can find the altered dataset [here](https://surfdrive.surf.nl/files/index.php/f/8713322177)

In case you want to re-do the dataset from scratch:

Remove the entries of the repository `wmixvideo/nfe` and the path ` src/main/java/com 
Fincatto/documentofiscal/mdfe3` as well as `fincatto/documentofiscal/cte300` in their path **in the valid.jsonl**.

From the validation set, remove the entries of the java validation file which have  in their path/url. 

From the test set, remove the entries of the java test file which have `base/base/src/main/com/thesett/util/log4j/` in their path with the repository `rupertlssmith/lojix` **and** `geopackage_sdk/src/main/java/mil/nga/geopackage/factory`.

The entries made issues with the obfuscation - they are addressed with the removal of 25 files in total (of 15000).
