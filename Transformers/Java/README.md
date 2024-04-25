# Java Transformer

This part of the Lampion Project alters Java Files using metamorphic transformations and returns/writes the altered javafiles as well as a manifest. 
It has two parts, the CLI and the CORE library.

It is currently still under development. 

## Build & Run 

To build the project, simply do: 

```sh
mvn clean package verify
```

To build an executable: 

```
mvn package verify
```

And to run the exploration tests (Not recommended for default):

```sh
mvn clean test -pExploration
```
The exploration-test profile will **only** run Tests tagged as `@Tag("Exploration")`.


To build with Docker: 

```sh
docker build --build-arg TRANSFORMER_VERSION=1.4-SNAPSHOT . -t lampion/java-transformer:1.4 -t lampion/java-transformer:latest -t ciselab/java-transformer:1.4 -t ciselab/java-transformer:latest -t ghcr.io/ciselab/lampion/java-transformer:1.4
```

where *snapshot*-versions are supported. 

To run with Docker adjust the docker-compose and run 'docker-compose up'.

## How to get started

It's highly recommended to start your reading on project level scope, e.g. the [projects README](../README.md) and the Skim over Objects of interests in the [Design Notes](../Resources/DesignNotes.md).
Once you have a feeling that you understand the project level scope and ideas,  you can start reading the code. 
If you just want to look at existing Transformations and how they work, see [Transformations.md](../Resources/Transformations.md).

If you want to add Transformations, have a look at 

1. The Transformer Interface
2. The Spoon Exploration Tests
3. Express what you want to do in a Spoon Test
4. Write your own Transformation in such a exploration test
5. Write the Transformation Class, keep it close to existing Transformations
6. Test it!

If you want to alter something in terms of program flow, the best starting point is *Engine.java*.
For the file IO the starting point would be *App.java*.

## Requirements 

- Maven
- Jdk 17

## Troubleshooting 

*When running the SQL-Lite parts from IDE, it tells me there is no valid SQLite Driver*: This is due to a missing classpath-entry for the SQLite driver. 
The concrete solution is linked in the SQLiteTests.java. 

*Some of the tests fail and tell me that files do not exist*: depending on your OS, the files are case-sensitive. 
I tried to avoid it, but you can check if all the expected folders are there, if you can find an issue with the naming of files, but you can also run `mvn test -Pnofiles` - however, this will run less tests. 
