# Transformer-Example
# https://libcst.readthedocs.io/en/latest/tutorial.html
from typing import Optional, Union

import libcst as cst
from libcst import MaybeSentinel, FlattenSentinel, RemovalSentinel


class RenameParameterTransformer(cst.CSTTransformer):
    def __init__(self):
        print("RenameParameterTransformer Init called")

    def visit_Param(self, node: "Param") -> Optional[bool]:
        print(f"Visit Param called",node.name.value)

    def leave_Param(
        self, original_node: "Param", updated_node: "Param"
    ) -> Union["Param", MaybeSentinel, FlattenSentinel["Param"], RemovalSentinel]:
        print(f"Leaving Param called",original_node.name.value)
        return original_node
