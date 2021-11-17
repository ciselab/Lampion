import lampion.cli

import pytest

# This prefix works for running pytest on project root
path_prefix: str = "./tests"


# You may want to change it to "./" to run the tests in the IDE.

def test_read_input_files_on_folder_should_give_two_csts():
    csts = lampion.cli.read_input_dir(f"{path_prefix}/test_inputs/multiple_files")

    assert len(csts) == 2


def test_read_input_files_on_file_should_give_one_cst():
    csts = lampion.cli.read_input_dir(f"{path_prefix}/test_inputs/hello_world.py")

    assert len(csts) == 1


def test_read_input_files_on_empty_dir_should_give_no_cst():
    csts = lampion.cli.read_input_dir(f"{path_prefix}/test_inputs/no_files")

    assert len(csts) == 0


def test_read_input_files_on_bad_path_should_raise_value_error():
    with pytest.raises(ValueError):
        lampion.cli.read_input_dir("./made_up_folder")


def test_read_input_files_on_bad_path_should_raise_value_error():
    with pytest.raises(ValueError):
        lampion.cli.read_input_dir("./made_up_file.py")


def test_main_with_good_file_should_not_fail():
    lampion.cli.dry_run(f"{path_prefix}/test_inputs/hello_world.py")


def test_main_with_good_folder_should_not_fail():
    lampion.cli.dry_run(f"{path_prefix}/test_inputs/multiple_files")


def test_main_with_bad_folder_should_fail():
    with pytest.raises(ValueError):
        lampion.cli.dry_run("./made_up_folder")


def test_main_with_bad_file_should_fail():
    with pytest.raises(ValueError):
        lampion.cli.dry_run("./made_up_file.py")
