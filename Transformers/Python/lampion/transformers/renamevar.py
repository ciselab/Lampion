# Transformer-Example
# https://libcst.readthedocs.io/en/latest/tutorial.html
import random
import string
from typing import Optional, Union

import libcst as cst
import logging as log

from libcst import MaybeSentinel, FlattenSentinel, RemovalSentinel, CSTNode

from lampion.transformers.basetransformer import BaseTransformer
from lampion.utils.naming import get_random_string


class RenameVariableTransformer(BaseTransformer):
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

    def apply(self, cst: CSTNode) -> CSTNode:
        visitor = self.__VariableCollector()

        altered_cst = cst

        tries: int = 0
        max_tries: int = 10

        while (not self._worked) and  tries <= max_tries:
            cst.visit(visitor)
            self._worked = visitor.finished

            seen_names = list(set([x.target.value for x in visitor.seen_variables]))
            # Exit early: No local Variables!
            if len(seen_names) == 0:
                self._worked = False
                return cst

            to_replace = random.choice(seen_names)
            replacement = get_random_string(5)

            renamer = self.__Renamer(to_replace,replacement)

            altered_cst = cst.visit(renamer)

            tries = tries + 1

        if tries == max_tries:
            log.warning(f"Rename Variable Visitor failed after {max_tries} attempt")

        #TODO: add Post-Processing Values here

        return altered_cst

    def reset(self) -> None:
        self._worked = False

    def worked(self) -> bool:
        return self._worked

    def categories(self) -> [str]:
        return ["Naming"]

    def postprocessing(self) -> None:
        self.reset()


    class __VariableCollector(cst.CSTVisitor):

        def __init__(self):
            self.seen_variables = []

        finished = True
        seen_variables = []

        # "visit_Name" visits what we want in the end, but is too broad:
        # The method class etc. also have names, but we only want assign ones
        # If we go only for assigned ones, we do not change parameters / methods etc.

        def visit_Assign_targets(self, node: "Assign") -> None:
            # There are assigns with multiple targets, e.g. tuples or dicts.
            # For now we focus on the simple cases
            if len(node.targets) == 1:
                self.seen_variables.append(node.targets[0])
                return

        # AnnAssign is for "Annotated Assign". It does not necessarily need a value assigned, you can
        # Assign only types without values.
        # See Libcst on this: https://libcst.readthedocs.io/en/latest/nodes.html?highlight=visit_Assign_Target#libcst.AnnAssign
        def visit_AnnAssign_target(self, node: "AnnAssign") -> None:
            self.seen_variables.append(node)
            return

    class __Renamer(cst.CSTTransformer):

        def __init__(self,to_replace:str, replacement:str):
            self.to_replace = to_replace
            self.replacement = replacement

        def leave_Name(
        self, original_node: "Name", updated_node: "Name"
    ) -> "BaseExpression":
            if original_node.value == self.to_replace:
                #print(f"Renamer found a node with name to replace! {self.to_replace}")
                return updated_node.with_changes(value=self.replacement)
            else:
                return updated_node
