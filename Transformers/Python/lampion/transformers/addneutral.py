import random
from typing import Optional

import logging as log

from libcst import CSTNode
import libcst as cst

from lampion.transformers.basetransformer import BaseTransformer


class AddNeutralElementTransformer(BaseTransformer):
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

    def __init__(self):
        log.info("AddNeutralElementTransformer Created")
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

        if tries == max_tries:
            log.warning(f"Add Neutral Element Transformer failed after {max_tries} attempts")

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
                replacement = f"({literal}+0.0)"
                expr = cst.parse_expression(replacement)
                updated_node = updated_node.deep_replace(updated_node, expr)
                self.worked = True

                return updated_node
            else:
                return updated_node

        def leave_Integer(
                self, original_node: "Integer", updated_node: "Integer"
        ) -> "BaseExpression":
            if self.replace_type == "integer" \
                    and original_node.deep_equals(self.to_replace) \
                    and not self.worked:
                literal = str(original_node.value)
                replacement = f"({literal}+0)"
                expr = cst.parse_expression(replacement)
                updated_node = updated_node.deep_replace(updated_node, expr)
                self.worked = True

                return updated_node
            return updated_node

        def leave_SimpleString(
                self, original_node: "SimpleString", updated_node: "SimpleString"
        ) -> "BaseExpression":
            if self.replace_type == "simple_string" \
                    and original_node.deep_equals(self.to_replace) \
                    and not self.worked:
                literal = str(original_node.value)
                replacement = f"({literal}+\"\")"
                expr = cst.parse_expression(replacement)
                updated_node = updated_node.deep_replace(updated_node, expr)
                self.worked = True

                return updated_node
            return updated_node
