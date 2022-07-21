import jsonlines    # For addressing json values as dictionaries in jsonl files
import os           # For File/Directory Creation
import sys          # For handling command args

"""
Notes: 
The filename needs to be class+function as otherwise they will overwrite each other
(Only the combination of class + function is unique, sometimes the same class and method names exist)
"""


def read_jsonl_and_print_all_to_java(jsonl_file: str, output_prefix: str = "./output/") -> ():
    """
    This method reads a jsonl_file and creates a java-file for each entry found.
    It also adds the remaining information of the json as a header to the file in a comment.
    :param jsonl_file: the filepath to look for the jsonl
    :return: nothing, creates one java file per line found
    """
    counter = 0
    with jsonlines.open(jsonl_file) as f:
        for line in f.iter():
            write_to_file(line, output_prefix)
            counter = counter + 1
    print(f"Wrote {counter} Entries from the JSONL file {jsonl_file} to .java files in {output_prefix}.")


def write_to_file(line: str, output_prefix: str = "./output/") -> ():
    """
    This method creates a full java file given a jsonl line.
    The java file will be created and named according to the the path and file name specified in the json line.
    :param line: The JsonL line to be written to the file
    :param output_prefix: the directory to which to write all the files, default to ./output/
    :return: creates a directory
    """
    # Read all relevant position information from the jsonl
    (path, filename, package, classname) = split_path_to_parts(line['path'])
    # Create the directories if necessary
    p = output_prefix + "/" + path
    os.makedirs(p, exist_ok=True)
    # create the file, fill it with the java class and close it
    func = line['func_name'].split('.')[1]
    f = open(output_prefix + "/" + path + "/" + classname + "_" + func + ".java", "w")
    f.write(wrap_in_class_and_package(line))
    f.close()


# This var simply holds all seen combinations of function and class names
# It is necessary as some methods are overloaded and would result in the same file
# Which results in issues with the java obfuscation which fails on duplicate java classes
seen_class_names = []


def wrap_in_class_and_package(line):
    """
    Wraps the content of the given line into a java package+class+markup_info
    Required as the entries are on function level,but the obfuscator runs on class level.

    This method does not write to a file, it simply alters the dictionary to a string.
    TODO: Maybe better format for markups, maybe move header up before the package
    :param line: the line of jsonl to be wrapped in a .java file with markup words for other attributes
    :return: the line wrapped in package and class
    """
    (path, file, package, classname) = split_path_to_parts(line['path'])
    func = line['func_name'].split('.')[1]

    # There was an issue on the java-obfuscation side that had troubles with duplicate java classes
    # This was due to the naming here, especially for overloaded functions
    # Hence, if there was a method already seen, just add a counter at the end of the classname to be unique
    final_classname = f"{classname}_{func}"
    counter = 2
    while final_classname in seen_class_names:
        final_classname = f"{classname}_{func}_{counter}"
        counter = counter + 1
    seen_class_names.append(final_classname)

    # Python has escape, but double { are the {-escape
    file_content = f"""
    package {package}; 
    /*
    python_helper_header_start
    ur_repo {line['repo']} ur_repo
    ur_url {line['url']} ur_url
    ur_path {line['path']} ur_path
    ur_func_name {line['func_name']} ur_func_name
    ur_docstring {(line['docstring']).encode('utf-8')} ur_docstring
    ur_doctokens {line['docstring_tokens']} ur_doctokens
    ur_sha {line['sha']} ur_sha
    ur_partition {line['partition']} ur_partition
    python_helper_header_end
    */
    public class {final_classname} {{

        {line['code']}
        
    }}
    """
    return file_content


def split_path_to_parts(path: str):
    """
    Helper to separate paths into information pieces
    :param path: the path of a java file including the file itself
    :return: a tuple of path,file,package name derived from path and class name derived from file
    """
    parts = path.split('/')
    package = ".".join(parts[:-1])
    path = "/".join(parts[:-1])
    file_name = parts[-1]
    classname = file_name.split(".")[0]
    values = (path, file_name, package, classname)
    return values


if __name__ == '__main__':
    """
    Note: The args start at 1, because sys.argv[0] is the script name itself
    """
    print("Running JsonL to Java ")
    if len(sys.argv) == 1:
        print("received no arguments - trying to default to 'java.jsonl' and writing to 'output'")
        read_jsonl_and_print_all_to_java('java.jsonl')
    elif len(sys.argv) == 2:
        print(f"Trying to read {sys.argv[1]}, writing to 'output'")
        read_jsonl_and_print_all_to_java(sys.argv[1])
    elif len(sys.argv) == 3:
        print(f"Trying to read {sys.argv[1]}, writing to {sys.argv[2]}")
        read_jsonl_and_print_all_to_java(sys.argv[1], sys.argv[2])
    else:
        print("Received an unkown number of arguments - aborting")
