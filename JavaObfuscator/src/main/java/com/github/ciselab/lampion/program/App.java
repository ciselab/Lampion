package com.github.ciselab.lampion.program;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import com.github.ciselab.lampion.transformations.TransformerRegistry;
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
    public static TransformerRegistry globalRegistry = new TransformerRegistry("default");
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

        System.out.println("Hello "+ configuration.get("greeting")+"!");

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
}
