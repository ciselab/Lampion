package com.github.ciselab.lampion.manifest;

import com.github.ciselab.lampion.program.App;
import com.github.ciselab.lampion.transformations.TransformationCategory;
import com.github.ciselab.lampion.transformations.TransformationResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class provides an interface to write a set of transformations into an SQLite Database, which will be newly created for this.
 *
 * The class covers everything from building the schema, doing a little health-check,
 * sorting and writing the results into different tables.
 *
 * In terms of writing to SQLite, most of the tables are ought to be very small.
 * The categories e.g. are exactly as much as you have defined in @TransformationCategory and for the TransformationNames
 * it is the very same, there are no more transformation_names as you have transformations in this program.
 * The Mapping Table can be at most (categories times names), which means that there should be at most ~200 mapping entries.
 * The only two big tables are the position table, which grows with the altered CTElements and the transformations table.
 * These scale directly with
 *  a) program size of the system under change
 *  b) configuration of the Obfuscator
 * These are the only two write processes worth of tuning.
 *
 * It is very much an extraction of the "SQLiteTests.java", which hold a set of exploration tests around SQLite.
 *
 * General Watch-Out points:
 * - SQLite does not support multiple statements in a single query. These need to be split into multiple queries.
 * - SQLite prepared Statements start setting their parameters from "1", replacing the first "?".
 */
public class SqliteManifestWriter implements ManifestWriter {
    private static Logger logger = LogManager.getLogger(SqliteManifestWriter.class);

    private String pathToSchemaFile;
    private String pathToDatabase;

    private Map<String,Long>                 name_lookup;
    private Map<CtElement,Long>              position_lookup;
    private Map<TransformationCategory,Long> category_lookup;

    public SqliteManifestWriter(String pathToSchemaFile, String pathToDatabase){
        // Check if the SQLite JDBC is properly configured and in Path
        // This is a regression check, may need to be adjusted for different drivers
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            logger.error("There was an error not finding the JDBC-SQLite Driver in the classpath",e);
            return;
        }
        // Value Checks
        if (pathToSchemaFile == null || pathToSchemaFile.isEmpty() || pathToSchemaFile.isBlank()) {
            throw new UnsupportedOperationException("Path To Database Schema cannot be null or empty");
        }
        if (pathToDatabase == null || pathToDatabase.isEmpty() || pathToDatabase.isBlank()) {
            throw new UnsupportedOperationException("Path To SQLite-Database cannot be null or empty");
        }
        if (Files.notExists(Path.of(pathToSchemaFile))) {
            throw new UnsupportedOperationException("There was no Schema-Files found under: " + pathToSchemaFile);
        }
        // Setting Attributes
        this.pathToDatabase = pathToDatabase;
        this.pathToSchemaFile = pathToSchemaFile;
        // Write Schema to database
        // If Database exists, nothing will be changed (Schema stmts are "Create if not exists ..."-only)
        try (Connection con = connectToSQLite(pathToDatabase)){
            String schemaSQL = readSchemaFile(pathToSchemaFile);
            createSchemaFromSQLString(con,schemaSQL);
        } catch (SQLException sqlException) {
            logger.error("There was an SQL Error creating the Schema. Scheck your SQLFile for validity.",sqlException);
        } catch (IOException ioException) {
            logger.error("There was an error finding/reading the Schema File.",ioException);
        }
    }

    public Connection connectToSQLite(String pathToDb) throws SQLException {
        String url = "jdbc:sqlite:"+pathToDb;
        Properties props = new Properties();

        Connection conn = DriverManager.getConnection(url,props);
        return conn;
    }

    @Override
    public void writeManifest(List<TransformationResult> transformations) {
        // Schema is created on startup

        // Sort relevant items to write beforehand, derive them from TransformationResults
        List<String> transformationNames =
                transformations.stream().map(r -> r.getTransformationName()).distinct().collect(Collectors.toList());
        List<TransformationCategory> categories =
                transformations.stream().flatMap(r -> r.getCategories().stream()).distinct().collect(Collectors.toList());
        List<CtElement> positionElements =
                transformations.stream().map(r -> r.getTransformedElement()).collect(Collectors.toList());

        try (Connection con = connectToSQLite(pathToDatabase)) {
            // For all items check
            // Insert Transformation Names,
            writeTransformationNames(con,transformationNames);
            // Insert Category Names,
            writeCategoryNames(con,categories);
            // Insert Positions
            writePositions(con,positionElements);

            // Create a Map to Lookup Name -> oid
            this.name_lookup = buildTransformationNameLookup(con);
            // Create a Map to lookup Category -> oid
            this.category_lookup = buildCategoryLookup(con);
            // Create a Map to lookup position -> oid
            this.position_lookup = createPositionLookup(con,positionElements);

            // Fill the Name-Category-Mapping Table
            writeNameToCategoryMapping(con,transformations);

            // iterate over all items and write to transformations_table
            writeTransformations(con,transformations);

            // Extra: Add a version / info table
            writeExtraInfo(con);
        } catch (SQLException throwables) {
            logger.error("There was an error writing the manifest to SQLite",throwables);
        }
    }

    /**
     * This method adds the JavaObfuscator-Version and the Date of Write to the "info" Table.
     * This method can be freely left out or enhanced, it does not contribute to any logic
     * and is only providing information for later debugging / archiving.
     * @param con the connection to the SQLite database
     * @throws SQLException
     */
    private void writeExtraInfo(Connection con) throws SQLException {
        con.prepareStatement("INSERT INTO info (info_key,info_value) " +
                "VALUES ('java_obfuscator_version'," + App.configuration.get("version") + ")" +
                ",('obfuscator_touched'," + Instant.now().toString() + ")" +
                ",('obfuscator_seed'," + App.globalRandomSeed + ");").execute();
    }

    /**
     * Writes the Transformations into the SQLite Database.
     * Requires the lookup tables to be filled beforehand.
     *
     * TODO: Do proper Batch-writing if its slow?
     *
     * @param con The SQLite Database Connection to Write to
     * @param results All transformations to be written to the Database
     * @throws SQLException whenever something with the connection is wrong.
     */
    private void writeTransformations(Connection con, List<TransformationResult> results) throws SQLException {
        if (name_lookup == null || position_lookup == null) {
            throw new UnsupportedOperationException("Either Position or Name Lookup are not initialized - cannot insert transformations.");
        }

        final String INSERT_TRANSFORMATION_SQL = "INSERT INTO transformations (name_reference,position_reference) VALUES (?,?);";
        var insertTransformationStmt = con.prepareStatement(INSERT_TRANSFORMATION_SQL);

        for (TransformationResult r : results) {
            insertTransformationStmt.setLong(1,name_lookup.get(r.getTransformationName()));
            insertTransformationStmt.setLong(2,position_lookup.get(r.getTransformedElement()));
            insertTransformationStmt.execute();
        }
    }

    private String readSchemaFile(String pathToDBSchema) throws IOException {
        String schemaSQL = Files.readString(Path.of(pathToDBSchema));
        return schemaSQL;
    }

    private void createSchemaFromSQLString(Connection con, String schemaSQL) throws SQLException {
        var parts = schemaSQL.split(";");
        logger.debug("There were " + parts.length + " SQL-Statements in the given file.");
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
     *
     * TODO: Batch Writing here, if it's slow
     */
    private void writePositions(Connection con, Collection<CtElement> elementWithPosition) throws SQLException {
        final String INSERT_POSITION_SQL = "INSERT INTO positions " +
                "(simple_class_name,fully_qualified_class_name,file_name,method_name,full_method_name) " +
                "VALUES (?,?,?,?,?);";

        var insertPositionStmt = con.prepareStatement(INSERT_POSITION_SQL);

        // For every element, get the items as precise as possible
        for (CtElement elem : elementWithPosition) {
            // Initialize variables empty
            String className,fullClassName,file,methodName,fullMethodName;
            file = elem.getPosition().getFile().getAbsolutePath();
            // Set the names according to their respective element
            if (elem instanceof CtClass) {
                // Check for classes - they have no method names
                CtClass elemCasted = (CtClass) elem;
                className = elemCasted.getSimpleName();
                fullClassName = elemCasted.getQualifiedName();
                // set methodNames to default
                methodName = "NONE";
                fullMethodName = "NONE";
            } else if (elem instanceof CtMethod) {
                // Check for methods - go to parents for getting class names
                CtMethod elemCasted = (CtMethod) elem;
                methodName = elemCasted.getSimpleName();
                fullMethodName = elemCasted.getSignature();
                // Get the class in which the method is defined
                CtClass methodParent = elemCasted.getParent(u -> u instanceof CtClass);
                className = methodParent.getSimpleName();
                fullClassName = methodParent.getQualifiedName();
            } else {
                // This is the case for everything below method
                // Get the method in which the transformation happened
                CtMethod methodChanged = elem.getParent(p -> p instanceof CtMethod);
                methodName = methodChanged.getSimpleName();
                fullMethodName = methodChanged.getSignature();
                // Get the class in which the transformation happened
                CtClass methodParent = methodChanged.getParent(u -> u instanceof CtClass);
                className = methodParent.getSimpleName();
                fullClassName = methodParent.getQualifiedName();
            }

            insertPositionStmt.setString(1,className);
            insertPositionStmt.setString(2,fullClassName);
            insertPositionStmt.setString(3,file);
            insertPositionStmt.setString(4,methodName);
            insertPositionStmt.setString(5,fullMethodName);

            insertPositionStmt.execute();
        }
    }

    private Map<CtElement,Long> createPositionLookup(Connection con, Collection<CtElement> elementToBeInMapping) throws SQLException {
        /* This is a bit trickier than categories, some issues are:
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

        // Read all required parts of positions
        final String SELECT_POSITION_SQL =
                "SELECT ROWID, fully_qualified_class_name, full_method_name,file_name FROM positions;";

        ResultSet position_results = con.prepareStatement(SELECT_POSITION_SQL).executeQuery();

        // Check for every result, whether there is a matching element in the elements to map
        while(position_results.next()){
            Long index = position_results.getLong("ROWID");
            String positionFullClassName = position_results.getString("fully_qualified_class_name");
            String positionFullMethodName = position_results.getString("full_method_name");
            String positionFileName = position_results.getString("file_name");

            // Filter the elements for matching ones
            // for every matching one, add it to the lookup
            elementToBeInMapping.stream()
                    .filter(e -> similarPosition(e,positionFullClassName,positionFullMethodName,positionFileName))
                    .forEach(p -> lookup.put(p,index));
        }

        //There should be as many keys in the lookup as there are elements given into this method
        //A missmatch is not enough to fail the program, but warn about it
        if (elementToBeInMapping.size() != lookup.keySet().size()) {
            logger.warn("There was a miss-match in positions - some CtElements have no corresponding position");
        }

        return lookup;
    }

    /**
     * This method is a helper to compare the results of the database read to the elements attributes in order
     * to build the Position Dictionary.
     *
     * The fully qualified names are enough - the simple names are not required.
     *
     * The file can be left out i guess, but I am not sure.
     * TODO: Check whether file is necessary and benefitial
     *
     * @param elem the Element to check for it's maching position
     * @param fullClassName the full Classname read from SQLite
     * @param fullMethodName the full Method name read from SQLite, ignored if there is "NONE"
     * @param file the file reaf from SQLIte
     * @return
     */
    private boolean similarPosition(CtElement elem, String fullClassName, String fullMethodName, String file) {

        if (elem instanceof CtClass) {
            // Check for classes - they have no method names
            CtClass elemCasted = (CtClass) elem;
            return elemCasted.getQualifiedName().equalsIgnoreCase(fullClassName)
                    && elem.getPosition().getFile().getAbsolutePath().equalsIgnoreCase(file);
        } else if (elem instanceof CtMethod) {
            // Check for methods - go to parents for getting class names
            CtMethod elemCasted = (CtMethod) elem;
            String elemFullMethodName = elemCasted.getSignature();
            // Get the class in which the method is defined
            CtClass methodParent = elemCasted.getParent(u -> u instanceof CtClass);
            String elemFullClassName = methodParent.getQualifiedName();


            return elemFullClassName.equalsIgnoreCase(fullClassName)
                    && elemFullMethodName.equalsIgnoreCase(fullMethodName)
                    && elem.getPosition().getFile().getAbsolutePath().equalsIgnoreCase(file);
        } else {
            // This is the case for everything below method
            // Get the method in which the transformation happened
            CtMethod methodChanged = elem.getParent(p -> p instanceof CtMethod);
            String elemFullMethodName = methodChanged.getSignature();
            // Get the class in which the transformation happened
            CtClass methodParent = methodChanged.getParent(u -> u instanceof CtClass);
            String elemFullClassName = methodParent.getQualifiedName();

            return elemFullClassName.equalsIgnoreCase(fullClassName)
                    && elemFullMethodName.equalsIgnoreCase(fullMethodName)
                    && elem.getPosition().getFile().getAbsolutePath().equalsIgnoreCase(file);
        }
    }
    /**
     * As Categories and Transformation Names are stored separately using a mapping table,
     * the mapping table need to be filled as well.
     * The Map can easily be derived using Transformation::getName and Transformation::getCategories
     *
     * It uses the toplevel lookups, that already must be initialized when this method is run.
     */
    private void writeNameToCategoryMapping(Connection con, Collection<TransformationResult> transformations ) throws SQLException {
        if (name_lookup == null || category_lookup == null) {
           throw new UnsupportedOperationException("Either Category or Name Lookup are not initialized - cannot build mapping.");
        }

        // This Stream iterates over the transformations and creates a distinct list by Transformation Name
        // The resulting List will have one transformation of each name.
        var distinctTransformations =
                transformations.stream()
                        .collect(
                                Collectors.toMap(TransformationResult::getTransformationName, p -> p, (p, q) -> p)
                        ).values();

        Map<String, Collection<TransformationCategory>> mapping = new HashMap<>();
        for (TransformationResult r : distinctTransformations) {
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
