"""
Contains the "RenameVariableTransformer" that renames a variable and all its uses.
"""
import random
from abc import ABC

import logging as log
import libcst as cst

from libcst import CSTNode

from lampion.transformers.basetransformer import BaseTransformer
from lampion.utils.naming import get_random_string, get_pseudo_random_string


class RenameVariableTransformer(BaseTransformer, ABC):
    """
    Transformer that renames a random local variable with a random name.
    The variable is renamed at all places, that means it will also be changed at
    different locations if things happen to be named the same.
    TODO: This could lead to problems if people name variables like methods?

    This transformer will not be applied if there are no assigned variables.
    Class-Attributes, or attributes in private / sub-classes are valid targets for change too.


    Before:
    > def hi():
    >   some = 5
    >   thing = 3
    >   return some + thing

    After:
    > def hi():
    >   whnpwhenpwh = 5
    >   thing = 3
    >   return whnpwhenpwh + thing

    """

    def __init__(self, string_randomness: str = "pseudo", max_tries:int = 75):
        if string_randomness in ["pseudo", "full"]:
            self.__string_randomness = string_randomness
        else:
            raise ValueError("Unrecognized Value for String Randomness, supported are pseudo and full")

        _worked = False
        self.set_max_tries(max_tries)
        log.info("RenameVariableTransformer created (%d Re-Tries)",self.get_max_tries())

    def apply(self, cst_to_alter: CSTNode) -> CSTNode:
        """
        Apply the transformer to the given CST.
        Returns the original CST on failure or error.

        Check the function "worked()" whether the transformer was applied.

        :param cst_to_alter: The CST to alter.
        :return: The altered CST or the original CST on failure.

        Also, see the BaseTransformers notes if you want to implement your own.
        """
        visitor = self.__VariableCollector()

        altered_cst = cst_to_alter

        tries: int = 0
        max_tries: int = self.get_max_tries()

        while (not self._worked) and tries <= max_tries:
            try:
                cst_to_alter.visit(visitor)
                self._worked = visitor.finished

                seen_names = list({x.target.value for x in visitor.seen_variables})
                # Exit early: No local Variables!
                if len(seen_names) == 0:
                    self._worked = False
                    return cst_to_alter

                to_replace = random.choice(seen_names)
                if self.__string_randomness == "pseudo":
                    replacement = get_pseudo_random_string()
                elif self.__string_randomness == "full":
                    replacement = get_random_string(5)
                else:
                    raise ValueError(
                        "Something changed the StringRandomness in RenameVariableTransformer to an invalid value.")

                renamer = self.__Renamer(to_replace, replacement)

                altered_cst = cst_to_alter.visit(renamer)
                tries = tries + 1

            except AttributeError:
                # This case happened when the seen variables were tuples
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

    class __VariableCollector(cst.CSTVisitor):
        """
        CSTVisitor that collects all variable-names in traversal.
        Any seen value is saved in an attribute "seen_variables"
        """

        def __init__(self):
            self.seen_variables = []

        finished = True
        seen_variables = []

        # "visit_Name" visits what we want in the end, but is too broad:
        # The method class etc. also have names, but we only want assign ones
        # If we go only for assigned ones, we do not change parameters / methods etc.

        def visit_Assign_targets(self, node: "Assign") -> None:
            """
            Adds the seen variables to the "seen_variables" attribute.
            :param node: the node touched in traversal
            :return: None
            """
            # There are assigns with multiple targets, e.g. tuples or dicts.
            # For now we focus on the simple cases
            if len(node.targets) == 1:
                self.seen_variables.append(node.targets[0])
                return

        # AnnAssign is for "Annotated Assign". It does not necessarily need a value assigned, you can
        # Assign only types without values.
        # See Libcst on this: https://libcst.readthedocs.io/en/latest/nodes.html?highlight=visit_Assign_Target#libcst.AnnAssign
        def visit_AnnAssign_target(self, node: "AnnAssign") -> None:
            """
            Adds the seen typed variables to the "seen_variables" attribute.
            :param node: the node touched in traversal
            :return: None
            """
            self.seen_variables.append(node)

    class __Renamer(cst.CSTTransformer):
        """
        The CSTTransformer that traverses the CST and renames variables.
        Currently does not care about the scoping - all occurrences will be renamed.
        """

        def __init__(self, to_replace: str, replacement: str):
            self.to_replace = to_replace
            self.replacement = replacement

        def leave_Name(
                self, original_node: "Name", updated_node: "Name"
        ) -> "BaseExpression":
            """
            Renames the variable if it was the one to be replaced.
            What to replace and what to replace with are given in __init__.

            Have a careful at the tests for this class to understand the behaviour.

            :param original_node: the node before change
            :param updated_node: the node after change
            :return: the node after change, too.
            """
            if original_node.value == self.to_replace:
                return updated_node.with_changes(value=self.replacement)
            return updated_node
