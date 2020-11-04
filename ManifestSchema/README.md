# Manifest Schema

This folder contains .sql files to create the required Schema in SQLite. 

They are intended to give a guidance for any other database and to be reusable potentially by new Obfuscators.

Additionally to schema creation, a set of SQL-Statements for manual inspection is provided.

For this repository, the database is always called "*manifest.db*".

## Schema

TBD Image of Schema

## Manual Creation 

If you want to create a local sqlite database with the schema do: 

```bash
sqlite3 manifest.db < createManifestSchema.sql
```

## CI Check

the *sqlCheck.sh* will be used to perform a simple test in CI whether the Schema is valid SQLite.

## Design Decisions

**Categories per Transformation not per Name:**
At the moment, the categories are mapped to the transformation using the transformation name, implying that all transformations with the same name share the same categories. 
One thing to consider would be to make the categories per transformation not per transformation, but that would increase the references required greatly. 
Therefore, the reference-less approach is taken until further adjustments are necessary.