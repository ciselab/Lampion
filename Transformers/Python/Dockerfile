FROM python:3.9.7

ARG LAMPION_VERSION=0.0.1
ARG WHEEL_POSTFIX=py2.py3-none-any

WORKDIR /lampion

COPY src ./src
COPY tests ./tests
COPY LICENSE .
COPY README.md .
COPY setup.cfg .
COPY pyproject.toml .


RUN pip install --upgrade pytest libcst build

RUN pytest -q
RUN python3 -m build --wheel


#RUN pip install --force-reinstall --only-binary=./dist/lampion_python_transformer-${LAMPION_VERSION}-${WHEEL_POSTFIX}.whl ./dist/lampion_python_transformer-${LAMPION_VERSION}-${WHEEL_POSTFIX}.whl
RUN pip install /lampion/dist/lampion_python_transformer-${LAMPION_VERSION}-${WHEEL_POSTFIX}.whl

#ENTRYPOINT ["tail","/dev/null","-f"]
#./usr/local/lib/python3.9/site-packages/lampion

ENTRYPOINT ["python","-m","lampion"]