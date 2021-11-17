import os
import sys

import libcst as cst
import logging as log
import random

from lampion.components.engine import Engine


def dry_run(path):
    log.info(f'Welcome to the Lampion-Python-Transformer')
    log.info(f"Reading File(s) from {path}")

    csts = read_input_dir(path)

    engine = Engine({}, "PLACEHOLDER")

    some = engine.run(csts)[0]

    log.debug("========================")
    log.debug(csts[0].code)
    log.debug("========================")
    log.debug("         Goes To        ")
    log.debug("========================")
    log.debug(some.code)


def read_input_dir(path: str) -> ["Node"]:
    """
    This method parses a given path to one or more Libcst Modules.
    It handles that you can pass the path to one file or to a folder .
    In case of a folder, all .py files are read in.

    :param path: The (relative or absolute) path to either a .py-file or a folder containing .py-files.
    :return: the parsed libcst modules of the found python files
    :raises: ValueError if the path was not found / did not exist
    :raises: LibCst Error if the files were faulty and not parsable.
    """
    # ErrorCase: Path does not exist.
    if not os.path.exists:
        raise ValueError("Path was not found!")

    # Case 1: Path is a directory
    if os.path.isdir(path):
        log.debug("Received Path is a Directory - Looking for multiple files")
        results = []
        for dirpath, dnames, fnames in os.walk(path):
            #TODO: Create a lookup of files:cst ?
            #TODO: Exclude __init__.py files? But keep it in Lookup?
            for f in fnames:
                if f.endswith(".py"):
                    ff = _file_to_string(os.path.join(dirpath, f))
                    found_cst = cst.parse_module(ff)
                    results.append(found_cst)
        return results
    # Case 2: Path is a file
    elif os.path.isfile(path):
        log.debug("Received Path points to a File")
        # ErrorCase: File is not .py
        if not path.endswith(".py"):
            raise ValueError("File Path did not end in .py!")
        f = _file_to_string(path)
        found_cst = cst.parse_module(f)
        return [found_cst]
    # ErrorCase: Weird Path
    else:
        raise ValueError("Your path seemed to be neither a directory nor a file!")


def _file_to_string(path: str):
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
