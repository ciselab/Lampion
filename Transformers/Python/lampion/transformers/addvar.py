# Transformer-Example
# https://libcst.readthedocs.io/en/latest/tutorial.html
import string
import random
import logging as log
from typing import Optional, Union

import libcst
import libcst as cst
import libcst.codegen.gather
from libcst import MaybeSentinel, FlattenSentinel, RemovalSentinel

# TODO: Flag for types?
from lampion.transformers.basetransformer import BaseTransformer


class AddVariableTransformer(BaseTransformer):

    def __init__(self):
        log.info("AddVariableTransformer Created")
        self._depth: int = 0
        self._stmts: int = 0
        self._worked = False

    _depth = 0
    _stmts = 0
    _worked = False

    def visit_SimpleStatementLine(self, node: "SimpleStatementLine") -> Optional[bool]:
        self._depth = self._depth + 1

    def leave_SimpleStatementLine(
            self, original_node: "SimpleStatementLine", updated_node: "SimpleStatementLine"
    ) -> Union["BaseStatement", FlattenSentinel["BaseStatement"], RemovalSentinel]:

        # Case 1: We successfully applied the Transformer, exit early, do nothing.
        if self._worked:
            return updated_node

        self._depth = self._depth - 1
        self._stmts = self._stmts + 1

        added_stmt = _makeSnippet()

        # Case 2: We did not alter yet, at the current (random) statement apply it in 1 of 20 cases.
        # TODO: this has a slight bias towards early nodes if the file is long?
        if random.random() < 0.05:
            self._worked = True
            # FlattenSentinels are what we want to replace 1 existing element (here 1 statement) with 1 or more statements
            # It takes care of things like indentation
            return cst.FlattenSentinel([added_stmt, updated_node])
        # Case 3: We did not alter it and chance was not triggered.
        # Re-Run the Transformer, better luck next time.
        else:
            return updated_node

    def reset(self):
        self._worked = False
        self._depth = 0
        self._stmts = 0

    def worked(self) -> bool:
        return self._worked

    def postprocessing(self):
        self.reset()

def _makeSnippet():
    name = _get_random_string(10)
    # TODO: make random value of different types
    value = random.randint(2, 1000)

    return libcst.parse_statement(f"{name} = {value}")


def _get_random_string(length):
    if length < 1:
        raise ValueError("Random Strings must have length 1 minimum.")
    # choose from all lowercase letter
    letters = string.ascii_letters + string.digits
    first_letter = random.choice(string.ascii_lowercase)
    result_str = ''.join(random.choice(letters) for i in range(length - 1))
    return first_letter + result_str
    # print("Random string of length", length, "is:", result_str)
