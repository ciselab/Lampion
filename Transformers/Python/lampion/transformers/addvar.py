# Transformer-Example
# https://libcst.readthedocs.io/en/latest/tutorial.html
import string
import random
from typing import Optional, Union


import libcst
import libcst as cst
import libcst.codegen.gather
from libcst import MaybeSentinel, FlattenSentinel, RemovalSentinel

#TODO: Flag for types?

class AddVariableTransformer(cst.CSTTransformer):

    def __init__(self):
        print("AddVariableTransformer Init called")
        self.depth : int = 0
        self.stmts : int = 0

    depth = 0
    stmts = 0

    # TODO: Apply it heuristically with a certain chance to be applied, so that it is not 1000 times everywhere
    # TODO: Exit after one application (with a flag or something?)

    def visit_SimpleStatementLine(self, node: "SimpleStatementLine") -> Optional[bool]:
        self.depth = self.depth + 1


    def leave_SimpleStatementLine(
        self, original_node: "SimpleStatementLine", updated_node: "SimpleStatementLine"
    ) -> Union["BaseStatement", FlattenSentinel["BaseStatement"], RemovalSentinel]:
        self.depth = self.depth - 1
        self.stmts = self.stmts + 1

        added_stmt = _makeSnippet()
        # Flatten Sentinels are what we want to replace 1 existing element (here 1 statement) with 1 or more statements
        # It takes care of things like indentation
        return cst.FlattenSentinel([added_stmt,updated_node])


def _makeSnippet():
    name = _get_random_string(10)
    #TODO: make random value of different types
    value = random.randint(2,1000)

    return libcst.parse_statement(f"{name} = {value}")


def _get_random_string(length):
    if length<1:
        raise ValueError("Random Strings must have length 1 minimum.")
    # choose from all lowercase letter
    letters = string.ascii_letters + string.digits
    first_letter = random.choice(string.ascii_lowercase)
    result_str = ''.join(random.choice(letters) for i in range(length-1))
    return first_letter + result_str
    #print("Random string of length", length, "is:", result_str)
