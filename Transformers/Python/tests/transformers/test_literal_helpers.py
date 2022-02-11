import libcst
from lampion.transformers.literal_helpers import get_all_literals

def test_get_all_literals_for_one_float_should_have_one_float():
    example_cst = libcst.parse_module("def some(): \n\ta = 0.5 \n\treturn a")

    literals = get_all_literals(example_cst)

    assert len(literals) == 1
    assert literals[0][0] == "float"


def test_get_all_literals_for_two_floats_should_have_two_floats():
    example_cst = libcst.parse_module("def some(): \n\ta = 0.5 \n\td = 2.6 \n\treturn a")

    literals = get_all_literals(example_cst)

    assert len(literals) == 2

def test_get_all_literals_for_identical_two_floats_should_have_two_floats():
    example_cst = libcst.parse_module("def some(): \n\ta = 0.5 \n\td = 0.5 \n\treturn a")

    literals = get_all_literals(example_cst)
    print(literals)
    assert len(literals) == 2

def test_get_all_literals_for_default_var_should_have_literal():
    example_cst = libcst.parse_module("def some(a = 0.5): \n\treturn a")

    literals = get_all_literals(example_cst)

    assert len(literals) == 1


def test_get_all_literals_for_return_value_should_have_literal():
    example_cst = libcst.parse_module("def some(): \n\treturn 0.5")

    literals = get_all_literals(example_cst)

    assert len(literals) == 1


def test_get_all_literals_for_one_string_should_have_one_string():
    example_cst = libcst.parse_module("def some(): \n\ta = 'huh' \n\treturn a")

    literals = get_all_literals(example_cst)

    assert len(literals) == 1
    assert literals[0][0] == "simple_string"


def test_get_all_literals_for_two_strings_should_have_two_strings():
    example_cst = libcst.parse_module("def some(): \n\ta = 'huh' \n\td = 'hah' \n\treturn a")

    literals = get_all_literals(example_cst)

    assert len(literals) == 2

def test_get_all_literals_for_identical_two_strings_should_have_two_strings():
    example_cst = libcst.parse_module("def some(): \n\ta = 'huh' \n\td = 'huh' \n\treturn a")

    literals = get_all_literals(example_cst)

    assert len(literals) == 2

def test_get_all_literals_for_string_default_var_should_have_literal():
    example_cst = libcst.parse_module("def some(a = 'hi'): \n\treturn a")

    literals = get_all_literals(example_cst)

    assert len(literals) == 1


def test_get_all_literals_for_string_return_value_should_have_literal():
    example_cst = libcst.parse_module("def some(): \n\treturn 'Hi'")

    literals = get_all_literals(example_cst)

    assert len(literals) == 1


def test_get_all_literals_for_one_int_should_have_one_int():
    example_cst = libcst.parse_module("def some(): \n\ta = 5 \n\treturn a")

    literals = get_all_literals(example_cst)

    assert len(literals) == 1
    assert literals[0][0] == "integer"


def test_get_all_literals_for_two_int_should_have_two_ints():
    example_cst = libcst.parse_module("def some(): \n\ta = 5 \n\td = 6 \n\treturn a")

    literals = get_all_literals(example_cst)

    assert len(literals) == 2

def test_get_all_literals_for_identical_two_ints_should_have_two_ints():
    example_cst = libcst.parse_module("def some(): \n\ta = 5 \n\td = 5 \n\treturn a")

    literals = get_all_literals(example_cst)

    assert len(literals) == 2

def test_get_all_literals_for_int_default_var_should_have_literal():
    example_cst = libcst.parse_module("def some(a = 5): \n\treturn a")

    literals = get_all_literals(example_cst)

    assert len(literals) == 1


def test_get_all_literals_for_int_return_value_should_have_literal():
    example_cst = libcst.parse_module("def some(): \n\treturn 5")

    literals = get_all_literals(example_cst)

    assert len(literals) == 1
