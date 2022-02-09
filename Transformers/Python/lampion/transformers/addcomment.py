"""
Contains the "AddCommentTransformer" that adds a (random) comment at a random position.
"""
import random
import logging as log
from abc import ABC
from typing import Union

import libcst as cst
import libcst.codegen.gather
from libcst import FlattenSentinel, RemovalSentinel, CSTNode

from lampion.transformers.basetransformer import BaseTransformer
from lampion.utils.naming import get_random_string, get_pseudo_random_string


class AddCommentTransformer(BaseTransformer, ABC):
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

    def __init__(self, string_randomness: str = "pseudo",max_tries:int = 50):
        if string_randomness in ["pseudo","full"]:
            self.__string_randomness = string_randomness
        else:
            raise ValueError("Unrecognized Value for String Randomness, supported are pseudo and full")

        self._worked = False
        self.set_max_tries(max_tries)
        log.info("AddCommentTransformer created (%d Re-Tries)",self.get_max_tries())

    _worked = False
    __string_randomness: str

    def apply(self, cst_to_alter: CSTNode) -> CSTNode:
        """
        Apply the transformer to the given CST.
        Returns the original CST on failure or error.

        Check the function "worked()" whether the transformer was applied.

        :param cst_to_alter: The CST to alter.
        :return: The altered CST or the original CST on failure.

        Also, see the BaseTransformers notes if you want to implement your own.
        """
        visitor = self.__AddCommentVisitor()

        altered_cst = cst_to_alter

        tries: int = 0
        max_tries : int = self.get_max_tries()

        while (not self._worked) and tries <= max_tries:
            altered_cst = cst_to_alter.visit(visitor)
            self._worked = visitor.finished
            tries = tries + 1

        if tries == max_tries:
            log.warning("Add Comment Transformer failed after %i attempts",max_tries)

        #TODO: add Post-Processing Values here

        return altered_cst

    def reset(self) -> None:
        """Resets the Transformer to be applied again.

           after the reset all local state is deleted, the transformer is fully reset.

           It holds:
           > a = SomeTransformer()
           > b = SomeTransformer()
           > someTree.visit(a)
           > a.reset()
           > assert a == b
        """
        self._worked = False

    def worked(self) -> bool:
        """
        Returns whether the transformer was successfully applied since the last reset.
        If the transformer cannot be applied for logical reasons it will return false without attempts.

        :returns bool
            True if the Transformer was successfully applied.
            False otherwise.

        """
        return self._worked

    def postprocessing(self) -> None:
        """
        Manages all behavior after application, in case it worked(). Also calls reset().
        """
        self.reset()

    def categories(self) -> [str]:
        """
        Gives the categories specified for this transformer.
        Used only for information and maybe later for filter purposes.
        :return: The categories what this transformer can be summarized with.
        """
        return ["Naming","Comment"]

    class __AddCommentVisitor(cst.CSTTransformer):
        """
        LibCST Transformer that runs over CSTS and (maybe) adds a random comment.
        Shape of the comment is determined in the "__init__" method.

        May is not applied by chance, to see whether it was successfully applied check
        the attribute "finished".
        """
        def __init__(self, string_randomness: str = "pseudo"):
            if string_randomness in ["pseudo","full"]:
                self.__string_randomness = string_randomness
            else:
                raise ValueError["Received invalid value for String Randomness, supported values are 'pseud' and 'full'"]

            log.debug("AddVariableVisitor Created")
            self.finished = False

        finished: bool = False
        __string_randomness: str

        def leave_SimpleStatementLine(
                self, original_node: "SimpleStatementLine", updated_node: "SimpleStatementLine"
        ) -> Union["BaseStatement", FlattenSentinel["BaseStatement"], RemovalSentinel]:
            """
            LibCSTTransformer that adds a random comment at a random place.
            Currently, at every statement a coin is flipped whether before the statement
            the comment will be introduced.
            Hence, this method can finish without any application.
            For this case, the Transformer re-runs this visitor in case of non-application.

            Double-Application is guarded with a flag.
            :param original_node: The original node before any traversal
            :param updated_node:  The node after (downstream) changes
            :return: The node after our changes
            """
            # Case 1: We successfully applied the Transformer, exit early, do nothing.
            if self.finished:
                return updated_node

            added_stmt = _make_snippet(string_randomness=self.__string_randomness)

            # Case 2: We did not alter yet, at the current (random) statement apply it in 1 of 20 cases.
            # TODO: this has a slight bias towards early nodes if the file is long?
            # TODO: Is there a way to see all nodes and pick one by random.choice(allNodes) ?
            if random.random() < 0.05:
                self.finished = True
                # FlattenSentinels are what we want to replace 1 existing element (here 1 statement)
                # with 1 or more statements. It takes care of things like indentation
                return cst.FlattenSentinel([added_stmt, updated_node])
            # Case 3: We did not alter it and chance was not triggered.
            # Re-Run the Transformer, better luck next time.
            return updated_node


def _make_snippet(string_randomness: str = "pseudo") -> CSTNode:
    """
    Creates a CSTNode of a comment made of random strings.
    Supported randomness are "pseudo" and "full".

    Example Pseudo Random:

    >>> _make_snippet("pseudo")
    >>> # store valid beaver lawyer get

    For Full Random:

    >>> _make_snippet("full")
    >>> # bowhg k0weg f125ghp

    :param string_randomness:  Whether to use "pseudo" or "full" random Strings for the snippet, default pseudo
    :return: A comment CST Node made of random strings
    :raises: ValueError in case of unknown string_randomness
    """
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
