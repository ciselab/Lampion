# Transformer-Example
# https://libcst.readthedocs.io/en/latest/tutorial.html
from typing import Optional, Union

import libcst as cst
from libcst import MaybeSentinel, FlattenSentinel, RemovalSentinel


class RenameParameterTransformer(cst.CSTTransformer):
    def __init__(self):
        print("RenameParameterTransformer Init called")

    def visit_Param(self, node: "Param") -> Optional[bool]:

        #replacement = node.name.with_changes(value="hi")
        #node.name.deep_replace(node,replacement)
        print(f"Visit Param called",node.name.value)

    def visit_Param_name(self, node: "Param") -> None:
        print("Visiting Param...Name?",node)

    def leave_Param_name(self, node: "Param") -> None:
        print("Leaving Param ... Name?")

    def leave_Param(
        self, original_node: "Param", updated_node: "Param"
    ) -> Union["Param", MaybeSentinel, FlattenSentinel["Param"], RemovalSentinel]:
        #replacement = original_node.name.with_changes(value="hi")
        #updated_node.deep_replace(original_node,replacement)
        #updated_node.name = "Hi"
        print(f"Leaving Param called",updated_node.name.value)
        return updated_node.with_changes(name="hi")
