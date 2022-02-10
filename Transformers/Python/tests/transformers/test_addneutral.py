import random
import regex as re

import libcst

from lampion.transformers.addneutral import AddNeutralElementTransformer, _reduce_brackets


# Floats

def test_addneutral_for_float_should_add_plus_0():
    example_cst = libcst.parse_module("def some(): \n\ta = 0.5 \n\treturn a")

    random.seed(1996)
    transformer = AddNeutralElementTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert "(0.5+0.0)" in altered_code


def test_addneutral_apply_twice_for_float_should_add_plus_0():
    example_cst = libcst.parse_module("def some(): \n\ta = 0.5 \n\treturn a")

    random.seed(1996)
    transformer = AddNeutralElementTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    transformer.reset()

    altered_cst = transformer.apply(altered_cst)
    altered_code = altered_cst.code

    assert altered_code.count("0.0") == 2


def test_addneutral_apply_to_method_with_two_floats_should_be_applied_once_only():
    example_cst = libcst.parse_module("def some(): \n\ta = 0.5\n\tb=0.3 \n\treturn a+b")

    random.seed(1996)
    transformer = AddNeutralElementTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert "0.0" in altered_code
    assert altered_code.count("0.0") == 1


def test_addneutral_apply_to_method_with_two_identical_floats_should_be_applied_is_applied_at_first_instance():
    example_cst = libcst.parse_module("def some(): \n\ta = 0.5\n\tb=0.5 \n\treturn a+b")

    random.seed(1996)
    transformer = AddNeutralElementTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert "0.0" in altered_code
    assert altered_code.count("0.0") == 1


def test_addneutral_apply_twice_for_float_should_work():
    example_cst = libcst.parse_module("def some(): \n\ta = 0.5 \n\treturn a")

    random.seed(1996)
    transformer = AddNeutralElementTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    transformer.reset()
    altered_cst = transformer.apply(altered_cst)

    assert transformer.worked()


def test_addneutral_for_float_in_return_should_add_plus_0():
    example_cst = libcst.parse_module("def some():\n\treturn 0.5")

    random.seed(1996)
    transformer = AddNeutralElementTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert "(0.5+0.0)" in altered_code


def test_addneutral_for_float_in_default_parameters_should_add_plus_0():
    example_cst = libcst.parse_module("def some(a: float = 0.5):\n\treturn a")

    random.seed(1996)
    transformer = AddNeutralElementTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert "(0.5+0.0)" in altered_code
    assert "0.0" in altered_code


def test_addneutral_for_float_in_default_parameters_should_work():
    example_cst = libcst.parse_module("def some(a: float = 0.5):\n\treturn a")

    random.seed(1996)
    transformer = AddNeutralElementTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    assert transformer.worked()


def test_addneutral_for_float_should_have_worked():
    example_cst = libcst.parse_module("def some(): \n\ta = 0.5 \n\treturn a")

    random.seed(1996)
    transformer = AddNeutralElementTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    assert transformer.worked()


# Strings

def test_addneutral_for_string_should_add_plus_empty_string():
    example_cst = libcst.parse_module("def some(): \n\ta = \"text\" \n\treturn a")

    random.seed(1996)
    transformer = AddNeutralElementTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert "(\"text\"+\"\")" in altered_code


def test_addneutral_apply_twice_for_string_should_add_plus_emptystrings():
    example_cst = libcst.parse_module("def some(): \n\ta = \"text\" \n\treturn a")

    random.seed(1996)
    transformer = AddNeutralElementTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    transformer.reset()

    altered_cst = transformer.apply(altered_cst)
    altered_code = altered_cst.code

    assert altered_code.count("\"\"") == 2


def test_addneutral_apply_to_method_with_two_different_strings_should_be_applied_once_only():
    example_cst = libcst.parse_module("def some(): \n\ta =\"hello\"\n\tb=\"world\" \n\treturn a+b")

    random.seed(1996)
    transformer = AddNeutralElementTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert "\"\"" in altered_code
    assert altered_code.count("\"\"") == 1


def test_addneutral_apply_to_method_with_two_identical_string_should_be_applied_is_applied_at_first_instance():
    example_cst = libcst.parse_module("def some(): \n\ta = \"hello\"\n\tb=\"hello\" \n\treturn a+b")

    random.seed(1996)
    transformer = AddNeutralElementTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert "\"\"" in altered_code
    assert altered_code.count("\"\"") == 1


def test_addneutral_apply_twice_for_string_should_work():
    example_cst = libcst.parse_module("def some(): \n\ta = \"hello\" \n\treturn a")

    random.seed(1996)
    transformer = AddNeutralElementTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    transformer.reset()
    altered_cst = transformer.apply(altered_cst)

    assert transformer.worked()


def test_addneutral_for_string_in_return_should_add_plus_emptystring():
    example_cst = libcst.parse_module("def some():\n\treturn \"world\"")

    random.seed(1996)
    transformer = AddNeutralElementTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert "(\"world\"+\"\")" in altered_code


def test_addneutral_for_string_in_default_parameters_should_add_plus_0():
    example_cst = libcst.parse_module("def some(a: str = \"hello\"):\n\treturn a")

    random.seed(1996)
    transformer = AddNeutralElementTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert "(\"hello\"+\"\")" in altered_code
    assert "\"\"" in altered_code


def test_addneutral_for_string_in_default_parameters_should_work():
    example_cst = libcst.parse_module("def some(a: str = \"hello\"):\n\treturn a")

    random.seed(1996)
    transformer = AddNeutralElementTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    assert transformer.worked()


def test_addneutral_for_string_should_have_worked():
    example_cst = libcst.parse_module("def some(): \n\ta = \"hello\" \n\treturn a")

    random.seed(1996)
    transformer = AddNeutralElementTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert transformer.worked()


def test_addneutral_add_a_lot_of_strings_should_fail_gracefully():
    # There is an issue with adding too many brackets to strings
    # This is not a python error, but from the LibCST Library
    # This test just shows / looks that there is no error thrown
    # I opened an issue with LibCST https://github.com/Instagram/LibCST/issues/640
    example_cst = libcst.parse_module("def some(): \n\ta = \"hello\" \n\treturn a")

    random.seed(1996)
    transformer = AddNeutralElementTransformer()
    altered_cst = example_cst
    for i in range(1, 90):
        transformer.reset()
        altered_cst = transformer.apply(altered_cst)

    assert True


# Integers
def test_addneutral_for_int_should_add_plus_0_variant_a():
    example_cst_a = libcst.parse_module("def some(): \n\ta = 5 \n\treturn a")

    random.seed(1996)
    transformer_a = AddNeutralElementTransformer(max_tries=120)
    transformer_a.reset()

    altered_cst_a = transformer_a.apply(example_cst_a)

    altered_code_a = altered_cst_a.code

    assert transformer_a.worked()
    assert "(5+0)" in altered_code_a


def test_addneutral_for_int_should_add_plus_0_variant_b():
    # Something was odd with adding 0 to 5, so I do a second test
    example_cst_b = libcst.parse_module("def some(): \n\ta = 33 \n\treturn a")

    random.seed(1996)
    transformer_b = AddNeutralElementTransformer(max_tries=120)

    altered_cst_b = transformer_b.apply(example_cst_b)

    altered_code = altered_cst_b.code
    assert transformer_b.worked()
    assert "(33+0)" in altered_code


def test_addneutral_for_int_should_add_plus_0_variant_c():
    # Something was odd with adding 0 to 5, so I do a third test
    example_cst_c = libcst.parse_module("def some(): \n\ta = 1 \n\treturn a")

    random.seed(1996)
    transformer_c = AddNeutralElementTransformer(max_tries=120)
    transformer_c.reset()

    altered_cst_c = transformer_c.apply(example_cst_c)
    altered_code = altered_cst_c.code

    assert transformer_c.worked()
    assert "(1+0)" in altered_code


def test_addneutral_apply_twice_for_int_should_add_plus_0():
    example_cst = libcst.parse_module("def some(): \n\ta = 5 \n\treturn a")

    random.seed(1996)
    transformer = AddNeutralElementTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    transformer.reset()

    altered_cst = transformer.apply(altered_cst)
    transformer.reset()
    altered_code = altered_cst.code

    print(altered_code)
    assert altered_code.count("0") == 2


def test_addneutral_apply_to_method_with_two_different_ints_should_be_applied_once_only():
    example_cst = libcst.parse_module("def some(): \n\ta = 5\n\tb=3 \n\treturn a+b")

    random.seed(1996)
    transformer = AddNeutralElementTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert "0" in altered_code
    assert altered_code.count("0") == 1


def test_addneutral_apply_to_method_with_two_identical_ints_should_be_applied_is_applied_at_first_instance():
    example_cst = libcst.parse_module("def some(): \n\ta = 4\n\tb = 4 \n\treturn a+b")

    random.seed(1996)

    transformer = AddNeutralElementTransformer(max_tries=150)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = str(altered_cst.code)

    assert "0" in altered_code
    assert altered_code.count("0") == 1


def test_addneutral_apply_to_method_with_two_identical_ints_should_work():
    example_cst = libcst.parse_module("def some(): \n\ta = 4\n\tb = 4 \n\treturn a+b")

    random.seed(1996)
    transformer = AddNeutralElementTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert transformer.worked()


def test_addneutral_apply_twice_for_int_should_work():
    example_cst = libcst.parse_module("def some(): \n\ta = 5 \n\treturn a")

    random.seed(1996)
    transformer = AddNeutralElementTransformer(max_tries=150)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    transformer.reset()
    altered_cst = transformer.apply(altered_cst)
    assert transformer.worked()


def test_addneutral_for_int_in_return_should_add_plus_0():
    example_cst = libcst.parse_module("def some():\n\treturn 5")

    random.seed(1996)
    transformer = AddNeutralElementTransformer(max_tries=150)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert "(5+0)" in altered_code


def test_addneutral_for_int_in_default_parameters_should_add_plus_0():
    example_cst = libcst.parse_module("def some(a: int = 5):\n\treturn a")

    random.seed(1996)
    transformer = AddNeutralElementTransformer(max_tries=120)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert "(5+0)" in altered_code
    assert "0" in altered_code


def test_addneutral_for_int_in_default_parameters_should_work():
    example_cst = libcst.parse_module("def some(a: int = 5):\n\treturn a")

    random.seed(1996)
    transformer = AddNeutralElementTransformer(max_tries=120)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    assert transformer.worked()


def test_addneutral_for_int_should_have_worked():
    example_cst = libcst.parse_module("def some(): \n\ta = 5 \n\treturn a")

    random.seed(1996)
    transformer = AddNeutralElementTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    assert transformer.worked()


def test_addneutral_for_int_apply_many_times_should_have_worked():
    example_cst = libcst.parse_module("def some(): \n\ta = 5 \n\treturn a")

    random.seed(1996)
    transformer = AddNeutralElementTransformer(max_tries=10)
    altered_cst = example_cst
    counter = 0
    many_times = 20

    for i in range(0, many_times):
        altered_cst = transformer.apply(altered_cst)
        counter = counter + 1 if transformer.worked() else counter
        transformer.reset()

    print(counter)

    altered_code = altered_cst.code
    assert altered_code.count("0") >= many_times / 2


def test_addneutral_for_string_apply_many_times_should_have_worked():
    example_cst = libcst.parse_module("def some(): \n\ta = \"hello\" \n\treturn a")

    random.seed(1996)
    transformer = AddNeutralElementTransformer(max_tries=10)
    altered_cst = example_cst
    counter = 0
    many_times = 20

    for i in range(1, many_times):
        altered_cst = transformer.apply(altered_cst)
        counter = counter + 1 if transformer.worked() else counter
        transformer.reset()

    altered_code = altered_cst.code
    assert altered_code.count("\"\"") >= many_times / 2


# Empty Methods // No Literals

def test_addneutral_method_has_no_literals_transformer_did_not_work():
    example_cst = libcst.parse_module("def some(): return Math.Pi")

    random.seed(1996)
    transformer = AddNeutralElementTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    assert not transformer.worked()


def test_addneutral_method_has_no_literals_code_did_not_change():
    example_cst = libcst.parse_module("def some(): return Math.Pi")
    initial_code = str(example_cst.code)

    random.seed(1996)
    transformer = AddNeutralElementTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = str(altered_cst.code)

    assert initial_code == altered_code


# Testing Bracket reduction

def test_reduce_brackets_for_strings():
    input = "((\"X\" + \"\") + \"\")"
    expected_output = "(\"X\" + \"\" + \"\")"
    result = _reduce_brackets(input)

    assert result == expected_output


def test_reduce_brackets_for_strings_stacked_string():
    input = '(("X" + "" + "" + "") + "")'
    expected_output = "(\"X\" + \"\" + \"\" + \"\" + \"\")"
    result = _reduce_brackets(input)

    assert result == expected_output


def test_reduce_brackets_for_strings_nothing_to_reduce_remains_unchanged():
    input = '("X" + "" + "" + "")'

    result = _reduce_brackets(input)

    assert result == input


def test_reduce_brackets_for_ints():
    input = "((5 + 0) + 0)"
    expected_output = "(5 + 0 + 0)"
    result = _reduce_brackets(input)

    assert result == expected_output


def test_reduce_brackets_for_ints_stacked_ints():
    input = '((5 + 0 + 0 + 0) + 0)'
    expected_output = "(5 + 0 + 0 + 0 + 0)"
    result = _reduce_brackets(input)

    assert result == expected_output


def test_reduce_brackets_for_ints_nothing_to_reduce_remains_unchanged():
    input = '(5 + 0 + 0 + 0)'

    result = _reduce_brackets(input)

    assert result == input


def test_reduce_brackets_mixed_elements_remains_unchanged():
    input = '((("5" + 0.0) + "") + 0)'

    result = _reduce_brackets(input)

    assert result == input


# Learning Regex
# (You always have to re-learn when you need it)

def test_replacing_brackets():
    # This test was useful to find the right Regex,
    # and is kept for documentation reasons
    sample = "((\"X\" + \"\") + \"\")"
    pattern = r'\(\("(.*?)" \+ ""\) \+ ""\)'
    result_pattern = r'("\1" + "" + "")'

    result = re.sub(pattern, result_pattern, sample)

    assert result.strip() == "(\"X\" + \"\" + \"\")"


def test_replacing_brackets_stacked():
    # This test was useful to find the right Regex,
    # and is kept for documentation reasons
    sample = '(("X" + "" + "" + "") + "")'
    pattern = r'\(\("(.*?)" \+ ""\) \+ ""\)'
    result_pattern = r'("\1" + "" + "")'

    result = re.sub(pattern, result_pattern, sample)

    assert result.strip() == "(\"X\" + \"\" + \"\" + \"\" + \"\")"
