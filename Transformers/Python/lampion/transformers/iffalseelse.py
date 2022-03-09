"""
Contains the "IfFalseElseTransformer" that wraps method-bodys in an if False else statement.
Twin of "IfTrueTransformer".
"""
import random
from abc import ABC

import logging as log

import libcst._nodes.base
from libcst import CSTNode

from lampion.transformers.basetransformer import BaseTransformer


class IfFalseElseTransformer(BaseTransformer, ABC):
    """
    Transformer that wraps method-bodies in an If-False-Else Statement.
    Can happen at any block-statement, hence it could be at a method body, try-block,

    Before:
    > def example1():
    >   name = "World"
    >   print(f"Hello {name}")

    After:
    > def example1():
    >   if (False):
    >       return
    >   else:
    >       name = "World"
    >       print(f"Hello {name}")


    Before:
    > def example2(num):
    >   if num % 2 == 0
    >       print("Even!")
    >   else:
    >       print("Odd!")

    After:
    > def example2(num):
    >   if num % 2 == 0
    >       print("Even!")
    >   else:
    >       if (False):
    >           return
    >       else:
    >           print("Odd!")

    The above added elements have redundant ( ) but I add them intentionally to be careful.
    """

    def __init__(self, max_tries: int = 50):
        self._worked = False
        self.set_max_tries(max_tries)
        log.info("IfTrueTransformer created (%d Re-Tries)", self.get_max_tries())

    def apply(self, cst_to_alter: CSTNode) -> CSTNode:
        """
        Apply the transformer to the given CST.
        Returns the original CST on failure or error.

        Check the function "worked()" whether the transformer was applied.

        :param cst_to_alter: The CST to alter.
        :return: The altered CST or the original CST on failure.

        Also, see the BaseTransformers notes if you want to implement your own.
        """

        altered_cst = cst_to_alter

        tries: int = 0
        max_tries: int = self.get_max_tries()

        while (not self._worked) and tries <= max_tries:
            try:
                transformer = self.__IfTrueWrapper()

                altered_cst = altered_cst.visit(transformer)

                self._worked = transformer.applied()
                tries = tries + 1

            except libcst._nodes.base.CSTValidationError:
                # This can happen if we try to add strings and add too many Parentheses
                # See https://github.com/Instagram/LibCST/issues/640
                tries = tries + 1
            except libcst._exceptions.ParserSyntaxError:
                # This can happen in two known cases:
                # 1. Original Code is buggy
                # 2. Transformation accidentally kills layout (e.g. removing indents)
                tries = tries + 1

        if tries == max_tries and not self.worked():
            log.warning("IfTrueTransformer failed after %i attempts", max_tries)

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
        return ["Smell", "Structure"]

    def postprocessing(self) -> None:
        """
        Manages all behavior after application, in case it worked(). Also calls reset().
        """
        self.reset()

    class __IfTrueWrapper(libcst.CSTTransformer):
        """
        Covers two options:

        1. IntendedBlock: Your normal CodeBlock, most average Case
        2. Simple Statement Suite: inline statement-blocks, without intendation

        Note: The LibCST Library does not like to create the AST elements by themselves
        (it does not have a lot of constructors etc.)
        Hence, we first make a small statement with the right condition, and replace the or-else-body.
        """

        def __init__(self):
            super().__init__()
            self.__applied = False
            self.chance = 0.1

        def applied(self):
            return self.__applied

        def leave_SimpleStatementSuite(
                self,
                original_node: "SimpleStatementSuite",
                updated_node: "SimpleStatementSuite",
        ) -> "BaseSuite":
            if not self.__applied and random.random() < self.chance:
                wrapper = libcst.parse_statement("if (False): \n\treturn None\nelse:\n\treturn 1")
                wrapper_with_body_changed = wrapper.deep_replace(wrapper.orelse.body, updated_node)

                self.__applied = True
                return wrapper_with_body_changed
            else:
                # Else case necessary, as the leave_X always needs to return something
                # None-Return will cause exceptions
                return updated_node

        def leave_IndentedBlock(
                self, original_node: "IndentedBlock", updated_node: "IndentedBlock"
        ) -> "BaseSuite":
            if not self.__applied and random.random() < self.chance:
                wrapper = libcst.parse_statement("if (False): \n\treturn None\nelse:\n\treturn 1")
                wrapper_with_body_changed = wrapper.deep_replace(wrapper.orelse.body, updated_node)

                self.__applied = True
                return wrapper_with_body_changed
            else:
                # Else case necessary, as the leave_X always needs to return something
                # None-Return will cause exceptions
                return updated_node
