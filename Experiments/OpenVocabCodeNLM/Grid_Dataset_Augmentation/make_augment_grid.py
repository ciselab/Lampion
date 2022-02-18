import jinja2 as jinja
import json


def run(grid_config_file: str = "./grid_configuration_tiny.json"):
    file_loader = jinja.FileSystemLoader('templates')
    env = jinja.Environment(loader=file_loader)

    augment_template = env.get_template('augment-docker-compose.yaml.j2')
    config_template = env.get_template('grid_experiment.properties.j2')
    preprocess = env.get_template('preprocess-docker-compose.yaml.j2')

    with open(grid_config_file) as f:
        grid_configurations = json.load(f)

    data_paths = [("train","./data/train"),("test","./data/test"),("valid","./data/valid")]
    properties_folder = "./properties"


    configurations = []
    counter = 0

    transformer_combinations = grid_configurations['transformer_combinations']
    transformations = grid_configurations['transformations']
    seeds = grid_configurations['seeds']

    for tcomb in transformer_combinations:
        for tn in transformations:
            for seed in seeds:
                for (data_name,data_path) in data_paths:
                    config = {
                        "transformations": tn,
                        "seed": seed,
                        "run_number": counter,
                        "path": f"configs/config_{counter}",
                        "path_to": f"configs/config_{counter}",
                        "config_file": f"configs/config_{counter}",
                        "dataset": data_name,
                        "id": counter,
                        "resulting_filename": f"lampion_{data_name}_{counter}",
                        "properties_folder": properties_folder,
                        "input_files": data_path,
                        "scope": "per_class",
                        "augmentation_output_path": f"/output/{counter}/{data_name}"
                    }
                    # Merge two dicts
                    config = {**config, **tcomb}
                    configurations.append(config)
                counter = counter + 1

    print(configurations)


if __name__ == '__main__':
    print("Hello World")
    run()
