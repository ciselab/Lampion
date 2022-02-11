from typing import Optional

import libcst


def get_all_literals(cst_to_collect: "Node") -> [(str, "Node")]:
    """
    Uses a LibCST Visitor to gather all "simple_string", "float" and "integer" literals in the given LibCST Node.
    If the LibCST Node is e.g. a module, it returns all modules.
    Can contain duplicates.
    For convenience, the return value is a tuple of ("type","value").

    :param cst_to_collect: The LibCST Node to gather literals in. Recursively traverses down-stream.
    :returns: The seen literals, as a list of (type,value). Value is a libcst literal node.

    Extracted as used in multiple places, and for easier testability.
    """
    visitor = __LiteralCollector()
    cst_to_collect.visit(visitor)
    results = [("simple_string", x) for x in visitor.seen_strings] \
              + [("float", x) for x in visitor.seen_floats] \
              + [("integer", x) for x in visitor.seen_integers]

    del visitor
    return results


class __LiteralCollector(libcst.CSTVisitor):

    def __init__(self):
        self.seen_floats = []
        self.seen_strings = []
        self.seen_integers = []

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
