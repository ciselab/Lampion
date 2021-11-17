# Transformer-Example
# https://libcst.readthedocs.io/en/latest/tutorial.html
import string
import random
import logging as log
from typing import Optional, Union

import libcst
import libcst as cst
import libcst.codegen.gather
from libcst import FlattenSentinel, RemovalSentinel

from lampion.transformers.basetransformer import BaseTransformer


class AddCommentTransformer(BaseTransformer):
    """
    Transformer that adds a random comment at a random position.

    Sometimes there is a probability that the Transformer is not applied just by randomness.
    For that case, just re-run it if it did not work.
    This is automatically done in Engine and documented in tests.

    Before:
    > def hi():
    >   print("Hello World")

    After:
    > def hi():
    >   # WKJNWHE MKPWEHÜ HG90ß15
    >   print("Hello World")

    """

    def __init__(self):
        log.info("AddCommentTransformer Created")
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
        # TODO: Is there a way to see all nodes and pick one by random.choice(allNodes) ?
        if random.random() < 0.05:
            self._worked = True
            # FlattenSentinels are what we want to replace 1 existing element (here 1 statement) with 1 or more statements
            # It takes care of things like indentation
            return cst.FlattenSentinel([added_stmt, updated_node])
        # Case 3: We did not alter it and chance was not triggered.
        # Re-Run the Transformer, better luck next time.
        else:
            return updated_node

    def reset(self) -> None:
        self._worked = False
        self._depth = 0
        self._stmts = 0

    def worked(self) -> bool:
        return self._worked

    def postprocessing(self) -> None:
        self.reset()

    def categories(self) -> [str]:
        return ["Naming","Comment"]

def _makeSnippet() -> "Node":
    pieces  = [_get_random_string(random.randint(1,8)) for x in range(1,random.randint(2,5))]

    comment = "# " + " ".join(pieces) + " \n"
    return libcst.parse_module(comment)


def _get_random_string(length: int) -> str:
    if length < 1:
        raise ValueError("Random Strings must have length 1 minimum.")
    # choose from all lowercase letter
    letters = string.ascii_letters + string.digits
    first_letter = random.choice(string.ascii_lowercase)
    result_str = ''.join(random.choice(letters) for i in range(length - 1))
    return first_letter + result_str
