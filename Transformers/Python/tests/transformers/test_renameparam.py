import libcst

from lampion.transformers.renameparam import RenameParameterTransformer


def test_rename_param_method_has_no_params_should_stay_unchanged():
    example_cst = libcst.parse_module("def hi(): \n\ta: int = 1\n\tprint(\"Hello World\")")
    initial_code = str(example_cst.code)

    transformer = RenameParameterTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    altered_code = altered_cst.code
    assert initial_code == altered_code

def test_rename_param_method_has_no_params_transformer_did_not_work():
    example_cst = libcst.parse_module("def hi(): \n\ta: int = 1\n\tprint(\"Hello World\")")

    transformer = RenameParameterTransformer()
    transformer.reset()

    transformer.apply(example_cst)

    assert not transformer.worked()

def test_rename_param_method_has_one_params_should_change():
    example_cst = libcst.parse_module("def hi(yyy): \n\ta: int = 1\n\tprint(f\"Hello {yyy}\")")
    initial_code = str(example_cst.code)

    transformer = RenameParameterTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    altered_code = altered_cst.code
    assert initial_code != altered_code


def test_rename_param_method_has_one_params_transformer_should_have_worked():
    example_cst = libcst.parse_module("def hi(yyy): \n\ta: int = 1\n\tprint(f\"Hello {yyy}\")")

    transformer = RenameParameterTransformer()
    transformer.reset()

    transformer.apply(example_cst)

    assert transformer.worked()


def test_rename_param_method_has_one_params_transformer_can_be_applied_any_time():
    example_cst = libcst.parse_module("def hi(yyy): \n\ta: int = 1\n\tprint(f\"Hello {yyy}\")")

    transformer = RenameParameterTransformer()

    altered_cst = example_cst

    for i in range(0,10):
        transformer.reset()
        altered_cst = transformer.apply(altered_cst)

    assert transformer.worked()

def test_rename_param_method_has_one_params_param_should_be_renamed():
    example_cst = libcst.parse_module("def hi(yyy): \n\ta: int = 1\n\tprint(f\"Hello {yyy}\")")

    transformer = RenameParameterTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    altered_code = altered_cst.code
    assert "yyy" not in altered_code



def test_rename_param_method_has_one_typed_param_should_be_renamed():
    example_cst = libcst.parse_module("def hi(yyy: str): \n\ta: int = 1\n\tprint(f\"Hello {yyy}\")")

    transformer = RenameParameterTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    altered_code = altered_cst.code
    assert "yyy" not in altered_code


def test_rename_param_method_has_one_typed_param_should_keep_type():
    example_cst = libcst.parse_module("def hi(yyy: str): \n\ta: int = 1\n\tprint(f\"Hello {yyy}\")")

    transformer = RenameParameterTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    altered_code = altered_cst.code
    assert "str" in altered_code


def test_rename_param_method_has_one_defaulted_param_should_be_renamed():
    example_cst = libcst.parse_module("def hi(yyy = \"World\"): \n\ta: int = 1\n\tprint(f\"Hello {yyy}\")")

    transformer = RenameParameterTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    altered_code = altered_cst.code
    assert "yyy" not in altered_code

def test_rename_param_method_has_one_defaulted_param_should_keep_default():
    example_cst = libcst.parse_module("def hi(yyy = \"World\"): \n\ta: int = 1\n\tprint(f\"Hello {yyy}\")")

    transformer = RenameParameterTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    altered_code = altered_cst.code
    assert "World" in altered_code

def test_rename_param_method_has_one_typed_defaulted_param_should_be_renamed():
    example_cst = libcst.parse_module("def hi(yyy: str = \"World\"): \n\ta: int = 1\n\tprint(f\"Hello {yyy}\")")

    transformer = RenameParameterTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    altered_code = altered_cst.code
    assert "yyy" not in altered_code

def test_rename_param_method_has_one_typed_defaulted_param_should_keep_default():
    example_cst = libcst.parse_module("def hi(yyy: str = \"World\"): \n\ta: int = 1\n\tprint(f\"Hello {yyy}\")")

    transformer = RenameParameterTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    altered_code = altered_cst.code
    assert "World" in altered_code

def test_rename_param_method_has_one_typed_defaulted_param_should_keep_type():
    example_cst = libcst.parse_module("def hi(yyy: str = \"World\"): \n\ta: int = 1\n\tprint(f\"Hello {yyy}\")")

    transformer = RenameParameterTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    altered_code = altered_cst.code
    assert "str" in altered_code

def test_rename_param_method_has_two_parameters_one_should_be_renamed():
    example_cst = libcst.parse_module("def hi(yyy,xxx): \n\ta: int = 1\n\tprint(f\"Hello {yyy} {xxx}\")")

    transformer = RenameParameterTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    altered_code = altered_cst.code
    yyy_in =  "yyy" in altered_code
    xxx_in = "xxx" in altered_code
    # the != works as a logical XOR in this case
    assert yyy_in != xxx_in

def test_rename_param_method_has_two_parameters_apply_often_should_both_rename():
    example_cst = libcst.parse_module("def hi(yyy,xxx): \n\ta: int = 1\n\tprint(f\"Hello {yyy} {xxx}\")")

    transformer = RenameParameterTransformer()
    altered_cst = example_cst
    for i in range(0,50):
        transformer.reset()
        altered_cst = transformer.apply(altered_cst)

    altered_code = altered_cst.code
    yyy_in =  "yyy" in altered_code
    xxx_in = "xxx" in altered_code
    # the != works as a logical XOR in this case
    assert (not yyy_in) and (not xxx_in)

def test_add_var_method_has_one_param_that_exists_in_str_should_be_kept_in_str():
    example_cst = libcst.parse_module("def hi(name = 1): \n\tprint(f\"name is {name}\")")

    transformer = RenameParameterTransformer()
    transformer.reset()

    altered_cst = transformer.apply(example_cst)

    altered_code = altered_cst.code
    assert "name" in altered_code
    assert "name = 1" not in altered_code