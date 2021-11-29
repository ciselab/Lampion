import random
from typing import Optional

import logging as log

from libcst import CSTNode
import libcst as cst

from lampion.transformers.basetransformer import BaseTransformer


class LambdaIdentityTransformer(BaseTransformer):
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

    def apply(self, cst: CSTNode) -> CSTNode:
        visitor = self.__LiteralCollector()

        altered_cst = cst

        tries: int = 0
        max_tries: int = 100

        while (not self._worked) and tries <= max_tries:
            cst.visit(visitor)

            seen_literals = \
                [("simple_string", x) for x in visitor.seen_strings] \
                + [("float", x) for x in visitor.seen_floats] \
                + [("integer", x) for x in visitor.seen_integers]
            # Exit early: No Literals to work on!
            if len(seen_literals) == 0:
                self._worked = False
                return cst

            to_replace = random.choice(seen_literals)

            replacer = self.__Replacer(to_replace[1], to_replace[0])

            altered_cst = cst.visit(replacer)

            tries = tries + 1
            self._worked = replacer.worked

        if tries == max_tries:
            log.warning(f"Lambda Identity Transformer failed after {max_tries} attempts")

        # TODO: add Post-Processing Values here

        return altered_cst

    def reset(self) -> None:
        self._worked = False

    def worked(self) -> bool:
        return self._worked

    def categories(self) -> [str]:
        return ["Smell", "Operators"]

    def postprocessing(self) -> None:
        self.reset()

    class __LiteralCollector(cst.CSTVisitor):
        finished = True
        seen_floats = []
        seen_strings = []
        seen_integers = []

        def visit_Float(self, node: "Float") -> Optional[bool]:
            self.seen_floats.append(node)

        def visit_Integer(self, node: "Integer") -> Optional[bool]:
            self.seen_integers.append(node)

        def visit_SimpleString(self, node: "SimpleString") -> Optional[bool]:
            self.seen_strings.append(node)

    class __Replacer(cst.CSTTransformer):

        def __init__(self, to_replace: "CSTNode", replace_type: str):
            self.to_replace = to_replace
            self.replace_type = replace_type
            self.worked = False

        def leave_Float(
                self, original_node: "Float", updated_node: "Float"
        ) -> "BaseExpression":
            if self.replace_type == "float" and original_node.deep_equals(self.to_replace) and not self.worked:
                literal = str(original_node.value)
                replacement = f"((lambda: {literal})())"
                expr = cst.parse_expression(replacement)
                updated_node = updated_node.deep_replace(updated_node, expr)
                self.worked = True

                return updated_node
            else:
                return updated_node

        def leave_Integer(
                self, original_node: "Integer", updated_node: "Integer"
        ) -> "BaseExpression":
            if self.replace_type == "integer" and original_node.deep_equals(self.to_replace) and not self.worked:
                literal = str(original_node.value)
                replacement = f"((lambda: {literal})())"
                expr = cst.parse_expression(replacement)
                updated_node = updated_node.deep_replace(updated_node, expr)
                self.worked = True

                return updated_node
            else:
                return updated_node

        def leave_SimpleString(
                self, original_node: "SimpleString", updated_node: "SimpleString"
        ) -> "BaseExpression":
            if self.replace_type == "simple_string" and original_node.deep_equals(self.to_replace) and not self.worked:
                literal = str(original_node.value)
                replacement = f"((lambda: {literal})())"
                expr = cst.parse_expression(replacement)
                updated_node = updated_node.deep_replace(updated_node, expr)
                self.worked = True

                return updated_node
            else:
                return updated_node
