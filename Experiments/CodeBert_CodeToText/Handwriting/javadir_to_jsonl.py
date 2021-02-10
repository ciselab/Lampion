import os
import regex as re
import sys
import json

"""
This script takes (when run as main) a directory to look for java files. 
For every javafile, it will split it into javadoc and a single method, 
and create a jsonl as required for codebert. 
The JSONL is "reduced" as it has less attributes, but it should have all required attributes to be thrown into CodeBert.
"""

def javadoc_to_tokens(javadoc: str, cut_at_params: bool = True):
    """
    Requires official Javadoc notation,
    that is
    /**
    *
    */

    Or a block comment
    /*
    *
    */

    Returns a tuple of ([doc-tokens],docstring)
    """
    # Fulltext has all "bad" characters
    fulltext = javadoc
    # Cleartext is cleared from all potential noise
    unescaped = fulltext\
        .replace('*','')\
        .replace('\n',' ')\
        .replace('\t',' ')\
        .strip()
    cleartext = ' '.join(unescaped.split())
    if cut_at_params and '@' in cleartext:
        cleartext = cleartext.split("@")[0]

    doc_tokens = re.findall(r"\w+(?:'\w+)*|[^\w\s]", cleartext)
    #print("javadoc to tokens to be done:",cleartext,doc_tokens)
    return (doc_tokens,cleartext)

def javacode_to_tokens(code:str):
    """
    Starting on method level, without javadocs

    returns a touple of ([code-tokens],code-string)
    """
    code_tokens = re.findall(r"\w+(?:'\w+)*|[^\w\s]", code)
    #print("Javacode to tokens to be done!",code,code_tokens)
    return (code_tokens,code)

def extractClassBody(body:str):
    """
    This method takes everthing after the first curly bracket until the last curly bracket. 
    It is intended to be used for Java Classes that have the first curly bracket after class XZ 
    and with a last closing bracket. 
    It does not work as intended for files with multiple classes / enums / other anomalies.
    """
    # I tried to do fancy regex stuff but ... failed. Regex are not easy and worse for multiline things.
    allAfterFirstBracket = "{".join((body.split("{")[1:]))
    allBeforeLastBracket = "}".join((allAfterFirstBracket.split("}"))[:-1])
    return allBeforeLastBracket


def process_javafile(java_code:str):
    """
    Takes everything from a fully read in java file, 
    splits into javadoc and javacode
    returns the tokenized versions of both.
    """

    javadoc_parts = ()
    code_parts = ()

    javadoc_comment = re.findall(r'\*\*(.*?)\*\/', java_code, re.S)
    block_comment = re.findall(r'\*(.*?)\*\/', java_code, re.S)
    if javadoc_comment:
        fulltext=javadoc_comment[0]
        javadoc_parts = javadoc_to_tokens(fulltext)
    elif block_comment:
        fulltext=block_comment[0]
        javadoc_parts = javadoc_to_tokens(fulltext)
    else:
        raise Exception("No 'normal' javadoc found")

    methodbody = java_code.split("*/")[1]
    code_parts = javacode_to_tokens(methodbody)

    return javadoc_parts + code_parts

def walkJavaFiles(dir: str, output_filename: str = "./compose_input/handwritten.jsonl"):
    """
    This method looks in the specified directory for all files ending in .java
    it looks for the javadoc and the javacode, and
    The found information is written to a new file,
    which is default named handwritten.jsonl
    """

    # The file to write to
    jsonL_file = open(output_filename,mode="w",encoding="utf-8")
    counter = 0 # For debug info

    for dirpath, dnames, fnames in os.walk(dir):
        for f in fnames:
            if f.endswith(".java"):
                # Open the file (read only) and read in all contents
                altered_javaFile = open(dirpath+"/"+f,"r")
                content = altered_javaFile.read()
                altered_javaFile.close()

                all_parts = process_javafile(content)
                method_dict = {
                    "repo":"handcrafted",
                    "original_string":content,
                    "language":"java",
                    "code":all_parts[3],
                    "code_tokens":all_parts[2],
                    "docstring":all_parts[1],
                    "docstring_tokens":all_parts[0]
                }
                json.dump(method_dict,jsonL_file)
                jsonL_file.write("\n")
                #print(all_parts)

                counter = counter + 1

    jsonL_file.close()
    print(f"Found {counter} Javafiles in {dir}")

if __name__ == '__main__':
    """
    Note: The args start at 1, because sys.argv[0] is the script name itself
    """
    print("Running Java to JSonL ")
    if(len(sys.argv)==1):
        print("received no arguments - trying to default reading from 'javafiles' writing to './compose_input/handwritten.jsonl'")
        walkJavaFiles("javafiles")
    elif(len(sys.argv)==2):
        print(f"Trying to read from {sys.argv[1]}, writing to './compose_input/handwritten.jsonl'")
        walkJavaFiles(sys.argv[1])
    elif(len(sys.argv)==3):
        print(f"Trying to read from {sys.argv[1]}, writing to {sys.argv[2]}")
        walkJavaFiles(sys.argv[1],sys.argv[2])
    else:
        print("Received an unknown number of arguments - aborting")