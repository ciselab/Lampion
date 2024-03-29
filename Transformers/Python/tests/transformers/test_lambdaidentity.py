import libcst
import regex as re
import random

from lampion.transformers.lambdaidentity import LambdaIdentityTransformer, _reduce_brackets


# Floats

def test_lambdaidentity_for_float_should_use_identity_lambda():
    example_cst = libcst.parse_module("def some(): \n\ta = 0.5 \n\treturn a")

    random.seed(1996)

    transformer = LambdaIdentityTransformer(max_tries=1)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert "lambda: 0.5" in altered_code
    assert "()" in altered_code


def test_lambdaidentity_apply_twice_for_float_should_use_identity_lambda():
    example_cst = libcst.parse_module("def some(): \n\ta = 0.5 \n\treturn a")

    random.seed(1996)
    transformer = LambdaIdentityTransformer(max_tries=1)
    # This test needs a bigger re-try value due to (strange) LibCST Behavior
    transformer.set_max_tries(1)

    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    transformer.reset()

    altered_cst = transformer.apply(altered_cst)
    altered_code = altered_cst.code

    assert altered_code.count("lambda") == 2


def test_lambdaidentity_apply_to_method_with_two_floats_should_be_applied_once_only():
    example_cst = libcst.parse_module("def some(): \n\ta = 0.5\n\tb=0.3 \n\treturn a+b")

    random.seed(1996)
    transformer = LambdaIdentityTransformer(max_tries=1)
    # This test needs a bigger re-try value due to (strange) LibCST Behavior
    transformer.set_max_tries(1)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert "lambda" in altered_code
    assert altered_code.count("lambda") == 1


def test_lambdaidentity_apply_to_method_with_two_identical_floats_should_be_applied_is_applied_at_first_instance():
    example_cst = libcst.parse_module("def some(): \n\ta = 0.5\n\tb=0.5 \n\treturn a+b")

    random.seed(1996)
    transformer = LambdaIdentityTransformer(max_tries=1)
    # This test needs a bigger re-try value due to (strange) LibCST Behavior
    transformer.set_max_tries(80)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert "lambda" in altered_code
    assert altered_code.count("lambda") == 1


def test_lambdaidentity_apply_twice_for_float_should_work():
    example_cst = libcst.parse_module("def some(): \n\ta = 0.5 \n\treturn a")

    random.seed(1996)
    transformer = LambdaIdentityTransformer(max_tries=1)
    # This test needs a bigger re-try value due to (strange) LibCST Behavior
    transformer.set_max_tries(50)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    transformer.reset()
    altered_cst = transformer.apply(altered_cst)

    assert transformer.worked()


def test_lambdaidentity_for_float_in_return_should_use_identity_lambda():
    example_cst = libcst.parse_module("def some():\n\treturn 0.5")

    random.seed(1996)
    transformer = LambdaIdentityTransformer(max_tries=1)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = str(altered_cst.code)

    assert "lambda: 0.5" in altered_code


def test_lambdaidentity_for_float_apply_twice_in_return_should_reduce():
    example_cst = libcst.parse_module("def some():\n\treturn 0.5")

    random.seed(1996)
    transformer = LambdaIdentityTransformer(max_tries=1)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    transformer.reset()

    altered_cst = transformer.apply(altered_cst)

    altered_code = str(altered_cst.code)

    assert altered_code.count("lambda") == 2
    assert '((lambda: lambda: 0.5)()())' in altered_code


def test_lambdaidentity_for_float_in_default_parameters_should_use_identity_lambda():
    example_cst = libcst.parse_module("def some(a: float = 0.5):\n\treturn a")

    random.seed(1996)
    transformer = LambdaIdentityTransformer(max_tries=1)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert "(lambda: 0.5)()" in altered_code
    assert "lambda" in altered_code


def test_lambdaidentity_for_float_in_default_parameters_should_work():
    example_cst = libcst.parse_module("def some(a: float = 0.5):\n\treturn a")

    random.seed(1996)
    transformer = LambdaIdentityTransformer(max_tries=1)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert transformer.worked()


def test_lambdaidentity_for_float_should_have_worked():
    example_cst = libcst.parse_module("def some(): \n\ta = 0.5 \n\treturn a")

    random.seed(1996)
    transformer = LambdaIdentityTransformer(max_tries=1)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert transformer.worked()


# Strings

def test_lambdaidentity_for_string_should_add_lambda_variant_a():
    example_cst = libcst.parse_module("def some(): \n\ta = \"text\" \n\treturn a")

    random.seed(1996)
    transformer = LambdaIdentityTransformer(max_tries=100)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    print("Altered Code", altered_code)
    # Happens in add neutral too
    assert "lambda" in altered_code


def test_lambdaidentity_for_string_should_add_lambda_variant_b():
    example_cst = libcst.parse_module("def some(): \n\ta = \"test\" \n\treturn a")

    random.seed(1996)
    transformer = LambdaIdentityTransformer(max_tries=100)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert "lambda" in altered_code


def test_lambdaidentity_for_string_should_add_lambda_variant_c():
    example_cst = libcst.parse_module("def some_other(): \n\ta = \"gold\" \n\treturn a")

    random.seed(1996)
    transformer = LambdaIdentityTransformer(max_tries=100)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    print("Altered Code", altered_code)

    assert "lambda" in altered_code


def test_lambdaidentity_apply_twice_for_string_should_use_identity_lambda():
    example_cst = libcst.parse_module("def some(): \n\ta = \"text\" \n\treturn a")

    random.seed(1996)
    transformer = LambdaIdentityTransformer(max_tries=1)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    transformer.reset()

    altered_cst = transformer.apply(altered_cst)
    altered_code = altered_cst.code

    assert altered_code.count("lambda") == 2


def test_lambdaidentity_apply_twice_for_string_should_reduce():
    example_cst = libcst.parse_module("def some(): \n\ta = \"text\" \n\treturn a")

    random.seed(1996)
    transformer = LambdaIdentityTransformer(max_tries=1)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    transformer.reset()

    altered_cst = transformer.apply(altered_cst)
    altered_code = str(altered_cst.code)

    assert altered_code.count("lambda") == 2
    assert '((lambda: lambda: "text")()())' in altered_code


def test_lambdaidentity_apply_to_method_with_two_different_strings_should_be_applied_once_only():
    example_cst = libcst.parse_module("def some(): \n\ta =\"hello\"\n\tb=\"world\" \n\treturn a+b")

    random.seed(1996)
    transformer = LambdaIdentityTransformer(max_tries=100)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert "lambda" in altered_code
    assert altered_code.count("lambda") == 1


def test_lambdaidentity_apply_to_method_with_two_identical_string_should_be_applied_is_applied_at_first_instance():
    example_cst = libcst.parse_module("def some(): \n\ta = \"hello\"\n\tb=\"hello\" \n\treturn a+b")

    random.seed(1996)
    transformer = LambdaIdentityTransformer(max_tries=50)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert "lambda" in altered_code
    assert altered_code.count("lambda") == 1


def test_lambdaidentity_apply_to_method_with_two_identical_string_transformer_worked():
    example_cst = libcst.parse_module("def some(): \n\ta = \"hello\"\n\tb=\"hello\" \n\treturn a+b")

    random.seed(1996)
    transformer = LambdaIdentityTransformer(max_tries=150)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    assert transformer.worked()


def test_lambdaidentity_apply_twice_for_string_should_work():
    example_cst = libcst.parse_module("def some(): \n\ta = \"hello\" \n\treturn a")

    random.seed(1996)
    transformer = LambdaIdentityTransformer(max_tries=125)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    transformer.reset()
    altered_cst = transformer.apply(altered_cst)
    altered_code = altered_cst.code

    assert transformer.worked()


def test_lambdaidentity_for_string_in_return_should_add_lambda_variant_a():
    example_cst = libcst.parse_module("def some():\n\treturn \"world\"")

    random.seed(1996)
    transformer = LambdaIdentityTransformer(max_tries=1)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code
    assert "lambda" in altered_code


def test_lambdaidentity_for_string_in_return_should_add_lambda_variant_b():
    example_cst = libcst.parse_module("def some():\n\treturn \"world\"")

    random.seed(1996)
    transformer = LambdaIdentityTransformer(max_tries=50)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert "lambda" in altered_code


def test_lambdaidentity_for_string_in_default_parameters_should_use_identity_lambda():
    example_cst = libcst.parse_module("def some(a: str = \"hello\"):\n\treturn a")

    random.seed(1996)
    transformer = LambdaIdentityTransformer(max_tries=1)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert "(lambda: \"hello\")()" in altered_code
    assert "lambda" in altered_code


def test_lambdaidentity_for_string_in_default_parameters_should_work():
    example_cst = libcst.parse_module("def some(a: str = \"hello\"):\n\treturn a")

    random.seed(1996)
    transformer = LambdaIdentityTransformer(max_tries=1)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert transformer.worked()


def test_lambdaidentity_for_string_should_have_worked():
    example_cst = libcst.parse_module("def some(): \n\ta = \"hello\" \n\treturn a")

    random.seed(1996)
    transformer = LambdaIdentityTransformer(max_tries=1)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert transformer.worked()


# Integers

def test_lambdaidentity_for_int_should_use_identity_lambda():
    example_cst = libcst.parse_module("def some(): \n\ta = 5 \n\treturn a")

    random.seed(1996)
    transformer = LambdaIdentityTransformer(max_tries=50)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert "lambda: 5" in altered_code


def test_lambdaidentity_apply_twice_for_int_should_use_identity_lambda():
    example_cst = libcst.parse_module("def some(): \n\ta = 5 \n\treturn a")

    random.seed(1996)
    transformer = LambdaIdentityTransformer(max_tries=50)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    transformer.reset()

    altered_cst = transformer.apply(altered_cst)
    altered_code = altered_cst.code

    assert altered_code.count("lambda") == 2


def test_lambdaidentity_apply_twice_for_int_should_reduce():
    example_cst = libcst.parse_module("def some(): \n\ta = 5 \n\treturn a")

    random.seed(1996)
    transformer = LambdaIdentityTransformer(max_tries=50)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    transformer.reset()

    altered_cst = transformer.apply(altered_cst)
    altered_code = str(altered_cst.code)

    assert altered_code.count("lambda") == 2
    assert "((lambda: lambda: 5)()())" in altered_code


def test_lambdaidentity_apply_to_method_with_two_different_ints_should_be_applied_once_only():
    example_cst = libcst.parse_module("def some(): \n\ta = 5\n\tb=3 \n\treturn a+b")

    random.seed(1996)
    transformer = LambdaIdentityTransformer(max_tries=1)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert "lambda" in altered_code
    assert altered_code.count("lambda") == 1


def test_lambdaidentity_apply_to_method_with_two_identical_ints_should_be_applied_is_applied_at_first_instance():
    example_cst = libcst.parse_module("def some(): \n\ta = 4\n\tb = 4 \n\treturn a+b")

    random.seed(1996)

    transformer = LambdaIdentityTransformer(max_tries=50)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = str(altered_cst.code)

    assert "lambda" in altered_code
    assert altered_code.count("lambda") == 1


def test_lambdaidentity_apply_to_method_with_two_identical_ints_should_work():
    example_cst = libcst.parse_module("def some(): \n\ta = 4\n\tb = 4 \n\treturn a+b")

    random.seed(1996)
    transformer = LambdaIdentityTransformer(max_tries=1)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert transformer.worked()


def test_lambdaidentity_apply_twice_for_int_should_work():
    example_cst = libcst.parse_module("def some(): \n\ta = 5 \n\treturn a")

    random.seed(1996)
    transformer = LambdaIdentityTransformer(max_tries=125)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    transformer.reset()
    altered_cst = transformer.apply(altered_cst)
    altered_code = altered_cst.code

    assert transformer.worked()


def test_lambdaidentity_for_int_in_return_should_use_identity_lambda():
    example_cst = libcst.parse_module("def some():\n\treturn 5")

    random.seed(1996)
    transformer = LambdaIdentityTransformer(max_tries=1)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert "(lambda: 5)" in altered_code


def test_lambdaidentity_for_int_in_default_parameters_should_use_identity_lambda():
    example_cst = libcst.parse_module("def some(a: int = 5):\n\treturn a")

    random.seed(1996)
    transformer = LambdaIdentityTransformer(max_tries=1)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert "(lambda: 5)()" in altered_code


def test_lambdaidentity_for_int_in_default_parameters_should_work():
    example_cst = libcst.parse_module("def some(a: int = 5):\n\treturn a")

    random.seed(1996)
    transformer = LambdaIdentityTransformer(max_tries=1)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    assert transformer.worked()


def test_lambdaidentity_for_int_should_have_worked():
    example_cst = libcst.parse_module("def some(): \n\ta = 5 \n\treturn a")

    random.seed(1996)
    transformer = LambdaIdentityTransformer(max_tries=1)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    assert transformer.worked()


# Empty Methods // No Literals

def test_lambdaidentity_method_has_no_literals_transformer_did_not_work():
    example_cst = libcst.parse_module("def some(): return Math.Pi")

    random.seed(1996)
    transformer = LambdaIdentityTransformer(max_tries=1)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert not transformer.worked()


def test_get_categories_is_not_null():
    transformer = LambdaIdentityTransformer()

    assert len(transformer.categories()) != 0

def test_lambdaidentity_method_has_no_literals_transformer_did_not_work_even_after_many_retries():
    example_cst = libcst.parse_module("def some(): return Math.Pi")

    random.seed(1996)
    transformer = LambdaIdentityTransformer(max_tries=150)
    transformer.reset()

    transformer.apply(example_cst)

    assert not transformer.worked()


def test_lambdaidentity_method_has_no_literals_code_did_not_change():
    example_cst = libcst.parse_module("def some(): return Math.Pi")
    initial_code = str(example_cst.code)

    random.seed(1996)
    transformer = LambdaIdentityTransformer(max_tries=1)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = str(altered_cst.code)

    assert initial_code == altered_code


def test_reduce_brackets_with_int_should_work():
    input = '((lambda: ((lambda: 5)()))())'
    expected = '((lambda: lambda: 5)()())'
    result = _reduce_brackets(input)

    assert result == expected


def test_reduce_brackets_with_float_should_work():
    input = '((lambda: ((lambda: 4.005)()))())'
    expected = '((lambda: lambda: 4.005)()())'
    result = _reduce_brackets(input)

    assert result == expected


def test_reduce_brackets_with_string_should_work():
    input = '((lambda: ((lambda: "Hello")()))())'
    expected = '((lambda: lambda: "Hello")()())'
    result = _reduce_brackets(input)

    assert result == expected


def test_reduce_brackets_with_two_ints_should_work():
    input = '((lambda: ((lambda: 1 + 2)()))())'
    expected = '((lambda: lambda: 1 + 2)()())'
    result = _reduce_brackets(input)

    assert result == expected


def test_reduce_brackets_with_two_ints_in_brackets_should_work():
    input = '((lambda: ((lambda: (1 + 2))()))())'
    expected = '((lambda: lambda: (1 + 2))()())'
    result = _reduce_brackets(input)

    assert result == expected


def test_reduce_brackets_only_one_lambda_nothing_changed():
    input = '((lambda: 5)())'
    result = _reduce_brackets(input)

    assert result == input


def test_reduce_brackets_no_lambdas_nothing_changed():
    input = 'var b = (5 + 21)'
    result = _reduce_brackets(input)

    assert result == input


def test_reduce_brackets_sample_from_tests_int():
    input = 'a = ((lambda: ((lambda: 5)()))())'
    expected = 'a = ((lambda: lambda: 5)()())'

    result = _reduce_brackets(input)

    assert result == expected


def test_reduce_brackets_sample_from_tests_float():
    input = 'a = ((lambda: ((lambda: 5.5)()))())'
    expected = 'a = ((lambda: lambda: 5.5)()())'

    result = _reduce_brackets(input)

    assert result == expected


def test_reduce_brackets_sample_from_tests_string():
    input = 'a = ((lambda: ((lambda: "abc")()))())'
    expected = 'a = ((lambda: lambda: "abc")()())'

    result = _reduce_brackets(input)

    assert result == expected


def test_reduce_brackets_sample_from_tests_string_string_in_brackets():
    input = 'a = ((lambda: ((lambda: ("abc"))()))())'
    expected = 'a = ((lambda: lambda: ("abc"))()())'

    result = _reduce_brackets(input)

    assert result == expected


# Manual Learning Regex
# To Find the right pattern and test it
# Kept for documentation / experimentation reasons

def test_reduce_brackets_int():
    sample = '((lambda: (lambda: 5)())()'

    pattern = r'\(\(lambda: \(lambda: (\w+)\)\(\)\)\(\)'
    output_pattern = r'((lambda: lambda: \1) ()())'
    result = re.sub(pattern, output_pattern, sample)

    assert result == '((lambda: lambda: 5) ()())'


def test_reduce_brackets_float():
    sample = '((lambda: (lambda: 5.2)())()'

    pattern = r'\(\(lambda: \(lambda: (.*?)\)\(\)\)\(\)'
    output_pattern = r'((lambda: lambda: \1) ()())'
    result = re.sub(pattern, output_pattern, sample)

    assert result == '((lambda: lambda: 5.2) ()())'


def test_reduce_brackets_string():
    sample = '((lambda: (lambda: "Hello World")())()'

    pattern = r'\(\(lambda: \(lambda: (.*?)\)\(\)\)\(\)'
    output_pattern = r'((lambda: lambda: \1) ()())'
    result = re.sub(pattern, output_pattern, sample)

    assert result == '((lambda: lambda: "Hello World") ()())'
