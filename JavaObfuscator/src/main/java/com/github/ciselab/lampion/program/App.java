package com.github.ciselab.lampion.program;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class App {

    private static Logger logger = LogManager.getLogger(App.class);

    public static Properties configuration = new Properties();

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

            logger.info("Found " + props.size() + " Properties");

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
