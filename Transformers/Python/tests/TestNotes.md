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

## Transformer Max-Tries

Some tests are flaky due to the nature of transformers and re-trying. 
This is unfortunate, but in general it turned out to be relatively reliable with high-re-try numbers.
While this must not happen in real code, and the engine has a separate re-try logic, 
for the tests often a high max-tries is required.

It can also (very unfortunately) happen that changing max-tries somewhere changes the current random state, which will fail other tests. 

I maybe have to be very strict about randomness and reset it very often.

**Update** I found a major issue in the Literal-Collector-Visitor. 
I extracted and tested it, it collected literals over multiple runs that were then 
filling up stuff in the wrong places. 
I.e., the second test had variables from the first tests, which may fail then by picking non-existing literals for transformation..


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