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
from lampion.utils.naming import get_random_string, get_pseudo_random_string


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


    def __init__(self, string_randomness: str = "pseudo"):
        if (string_randomness == "pseudo") or (string_randomness == "full"):
            self.__string_randomness = string_randomness
        else:
            raise ValueError("Unrecognized Value for String Randomness, supported are pseudo and full")

        log.info("AddCommentTransformer Created")
        self._worked = False

    _worked = False
    __string_randomness: str

    def apply(self, cst: CSTNode) -> CSTNode:
        visitor = self.__AddCommentVisitor()

        altered_cst = cst

        tries: int = 0
        max_tries : int = 10

        while (not self._worked) and tries <= max_tries:
            altered_cst = cst.visit(visitor)
            self._worked = visitor.finished
            tries = tries + 1

        if tries == max_tries:
            log.warning(f"Add Comment Visitor failed after {max_tries} attempt")

        #TODO: add Post-Processing Values here

        return altered_cst

    def reset(self) -> None:
        self._worked = False

    def worked(self) -> bool:
        return self._worked

    def postprocessing(self) -> None:
        self.reset()

    def categories(self) -> [str]:
        return ["Naming","Comment"]

    class __AddCommentVisitor(cst.CSTTransformer):

        def __init__(self, string_randomness: str = "pseudo"):
            log.debug("AddVariableVisitor Created")
            self._finished = False
            self.__string_randomness = string_randomness

        finished: bool = False
        __string_randomness: str

        def visit_SimpleStatementLine(self, node: "SimpleStatementLine") -> Optional[bool]:
            return

        def leave_SimpleStatementLine(
                self, original_node: "SimpleStatementLine", updated_node: "SimpleStatementLine"
        ) -> Union["BaseStatement", FlattenSentinel["BaseStatement"], RemovalSentinel]:

            # Case 1: We successfully applied the Transformer, exit early, do nothing.
            if self.finished:
                return updated_node

            added_stmt = _makeSnippet(string_randomness=self.__string_randomness)

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


def _makeSnippet(string_randomness: str = "pseudo") -> CSTNode:
    if string_randomness == "full":
        pieces  = [get_random_string(random.randint(1,8)) for x in range(1,random.randint(2,5))]
    elif string_randomness == "pseudo":
        suppliers = [
            lambda: get_pseudo_random_string(with_keyword=True,with_job=False,with_animal=False,with_adjective=False),
            lambda: get_pseudo_random_string(with_keyword=False,with_job=True,with_animal=False,with_adjective=False),
            lambda: get_pseudo_random_string(with_keyword=False,with_job=False,with_animal=True,with_adjective=False),
            lambda: get_pseudo_random_string(with_keyword=False,with_job=False,with_animal=False,with_adjective=True),
        ]
        pieces  = [random.choice(suppliers)() for x in range(1,random.randint(3,8))]
    else:
        raise ValueError("Unrecognized Value for String Randomness, supported are pseudo and full")
    comment = "# " + " ".join(pieces) + " \n"
    return libcst.parse_module(comment)
