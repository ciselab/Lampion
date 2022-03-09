"""
Contains the "AddVariableTransformer" that inserts an unused variable with a random value.
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


class AddVariableTransformer(BaseTransformer, ABC):
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

    Currently, it can add ints, floats, doubles and strings.
    Adding the type is an optional flag.
    """

    def __init__(self, string_randomness: str = "pseudo", max_tries : int = 50):
        if string_randomness in ["pseudo","full"]:
            self.__string_randomness = string_randomness
        else:
            raise ValueError("Unrecognized Value for String Randomness, supported are pseudo and full")

        self._worked = False
        self.set_max_tries(max_tries)
        log.info("AddVariableTransformer created (%d Re-Tries)",self.get_max_tries())

    def apply(self, cst_to_alter: CSTNode) -> CSTNode:
        """
        Apply the transformer to the given CST.
        Returns the original CST on failure or error.

        Check the function "worked()" whether the transformer was applied.

        :param cst_to_alter: The CST to alter.
        :return: The altered CST or the original CST on failure.

        Also, see the BaseTransformers notes if you want to implement your own.
        """
        visitor = self.__AddVarVisitor()

        altered_cst = cst_to_alter

        tries: int = 0
        max_tries: int = self.get_max_tries()

        while (not self._worked) and tries <= max_tries:
            altered_cst = cst_to_alter.visit(visitor)
            self._worked = visitor.finished
            tries = tries + 1

        if tries == max_tries:
            log.warning("Add Variable Visitor failed after %i attempts",max_tries)

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

    def categories(self) -> [str]:
        """
        Hardcoded return of the categories that this transformer satisfies.
        Can be used to e.g. filter transformers for certain categories.
        :return: The categories of change that this transformer matches.
        """
        return ["Naming", "Smell"]

    def postprocessing(self) -> None:
        """
        Manages all behavior after application, in case it worked(). Also calls reset().
        """
        self.reset()

    class __AddVarVisitor(cst.CSTTransformer):

        def __init__(self, string_randomness: str = "pseudo"):
            log.debug("AddVariableVisitor Created")
            self.finished = False
            self.__string_randomness = string_randomness

        finished = False

        def leave_SimpleStatementLine(
                self, original_node: "SimpleStatementLine", updated_node: "SimpleStatementLine"
        ) -> Union["BaseStatement", FlattenSentinel["BaseStatement"], RemovalSentinel]:
            """
            LibCSTTransformer that adds random variables at a random place.
            Currently, at every statement a coin is flipped whether before the statement
            a random variable should be introduced.
            hence, this method can finish without any application.
            For this case, the Transformer re-runs this visitor in case of non-application.

            Double-Application is guarded with a flag.
            :param original_node: The original node before any traversal
            :param updated_node:  The node after (downstream) changes
            :return: The node after our changes
            """
            # Case 1: We successfully applied the Transformer, exit early, do nothing.
            if self.finished:
                return updated_node

            added_stmt = self._make_snippet(self.__string_randomness)

            # Case 2: We did not alter yet, at the current (random) statement apply it in 1 of 20 cases.
            # TODO: this has a slight bias towards early nodes if the file is long?
            # TODO: Is there a way to see all nodes and pick one by random.choice(allNodes) ?
            if random.random() < 0.05:
                self.finished = True
                # FlattenSentinels are what we want to replace 1 existing element (here 1 statement)
                # with 1 or more statements. It takes care of things like indentation.
                return cst.FlattenSentinel([added_stmt, updated_node])
            # Case 3: We did not alter it and chance was not triggered.
            # Re-Run the Transformer, better luck next time.
            return updated_node

        _supported_types = ["int", "float", "str"]
        _add_types = True

        def _make_snippet(self, string_randomness: str = "pseudo") -> CSTNode:
            """
            Helper-Function to produce a snippet of
            > random_name = random_value
            or
            > random_name: according_type = random_value

            Producible types (chosen at random) are string, integer and float.
            Whether or not to add types to these snippets is managed by a class-level attribute.

            :param string_randomness: How the variable-names should look like. Supported input is "pseudo" or "full".
            :return: A CST Assign-Statement with random values, names and types.
            :raises: ValueError in case of different strings.
            """
            if string_randomness == "pseudo":
                name = get_pseudo_random_string()
            elif string_randomness == "full":
                name = get_random_string(5)
            else:
                raise ValueError(
                    "Something changed the StringRandomness in AddVariableTransformer to an invalid value.")

            type_str = random.choice(self._supported_types)

            if self._add_types:
                name = f"{name}: {type_str}"

            value = ""
            if type_str == "str":
                value = f"\"{get_random_string(random.randint(3, 30))}\""
            if type_str == "int":
                value = random.randint(2, 1000)
            if type_str == "float":
                value = random.random()

            return libcst.parse_statement(f"{name} = {value}")
