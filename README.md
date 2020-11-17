# Lampion

This project aims to help you with explainability of your Codebased ML-Models.
It is based on the idea of [Metamorphic Transformations](https://en.wikipedia.org/wiki/Metamorphic_code) which alter the code-syntax but keep the meaning of the code.
When applied to bytecode, this is often called *Obfuscation*, and examples are changing variablesnames or introducing dead code.

The provided Java-Obfuscator is configurable to provide a number of metamorphic transformations on SourceCode and in addition produces a *Alternation Manifest* - a record what has been changed how much.
With this record, and the results of the Model under Test, the visualizer is able to determine correlations what transformations affect your model how heavily.

![Overview](./Resources/Overview.PNG)

## Getting Started

Further information as well as instructions on the components can be in their sub-folders.

The folder *Manifest-Schema* holds sql-files to generate a valid schema required for visualisation.

General information on design decisions can be found in the [Design-Notes](./Resources/DesignNotes.md).

A examples and reasoning on the metamorphic transformations can be found in [Transformations.md](./Resources/Transformations.md).

## Related & Similar Work

The Paper [Embedding Java Classes with code2vec: Improvements from Variable Obfuscation](https://arxiv.org/pdf/2004.02942.pdf) and it's accompanying [repository](https://github.com/basedrhys/obfuscated-code2vec) investigate the impact of changing variable names on the performance / robustness of Code2Vec based models.
With variable renaming being a subset of this work, it can be seen as related work with a different goal.
Another similar work is from [the code2vec authors](https://github.com/tech-srl/code2vec) with their paper [Adversarial Examples for Models of Code](https://arxiv.org/pdf/1910.07517.pdf).
In their paper they use common adversarial generation to create certain (wrong) predictions by changing variable names or introducing newly, unused variables. 
This is another sub-part of this project, but instead of going for explainability they go straight for exploits on the system.

A more precise differentiation will be done in an actual publication.
