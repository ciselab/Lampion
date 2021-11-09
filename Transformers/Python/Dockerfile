FROM python:3.9.7

WORKDIR /lampion

COPY src /lampion/src
COPY tests /lampion/tests
COPY LICENSE .
COPY README.md .
COPY setup.cfg .
COPY pyproject.toml .

RUN pip install pytest
RUN pip install --upgrade build

RUN pytest -q
RUN python3 -m build

ENTRYPOINT ["echo","hello world"]