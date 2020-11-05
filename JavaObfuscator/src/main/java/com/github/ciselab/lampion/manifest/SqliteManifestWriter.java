package com.github.ciselab.lampion.manifest;

import com.github.ciselab.lampion.transformations.TransformationResult;

import java.util.List;

public class SqliteManifestWriter implements ManifestWriter {

    public SqliteManifestWriter(){
        // Check if the SQLite JDBC is properly configured an in Path
        // This is a regression check
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

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
}
