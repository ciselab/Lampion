package com.github.ciselab.lampion.exploration;

import com.github.ciselab.lampion.manifest.SqliteManifestWriter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * These tests are to "explore" SQLite, which is obviously not a heavy technology or anything,
 * But I wanted to have my "playing around" with it documented and kept here.
 *
 * The following this should be done to be ready for copy pasting:
 *
 * TODO:
 * - Open Connection
 * - Create Schema
 * - Create Schema from File
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
 * Solve it using:
 * mvn clean dependency:purge-local-repository
 * This will re-download all your dependencies.
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
    void testCreateDatabase_shouldBeCreated() throws ClassNotFoundException {
        String dbName = "explore_creation.db";

        Class.forName("org.sqlite.JDBC");

        String url = "jdbc:sqlite:"+ pathToDatabase + dbName;

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            fail();
        }

    }

}
