package com.github.ciselab.lampion.exploration;

import com.github.ciselab.lampion.manifest.SqliteManifestWriter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * These tests are to "explore" SQLite, which is obviously not a heavy technology or anything,
 * But I wanted to have my "playing around" with it documented and kept here.
 *
 * There was an issue with creating all tables at once. This is due to jdbc aborting // returning after the first statement.
 * https://stackoverflow.com/questions/10797794/multiple-queries-executed-in-java-in-single-statement
 * There is a "allowMultiQueries"-property, but that is only and explicitly available for mysql (and hence mariadb).
 * For SQLite, there is no such thing.
 * Mitigate splitting the statements by ; and run them after each other. Yikes.
 *
 * The following this should be done to be ready for copy pasting:
 *
 * TODO:
 * - Insert Value
 * - Read Value
 * - All with Prepared Statements
 * - Close connection
 *
 * Further Reading:
 * - https://www.sqlitetutorial.net/sqlite-java/        Tutorial Site
 * - https://github.com/xerial/sqlite-jdbc              Repo of driver
 *
 * Troubleshooting:
 *
 * There was the following issue with first running the SQLLite Exploration Tests:
 * "No suitable driver found for jdbc:sqlite:./src/test/resources/sql_exploration/explore_creation.db"
 * This happens if there is no sqlite-driver jar in your classpath (https://stackoverflow.com/questions/16725377/no-suitable-driver-found-sqlite)
 * Solve it in IntelliJ doing:
 *  1. right click the JavaObfuscator in Project Explorer
 *  2. F4 (Open Module Info)
 *  3. Navigate to Libraries, press +
 *  4. "new Maven dependency"
 *  5. Search for org.xerial.sqlite-jdbc
 *  6. Press Apply
 */
public class SqliteTests {

    String pathToDatabase = "./src/test/resources/sql_exploration/";

    @Tag("Exploration")
    @Tag("Regression")
    @Test
    void testCheckForSQLite_DriverIsInClassPath() throws ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        return;
    }

    @Tag("Exploration")
    @Tag("File")
    @Tag("System")
    @Test
    void testCreateDatabase_shouldBeCreated() throws SQLException, IOException {
        String dbName = "explore_creation.db";

        //Short check whether there was proper cleanup
        assertFalse(Files.exists(Path.of(pathToDatabase,dbName)));

        String url = "jdbc:sqlite:"+ pathToDatabase + dbName;

        Connection conn = DriverManager.getConnection(url);
        if (conn != null) {
            DatabaseMetaData meta = conn.getMetaData();
        }
        // Without the close, the files cannot be deleted
        conn.close();

        //Check the database was created
        assertTrue(Files.exists(Path.of(pathToDatabase,dbName)));

        // Cleanup
        Files.delete(Path.of(pathToDatabase,dbName));
    }

    @Tag("Exploration")
    @Tag("File")
    @Test
    void testReadLink_CheckIfALinkedSQLFileCanBeRead() throws IOException {
        String pathToDBSchema = "./src/main/resources/createManifestTables.sql";

        assertTrue(Files.exists(Path.of(pathToDBSchema)));

        String content = Files.readString(Path.of(pathToDBSchema));

        assertNotNull(content);

        assertTrue(content.contains("CREATE TABLE"));
    }

    @Tag("Exploration")
    @Test
    void createInMemDb() throws SQLException {
        String url = "jdbc:sqlite::memory:";

        Connection conn = DriverManager.getConnection(url);
        if (conn != null) {
            DatabaseMetaData meta = conn.getMetaData();
        }

        conn.close();

    }

    @Tag("Exploration")
    @Test
    void testAllowMultiQueries_mitigateUsingSplit() throws SQLException, IOException{
        String dbName = "multiqueries_split.db";

        assertFalse(Files.exists(Path.of(pathToDatabase,dbName)));

//useUnicode=true&characterEncoding=utf8&allowMultiQueries=true

        String url = "jdbc:sqlite:"+pathToDatabase+dbName;
        Properties props = new Properties();

        Connection conn = DriverManager.getConnection(url,props);
        if (conn != null) {
            DatabaseMetaData meta = conn.getMetaData();

            String multiquerySQL = "CREATE TABLE some ( a TEXT); INSERT INTO some (a) VALUES ('hello');";

            var parts = multiquerySQL.split(";");

            for (var p : parts){
                conn.prepareStatement(p).execute();
            }
            //var res = conn.prepareStatement(multiquerySQL).execute();

            String checkSQL = "SELECT a from some;";
            var results = conn.prepareStatement(checkSQL).executeQuery();

           assertEquals("hello",results.getString("a"));
        }
        // Without the close, the files cannot be deleted
        conn.close();

        // Cleanup
        Files.delete(Path.of(pathToDatabase,dbName));
    }

    private void fireMultiStatement(String sqlWithMultiStatement,Connection con) throws SQLException {
        var parts = sqlWithMultiStatement.split(";");

        for (var p : parts){
            con.prepareStatement(p).execute();
        }
    }

    @Tag("Exploration")
    @Test
    void testCreateSchema_WithInMemoryDatabase_shouldHaveTables() throws SQLException, IOException {
        String url = "jdbc:sqlite::memory:";
        String pathToDBSchema = "./src/main/resources/createManifestTables.sql";

        Properties props = new Properties();

        assertTrue(Files.exists(Path.of(pathToDBSchema)));

        String schemaSQL = Files.readString(Path.of(pathToDBSchema));

        Connection conn = DriverManager.getConnection(url,props);
        if (conn != null) {
            fireMultiStatement(schemaSQL,conn);

            //Check Table Existance
            String infoSQL = "SELECT name FROM sqlite_master WHERE type='table' AND name='info'";
            var tableStmt = conn.prepareStatement(infoSQL);
            var tableRes = tableStmt.executeQuery();
            assertEquals("info",tableRes.getString("name"));

            //Check Table Existance
            String transformSQL = "SELECT name FROM sqlite_master WHERE type='table' AND name='transformations'";
            var transStmt = conn.prepareStatement(transformSQL);
            var transRes = transStmt.executeQuery();
            assertEquals("transformations",transRes.getString("name"));
        }
        // Without the close, the files cannot be deleted
        conn.close();
    }

    @Tag("Exploration")
    @Test
    void testCreateSchema_WithInMemoryDatabase_VersionIsSet() throws SQLException, IOException {
        String url = "jdbc:sqlite::memory:";
        String pathToDBSchema = "./src/main/resources/createManifestTables.sql";

        Properties props = new Properties();

        assertTrue(Files.exists(Path.of(pathToDBSchema)));

        String schemaSQL = Files.readString(Path.of(pathToDBSchema));

        Connection conn = DriverManager.getConnection(url,props);
        if (conn != null) {
            fireMultiStatement(schemaSQL,conn);

            //Check Table Existance
            String infoSQL = "SELECT info_value FROM info WHERE info_key='schema_version'";
            var tableStmt = conn.prepareStatement(infoSQL);
            var tableRes = tableStmt.executeQuery();
            assertEquals("1.0",tableRes.getString("info_value"));
        }
        // Without the close, the files cannot be deleted
        conn.close();
    }


    @Tag("Exploration")
    @Test
    void testInsertValuesWithPreparedStatement_CategoryIsWritten() throws SQLException, IOException {
        String url = "jdbc:sqlite::memory:";
        String pathToDBSchema = "./src/main/resources/createManifestTables.sql";

        String schemaSQL = Files.readString(Path.of(pathToDBSchema));

        Connection conn = DriverManager.getConnection(url);
        if (conn != null) {
            fireMultiStatement(schemaSQL,conn);

            String insertCategorySQL = "INSERT INTO transformation_categories (category_name) VALUES (?);";
            var prep = conn.prepareStatement(insertCategorySQL);
            prep.setString(1,"TestCategory");
            prep.execute();

            String seeAllCategoriesSQL = "SELECT ROWID, category_name FROM transformation_categories;";
            var preppedRead = conn.prepareStatement(seeAllCategoriesSQL);

            var readResult = preppedRead.executeQuery();

            assertEquals("TestCategory",readResult.getString("category_name"));
            assertEquals(1,readResult.getInt("ROWID"));
        }
        // Without the close, the files cannot be deleted
        conn.close();
    }

    @Tag("Exploration")
    @Test
    void testInsertMultipleCategories_BuildRowIdLookup_LookupHasAllElements() throws SQLException, IOException {
        /**
         * To check what the response is when I do an insert statement.
         */
        String url = "jdbc:sqlite::memory:";
        String pathToDBSchema = "./src/main/resources/createManifestTables.sql";

        String[] categoriesToWrite = new String[]{
                "Test1", "Test2", "Test3"
        };

        String schemaSQL = Files.readString(Path.of(pathToDBSchema));

        Connection conn = DriverManager.getConnection(url);
        if (conn != null) {
            fireMultiStatement(schemaSQL,conn);

            String insertCategorySQL = "INSERT INTO transformation_categories (category_name) VALUES (?);";
            var prep = conn.prepareStatement(insertCategorySQL);
            for(String category : categoriesToWrite) {
                prep.setString(1,category);
                prep.execute();
            }

            String seeAllCategoriesSQL = "SELECT ROWID, category_name FROM transformation_categories;";
            var preppedRead = conn.prepareStatement(seeAllCategoriesSQL);

            var readResult = preppedRead.executeQuery();

            Map<String,Long> lookup = new HashMap<>();

            while(readResult.next()){
                lookup.put(readResult.getString("category_name"),readResult.getLong("ROWID"));
            }

            assertEquals(categoriesToWrite.length, lookup.entrySet().size());
        }
        // Without the close, the files cannot be deleted
        conn.close();
    }

}
