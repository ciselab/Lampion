# Lampion

This project aims to help you with explainability of your Codebased ML-Models. 
It is based on the idea of [Metamorphic Transformations]() which alter the code-syntax but keep the meaning of the code. 
When applied to bytecode, this is often called *Obfuscation*, and examples are changing variablesnames or introducing dead code.

The provided Java-Obfuscator is configurable to provide a number of metamorphic transformations on SourceCode and in addition produces a *Alternation Manifest* - a record what has been changed how much. 
With this record, and the results of the Model under Test, the visualizer is able to determine correlations what transformations affect your model how heavily.

![Overview](./Resources/Overview.PNG)

## Getting Started

Further information as well as instructions on the components can be in their repositories. 

The folder *Manifest-Schema* holds sql-files to generate a valid schema required for visualisation.

General information on design decisions can be found in the [Design-Notes](./Resources/DesignNotes.md).
