# Design Notes

This file holds some considerations for the implementation details that occurred throughout the project.

## Naming 

A short dictionary to understand names and namings in this project: 

- **AST**: [Abstract Syntax Tree](https://en.wikipedia.org/wiki/Abstract_syntax_tree) - a representation of the code and program produces by a compiler. The ASTs in this project are provided by the [Spoon Library](https://github.com/INRIA/spoon/). 
- **Metamorphic Transformations**: A metamorphic transformation originates from machine vision, where initially the images get flipped, rotated or noised in order to test/improve the model under test. The initial image is still the same for humans, and has the same degree of information (e.g. it is still a dog on the picture, even if it's turned upside down). This behavior is transferred to code and models working on code, for quick-examples see [The overview image](./ExampleTransformations.PNG) or [more elaborate definitions](./Transformations.md). The term *"metamorphic transformation"* and *"transformation"* are used interchangeably in this project, as there are no other transformations.

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
- A "Before <-> After" of the code, at least for debugging purposes.

The initial idea for a recording the transformations was a (simple) file in either CSV or JSON.
However, the categories which are of variable length break CSVs (in a nice way).
And JSON, while human readable, has an explosion in size and as big-data amounts are to be expected, storing millions of records as JSON is a bad idea.
The same reasoning applies to (plain) xml.

Hence, the data needs to be compressible and support variable length inputs.
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

### Enum vs. Strings for Categories

When implementing the Categories in Java, I decided to go for an enumeration instead of a `Set<String>` to implement the categories for the results. 
This was done to have documentation on the categories in a single place (the enum file) as well as to miss out typos. 
Hence, it seems like a good approach. 

For the SQL file, it could be possible to store the categories as enums as well. 
The issue with this is, that whenever there is a transformation implemented with a new category, the database will need to change as well. 
And, furthermore, older databases will be unusable with the current version. 

After the data is in the SQL Database, it will be used "read only" anyway, so the additional safety provided by enums is not necessary. 

Therefore, the obfuscator instantiates the categories table of the database with all categories found in its enumeration and linking the entries to the categories table instead of using an database-enumeration.
This approach helps to be a bit more flexible in terms of the database and also helps to re-use the schema for more and different obfuscators. (Let's say you write another obfuscator in Python with different Categories, then you'd have to sync the enum over java, python and sql)

*See:*

- [TransformationCategory.java](../JavaObfuscator/src/main/java/com/github/ciselab/lampion/transformations/TransformationCategory.java)

## Obfuscator

### Registration of Transformations

For the Transformations I'd have liked to have a single class per Transformation with a shared interface.
That is all fun and games until it comes to randomly picking a Transformation.

If another component needs to pick amongst all available Transformations, there must be either

- A concept where each Transformation self-registers in an entity
- An Entity knows all available Transformations (God Class)

Both have up and downsides:

The Self-Registering will be more decentralized, sticking closer to object oriented standards (each class is responsible for it's registration).
However, using the self-registered transformations will require the use of reflections, which is horrible to test and hard to understand.

The God-Class breaks with responsibilities and can grow immensely large.
On the other side, it's easier to spot issues and test it, as well as it's easier to get started with in the first place.

**At the moment the God Class is chosen**, as in the first prototypes there will be little to no issues with size and only a few Transformations implemented.

**Update:** Apart from the initial thoughts, a compromise has been chosen. The initial thoughts are kept for now to show the reasoning.
Instead of implementing *Transformations* now implemented are *Transformers*. 
These Transformers can be build and scoped by need and can be registered in a central entity. 
Every Transformer has a static method that registers itself in a Transformer Registry. 
Then all the ugly parts of reflections are dodged, and the parts of the God Class can be tested easier. 

This also enables to have a registry-class, where the App has one central registry from system startup or initializes a registry according to configuration. With the registry being an object, it breaks with the issues of the God-Class and enhances testing.

*See:*

- [Transformation.java](../JavaObfuscator/src/main/java/com/github/ciselab/lampion/transformations/Transformer.java)
- [TransformationRegistry.java](../JavaObfuscator/src/main/java/com/github/ciselab/lampion/transformations/TransformationRegistry.java)

### Attributes of Transformations

Transformations come with a variety of possible attributes,
but to have a nicely configurable program it's important to address them probably.

Each Transformation has

- A (unique) name
- A set of categories (such as NLP-Relevant, Structure-Relevant, ...)
- Whether it is (likely to be) a Code-Smell
- Whether it is (likely) to affect generated Bytecode
- A set of Transformations to which it is exclusive
- A set of sanity-checks whether it is applicable

Some Transformations are diametric to each other and hence useless when applied after each other, examples are removing comments versus duplicating comments.
Some Transformations are exclusive to themselves, that is e.g. changing a method name twice does not make sense.

Sanity checks are a super set of exclusiveness-checks and cover things such as that you can't alter variable names for a method that has no variables.
For any `Transformation<T>` the sanity checks can be expressed as a `Set<Predicate<T>>`.

These Attributes should enable a managing component to

1. pick only valid, none code breaking, Transformations for any method
2. pick Transformations based on a specified distribution
