import argparse
import os
import shutil
import logging
import sys


def encode_files(path_to_raw:str,path_to_encoding:str,output_folder:str,initial_file_ending:str=".py",file_ending:str="py_enc") -> None:
    '''
    This method applies the subword-nmt to any file in a directory.
    A copy of the directory with the encoded files will be build under output_folder.
    If the output folder already exist, the files will be deleted and created anew.
    Any structure of files and directories will be mirrored in the output_folder.

    This method calls elements from the console (e.g. the subword-nmt).
    Hence, the console MUST support the subword-nmt command.

    :param path_to_raw: where to find the .py files to be encoded.
    :param path_to_encoding: where to find the encoding file.
    :param output_folder: The folder where to write the encoded files to. Will be created if not existent.
    :param initial_file_ending: the postfix which files should be encoded. Default: ".py".
    :param file_ending: the new postfix for the encoded files. Usually the filename will be kept, e.g. `A.py` -> `A.py_enc`
    :return: None, as side-effect the new encoded items will be created.
    '''

    logging.info(f"Starting Encoding of files in {path_to_raw} with {path_to_encoding}, "
                 f"creating {file_ending}-files in {output_folder}")
    assert os.path.exists(path_to_raw) and os.path.isdir(path_to_raw)
    assert os.path.exists(path_to_encoding) and os.path.isfile(path_to_encoding)

    counter: int = 0

    if not os.path.exists(output_folder):
        os.makedirs(output_folder,exist_ok=False)
    else:
        logging.debug("Found existing output folder")
        try:
            shutil.rmtree(output_folder)
        except IOError:
            logging.warning("Could not wipe %s ! Files will be overwritten, but if data changed you will create 'Phantoms'!",output_folder)

    for dirpath, dnames, fnames in os.walk(path_to_raw):
        for f in fnames:
            if f.endswith(initial_file_ending):
                full_path = os.path.join(dirpath,f)
                # Remove the last 3 characters, exactly ".py", then add our (custom) postfix
                encoded_f = f[:-3] + "." + file_ending
                # If both paths to join are absolute, there are issues joining them.
                # This was the case within docker, as /data and /output are both absolute.
                adjusted_dirpath = dirpath[1:] if dirpath.startswith("/") else dirpath
                encoded_file_dir = os.path.join(output_folder,adjusted_dirpath)
                encoded_file_path = os.path.join(encoded_file_dir,encoded_f)
                # Make necessary folders
                os.makedirs(encoded_file_dir,exist_ok=True)
                # Run the subword-nmt Command
                logging.debug(f"Encoding {full_path} to {encoded_file_path} ... ")
                os.system(f"subword-nmt apply-bpe -c {path_to_encoding} < {full_path} > {encoded_file_path}")
                counter += 1

    logging.info(f"Finished encoding of files, {counter} files in total converted.")


def merge_encoded_files(path_to_encoded_files:str,output_file:str,file_ending:str=".py_enc") -> None:
    '''
    Iterates over all items in the directory path_to_encoded_files
    that end with file_ending, and write them to a single file called output_file.

    The resulting file matches the format needed vor the OpenVocabCodeNLM Training and Testing.
    That means, all whitespace is removed and the (encoded) tokens
    of one file are written into one line, separated by one space.

    If output_file exists, it will be overwritten (NOT Appended!)

    :param path_to_encoded_files: A folder containing the encoded files
    :param output_file: The file to write to, will be overwritten if existent
    :param file_ending: the file ending of files that will be included in the accumulated file. All other file endings are ignored.
    :return: None, as side-effect a summarized encoding file will be created.
    '''

    logging.info(f"Starting to merge encoded files in {path_to_encoded_files} into {output_file}")

    assert os.path.exists(path_to_encoded_files) and os.path.isdir(path_to_encoded_files)

    counter: int = 0

    with open(output_file,mode="w") as output_f:
        # While the merged file is open, iterate over all input files
        for dirpath, dnames, fnames in os.walk(path_to_encoded_files):
            for f in fnames:
                if f.endswith(file_ending):
                    full_path = os.path.join(dirpath, f)
                    # For every input file, read the lines, remove additional whitespace, add them to the merged file
                    with open(full_path,mode="r") as encoded_file:
                        for l in encoded_file.readlines():
                            output_f.write(l.strip())
                        counter += 1
                    # After adding all file content, write a new line to start next entry
                    output_f.write("\n")

    logging.info(f"Finished merging of files, {counter} files in total merged.")


def main() -> None:
    '''
    Main Method of the preparation.
    Orchestrates:
    1. Setup & ArgParsing
    2. Runs Encoding of Files
    3. Runs merging of files

    The input files remain untouched, altered copies and artifacts will be created where specified.
    As this file is intended to be run in docker, the default parameters point to rootlevel folders (/data,/output).
    Be careful if you run this on your real machine outside of docker.

    For information on required arguments, please run 'python prep.py -h'.
    :return: None.
    '''
    parser = argparse.ArgumentParser(
        description='Applies metamorphic transformations to Python Code '
                    'in Order to make it verbose & different but functionally identical'
    )
    parser.add_argument('input_folder', metavar='input_folder', type=str, nargs=1, default="/data",
                        help='A path to folder containing .py files to be encoded')

    parser.add_argument('output_folder',metavar='output_folder', type=str, nargs=1, default="/output",
                        help="Prefix for the folder to place output in. "
                             "Within this new folder, the initial structure will be replicated. "
                             "Any files will be overwritten.")

    parser.add_argument('encoding_path',metavar='encoding_path', type=str, nargs=1, default="/encodings/python_encoding.enc_bpe_10000",
                        help="The path at which to find the encoding file. Be careful as different languages need different encodings!")

    parser.add_argument('merged_filename',metavar='merged_filename', type=str, nargs=1, default="selfmade_pre_enc_10000",
                        help="The name of the merged encoding file usable for the OpenVocabCodeNLM Experiments. Will be placed in 'output_folder'.")

    parser.add_argument('file_ending',metavar='file_ending',type=str,nargs=1,default=".py",
                        help="Which files in 'input_folder' should be encoded? Based on string.endswith(file_ending). Default: '.py'")

    parser.add_argument('encoded_file_ending',metavar='encoded_file_ending',type=str,nargs=1,default=".py_enc",
                        help="Specifies the filetype of the created encoded files. Default: '.py_enc'")

    parser.add_argument('loglevel',metavar="loglevel", type=str, nargs="?", default="info",
                        help="The loglevel for printing logs. Default \'info\'. supported: \'warn\',\'info\',\'debug\'")

    args = parser.parse_args()

    input_folder = args.input_folder[0]
    output_folder = args.output_folder[0]
    encoding_path = args.encoding_path[0]
    merged_filename = args.merged_filename[0]
    merged_filepath = os.path.join(output_folder,merged_filename)
    file_ending = args.file_ending[0]
    encoded_file_ending = args.encoded_file_ending[0]
    log_level_arg = args.loglevel.lower()

    loglevel = logging.INFO
    if  log_level_arg== "debug":
        loglevel = logging.DEBUG
    elif log_level_arg == "info":
        loglevel = logging.INFO
    elif log_level_arg == "warn":
        loglevel = logging.WARNING
    else:
        print(f"Received unknown/unsupported format for loglevel (\"{log_level_arg}\") - defaulting to info")
    formatter = logging.Formatter("%(asctime)s %(levelname)s:%(message)s")
    logging.basicConfig(filename='openvocab_preparation.log', level=loglevel,format='%(asctime)s %(levelname)s:%(message)s')
    # The Console Handler does add print to console, otherwise it would be quiet.
    console_handler = logging.StreamHandler()
    console_handler.setLevel(logging.DEBUG)
    console_handler.setFormatter(formatter)
    logging.getLogger().addHandler(console_handler)

    logging.info("Starting File-Preparation")
    logging.debug("Received args:")
    logging.debug("\t input_folder: %s",input_folder)
    logging.debug("\t output_folder: %s",output_folder)
    logging.debug("\t encoding_path: %s",encoding_path)
    logging.debug("\t merged_filename: %s",merged_filename)
    logging.debug("\t file_ending: %s",file_ending)
    logging.debug("\t encoded_file_ending: %s",encoded_file_ending)

    encode_files(path_to_raw=input_folder,path_to_encoding=encoding_path,output_folder=output_folder,
                 initial_file_ending=file_ending,file_ending=encoded_file_ending)
    merge_encoded_files(path_to_encoded_files=output_folder,output_file=merged_filepath,file_ending=encoded_file_ending)

    logging.info("Finished File-Preparation - exiting successfully")
    sys.exit(0)

if __name__ == '__main__':
    main()
