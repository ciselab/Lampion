package com.github.ciselab.lampion.manifest;

import com.github.ciselab.lampion.transformations.TransformationCategory;
import com.github.ciselab.lampion.transformations.TransformationResult;
import spoon.reflect.declaration.CtElement;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class provides an interface to write a set of transformations into an SQLite Database, which will be newly created for this.
 *
 * The class covers everything from building the schema, doing a little healthcheck,
 * sorting and writing the results into different tables.
 *
 *
 * It is very much an extraction of the "SQLiteTests.java", which hold a set of exploration tests around SQLite.
 *
 * General Watch-Out points:
 * - SQLite does not support multiple statements in a single query. These need to be split into multiple queries.
 * - SQLite prepared Statements start setting their parameters from "1", replacing the first "?".
 */
public class SqliteManifestWriter implements ManifestWriter {

    private String pathToSchemaFile;
    private String pathToDatabase;

    private Map<String,Long>                 name_lookup;
    private Map<CtElement,Long>              position_lookup;
    private Map<TransformationCategory,Long> category_lookup;

    public SqliteManifestWriter(String pathToSchemaFile, String pathToDatabase){
        // Check if the SQLite JDBC is properly configured and in Path
        // This is a regression check
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (pathToSchemaFile == null || pathToSchemaFile.isEmpty() || pathToSchemaFile.isBlank()) {
            throw new UnsupportedOperationException("Path To Database Schema cannot be null or empty");
        }
        if (pathToDatabase == null || pathToDatabase.isEmpty() || pathToDatabase.isBlank()) {
            throw new UnsupportedOperationException("Path To SQLite-Database cannot be null or empty");
        }
        if (Files.notExists(Path.of(pathToSchemaFile))) {
            throw new UnsupportedOperationException("There was no Schema-Files found under: " + pathToSchemaFile);
        }
        this.pathToDatabase = pathToDatabase;
        this.pathToSchemaFile = pathToSchemaFile;
    }

    @Override
    public void writeManifest(List<TransformationResult> results) {

        // Create Schema on Startup

        // Sort results by class or method scope
        // Do so using checking for parents being methods

        // Check that they are still in order of their application after sorting / splitting

        // For all items check
            // Insert Transformation Names,
            // Insert Category Names,
            // Insert Positions

        // Create a Map to Lookup Name -> oid
        // Create a Map to lookup position -> oid

        // iterate over all items and write to transformations_table

        // Extra: Add a version / info table
    }

    private String readSchemaFile(String pathToDBSchema) throws IOException {
        String schemaSQL = Files.readString(Path.of(pathToDBSchema));
        return schemaSQL;
    }

    private void createSchemaFromSQLString(Connection con, String schemaSQL) throws SQLException {
        var parts = schemaSQL.split(";");

        for (var p : parts){
            con.prepareStatement(p).execute();
        }
    }

    private void writeCategoryNames(Connection con, Collection<TransformationCategory> categories) throws SQLException {
        final String INSERT_CATEGORY_SQL = "INSERT INTO transformation_categories (category_name) VALUES (?);";
        var insertCategoryStmt = con.prepareStatement(INSERT_CATEGORY_SQL);
        for(TransformationCategory category : categories) {
            insertCategoryStmt.setString(1,category.name());
            insertCategoryStmt.execute();
        }
    }

    private Map<TransformationCategory,Long> buildCategoryLookup(Connection con) throws SQLException {
        HashMap<TransformationCategory,Long> lookup = new HashMap<>();

        final String SELECT_ALL_CATEGORIES_SQL = "SELECT ROWID, category_name FROM transformation_categories;";
        var categoryReadStmt = con.prepareStatement(SELECT_ALL_CATEGORIES_SQL);
        var categoryReadResult = categoryReadStmt.executeQuery();

        while(categoryReadResult.next()){
            lookup.put(TransformationCategory.valueOf(categoryReadResult.getString("category_name"))
                    ,categoryReadResult.getLong("ROWID"));
        }
        return lookup;
    }

    private void writeTransformationNames(Connection con, Collection<String> names) throws SQLException {
        final String INSERT_NAME_SQL = "INSERT INTO transformation_names (transformation_name) VALUES (?);";
        var insertTransformationNameStmt = con.prepareStatement(INSERT_NAME_SQL);

        for(String name : names) {
            insertTransformationNameStmt.setString(1,name);
            insertTransformationNameStmt.execute();
        }
    }

    private Map<String,Long> buildTransformationNameLookup(Connection con) throws SQLException {
        HashMap<String,Long> lookup = new HashMap<>();

        // Build Transformations Lookup
        String SELECT_ALL_NAMES_SQL = "SELECT ROWID, transformation_name FROM transformation_names;";
        var nameReadStmt = con.prepareStatement(SELECT_ALL_NAMES_SQL);
        var nameReadResult = nameReadStmt.executeQuery();

        while(nameReadResult.next()){
            lookup.put(nameReadResult.getString("transformation_name"),nameReadResult.getLong("ROWID"));
        }

        return lookup;
    }

    /**
     * With the position it's a bit more difficult, as the elements have different scopes
     * Some CtElements are Methods and "well formed", some are classes, but others are e.g. CtLiterals
     * If a literal or anything similar occurs, it needs a way to find it's correct parents and read attributes
     * from there.
     */
    private void writePositions(Connection con, Collection<CtElement> elementWithPosition) throws SQLException {

    }


    private Map<CtElement,Long> createPositionLookup(Connection con, Collection<CtElement> elementToBeInMapping) throws SQLException {
        /* This is a bit trickier than categories,
           some issues are:
           1. Two elements can have the same position
           2. Maybe an Element has different set of results
           Therefore, this method also needs the elements as inputs to be semi-pure.

           To build the lookup, do:
           1. Read all positions
           2. check for all ElementsToBeInMapping whether the position matches
           3. if the position matches, add element+position to lookup
           4. get next position

           The only other way to achieve this would be to create a Map<Predicate<CtElement>,Long> but that is rather overengineered.
         */
        HashMap<CtElement,Long> lookup = new HashMap<>();

        return lookup;
    }

    /**
     * As Categories and Transformation Names are stored separately using a mapping table,
     * the mapping table need to be filled as well.
     * The Map can easily be derived using Transformation::getName and Transformation::getCategories
     *
     * It uses the toplevel lookups, that already must be initialized when this method is run.
     */
    private void writeNameToCategoryMapping(Connection con, Collection<TransformationResult> resultsWithDistinctNames ) throws SQLException {
        if (name_lookup == null || category_lookup == null) {
           throw new UnsupportedOperationException("Either Category or Name Lookup are not initialized - cannot build mapping.");
        }

        Map<String, Collection<TransformationCategory>> mapping = new HashMap<>();
        for (TransformationResult r : resultsWithDistinctNames) {
            mapping.put(r.getTransformationName(),r.getCategories());
        }

        final String WRITE_NAME_CATEGORY_MAPPING_SQL =
                "INSERT INTO transformation_name_category_mapping (name_reference,category_reference) VALUES (?,?);";
        var writeNameCategoryMappingSQL = con.prepareStatement(WRITE_NAME_CATEGORY_MAPPING_SQL);

        for(String transformation_name : mapping.keySet()) {
            for(TransformationCategory category : mapping.get(transformation_name)){
                writeNameCategoryMappingSQL.setLong(1,name_lookup.get(transformation_name));
                writeNameCategoryMappingSQL.setLong(2,category_lookup.get(category));
                writeNameCategoryMappingSQL.execute();
            }
        }
    }


}
