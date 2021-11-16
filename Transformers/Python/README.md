# Python Transformer

This part of the Lampion Project alters Python Files using metamorphic transformations 
and returns/writes the altered files as well as a manifest.

It is currently in early development.


## Build & Run
```
pip install libcst
```

Build (in Python-Root): 

```
python -m build
```

Test (in Python-Root):

```
python -m pytest tests/
```
Or with coverage `python -m pytest --cov=lampion tests/`


Install the python transformer

``` 
pip install --force-reinstall .\dist\lampion_python_transformer-0.0.1-py2.py3-none-any.whl
```

Run with: 
``` 
python -m lampion
```

Check linting with:
``` 
pylint ./lampion
```

## Requirements

- Docker 11+
- [Alternative] Python 3.9 + Pip

## Built with:

Build with [LibCST](https://github.com/Instagram/LibCST)

Package structure from the [pypi tutorial](https://packaging.python.org/tutorials/packaging-projects/)
