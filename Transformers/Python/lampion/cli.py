"""
The CLI module contains
- a run function to perform the actual toplevel tasks
- a main function that parses args, sets up random and logging, and starts run
- read_input_dir to read files from path into LibCST nodes
- read_config_file to read a config file from path
- helpers to parse config values
"""
from __future__ import annotations

import os
import sys
import random
import argparse

import logging as log
import libcst as cst

from libcst import CSTNode
from lampion.components.engine import Engine


def run(path_to_code:str ,path_to_config:str = None, output_prefix:str = "lampion_output", print_sample_diff: bool = True) -> None:
    """
    Primary function to read the files, read the configuration, and run the engine.
    Separated from main for testability, as main() needs sys.args .

    :param path_to_code: Path to Directory or File of Code.
    :param path_to_config: Path to Configuration to read in.
    :param output_prefix: Prefix to put before the output, will be created if not existing.
    :param print_sample_diff: Whether or not to output one CST to Log. Default is true.
    :return: None
    """
    log.info('Welcome to the Lampion-Python-Transformer')
    log.info("Reading File(s) from %s",path_to_code)

    csts = read_input_dir(path_to_code)
    config = read_config_file(path_to_config)

    # Set seed a-new if one was found in config.
    if "seed" in config.keys() and config["seed"] and isinstance(config["seed"],int):
        random.seed(config["seed"])

    engine = Engine(config, output_prefix)

    altered_csts = engine.run(csts)

    if print_sample_diff:
        initial_tuple = random.choice(csts)
        altered_tuple = [x for x in altered_csts if x[0] == initial_tuple[0]][0]

        initial_cst_code = str((initial_tuple[1]).code)
        altered_cst_code = str((altered_tuple[1]).code)
        
        # This is intentionally print and not logging 
        print("Before:\n")
        print(initial_cst_code)
        print("\n")
        print("After:\n")
        print(altered_cst_code)
        print("\n")

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
        log.info("Received Path is a Directory - Looking for multiple files")
        log.info("This might take a while ... (run in debug to see process)")
        results = []
        fails = []
        file_counter = 0
        for dirpath, _, fnames in os.walk(path):
            # TODO: Exclude __init__.py files? But keep it in Lookup?
            for f in fnames:
                if f.endswith(".py"):
                    file_counter += 1
                    file_path:str = os.path.join(dirpath, f)
                    log.debug("Parsing ... %s",file_path)
                    try:
                        file_content:str = _file_to_string(file_path)
                        found_cst = cst.parse_module(file_content)
                        log.debug("Parsed %s",file_path)
                        results.append(file_path, found_cst)
                    except:
                        # Known (common) possible Errors: 
                        # UTF8-Error when reading files with strange encodings
                        # ParserSyntaxError when reading old python (v2)
                        # Type-Error for rare cases
                        fails = fails.append(file_path) if fails else [file_path]
                        log.debug("Failure in Parsing %s",file_path)
        # Log Stats of failures and explicitly which files
        if fails and len(fails)>0 :
            log.info("Failed Paths (%d) to parse:",len(fails))
            for e in fails:
                log.info("\t %s",e)
        log.info("Found and (successfully) parsed %d of %d files",len(results),file_counter)
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
        log.warning("The configuration-file at %s did not end with the expected .properties",path)

    # Read in File, check for emptyness
    lines: [str] = []
    with open(path,"r") as config_file:
        lines = config_file.readlines()
    if not lines:
        log.warning("The configuration-file at %s was empty",path)
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
    for tup in tuples:
        # Try to parse to bool
        parsed_value = __str2bool(tup[1])
        if isinstance(parsed_value,bool):
            config[tup[0]] = parsed_value
            continue
        # Try to parse to int
        parsed_value = __str2int(tup[1])
        if isinstance(parsed_value,int):
            config[tup[0]] = parsed_value
            continue
        # otherwise keep it
        config[tup[0]] = tup[1]
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
    """
    Reads in a whole file under path, returns it's content.
    File is treated as read-only.
    :param path: the path where to look for the file
    :return: the file's content
    :raises: FileNotFoundError if file does not exist
    """
    with open(path, 'r') as file:
        return file.read()

def main() -> None:
    """
    Reads / Checks Sys-Args and runs engine accordingly.
    Also sets global elements like a default seed (in case config has none) and logging.

    Separated from run() for testability, as this main needs sys-args and run() is "clean".
    :return: None, exit 0 on success.
    """

    parser = argparse.ArgumentParser(
        description='Applies metamorphic transformations to Python Code '
                    'in Order to make it verbose & different but functionally identical'
    )
    parser.add_argument('config', metavar='config', type=str, nargs=1,
                        help='The config file to use with the transformer')
    parser.add_argument('input', metavar='input', type=str, nargs=1,
                        help='A path to either a folder containing .py files or a path to a .py file')
    parser.add_argument('output',metavar='output', type=str, nargs=1, default="lampion_output",
                        help="Prefix for the folder to place output in. "
                             "Within this new folder, the initial structure will be replicated. "
                             "Any files will be overwritten.")

    parser.add_argument('loglevel',metavar="log", type=str, nargs="?", default="info",
                        help="The loglevel for printing logs. Default \'info\'. supported: \'warn\',\'info\',\'debug\'" )

    parser.add_argument('example', metavar="exp", type=bool, nargs="?", default=True,
                        help="Whether or not to print an example of a changed file. ")

    args = parser.parse_args()

    path = args.input[0]
    config = args.config[0]
    output = args.output[0]
    example = args.example

    loglevel = log.INFO
    if args.loglevel.lower() == "debug":
        loglevel = log.DEBUG
    elif args.loglevel.lower() == "info":
        loglevel = log.INFO
    elif args.loglevel.lower() == "warn":
        loglevel = log.WARNING
    else:
        print("Received unknown/unsupported format for loglevel - defaulting to info (%s)",args.loglevel.lower())
    
    log.basicConfig(level=loglevel,format='%(asctime)s [%(levelname)s]\t%(message)s')
   
    random.seed(19961106)

    run(path_to_code=path,path_to_config=config,output_prefix=output,print_sample_diff=example)

    sys.exit(0)
