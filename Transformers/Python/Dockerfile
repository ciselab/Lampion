ARG PYTHON_VERSION=3.9.7
FROM python:${PYTHON_VERSION}

ARG LAMPION_VERSION=0.0.1
ARG WHEEL_POSTFIX=py2.py3-none-any

LABEL maintainer="L.H.Applis@tu-delft.nl"

# Where the lampion should be applied/run, default to a sample file
ENV target="/lampion_target/hello_world.py"
ENV configfile=""

WORKDIR /lampion

#Copy project files
COPY lampion ./lampion
COPY tests ./tests
COPY LICENSE .
COPY README.md .
COPY setup.cfg .
COPY pyproject.toml .
COPY entrypoint.sh .
COPY requirements.txt .

#Copy the example
COPY tests/test_inputs/hello_world.py /lampion_target/

RUN pip install --upgrade pytest libcst build

RUN python3 -m pytest tests/
RUN python3 -m build --wheel

RUN pip install /lampion/dist/lampion_python_transformer-${LAMPION_VERSION}-${WHEEL_POSTFIX}.whl

ENTRYPOINT ["sh","entrypoint.sh"]