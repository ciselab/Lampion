import os.path

import jinja2 as jinja
import json


def run(grid_config_file: str = "./grid_configuration_tiny.json", output_folder:str = "./grid_augmentation"):
    file_loader = jinja.FileSystemLoader('templates')
    env = jinja.Environment(loader=file_loader)

    augment_template = env.get_template('augment-docker-compose.yaml.j2')
    config_template = env.get_template('grid_experiment.properties.j2')
    preprocess_template = env.get_template('preprocess-docker-compose.yaml.j2')

    with open(grid_config_file) as f:
        grid_configurations = json.load(f)

    data_paths = [("train","./data/train"),("test","./data/test"),("valid","./data/valid")]
    properties_folder = "./configs"

    encoding_name="python_bpe_10000"

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
                        "config_name": f"config_{counter}.properties",
                        "config_file": f"/config/config_{counter}.properties",
                        "dataset": data_name,
                        "id": counter,
                        "resulting_filename": f"lampion_{data_name}_{counter}",
                        "properties_folder": properties_folder,
                        "scope": "per_class",
                        "augmentation_output_path": f"./augmentation_output/{counter}/{data_name}",
                        "augmentation_data_input_path" : data_path,
                        "path_to_encoding": f"{os.path.join(output_folder,'encodings',encoding_name)}"
                    }
                    # Merge two dicts
                    config = {**config, **tcomb}
                    configurations.append(config)
                counter = counter + 1

    os.makedirs(output_folder,exist_ok=True)
    os.makedirs(os.path.join(output_folder,"configs"),exist_ok=True)
    for conf in configurations:
        print(conf)
        augment_file = open(os.path.join(output_folder,f"augment-{conf['dataset']}-{conf['id']}-docker-compose.yaml"),mode="w")
        augment_content = augment_template.render(**conf)
        augment_file.write(augment_content)
        augment_file.close()

        config_file = open(os.path.join(output_folder,"configs",conf["config_name"]),mode="w")
        config_content = config_template.render(**conf)
        config_file.write(config_content)
        config_file.close()

        preprocess_file = open(os.path.join(output_folder, f"preprocess-{conf['dataset']}-{conf['id']}-docker-compose.yaml"),
                            mode="w")
        preprocess_content = preprocess_template.render(**conf)
        preprocess_file.write(preprocess_content)
        preprocess_file.close()

if __name__ == '__main__':
    print("Hello World")
    run()
