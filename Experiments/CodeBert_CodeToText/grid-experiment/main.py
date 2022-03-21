import shutil

from jinja2 import Environment, FileSystemLoader  # For templating with jinja
import os  # For File/Directory Creation
import json  # For reading in the configurations
import argparse  # For handling a nice commandline interface


def run(
        grid_config_file: str,
        number_of_preprocessing_containers: int = 0,
        number_of_experiment_containers: int = 0,
        preprocessing_image: str = None,
        gpu_use: bool = True) -> None:
    """
    Primary Method of this file. It will
    1. Read the templates
    2. Read the grid-experiment-configs
    3. Create a set of derivative experiment-configs
    4.1 Sort Configs in Compose-Batches if configured
    4.2 Fill all Templates accordingly
    5. Print the filled templates to files

    The parameters "number_of_preprocessing_containers" and "number_of_experiment_containers"
    handle how many containers will be put in one compose template.
    - If set to 1, each compose will have one container.
    - If set to 3, each compose will have three containers running in parallel (except the last one, which might have 2 or 1).
    - If set to 0, all containers will be written in one compose.
    These numbers should be chosen (a) by your available hardware in strength and (b) by how much you can spread over different machines.

    :param grid_config_file: the path to a .json file containing the information how to do the grid experiment.
    :param number_of_experiment_containers: How many codebert containers will be started per compose.
        If 0, all are in one compose.
    :param number_of_preprocessing_containers: How many preprocesssing containers will be started per compose.
        If 0, all are in one compose.
    :param preprocessing_image: The image used in the preprocessing composes.
        Must have label attached, e.g.: "ciselab/lampion/codebert-java-preprocessing:1.2"
    :param gpu_use: Whether the experiment composes should have GPUs.
        If true (default), memory and CPU limitations will be ignored from grid_config_file.

    Note: The provided config.properties.j2 is based on the (more complex) java-transformer-properties.
    The Python Transformer just ignores the extra-values.
    Take a careful look at the first run of Python Preprocessing and inspect the logged statements.
    """
    file_loader = FileSystemLoader('templates')
    env = Environment(loader=file_loader)

    preprocessing_template = env.get_template('preprocessing-docker-compose.yaml.j2')
    config_template = env.get_template('config.properties.j2')
    experiment_template = env.get_template('experiment-docker-compose.yaml.j2')
    experiment_with_train_template = env.get_template('experiment-with-training-docker-compose.yaml.j2')

    with open(grid_config_file) as f:
        grid_configurations = json.load(f)

    configurations = []
    counter = 0

    transformer_combinations = grid_configurations['transformer_combinations']
    transformations = grid_configurations['transformations']
    seeds = grid_configurations['seeds']
    models = grid_configurations['models']

    lang:str = "python" if "python" in preprocessing_image else "java"
    experiment_container_version:str = "1.3"

    output_dir_grid_experiment = "experiment-setup"
    os.makedirs(output_dir_grid_experiment, exist_ok=True)

    explicitImports = grid_configurations["ExplicitImports"]

    for tcomb in transformer_combinations:
        for tn in transformations:
            for seed in seeds:
                for model in models:
                    config = {
                        "transformations": tn,
                        "seed": seed,
                        "run_number": counter,
                        "path": f"{output_dir_grid_experiment}/configs/config_{counter}",
                        "path_to": f"configs/config_{counter}",
                        "model_name": f"{model}",
                        "ExplicitImports": f"{explicitImports}".lower(),
                        "preprocessing_image": preprocessing_image,
                        "gpu_use": gpu_use,
                        "lang": lang
                    }
                    # Merge two dicts
                    config = {**config, **tcomb}
                    configurations.append(config)
                    counter = counter + 1

    print(f"Built {len(configurations)} configurations from {grid_config_file}")

    for config in configurations:
        os.makedirs(config['path'], exist_ok=True)
        config_file = open(os.path.join(config['path'], "config.properties"), "w")
        config_content = config_template.render(config)
        config_file.write(config_content)
        config_file.close()

    # Case one: There is no specified number of containers per shard, just write all in one file
    if (number_of_preprocessing_containers == 0):
        preprocessing_file = open(os.path.join(output_dir_grid_experiment, "preprocessing-docker-compose.yaml"), "w")
        preprocessing_content = preprocessing_template.render(configurations=configurations)
        preprocessing_file.write(preprocessing_content)
        preprocessing_file.close()
    # Case two: the user wanted atmost "number_of_preprocessing_containers" per compose-file
    # group the configs in smaller sub-configs and make a new template for them
    else:
        preproc_file = 1;
        low = 0;
        up = number_of_preprocessing_containers;
        while low < len(configurations):
            up = min(up, len(configurations))  # Set the upper bound to not count out of the configs
            sub_configurations = configurations[low:up];
            # Write the file
            preprocessing_file = open(
                os.path.join(output_dir_grid_experiment, f"preprocessing-docker-compose-part-{preproc_file}.yaml"), "w")
            preprocessing_content = preprocessing_template.render(configurations=sub_configurations)
            preprocessing_file.write(preprocessing_content)
            preprocessing_file.close()
            # Increase all the necessary counters
            low += number_of_preprocessing_containers
            up += number_of_preprocessing_containers
            preproc_file += 1
        print(
            f"Finished writing {preproc_file} preprocessing files with at most {number_of_preprocessing_containers} per file")

    if number_of_experiment_containers == 0:
        experiment_file = open(
            os.path.join(output_dir_grid_experiment, "experiment-docker-compose.yaml"), "w")
        experiment_content = experiment_template.render(
            configurations=configurations,
            batch_size=grid_configurations['batch_size'],
            mem_limit=grid_configurations['mem_limit'])
        experiment_file.write(experiment_content)
        experiment_file.close()
        experiment_with_train_file = open("experiment-with-training-docker-compose.yaml", "w")
        experiment_with_train_content = experiment_with_train_template.render(
            configurations=configurations,
            batch_size=grid_configurations['batch_size'],
            mem_limit=grid_configurations['mem_limit'])
        experiment_with_train_file.write(experiment_with_train_content)
        experiment_with_train_file.close()
    else:
        exp_file = 1;
        low = 0;
        up = number_of_experiment_containers;
        while low < len(configurations):
            up = min(up, len(configurations))  # Set the upper bound to not count out of the configs
            sub_configurations = configurations[low:up];
            # Write the exp-file
            experiment_file = open(
                os.path.join(output_dir_grid_experiment, f"experiment-docker-compose-part-{exp_file}.yaml"), "w")
            experiment_content = experiment_template.render(configurations=sub_configurations,
                                                            experiment_image_version=experiment_container_version)
            experiment_file.write(experiment_content)
            experiment_file.close()
            # Write the exp-file with training
            experiment_with_train_file = open(
                os.path.join(output_dir_grid_experiment,
                             f"experiment-with-training-docker-compose-part-{exp_file}.yaml"), "w")
            experiment_with_train_content = experiment_with_train_template.render(
                configurations=sub_configurations,
                batch_size=grid_configurations['batch_size'],
                mem_limit=grid_configurations['mem_limit'],
                experiment_image_version=experiment_container_version)
            experiment_with_train_file.write(experiment_with_train_content)
            experiment_with_train_file.close()

            # Increase all the necessary counters
            low += number_of_experiment_containers
            up += number_of_experiment_containers
            exp_file += 1

    # Last step: Copy helper files
    copy_other_files(target_dir=output_dir_grid_experiment)


def copy_other_files(target_dir: str) -> None:
    path_to_extractor_file = "./extractor.sh"
    path_to_replicator_file = "./replicator.sh"

    path_to_data_folder = "./ur_dataset"
    path_to_model_folder = "./model"

    if os.path.exists(path_to_extractor_file) and os.path.isfile(path_to_extractor_file):
        shutil.copyfile(path_to_extractor_file, os.path.join(target_dir, path_to_extractor_file))
    else:
        print("Did not find the extractor file nearby - not packaging it")

    if os.path.exists(path_to_replicator_file) and os.path.isfile(path_to_replicator_file):
        shutil.copyfile(path_to_replicator_file, os.path.join(target_dir, path_to_replicator_file))
    else:
        print("Did not find the replicator file nearby - not packaging it")

    if os.path.exists(path_to_data_folder) and os.path.isdir(path_to_data_folder):
        shutil.copytree(path_to_data_folder, os.path.join(target_dir, path_to_data_folder))
    else:
        print("Did not find the ur_data folder nearby - not packaging it")

    if os.path.exists(path_to_model_folder) and os.path.isdir(path_to_model_folder):
        shutil.copytree(path_to_model_folder, os.path.join(target_dir, path_to_model_folder))
    else:
        print("Did not find the model folder nearby - not packaging it")


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Creates the sub-configs and docker compose files for a Lampion-Grid '
                                                 'Experiment')
    parser.add_argument('configfile', metavar='cf', type=str, nargs=1,
                        help='The config file to create the grid experiment from')
    parser.add_argument('-preprocessing_image', nargs='?', type=str,
                        default="ciselab/lampion/codebert-java-preprocessing:latest",
                        help="Which preprocessing docker-image (and version) to use. Version must be specified. ")

    parser.add_argument('-np', type=int, nargs='?',
                        help='number of containers started per preprocessing-compose '
                             '(1 means one compose per container), '
                             '0 for "all in one file".', default=0)
    parser.add_argument('-ne', type=int, nargs='?',
                        help='number of containers started per experiment-compose (1 means one compose per container), '
                             '0 for "all in one file".', default=0)
    parser.add_argument('--use-gpus', dest='use_gpu', action='store_true')
    parser.add_argument('--use-cpus', dest='use_gpu', action='store_false')
    parser.set_defaults(use_gpu=False)

    args = parser.parse_args()

    print(args)

    run(args.configfile[0], number_of_experiment_containers=args.ne, number_of_preprocessing_containers=args.np,
        preprocessing_image=args.preprocessing_image,
        gpu_use=args.use_gpu)
