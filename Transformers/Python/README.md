


## Info:

Build with [LibCST](https://github.com/Instagram/LibCST)

Package structure from the [pypi tutorial](https://packaging.python.org/tutorials/packaging-projects/)

## Setup
```
pip install libcst
```

Build (in Python-Root): 

```
python -m build
```

Test (in Python-Root):

```
pytest
```

Install the python transformer

``` 
pip install --force-reinstall .\dist\lampion_python_transformer-0.0.1-py2.py3-none-any.whl
```

Run with: 
``` 
python -m lampion
```