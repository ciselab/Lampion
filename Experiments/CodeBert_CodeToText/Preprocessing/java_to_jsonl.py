import os
import regex as re
import sys

def headerToValues(header: str) -> [(str,str)]:
    """
    This method takes a (full) header as specified in jsonl_to_java.py
    And returns a list of tuples (key,value) fully encoded as both strings.

    This method needs to be changed when the the corresponding header is changed.
    """
    url=header.split("ur_url")[1].strip()
    repo=header.split("ur_repo")[1].strip()
    sha=header.split("ur_sha")[1].strip()
    docstring=header.split("ur_docstring")[1]
    docstring = docstring[3:-2]
    docstring = f"\"{docstring}\""
    docstring = docstring.replace("\\\\n","\\n")
    doctokens=header.split("ur_doctokens")[1]
    path = header.split("ur_path")[1].strip()
    partition = header.split("ur_partition")[1].strip()
    return [("url",f"\"{url}\""),
        ("repo",f"\"{repo}\""),
        ("sha",f"\"{sha}\""),
        ("docstring",docstring),
        ("docstring_tokens",doctokens),
        ("path",f"\"{path}\""),
        ("partition",f"\"{partition}\"")]

def walkJavaFiles(dir: str, output_filename: str = "altered_java.jsonl"):
    """
    This method looks in the specified directory for all files ending in .java
    for every such file, reads the header of the file (which must match the one specified in jsonl_to_java.py)
    and it reads the method-body (also specified using markup words).
    The found information is written to a new file, default named altered_java.jsonl
    """

    # The file to write to
    jsonL_file = open(output_filename,"w")
    counter = 0 # For debug info

    for dirpath, dnames, fnames in os.walk(dir):
        for f in fnames:
            if f.endswith(".java"):
                # Open the file (read only) and read in all contents
                altered_javaFile = open(dirpath+"/"+f,"r")
                content = altered_javaFile.read()
                altered_javaFile.close()

                '''
                This is a bit messy. 
                First, the methodbody needs to be extracted using a helper method
                Then, it get encoded to escape the newlines into \ns 
                Then, it needs to be decoded to be non binary
                After that, wrap it in normal string-quotes (not single quotes)
                '''

                methodbody = extractClassBody(content)
                methodbody = methodbody.encode('unicode_escape').decode("utf-8")
                methodbody = f"\"{methodbody}\""
                methodtokens = re.findall(r"\w+(?:'\w+)*|[^\w\s]", methodbody.replace("\\n",""))

                # Look for the header by markup words, and extract the values
                header = (content.split("python_helper_header_start")[1]).split("python_helper_header_end")[0]
                header_vals = headerToValues(header)

                # After reading all the values, start an accumulator object that represents a single line of jsonl
                acc = "{"
                for pair in header_vals:
                    acc = acc + f"\"{pair[0]}\"={pair[1]} , "
                acc = acc + f"\"code_tokens\"={methodtokens} , "
                acc = acc + f"\"code\"={methodbody} }}"
                jsonL_file.write(acc)

                counter = counter + 1

    jsonL_file.close()
    print(f"Found {counter} Javafiles in {dir}")


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

if __name__ == '__main__':
    """
    Note: The args start at 1, because sys.argv[0] is the script name itself
    """
    print("Running Java to JSonL ")
    if(len(sys.argv)==1):
        print("received no arguments - trying to default reading from 'output' writing to 'altered_java.jsonl'")
        walkJavaFiles("output")
    elif(len(sys.argv)==2):
        print(f"Trying to read from {sys.argv[1]}, writing to 'altered_java.jsonl'")
        walkJavaFiles(sys.argv[1])
    elif(len(sys.argv)==3):
        print(f"Trying to read from {sys.argv[1]}, writing to {sys.argv[2]}")
        walkJavaFiles(sys.argv[1],sys.argv[2])
    else:
        print("Received an unknown number of arguments - aborting")