import random
from typing import Optional

import libcst as cst
import logging as log

from libcst import CSTNode

from lampion.transformers.basetransformer import BaseTransformer
from lampion.utils.naming import get_random_string, get_pseudo_random_string


class RenameParameterTransformer(BaseTransformer):
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

    __string_randomness: str

    def __init__(self, string_randomness: str = "pseudo"):
        if (string_randomness == "pseudo") or (string_randomness == "full"):
            self.__string_randomness = string_randomness
        else:
            raise ValueError("Unrecognized Value for String Randomness, supported are pseudo and full")

        log.info("RenameParameterTransformer Created")

    def apply(self, cst: CSTNode) -> CSTNode:
        visitor = self.__ParameterCollector()

        altered_cst = cst

        tries: int = 0
        max_tries: int = 10

        while (not self._worked) and  tries <= max_tries:
            cst.visit(visitor)
            self._worked = visitor.finished

            seen_names = list(set([x.value for x in visitor.seen_params]))
            # Exit early: No local Variables!
            if len(seen_names) == 0:
                self._worked = False
                return cst

            to_replace = random.choice(seen_names)

            if self.__string_randomness == "pseudo":
                replacement = get_pseudo_random_string()
            elif self.__string_randomness == "full":
                replacement = get_random_string(5)
            else:
                raise ValueError(
                    "Something changed the StringRandomness in RenameParamTransformer to an invalid value.")

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


    class __ParameterCollector(cst.CSTVisitor):

        def __init__(self):
            self.seen_params = []

        finished = True
        seen_params = []

        # "visit_Name" visits what we want in the end, but is too broad:
        # The method class etc. also have names, but we only want assign ones
        # If we go only for assigned ones, we do not change parameters / methods etc.

        def visit_Param(self, node: "Param") -> Optional[bool]:

            self.seen_params.append(node.name)
            return

    class __Renamer(cst.CSTTransformer):

        def __init__(self,to_replace:str, replacement:str):
            self.to_replace = to_replace
            self.replacement = replacement

        def leave_Name(
        self, original_node: "Name", updated_node: "Name"
    ) -> "BaseExpression":
            if original_node.value == self.to_replace:
                return updated_node.with_changes(value=self.replacement)
            else:
                return updated_node