# OpenVocabCodeNLM - Grid Dataset Augmentation

This folder contains the files to make all files required for a grid augmentation run. 
That is, you run the [make_augment_grid](./make_augment_grid.py)-script here, and you get a set of configurations and docker-composes to run in order. 
After running the composes, you get a set of files required for the further processing.

Summarized:

1. Adjust the config here
2. Run the script
3. You now have a lot of composes, configs and one orchestrating file
4. you can now ship this / keep it for reproduction
5. Run the composes
6. You now have a lot of altered files / datasets

## Requirements 

- The preprocessing image
- The python-transformer-lampion image
- The (cleaned) raw data
- The (python) encodings required in preprocessing
- the requirements here installed, in case you want to make your own composes (above step 2)


## Required Structure

``` 
data/
├── test
├── train
└── valid
```