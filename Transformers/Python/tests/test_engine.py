import pytest

import libcst as cst
import random as random

from lampion.components.engine import Engine


def test_create_engine_with_empty_config():
    testobject = Engine({}, "PLACEHOLDER")


def test_create_engine_with_none_config():
    testobject = Engine(None, "PLACEHOLDER")


def test_default_engine_has_transformers():
    testobject = Engine({}, "PLACEHOLDER")

    assert len(testobject.transformers) > 0



def test_run_with_default_transformers_gives_output():
    testobject = Engine(None,"PLACEHOLDER")

    example_cst = [example()]

    altered_cst = testobject.run(example_cst)[0]

    assert altered_cst

def test_run_with_default_transformers_output_different_to_input():
    testobject = Engine(None,"PLACEHOLDER")

    example_cst = [example()]
    initial_value = str(example_cst[0].code)

    altered_cst = testobject.run(example_cst)[0]

    #print(altered_cst.code)
    #print("GOES TOO ...")
    #print(example_cst[0].code)

    assert altered_cst.code != initial_value

def test_run_with_default_transformers_with_two_CSTs_both_changed():
    testobject = Engine(None,"PLACEHOLDER")

    example_cst_A = cst.parse_module("def hi(): \n\tprint(\"Hello World\")")
    example_cst_B = cst.parse_module("def bye(): \n\tprint(\"Goodbye (cruel) World\")")
    example_A_value = str(example_cst_A.code)
    example_B_value = str(example_cst_B.code)

    csts = [example_cst_A,example_cst_B]

    altered_csts = testobject.run(csts)

    altered_cst_A = [x for x in altered_csts if "Hello" in x.code][0]
    altered_cst_B = [x for x in altered_csts if "Goodbye" in x.code][0]

    # This Test needs a seed, as sometimes it can happen that all 10 transformations are at one cst
    random.seed(15)

    #print(altered_cst_B.code)
    #print(altered_cst_A.code)

    assert altered_cst_A.code != example_A_value
    assert altered_cst_B.code != example_B_value



def example():
    return cst.parse_module("def hi(): \n\tprint(\"Hello World\")")
