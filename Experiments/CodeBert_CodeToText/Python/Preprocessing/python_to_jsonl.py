import io
import os
import regex as re
import sys
import json
import tokenize


def header_to_values(header: str) -> [(str, str)]:
    """
    This method takes a (full) header as specified in jsonl_to_java.py
    And returns a list of tuples (key,value) fully encoded as both strings.

    This method needs to be changed when the the corresponding header is changed.
    """
    url = header.split("ur_url")[1].strip()
    repo = header.split("ur_repo")[1].strip()
    sha = header.split("ur_sha")[1].strip()
    func_name = header.split("ur_func_name")[1].strip()

    # This Docstring encoding is the result of some regression bugs
    # In general there are some issues created with json -> string -> file -> string -> json
    docstring = header.split("ur_docstring")[1]
    docstring = docstring[3:-2]
    docstring = docstring.encode('unicode_escape').decode("utf-8")
    docstring = docstring.replace('"', '\\"')
    docstring = docstring.replace("\\\\n", "\\n")
    docstring = docstring.replace("\\n", " ")
    docstring = docstring.replace("\\\\'", " ")
    docstring = docstring.replace("\\'", " ")
    docstring = docstring.replace("\\`", " ")
    docstring = f"\"{docstring}\""

    doctokens = header.split("ur_doctokens")[1]
    doctokens = eval(doctokens[2:-2])
    path = header.split("ur_path")[1].strip()
    partition = header.split("ur_partition")[1].strip()
    return [("url", f"\"{url}\""),
            ("repo", f"\"{repo}\""),
            ("sha", f"\"{sha}\""),
            ("func_name", f"\"{func_name}\""),
            ("docstring", docstring),
            ("docstring_tokens", doctokens),
            ("path", f"\"{path}\""),
            ("partition", f"\"{partition}\"")]


def walk_python_files(dir: str, output_filename: str = "altered_python.jsonl"):
    """
    This method looks in the specified directory for all files ending in .py
    for every such file, reads the header of the file (which must match the one specified in jsonl_to_python.py)
    and it reads the method-body (also specified using markup words).
    The found information is written to a new file, default named altered_python.jsonl
    """

    # The file to write to
    jsonL_file = open(output_filename, mode="w", encoding="utf-8")
    counter = 0  # For debug info

    for dirpath, dnames, fnames in os.walk(dir):
        for f in fnames:
            if f.endswith(".py"):
                # Open the file (read only) and read in all contents
                altered_python_file = open(dirpath + "/" + f, "r")
                content = altered_python_file.read()
                altered_python_file.close()

                '''
                This is a bit messy. 
                First, the methodbody needs to be extracted using a helper method
                Then, it get encoded to escape the newlines into "\n"-s 
                Then, it needs to be decoded to be non binary
                After that, wrap it in normal string-quotes (not single quotes)
                '''

                method_body = extract_class_body(content)
                method_body = method_body.encode('unicode_escape').decode("utf-8").replace('"', r'\"')
                method_body = f"\"{method_body}\""
                method_tokens = re.findall(r"\w+(?:'\w+)*|[^\w\s]", method_body.replace("\\n", ""))
                method_tokens = [m for m in method_tokens if m != '"']

                m_tokens = {
                    "code_tokens": method_tokens
                }

                # Look for the header by markup words, and extract the values
                header = (content.split("python_helper_header_start")[1]).split("python_helper_header_end")[0]
                header_vals = header_to_values(header)

                # After reading all the values, start an accumulator object that represents a single line of jsonl
                acc = "{"
                for pair in header_vals:
                    if (pair[0] == "docstring_tokens"):
                        d_tokens = {"docstring_tokens": pair[1]}
                        acc = acc + json.dumps(d_tokens)[1:-1] + " , "
                    else:
                        acc = acc + f"\"{pair[0]}\": {pair[1]} , "
                acc = acc + json.dumps(m_tokens)[1:-1] + " , "

                # Regression: There was an issue with code containing \ux00 breaking the json
                acc = acc + f"\"code\": {method_body} }}"
                acc = acc + "\n"  # Line break after entry
                jsonL_file.write(acc)

                counter = counter + 1

    jsonL_file.close()
    print(f"Found {counter} Python-Files in {dir}")


def extract_class_body(body: str) -> str:
    """
    This method takes everything after the class-definition.
    Technically, this uses a method stolen from stackoverflow with a lot of magic.
    This was necessary, as it is very hard to remove block-comments while not removing block-strings.

    It does not work as intended for files with multiple classes / other anomalies (At least it will have information-loss).
    """
    no_comment_class_body = remove_comments_and_docstrings(body)
    return no_comment_class_body


def remove_comments_and_docstrings(source: str) -> str:
    """
    Returns 'source' minus comments and docstrings.
    Shamelessly stolen from: https://stackoverflow.com/questions/1769332/script-to-remove-python-comments-docstrings/1769577#1769577
    With the difference that the stated cStringIO is simply io in newer python.
    """
    io_obj = io.StringIO(source)
    out = ""
    prev_toktype = tokenize.INDENT
    last_lineno = -1
    last_col = 0
    for tok in tokenize.generate_tokens(io_obj.readline):
        token_type = tok[0]
        token_string = tok[1]
        start_line, start_col = tok[2]
        end_line, end_col = tok[3]
        ltext = tok[4]
        # The following two conditionals preserve indentation.
        # This is necessary because we're not using tokenize.untokenize()
        # (because it spits out code with copious amounts of oddly-placed
        # whitespace).
        if start_line > last_lineno:
            last_col = 0
        if start_col > last_col:
            out += (" " * (start_col - last_col))
        # Remove comments:
        if token_type == tokenize.COMMENT:
            pass
        # This series of conditionals removes docstrings:
        elif token_type == tokenize.STRING:
            if prev_toktype != tokenize.INDENT:
                # This is likely a docstring; double-check we're not inside an operator:
                if prev_toktype != tokenize.NEWLINE:
                    # Note regarding NEWLINE vs NL: The tokenize module
                    # differentiates between newlines that start a new statement
                    # and newlines inside of operators such as parens, brackes,
                    # and curly braces.  Newlines inside of operators are
                    # NEWLINE and newlines that start new code are NL.
                    # Catch whole-module docstrings:
                    if start_col > 0:
                        # Unlabelled indentation means we're inside an operator
                        out += token_string
                    # Note regarding the INDENT token: The tokenize module does
                    # not label indentation inside of an operator (parens,
                    # brackets, and curly braces) as actual indentation.
                    # For example:
                    # def foo():
                    #     "The spaces before this docstring are tokenize.INDENT"
                    #     test = [
                    #         "The spaces before this string do not get a token"
                    #     ]
        else:
            out += token_string
        prev_toktype = token_type
        last_col = end_col
        last_lineno = end_line
    return out


if __name__ == '__main__':
    """
    Note: The args start at 1, because sys.argv[0] is the script name itself
    """
    print("Running Java to JSonL ")
    if (len(sys.argv) == 1):
        print("received no arguments - trying to default reading from 'output' writing to 'altered_python.jsonl'")
        walk_python_files("output")
    elif (len(sys.argv) == 2):
        print(f"Trying to read from {sys.argv[1]}, writing to 'altered_python.jsonl'")
        walk_python_files(sys.argv[1])
    elif (len(sys.argv) == 3):
        print(f"Trying to read from {sys.argv[1]}, writing to {sys.argv[2]}")
        walk_python_files(sys.argv[1], sys.argv[2])
    else:
        print("Received an unknown number of arguments - aborting")
