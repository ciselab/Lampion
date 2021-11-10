import sys

import libcst as cst
from libcst.tool import dump

from src.lampion.transformers.renamevar import RenameParameterTransformer


def dry_run(path):
    print(f'Welcome to the Lampion-Python-Transformer')

    print("Reading File")
    # Read a sample file with hellow world
    f = file_to_string(path)
    sample_cst = cst.parse_module(f)

    print("Starting my Transformer")

    homebrew_transformer = RenameParameterTransformer()
    sample_cst.visit(homebrew_transformer)


def file_to_string(path):
    # TODO: Check Folder vs. File
    with open(path, 'r') as file:
        return file.read()


if __name__ == '__main__':
    # TODO: Check ARGnum
    # TODO: Test for missing args
    path = sys.argv[1]
    dry_run(path)
