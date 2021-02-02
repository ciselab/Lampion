from jinja2 import Environment, FileSystemLoader    # For templating with jinja
import os                                           # For File/Directory Creation
import json                                         # For reading in the configurations
import argparse                                     # For handling a nice commandline interface

def run(grid_config_file, number_of_preprocessing_containers=0, number_of_experiment_containers=0):
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

    writeManifest=grid_configurations["WriteManifest"],
    explicitImports=grid_configurations["ExplicitImports"]

    for tcomb in transformer_combinations:
        for tn in transformations:
            for seed in seeds:
                for model in models:
                    config = {
                        "transformations" : tn,
                        "seed":seed,
                        "run_number": counter,
                        "path": f"configs/config_{counter}",
                        "path_to": f"configs/config_{counter}",
                        "model_name": f"{model}",
                        "WriteManifest": f"{writeManifest}".lower(),
                        "ExplicitImports": f"{explicitImports}".lower()
                    }
                    # Merge two dicts
                    config = {**config,**tcomb}
                    configurations.append(config)
                    counter = counter + 1

    print(f"Built {len(configurations)} configurations from {grid_config_file}")

    for config in configurations:
        os.makedirs(config['path'],exist_ok=True)
        config_file = open(config['path']+"/config.properties","w")
        config_content = config_template.render(config)
        config_file.write(config_content)
        config_file.close()

    # Case one: There is no specified number of containers per shard, just write all in one file
    if(number_of_preprocessing_containers==0):
        preprocessing_file = open("preprocessing-docker-compose.yaml","w")
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
            up = min(up,len(configurations))        # Set the upper bound to not count out of the configs
            sub_configurations=configurations[low:up];
            # Write the file
            preprocessing_file = open(f"preprocessing-docker-compose-part-{preproc_file}.yaml","w")
            preprocessing_content = preprocessing_template.render(configurations=sub_configurations)
            preprocessing_file.write(preprocessing_content)
            preprocessing_file.close()
            # Increase all the necessary counters
            low += number_of_preprocessing_containers
            up += number_of_preprocessing_containers
            preproc_file += 1
        print(f"Finished writing {preproc_file} preprocessing files with atmost {number_of_preprocessing_containers} per file")

    if number_of_experiment_containers==0:
        experiment_file = open("experiment-docker-compose.yaml","w")
        experiment_content = experiment_template.render(
            configurations=configurations,
            batch_size=grid_configurations['batch_size'],
            mem_limit=grid_configurations['mem_limit'])
        experiment_file.write(experiment_content)
        experiment_file.close()
        experiment_with_train_file = open("experiment-with-training-docker-compose.yaml","w")
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
            experiment_file = open(f"experiment-docker-compose-part-{exp_file}.yaml", "w")
            experiment_content = experiment_template.render(configurations=sub_configurations)
            experiment_file.write(experiment_content)
            experiment_file.close()
            # Write the exp-file with training
            experiment_with_train_file = open(f"experiment-with-training-docker-compose-part-{exp_file}.yaml", "w")
            experiment_with_train_content = experiment_with_train_template.render(
                configurations=sub_configurations,
                batch_size=grid_configurations['batch_size'],
                mem_limit=grid_configurations['mem_limit'])
            experiment_with_train_file.write(experiment_with_train_content)
            experiment_with_train_file.close()

            # Increase all the necessary counters
            low += number_of_experiment_containers
            up += number_of_experiment_containers
            exp_file += 1

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Creates the sub-configs and docker compose files for a Lampion-Grid Experiment')
    parser.add_argument('configfile', metavar='cf', type=str, nargs=1,
                        help='The config file to create the grid experiment from')
    parser.add_argument('-np', type=int, nargs='?',
                        help='number of containers started per preprocessing-compose (1 means one compose per container), '
                             '0 for "all in one file".',default=0)
    parser.add_argument('-ne', type=int, nargs='?',
                        help='number of containers started per experiment-compose (1 means one compose per container), '
                             '0 for "all in one file".', default=0)


    args = parser.parse_args()

    print(args)

    run(args.configfile[0],number_of_experiment_containers=args.ne,number_of_preprocessing_containers=args.np)
