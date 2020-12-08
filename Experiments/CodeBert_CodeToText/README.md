# CodeBert - Code-To-Text

This experiment (and folder) is all about implementing the meta experiment around the CodeBert Code-To-Text 

## Summary

The CodeBERT Code-To-Text experiment uses a neural network based on RoBERTa which trains in a bi-modal fashion on code and documentation tokens. 
The model is supposed to learn corresponding pairs of tokens, this approach is similar to what image captioning does (Image captioning AIs learn on pairs of images and their captions).
After learning, the model should be able to generate Documentation for an previously unseen method. 
The documentation is evaluated against the actual documentation of the method using the BLEU-Score (a common translation metric). 

While the experiment from Microsoft/Nvidia has multiple languages, this experiment first focusses on java. 

## Structure

- *Preprocessing* contains all items necessary to containerise the preprocessing done including the obfuscation
- *GridExperiment* will contain all elements necessary to run the experiment in a parameterized, grid fashion (to be done)
- *Visualisation* will contain the evaluation of the meta experiment, using the results of the grid experiment (to be done)

## Further Reading 

- [Microsofts Repository](https://github.com/microsoft/CodeXGLUE/tree/main/Code-Text/code-to-text)
- [Reproduction Package](https://github.com/ciselab/CodeBert-CodeToText-Reproduction)
