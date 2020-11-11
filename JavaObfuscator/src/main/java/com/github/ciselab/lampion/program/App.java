package com.github.ciselab.lampion.program;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import com.github.ciselab.lampion.manifest.ManifestWriter;
import com.github.ciselab.lampion.manifest.SqliteManifestWriter;
import com.github.ciselab.lampion.support.RandomNameFactory;
import com.github.ciselab.lampion.transformations.TransformerRegistry;
import com.github.ciselab.lampion.transformations.transformers.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Entrypoint for this program.
 *
 * Holds two global static variables:
 * - Configuration
 * - Transformer-Registry
 * See their comments for further information. They are intended for read-only purpose.
 */
public class App {

    private static Logger logger = LogManager.getLogger(App.class);

    // The global configuration used throughout the program. It is read from file
    // They only contain pairs of <String,String> (or atleast it is used that way)
    public static Properties configuration = new Properties();

    // The global registry in which every Transformation registers itself at system startup.
    // Is passed to the engine, and can be exchanged beforehand to set certain scenarios.
    // For further info, see DesignNotes.md "Registration of Transformations"
    public static TransformerRegistry globalRegistry = createDefaultRegistry();

    // Used to instantiate the random seeds of the delegated Transformers in the default TransformerRegistry
    public static final long globalRandomSeed = 2020;

    public static void main(String[] args) {
        logger.info("Starting Lampion Obfuscator");

        if (args.length == 0) {
            logger.info("Found no argument for config path - looking at default location");
            setPropertiesFromFile("./src/main/resources/config.properties");
            // start program
        } else if (args.length == 1) {
            logger.info("Received one argument - looking for properties in " + args[0]);
            setPropertiesFromFile(args[0]);
        } else {
            logger.warn("Received an unknown number of arguments! Not starting.");
            return;
        }

        Engine engine = buildEngineFromProperties(App.configuration);

        engine.run();

        logger.info("Everything done - closing Lampion Obfuscator");
    }

    /**
     * This methods tries to read the filepath and overwrites all default properties with the properties found there.
     * If there are any issues, or no properties found in the file, it fails gracefully with a warning.
     * @param filepath the filepath at which to look for the file. Both relative and absolute values are supported.
     */
    private static void setPropertiesFromFile(String filepath) {

        try {
            logger.debug("Received " + filepath + " as raw input - resolving it to be absolute");

            logger.info("Looking for Properties file @" + filepath);

            File configFile = new File(filepath);
            FileReader reader = new FileReader(configFile);
            Properties props = new Properties();
            props.load(reader);

            logger.debug("Found " + props.size() + " Properties");

            // iterate over the key-value pairs and add them to the global configuration
            for (var kv : props.entrySet()) {
                App.configuration.put(kv.getKey(),kv.getValue());
            }

            reader.close();
        } catch (FileNotFoundException ex) {
            // file does not exist
            logger.error("Property file not found at " + filepath,ex);
        } catch (IOException ex) {
            logger.error(ex);
            // I/O error
        }
    }

    /**
     * This method builds an engine according to the found / given properties
     * It will build a registry or take the default ones, look for IO-Directories and check the value correctness.
     * It returns a fully functional Engine to be run.
     *
     * Later, this function will also contain builders for more or less transformations as well as distributions.
     *
     * @param properties The key-value pairs read at system startup.
     * @return A fully configured, ready to go Engine
     * @throws UnsupportedOperationException whenever properties where missing or invalid
     */
    private static Engine buildEngineFromProperties(Properties properties){
        // Build Registry
        // Currently skipped for default
        TransformerRegistry registry = globalRegistry;

        // Read Input and Output Dir
        String inputDir,outputDir;
        if(properties.get("inputDirectory") != null){
            inputDir = (String) properties.getProperty("inputDirectory");
        } else {
            throw new UnsupportedOperationException("There was no input-directory specified in the properties");
        }
        if(properties.get("outputDirectory") != null) {
            outputDir = (String) properties.get("outputDirectory");
        } else {
            throw new UnsupportedOperationException("There was no output-directory specified in the properties");
        }

        // Build Base-Engine
        Engine engine = new Engine(inputDir,outputDir,registry);

        // Set Transformation-Scopes
        Engine.TransformationScope transformationScope = Engine.TransformationScope.global;
        long transformations = 100;
        if(properties.get("transformationscope") != null){
            Engine.TransformationScope.valueOf(properties.getProperty("transformationscope"));
        } else {
            logger.warn("There was no TransformationScope specified in the configuration - defaulting to global scope.");
        }
        if(properties.get("transformations") != null) {

        } else {
            logger.warn("There was no number of transformations specified in configuration - defaulting to " + transformations);
        }
        engine.setNumberOfTransformationsPerScope(transformations,transformationScope);

        // Read Items for SQLite
        String databaseName="TransformationManifest.db";
        String databaseDirectory="./manifest";
        String pathToSchema="./createSQLiteManifest";
        if(properties.get("databaseName") != null){
            databaseName = (String) properties.get("databaseName");
        } else {
            logger.debug("There was no DatabaseName specified in the configuration - defaulting to " + databaseName);
        }
        if(properties.get("databaseDirectory") != null){
            databaseDirectory = (String) properties.get("databaseDirectory");
        } else {
            logger.debug("There was no Directory specified to write manifest in the configuration - defaulting to " + databaseDirectory);
        }
        if(properties.get("pathToSchema") != null){
            pathToSchema = (String) properties.get("pathToSchema");
        } else {
            // The check for Schema validity and existance is done in the constructor of SQLite Writer
            logger.warn("There was no SchemaPath specified in the configuration - defaulting to "+pathToSchema);
        }

        String fullDatabasePath = databaseDirectory.endsWith("/") ? databaseDirectory+databaseName : databaseDirectory+"/"+databaseName;
        logger.debug("Full SQLite-Database Path: " + fullDatabasePath);

        // Build SQLite Writer Schema
        ManifestWriter writer = new SqliteManifestWriter(pathToSchema,fullDatabasePath);

        // Socket Writer into Base Engine
        engine.setManifestWriter(writer);

        // Set Seed(s)
        long seed = globalRandomSeed;
        if(properties.get("seed") != null){
            seed = Long.parseLong((String) properties.get("seed"));
        } else {
            logger.warn("There was no Seed specified - defaulting to " + seed);
        }

        engine.setRandomSeed(seed);
        RandomNameFactory.setSeed(seed);
        for(var t: registry.getRegisteredTransformers()){
            t.setSeed(seed);
        }

        // Alter / Change Distributions
        // Currently skipped

        // Return the build engine
        return engine;
    }

    /*
    The Code below covers an issue found with the runtime an reading the packages.
    See "AppTests::testDefaultRegistry_ShouldNotBeEmpty" for a broader explanation
     */

    private static TransformerRegistry createDefaultRegistry() {
        TransformerRegistry registry = new TransformerRegistry("default");

        registry.registerTransformer(new IfTrueTransformer(globalRandomSeed));

        // There are many issues with this one ...
        //registry.registerTransformer(new LambdaIdentityTransformer(globalRandomSeed));
        registry.registerTransformer(new RandomInlineCommentTransformer(globalRandomSeed));
        registry.registerTransformer(new RandomParameterNameTransformer(globalRandomSeed));
        registry.registerTransformer(new EmptyMethodTransformer(globalRandomSeed));

        return registry;
    }

}
