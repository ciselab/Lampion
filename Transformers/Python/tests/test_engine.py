import pytest

import libcst as cst

from lampion.components.engine import Engine


def test_create_engine_with_empty_config():
    testobject = Engine({}, "PLACEHOLDER")
    assert testobject


def test_create_engine_with_none_config():
    testobject = Engine(None, "PLACEHOLDER")
    assert testobject

def test_default_engine_has_transformers():
    testobject = Engine({}, "PLACEHOLDER")

    assert len(testobject.transformers) > 0


def test_run_with_default_transformers_gives_output():
    testobject = Engine(None, "PLACEHOLDER")

    example_cst = [example()]

    altered_cst = testobject.run(example_cst)[0]

    assert altered_cst


def test_run_with_default_transformers_input_remains_unchanged():
    testobject = Engine(None, "PLACEHOLDER")

    example_cst = example()
    old_code = str(example_cst.code)

    testobject.run([example_cst])

    new_code = str(example_cst.code)

    assert old_code == new_code


def test_run_with_default_transformers_output_different_to_input():
    testobject = Engine(None, "PLACEHOLDER")

    example_cst = [example()]
    initial_value = str(example_cst[0].code)

    altered_cst = testobject.run(example_cst)[0]

    assert altered_cst.code != initial_value


def test_run_with_default_transformers_with_two_CSTs_both_changed():
    testobject = Engine(None, "PLACEHOLDER")

    example_cst_A = cst.parse_module("def hi(): \n\tprint(\"Hello World\")")
    example_cst_B = cst.parse_module("def bye(): \n\tprint(\"Goodbye (cruel) World\")")
    example_A_value = str(example_cst_A.code)
    example_B_value = str(example_cst_B.code)

    csts = [example_cst_A, example_cst_B]

    altered_csts = testobject.run(csts)

    altered_cst_A = [x for x in altered_csts if "Hello" in x.code][0]
    altered_cst_B = [x for x in altered_csts if "Goodbye" in x.code][0]

    assert altered_cst_A.code != example_A_value
    assert altered_cst_B.code != example_B_value


def test_config_default_config_is_not_none_and_not_empty():
    testobject = Engine()

    assert testobject.config
    assert len(testobject.config) > 0


def test_config_empty_config_uses_default():
    testobject = Engine(config={})

    assert testobject.config
    assert len(testobject.config) > 0


def test_config_with_overwritten_seed_should_be_new_seed():
    overwrite_config = {"seed": 100}

    testobject = Engine(config=overwrite_config)
    received_seed = testobject.config["seed"]

    assert received_seed == 100


def example():
    return cst.parse_module("def hi(): \n\tprint(\"Hello World\")")
