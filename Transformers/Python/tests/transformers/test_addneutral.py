import libcst

from lampion.transformers.addneutral import AddNeutralElementTransformer


def test_addneutral_for_float_should_add_plus_0():
    example_cst = libcst.parse_module("def some(): \n\ta = 0.5 \n\treturn a")

    transformer = AddNeutralElementTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert "(0.5+0.0)" in altered_code


def test_addneutral_apply_twice_for_float_should_add_plus_0():
    example_cst = libcst.parse_module("def some(): \n\ta = 0.5 \n\treturn a")

    transformer = AddNeutralElementTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    transformer.reset()

    altered_cst = transformer.apply(altered_cst)
    altered_code = altered_cst.code

    assert altered_code.count("0.0") == 2

def test_addneutral_apply_to_method_with_two_floats_should_be_applied_once_only():
    example_cst = libcst.parse_module("def some(): \n\ta = 0.5\n\tb=0.3 \n\treturn a+b")

    transformer = AddNeutralElementTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert "0.0" in altered_code
    assert altered_code.count("0.0") == 1


def test_addneutral_apply_to_method_with_two_identical_floats_should_be_applied_is_applied_at_first_instance():
    example_cst = libcst.parse_module("def some(): \n\ta = 0.5\n\tb=0.5 \n\treturn a+b")

    transformer = AddNeutralElementTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert "0.0" in altered_code
    assert altered_code.count("0.0") == 1

def test_addneutral_apply_twice_for_float_should_work():
    example_cst = libcst.parse_module("def some(): \n\ta = 0.5 \n\treturn a")

    transformer = AddNeutralElementTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    transformer.reset()
    altered_cst = transformer.apply(altered_cst)
    altered_code = altered_cst.code

    assert transformer.worked()


def test_addneutral_for_float_in_return_should_add_plus_0():
    example_cst = libcst.parse_module("def some():\n\treturn 0.5")

    transformer = AddNeutralElementTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert "(0.5+0.0)" in altered_code



def test_addneutral_for_float_in_default_parameters_should_add_plus_0():
    example_cst = libcst.parse_module("def some(a: float = 0.5):\n\treturn a")

    transformer = AddNeutralElementTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert "(0.5+0.0)" in altered_code
    assert "0.0" in altered_code


def test_addneutral_for_float_in_default_parameters_should_work():
    example_cst = libcst.parse_module("def some(a: float = 0.5):\n\treturn a")

    transformer = AddNeutralElementTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert transformer.worked()

def test_addneutral_for_float_should_have_worked():
    example_cst = libcst.parse_module("def some(): \n\ta = 0.5 \n\treturn a")

    transformer = AddNeutralElementTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert transformer.worked()


def test_addneutral_method_has_no_literals_transformer_did_not_work():
    example_cst = libcst.parse_module("def some(): return Math.Pi")

    transformer = AddNeutralElementTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = altered_cst.code

    assert not transformer.worked()

def test_addneutral_method_has_no_literals_code_did_not_change():
    example_cst = libcst.parse_module("def some(): return Math.Pi")
    initial_code = str(example_cst.code)

    transformer = AddNeutralElementTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)
    altered_code = str(altered_cst.code)

    assert initial_code == altered_code
