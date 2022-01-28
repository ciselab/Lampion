import argparse
import os
import shutil
import logging


def encode_files(path_to_raw:str,path_to_encoding:str,output_folder:str,file_ending:str="py_enc") -> None:
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
        shutil.rmtree(output_folder)

    for dirpath, dnames, fnames in os.walk(path_to_raw):
        for f in fnames:
            if f.endswith(".py"):
                #print(dirpath,f)
                full_path = os.path.join(dirpath,f)
                # Remove the last 3 characters, exactly ".py", then add our postfix
                encoded_f = f[:-3] + "." + file_ending
                encoded_file_dir = os.path.join(output_folder,dirpath)
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


if __name__ == '__main__':
    logging.basicConfig(level=logging.INFO,format='%(asctime)s %(levelname)s:%(message)s')

    logging.info("Starting File-Preparation")
    # TODO: Arguments!
    encode_files("./my_sample_files","./python_encodings/python_encoding.enc_bpe_10000","prep_output")
    merge_encoded_files("./prep_output","./prep_output/merged_output.txt")
    logging.info("Finished File-Preparation - exiting successfully")