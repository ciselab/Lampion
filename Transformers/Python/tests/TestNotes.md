# Test Nodes 

Some things I realized during testing, that I wanted to keep track of. 

## Randomness

Sometimes Tests work with random components. 
For example the engine takes random csts and 
random transformers and applies them to each other.

So I tried setting the seed for randomness in the test like 

```python 
import random as random 
random.seed(15)
```

Which seemed to reduce randomness, but some tests are still flaky. 
I am not certain how to deal with this to be honest, 
but I hope that once I have configuration properly running it all works out.

## CST Code Comparison

Strings are immutable in Python, but the code element of a cst is not. 
so: 

```python
old_code = cst.code
cst.code = "A"

assert old_code == "A"
```

will pass! 

One has to make a str copy: 
```python
old_code = str(cst.code)
cst.code = "A"

assert old_code == "A"
```

which fails and is the intended behavior.