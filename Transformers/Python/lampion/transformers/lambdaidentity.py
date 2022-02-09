"""
Contains the "LambdaIdentityTransformer" that wraps literals into lambda functions and calls them.
"""
import random
from abc import ABC
from typing import Optional

import logging as log

from libcst import CSTNode
import libcst as cst

from lampion.transformers.basetransformer import BaseTransformer


class LambdaIdentityTransformer(BaseTransformer, ABC):
    """
    Transformer that wraps literals in a lambda function that is immediately called.
    This procedure is often called an identity-function, hence the name of the transformer.
    Brackets are added pre-cautiously, even if they might be redundant.

    IMPORTANT: This is not identical behaviour to the Java Transformer, as the Python Transformer only works for Literals,
    while Java works on any typed element.
    TODO: Extend this behaviour for more elements than literals? In theory all expressions are fine.

    Before:
    > def example():
    >   return 1

    After:
    > def example():
    >   return ((lambda: 1)())


    Before:
    > def example2():
    >   name = "World"
    >   print(f"Hello {name}")

    After:
    > def example2():
    >   name = ((lambda:"World")())
    >   print(f"Hello {name}")

    The above added elements have redundant ( ) but I add them intentionally,
    so that I do not run into weird bugs about precedence.
    LibCST does not support finding this kind of behaviour afaik.
    """

    def __init__(self):
        log.info("LambdaIdentityTransformer Created")
        self._worked = False

    def apply(self, cst_to_alter: CSTNode) -> CSTNode:
        """
        Apply the transformer to the given CST.
        Returns the original CST on failure or error.

        Check the function "worked()" whether the transformer was applied.

        :param cst_to_alter: The CST to alter.
        :return: The altered CST or the original CST on failure.

        Also, see the BaseTransformers notes if you want to implement your own.
        """
        visitor = self.__LiteralCollector()

        altered_cst = cst_to_alter

        tries: int = 0
        max_tries: int = 100

        while (not self._worked) and tries <= max_tries:
            try:
                cst_to_alter.visit(visitor)

                seen_literals = \
                    [("simple_string", x) for x in visitor.seen_strings] \
                    + [("float", x) for x in visitor.seen_floats] \
                    + [("integer", x) for x in visitor.seen_integers]
                # Exit early: No Literals to work on!
                if len(seen_literals) == 0:
                    self._worked = False
                    return cst_to_alter

                to_replace = random.choice(seen_literals)

                replacer = self.__Replacer(to_replace[1], to_replace[0])

                altered_cst = cst_to_alter.visit(replacer)

                tries = tries + 1
                self._worked = replacer.worked
            except AttributeError:
                # This case happened when the seen variables were tuples
                # Seen in OpenVocabCodeNLM Test Data
                tries = tries + 1
            except cst._nodes.base.CSTValidationError:
                # This can happen if we try to add strings and add too many Parentheses
                # See https://github.com/Instagram/LibCST/issues/640
                tries = tries + 1

        if tries == max_tries:
            log.warning("Lambda Identity Transformer failed after %i attempt",max_tries)

        # TODO: add Post-Processing Values here

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

        :returns bool:
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
        return ["Smell", "Operators"]

    def postprocessing(self) -> None:
        """
        Manages all behavior after application, in case it worked(). Also calls reset().
        """
        self.reset()

    class __LiteralCollector(cst.CSTVisitor):
        finished = True
        seen_floats = []
        seen_strings = []
        seen_integers = []

        def visit_Float(self, node: "Float") -> Optional[bool]:
            """
            LibCST built-in traversal that puts all seen float-literals in the known literals.
            """
            self.seen_floats.append(node)

        def visit_Integer(self, node: "Integer") -> Optional[bool]:
            """
            LibCST built-in traversal that puts all seen float-literals in the known literals.
            """
            self.seen_integers.append(node)

        def visit_SimpleString(self, node: "SimpleString") -> Optional[bool]:
            """
            LibCST built-in traversal that puts all seen SimpleString-literals in the known literals.
            """
            self.seen_strings.append(node)

    class __Replacer(cst.CSTTransformer):
        """
        The CSTTransformer that traverses the CST and replaces literals with lambda: literal.
        Currently to cover issues with scoping / identity of literals,
        the first instance of the literal will be altered.

        See the tests for an expression of the behaviour.
        """

        def __init__(self, to_replace: "CSTNode", replace_type: str):
            self.to_replace = to_replace
            self.replace_type = replace_type
            self.worked = False

        def leave_Float(
                self, original_node: "Float", updated_node: "Float"
        ) -> "BaseExpression":
            """
            LibCST function to traverse floats.
            If the float to replace is found, it is replaced by
            > 0.5 -> ((lambda: 0.5)())
            :param original_node: The node before change
            :param updated_node: The node after (downstream) changes
            :return: the updated node after our changes
            """
            if self.replace_type == "float" and original_node.deep_equals(self.to_replace) and not self.worked:
                literal = str(original_node.value)
                replacement = f"((lambda: {literal})())"
                expr = cst.parse_expression(replacement)
                updated_node = updated_node.deep_replace(updated_node, expr)
                self.worked = True

                return updated_node
            return updated_node

        def leave_Integer(
                self, original_node: "Integer", updated_node: "Integer"
        ) -> "BaseExpression":
            """
            LibCST function to traverse integers.
            If the integer to replace is found, it is replaced by
            > 5 -> ((lambda: 5)())
            :param original_node: The node before change
            :param updated_node: The node after (downstream) changes
            :return: the updated node after our changes
            """
            if self.replace_type == "integer" and original_node.deep_equals(self.to_replace) and not self.worked:
                literal = str(original_node.value)
                replacement = f"((lambda: {literal})())"
                expr = cst.parse_expression(replacement)
                updated_node = updated_node.deep_replace(updated_node, expr)
                self.worked = True

                return updated_node
            return updated_node

        def leave_SimpleString(
                self, original_node: "SimpleString", updated_node: "SimpleString"
        ) -> "BaseExpression":
            """
            LibCST function to traverse simple strings.
            If the simple-string to replace is found, it is replaced by
            > "hey" -> ((lambda: "hey")())
            :param original_node: The node before change
            :param updated_node: The node after (downstream) changes
            :return: the updated node after our changes
            """
            if self.replace_type == "simple_string" and original_node.deep_equals(self.to_replace) and not self.worked:
                literal = str(original_node.value)
                replacement = f"((lambda: {literal})())"
                expr = cst.parse_expression(replacement)
                updated_node = updated_node.deep_replace(updated_node, expr)
                self.worked = True

                return updated_node
            return updated_node
