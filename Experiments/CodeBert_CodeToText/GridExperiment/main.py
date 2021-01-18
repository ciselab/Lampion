from jinja2 import Environment, FileSystemLoader    # For templating with jinja
import os                                           # For File/Directory Creation
import sys                                          # For handling command args
import json                                         # For reading in the configurations


def run(grid_config_file):
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
                        "model_name": f"{model}"
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

    preprocessing_file = open("preprocessing-docker-compose.yaml","w")
    preprocessing_content = preprocessing_template.render(configurations=configurations)
    preprocessing_file.write(preprocessing_content)
    preprocessing_file.close()

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

if __name__ == '__main__':
    if(len(sys.argv)==2):
        run(sys.argv[1])
    else:
        run('grid_configuration.json')
