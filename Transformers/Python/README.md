# Python Transformer

This part of the Lampion Project alters Python Files using metamorphic transformations 
and returns/writes the altered files as well as a manifest (*Manifest still pending*).

It is currently in early development, I implemented every feature for the first trials, 
but I expect bugs popping up when you use them on actual data.


## Build & Run
```bash
pip install libcst build pytest pytest-cov
```

Build (in Python-Root): 

```bash
python -m build
```

Test (in Python-Root):

```bash
python -m pytest tests/
```
Or with coverage `python -m pytest --cov=lampion tests/`


Install the python transformer

```bash
pip install --force-reinstall ./dist/lampion_python_transformer-0.0.1-py2.py3-none-any.whl
```

Run with: 
```bash
python -m lampion ./tests/test_configs/test1.properties ./tests/test_inputs/hello_world.py ./lampion_output
```

Check linting with:
``` bash
pylint --rcfile=.pylintrc ./lampion
```

### Docker

```bash
docker build -t lampion/python-transformer:unstable .
docker run lampion/python-transformer:unstable
```

## Requirements

- Docker 11+
- [Alternative] Python 3.9 + Pip

## Built with:

Build with [LibCST](https://github.com/Instagram/LibCST)

Package structure from the [pypi tutorial](https://packaging.python.org/tutorials/packaging-projects/)
