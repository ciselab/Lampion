# Transformer-Example
# https://libcst.readthedocs.io/en/latest/tutorial.html
import string
import random
import logging as log
from typing import Optional, Union

import libcst
import libcst as cst
import libcst.codegen.gather
from libcst import FlattenSentinel, RemovalSentinel, CSTNode

from lampion.transformers.basetransformer import BaseTransformer
from lampion.utils.naming import get_random_string


class AddVariableTransformer(BaseTransformer):
    """
    Transformer that adds a random unused variable at a random position.

    Sometimes there is a probability that the Transformer is not applied just by randomness.
    For that case, just re-run it if it did not work.
    This is automatically done in Engine and documented in tests.

    Before:
    > def hi():
    >   print("Hello World")

    After:
    > def hi():
    >   hgwe: str = "anjlkgwe"
    >   print("Hello World")

    Currently it can add ints, floats, doubles and strings.
    Adding the type is an optional flag.
    """

    def apply(self, cst: CSTNode) -> CSTNode:
        visitor = self.__AddVarVisitor()

        altered_cst = cst

        tries: int = 0
        max_tries : int = 100

        while (not self._worked) and  tries <= max_tries:
            altered_cst = cst.visit(visitor)
            self._worked = visitor.finished
            tries = tries + 1

        if tries == max_tries:
            log.warning(f"Add Variable Visitor failed after {max_tries} attempt")

        #TODO: add Post-Processing Values here

        return altered_cst

    def reset(self) -> None:
        self._worked = False

    def worked(self) -> bool:
        return self._worked

    def categories(self) -> [str]:
        return ["Naming","Smell"]

    def postprocessing(self) -> None:
        self.reset()


    class __AddVarVisitor(cst.CSTTransformer):

        def __init__(self):
            log.debug("AddVariableVisitor Created")
            self.finished = False

        finished = False

        def visit_SimpleStatementLine(self, node: "SimpleStatementLine") -> Optional[bool]:
            return

        def leave_SimpleStatementLine(
                self, original_node: "SimpleStatementLine", updated_node: "SimpleStatementLine"
        ) -> Union["BaseStatement", FlattenSentinel["BaseStatement"], RemovalSentinel]:

            # Case 1: We successfully applied the Transformer, exit early, do nothing.
            if self.finished:
                return updated_node

            added_stmt = self._makeSnippet()

            # Case 2: We did not alter yet, at the current (random) statement apply it in 1 of 20 cases.
            # TODO: this has a slight bias towards early nodes if the file is long?
            # TODO: Is there a way to see all nodes and pick one by random.choice(allNodes) ?
            if random.random() < 0.05:
                self.finished = True
                # FlattenSentinels are what we want to replace 1 existing element (here 1 statement) with 1 or more statements
                # It takes care of things like indentation
                return cst.FlattenSentinel([added_stmt, updated_node])
            # Case 3: We did not alter it and chance was not triggered.
            # Re-Run the Transformer, better luck next time.
            else:
                return updated_node


        _supported_types = ["int", "float", "str"]
        _add_types = True

        def _makeSnippet(self) -> CSTNode:
            name = get_random_string(10)

            type = random.choice(self._supported_types)

            if self._add_types:
                name = f"{name}: {type}"

            value = ""
            if type == "str":
                value = f"\"{get_random_string(random.randint(3,30))}\""
            if type == "int":
                value = random.randint(2, 1000)
            if type == "float":
                value = random.random()

            return libcst.parse_statement(f"{name} = {value}")

