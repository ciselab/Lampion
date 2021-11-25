from __future__ import annotations

import os
import sys

import libcst as cst
import logging as log
import random

from libcst import CSTNode

from lampion.components.engine import Engine


def run(path_to_code:str ,path_to_config:str = None, output_prefix:str = "lampion_output") -> None:
    """
    Primary function to read the files, read the configuration, and run the engine.
    Separated from main for testability, as main() needs sys-args.

    :param path_to_code: Path to Directory or File of Code.
    :param path_to_config: Path to Configuration to read in.
    :param output_prefix: Prefix to put before the output, will be created if not existing.
    :return: None
    """
    log.info(f'Welcome to the Lampion-Python-Transformer')
    log.info(f"Reading File(s) from {path_to_code}")

    csts = read_input_dir(path_to_code)
    config = read_config_file(path_to_config)

    # Set seed a-new if one was found in config.
    if config["seed"] and isinstance(config["seed"],int):
        random.seed(config["seed"])

    engine = Engine(config, output_prefix)

    engine.run(csts)[0]

    log.info("Python Transformer finished - exiting")


def read_input_dir(path: str) -> [(str, CSTNode)]:
    """
    This method parses a given path to one or more Libcst Modules.
    It handles that you can pass the path to one file or to a folder .
    In case of a folder, all .py files are read in.

    Every found CST gets it's (relative) path safed to it, to enable later printing.

    :param path: The (relative or absolute) path to either a .py-file or a folder containing .py-files.
    :return: the parsed libcst modules of the found python files with an ID assigned to it
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
            # TODO: Exclude __init__.py files? But keep it in Lookup?
            for f in fnames:
                if f.endswith(".py"):
                    ff = _file_to_string(os.path.join(dirpath, f))
                    found_cst = cst.parse_module(ff)
                    results.append((os.path.join(dirpath, f), found_cst))
        return results
    # Case 2: Path is a file
    elif os.path.isfile(path):
        log.debug("Received Path points to a File")
        # ErrorCase: File is not .py
        if not path.endswith(".py"):
            raise ValueError("File Path did not end in .py!")
        f = _file_to_string(path)
        found_cst = cst.parse_module(f)
        return [(path, found_cst)]
    # ErrorCase: Weird Path
    else:
        raise ValueError("Your path seemed to be neither a directory nor a file!")


def read_config_file(path: str) -> dict:
    """
    Reads the Configuration-File at a given path.
    The Configuration File is supposed to be named .properties and consist of key=value-pairs.
    Any Boolean or int is parsed for the dictionary, everything that did not match this is kept as a string.
    Error Handling for bad-string input is pushed towards the methods relying on the config.

    A default config is given, so it is possible to give a partial config and result in a running program.
    Any default value is overwritten by the found config.
    :param path: the path to the config file, "None" for going with default values
    :return: a dictionary parsed from the configuration file
    :raises: ValueError in case of non-existing file.
    """
    # Shortwire default values
    if path is None:
        log.info("Received \"None\" as path to config file, going with default values")
        return {}

    # Check Input-Values
    if not os.path.exists(path) or not os.path.isfile(path):
        raise ValueError("Path to ConfigFile did not exist or was not a file!")
    if ".properties" not in path:
        log.warning(f"The configuration-file at {path} did not end with the expected .properties")

    # Read in File, check for emptyness
    lines: [str] = []
    with open(path,"r") as config_file:
        lines = config_file.readlines()
    if not lines:
        log.warning(f"The configuration-file at {path} was empty")
        return {}
    # Split the Lines and add to tuples
    tuples: [(str,str)] = []
    for line in lines:
        # Filter out Empty Lines, and other tokens like Line-ends \n
        if line.strip() == "":
            continue
        if line.startswith("#"):
            continue
        if "=" not in line:
            continue
        # Remove bad / unwanted elements from the line before splitting
        cleaned_line = line.strip().replace(" ","").replace("\n","")
        parts = cleaned_line.split("=",1)
        tuples.append((parts[0],parts[1]))
    # Try to parse the tuples and return dict
    config: dict = {}
    for tuple in tuples:
        # Try to parse to bool
        parsed_value = __str2bool(tuple[1])
        if isinstance(parsed_value,bool):
            config[tuple[0]] = parsed_value
            continue
        # Try to parse to int
        parsed_value = __str2int(tuple[1])
        if isinstance(parsed_value,int):
            config[tuple[0]] = parsed_value
            continue
        # otherwise keep it
        config[tuple[0]] = tuple[1]
    # Return the Dict
    return config

def __str2bool(value) -> str | bool:
    """
    Parses a str to bool if possible. Returns the str otherwise.
    """
    if value.lower() in ["yes","true","1"]:
        return True
    if value.lower() in ["no", "false", "0"]:
        return False
    return value

def __str2int(value) -> str | int:
    """
    Parses a str to int if possible. Returns the str otherwise.
    """
    if value.isnumeric():
        return int(value)
    return value

def _file_to_string(path: str) -> str:
    with open(path, 'r') as file:
        return file.read()


def main() -> None:
    """
    Reads / Checks Sys-Args and runs engine accordingly.
    Also sets global elements like a default seed (in case config has none) and logging.

    Separated from run() for testability, as this main needs sys-args and run() is "clean".
    :return: None, exit 0 on success.
    """
    # TODO: Check ARGnum
    # TODO: Test for missing args
    path = sys.argv[1]

    random.seed(69)

    log.basicConfig(filename='lampion.log', encoding='utf-8', level=log.DEBUG)
    log.getLogger().addHandler(log.StreamHandler(sys.stdout))

    run(path)

    os.system.exit(0)