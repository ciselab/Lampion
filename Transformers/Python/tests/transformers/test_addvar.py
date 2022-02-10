import libcst
import random

from lampion.transformers.addvar import AddVariableTransformer


def test_addvar_working_example_should_add_line():
    example_cst = example()
    random.seed(1996)

    transformer = AddVariableTransformer(max_tries=100)
    transformer.reset()

    altered_cst = visit_until_worked(example_cst, transformer)

    altered_code = altered_cst.code
    print(altered_code)
    lines = len(altered_code.splitlines())
    assert (lines == 3)


def test_addvar_empty_module_should_not_add_line():
    example_cst = libcst.parse_module("")
    random.seed(1996)

    transformer = AddVariableTransformer()
    transformer.reset()

    altered_cst = visit_until_worked(example_cst, transformer)

    altered_code = altered_cst.code
    print(altered_code)
    lines = len(altered_code.splitlines())
    assert (lines == 0)


def test_addvar_empty_module_should_not_work():
    example_cst = libcst.parse_module("")
    random.seed(1996)

    transformer = AddVariableTransformer()
    transformer.reset()

    altered_cst = visit_until_worked(example_cst, transformer)

    assert (not transformer.worked())


def test_addvar_working_example_should_set_transformer_to_worked():
    example_cst = example()
    random.seed(1996)

    transformer = AddVariableTransformer()
    transformer.reset()

    altered_cst = visit_until_worked(example_cst, transformer)

    assert (transformer.worked())


def test_example_should_have_2_lines():
    """
    Dummy Test to check that the \n is picked up correctly
    """
    example_cst = example()

    lines = len(example_cst.code.splitlines())
    assert (lines == 2)


def visit_until_worked(cst, transformer):
    """
    Small Helper as the re-try logic is in the Engine.
    Without this, it can simply happen that the transformer "does not fire" at the moment.
    But until I have figured something for this, I still want to have tests so I have this helper.
    :param cst:
    :param transformer:
    :return:
    """
    altered = transformer.apply(cst)
    return altered


def example():
    return libcst.parse_module("def hi(): \n\tprint(\"Hello World\")")
