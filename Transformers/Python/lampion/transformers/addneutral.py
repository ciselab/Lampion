"""
Contains the "AddNeutralElementTransformer" that adds +0 to integers or +"" to strings.
"""
import random
from abc import ABC

import logging as log

import libcst._nodes.base
from libcst import CSTNode
import libcst as cst
import regex as re

from lampion.transformers.basetransformer import BaseTransformer
from lampion.transformers.literal_helpers import get_all_literals


class AddNeutralElementTransformer(BaseTransformer, ABC):
    """
    Transformer that adds neutral elements after literals.
    Currently, this supports strings, ints and doubles.
    Brackets are added pre-cautiously, even if they might be redundant.

    IMPORTANT: This is not identical behaviour to the Java Transformer, as the Python Transformer only works for Literals,
    while Java works on any typed element.

    Before:
    > def example():
    >   return 1

    After:
    > def example():
    >   return (1 + 0)


    Before:
    > def example2():
    >   name = "World"
    >   print(f"Hello {name}")

    After:
    > def example2():
    >   name = ("World" + "")
    >   print(f"Hello {name}")

    The above added elements have redundant ( ) but I add them intentionally,
    so that I do not run into weird bugs about precedence.
    LibCST does not support finding this kind of behaviour afaik.
    """

    def __init__(self, max_tries: int = 5):
        self._worked = False
        self.set_max_tries(max_tries)
        log.info("AddNeutralElementTransformer created (%d Re-Tries)", self.get_max_tries())

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

        seen_literals = get_all_literals(altered_cst)
        # Exit early: No Literals to work on!
        if len(seen_literals) == 0:
            self._worked = False
            return cst_to_alter

        tries: int = 0
        max_tries: int = self.get_max_tries()

        while (not self._worked) and tries <= max_tries:
            try:
                to_replace = random.choice(seen_literals)

                replacer = self.__Replacer(to_replace[1], to_replace[0])

                altered_cst = cst_to_alter.visit(replacer)

                altered_code = altered_cst.code
                reduced_code = _reduce_brackets(altered_code)

                altered_cst = cst.parse_module(reduced_code)

                tries = tries + 1
                self._worked = replacer.worked
            except libcst._nodes.base.CSTValidationError:
                # This can happen if we try to add strings and add too many Parentheses
                # See https://github.com/Instagram/LibCST/issues/640
                tries = tries + 1
            except libcst._exceptions.ParserSyntaxError:
                # This can happen in two known cases:
                # 1. Original Code is buggy
                # 2. Reduction accidentally kills layout (e.g. removing indents)
                tries = tries + 1

        if tries == max_tries:
            log.warning("Add_Neutral_Element Transformer failed after %i attempts", max_tries)

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

        returns bool:
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
        The CSTTransformer that traverses the CST and replaces literals with literal+neutral element.
        Currently to cover issues with scoping / identity of literals,
        the first instance of the literal will be altered.
        """

        def __init__(self, to_replace: "CSTNode", replace_type: str):
            self.to_replace = to_replace
            self.replace_type = replace_type
            self.worked = False

        def leave_Float(
                self, original_node: "Float", updated_node: "Float"
        ) -> "BaseExpression":
            """
            LibCST function to traverse simple strings.
            If the simple-string to replace is found, it is replaced by
            > 0.5 -> (0.5+0.0)
            :param original_node: The node before change
            :param updated_node: The node after (downstream) changes
            :return: the updated node after our changes
            """
            if self.replace_type == "float" and original_node.deep_equals(self.to_replace) and not self.worked:
                literal = str(original_node.value)
                replacement = f"({literal} + 0.0)"
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
            > 4 -> (4+0)
            :param original_node: The node before change
            :param updated_node: The node after (downstream) changes
            :return: the updated node after our changes
            """
            if self.replace_type == "integer" \
                    and original_node.deep_equals(self.to_replace) \
                    and not self.worked:
                literal = str(original_node.value)
                replacement = f"({literal} + 0)"
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
            > "hey" -> ("hey"+"")
            :param original_node: The node before change
            :param updated_node: The node after (downstream) changes
            :return: the updated node after our changes
            """
            if self.replace_type == "simple_string" \
                    and original_node.deep_equals(self.to_replace) \
                    and not self.worked:
                literal = str(original_node.value)
                replacement = f"({literal} + \"\")"
                expr = cst.parse_expression(replacement)
                updated_node = updated_node.deep_replace(updated_node, expr)
                self.worked = True
                return updated_node
            return updated_node


def _reduce_brackets(to_reduce: str) -> str:
    """
    Reduces redundant brackets within code.
    As matching parentheses is something regex cannot do (or cannot do well),
    the patterns are hardcoded to the alternations done in the visitors.
    That is, it only changes found patterns for empty strings, + 0 and + 0.0.

    Examples:
    >>> _reduce_brackets('(("X" + "") + "")')
    >>> '("X" + "" + "")'
    >>> _reduce_brackets('("X" + ("" + ""))')
    >>> '("X" + "" + "")'
    or:
    >>> _reduce_brackets('(("X" + "" + "" + "") + "")')
    >>> "(\"X\" + \"\" + \"\" + \"\" + \"\")"
    For more tests, see the class-level tests.

    This method is intented to be used AFTER the alternation, so that the code never "grows" to big.
    I.E. this should be called once the AST has been changed and not before changing the AST.

    :param to_reduce str: the string to be reduced
    :returns str: the string with less brackets, if no match then the unchanged string
    This method was necessary as there is an issue with LibCST once it reaches too many opening brackets.
    See Issue: https://github.com/Instagram/LibCST/issues/640
    """
    # (.*?) matches any character in a greedy way
    # What I would like more is "any Character, a Space, a Plus and Quote-Mark" but I was not able to express it
    # TODO: sharpen regex match
    string_pattern = r'\(\("(.*?)" \+ ""\) \+ ""\)'
    string_pattern_2 = r'\("(.*?)" \+ \("" \+ ""\)\)'
    string_result_pattern = r'("\1" + "" + "")'

    int_pattern = r'\(\((.*?) \+ 0\) \+ 0\)'
    int_pattern_2 = r'\((.*?) \+ \(0 \+ 0\)\)'
    int_result_pattern = r'(\1 + 0 + 0)'

    float_pattern = r'\(\((.*?) \+ 0.0\) \+ 0.0\)'
    float_pattern_2 = r'\((.*?) \+ \(0.0 \+ 0.0\)\)'
    float_result_pattern = r'(\1 + 0.0 + 0.0)'

    result:str = to_reduce

    result = re.sub(string_pattern, string_result_pattern, result)
    result = re.sub(string_pattern_2, string_result_pattern, result)
    result = re.sub(int_pattern, int_result_pattern, result)
    result = re.sub(int_pattern_2, int_result_pattern, result)
    result = re.sub(float_pattern, float_result_pattern, result)
    result = re.sub(float_pattern_2, float_result_pattern, result)

    result = result.replace("  "," ")

    return result
