package com.github.ciselab.lampion.manifest;

import com.github.ciselab.lampion.transformations.TransformationResult;

import java.util.List;

/**
 * This interface is used to write the transformation-results in an orderly manner to disk.
 *
 * The interface is used to introduce a separation layer for testing, as well as for exchange-ability of
 * SQLite and (other) SQL-databases.
 *
 * The writer should, in it's constructor, set up all required steps such as building the database schema or open a connection.
 *
 * See DesignNotes.md "Representation on Disk" for some insight on design choices.
 */
public interface ManifestWriter {

    void writeManifest(List<TransformationResult> results);

}
