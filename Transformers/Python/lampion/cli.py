import sys

import libcst as cst
import logging as log
import random

from lampion.components.engine import Engine


def dry_run(path):
    log.info(f'Welcome to the Lampion-Python-Transformer')

    log.info(f"Reading File(s) from {path}")
    # Read a sample file with hellow world
    f = file_to_string(path)
    sample_cst = cst.parse_module(f)

    engine = Engine({}, "PLACEHOLDER", "PLACEHOLDER")

    some = engine.run([sample_cst])[0]
    log.debug("========================")
    log.debug(sample_cst.code)
    log.debug("========================")
    log.debug("         Goes To        ")
    log.debug("========================")
    log.debug(some.code)


def file_to_string(path):
    # TODO: Check Folder vs. File
    with open(path, 'r') as file:
        return file.read()


def main():
    # TODO: Check ARGnum
    # TODO: Test for missing args
    path = sys.argv[1]

    random.seed(69)

    log.basicConfig(filename='lampion.log', encoding='utf-8', level=log.DEBUG)
    log.getLogger().addHandler(log.StreamHandler(sys.stdout))

    dry_run(path)
