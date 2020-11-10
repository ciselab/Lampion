# Java Obfuscator

This part of the Lampion Project alters Java Files using metamorphic transformations and returns/writes the altered javafiles as well as a manifest. 

It is currently still under development. 

## Build & Run 

To build the project, simply do: 

```sh
mvn clean build test
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

## How to get started

It's highly recommended to start your reading on project level scope, e.g. the [projects README](../README.md) and the Skim over Objects of interests in the [Design Notes](../Resources/DesignNotes.md).
Once you have a feeling that you understand the project level scope and ideas,  you can start reading the code. 
If you just want to look at existing Transformations and how they work, see [Transformations.md](../Resources/Transformations.md).

If you want to add Transformations, have a look at 

1. The Transformation Interface
2. The Spoon Exploration Tests
3. Express what you want to do in a Spoon Test
4. Write your own Transformation in such a exploration test
5. Write the Transformation Class, keep it close to existing Transformations
6. Test it!

If you want to alter something in terms of program flow or IO behaviour, the best starting point is *Engine.java*.

## Requirements 

- Maven
- Jdk 15

## Troubleshooting 

*When running the SQL-Lite parts from IDE, it tells me there is no valid SQLite Driver*: This is due to a missing classpath-entry for the SQLite driver. The concrete solution is linked in the SQLiteTests.java. 