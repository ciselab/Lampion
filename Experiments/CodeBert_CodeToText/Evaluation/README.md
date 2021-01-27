# Codebert Code-To-Text Evaluation

This part of the repository contains the evaluation of the [Grid Experiment](../GridExperiment).

The results should be placed under /data and be addressed from within the notebook. 

## Data 

The data should contain the `config.properties`-file and the `test_0.output`.
The structure should be as follows: 

```
./data
    /GridExp_XY
        /configs
            /reference
                test_0.gold
                test_0.output
                bleu.txt (optional, can be created in notebook)
            /config_0
                config.properties
                test_0.gold
                test_0.output
                bleu.txt (optional, can be created in notebook)
            /config_1
                config.properties
                test_0.gold
                test_0.output
                bleu.txt (optional, can be created in notebook)
    ...
```

Make sure that the reference was created using the same dataset, 
e.g. if data has been filtered out that the reference still reflects the exact same test-set.

## Licence

The [bleu_evaluator.py](bleu_evaluator.py) is from the microsoft repository and might follow a different licence.

Everything else is MIT Licenced.