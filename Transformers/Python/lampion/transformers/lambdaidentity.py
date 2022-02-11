"""
Contains the "LambdaIdentityTransformer" that wraps literals into lambda functions and calls them.
"""
import random
import regex as re
from abc import ABC
from typing import Optional

import logging as log

from libcst import CSTNode
import libcst as cst

from lampion.transformers.basetransformer import BaseTransformer
from lampion.transformers.literal_helpers import get_all_literals


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

    Note: For some Transformers we need libcst.the parse_statement,
    however lambdas and function calls are both expressions, so all elements here are expressions.
    See: https://docs.python.org/2/reference/expressions.html#
    """

    def __init__(self, max_tries: int = 5):
        self._worked = False
        self.set_max_tries(max_tries)
        log.info("LambdaIdentityTransformer created (%d Re-Tries)", self.get_max_tries())

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

        seen_literals = get_all_literals(cst_to_alter)
        # Exit early if no matching literals exist
        if len(seen_literals) == 0:
            self._worked = False
            return altered_cst

        tries: int = 0
        max_tries: int = self.get_max_tries()

        while (not self._worked) and tries <= max_tries:
            try:
                to_replace = random.choice(seen_literals)

                replacer = self.__Replacer(to_replace[1], to_replace[0])

                altered_cst = cst_to_alter.visit(replacer)
                altered_code = str(altered_cst.code)
                reduced_code = _reduce_brackets(altered_code)

                altered_cst = cst.parse_module(reduced_code)

                tries = tries + 1
                self._worked = replacer.replacer_finished
                return altered_cst
            except AttributeError:
                # This case happened when the seen variables were tuples
                # Seen in OpenVocabCodeNLM Test Data
                tries = tries + 1
            except cst._nodes.base.CSTValidationError:
                # This can happen if we add too many (opening) Parentheses
                # See https://github.com/Instagram/LibCST/issues/640
                tries = tries + 1
            except cst._exceptions.ParserSyntaxError:
                # This can happen in two known cases:
                # 1. Original Code is buggy
                # 2. Reduction accidentally kills layout (e.g. removing indents)
                tries = tries + 1

        if tries == max_tries:
            log.warning("Lambda Identity Transformer failed after %i attempt", max_tries)

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
            self.replacer_finished = False

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
            if self.replace_type == "float" \
                    and original_node.deep_equals(self.to_replace) \
                    and not self.replacer_finished:
                literal = str(original_node.value)
                replacement = f"((lambda: {literal})())"
                expr = cst.parse_expression(replacement)
                updated_node = updated_node.deep_replace(updated_node, expr)
                self.replacer_finished = True

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
            if self.replace_type == "integer" \
                    and original_node.deep_equals(self.to_replace) \
                    and not self.replacer_finished:
                literal = str(original_node.value)
                replacement = f"((lambda: {literal})())"
                expr = cst.parse_expression(replacement)
                updated_node = updated_node.deep_replace(updated_node, expr)
                self.replacer_finished = True

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
            if self.replace_type == "simple_string" \
                    and original_node.deep_equals(self.to_replace) \
                    and not self.replacer_finished:
                literal = str(original_node.value)
                replacement = f"((lambda: {literal})())"
                expr = cst.parse_expression(replacement)
                updated_node = updated_node.deep_replace(updated_node, expr)
                self.replacer_finished = True

                return updated_node
            return updated_node


def _reduce_brackets(to_reduce: str) -> str:
    """
    Reduces redundant brackets within code.
    As matching parentheses is something regex cannot do (or cannot do well),
    the patterns are hardcoded to the alternations done in the visitors.
    That is, it only changes found patterns for double lambdas and changes bracket-order.


    Examples:
    >>> _reduce_brackets('((lambda: ((lambda: "Hello World")()))())')
    >>> '((lambda: lambda: "Hello World")()())'
    or:
    >>> _reduce_brackets('((lambda: (lambda: 5.2)())())')
    >>> '((lambda: lambda: 5.2)()())'
    For more tests, see the class-level tests.

    This method is intented to be used AFTER the alternation, so that the code never "grows" to big.
    I.E. this should be called once the AST has been changed and not before changing the AST.

    :param to_reduce str: the string to be reduced
    :returns str: the string with less brackets, if no match then the unchanged string
    This method was necessary as there is an issue with LibCST once it reaches too many opening brackets.
    This does "only" remove one pair of brackets, but there are less "opening" brackets
    See Issue: https://github.com/Instagram/LibCST/issues/640
    """
    # (.*?) matches any character in a greedy way
    # What I would like more is "any Character, a Space, a Plus and Quote-Mark" but I was not able to express it
    # TODO: sharpen regex match

    pattern = r'\(\(lambda: \(\(lambda: (.*?)\)\(\)\)\)\(\)\)'
    output_pattern = r'((lambda: lambda: \1)()())'
    result = re.sub(pattern, output_pattern, to_reduce)

    return result
