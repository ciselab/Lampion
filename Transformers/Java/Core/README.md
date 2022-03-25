# CORE library part of the Java Transformer

This part of the Lampion Project is the CORE library. 
Here the transformers and the main engine are located.

## How to add a transformer
All transformers extend the BaseTransformer so this would be a good starting point on how a transformer is built.
A new transformer should only change the look of the code but no functionality. This will change the AST that is given as a parameter in the applyAtRandom method.

All new parameters have certain constraints to which they need to adhere. Examples of this would be the following three constraints for the rename variable transformer:

1. There are methods in the Ast
2. The methods have variables
3. There are methods that have not-randomized / not altered names (altering them twice would be useless)

Besides the constraints, all new transformers will also have two constructors, one with a seed parameter and one without.
And an applyAtRandom method which is called in *Engine.java* and is the entry point to the transformer.
Lastly, all new transformers need to be added to the list of transformers in the CLI.