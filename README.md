# OpenVocabCodeNLM Preprocessing

This repository helps me to figure the pre-processing for the [OpenVocabCodeNLM](https://github.com/mast-group/OpenVocabCodeNLM) out and streamline it.

This is focussed on `.py`-files and uses the [subword-nmt-library](https://github.com/rsennrich/subword-nmt).
I put in everything necessary to run other file-types, but I have only run it with python at the moment.

The repository encodings were drawn from [Zenodo](https://zenodo.org/record/3628636).
I do intend to keep the original encodings and do not provide / create my own.

## Requirements

For Docker: **Docker 20**

From Python **3.9**:

```console
pip install -r requirements.txt
pip install subword-nmt==0.3.8
```

## How To

**For Docker:**

Adjust the params in the provided [example docker-compose](./docker-compose.yaml), then run:

```shell
docker-compose up --build
```

**For Python:**

First, make sure that your subwort-nmt is available by checking `subword-nmt --help`.

Then run: 

```shell 
python prep.py my_sample_files prep_output python_encodings.enc_bpe_10000 selfmade_pre_enc_10000 info
```

Info can be gained by `python prep.py -h`

## Expected Output

Within the specified output folder, you should see one `.py_enc`-file per `.py`-file in input.
The `.py_enc`-files look similar to this: 
``` 
if __name__ == '__main@@ __@@ '@@ :
    s@@ ome = "@@ Le@@ on@@ har@@ d"
    print@@ (@@ "@@ Th@@ e main@@ tain@@ er her@@ e is ",@@ s@@ ome@@ )
```

Where `@@` marks, that the token is not "closed", i.e. there is a connection to the one after.

The input files should remain untouched, and the output folder *replicates* the structure of the input folder.

The `py_enc`-files will be merged to a dataset-file (without file ending), that consists of one-line per `py_enc`-file.
The merged file looks like this:

``` 
if __name__ == '__main@@ __@@ '@@ :s@@ ome = "@@ Le@@ on@@ har@@ d"print@@ (@@ "@@ Th@@ e main@@ tain@@ er her@@ e is ",@@ s@@ ome@@ )print@@ (@@ "@@ An@@ y issu@@ es c@@ an be forwar@@ ded to@@ :@@ "@@ )print@@ (@@ "--@@ - n@@ ob@@ o@@ dy --@@ -@@ "@@ )
def sum@@ 2@@ (@@ a@@ , b@@ )@@ :return a + b@@ ;def ac@@ c@@ (@@ list@@ : [@@ int@@ ]@@ ) -@@ > int@@ :return (@@ sum@@ (@@ list@@ )@@ )
def gre@@ et@@ (@@ a@@ :@@ str = "@@ Le@@ on@@ har@@ d" ) -@@ > str@@ :return "H@@ ell@@ o " + adef s@@ ome_@@ other_@@ function@@ ( l@@ ,@@ t@@ ,@@ f@@ ) -@@ > float@@ :return l ^ t + 2 * f
```
