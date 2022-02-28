"""
Contains the "RenameParameterTransformer" that renames a parameter of a method and all of it's uses.
"""
import random
from abc import ABC
from typing import Optional

import logging as log
import libcst as cst

from libcst import CSTNode

from lampion.transformers.basetransformer import BaseTransformer
from lampion.utils.naming import get_random_string, get_pseudo_random_string


class RenameParameterTransformer(BaseTransformer, ABC):
    """
    Transformer that renames a random parameter with a random name.
    The parameter is renamed at all places, that means it will also be changed at
    different locations if things happen to be named the same.
    TODO: This could lead to problems if people name parameters like methods?

    This transformer will not be applied if there are no assigned variables.
    Class-Attributes, or attributes in private / sub-classes are valid targets for change too.


    Before:
    > def example(some,thing):
    >   return some + thing

    After:
    > def hi(whnpwhenpwh,thing):
    >   return whnpwhenpwh + thing

    """

    def __init__(self, string_randomness: str = "pseudo", max_tries:int = 25):
        if string_randomness in ["pseudo", "full"]:
            self.__string_randomness = string_randomness
        else:
            raise ValueError("Unrecognized Value for String Randomness, supported are pseudo and full")

        self._worked = False
        self.set_max_tries(max_tries)
        log.info("RenameParameterTransformer created (%d Re-Tries)",self.get_max_tries())

    def apply(self, cst_to_alter: CSTNode) -> CSTNode:
        """
        Apply the transformer to the given CST.
        Returns the original CST on failure or error.

        Check the function "worked()" whether the transformer was applied.

        :param cst_to_alter: The CST to alter.
        :return: The altered CST or the original CST on failure.

        Also, see the BaseTransformers notes if you want to implement your own.
        """
        visitor = self.__ParameterCollector()

        altered_cst = cst_to_alter

        tries: int = 0
        max_tries: int = self.get_max_tries()

        while (not self._worked) and tries <= max_tries:
            try:
                cst_to_alter.visit(visitor)
                self._worked = visitor.finished

                seen_names = list({x.value for x in visitor.seen_params})
                # Exit early: No local Variables!
                if len(seen_names) == 0:
                    self._worked = False
                    return cst_to_alter

                to_replace = random.choice(seen_names)

                if self.__string_randomness == "pseudo":
                    replacement = get_pseudo_random_string()
                elif self.__string_randomness == "full":
                    replacement = get_random_string(5)

                renamer = self.__Renamer(to_replace, replacement)

                altered_cst = cst_to_alter.visit(renamer)
                tries = tries + 1

            except AttributeError:
                # This case happened when the seen variables were not tuples
                # Seen in OpenVocabCodeNLM Test Data
                tries = tries + 1

        if tries == max_tries:
            log.warning("Rename Variable Transformer failed after %i attempt",max_tries)

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
        Gives the categories specified for this transformer.
        Used only for information and maybe later for filter purposes.
        :return: The categories what this transformer can be summarized with.
        """
        return ["Naming"]

    def postprocessing(self) -> None:
        """
        Manages all behavior after application, in case it worked(). Also calls reset().
        """
        self.reset()

    class __ParameterCollector(cst.CSTVisitor):
        """
        The CSTVisitor that traverses the CST and collects available parameter-names.
        """

        def __init__(self):
            self.seen_params = []

        finished = True
        seen_params = []

        # "visit_Name" visits what we want in the end, but is too broad:
        # The method class etc. also have names, but we only want assign ones
        # If we go only for assigned ones, we do not change parameters / methods etc.

        def visit_Param(self, node: "Param") -> Optional[bool]:
            """
            LibCST method that adds any seen parameter to the objects attribute.
            """
            self.seen_params.append(node.name)

    class __Renamer(cst.CSTTransformer):
        """
        The CSTTransformer that traverses the CST and renames parameters.
        """

        def __init__(self, to_replace: str, replacement: str):
            self.to_replace = to_replace
            self.replacement = replacement

        def leave_Name(
                self, original_node: "Name", updated_node: "Name"
        ) -> "BaseExpression":
            """
            Renames the parameter if it was the one to be altered.
            What to replace and what to replace with are given in __init__.

            Have a careful at the tests for this class to understand the behaviour.

            :param original_node: the node before change
            :param updated_node: the node after change
            :return: the node after change too.
            """
            if original_node.value == self.to_replace:
                return updated_node.with_changes(value=self.replacement)
            return updated_node
