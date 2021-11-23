#RenameVariableTransformer
import libcst
import pytest

from lampion.transformers.renamevar import RenameVariableTransformer



def test_add_var_method_has_one_variable_should_change():
    example_cst = libcst.parse_module("def hi(): \n\ta = 1\n\tprint(\"Hello World\")\n\treturn a")

    transformer = RenameVariableTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    altered_code = altered_cst.code
    assert example_cst.code != altered_code

def test_add_var_method_has_one_variable_should_change_at_all_places():
    example_cst = libcst.parse_module("def hi(): \n\tyyy = 1\n\tyyy = yyy + 1\n\treturn yyy")

    transformer = RenameVariableTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    altered_code = altered_cst.code
    assert "yyy" not in altered_code

def test_add_var_method_has_one_variable_should_keep_assigned_value():
    example_cst = libcst.parse_module("def hi(): \n\ta = 1\n\tprint(\"Hello World\")")

    transformer = RenameVariableTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    altered_code = altered_cst.code
    assert "1" in altered_code

def test_add_var_method_has_one_variable_should_keep_method_name():
    example_cst = libcst.parse_module("def hi(): \n\ta = 1\n\tprint(\"Hello World\")")

    transformer = RenameVariableTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    altered_code = altered_cst.code
    assert "hi()" in altered_code
    assert "print" in altered_code

def test_add_var_method_has_two_variables_should_keep_assigned_values():
    example_cst = libcst.parse_module("def hi(): \n\ta = 1\n\tb = 2\n\tprint(\"Hello World\")")

    transformer = RenameVariableTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    altered_code = altered_cst.code
    assert "1" in altered_code
    assert "2" in altered_code

def test_add_var_method_has_one_typed_variable_should_change():
    example_cst = libcst.parse_module("def hi(): \n\ta: int = 1\n\tprint(\"Hello World\")")

    transformer = RenameVariableTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    altered_code = altered_cst.code
    assert example_cst.code != altered_code


def test_add_var_method_has_one_typed_variable_should_keep_type():
    example_cst = libcst.parse_module("def hi(): \n\ta: int = 1\n\tprint(\"Hello World\")")

    transformer = RenameVariableTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    altered_code = altered_cst.code
    assert "int" in altered_code

def test_add_var_method_has_two_variables_should_change():
    example_cst = libcst.parse_module("def hi(): \n\ta = 1\n\tb = 2\n\tprint(\"Hello World\")")

    transformer = RenameVariableTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    altered_code = altered_cst.code
    assert example_cst.code != altered_code

def test_add_var_method_has_no_variables_should_stay_unchanged():
    example_cst = libcst.parse_module("def hi(): \n\tprint(\"Hello World\")")

    transformer = RenameVariableTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    altered_code = altered_cst.code
    assert example_cst.code == altered_code


def test_rename_var_method_is_in_class_class_should_stay_unchanged():
    example_cst = libcst.parse_module("class some:\n\tdef hi():  \n\t\ta = 1\n\t\tprint(\"Hello World\")")

    transformer = RenameVariableTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    altered_code = altered_cst.code
    assert "some" in altered_code

def test_rename_var_method_is_in_class_attribute_can_change():
    example_cst = libcst.parse_module("class some:\n\tbbb = 3\n\tdef hi():  \n\t\ta = 1\n\t\tprint(\"Hello World\")")

    transformer = RenameVariableTransformer()
    i = 0
    while i < 10:
        transformer.reset()
        altered_cst = transformer.apply(example_cst)
        i = i + 1

    altered_code = altered_cst.code
    assert "bbb" not in altered_code
    assert "3" in altered_code

def test_rename_var_method_is_in_class_should_change():
    example_cst = libcst.parse_module("class some:\n\tdef hi():  \n\t\ta: int = 1\n\t\tprint(\"Hello World\")")

    transformer = RenameVariableTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    altered_code = altered_cst.code
    assert example_cst.code != altered_code


def example_with_one_var():
    return libcst.parse_module("def hi(): \n\ta = 1\n\tprint(\"Hello World\")")

def example_with_two_vars():
    return libcst.parse_module("def hi(): \n\ta = 1\n\tb = 2\n\tprint(\"Hello World\")")

def example_with_one_typed_var():
    return libcst.parse_module("def hi(): \n\ta: int = 1\n\tprint(\"Hello World\")")