import os

import libcst

import lampion.cli

import pytest

# This prefix works for running pytest on project root
path_prefix: str = "./tests"


# You may want to change it to "./" to run the tests in the IDE.

def test_read_input_files_on_folder_should_give_two_csts():
    csts = lampion.cli.read_input_dir(f"{path_prefix}/test_inputs/multiple_files")

    assert len(csts) == 2


def test_read_input_files_on_buggy_folder_should_give_no_csts():
    # The read_input should not find valid files, but should also not fail.
    # A "graceful" empty dir is expected
    csts = lampion.cli.read_input_dir(f"{path_prefix}/test_inputs/buggy_files")

    assert len(csts) == 0


def test_read_input_files_on_buggy_and_ok_folder_should_give_two_csts():
    # The buggy files should be ignored, while the ok files should be read in.
    # This means 2 buggy, 2 ok.
    csts = lampion.cli.read_input_dir(f"{path_prefix}/test_inputs/buggy_and_ok_files")

    assert len(csts) == 2


def test_read_input_file_on_buggy_file_should_throw_error():
    with pytest.raises(libcst._exceptions.ParserSyntaxError):
        lampion.cli.read_input_dir(f"{path_prefix}/test_inputs/buggy_files/faulty_py3.py")


def test_read_input_file_on_python2_file_should_throw_error():
    with pytest.raises(libcst._exceptions.ParserSyntaxError):
        lampion.cli.read_input_dir(f"{path_prefix}/test_inputs/buggy_files/python2.py")


def test_read_input_files_on_file_should_give_one_cst():
    csts = lampion.cli.read_input_dir(f"{path_prefix}/test_inputs/hello_world.py")

    assert len(csts) == 1


def test_read_input_files_on_empty_dir_should_not_give_value_error():
    csts = lampion.cli.read_input_dir(f"{path_prefix}/test_inputs/no_files")
    assert len(csts) == 0


def test_read_input_files_on_bad_path_should_raise_value_error():
    with pytest.raises(ValueError):
        lampion.cli.read_input_dir("./made_up_folder")


def test_read_input_files_on_bad_path_should_raise_FileNotFoundError():
    with pytest.raises(ValueError):
        lampion.cli.read_input_dir("./made_up_file.py")


def test_main_with_good_file_should_not_fail():
    lampion.cli.run(f"{path_prefix}/test_inputs/hello_world.py")


def test_main_with_good_folder_should_not_fail():
    lampion.cli.run(f"{path_prefix}/test_inputs/multiple_files")


def test_main_with_bad_folder_should_fail():
    with pytest.raises(ValueError):
        lampion.cli.run("./made_up_folder")


def test_main_with_bad_file_should_fail():
    with pytest.raises(ValueError):
        lampion.cli.run("./made_up_file.py")


def test_read_config_bad_path_raises_valueError():
    with pytest.raises(ValueError):
        lampion.cli.read_config_file("./made_up_folder")


def test_read_config_with_none_value_gives_empty_dict():
    config = lampion.cli.read_config_file(None)
    assert len(config) == 0


def test_read_config_with_empty_files_gives_empty_dict():
    config = lampion.cli.read_config_file(f"{path_prefix}/test_configs/empty.properties")
    assert len(config) == 0


def test_read_config_with_example_file_finds_values():
    config = lampion.cli.read_config_file(f"{path_prefix}/test_configs/test1.properties")

    assert config is not {}


def test_read_config_with_wrong_ending_finds_values():
    config = lampion.cli.read_config_file(f"{path_prefix}/test_configs/test3.txt")

    assert config is not {}


def test_read_config_parses_bools_properly():
    config = lampion.cli.read_config_file(f"{path_prefix}/test_configs/parse_booleans.properties")

    value_a = config["a"]
    value_b = config["b"]
    value_c = config["c"]
    value_d = config["d"]

    assert value_a == True
    assert value_b == True
    assert value_c == False
    assert value_d == False


def test_read_config_parses_bools_to_types():
    config = lampion.cli.read_config_file(f"{path_prefix}/test_configs/parse_booleans.properties")

    value_a = config["a"]
    value_b = config["b"]
    value_c = config["c"]
    value_d = config["d"]

    assert isinstance(value_a, bool)
    assert isinstance(value_b, bool)
    assert isinstance(value_c, bool)
    assert isinstance(value_d, bool)


def test_read_config_does_not_parse_doubles():
    config = lampion.cli.read_config_file(f"{path_prefix}/test_configs/parse_doubles.properties")

    value_a = config["a"]
    value_b = config["b"]
    value_c = config["c"]

    assert value_a == "1.5"
    assert value_b == "2,6"
    assert value_c == "0.1"
    assert isinstance(value_a, str)
    assert isinstance(value_b, str)
    assert isinstance(value_c, str)


def test_read_config_parses_ints_properly():
    config = lampion.cli.read_config_file(f"{path_prefix}/test_configs/parse_ints.properties")

    value_a = config["a"]
    value_transformations = config["transformations"]
    assert value_a == 50
    assert value_transformations == 10


def test_read_config_with_example_file_finds_values():
    config = lampion.cli.read_config_file(f"{path_prefix}/test_configs/test1.properties")

    assert config is not {}


def test_read_config_with_example_file_2_has_seed():
    config = lampion.cli.read_config_file(f"{path_prefix}/test_configs/test2.properties")

    assert config["seed"] is not None
    assert config["seed"] == 123


def test_read_config_with_example_file_1_has_transformers_as_expected():
    config = lampion.cli.read_config_file(f"{path_prefix}/test_configs/test1.properties")

    assert config["AddUnusedVariableTransformer"] == True
    assert config["UnusedVariableStringRandomness"] == "full"

    assert config["AddCommentTransformer"] == False
    assert config["AddCommentStringRandomness"] == "pseudo"


def test_read_config_with_example_file_2_has_transformers_as_expected():
    config = lampion.cli.read_config_file(f"{path_prefix}/test_configs/test2.properties")

    assert config["AddUnusedVariableTransformer"] == False
    assert config["UnusedVariableStringRandomness"] == "full"

    assert config["AddCommentTransformer"] == False


def test_read_config_two_different_files_have_different_dicts():
    config1 = lampion.cli.read_config_file(f"{path_prefix}/test_configs/test1.properties")
    config2 = lampion.cli.read_config_file(f"{path_prefix}/test_configs/test2.properties")

    assert config1 != config2


def test_read_config_with_lots_of_whitespace_should_be_parsed_without_any_whitespaces():
    config = lampion.cli.read_config_file(f"{path_prefix}/test_configs/test3.properties")

    assert config["AddUnusedVariableTransformer"] == False
    assert config["UnusedVariableStringRandomness"] == "full"

    assert config["AddCommentTransformer"] == False
    assert config["seed"] == 123
    assert config["transformation_scope"] == "global"
