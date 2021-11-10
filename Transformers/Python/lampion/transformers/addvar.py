# Transformer-Example
# https://libcst.readthedocs.io/en/latest/tutorial.html
from typing import Optional, Union

import libcst as cst
import libcst.codegen.gather
from libcst import MaybeSentinel, FlattenSentinel, RemovalSentinel


class AddVariableTransformer(cst.CSTTransformer):
    def __init__(self):
        print("AddVariableTransformer Init called")

    def visit_IndentedBlock_body(self, node: "IndentedBlock") -> None:
        print("Visiting block: ", node)
        #p = libcst.AnnAssign()