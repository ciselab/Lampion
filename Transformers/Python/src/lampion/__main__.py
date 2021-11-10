import sys

import libcst as cst
from libcst.tool import dump


def dry_run(path):
    print(f'Welcome to the Lampion-Python-Transformer')
    print("Sample with non-read expression")
    # Check the CST Example
    print(dump(cst.parse_expression("(25 + 2)")))

    print("Reading Sample File")
    # Read a sample file with hellow world
    f = file_to_string(path)
    hello_world_cst = cst.parse_module(f)
    print(dump(hello_world_cst))

    # Transformer-Example
    # https://libcst.readthedocs.io/en/latest/tutorial.html

    # Packaging Tutorial?
    # https://packaging.python.org/

def file_to_string(path):
    with open(path, 'r') as file:
        return file.read()


if __name__ == '__main__':
    path = sys.argv[1]
    dry_run(path)
