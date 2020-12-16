# CodeBert Code-To-Text Preprocessing 

This part of the repository builds a container that alternates the training/test data of the CodeBERT-Experiment using the Java-Obfuscator. 

It first makes .java files for all lines of the jsonl-dataset, 
then it runs the obfuscator, 
then it stitches the altered .java files back together into a new jsonl.

Place the dataset in the *compose_input*-folder, 
adjust the name in the docker-compose.yml, 
adjust the config to your liking, 

and run it with 

```
docker-compose up --build
```

For the Grid experiment, including the replication package and multiple configs, there will be a separate repository. 

The above given compose is just a step to verify this building brick of the meta experiment.    

## Requirements

The JavaObfuscator Image needs to be build/availible.
See the other part of the repository for instructions how to build it.

Docker 19+

## Limitations

As by the definition of the experiment, there are some limitations in what the obfuscator can do: 

1. The Experiment does not allow comments
2. Due to preprocessing reasons, any transformations that add methods will add the additional methods to the code of the experiment. This is debatable
3. As the references are missing, the code cannot compile, blocking some transformations to be applied multiple times (such as the Lambda Transformation)