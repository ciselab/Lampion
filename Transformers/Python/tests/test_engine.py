import libcst as cst
from libcst import CSTNode

from lampion.components.engine import Engine


def test_create_engine_with_empty_config():
    testobject = Engine({}, "PLACEHOLDER")
    assert testobject


def test_create_engine_with_none_config():
    testobject = Engine(None, "PLACEHOLDER_ENGINE_OUTPUT")
    assert testobject


def test_default_engine_has_transformers():
    testobject = Engine({}, "PLACEHOLDER")

    assert len(testobject.get_transformers()) > 0


def test_run_with_default_transformers_gives_output():
    testobject = Engine(None, "PLACEHOLDER_ENGINE_OUTPUT")

    example_cst = [example()]

    altered_cst = testobject.run(example_cst)[0]

    assert altered_cst


def test_run_with_default_transformers_input_remains_unchanged():
    testobject = Engine(None, "PLACEHOLDER_ENGINE_OUTPUT")

    example_cst = [example()]
    initial_value = str(example_cst[0][1].code)

    altered_csts = testobject.run(example_cst)

    testobject.run(example_cst)

    post_change_code = get_first_code(example_cst)

    assert initial_value == post_change_code


def test_run_with_default_transformers_output_different_to_input():
    testobject = Engine(None, "PLACEHOLDER_ENGINE_OUTPUT")

    example_cst = [example()]
    initial_value = str(example_cst[0][1].code)

    altered_csts = testobject.run(example_cst)
    altered_cst = altered_csts[0][1]

    assert altered_cst.code != initial_value


def test_run_with_default_transformers_with_two_CSTs_both_changed():
    testobject = Engine(None, "PLACEHOLDER_ENGINE_OUTPUT")

    example_cst_A = cst.parse_module("def hi(): \n\tprint(\"Hello World\")")
    example_cst_B = cst.parse_module("def bye(): \n\tprint(\"Goodbye (cruel) World\")")
    example_A_value = str(example_cst_A.code)
    example_B_value = str(example_cst_B.code)

    csts = [("PLACEHOLDER", example_cst_A), ("PLACEHOLDER", example_cst_B)]

    altered_csts = testobject.run(csts)

    altered_cst_A = get_first_code([x for x in altered_csts if "Hello" in x[1].code])
    altered_cst_B = get_first_code([x for x in altered_csts if "Goodbye" in x[1].code])

    assert altered_cst_A != example_A_value
    assert altered_cst_B != example_B_value


def test_run_with_default_transformers_with_two_CSTs_output_has_paths_to_compare():
    # This is a regression test, as when I wanted to print some of the changed code
    # It started to explode around my head
    testobject = Engine(None, "PLACEHOLDER_ENGINE_OUTPUT")

    example_cst_A = cst.parse_module("def hi(): \n\tprint(\"Hello World\")")
    example_cst_B = cst.parse_module("def bye(): \n\tprint(\"Goodbye (cruel) World\")")

    csts = [("PLACEHOLDER_A", example_cst_A), ("PLACEHOLDER_B", example_cst_B)]

    altered_csts = testobject.run(csts)

    assert len(altered_csts) == 2

    first_altered_cst = altered_csts[0]

    assert first_altered_cst[0]
    assert first_altered_cst[1]

    second_altered_cst = altered_csts[1]

    assert second_altered_cst[0]
    assert second_altered_cst[1]


def test_run_with_default_transformers_with_two_CSTs_both_inputs_stay_unchanged():
    testobject = Engine(None, "PLACEHOLDER_ENGINE_OUTPUT")

    example_cst_A = cst.parse_module("def hi(): \n\tprint(\"Hello World\")")
    example_cst_B = cst.parse_module("def bye(): \n\tprint(\"Goodbye (cruel) World\")")
    example_A_value = str(example_cst_A.code)
    example_B_value = str(example_cst_B.code)

    csts = [("PLACEHOLDER", example_cst_A), ("PLACEHOLDER", example_cst_B)]

    testobject.run(csts)

    assert example_cst_A.code == example_A_value
    assert example_cst_B.code == example_B_value


def test_with_one_file_after_transformation_path_is_the_same():
    testobject = Engine(None, "PLACEHOLDER_ENGINE_OUTPUT")

    example_cst = [example()]
    initial_path = str(example_cst[0][0])

    altered_csts = testobject.run(example_cst)
    post_change_path = altered_csts[0][0]

    assert post_change_path == initial_path


def test_run_with_two_csts_paths_match():
    testobject = Engine(None, "PLACEHOLDER_ENGINE_OUTPUT")

    example_cst_A = cst.parse_module("def hi(): \n\tprint(\"Hello World\")")
    example_cst_B = cst.parse_module("def bye(): \n\tprint(\"Goodbye (cruel) World\")")

    csts = [("PLACEHOLDER_A", example_cst_A), ("PLACEHOLDER_B", example_cst_B)]

    altered_csts = testobject.run(csts)

    altered_cst_A = [x for x in altered_csts if "Hello" in x[1].code]
    altered_cst_B = [x for x in altered_csts if "Goodbye" in x[1].code]

    altered_cst_path_A = altered_cst_A[0][0]
    altered_cst_path_B = altered_cst_B[0][0]
    assert altered_cst_path_A == "PLACEHOLDER_A"
    assert altered_cst_path_B == "PLACEHOLDER_B"


def test_run_with_two_csts_second_method_is_kept():
    testobject = Engine(None, "PLACEHOLDER_ENGINE_OUTPUT")

    example_cst_A = cst.parse_module("def hi(): \n\tprint(\"Hello World\")")
    example_cst_B = cst.parse_module("def bye(): \n\tprint(\"Goodbye (cruel) World\")")

    csts = [("PLACEHOLDER_A", example_cst_A), ("PLACEHOLDER_B", example_cst_B)]

    altered_csts = testobject.run(csts)

    altered_cst_B = [x for x in altered_csts if "Goodbye" in x[1].code]

    # If one of them is 0 and the other one is two,
    # then there was an issue in putting them back in the engines running asts
    assert len(altered_cst_B) == 1


def test_run_with_two_csts_first_method_is_kept():
    testobject = Engine(None, "PLACEHOLDER_ENGINE_OUTPUT")

    example_cst_A = cst.parse_module("def hi(): \n\tprint(\"Hello World\")")
    example_cst_B = cst.parse_module("def bye(): \n\tprint(\"Goodbye (cruel) World\")")

    csts = [("PLACEHOLDER_A", example_cst_A), ("PLACEHOLDER_B", example_cst_B)]

    altered_csts = testobject.run(csts)

    altered_cst_A = [x for x in altered_csts if "Hello" in x[1].code]

    # If one of them is 0 and the other one is two,
    # then there was an issue in putting them back in the engines running asts
    assert len(altered_cst_A) == 1


def test_run_with_two_csts_check_only_one_transformation_one_touched():
    testobject = Engine(config={"transformations": 1, "transformationscope": "global"})

    example_cst_A = cst.parse_module("def hi(): \n\tprint(\"Hello World\")")
    example_cst_B = cst.parse_module("def bye(): \n\tprint(\"Goodbye (cruel) World\")")

    csts = [("PLACEHOLDER_A", example_cst_A), ("PLACEHOLDER_B", example_cst_B)]

    testobject.run(csts)

    assert len(testobject.get_touched_paths()) == 1


def test_run_with_two_csts_check_many_transformations_both_touched():
    testobject = Engine(config={"transformations": 20, "transformationscope": "global"})

    example_cst_A = cst.parse_module("def hi(): \n\tprint(\"Hello World\")")
    example_cst_B = cst.parse_module("def bye(): \n\tprint(\"Goodbye (cruel) World\")")

    csts = [("PLACEHOLDER_A", example_cst_A), ("PLACEHOLDER_B", example_cst_B)]

    testobject.run(csts)

    assert len(testobject.get_touched_paths()) == 2


def test_run_with_two_csts_no_transformations_none_touched():
    testobject = Engine(config={"transformations": 0, "transformationscope": "global"})

    example_cst_A = cst.parse_module("def hi(): \n\tprint(\"Hello World\")")
    example_cst_B = cst.parse_module("def bye(): \n\tprint(\"Goodbye (cruel) World\")")

    csts = [("PLACEHOLDER_A", example_cst_A), ("PLACEHOLDER_B", example_cst_B)]

    testobject.run(csts)

    assert len(testobject.get_touched_paths()) == 0


def test_touched_engine_not_run_none_touched():
    testobject = Engine(config={"transformations": 0, "transformationscope": "global"})

    assert len(testobject.get_touched_paths()) == 0


def test_config_default_config_is_not_none_and_not_empty():
    testobject = Engine()

    assert testobject.get_config()
    assert len(testobject.get_config()) > 0


def test_config_empty_config_uses_default():
    testobject = Engine(config={})

    assert testobject.get_config()
    assert len(testobject.get_config()) > 0


def test_config_with_overwritten_seed_should_be_new_seed():
    overwrite_config = {"seed": 100}

    testobject = Engine(config=overwrite_config)
    received_seed = testobject.get_config()["seed"]

    assert received_seed == 100


def example() -> (str, CSTNode):
    return ("PATH_PLACEHOLDER", cst.parse_module("def hi(): \n\tprint(\"Hello World\")"))


def get_first_code(tuples: [(str, CSTNode)]) -> str:
    return tuples[0][1].code
