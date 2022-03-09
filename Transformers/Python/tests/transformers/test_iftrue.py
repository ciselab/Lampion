import random

import libcst

from lampion.transformers.iftrue import IfTrueTransformer


def test_if_true_simple_application_should_change_code():
    random.seed(1996)
    example_cst = libcst.parse_module("def some(): return Math.Pi")
    initial_code = str(example_cst.code)

    transformer = IfTrueTransformer()

    altered_cst = transformer.apply(example_cst)
    altered_code = str(altered_cst.code)

    assert altered_code != initial_code


def test_if_true_simple_application_should_have_if_True():
    random.seed(1996)
    example_cst = libcst.parse_module("def some(): return Math.Pi")

    transformer = IfTrueTransformer(max_tries=100)

    altered_cst = transformer.apply(example_cst)
    altered_code = str(altered_cst.code)

    assert "True" in altered_code
    assert "if" in altered_code


def test_if_true_simple_application_transformer_worked():
    random.seed(1996)
    example_cst = libcst.parse_module("def some(): return Math.Pi")
    initial_code = str(example_cst.code)

    transformer = IfTrueTransformer()
    transformer.apply(example_cst)
    assert transformer.worked()


def test_if_true_simple_application_transformer_reset_works_as_expected():
    random.seed(1996)
    example_cst = libcst.parse_module("def some(): return Math.Pi")

    transformer = IfTrueTransformer()
    transformer.apply(example_cst)

    assert transformer.worked()
    transformer.reset()
    assert not transformer.worked()


def test_if_true_simple_application_should_change_code_example_b():
    random.seed(1996)
    example_cst = libcst.parse_module("def some_other(): \n\tprint('Hello World')")
    initial_code = str(example_cst.code)

    transformer = IfTrueTransformer()

    altered_cst = transformer.apply(example_cst)
    altered_code = str(altered_cst.code)

    assert altered_code != initial_code
    assert "True" in altered_code
    assert "if" in altered_code


def test_if_true_multiline_method_should_be_applied():
    random.seed(1996)
    example_cst = libcst.parse_module("def some(): \n\ta = Math.Pi \n\treturn a")
    transformer = IfTrueTransformer()

    altered_cst = transformer.apply(example_cst)
    altered_code = str(altered_cst.code)

    assert "True" in altered_code
    assert "if" in altered_code


def test_if_true_apply_multiple_times_should_be_applied_multiple_times():
    random.seed(1996)
    example_cst = libcst.parse_module("def some(): \n\ta = Math.Pi \n\treturn a")
    transformer = IfTrueTransformer()
    altered_cst = example_cst

    for i in range(0, 10):
        altered_cst = transformer.apply(altered_cst)
        transformer.reset()
    altered_code = str(altered_cst.code)

    assert altered_code.count("if") == 10
    assert altered_code.count("True") == 10


def test_if_true_empty_module_should_not_change():
    random.seed(1996)
    example_cst = libcst.parse_module("")
    initial_code = str(example_cst.code)

    transformer = IfTrueTransformer()

    altered_cst = transformer.apply(example_cst)
    altered_code = str(altered_cst.code)

    assert altered_code == initial_code


def test_if_true_empty_module_should_not_work():
    random.seed(1996)
    example_cst = libcst.parse_module("")

    transformer = IfTrueTransformer()

    transformer.apply(example_cst)

    assert not transformer.worked()


def test_if_true_method_is_in_class_should_change():
    example_cst = libcst.parse_module("class some:\n\tdef hi():  \n\t\ta: int = 1\n\t\tprint(\"Hello World\")")
    random.seed(1996)

    transformer = IfTrueTransformer(max_tries=150)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    altered_code = str(altered_cst.code)
    assert example_cst.code != altered_code

    assert "if" in altered_code
    assert "True" in altered_code


def test_if_true_method_has_if_is_applied_a_second_time():
    code: str = """
def even(num):
    if (num % 2) == 0:
        return True
    else:
        return False
    """

    example_cst = libcst.parse_module(code)
    random.seed(1996)

    transformer = IfTrueTransformer(max_tries=150)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    altered_code = str(altered_cst.code)

    assert altered_code.count("if") == 2
    assert altered_code.count("True") == 2


def test_if_true_two_methods_if_is_applied_once():
    code: str = """
def even(num):
    return (num % 2) == 0

def odd(num):
    return not even(num)
    """

    example_cst = libcst.parse_module(code)
    random.seed(1996)

    transformer = IfTrueTransformer(max_tries=25)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    altered_code = str(altered_cst.code)

    assert altered_code.count("if") == 1
    assert altered_code.count("True") == 1


def test_if_true_method_with_try_should_work():
    code: str = """
def some():
    try:
        open("meme.png")
    except:
        print("No meme")
        """

    example_cst = libcst.parse_module(code)
    random.seed(1996)

    transformer = IfTrueTransformer(max_tries=25)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    altered_code = str(altered_cst.code)

    assert altered_code.count("if") == 1
    assert altered_code.count("True") == 1


def test_if_true_method_with_while_should_work():
    code: str = """
def some(a):
    while a<100:
        a = a + 1
        """

    example_cst = libcst.parse_module(code)
    random.seed(1996)

    transformer = IfTrueTransformer(max_tries=25)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    altered_code = str(altered_cst.code)

    assert altered_code.count("if") == 1
    assert altered_code.count("True") == 1


def test_if_true_method_with_for_should_work():
    code: str = """
def some(a):
    for a in range(1,10):
        print(a)
        """

    example_cst = libcst.parse_module(code)
    random.seed(1996)

    transformer = IfTrueTransformer(max_tries=25)
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    altered_code = str(altered_cst.code)

    assert altered_code.count("if") == 1
    assert altered_code.count("True") == 1


def test_constructor_if_true_transformer_has_categories():
    transformer = IfTrueTransformer()

    assert len(transformer.categories()) > 0


def test_constructor_fresh_transformer_has_not_worked():
    transformer = IfTrueTransformer()
    assert not transformer.worked()
