# Design Notes

This file holds some considerations for the implementation details that occurred throughout the project.

## Alternation Manifest

### Representation on Disk 

For the alternation manifest, it should have a list of entries where each entry should have: 

- A (fully qualified) methodname
- A (fully qualified) classname
- A list of Transformations

Where each Transformation should have: 

- A name / type
- A set of categories it belongs to
- A scope
- A "Before <-> After" of the code, atleast for debugging purposes.

The initial idea for a recording the transformations was a (simple) file in either CSV or JSON. 
However, the categories which are of variable length break csv's (in a nice way).
And JSON, while human readable, has an explosion in size and as big-data amounts are to be expected, storing millions of records as JSON is a bad idea.
The same reasoning applies to (plain) xml.

Hence, the data needs to be compressable and support variable length inputs. 
Candidates would be binary-serialized xml or Binary-JSON in a compressed format.
Another more exotic candidate would be Java-Serialization of the objects, however then the visualisation is also bound to Java.
As I do not want to poke around in binary data with python, a different idea has been chosen: 

SQL & SQLite. 

SQLLite is fast, everywhere and supports the required schema in a good way. 
SQLLite comes with a build in compression.

There are two further benefits: 

- When only using standard SQL and ODBC, the obfuscator can store the manifest in a remote library
- Some queries can be performed on the database, saving time in python

The negative point in using SQL is that it is compared to a file a lot of additional work for both sides, visualisation and writing.
